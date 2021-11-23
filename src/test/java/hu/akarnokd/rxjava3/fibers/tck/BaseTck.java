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
import org.reactivestreams.tck.*;
import org.testng.annotations.*;

import io.reactivex.rxjava3.core.Flowable;

/**
 * Base abstract class for Flowable verifications, contains support for creating
 * Iterable range of values.
 * 
 * @param <T> the element type
 */
@Test
public abstract class BaseTck<T> extends PublisherVerification<T> {

    public BaseTck() {
        this(100);
    }

    public BaseTck(long timeout) {
        super(new TestEnvironment(timeout));
    }

    @Override
    public Publisher<T> createFailedPublisher() {
        return Flowable.error(new IOException());
    }

    @Override
    public long maxElementsFromPublisher() {
        return 1024;
    }

    protected static ExecutorService service;

    @BeforeClass
    public static void before() {
        service = Executors.newVirtualThreadPerTaskExecutor();
    }

    @AfterClass
    public static void after() {
        service.shutdown();
    }
}
