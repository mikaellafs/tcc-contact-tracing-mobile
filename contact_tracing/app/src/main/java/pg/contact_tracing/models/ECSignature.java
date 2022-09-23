package pg.contact_tracing.models;

import pg.contact_tracing.utils.CryptoManager;

public class ECSignature {
    private byte[] signature, message;

    public ECSignature(byte[] message, byte[] signature) {
        this.message = message;
        this.signature = signature;
    }

    public byte[] getMessage() {
        return message;
    }

    public byte[] getSignature() {
        return signature;
    }

    public String toString(){
        return "Signature(signature: " + CryptoManager.bytesToString(signature) + ")";
    }
}
