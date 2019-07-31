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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.*;

import org.reactivestreams.*;

import io.reactivex.*;
import io.reactivex.Scheduler.Worker;
import io.reactivex.internal.util.BackpressureHelper;

/**
 * Runs a generator callback on a Fiber backed by a Worker of the given scheduler
 * and signals events emitted by the generator considering any downstream backpressure.
 *
 * @param <T> the element type of the flow
 */
final class FlowableCreateFiberScheduler<T> extends Flowable<T> {

    final FiberGenerator<T> generator;

    final Scheduler scheduler;

    FlowableCreateFiberScheduler(FiberGenerator<T> generator, Scheduler scheduler) {
        this.generator = generator;
        this.scheduler = scheduler;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        var worker = scheduler.createWorker();
        var parent = new CreateFiberSubscription<>(s, worker, generator);
        s.onSubscribe(parent);

        var fiber = FiberScope.background().schedule(worker::schedule, parent);
        parent.setFiber(fiber);
    }

    static final class CreateFiberSubscription<T> extends AtomicLong implements Subscription, Callable<Void>, FiberEmitter<T> {

        private static final long serialVersionUID = -6959205135542203083L;

        final Subscriber<? super T> downstream;

        final Worker worker;

        final FiberGenerator<T> generator;

        final AtomicReference<Object> fiber;

        final ResumableFiber consumerReady;

        volatile Throwable stop;

        static final Throwable STOP = new Throwable("Downstream cancelled");

        long produced;

        CreateFiberSubscription(Subscriber<? super T> downstream, Worker worker, FiberGenerator<T> generator) {
            this.downstream = downstream;
            this.worker = worker;
            this.generator = generator;
            this.fiber = new AtomicReference<>();
            this.consumerReady = new ResumableFiber();
        }

        @Override
        public Void call() {
            try {
                try {
                    generator.generate(this);
                } catch (Throwable ex) {
                    if (ex != STOP) {
                        downstream.onError(ex);
                    }
                    return null;
                }
                var s = stop;
                if (s != STOP) {
                    if (s == null) {
                        downstream.onComplete();
                    } else {
                        downstream.onError(s);
                    }
                }
            } finally {
                fiber.set(this);
                worker.dispose();
            }
            return null;
        }

        @Override
        public void request(long n) {
            if (BackpressureHelper.add(this, n) == 0L) {
                consumerReady.resume();
            }
        }

        @Override
        public void cancel() {
            stop = STOP;
            request(1);
        }

        @Override
        public void emit(T item) throws Throwable {
            var p = produced;
            if (get() == p && stop == null) {
                p = BackpressureHelper.produced(this, p);
                if (p == 0 && stop == null) {
                    consumerReady.await();
                }
            }

            var s = stop;
            if (s == null) {
                downstream.onNext(item);
                produced = p + 1;
            } else {
                throw s;
            }
        }

        public void setFiber(Fiber<?> fiber) {
            if (this.fiber.get() != null || this.fiber.compareAndSet(null, fiber)) {
                if (this.fiber.get() != this) {
                    fiber.cancel();
                }
            }
        }
    }
}