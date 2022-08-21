package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto {

	/**
	 * See all possible algorithms in the
	 * <a href="https://docs.oracle.com/javase/9/docs/specs/security/standard-names.html#messagedigest-algorithms">
	 * Java Security Standard Algorithm Names Specification
	 * </a>.
	 */
	public enum HashingAlgorithm {
		SHA256("SHA-256");

		private final String name;

		HashingAlgorithm(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	public static final String jwtId = "Authorization";
	public static final String bearerPrefix = "Bearer ";

	public static String hash(HashingAlgorithm algorithm, byte[]... input) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(algorithm.toString());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		byte[] total = Common.concatByteArrays(input);
		md.update(total);
		return Convert.toBase16(md.digest());
	}

}
