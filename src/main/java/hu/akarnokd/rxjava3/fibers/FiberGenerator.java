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
 * Interface to implement to produce elements when asked by
 * {@link FiberInterop#create(FiberGenerator, java.util.concurrent.ExecutorService)}.
 * <p>
 * To signal {@code onComplete}, return normally from {@link #generate(FiberEmitter)}.
 * To signal {@code onError}, throw any exception from {@link #generate(FiberEmitter)}.
 * @param <T> the element type generated
 */
@FunctionalInterface
public interface FiberGenerator<T> {

    /**
     * The method to implement and start emitting items.
     * @param emitter use {@link FiberEmitter#emit(Object)} to generate values
     * @throws Throwable if the generator wishes to signal {@code onError}.
     */
    void generate(FiberEmitter<T> emitter) throws Throwable;
}
