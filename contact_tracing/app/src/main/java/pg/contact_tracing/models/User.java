package pg.contact_tracing.models;

public class User {
    private String id;
    private String publicKey;
    private String password = "";

    public User(String id, String publicKey) {
        this.id = id;
        this.publicKey = publicKey;
    }

    public User(String id, String publicKey, String password) {
        this.id = id;
        this.publicKey = publicKey;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String toString(){
        return "User(id: " + id + ", publicKey: " + publicKey + ")";
    }

    public String getPassword() {
        return password;
    }
}
