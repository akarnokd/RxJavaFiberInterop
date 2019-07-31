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

import java.util.concurrent.Executor;

import org.reactivestreams.Subscriber;

import hu.akarnokd.rxjava3.fibers.FlowableCreateFiberScheduler.CreateFiberSubscription;
import io.reactivex.Flowable;

/**
 * Runs a generator callback on a Fiber backed by a Worker of the given scheduler
 * and signals events emitted by the generator considering any downstream backpressure.
 *
 * @param <T> the element type of the flow
 */
final class FlowableCreateFiberExecutor<T> extends Flowable<T> {

    final FiberGenerator<T> generator;

    final Executor executor;

    FlowableCreateFiberExecutor(FiberGenerator<T> generator, Executor executor) {
        this.generator = generator;
        this.executor = executor;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        var parent = new ExecutorCreateFiberSubscription<>(s, generator);
        s.onSubscribe(parent);

        var fiber = FiberScope.background().schedule(executor, parent);
        parent.setFiber(fiber);
    }


    static final class ExecutorCreateFiberSubscription<T> extends CreateFiberSubscription<T> {
        private static final long serialVersionUID = -8552685969992500057L;

        ExecutorCreateFiberSubscription(Subscriber<? super T> downstream, FiberGenerator<T> generator) {
            super(downstream, generator);
        }

        @Override
        protected void cleanup() {
        }
    }

}
