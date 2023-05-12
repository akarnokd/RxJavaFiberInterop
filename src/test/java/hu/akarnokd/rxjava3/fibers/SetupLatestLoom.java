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

import java.io.ByteArrayOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class SetupLatestLoom {

    private SetupLatestLoom() {
        // program
    }

    public static void main(String[] args) throws Exception {
        URL u = new URI("https://jdk.java.net/loom/").toURL();

        ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);

        u.openConnection().getInputStream().transferTo(bout);

        String s = bout.toString(StandardCharsets.UTF_8);

        int j = 0;

        String url = "";
        for (;;) {
            int i = s.indexOf("https://download.java.net/java/early_access/loom", j);

            if (i >= 0) {
                int k = s.indexOf("\"", i + 10);
                url = s.substring(i, k);

                if (url.endsWith(".tar.gz") && url.contains("linux-x64")) {
                    break;
                }
                url = "";
            } else {
                break;
            }
            j = i + 10;
        }

        if (url.isEmpty()) {
            System.err.println("Unable to find a Loom download URL for Linux");
            return;
        }

        System.out.println("Found " + url);

        String fileName = url.substring(url.lastIndexOf("/") + 1);

        var loomSh = Paths.get("use_loom.sh");

        List<String> lines = Files.readAllLines(loomSh);

        for (int i = 0; i < lines.size(); i++) {

            if (lines.get(i).startsWith("wget ")) {
                lines.set(i, "wget " + url);
            }
            if (lines.get(i).startsWith("tar -zxvf ")) {
                lines.set(i, "tar -zxvf " + fileName);
            }
        }
        Files.write(loomSh, lines);
        System.out.println("Done");
    }
}
