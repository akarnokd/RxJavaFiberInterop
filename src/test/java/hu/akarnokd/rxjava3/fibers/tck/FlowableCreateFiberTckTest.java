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

import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import hu.akarnokd.rxjava3.fibers.FiberInterop;

@Test
public class FlowableCreateFiberTckTest extends BaseTck<Long> {

    @Override
    public Publisher<Long> createPublisher(final long elements) {
        return
                FiberInterop.create(emitter -> {
                    for (var i = 0L; i < elements; i++) {
                        emitter.emit(i);
                    }
                });
    }

}
