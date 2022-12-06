package pg.contact_tracing.datasource.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import pg.contact_tracing.models.LocalStorageKey;

public class SharedPreferencesStorage {
    SharedPreferences storage;

    public SharedPreferencesStorage(Context context, LocalStorageKey STORAGE_NAME) {
        storage = context.getSharedPreferences(STORAGE_NAME.toString(), Context.MODE_PRIVATE);
    }

    public String getValue(LocalStorageKey key) {
        String value = storage.getString(key.toString(), "");
        return value;
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
