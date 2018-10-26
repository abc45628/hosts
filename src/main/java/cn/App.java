package cn;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */


public class App {
    private static final List<String> urlPartList = new ArrayList<>();
    private static final String local_ip = "0.0.0.1";
    private static final String dead_str = "#dead";
    private static final List<String> undead = new ArrayList<>();
    private static final List<String> dead = new ArrayList<>();
    private static final IdentityHashMap<String, Object> urlMapRoot = new IdentityHashMap<>();
    private static final IdentityHashMap<String, Object> urlMapRootResult = new IdentityHashMap<>();

    private App() {}

    public static void main(String[] args) throws IOException {
        readFile();
        hostToMap();
        cleanDuplicateUrl(urlMapRoot, urlMapRootResult);


        Gson gson = new Gson();
        System.out.println(gson.toJson(urlMapRoot));
    }

    private static void cleanDuplicateUrl(IdentityHashMap<String, Object> source, IdentityHashMap<String, Object> result) {
        List<String> rootKey = new ArrayList<>(source.keySet());
        Collections.sort(rootKey);
        for (int i = 0; i < rootKey.size(); i++) {
            String key = rootKey.get(i);
            Map<String, List<String>> stringMap = new HashMap<>();//?
            Object lastPart = urlMapRoot.get(key);
            if (lastPart instanceof String) {
                List<String> lastPartList = stringMap.get(key);
                if (lastPartList == null) {
                    lastPartList = new ArrayList<>();
                    stringMap.put(key, lastPartList);
                }
                if (lastPartList.contains(lastPart)) {
                    continue;//说明lastPart之前已经写入新的map，现在碰上重复了
                } else {
                    lastPartList.add((String) lastPart);
                }
            }
        }
        System.out.println(rootKey);
    }

    private static void hostToMap() {
        for (int i = 0; i < dead.size(); i++) {
            String deadRule = dead.get(i);
            if (deadRule.isEmpty() || deadRule.startsWith("#")) {continue; }

            String url = deadRule.split(" ")[1];
            String[] urlPart = url.split("\\.");

            Map<String, Object> parentMap = urlMapRoot;
            Map<String, Object> subMap = null;
            for (int j = urlPart.length - 1; j >= 0; j--) {
                String part = getPart(urlPart[j]);
                if (j == 0) {
                    part = new String(part);
                    parentMap.putIfAbsent(part, part);
                } else {
                    subMap = (Map<String, Object>) parentMap.get(part);
                    if (subMap == null) {
                        subMap = new IdentityHashMap<>();
                        parentMap.put(part, subMap);
                    }
                    parentMap = subMap;
                    subMap = null;
                }
            }
//            System.out.println(urlMapRoot);
        }
    }

    private static String getPart(String part) {
        int indexOf = urlPartList.indexOf(part);
        if (urlPartList.contains(part)) {
            part = urlPartList.get(indexOf);
        } else {
            urlPartList.add(part);
        }
        return part;
    }


    private static void readFile() throws IOException {
        Path path = Paths.get("hosts.1");
        List<String> list = Files.readAllLines(path, Charset.forName("GB2312"));
        boolean isdead = false;
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (isdead) {
                dead.add(s);
            } else {
                if (s.equals(dead_str)) {
                    isdead = true;
                    continue;
                }
                undead.add(s);
            }
        }
    }
}
