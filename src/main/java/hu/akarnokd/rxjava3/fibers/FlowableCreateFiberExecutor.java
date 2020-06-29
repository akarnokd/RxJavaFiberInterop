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

import org.reactivestreams.Subscriber;

import hu.akarnokd.rxjava3.fibers.FlowableCreateFiberScheduler.CreateFiberSubscription;
import io.reactivex.rxjava3.core.Flowable;

/**
 * Runs a generator callback on a Fiber backed by a Worker of the given scheduler
 * and signals events emitted by the generator considering any downstream backpressure.
 *
 * @param <T> the element type of the flow
 */
final class FlowableCreateFiberExecutor<T> extends Flowable<T> {

    final FiberGenerator<T> generator;

    final ExecutorService executor;

    FlowableCreateFiberExecutor(FiberGenerator<T> generator, ExecutorService executor) {
        this.generator = generator;
        this.executor = executor;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        var scope = Executors.newThreadExecutor(Thread.builder().virtual(executor).factory());
        var parent = new ExecutorCreateFiberSubscription<>(s, generator, scope);
        s.onSubscribe(parent);

        scope.submit(parent);
    }


    static final class ExecutorCreateFiberSubscription<T> extends CreateFiberSubscription<T> {
        private static final long serialVersionUID = -8552685969992500057L;

        ExecutorService scope;

        ExecutorCreateFiberSubscription(Subscriber<? super T> downstream, FiberGenerator<T> generator, ExecutorService scope) {
            super(downstream, generator);
            this.scope = scope;
        }

        @Override
        protected void cleanup() {
            var e = scope;
            scope = null;
            if (e != null) {
                e.close();
            }
        }
    }

}
