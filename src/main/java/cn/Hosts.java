package cn;

import com.google.gson.Gson;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


@SuppressWarnings("unchecked")
public class Hosts {
    private static final boolean test = false;
    private static final Charset CHARSET = Charset.forName("GB2312");
    private static final String local_ip = "::";
    private static final String dead_str = "#dead";

    private static final List<String> undead = new ArrayList<>();
    private static final List<String> undeadHostRecord = new ArrayList<>();
    private static final IdentityHashMap<String, Object> undead_urlMapRoot = new IdentityHashMap<>();

    /** hosts广告部分每一条记录，不包括注释 */
    private static final List<String> dead = new ArrayList<>();
    private static final List<String> deadHostRecord = new ArrayList<>();
    /** 和 {@link Hosts#dead} */
    private static final IdentityHashMap<String, Object> dead_urlMapRoot = new IdentityHashMap<>();

    /** 记录不同域名的共同注释 */
    private static final Map<String, Set<String>> urlGroupComment = new HashMap<>();
    private static final Set<String> urlGroupComment_domain = new HashSet<>();

    static {
        urlGroupComment.put("#友盟", new HashSet<>(Arrays.asList("cnzz.com")));
        urlGroupComment.put("#谷歌", new HashSet<>(Arrays.asList("google-analytics.com", "googleadservices.com", "googlesyndication.com", "googletagmanager.com")));
        for (Set<String> value : urlGroupComment.values()) {
            urlGroupComment_domain.addAll(value);
        }

    }

    /** 记录每条记录的注释 */
    private static final Map<String, String> urlComment = new HashMap<>();

    private Hosts() {}

    public static void main(String[] args) throws IOException {
        readFile();

        //region 广告部分
        deadHostToIdentityHashMap(dead, dead_urlMapRoot);
        cleanDuplicateUrl(dead_urlMapRoot);
        dead.clear();
        orderByDomain(dead_urlMapRoot, new ArrayList<>(), dead);
        buildHostRecord(dead, deadHostRecord);
        //endregion 广告部分

        // region 普通hosts部分
        buildHostRecord(undead, undeadHostRecord);
        // endregion 普通hosts部分

        System.out.println();
        outputHostFile();
    }

    /** 输出文件 */
    private static void outputHostFile() {
        List<String> output = new ArrayList<>(undeadHostRecord);
        output.add("\n" + dead_str + "\n");
        output.addAll(deadHostRecord);
        Path path = test ? Paths.get("hosts1") : Paths.get("hosts");
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                FileChannel channel = fos.getChannel();
        ) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 4 * 10);
            String s = String.join("", output);
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
//                String urlcomment = MapUtils.getString(urlComment, s, "");
//                s = local_ip + " " + s + urlcomment + "\n";
            }
            source.add(i, s);
            source.remove(i + 1);
        }
    }

    /** 生成host记录 */
    private static void buildHost2() {
//        Set<String> keySet = urlGroupComment.keySet();
//        for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext(); ) {
//            String key = iterator.next();
//            int bestMatch = -1;
//            int i = dead.indexOf(key);
//            if (i == -1) {
//                continue;
//            }
////            dead.add(i, urlGroupComment.get(key));
////            for (int i = 0; i < dead.size(); i++) {
////                String s = dead.get(i);
////            }
//        }
        System.out.println();
    }

    /** 生成真正的hosts记录 */
    private static void buildHostRecord(List<String> source, List<String> result) {
        if (source == undead) {
            for (String s : source) {
                result.add(s + "\n");
            }
        } else {
            for (String host : source) {
                String comment = urlComment.get(host);
                String s = null;
                if (comment != null) {
                    s = local_ip + " " + host + " " + comment + "\n";
                } else {
                    s = local_ip + " " + host + "\n";
                }
                result.add(s);
            }
        }
//        System.out.println();
    }

    /** 根据域排序 */
    static void orderByDomain(IdentityHashMap<String, Object> source, List<String> urlPartList, List<String> urlListRoot) {
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
                orderByDomain((IdentityHashMap<String, Object>) o, urlPartList, urlListRoot);
                urlPartList.remove(urlPartList.size() - 1);
            }
        }
        System.out.println();
    }


    /** 去除重复的网址 */
    static void cleanDuplicateUrl(IdentityHashMap<String, Object> source) {
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
    private static void deadHostToIdentityHashMap(List<String> source, IdentityHashMap<String, Object> mapSource) {
        List<String> urlPartList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\s+");
        Pattern pattern1 = Pattern.compile("\\.");
        for (int i = 0; i < source.size(); i++) {
            String hostRecord = source.get(i);
            if (hostRecord.isEmpty() || hostRecord.startsWith("#")) {
                continue;
            }
            String[] hr = pattern.split(hostRecord);//格式:ip 主机 #注释
            String ip = hr[0];
            String domain = hr[1];
            if (hr.length == 3) {//部分host后面跟着#注释
                String comment = hr[2];
                urlComment.put(domain, comment);
            }

            String[] domainPart = pattern1.split(domain);
            Map<String, Object> parentMap = mapSource;
            for (int j = domainPart.length - 1; j >= 0; j--) {
                String part = getPart(domainPart[j], urlPartList);//避免同一个层级key重复
                if (j == 0) {
                    //避免 a.baidu.com  1.a.baidu.com的情况
                    part += "";//new String(part);
                    parentMap.putIfAbsent(part, part);
                } else {
                    Map<String, Object> subMap = (Map<String, Object>) parentMap.get(part);
                    if (subMap == null) {
                        subMap = new IdentityHashMap<>();
                        parentMap.put(part, subMap);
                    }
                    parentMap = subMap;
                }
            }
        }
        System.out.println(new Gson().toJson(mapSource));
    }

    /** 将每个域名根据点号分割出每个域 */
    @Deprecated
    static void hostToIdentityHashMap(List<String> source, IdentityHashMap<String, Object> mapSource) {
        List<String> urlPartList = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {
            String domain = source.get(i);

            String[] domainPart = domain.split("\\.");
            Map<String, Object> parentMap = mapSource;
            for (int j = domainPart.length - 1; j >= 0; j--) {
                String part = getPart(domainPart[j], urlPartList);//避免同一个层级key重复
                if (j == 0) {
                    //避免 a.baidu.com  1.a.baidu.com的情况
                    part += "";//new String(part);
                    parentMap.putIfAbsent(part, part);
                } else {
                    Map<String, Object> subMap = (Map<String, Object>) parentMap.get(part);
                    if (subMap == null) {
                        subMap = new IdentityHashMap<>();
                        parentMap.put(part, subMap);
                    }
                    parentMap = subMap;
                }
            }
        }
//        System.out.println(new Gson().toJson(mapSource));
    }


    private static String getPart(String part, List<String> urlPartList) {
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
        Path path = test ? Paths.get("hosts") : Paths.get("C:\\Windows\\System32\\drivers\\etc\\hosts");

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
            if (isdead) {
                if (s.startsWith("#")) {//跳过注释
                    continue;
                }
                dead.add(s);
            } else {
                undead.add(s);
            }
        }
        if (!isdead) {
            throw new IllegalArgumentException("没找到广告ip,检查hosts文件");
        }
        System.out.println();
    }

}
