package pg.contact_tracing.domain.usecases;

import android.content.Context;

import pg.contact_tracing.domain.errors.UserInformationNotFoundException;
import pg.contact_tracing.domain.models.LocalStorageKey;
import pg.contact_tracing.infra.repositories.LocalStorageRepository;

public class UserInformationsUseCase {
    LocalStorageRepository repository;

    public UserInformationsUseCase(Context context) {
        repository = new LocalStorageRepository(context, LocalStorageKey.USER_INFO_STORAGE);
    }
    public String getUUID() throws UserInformationNotFoundException {
        String uuid = repository.getValue(LocalStorageKey.USER_UUID);

        if (uuid == "") {
            throw new UserInformationNotFoundException("Could not find UUID");
        }
        return uuid;
    }

    public void saveUUID(String uuid) {
        repository.saveValue(LocalStorageKey.USER_UUID, uuid);
    }

    public String getPrivateKey() throws  UserInformationNotFoundException {
        String sk = repository.getValue(LocalStorageKey.USER_PRIVATE_KEY);

        if (sk == "") {
            throw new UserInformationNotFoundException("Could not find private key");
        }
        return sk;
    }

    public void savePrivateKey(String key) {
        repository.saveValue(LocalStorageKey.USER_PRIVATE_KEY, key);
    }

    public String getPublicKey() throws  UserInformationNotFoundException {
        String pk = repository.getValue(LocalStorageKey.USER_PUBLIC_KEY);

        if (pk == "") {
            throw new UserInformationNotFoundException("Could not find public key");
        }
        return pk;
    }

    public void savePublicKey(String key) {
        repository.saveValue(LocalStorageKey.USER_PRIVATE_KEY, key);
    }

    public int getAppManufacturer() {
        // TODO: get app hash to be used as manufacturer id in beacon service
        return 0x0118;
    }

    public void clearInfos() {
        repository.clearStorage();
    }
}
