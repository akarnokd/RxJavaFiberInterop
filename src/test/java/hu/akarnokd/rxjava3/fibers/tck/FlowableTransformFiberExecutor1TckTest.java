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

package hu.akarnokd.rxjava3.fibers.tck;

import java.io.IOException;

import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import hu.akarnokd.rxjava3.fibers.FiberInterop;
import io.reactivex.Flowable;

@Test
public class FlowableTransformFiberExecutor1TckTest extends BaseTck<Long> {

    @Override
    public Publisher<Long> createPublisher(final long elements) {
        var half = elements >> 1;
        var rest = elements - half;
        return Flowable.rangeLong(0, rest)
                .compose(FiberInterop.transform((v, emitter) -> {
                    emitter.emit(v);
                    if (v < rest - 1 || half == rest) {
                        emitter.emit(v);
                    }
                }, 1));
    }

    @Override
    public Publisher<Long> createFailedPublisher() {
        return Flowable.just(1)
                .compose(FiberInterop.transform((v, emitter) -> {
                    throw new IOException();
                }, 1));
    }
}
