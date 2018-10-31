package cn;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hello world!
 */


public class App {
    private static final Charset CHARSET = Charset.forName("GB2312");
    private static final List<String> urlPartList = new ArrayList<>();
    private static final String local_ip = "0.0.0.1";
    private static final String dead_str = "#dead";
    private static final List<String> undead = new ArrayList<>();
    private static final IdentityHashMap<String, Object> undead_urlMapRoot = new IdentityHashMap<>();
    private static final List<String> dead = new ArrayList<>();
    private static final IdentityHashMap<String, Object> dead_urlMapRoot = new IdentityHashMap<>();
    /** 记录不同域名的共同注释 */
    private static final Map<String, String> urlGroupComment = new HashMap<>();
    /** 记录每条记录的注释 */
    private static final Map<String, String> urlComment = new HashMap<>();

    static {
        urlGroupComment.put("cm.lianmeng.360.cn", "#360联盟");
        urlGroupComment.put("crs.baidu.com", "#baidu网盟");
        urlGroupComment.put("c.cnzz.com", "#cnzz");
        urlGroupComment.put("www.google-analytics.com", "#google");
    }

    private App() {}

    public static void main(String[] args) throws IOException {
        readFile();
        hostToMap(dead, dead_urlMapRoot);
        hostToMap(undead, undead_urlMapRoot);

        cleanDuplicateUrl(dead_urlMapRoot);
        cleanDuplicateUrl(undead_urlMapRoot);

        dead.clear();
        undead.clear();

        buildHost1(dead_urlMapRoot, new ArrayList<>(), dead);
        buildHost1(undead_urlMapRoot, new ArrayList<>(), undead);
        buildHost2();
        buildHost3(dead);
        buildHost3(undead);
//        System.out.println(new Gson().toJson(dead_urlMapRoot));
//        System.out.println(new Gson().toJson(undead_urlMapRoot));
//        System.out.println();
//        System.out.println(dead);
//        System.out.println(undead);

        undead.add("\n\n" + dead_str + "\n");
        undead.addAll(dead);


        System.out.println(String.join("", undead));
        outputFile();
    }

    /** 输出文件 */
    private static void outputFile() {
        Path path = Paths.get("hosts");
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                FileChannel channel = fos.getChannel();
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 4 * 10);
            String s = String.join("", undead);
            buffer.put(s.getBytes(CHARSET));
            buffer.flip();
            channel.write(buffer);
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void buildHost3(List<String> source) {
        for (int i = 0; i < source.size(); i++) {
            String s = source.get(i);
            if (s.startsWith("#")) {
                s = "\n" + s + "\n";
            } else {
                String urlcomment = MapUtils.getString(urlComment, s, "");
                s = local_ip + " " + s + urlcomment + "\n";
            }
            source.add(i, s);
            source.remove(i + 1);
        }
    }

    /** 生成host记录 */
    private static void buildHost2() {
        Set<String> keySet = urlGroupComment.keySet();
        for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            int bestMatch = -1;
            int i = dead.indexOf(key);
            if (i == -1) {
                continue;
            }
            dead.add(i, urlGroupComment.get(key));
//            for (int i = 0; i < dead.size(); i++) {
//                String s = dead.get(i);
//            }
        }
        System.out.println();
    }

    /** 生成host记录 */
    @SuppressWarnings("unchecked")
    private static void buildHost1(IdentityHashMap<String, Object> source, List<String> urlPartList, List<String> urlListRoot) {
        List<String> sourceKey = new ArrayList<>(source.keySet());
        Collections.sort(sourceKey);
        for (int i = 0; i < sourceKey.size(); i++) {
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
                urlListRoot.add(url);
                urlPartList.remove(urlPartList.size() - 1);
            } else if (o instanceof IdentityHashMap) {
                urlPartList.add(key);
                buildHost1((IdentityHashMap<String, Object>) o, urlPartList, urlListRoot);
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

    /** 抽取每条host的URL部分，根据点号分割出每个域 */
    @SuppressWarnings("unchecked")
    private static void hostToMap(List<String> source, IdentityHashMap<String, Object> mapSource) {
        for (int i = 0; i < source.size(); i++) {
            String hostRecord = source.get(i);
            if (hostRecord.isEmpty() || hostRecord.startsWith("#")) {
                continue;
            }
            String[] hr = hostRecord.split(" ");
            String url = hr[1];
            if (hr.length == 3) {
                urlComment.put(url, " " + hr[2]);
            }
            String[] urlPart = url.split("\\.");

            Map<String, Object> parentMap = mapSource;
            for (int j = urlPart.length - 1; j >= 0; j--) {
                String part = getPart(urlPart[j]);
                if (j == 0) {
                    part = new String(part);
                    parentMap.putIfAbsent(part, part);
                } else {
                    Map<String, Object> subMap = (Map<String, Object>) parentMap.get(part);
                    if (subMap == null) {
                        subMap = new IdentityHashMap<>();
                        parentMap.put(part, subMap);
                    }
                    parentMap = subMap;
                    subMap = null;
                }
            }
        }
        System.out.println();
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

    /** 将host分成2部分，dead和undead */
    private static void readFile() throws IOException {
//        Path path = Paths.get("hosts");
        Path path = Paths.get("C:\\Windows\\System32\\drivers\\etc\\hosts");
        List<String> list = Files.readAllLines(path, CHARSET);
        boolean isdead = false;
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (StringUtils.isBlank(s)) {
                continue;
            }
            if (s.equals(dead_str)) {
                isdead = true;
                continue;
            }
            if (s.startsWith("#")) {//跳过注释
                continue;
            }
            if (isdead) {
                dead.add(s);
            } else {
                undead.add(s);
            }
        }
        System.out.println();
    }
}
