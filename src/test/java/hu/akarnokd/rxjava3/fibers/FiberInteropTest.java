/*
 * Copyright 2019 David Karnok
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

import static org.testng.Assert.assertTrue;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import io.reactivex.rxjava3.functions.Consumer;

@Test
public class FiberInteropTest {

    @Test
    public void checkClass() {
        TestHelper.checkUtilityClass(FiberInterop.class);
    }

    @Test
    public void checkIsInsideFiber() {
        try (var scope = Executors.newVirtualThreadPerTaskExecutor()) {
            FiberInterop.create(emitter -> {
                emitter.emit(Thread.currentThread().isVirtual());
            }, scope)
            .test()
            .awaitDone(5, TimeUnit.SECONDS)
            .assertResult(true);
        }
    }

    @Test
    public void checkIsInsideFiberExec() throws Throwable {
        try (var exec = Executors.newSingleThreadExecutor()) {
            FiberInterop.create(emitter -> {
                emitter.emit(Thread.currentThread().isVirtual());
            }, exec)
            .test()
            .awaitDone(5, TimeUnit.SECONDS)
            .assertResult(false);

            exec.shutdown();
        }
    }

    @Test
    public void plainVirtual() {
        var result = new AtomicReference<Boolean>();
        try (var scope = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())) {
            scope.submit(() -> result.set(Thread.currentThread().isVirtual()));
        }

        assertTrue(result.get());
    }

    static void withVirtual(Consumer<ExecutorService> call) throws Throwable {
        try (var exec = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())) {
            call.accept(exec);
        }
    }
}
