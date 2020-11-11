package com.wwc2.networks.server.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Administrator on 2018/4/26.
 */

public class SignUtils {
    public static String createSign(String appkey, Map<String, Object> params) {
        try {
            Map<String, Object> map = sortMapByKey(params);
            StringBuffer sb = new StringBuffer();
            Set<String> keySet = map.keySet();
            Iterator<String> it = keySet.iterator();
            int size = map.size();
            int count = 0;
            while (it.hasNext()) {
                count++;
                String k = it.next();
                String v = (String) map.get(k);
                if (null != v && !"".equals(v)
                        && !"sign".equals(k)) {
                    if (count == size) {
                        sb.append(k + "=" + v);
                    } else {
                        sb.append(k + "=" + v + "&");
                    }
                }
            }
            String sign1 = Utils.md5(sb.toString());
            String sign = Utils.md5(sign1 + appkey);
            return sign;
        } catch (Exception ex) {
            LogUtils.e(ex);
        }
        return null;
    }

    public static Map<String, Object> sortMapByKey(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, Object> sortMap = new TreeMap<String, Object>(
                new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;
    }

    private static class MapKeyComparator implements Comparator<String> {
        @Override
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }
    }
}
