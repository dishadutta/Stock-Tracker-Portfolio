package main.java.utility;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class SecurityUtils {
	static final byte[] salt = new BigInteger("39e858f86df9b909a8c87cb8d9ad599", 16).toByteArray();

	public static String hash(String data) {
		KeySpec spec = new PBEKeySpec(data.toCharArray(), salt, 64, 128);
		SecretKeyFactory f;
		byte[] hash;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = f.generateSecret(spec).getEncoded();
			Base64.Encoder enc = Base64.getEncoder();
			return enc.encodeToString(hash);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
			e1.printStackTrace();
		}
		return null;
	}
}
