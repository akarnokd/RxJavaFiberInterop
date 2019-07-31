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

import org.reactivestreams.*;

import io.reactivex.*;

final class FlowableTransformFiberScheduler<T, R> extends Flowable<R>
implements FlowableTransformer<T, R> {

    final Flowable<T> source;
    
    final FiberTransformer<T, R> transformer;
    
    final Scheduler scheduler;
    
    FlowableTransformFiberScheduler(Flowable<T> source,
            FiberTransformer<T, R> transformer, Scheduler scheduler) {
        this.source = source;
        this.transformer = transformer;
        this.scheduler = scheduler;
    }

    @Override
    public Publisher<R> apply(Flowable<T> upstream) {
        return new FlowableTransformFiberScheduler<>(upstream, transformer, scheduler);
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        // TODO Auto-generated method stub
        
    }

}
