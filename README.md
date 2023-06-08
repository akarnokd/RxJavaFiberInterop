# RxJavaFiberInterop

Library for interoperation between RxJava 3 and JDK 21's Virtual Threads (Project Loom).

<a href='https://github.com/akarnokd/RxJavaFiberInterop/actions?query=workflow%3A%22Java+CI+with+Gradle%22'><img src='https://github.com/akarnokd/RxJavaFiberInterop/workflows/Java%20CI%20with%20Gradle/badge.svg'></a>
[![codecov.io](http://codecov.io/github/akarnokd/RxJavaFiberInterop/coverage.svg?branch=master)](http://codecov.io/github/akarnokd/RxJavaFiberInterop?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-fiber-interop/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-fiber-interop)

```groovy

dependencies {
    implementation "com.github.akarnokd:rxjava3-fiber-interop:0.0.17"
}
```

Requires a JDK that has Virtual Threads as standard feature (i.e., not preview), such as [https://jdk.java.net/21/](https://jdk.java.net/21/).

# Components

## FiberInterop

### Schedulers

Currently, the Virtual Thread API does not offer public means to specify the carrier thread(pool) thus it is not possible to use RxJava `Scheduler`s as such.

You can use the `Schedulers.from` though to convert the Fork-Join-pool backed standard Virtual Thread Executor into an RxJava `Scheduler`:

```java
var vte = Executors.newVirtualThreadExecutor();
Scheduler vtScheduler = Schedulers.from(vte);

// sometime later
vte.close();
```

You can then use `vtScheduler` from the example with `subscribeOn` and `observeOn` to let traditional functional callbacks to block virtually:

```java
Observable.fromCallable(() -> someBlockingNetworkCall())
.subscribeOn(vtScheduler)
.observeOn(vtScheduler)
.map(v -> someOtherBlockingCall(v))
.observeOn(uiThread)
.subscribe(v -> label.setText(v), e -> label.setText(e.toString()));
```

:information_source: You need the special operators below to make RxJava's non-blocking backpressure into virtually blocked backpressure.

### create

Creates a `Flowable` from a generator callback, that can emit via `FiberEmitter`, run on an `ExecutorService` provided by the user and
is suspended automatically upon backpressure. The callback is executed inside the virtual thread thus you can call the usual blocking APIs and get suspensions the same way.

The created `Flowable` will complete once the callback returns normally or with an error if the callback throws an exception.

```java
try (var scope = Executors.newVirtualThreadExecutor()) {
    FiberInterop.create(emitter -> {
        for (int i = 1; i <= 5; i++) {
             emitter.emit(1);
        }
    }, scope)
    .test()
    .awaitDone(5, TimeUnit.SECONDS)
    .assertResult(1, 2, 3, 4, 5);
}
```

### transform

Transforms each upstream value via a callback that can emit zero or more values for each of those upstream values, run on an `ExecutorService` provided by the user and is suspended automatically upon backpressure. The callback is executed inside the virtual thread thus you can call the usual blocking APIs and get suspensions the same way.

```java
try (var scope = Executors.newVirtualThreadExecutor()) {
    Flowable.range(1, 5)
    .compose(FiberInterop.transform((value, emitter) -> {
        emitter.emit(value);
        emitter.emit(value + 1);
    }, scope))
    .test()
    .awaitDone(5, TimeUnit.SECONDS)
    .assertResult(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
}
```

### blockingXXX

RxJava uses `java.util.concurrent` locks and `CountDownLatches` via its `blockingXXX` which will automatically work within a virtual thread. Therefore, there is no need for a separate interop operator. Just block.

```java
try (var scope = Executors.newVirtualThreadExecutor()) {
    scope.submit(() -> {
        var v = Flowable.just(1)
        .delay(1, TimeUnit.SECONDS)
        .blockingLast();
        System.out.println(v);
    });
}
```
