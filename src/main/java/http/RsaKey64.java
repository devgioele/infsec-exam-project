package http;

/** Base 64 version of RsaKey. Convenient for efficient transmission. */
public class RsaKey64 {

	public final String modulus;
	public final String exponent;

	public RsaKey64(String modulus, String exponent) {
		this.modulus = modulus;
		this.exponent = exponent;
	}

	@Override
	public String toString() {
		return "RsaKey64 {" + "\nmodulus: " + modulus + "\nexponent: " + exponent + "\n}";
	}

}
