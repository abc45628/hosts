package cn;

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
    private static final IdentityHashMap<String, Object> undead_urlMapRoot = new IdentityHashMap<>();
    private static final List<String> dead = new ArrayList<>();
    private static final IdentityHashMap<String, Object> dead_urlMapRoot = new IdentityHashMap<>();
    private static final String result = "";

    private App() {}

    public static void main(String[] args) throws IOException {
        readFile();
        hostToMap(dead, dead_urlMapRoot);
        hostToMap(undead, undead_urlMapRoot);

        cleanDuplicateUrl(dead_urlMapRoot);
        cleanDuplicateUrl(undead_urlMapRoot);

        dead.clear();
        undead.clear();

        output(dead_urlMapRoot, null, dead_urlMapRoot, dead);
        output(undead_urlMapRoot, null, undead_urlMapRoot, undead);

//        System.out.println(new Gson().toJson(dead_urlMapRoot));
//        System.out.println(new Gson().toJson(undead_urlMapRoot));
//        System.out.println();
//        System.out.println(dead);
//        System.out.println(undead);

        undead.add("\n\n" + dead_str + "\n");
        undead.addAll(dead);
        System.out.println(String.join("", undead));
    }

    /** 输出网址 */
    @SuppressWarnings("unchecked")
    private static void output(IdentityHashMap<String, Object> source, List<String> urlPartList, IdentityHashMap<String, Object> sourceRoot, List<String> urlListRoot) {
        List<String> sourceKey = new ArrayList<>(source.keySet());
        Collections.sort(sourceKey);
        for (int i = 0; i < sourceKey.size(); i++) {
            if (source == sourceRoot) {
                urlPartList = new ArrayList<>();
            }
            String key = sourceKey.get(i);
            Object o = source.get(key);
            if (o instanceof String) {
                urlPartList.add(key);
                String url = "";
                for (int j = urlPartList.size() - 1; j >= 0; j--) {
                    String s = urlPartList.get(j);
                    url = url + "." + s;
                }
                url = url.substring(1);
                url = local_ip + " " + url + "\n";
                urlListRoot.add(url);
                urlPartList.remove(urlPartList.size() - 1);
//                System.out.println();
            } else if (o instanceof IdentityHashMap) {
                urlPartList.add(key);
                output((IdentityHashMap<String, Object>) o, urlPartList, sourceRoot, urlListRoot);
                urlPartList.remove(urlPartList.size() - 1);
            }
        }
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
    private static void hostToMap(List<String> source, IdentityHashMap<String, Object> mapSource) {
        for (int i = 0; i < source.size(); i++) {
            String deadRule = source.get(i);
            if (deadRule.isEmpty() || deadRule.startsWith("#")) {
                continue;
            }

            String url = deadRule.split(" ")[1];
            String[] urlPart = url.split("\\.");

            Map<String, Object> parentMap = mapSource;
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
