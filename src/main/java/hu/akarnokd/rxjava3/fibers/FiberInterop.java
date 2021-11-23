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

import java.util.Objects;
import java.util.concurrent.*;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.internal.functions.ObjectHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * Sources, transformers and consumers working with fiber-based suspendable methods.
 * 
 * @since 0.0.1
 */
public final class FiberInterop {

    /** Utility class. */
    private FiberInterop() {
        throw new IllegalStateException("No instances!");
    }

    public static <T> Flowable<T> create(FiberGenerator<T> generator, ExecutorService executor) {
        Objects.requireNonNull(generator, "generator is null");
        Objects.requireNonNull(executor, "executor is null");
        return RxJavaPlugins.onAssembly(new FlowableCreateFiberExecutor<>(generator, executor));
    }

    public static <T, R> FlowableTransformer<T, R> transform(FiberTransformer<T, R> transformer, ExecutorService scheduler) {
        return transform(transformer, scheduler, Flowable.bufferSize());
    }

    public static <T, R> FlowableTransformer<T, R> transform(FiberTransformer<T, R> transformer, ExecutorService scheduler, int prefetch) {
        Objects.requireNonNull(transformer, "transformer is null");
        Objects.requireNonNull(scheduler, "scheduler is null");
        ObjectHelper.verifyPositive(prefetch, "prefetch");
        return new FlowableTransformFiberExecutor<>(null, transformer, scheduler, prefetch);
    }
}
