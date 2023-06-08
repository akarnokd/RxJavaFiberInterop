/*
 * Copyright 2019-Present David Karnok
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

import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.rxjava3.annotations.NonNull;

/**
 * Copy of RxJava's helper methods.
 */
class Helpers {
    /** Utility class. */
    private Helpers() {
        throw new IllegalStateException();
    }

    /**
     * Atomically adds the positive value n to the requested value in the {@link AtomicLong} and
     * caps the result at {@link Long#MAX_VALUE} and returns the previous value.
     * @param requested the {@code AtomicLong} holding the current requested value
     * @param n the value to add, must be positive (not verified)
     * @return the original value before the add
     */
    public static long add(@NonNull AtomicLong requested, long n) {
        for (;;) {
            long r = requested.get();
            if (r == Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            long u = addCap(r, n);
            if (requested.compareAndSet(r, u)) {
                return r;
            }
        }
    }
    /**
     * Adds two long values and caps the sum at {@link Long#MAX_VALUE}.
     * @param a the first value
     * @param b the second value
     * @return the sum capped at {@link Long#MAX_VALUE}
     */
    public static long addCap(long a, long b) {
        long u = a + b;
        if (u < 0L) {
            return Long.MAX_VALUE;
        }
        return u;
    }
    /**
     * Validate that the given value is positive or report an IllegalArgumentException with
     * the parameter name.
     * @param value the value to validate
     * @param paramName the parameter name of the value
     * @return value
     * @throws IllegalArgumentException if bufferSize &lt;= 0
     */
    public static int verifyPositive(int value, String paramName) {
        if (value <= 0) {
            throw new IllegalArgumentException(paramName + " > 0 required but it was " + value);
        }
        return value;
    }
}
