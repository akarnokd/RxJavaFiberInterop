# RxJavaFiberInterop
Library for interoperation between RxJava 3 and Project Loom's Fibers.

<a href='https://github.com/akarnokd/RxJavaFiberInterop/actions?query=workflow%3A%22Java+CI+with+Gradle%22'><img src='https://github.com/akarnokd/RxJavaFiberInterop/workflows/Java%20CI%20with%20Gradle/badge.svg'></a>
[![codecov.io](http://codecov.io/github/akarnokd/RxJavaFiberInterop/coverage.svg?branch=master)](http://codecov.io/github/akarnokd/RxJavaFiberInterop?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-fiber-interop/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-fiber-interop)

```groovy

dependencies {
    implementation "com.github.akarnokd:rxjava3-fiber-interop:0.0.17"
}
```

Always requires the latest Loom build from [https://jdk.java.net/loom/](https://jdk.java.net/21/)

*Note that Loom is in early access and the API, naming and usage keeps changing, a lot.*

# Components

## FiberInterop

### create

Creates a `Flowable` from a generator callback, that can emit via `FiberEmitter`, run on an `ExecutorService` provided by the user and
is suspended automatically upon backpressure.

:warning: Since version 0.0.15 of this library and with the current latest Loom preview 18b5, there is no public way to
specify a carrier thread beneath a virtual thread executor anymore. Therefore, the single-argument and `Scheduler`-argumented overloads
of `create` have been removed.

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

Transforms each upstream value via a callback that can emit zero or more values for each of those upstream values, run on an `ExecutorService` provided by the user and
is suspended automatically upon backpressure.

:warning: Since version 0.0.15 of this library and with the current latest Loom preview 18b5, there is no public way to
specify a carrier thread beneath a virtual thread executor anymore. Therefore, the single-argument and `Scheduler`-argumented overloads
of `create` have been removed.

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
