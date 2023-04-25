package com.example.clientfx.Convert;

import com.example.clientfx.pojo.Message;

import java.util.*;

public class MyJsonConverter {

    public static void main(String[] args) {
        // 构造对象：ID为10001， names为 ["Alice", "Bob"]
        Map<String, Object> myMap = new HashMap<>();
        List<String> names = new ArrayList<>(Arrays.asList("Alice", "Bob"));
        myMap.put("id", "10001");
        myMap.put("names", names);

        // 将MyObject对象序列化为JSON字符串
        String jsonString = (String)MyJsonConverter.encode(myMap);
        System.out.println(jsonString); // -> {"id":"10001","names":["Alice","Bob"]}

        // 将JSON字符串反序列化为MyObject对象
        Map<String, Object> decMap = (HashMap<String, Object>)MyJsonConverter.decode(jsonString);
        String id = (String)decMap.get("id");
        List<String> decNames = (ArrayList<String>)decMap.get("names");
        System.out.println(id); // -> 10001
        System.out.println(decNames.toString()); // -> [Alice, Bob]
    }

    /**
     * 将Java对象转换为JSON对象
     *
     * @param obj Java对象
     * @return JSON对象
     */
    public static Object encode(Object obj) {
        if (obj == null) {
            return "null";
        }
        // 字符串类型
        if (obj instanceof String) {
            return "\"" + escape((String)obj) + "\"";
        }
        // 数组类型
        if (obj instanceof List<?>) {
            List<?> list = (List<?>)obj;
            StringBuffer sb = new StringBuffer();
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                // 递归处理内部元素
                sb.append(encode(list.get(i)));
                if (i != list.size()-1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        }
        // 对象类型
        if (obj instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>)obj;
            StringBuffer sb = new StringBuffer();
            sb.append("{");
            for (Object key : map.keySet()) {
                // 递归处理属性值
                sb.append("\"").append(key).append("\":")
                        .append(encode(map.get(key))).append(",");
            }
            if (map.keySet().size() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("}");
            return sb.toString();
        }
        // 其他类型直接转换为字符串
        return obj.toString();
    }


    /**
     * 将JSON对象转换为Java对象
     *
     * @param json JSON对象
     * @return Java对象
     */
    public static Object decode(String json) {
        if (json == null || "null".equals(json)) {
            return null;
        }
        if (json.startsWith("\"") && json.endsWith("\"")) {
            return unescape(json.substring(1, json.length()-1));
        }
        if (json.startsWith("[") && json.endsWith("]")) {
            List<Object> list = new ArrayList<>();
            String[] values = json.substring(1, json.length()-1).split(",");
            for (String value : values) {
                // 递归处理内部元素
                list.add(decode(value.trim()));
            }
            return list;
        }
        if (json.startsWith("{") && json.endsWith("}")) {
            Map<String, Object> map = new HashMap<>();
            String[] pairs = json.substring(1, json.length()-1).split(",");
            for (String pair : pairs) {
                // 分离键值对
                String[] kv = pair.split(":");
                String key = kv[0].trim().substring(1, kv[0].trim().length()-1);
                Object value = decode(kv[1].trim());
                map.put(key, value);
            }
            return map;
        }
        return json;
    }

    /**
     * 转义需要转义的字符
     *
     * @param str 需要转义的字符串
     * @return 转义后的字符串
     */
    private static String escape(String str) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
    }

    /**
     * 还原转义字符
     *
     * @param str 包含转义字符的字符串
     * @return 转义后的字符串
     */
    private static String unescape(String str) {
        StringBuffer sb = new StringBuffer(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && i < str.length()-1) {
                char nextChar = str.charAt(++i);
                switch (nextChar) {
                    case '\"': sb.append('\"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(nextChar); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}