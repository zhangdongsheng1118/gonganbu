package com.yixin.tinode.api.encrypt;

import java.security.MessageDigest;

/**
 * 采用MD5加密解密
 * 
 */
public class MD5Util {

	/***
	 * MD5加密 生成32位md5�?
	 */
	public static String encrypt(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();

	}

	/**
	 * 加密解密算法 执行�?��加密，两次解�?
	 */
	private static String convertMD5(String inStr) {

		char[] a = inStr.toCharArray();
		for (int i = 0; i < a.length; i++) {
			a[i] = (char) (a[i] ^ 't');
		}
		String s = new String(a);
		return s;

	}
	/**
	 * MD5 解密
	 * @param md5 要解密的MD5
	 * @return
	 */
	public static String decrypt(String md5){
		String result = null;
		result = convertMD5(convertMD5(md5));
		return result;
	}
}
