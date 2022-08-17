package server.crypto;

public class CryptoConfig {

	public final byte[] secret;

	/** The duration of a JWT in milliseconds */
	public final int jwtLifetime;

	public final String jwtAlgorithm;

	public CryptoConfig(byte[] secret, int jwtLifetime, String jwtAlgorithm) {
		this.secret = secret;
		this.jwtLifetime = jwtLifetime;
		this.jwtAlgorithm = jwtAlgorithm;
	}

}
