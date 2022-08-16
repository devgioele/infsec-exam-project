package util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Convert {

	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

	public static String toBase16(byte[] bytes) {
		byte[] hexChars = new byte[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	public static String toBase64(String str) {
		return Base64.getEncoder().encodeToString(str.getBytes());
	}

	public static String fromBase64(String str) {
		return new String(Base64.getDecoder().decode(str));
	}

}
