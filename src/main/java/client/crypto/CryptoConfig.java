package client.crypto;

import java.util.*;

public class CryptoConfig {

	public final int rsaKeySize;
	private final Map<String, RsaKey> privateKeys = new HashMap<>();

	public CryptoConfig(int rsaKeySize) {
		this.rsaKeySize = rsaKeySize;
	}

	public void addPrivateKey(String id, RsaKey key) {
		privateKeys.put(id, key);
	}

	public Optional<RsaKey> getPrivateKey(String id) {
		return privateKeys.entrySet().stream().filter(entry -> entry.getKey().equals(id))
				.map(Map.Entry::getValue).findAny();
	}

}
