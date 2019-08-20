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
import java.util.concurrent.*;

import org.reactivestreams.Publisher;
import org.testng.annotations.Test;

import hu.akarnokd.rxjava3.fibers.*;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Test
public class FlowableTransformFiberExecutorTckTest extends BaseTck<Long> {

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
                }, service));
    }

    @Override
    public Publisher<Long> createFailedPublisher() {
        return Flowable.just(1)
                .compose(FiberInterop.transform((v, emitter) -> {
                    throw new IOException();
                }, service));
    }

    @Test
    public void slowProducer() {
        var log = new ConcurrentLinkedQueue<String>();

        var ts = Flowable.range(1, 10)
        .doOnNext(v -> log.offer("Range: " + v))
        .doOnRequest(v -> log.offer("SubscribeOn requested: " + v))
        .doOnSubscribe(s -> log.offer("Range subscribed to"))
        .subscribeOn(Schedulers.computation())
        .doOnRequest(v -> log.offer("Map requested: " + v))
        .doOnSubscribe(s -> log.offer("subscribeOn subscribed to"))
        .map(v -> {
            log.offer("Map: " + v);
            log.offer("Map interrupted? " + Thread.interrupted());
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                log.offer("Map sleep interrupted");
            }
            return v;
        })
        .doOnRequest(v -> log.offer("Transform requested: " + v))
        .compose(FiberInterop.transform((v, emitter) -> {
            log.offer("Tansform before emit: " + v);
            emitter.emit(v);
            log.offer("Tansform after emit: " + v);
        }))
        .doOnRequest(v -> log.offer("Test requested: " + v))
        .doOnNext(v -> log.offer("Test received: " + v))
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        ;

        try {
            ts.assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        } catch (AssertionError ex) {
            var sb = new StringBuilder();
            log.forEach(v -> sb.append("\r\n").append(v));
            var exc = new AssertionError(sb.append("\r\n").toString(), ex);
            throw exc;
        }
    }

    @Test
    public void slowProducerService() {
        TestHelper.checkObstruction();

        Flowable.range(1, 10)
        .subscribeOn(Schedulers.computation())
        .map(v -> {
            Thread.interrupted();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                // ignored
            }
            return v;
        })
        .compose(FiberInterop.transform((v, emitter) -> {
            emitter.emit(v);
        }, service))
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }
}
