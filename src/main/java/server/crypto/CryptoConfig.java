package server.crypto;

import util.Crypto;

public class CryptoConfig {

	public final byte[] secret;

	/** The duration of a JWT in milliseconds */
	public final int jwtLifetime;

	public final Crypto.HashingAlgorithm jwtAlgorithm;

	public CryptoConfig(byte[] secret, int jwtLifetime, Crypto.HashingAlgorithm jwtAlgorithm) {
		this.secret = secret;
		this.jwtLifetime = jwtLifetime;
		this.jwtAlgorithm = jwtAlgorithm;
	}

}
