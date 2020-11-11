package com.wwc2.networks.server.protection;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wwc2.networks.server.utils.LogUtils;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class EncryUtil {
	public static String handleRSA(TreeMap<String, Object> map,
			String privateKey) {
		StringBuffer sbuffer = new StringBuffer();
		for (Entry<String, Object> entry : map.entrySet()) {
			sbuffer.append(entry.getValue());
		}
		String signTemp = sbuffer.toString();

		String sign = "";
		if (!TextUtils.isEmpty(privateKey)) {
			sign = RSA.sign(signTemp, privateKey);
		}
		return sign;
	}

	public static boolean checkDecryptAndSign(String data, String encrypt_key,
			String clientPublicKey, String serverPrivateKey) throws Exception {

		String AESKey = "";
		try {
			AESKey = RSA.decrypt(encrypt_key, serverPrivateKey);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtils.e(e.getMessage());
			return false;
		}

		String realData = ConvertUtils.hexStringToString(AES.decryptFromBase64(data, AESKey));
		TreeMap<String, String> map = new Gson().fromJson(realData,
				new TypeToken<TreeMap<String, String>>(){}.getType());
		String sign = map.get("sign").trim();
		StringBuffer signData = new StringBuffer();
		Iterator<Entry<String, String>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			if (entry.getKey().equals("sign")) {
				continue;
			}
			signData.append(entry.getValue() == null ? "" : entry.getValue());
		}
		boolean result = RSA.checkSign(signData.toString(), sign,
				clientPublicKey);

		return result;
	}

	public static String handleHmac(TreeMap<String, String> map, String hmacKey) {
		StringBuffer sbuffer = new StringBuffer();
		for (Entry<String, String> entry : map.entrySet()) {
			sbuffer.append(entry.getValue());
		}
		String hmacTemp = sbuffer.toString();

		String hmac = "";
		if (TextUtils.isEmpty(hmacKey)) {
			hmac = Digest.hmacSHASign(hmacTemp, hmacKey, Digest.ENCODE);
		}
		return hmac;
	}
}
