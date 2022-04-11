/*
 * Copyright 2019-Present David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.akarnokd.rxjava3.fibers;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * Fundamental primitive for suspending and resuming a Thread.
 * @since 0.0.1
 */
public class ResumableFiber extends AtomicReference<Object> {

    private static final long serialVersionUID = -3462467580179834124L;

    /**
     * Indicates the await can resume without parking the fiber.
     */
    static final Object READY = "Ready";

    /**
     * Wait for a resumption by a {@link #resume()} call.
     * This method won't suspend the current Fiber if there was already
     * a resume indication.
     */
    public final void await() {
        Thread toUnpark = Thread.currentThread();

        for (;;) {
            var current = get();
            if (current == READY) {
                break;
            }

            if (current != null && current != toUnpark) {
                throw new IllegalStateException("Only one (Virtual)Thread can await this ResumableFiber!");
            }

            if (compareAndSet(null, toUnpark)) {
                LockSupport.park();
                // we don't just break here because park() can wake up spuriously
                // if we got a proper resume, get() == READY and the loop will quit above
            }
        }
        // clear the resume indicator so that the next await call will park without a resume()
        clear();
    }

    /**
     * Clears any resumption/ready object from this ResumableFiber.
     */
    public final void clear() {
        getAndSet(null);
    }

    /**
     * Trigger a resumption of a fiber suspended in {@link #await()}.
     * This method can be called from multiple threads and multiple times.
     * Note that this method is not guaranteed to act as a full memory barrier
     * if there was a resume() call previously and the suspend side didn't suspend yet.
     */
    public final void resume() {
        if (get() != READY) {
            var old = getAndSet(READY);
            if (old != READY) {
                LockSupport.unpark((Thread)old);
            }
        }
    }
}
