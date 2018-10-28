package cn;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private App() {}

    public static void main(String[] args) throws IOException {
        readFile();
        hostToMap();
        cleanDuplicateUrl(urlMapRoot);


        Gson gson = new Gson();
        System.out.println(gson.toJson(urlMapRoot));
    }


    /** 去除重复的网址 */
    @SuppressWarnings("unchecked")
    private static void cleanDuplicateUrl(IdentityHashMap<String, Object> source) {
        List<String> sourceKey = new ArrayList<>(source.keySet());
        Collections.sort(sourceKey);
        String curKey = null;
        List<String> curKeyList = new ArrayList<>();
        for (int i = 0; i < sourceKey.size(); i++) {
            String s = sourceKey.get(i);
            if (curKey == null) {
                curKey = s;
            }
            if (curKey.equals(s)) {
                curKeyList.add(s);
                int j = i + 1;
                if (j < sourceKey.size()) {
                    if (sourceKey.get(j).equals(curKey)) {
                        continue;
                    }
                }
            }
            //
            Set<Object> urlSet = new HashSet<>();
            for (String s1 : curKeyList) {
                Object o = source.get(s1);
                if (o instanceof IdentityHashMap) {
                    cleanDuplicateUrl((IdentityHashMap<String, Object>) o);
                    continue;
                }

                if (urlSet.contains(o)) {
                    source.remove(s1);
                }
                urlSet.add(o);
            }
            //
            curKey = null;
            curKeyList = new ArrayList<>();
        }
//        System.out.println(sourceKey);
    }


    @SuppressWarnings("unchecked")
    private static void hostToMap() {
        for (int i = 0; i < dead.size(); i++) {
            String deadRule = dead.get(i);
            if (deadRule.isEmpty() || deadRule.startsWith("#")) {
                continue;
            }

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
