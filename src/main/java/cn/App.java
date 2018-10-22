package cn;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hello world!
 */
public class App {
    static List<String> urlPartList = new ArrayList<>();
    static String local_ip = "0.0.0.1";
    static String dead_str = "#dead";
    static List<String> undead = new ArrayList<>();
    static List<String> dead = new ArrayList<>();
    static Map<String, ?> urlMap = new IdentityHashMap<>();

    public static void main(String[] args) throws IOException {
        readFile();
        for (int i = 0; i < dead.size(); i++) {
            String deadRule = dead.get(i);
            if (deadRule.length() == 0 || deadRule.startsWith("#")) {continue; }

            String url = deadRule.split(" ")[1];
            String[] urlPart = url.split("\\.");

            Map parentMap = urlMap;
            Map subMap = null;
            for (int j = urlPart.length - 1; j >= 0; j--) {
                String part = getPart(urlPart[j]);
                if (j == 0) {
                    part = new String(part);
                    Object o = parentMap.get(part);
                    if (o == null) {
                        parentMap.put(part, part);
                    }
                } else if (j != 0) {
                    subMap = (Map) parentMap.get(part);
                    if (subMap == null) {
                        subMap = new IdentityHashMap();
                        parentMap.put(part, subMap);
                    }
                    parentMap = subMap;
                    subMap = null;
                }
            }
//            System.out.println(urlMap);
        }
        Gson gson = new Gson();

        System.out.println(gson.toJson(urlMap));
    }

    static String getPart(String part) {
        int indexOf = urlPartList.indexOf(part);
        if (indexOf == -1) {
            urlPartList.add(part);
        } else {
            part = urlPartList.get(indexOf);
        }
        return part;
    }


    private static void readFile() throws IOException {
        Path path = Paths.get("hosts.1");
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
