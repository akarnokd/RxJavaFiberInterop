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

import static org.testng.Assert.assertTrue;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.*;

import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Test
public class FiberInteropTest {

    @Test
    public void checkClass() {
        TestHelper.checkUtilityClass(FiberInterop.class);
    }

    @Test
    public void checkIsInsideFiber() {
        FiberInterop.create(emitter -> {
            emitter.emit(Thread.currentThread().isVirtual());
        })
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(true);
    }

    @org.junit.Test
    public void checkIsInsideFiberJunit() {
        FiberInterop.create(emitter -> {
            emitter.emit(Thread.currentThread().isVirtual());
        })
        .test()
        .awaitDone(5, TimeUnit.SECONDS)
        .assertResult(true);
    }

    @Test
    public void checkIsInsideFiberExec() throws Throwable {
        try (var exec = Executors.newSingleThreadExecutor()) {
            FiberInterop.create(emitter -> {
                emitter.emit(Thread.currentThread().isVirtual());
            }, exec)
            .test()
            .awaitDone(5, TimeUnit.SECONDS)
            .assertResult(true);
        }
    }

    @org.junit.Test
    public void checkIsInsideFiberJunitExec() throws Throwable {
        try (var exec = Executors.newSingleThreadExecutor()) {
            FiberInterop.create(emitter -> {
                emitter.emit(Thread.currentThread().isVirtual());
            }, exec)
            .test()
            .awaitDone(5, TimeUnit.SECONDS)
            .assertResult(true);
        }
    }

    @Test
    public void virtualParent() {
        var result = new AtomicReference<Boolean>();
        try (var exec = Executors.newSingleThreadExecutor()) {
            try (var scope = Executors.newThreadExecutor(Thread.ofVirtual().scheduler(exec).factory())) {
                scope.submit(() -> result.set(Thread.currentThread().isVirtual()));
            }
        }

        assertTrue(result.get());
    }

    @Test
    public void virtualParentScheduler() {
        var worker = Schedulers.computation().createWorker();
        try {
            var result = new AtomicReference<Boolean>();
            try (var scope = Executors.newThreadExecutor(Thread.ofVirtual().scheduler(worker::schedule).factory())) {
                scope.submit(() -> result.set(Thread.currentThread().isVirtual()));
            }

            assertTrue(result.get());
        } finally {
            worker.dispose();
        }
    }

    @Test
    @Ignore("This hangs for some reason, probably the Loom preview limitation")
    public void nestedVirtual() {
        var result = new AtomicReference<Boolean>();
        try (var exec = Executors.newSingleThreadExecutor()) {
            try (var scope = Executors.newThreadExecutor(Thread.ofVirtual().scheduler(exec).factory())) {
                try (var scope2  = Executors.newThreadExecutor(Thread.ofVirtual().scheduler(scope).factory())) {
                    scope2.submit(() -> result.set(Thread.currentThread().isVirtual()));
                }
            }
        }

        assertTrue(result.get());
    }

    @Test
    public void plainVirtual() {
        var result = new AtomicReference<Boolean>();
        try (var exec = Executors.newSingleThreadExecutor()) {
            try (var scope = Executors.newThreadExecutor(Thread.ofVirtual().scheduler(exec).factory())) {
                scope.submit(() -> result.set(Thread.currentThread().isVirtual()));
            }
        }

        assertTrue(result.get());
    }

    static void withVirtual(Consumer<ExecutorService> call) throws Throwable {
        try (var exec = Executors.newThreadExecutor(Thread.ofVirtual().factory())) {
            call.accept(exec);
        }
    }

    static void withVirtual(Executor parent, Consumer<ExecutorService> call) throws Throwable {
        try (var exec = Executors.newThreadExecutor(Thread.ofVirtual().scheduler(parent).factory())) {
            call.accept(exec);
        }
    }
}
