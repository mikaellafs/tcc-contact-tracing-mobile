package pg.contact_tracing.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import pg.contact_tracing.datasource.sqlite.SQLiteContactsStorage;
import pg.contact_tracing.models.Contact;
import pg.contact_tracing.models.LocalStorageKey;
import pg.contact_tracing.utils.ContactAdapter;

public class UserContactsRepository {
    private static String USER_CONTACTS_REPOSITORY_LOG = "USER_CONTACTS_REPOSITORY";
    SQLiteContactsStorage storage;

    public UserContactsRepository(Context context) {
        storage = new SQLiteContactsStorage(context, LocalStorageKey.USER_CONTACTS_STORAGE.toString(), 1);
    }

    public void addNewContact(Contact contact) {
        Log.i(USER_CONTACTS_REPOSITORY_LOG, "Add new contact: " + contact.toString());

        ContentValues values = ContactAdapter.toTable(contact);
        storage.addNewContact(values);
    }

    // Selection example: "id=83hdw AND ..."
    public ArrayList<Contact> getContact(String selection, String sortBy, String groupBy, Integer limit) {
        Log.i(USER_CONTACTS_REPOSITORY_LOG, "Get contact with selection "+ selection + " ordered by " + sortBy + " with limit of " + limit);
        ArrayList<Contact> contacts = new ArrayList<>();

        ArrayList<ContentValues> valuesList = storage.getContact(selection, null, sortBy, groupBy, limit);
        Log.i(USER_CONTACTS_REPOSITORY_LOG, "Selection got " + valuesList.size() + " results");

        for (ContentValues values : valuesList)
            contacts.add(ContactAdapter.toDomain(values));

        return contacts;
    }

    public void updateContact(Contact contact) {
        Log.i(USER_CONTACTS_REPOSITORY_LOG, "Update contact " + contact.toString());
        storage.updateContact(contact.getId(), ContactAdapter.toTable(contact));
    }
}
