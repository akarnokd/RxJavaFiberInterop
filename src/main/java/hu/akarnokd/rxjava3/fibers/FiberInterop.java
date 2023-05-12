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

import java.util.Objects;
import java.util.concurrent.*;

import io.reactivex.rxjava3.annotations.BackpressureKind;
import io.reactivex.rxjava3.annotations.BackpressureSupport;
import io.reactivex.rxjava3.annotations.SchedulerSupport;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.internal.functions.ObjectHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;

/**
 * Sources and transformers working with
 * virtual thread-based executors that allow
 * efficient blocking via suspensions and resumptions.
 * <p>
 * Examples:
 * <pre><code>
 * try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
 *     FiberInterop.<Integer>create(emitter -> {
 *         for (int i = 0; i &lt; 10; i++) {
 *             Thread.sleep(1000);
 *             emitter.emit(i);
 *         }
 *     }, executor)
 *     .subscribe(
 *         System.out::println,
 *         Throwable::printStackTrace,
 *         () -> System.out.println("Done")
 *     );
 * }
 * </code></pre>
 * @since 0.0.1
 */
public final class FiberInterop {

    /** Utility class. */
    private FiberInterop() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Construct a {@link Flowable} and use the given {@code generator}
     * to generate items on demand while running on the given {@link ExecutorService}.
     * <p>
     * Note that backpressure is handled via blocking so it is recommended the provided
     * {@code ExecutorService} uses virtual threads, such as the one returned by
     * {@link Executors#newVirtualThreadPerTaskExecutor()}.
     * @param <T> the element type to emit
     * @param generator the callback used to generate items on demand by the downstream
     * @param executor the target {@code ExecutorService} to use for running the callback
     * @return the new {@code Flowable} instance
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public static <T> Flowable<T> create(FiberGenerator<T> generator, ExecutorService executor) {
        Objects.requireNonNull(generator, "generator is null");
        Objects.requireNonNull(executor, "executor is null");
        return RxJavaPlugins.onAssembly(new FlowableCreateFiberExecutor<>(generator, executor));
    }

    /**
     * Construct a transformer to be used via {@link Flowable#compose(FlowableTransformer)}
     * which can turn an upstream item into zero or more downstream values by running
     * on the given {@link ExecutorService}.
     * <p>
     * Note that backpressure is handled via blocking so it is recommended the provided
     * {@code ExecutorService} uses virtual threads, such as the one returned by
     * {@link Executors#newVirtualThreadPerTaskExecutor()}.
     * @param <T> the upstream element type
     * @param <R> the downstream element type
     * @param transformer the callback whose {@link FiberTransformer#transform(Object, FiberEmitter)} is invoked for each upstream item
     * @param executor the target {@code ExecutorService} to use for running the callback
     * @return the new {@code FlowableTransformer} instance
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public static <T, R> FlowableTransformer<T, R> transform(FiberTransformer<T, R> transformer, ExecutorService executor) {
        return transform(transformer, executor, Flowable.bufferSize());
    }

    /**
     * Construct a transformer to be used via {@link Flowable#compose(FlowableTransformer)}
     * which can turn an upstream item into zero or more downstream values by running
     * on the given {@link ExecutorService}.
     * <p>
     * Note that backpressure is handled via blocking so it is recommended the provided
     * {@code ExecutorService} uses virtual threads, such as the one returned by
     * {@link Executors#newVirtualThreadPerTaskExecutor()}.
     * @param <T> the upstream element type
     * @param <R> the downstream element type
     * @param transformer the callback whose {@link FiberTransformer#transform(Object, FiberEmitter)} is invoked for each upstream item
     * @param executor the target {@code ExecutorService} to use for running the callback
     * @param prefetch the number of items to fetch from the upstream.
     * @return the new {@code FlowableTransformer} instance
     */
    @BackpressureSupport(BackpressureKind.FULL)
    @SchedulerSupport(SchedulerSupport.CUSTOM)
    public static <T, R> FlowableTransformer<T, R> transform(FiberTransformer<T, R> transformer, ExecutorService executor, int prefetch) {
        Objects.requireNonNull(transformer, "transformer is null");
        Objects.requireNonNull(executor, "executor is null");
        ObjectHelper.verifyPositive(prefetch, "prefetch");
        return new FlowableTransformFiberExecutor<>(null, transformer, executor, prefetch);
    }
}
