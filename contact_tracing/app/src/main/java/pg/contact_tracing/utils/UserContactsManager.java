package pg.contact_tracing.utils;

import static pg.contact_tracing.datasource.sqlite.SQLiteContactsStorageStrings.ORDER_BY_LAST_CONTACT_DESC;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;

import pg.contact_tracing.di.DI;
import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.Contact;
import pg.contact_tracing.models.ECSignature;
import pg.contact_tracing.models.RiskNotification;
import pg.contact_tracing.repositories.UserContactsRepository;

public class UserContactsManager {
    private static String USER_CONTACTS_MANAGER_LOG = "USER_CONTACTS_MANAGER";
    private final long MAX_TIME_DIFF = 2 * 60 * 1000; // 2 minutos
    UserContactsRepository repository;
    CryptoManager cryptoManager;

    public UserContactsManager() {
        try {
            repository = DI.resolve(UserContactsRepository.class);
            cryptoManager = DI.resolve(CryptoManager.class);
        } catch(Exception e) {
            Log.e(USER_CONTACTS_MANAGER_LOG, "Failed to resolve UserContacts repository or CryptoManager: " + e);
            repository = null;
        }
    }

    public UserContactsManager(UserContactsRepository repo, CryptoManager crypto) {
        repository = repo;
        cryptoManager = crypto;
    }
    public void saveBeacon(Beacon beacon, Context context) {
        Log.i(USER_CONTACTS_MANAGER_LOG, "Save beacon: " + beacon.toString());
        long now = new Date().getTime();

        // Get last contact saved if it exists
        String selection = "token='" + beacon.getId1().toString() + "'";
        ArrayList<Contact> contacts = repository.getContact(selection, ORDER_BY_LAST_CONTACT_DESC, null, 1);

        Log.i(USER_CONTACTS_MANAGER_LOG, "Got " + contacts.size() + " contacts from storage with selection " + selection + " with limit of 1");
        if (contacts.size() > 0) {
            Contact contact = contacts.get(0);

            // Check if user is still in contact
            long time_diff = now - contact.getLastContactTimestamp();
            Log.i(USER_CONTACTS_MANAGER_LOG, "Time diff: " + time_diff);

            if (time_diff < MAX_TIME_DIFF) {
                Log.i(USER_CONTACTS_MANAGER_LOG, "There's a contact saved with less than 2 minutes with token " + contact.getToken());
                contact.setLastContactTimestamp(now);
                repository.updateContact(contact);
                return;
            }
        }

        // It's a new contact
        Log.i(USER_CONTACTS_MANAGER_LOG, "There's no contact saved with less than 2 minutes, creating a new contact in storage...");
        float batteryLevel = getBatteryLevel(context);
        Log.i(USER_CONTACTS_MANAGER_LOG, "Battery level:" + batteryLevel);

        Contact contact = new Contact(
                beacon.getId1().toString(),
                now,
                beacon.getDistance(),
                beacon.getRssi(),
                batteryLevel);

        repository.addNewContact(contact);
    }

    public String makeContactMessageAsJson(Contact contact, String id)
            throws JSONException,
            UserInformationNotFoundException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            SignatureException,
            InvalidKeyException {
        JSONObject message = new JSONObject();

        message.put("id", contact.getId()); // To delete from memory after completed delivery
        message.put("contact", ContactAdapter.toJSONObject(contact));
        message.put("user", id);

        String msgStr = message.toString();
        ECSignature sig = cryptoManager.sign(msgStr);

        message.put("signature", sig);

        return message.toString();
    }

    public String getBannerMessageIfAtRisk() {
        RiskNotification notification = repository.getNotification(1);

        return notification == null ? null : notification.getMessage();
    }

    private float getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return ((float)level / (float)scale) * 100.0f;
    }
}
