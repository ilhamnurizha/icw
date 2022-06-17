package io.icw.pow;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryption {
	public static String getSha256(final String strText) {
		return encryption(strText, "SHA-256");
	}

	public static String getSha512(final String strText) {
		return encryption(strText, "SHA-512");
	}
	
	public static String getMd5(String data) {
		return encryption(data, "MD5");
	}

	private static String encryption(final String strText, final String strType) {
		String result = null;
		if (strText != null && strText.length() > 0) {
			try {
				MessageDigest messageDigest = MessageDigest.getInstance(strType);
				messageDigest.update(strText.getBytes());
				byte[] byteBuffer = messageDigest.digest();
				StringBuilder strHexString = new StringBuilder();
				for (byte aByteBuffer : byteBuffer) {
					String hex = Integer.toHexString(0xff & aByteBuffer);
					if (hex.length() == 1) {
						strHexString.append('0');
					}
					strHexString.append(hex);
				}
				result = strHexString.toString();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}