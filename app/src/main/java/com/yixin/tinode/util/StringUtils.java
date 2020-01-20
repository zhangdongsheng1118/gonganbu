package com.yixin.tinode.util;

import java.util.List;

/**
 * @创建者 CSDN_LQR
 * @描述 字符串工具类
 */
public class StringUtils {
    /**
     * 得到不为空的字符串
     *
     * @param o
     * @return
     */
    public static String getNotNULLStr(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * 判断字符串或集合是否为空
     *
     * @param o
     * @return
     */
    public static boolean isEmpty(Object o) {
        if (o instanceof List)
            return ((List) o).size() == 0;
        return o == null || o.toString().equals("");
    }

    /**
     * 判断字符串是否为空（空格字符串也是blank）
     *
     * @param s
     * @return
     */
    public static boolean isBlank(final CharSequence s) {
        if (s == null) {
            return true;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 函数传入汉字的Unicode编码字符串，返回相应的汉字字符串
     *
     * @return
     */
    public static String decodeUnicode(final String utfString) {
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;

        while ((i = utfString.indexOf("\\u", pos)) != -1) {
            sb.append(utfString.substring(pos, i));
            if (i + 5 < utfString.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(utfString.substring(i + 2, i + 6), 16));
            }
        }
        sb.append(utfString.substring(pos, utfString.length()));

        return sb.toString();
    }

    //传入字母数字等组成的串，得到对应的unicode的一串数字,用于计算AV的roomID
    public static String GetUnicodeArrayFromString(String topicName){
        int topicName_len=topicName.length();
        String str_number="";
        if(topicName_len<5){
            return null;
        }
        for(int i=0;i<4;i++){
            int unicode_num=topicName.codePointAt(topicName_len-i-1);//charCodeAt(topicName_len-i-1);
            str_number=str_number+String.valueOf(unicode_num);
        }

        return str_number.toString();
    }

    //传入两个字符串，有两种方法，一种是按照大小排序，另外一种是做差并取绝对值，先用绝对值这种
    //str1和str2前后没有关系,都取后四个就行
    public static String GetOrderedUnicodeArrayFromString(String topicName1,String topicName2){
        int topicName_len1=topicName1.length();
        int topicName_len2=topicName2.length();
        String str_number="";
        if(topicName_len1<5||topicName_len2<5){
            return null;
        }
        for(int i=0;i<4;i++){
            int unicode_num1=topicName1.codePointAt(topicName_len1-i-1);//charCodeAt(topicName_len-i-1);
            int unicode_num2=topicName2.codePointAt(topicName_len2-i-1);
            str_number=str_number+String.valueOf(Math.abs(unicode_num1-unicode_num2));
        }

        return str_number.toString();
    }
}