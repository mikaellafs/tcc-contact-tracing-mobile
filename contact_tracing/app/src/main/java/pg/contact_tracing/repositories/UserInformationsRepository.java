package pg.contact_tracing.repositories;

import android.content.Context;

import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.LocalStorageKey;
import pg.contact_tracing.datasource.local.SharedPreferencesStorage;

public class UserInformationsRepository {
    SharedPreferencesStorage storage;

    public UserInformationsRepository(Context context) {
        storage = new SharedPreferencesStorage(context, LocalStorageKey.USER_INFO_STORAGE);
    }
    public String getUUID() throws UserInformationNotFoundException {
        return "2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6";
//        String uuid = storage.getValue(LocalStorageKey.USER_UUID);
//
//        if (uuid == "") {
//            throw new UserInformationNotFoundException("Could not find UUID");
//        }
//        return uuid;
    }

    public void saveUUID(String uuid) {
        storage.saveValue(LocalStorageKey.USER_UUID, uuid);
    }

    public String getPrivateKey() throws  UserInformationNotFoundException {
        String sk = storage.getValue(LocalStorageKey.USER_PRIVATE_KEY);

        if (sk == "") {
            throw new UserInformationNotFoundException("Could not find private key");
        }
        return sk;
    }

    public void savePrivateKey(String key) {
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
        storage.saveValue(LocalStorageKey.USER_PRIVATE_KEY, key);
    }

    public int getAppManufacturer() {
        // TODO: get app hash to be used as manufacturer id in beacon service
        return 0x0118;
    }

    public void clearInfos() {
        storage.clearStorage();
    }
}
