package pg.contact_tracing.services.core;

import static pg.contact_tracing.datasource.sqlite.SQLiteContactsStorageStrings.ORDER_BY_FIRST_CONTACT_ASC;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import pg.contact_tracing.di.DI;
import pg.contact_tracing.exceptions.InstanceNotRegisteredDIException;
import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.Contact;
import pg.contact_tracing.repositories.UserContactsRepository;
import pg.contact_tracing.repositories.UserInformationsRepository;
import pg.contact_tracing.services.mqtt.MqttClientService;
import pg.contact_tracing.utils.CryptoManager;
import pg.contact_tracing.utils.NotificationBroadcastCenter;
import pg.contact_tracing.utils.UserContactsManager;

public class MqttContactTracingService extends Service implements MqttCallback {
    private static final String MQTT_CONTACT_TRACING_SERVICE_LOG = "MQTT_CONTACT_TRACING_SERVICE";

    private static final String CONTACTS_PRODUCER_SERVICE_LOG = "CONTACTS_PRODUCER_SERVICE";
    private static final int SEND_CONTACTS_INTERVAL = 10 * 60 * 1000; // 10 minutos
    private static final String SEND_CONTACTS_TOPIC = "contact";
    private static final int SEND_CONTACTS_LIMIT = 30;

    private static final String NOTIFICATION_CONSUMER_SERVICE_LOG = "NOTIFICATION_CONSUMER_SERVICE";
    private static final String RECEIVE_NOTIFICATION_BASE_TOPIC = "notification";

    MqttClientService client;
    UserContactsRepository repository;
    UserContactsManager helper = null;
    String userId = null;

    public static boolean isRunning;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            client = DI.resolve(MqttClientService.class);
            repository = DI.resolve(UserContactsRepository.class);

            UserInformationsRepository infosRepo = DI.resolve(UserInformationsRepository.class);
            userId = infosRepo.getID();

            CryptoManager cryptoManager = DI.resolve(CryptoManager.class);
            helper = new UserContactsManager(repository, cryptoManager);

        } catch (InstanceNotRegisteredDIException e) {
            Log.e(MQTT_CONTACT_TRACING_SERVICE_LOG, "Failed to resolve dependency: " + e.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (helper == null || userId == null) {
            Log.e(CONTACTS_PRODUCER_SERVICE_LOG, "Failed to start service");
            isRunning = false;

            stopSelf();
            return START_NOT_STICKY;
        }

        client.setCallBack(this);
        Log.i(CONTACTS_PRODUCER_SERVICE_LOG, "Sending contacts service has started!");
        MqttContactTracingService.isRunning = true;

        try {
            listenToNotifications();
        } catch (MqttException e) {
            isRunning = false;
            stopSelf();
            return START_NOT_STICKY;
        }

        startSendContactsTask();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void listenToNotifications() throws MqttException {
        String topic = RECEIVE_NOTIFICATION_BASE_TOPIC + "/" + userId;
        client.subscribe(topic, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(NOTIFICATION_CONSUMER_SERVICE_LOG, "Success on listening to notifications");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(NOTIFICATION_CONSUMER_SERVICE_LOG, "Failed to listen to notifications: " + exception.getMessage());
            }
        });
    }

    private void startSendContactsTask() {
        Log.i(CONTACTS_PRODUCER_SERVICE_LOG, "Scheduling sending contacts in " + SEND_CONTACTS_INTERVAL + " seconds");
        new Timer().schedule(new TimerTask(){
            @Override
            public void run(){
                AsyncTask.execute(() -> sendContacts());
            }
        }, SEND_CONTACTS_INTERVAL);
    }

    private void sendContacts() {
        try {
            ArrayList<Contact> lastContacts = repository.getContact(
                    null,
                    ORDER_BY_FIRST_CONTACT_ASC,
                    null,
                    SEND_CONTACTS_LIMIT);

            for (Contact contact : lastContacts) {
                String message = helper.makeContactMessageAsJson(contact, userId);
                client.publish(SEND_CONTACTS_TOPIC, message.getBytes());
            }
        } catch (UserInformationNotFoundException | JSONException
                | NoSuchAlgorithmException | InvalidKeySpecException
                | SignatureException | InvalidKeyException e) {

            Log.e(CONTACTS_PRODUCER_SERVICE_LOG, "Failed to make message to send contact.");
        } catch (MqttException e) {
            Log.e(CONTACTS_PRODUCER_SERVICE_LOG, "Failed to send message to broker.");

        } finally {
            startSendContactsTask();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(MQTT_CONTACT_TRACING_SERVICE_LOG, "Lost connection with broker");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(NOTIFICATION_CONSUMER_SERVICE_LOG, "Message received from topic '"+ topic + "': " + new String(message.getPayload()));

        JSONObject notification = new JSONObject(message.toString());
        boolean isUserAtRisk = notification.getBoolean("risk");
        String notificationMessage = notification.getString("message");

        NotificationBroadcastCenter.Event NOTIFICATION_TYPE = isUserAtRisk ? NotificationBroadcastCenter.Event.RISK_NOTIFICATION : NotificationBroadcastCenter.Event.NOT_RISK_NOTIFICATION;
        NotificationBroadcastCenter.sendNotification(this, NOTIFICATION_TYPE, notificationMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(CONTACTS_PRODUCER_SERVICE_LOG, "Message sent succefully");
    }
}
