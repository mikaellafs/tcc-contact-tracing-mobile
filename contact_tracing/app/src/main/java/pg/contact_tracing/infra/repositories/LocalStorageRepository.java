package pg.contact_tracing.infra.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import pg.contact_tracing.domain.models.LocalStorageKey;

public class LocalStorageRepository {
    SharedPreferences storage;

    public LocalStorageRepository(Context context, LocalStorageKey STORAGE_NAME) {
        storage = context.getSharedPreferences(STORAGE_NAME.toString(), Context.MODE_PRIVATE);
    }

    public String getValue(LocalStorageKey key) {
        return storage.getString(key.toString(), "");
    }

    public void saveValue(LocalStorageKey key, String value) {
        SharedPreferences.Editor editor = storage.edit();
        editor.putString(key.toString(), value);
        editor.commit();
    }

    public void removeKey(LocalStorageKey key) {
        SharedPreferences.Editor editor = storage.edit();
        editor.remove(key.toString());
        editor.commit();
    }

    public void clearStorage() {
        SharedPreferences.Editor editor = storage.edit();
        editor.clear();
        editor.commit();
    }
}
