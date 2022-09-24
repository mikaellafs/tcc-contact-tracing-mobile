package pg.contact_tracing.utils;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import pg.contact_tracing.di.DI;
import pg.contact_tracing.exceptions.InstanceNotRegisteredDIException;
import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.ECSignature;
import pg.contact_tracing.repositories.UserInformationsRepository;

public class CryptoManager {
    private static final String CRYPTO_MANAGER_LOG = "CRYPTO_MANAGER";
    private static final String CRYPTO_ALGORITHM = "EC";
    private static final String SIGNATURE_ALGORITHM = "SHA1WithECDSA";
    private static final int KEY_SIZE = 256;

    private UserInformationsRepository userInformationsRepository;

    public CryptoManager() {
        try {
            userInformationsRepository = DI.resolve(UserInformationsRepository.class);
        } catch (InstanceNotRegisteredDIException e) {
            Log.e(CRYPTO_MANAGER_LOG, e.toString());
            userInformationsRepository = null;
            System.exit(1);
        }
    }

    public static String bytesToString(byte[] key) {
        return Base64.encodeToString(key, Base64.DEFAULT).replace("\n", "");
    }

    public static byte[] stringToBytes(String key) {
        return Base64.decode(key, Base64.DEFAULT);
    }

    public String generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(CRYPTO_ALGORITHM);
        generator.initialize(KEY_SIZE, new SecureRandom());

        KeyPair pair = generator.generateKeyPair();

        String pk = bytesToString(pair.getPublic().getEncoded());
        String sk = bytesToString(pair.getPrivate().getEncoded());

        // Save sk and pk in repository
        userInformationsRepository.savePublicKey(pk);
        userInformationsRepository.savePrivateKey(sk);

        Log.i(CRYPTO_MANAGER_LOG, "Public key generated:" + pk);
        Log.i(CRYPTO_MANAGER_LOG, "Private key generated:" + sk);

        return pk;
    }

    public ECSignature sign(String value)
            throws UserInformationNotFoundException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException,
            SignatureException {
        Log.i(CRYPTO_MANAGER_LOG, "Signing message: " + value);
        byte[] data = value.getBytes(StandardCharsets.UTF_8);

        String skString = userInformationsRepository.getPrivateKey();
        Log.i(CRYPTO_MANAGER_LOG, "Recovered private key: " + skString);
        PrivateKey sk = generatePrivateKeyFrom(stringToBytes(skString));

        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initSign(sk);
        sig.update(data);

        byte[] signatureBytes = sig.sign();
        Log.i(CRYPTO_MANAGER_LOG, "Signature: " + bytesToString(signatureBytes));

        return new ECSignature(data, signatureBytes);
    }

    public boolean verifySign(ECSignature s)
            throws UserInformationNotFoundException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException,
            SignatureException {
        Log.i(CRYPTO_MANAGER_LOG, "Verifying signature: " + bytesToString(s.getSignature()));

        String pkString = userInformationsRepository.getPublicKey();
        Log.i(CRYPTO_MANAGER_LOG, "Recovered public key: " + pkString);
        PublicKey pk = generatePublicKeyFrom(stringToBytes(pkString));

        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(pk);
        sig.update(s.getMessage());

        return sig.verify(s.getSignature());
    }

    private PrivateKey generatePrivateKeyFrom(byte[] sk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance(CRYPTO_ALGORITHM);
        return kf.generatePrivate(new PKCS8EncodedKeySpec(sk));
    }

    private PublicKey generatePublicKeyFrom(byte[] pk) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory kf = KeyFactory.getInstance(CRYPTO_ALGORITHM);
        return kf.generatePublic(new X509EncodedKeySpec(pk));
    }
}
