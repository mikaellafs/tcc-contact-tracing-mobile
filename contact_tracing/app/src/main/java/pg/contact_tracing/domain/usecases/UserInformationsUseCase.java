package pg.contact_tracing.domain.usecases;

public class UserInformationsUseCase {
    public String getUUID() {
        // TODO: get UUID in local storage repository
        return "2f234454-cf6d-4a0f-adf2-f4911ba9ffa6";
    }

    public void saveUUID() {
        // TODO: save UUID in local storage repository
    }

    public void removeUUID() {
        // TODO: remove UUID in local storage repository
    }

    public String getKeyPair() {
        // TODO: get key pair in local storage repository
        return "";
    }

    public void saveKeyPair() {
        // TODO: save key pair in local storage repository
    }

    public int getAppManufacturer() {
        // TODO: get app hash to be used as manufacturer id in beacon service
        return 0x0118;
    }
}
