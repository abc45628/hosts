package cn;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */
public class App {
    static String local_ip = "0.0.0.1";
    static String dead_str = "#dead";
    static List<String> undead = new ArrayList<>();
    static List<String> dead = new ArrayList<>();
    static Map<String, ?> urlMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        readFile();
        for (int i = 0; i < dead.size(); i++) {
            String deadRule = dead.get(i);
            if (deadRule.length() == 0 || deadRule.startsWith("#")) {continue; }
            String url = deadRule.split(" ")[1];
            String[] urlPart = url.split("\\.");
            for (int j = urlPart.length - 1; j >= 0; j--) {
                String part = urlPart[j];
                if (j != 0) {
                    Map<String, Map<String, ?>> m = (Map<String, Map<String, ?>>) urlMap.get(part);

                } else {
                    String s = (String) urlMap.get(part);
                }
                System.out.println(part);
            }
        }
    }

    static void dealDeadRule(String part, Map m, int index) {
        Map o = (Map) m.get(part);
        if (o == null) {
            o = new HashMap();
        }

    }

    private static void readFile() throws IOException {
        Path path = Paths.get("hosts");
        List<String> list = Files.readAllLines(path, Charset.forName("GB2312"));
        boolean isdead = false;
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (!isdead) {
                if (s.equals(dead_str)) {
                    isdead = true;
                    continue;
                }
                undead.add(s);
            } else {
                dead.add(s);
            }
        }
    }
}
