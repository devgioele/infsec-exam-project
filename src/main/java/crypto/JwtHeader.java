package crypto;

public class JwtHeader {
	public final String alg;
	public final String typ = "JWT";

	public JwtHeader(String alg) {
		this.alg = alg;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		JwtHeader jwtHeader = (JwtHeader) o;

		if (!alg.equals(jwtHeader.alg)) return false;
		return typ.equals(jwtHeader.typ);
	}

	@Override
	public int hashCode() {
		int result = alg.hashCode();
		result = 31 * result + typ.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return "JwtHeader{" + "alg='" + alg + '\'' + ", typ='" + typ + '\'' + '}';
	}

}
