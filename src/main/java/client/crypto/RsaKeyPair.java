package client.crypto;

public class RsaKeyPair {

	public final RsaKey privateKey;
	public final RsaKey publicKey;

	public RsaKeyPair(RsaKey privateKey, RsaKey publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}

}
