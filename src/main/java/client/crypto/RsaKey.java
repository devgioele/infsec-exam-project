package client.crypto;

import http.RsaKey64;
import util.Convert;

import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RsaKey {

	public final BigInteger modulus;
	public final BigInteger exponent;

	public RsaKey(BigInteger modulus, BigInteger exponent) {
		this.modulus = modulus;
		this.exponent = exponent;
	}

	/**
	 * Applies this RSA key to the given byte array.
	 * Whether this is an encryption or a decryption is up to the method caller.
	 */
	public BigInteger apply(BigInteger secret) {
		// secret^exponent % modulus
		return secret.modPow(exponent, modulus);
	}

	public RsaKey64 to64() {
		return new RsaKey64(Convert.toBase64(modulus), Convert.toBase64(exponent));
	}

	public static RsaKey from64(RsaKey64 key) {
		return new RsaKey(Convert.fromBase64(key.modulus), Convert.fromBase64(key.exponent));
	}

	@Override
	public String toString() {
		return "RsaKey {" + "\nmodulus: " + modulus + "\nexponent: " + exponent + "\n}";
	}

}
