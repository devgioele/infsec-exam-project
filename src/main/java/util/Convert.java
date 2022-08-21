package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Convert {

	public static final Gson gson = new GsonBuilder().create();
	public static final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

	public static String toBase16(byte[] bytes) {
		byte[] hexChars = new byte[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	public static String toBase64Url(String str) {
		return Base64.getUrlEncoder().withoutPadding()
				.encodeToString(str.getBytes(StandardCharsets.UTF_8));
	}


	public static String fromBase64Url(String str) {
		return new String(Base64.getUrlDecoder().decode(str.getBytes(StandardCharsets.UTF_8)));
	}

	public static String toBase64(BigInteger num) {
		return toBase64(num.toByteArray());
	}

	public static BigInteger fromBase64(String str) {
		return new BigInteger(Base64.getDecoder().decode(str));
	}

	public static String toBase64(byte[] b) {
		return new String(Base64.getEncoder().encode(b));
	}

}
