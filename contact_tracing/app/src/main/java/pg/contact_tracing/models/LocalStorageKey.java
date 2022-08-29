package pg.contact_tracing.models;

public enum LocalStorageKey {
    USER_INFO_STORAGE {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_INFO_STORAGE";
        }

    },
    USER_UUID {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_UUID";
        }
    },
    USER_PRIVATE_KEY {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_SK";
        }
    },
    USER_PUBLIC_KEY {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_PK";
        }
    },
    USER_CONTACTS_STORAGE {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_CONTACTS_STORAGE";
        }
    },
    USER_CONTACTS_UUID_RECEIVED {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_CONTACTS_" + uuid;
        }
    };

    String uuid = "";
}
