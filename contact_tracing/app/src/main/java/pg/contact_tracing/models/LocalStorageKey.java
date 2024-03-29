package pg.contact_tracing.models;

public enum LocalStorageKey {
    USER_INFO_STORAGE {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_INFO_STORAGE";
        }

    },
    DEVICE_ID {
        @Override
        public String toString() {
            return "CONTACT_TRACING_DEVICE_ID";
        }
    },
    USER_ID {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_ID";
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
    SERVER_PUBLIC_KEY {
        @Override
        public String toString() {
            return "CONTACT_TRACING_SERVER_PK";
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
