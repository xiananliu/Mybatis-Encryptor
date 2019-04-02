package com.github.lxn.mybatisEncryptor.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * AES对称加密工具类
 * 
 * 
 * 加密方法：encrpt 解密方法：decrypt
 * 
 * @author yfchenmin
 * @version 1.0
 */
public class AESCoder {
	private static final String KEY_ALGORITHM = "AES";
	private static final String CIPHER_ALOGORITHM = "AES/ECB/PKCS5Padding";

	/**
	 * 转换密钥
	 * 
	 * @param key
	 * @return Key
	 */
	private static Key toKey(byte[] key) {
		SecretKey secretKey = new SecretKeySpec(key, KEY_ALGORITHM);
		return secretKey;
	}

	/**
	 * 加密
	 * 
	 * @param data
	 *            所要加密的字符串；
	 * @param key
	 *            用来加密的密钥；
	 * @return String 返回加密后的字符串；
	 * @throws Exception
	 */
	public static String encrpt(String data, String key) throws Exception {
		Key k = toKey(key.getBytes());
		Cipher cipher = Cipher.getInstance(CIPHER_ALOGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, k);
		return StringUtils.parseByte2HexStr(cipher.doFinal(data
				.getBytes(StandardCharsets.UTF_8)));
	}



	/**
	 * 解密
	 * 
	 * @param data
	 *            所要解密的字符串；
	 * @param key
	 *            用来解密的密钥；
	 * @return String 返回解密后的字符串；
	 * @throws Exception
	 */
	public static String decrypt(String data, String key) throws Exception {
		Key k = toKey(key.getBytes());
		Cipher cipher = Cipher.getInstance(CIPHER_ALOGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, k);
		return new String(cipher.doFinal(StringUtils
				.parseHexStr2Byte(data)), StandardCharsets.UTF_8);
	}

      
}
