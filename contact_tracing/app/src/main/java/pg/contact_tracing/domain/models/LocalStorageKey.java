package pg.contact_tracing.domain.models;

public enum LocalStorageKey {
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
    USER_CONTACTS_UUIDS {
        @Override
        public String toString() {
            return "CONTACT_TRACING_USER_CONTACTS_UUIDS";
        }
    }
}
