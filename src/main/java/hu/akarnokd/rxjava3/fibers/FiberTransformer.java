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
 * Interface called by the {@link FiberInterop#transform(FiberTransformer, java.util.concurrent.ExecutorService)}
 * operator to generate any number of output values based of the current input of the upstream.
 *
 * @param <T> the source value type
 * @param <R> the result value type
 */
@FunctionalInterface
public interface FiberTransformer<T, R> {

    /**
     * Implement this method to generate any number of items via
     * {@link FiberEmitter#emit(Object)}.
     * 
     * @param value the upstream value
     * @param emitter the emitter to use to generate result value(s)
     * @throws Throwable signaled as {@code onError} for the downstream.
     */
    void transform(T value, FiberEmitter<R> emitter) throws Throwable;
}
