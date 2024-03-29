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

/**
 * Interface handed to user code in {@link FiberInterop#create(FiberGenerator, java.util.concurrent.ExecutorService)} callback.
 * @param <T> the element type to emit
 */
@FunctionalInterface
public interface FiberEmitter<T> {

    /**
     * Signal the next item
     * @param item the item to signal
     * @throws Throwable an arbitrary exception if the downstream cancelled
     */
    void emit(T item) throws Throwable;
}
