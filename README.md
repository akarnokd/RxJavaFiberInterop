# RxJavaFiberInterop
Library for interoperation between RxJava 3 and Project Loom's Fibers.

<a href='https://travis-ci.org/akarnokd/RxJavaFiberInterop/builds'><img src='https://travis-ci.org/akarnokd/RxJavaFiberInterop.svg?branch=master'></a>
[![codecov.io](http://codecov.io/github/akarnokd/RxJavaFiberInterop/coverage.svg?branch=master)](http://codecov.io/github/akarnokd/RxJavaFiberInterop?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-fiber-interop/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-fiber-interop)

```groovy

dependencies {
    implementation "com.github.akarnokd:rxjava3-fiber-interop:0.0.5"
}
```


# Components

## FiberInterop

### create

Creates a `Flowable` from a generator callback, that can emit via `FiberEmitter`, run in a Fiber backed by the `ForkJoinPool.commonPool()` or any `Executor`/`Scheduler` provided as argument
and suspended automatically on downstream backpressure.

```java
FiberInterop.create(emitter -> {
    for (int i = 1; i <= 5; i++) {
         emitter.emit(1);
    }
})
.test()
.awaitDone(5, TimeUnit.SECONDS)
.assertResult(1, 2, 3, 4, 5);
```

### transform

Transforms each upstream value via a callback that can emit zero or more values for each of those upstream values, run in a Fiber backed by the `ForkJoinPool.commonPool()` or any `Executor`/`Scheduler` provided as argument
and suspended automatically on downstream backpressure.

```java
Flowable.range(1, 5)
.compose(FiberInterop.transform((value, emitter) -> {
    emitter.emit(value);
    emitter.emit(value + 1);
}))
.test()
.awaitDone(5, TimeUnit.SECONDS)
.assertResult(1, 2, 2, 3, 3, 4, 4, 5, 5, 6);
```
