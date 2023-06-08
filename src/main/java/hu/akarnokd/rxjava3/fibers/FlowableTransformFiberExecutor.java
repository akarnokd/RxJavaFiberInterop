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
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.*;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.operators.SpscArrayQueue;

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

    static final class ExecutorTransformFiberSubscriber<T, R> extends AtomicLong
    implements FlowableSubscriber<T>, Subscription, FiberEmitter<R>, Callable<Void> {

        private static final long serialVersionUID = -4702456711290258571L;

        Subscriber<? super R> downstream;

        final FiberTransformer<T, R> transformer;

        final int prefetch;

        final AtomicLong requested;

        final ResumableFiber producerReady;

        final ResumableFiber consumerReady;

        final SpscArrayQueue<T> queue;

        Subscription upstream;

        volatile boolean done;
        Throwable error;

        volatile boolean cancelled;

        static final Throwable STOP = new Throwable("Downstream cancelled");

        long produced;

        ExecutorTransformFiberSubscriber(Subscriber<? super R> downstream,
                FiberTransformer<T, R> transformer,
                int prefetch) {
            this.downstream = downstream;
            this.transformer = transformer;
            this.prefetch = prefetch;
            this.requested = new AtomicLong();
            this.producerReady = new ResumableFiber();
            this.consumerReady = new ResumableFiber();
            this.queue = new SpscArrayQueue<>(prefetch);
        }

        @Override
        public void onSubscribe(Subscription s) {
            upstream = s;
            downstream.onSubscribe(this);
            s.request(prefetch);
        }

        @Override
        public void onNext(T t) {
            queue.offer(t);
            if (getAndIncrement() == 0) {
                producerReady.resume();
            }
        }

        @Override
        public void onError(Throwable t) {
            error = t;
            onComplete();
        }

        @Override
        public void onComplete() {
            done = true;
            if (getAndIncrement() == 0) {
                producerReady.resume();
            }
        }

        @Override
        public void emit(R item) throws Throwable {
            Objects.requireNonNull(item, "item is null");

            var p = produced;
            while (requested.get() == p && !cancelled) {
                consumerReady.await();
            }

            if (cancelled) {
                throw STOP;
            }

            downstream.onNext(item);

            produced = p + 1;
        }

        @Override
        public void request(long n) {
            Helpers.add(requested, n);
            consumerReady.resume();
        }

        @Override
        public void cancel() {
            cancelled = true;
            upstream.cancel();
            // cleanup(); don't kill the worker

            producerReady.resume();
            consumerReady.resume();
        }

        @Override
        public Void call() {
            try {
                try {
                    var consumed = 0L;
                    var limit = prefetch - (prefetch >> 2);
                    var wip = 0L;

                    while (!cancelled) {
                        var d = done;
                        var v = queue.poll();
                        var empty = v == null;

                        if (d && empty) {
                            var ex = error;
                            if (ex != null) {
                                downstream.onError(ex);
                            } else {
                                downstream.onComplete();
                            }
                            break;
                        }

                        if (!empty) {

                            if (++consumed == limit) {
                                consumed = 0;
                                upstream.request(limit);
                            }

                            transformer.transform(v, this);

                            continue;
                        }

                        wip = addAndGet(-wip);
                        if (wip == 0L) {
                            producerReady.await();
                        }
                    }
                } catch (Throwable ex) {
                    if (ex != STOP && !cancelled) {
                        upstream.cancel();
                        downstream.onError(ex);
                    }
                    return null;
                }
            } finally {
                queue.clear();
                downstream = null;
            }
            return null;
        }
    }
}
