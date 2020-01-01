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

import java.util.concurrent.*;

import org.reactivestreams.*;

import hu.akarnokd.rxjava3.fibers.FlowableTransformFiberScheduler.TransformFiberSubscriber;
import io.reactivex.rxjava3.core.*;

final class FlowableTransformFiberExecutor<T, R> extends Flowable<R>
implements FlowableTransformer<T, R> {

    final Flowable<T> source;

    final FiberTransformer<T, R> transformer;

    final ExecutorService executor;

    final int prefetch;

    FlowableTransformFiberExecutor(Flowable<T> source,
            FiberTransformer<T, R> transformer, ExecutorService executor, int prefetch) {
        this.source = source;
        this.transformer = transformer;
        this.executor = executor;
        this.prefetch = prefetch;
    }

    @Override
    public Publisher<R> apply(Flowable<T> upstream) {
        return new FlowableTransformFiberExecutor<>(upstream, transformer, executor, prefetch);
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        var parent = new ExecutorTransformFiberSubscriber<>(s, transformer, prefetch);
        source.subscribe(parent);
        executor.submit(parent);
    }

    static final class ExecutorTransformFiberSubscriber<T, R> extends TransformFiberSubscriber<T, R> {

        private static final long serialVersionUID = 6360560993564811498L;

        ExecutorTransformFiberSubscriber(Subscriber<? super R> downstream,
                FiberTransformer<T, R> transformer,
                int prefetch) {
            super(downstream, transformer, prefetch);
        }

        @Override
        protected void cleanup() {
        }
    }
}
