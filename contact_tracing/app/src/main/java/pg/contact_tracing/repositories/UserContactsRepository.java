package pg.contact_tracing.repositories;

import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;

import pg.contact_tracing.datasource.local.SQLiteContactsStorage;
import pg.contact_tracing.datasource.local.SQLiteContactsStorageStrings;
import pg.contact_tracing.models.Contact;
import pg.contact_tracing.models.LocalStorageKey;
import pg.contact_tracing.utils.ContactAdapter;

public class UserContactsRepository {
    SQLiteContactsStorage storage;

    public UserContactsRepository(Context context) {
        storage = new SQLiteContactsStorage(context, LocalStorageKey.USER_CONTACTS_STORAGE.toString(), 1);
    }

    public void addNewContact(Contact contact) {
        ContentValues values = ContactAdapter.toTable(contact);
        storage.addNewContact(values);
    }

    // Selection example: "id=83hdw AND ..."
    public ArrayList<Contact> getContact(String selection, String groupBy, Integer limit) {
        ArrayList<Contact> contacts = new ArrayList<>();

        ArrayList<ContentValues> valuesList = storage.getContact(selection, null, groupBy, limit);

        for (ContentValues values : valuesList)
            contacts.add(ContactAdapter.toDomain(values));

        return contacts;
    }

    public void updateContact(Contact contact) {
        storage.updateContact(contact.getId(), ContactAdapter.toTable(contact));
    }
}
