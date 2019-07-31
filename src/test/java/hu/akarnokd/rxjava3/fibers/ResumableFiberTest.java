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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class ResumableFiberTest {

    @Test
    public void parkNormalThread() {
        var ready1 = new ResumableFiber();
        var ready2 = new ResumableFiber();

        var t = new Thread(() -> {
            ready1.await();
            ready2.resume();
        });
        t.start();

        ready1.resume();
        ready2.await();
    }

    @Test
    public void concurrentAwait() {
        var ready1 = new ResumableFiber();

        var stateEx = 0;

        try {
            var t = new Thread(() -> {
                ready1.await();
            });
            t.start();

            while (ready1.get() == null) { }
            try {
                ready1.await();
            } catch (IllegalStateException ex) {
                stateEx++;
            }
        } finally {
            ready1.resume();
        }

        assertEquals(1, stateEx++);
    }
}
