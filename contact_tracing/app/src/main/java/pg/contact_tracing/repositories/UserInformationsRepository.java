package pg.contact_tracing.repositories;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.LocalStorageKey;
import pg.contact_tracing.datasource.sharedpreferences.SharedPreferencesStorage;
import pg.contact_tracing.utils.CryptoManager;

public class UserInformationsRepository {
    private static final String USER_INFORMATIONS_REPOSITORY_LOG = "USER_INFORMATIONS_REPOSITORY";
    SharedPreferencesStorage storage;
    Context context;

    public UserInformationsRepository(Context context) {
        this.context = context;
        storage = new SharedPreferencesStorage(context, LocalStorageKey.USER_INFO_STORAGE);
    }

    // Get android id sha256 encoded hexadecimal string
    public String getID() {
        String id = storage.getValue(LocalStorageKey.USER_ID);

        if (id == "") {
            String androidId = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
            Log.i(USER_INFORMATIONS_REPOSITORY_LOG, "Android ID: " + androidId);

            try {
                byte[] hashAndroidID = CryptoManager.toSha128(androidId);
//                String hexAndroidID = Hex.encodeHexString(hashAndroidID);
                String hexAndroidID = new String(Hex.encodeHex(hashAndroidID));

                saveID(hexAndroidID);
                id = hexAndroidID;
                Log.i(USER_INFORMATIONS_REPOSITORY_LOG, "Hash HEX Android ID: " + hexAndroidID);
            } catch (NoSuchAlgorithmException e) {
                Log.e(USER_INFORMATIONS_REPOSITORY_LOG, "Failed to get Android ID hash");
                id = "";
            }
        }

        Log.i(USER_INFORMATIONS_REPOSITORY_LOG, "Get user id: " + id);
        return id;
    }

    public void saveID(String id) {
        storage.saveValue(LocalStorageKey.USER_ID, id);
    }

    public String getPrivateKey() throws UserInformationNotFoundException {
        String sk = storage.getValue(LocalStorageKey.USER_PRIVATE_KEY);

        if (sk == "") {
            throw new UserInformationNotFoundException("Could not find private key");
        }
        return sk;
    }

    public void savePrivateKey(String key) {
        Log.i(USER_INFORMATIONS_REPOSITORY_LOG, "Private: " + key);
        storage.saveValue(LocalStorageKey.USER_PRIVATE_KEY, key);
    }

    public String getPublicKey() throws UserInformationNotFoundException {
        String pk = storage.getValue(LocalStorageKey.USER_PUBLIC_KEY);

        if (pk == "") {
            throw new UserInformationNotFoundException("Could not find public key");
        }
        return pk;
    }

    public void savePublicKey(String key) {
        Log.i(USER_INFORMATIONS_REPOSITORY_LOG, "Public: " + key);
        storage.saveValue(LocalStorageKey.USER_PUBLIC_KEY, key);
    }

    public String getServerPublicKey() throws UserInformationNotFoundException {
        String pk = storage.getValue(LocalStorageKey.SERVER_PUBLIC_KEY);

        if (pk == "") {
            throw new UserInformationNotFoundException("Could not find server public key");
        }
        return pk;
    }

    public void saveServerPublicKey(String key) {
        storage.saveValue(LocalStorageKey.SERVER_PUBLIC_KEY, key);
    }

    public int getAppManufacturer() {
        // TODO: get app hash to be used as manufacturer id in beacon service
        return 0x0118;
    }

    public void clearInfos() {
        storage.clearStorage();
    }
}
