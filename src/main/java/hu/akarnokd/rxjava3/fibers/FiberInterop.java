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

import io.reactivex.*;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

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

    public static <T> Flowable<T> create(FiberGenerator<T> generator) {
        return create(generator, Schedulers.computation());
    }

    public static <T> Flowable<T> create(FiberGenerator<T> generator, Scheduler scheduler) {
        Objects.requireNonNull(generator, "generator is null");
        Objects.requireNonNull(scheduler, "scheduler is null");
        return RxJavaPlugins.onAssembly(new FlowableCreateFiber<>(generator, scheduler));
    }
}
