package hu.akarnokd.rxjava3.fibers;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public final class SetupLatestLoom {

    private SetupLatestLoom() {
        // program
    }

    public static void main(String[] args) throws Exception {
        URL u = new URL("https://jdk.java.net/loom/");

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

                if (url.endsWith(".tar.gz") && url.contains("linux")) {
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
