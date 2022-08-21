package client.crypto;

import client.util.ClientLogger;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Rsa {

	/**
	 * Generates an RSA key pair using the Miller-Rabin primality test and Euler's totient.
	 * The size of the keys is determined by the given bitLength parameter.
	 * <br><br>
	 * Max. bit length of modulus = bitLength <br>
	 * Max. bit length of public exponent = bitLength <br>
	 * Max. bit length of private exponent = bitLength + 1 <br>
	 * <br>
	 * The number of digits required in any other base B is ceil(bits/log_2(B)).
	 * For example, the number of digits that a number with a bit length of 20 required in base 64 is ceil(20/log_2(64)) = 4.
	 * <br><br>
	 * Let |x| denote the bit length of x. <br>
	 * |p| = |q| = bitLength / 2 <br>
	 * |n| <= |p| + |q| <br>
	 * |n| <= bitLength <br>
	 * |totient(n)| = |p-1| + |q-1| <= |p| + |q| <br>
	 * |totient(n)| <= |p| + |q| <br>
	 * |totient(n)| <= bitLength <br>
	 * |e| < |totient(n)| <br>
	 * |e| <= |p| + |q| <br>
	 * |e| <= bitLength <br>
	 * |d| <= |totient(n)+1| <br>
	 * |totient(n)+1| <= |totient(n)| + 1 <br>
	 * |d| <= |totient(n)| + 1 <br>
	 * |d| <= |p| + |q| + 1 <br>
	 * |d| <= bitLength + 1
	 * <br><br>
	 */
	public static RsaKeyPair genKeyPair(int bitLength) {
		SecureRandom rnd = new SecureRandom();
		// Two random different primes of the right bit length
		BigInteger p = null, q = null;
		while (p == null || p.equals(q)) {
			p = BigInteger.probablePrime(bitLength / 2, rnd);
			q = BigInteger.probablePrime(bitLength / 2, rnd);
		}
		ClientLogger.printf("p = %s%n", p);
		ClientLogger.printf("q = %s%n", q);
		// Modulus
		BigInteger n = p.multiply(q);
		ClientLogger.printf("n = %s%n", n);
		// Euler's totient
		BigInteger totient = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		ClientLogger.printf("Euler's totient = %s%n", totient);
		// Public exponent
		// = coprime e such that 1 < e < totient
		// Shortcut: Choose a prime that is not a divisor of the totient
		// Shortcut: Choose a prime larger than max(p, q)
		BigInteger e = max(p, q).nextProbablePrime();
		ClientLogger.printf("e = %s%n", e);
		// Private exponent
		// = the multiplicative inverse of e mod totient
		BigInteger d = e.modInverse(totient);
		ClientLogger.printf("d = %s%n", d);
		RsaKey publicKey = new RsaKey(n, e);
		RsaKey privateKey = new RsaKey(n, d);
		return new RsaKeyPair(privateKey, publicKey);
	}

	private static BigInteger max(BigInteger a, BigInteger b) {
		if(a.compareTo(b) > 0) {
			return a;
		}
		return b;
	}

}
