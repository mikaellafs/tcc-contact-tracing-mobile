package pg.contact_tracing.services.core;

import static pg.contact_tracing.datasource.sqlite.SQLiteContactsStorageStrings.ORDER_BY_FIRST_CONTACT_ASC;

import android.app.Notification;
import android.app.NotificationManager;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import pg.contact_tracing.R;
import pg.contact_tracing.di.DI;
import pg.contact_tracing.exceptions.InstanceNotRegisteredDIException;
import pg.contact_tracing.exceptions.UserInformationNotFoundException;
import pg.contact_tracing.models.Contact;
import pg.contact_tracing.models.RiskNotification;
import pg.contact_tracing.repositories.UserContactsRepository;
import pg.contact_tracing.repositories.UserInformationsRepository;
import pg.contact_tracing.services.mqtt.MqttClientService;
import pg.contact_tracing.utils.CryptoManager;
import pg.contact_tracing.utils.NotificationBroadcastCenter;
import pg.contact_tracing.utils.NotificationCreator;
import pg.contact_tracing.utils.adapters.RiskNotificationAdapter;
import pg.contact_tracing.utils.UserContactsManager;

public class MqttContactTracingService extends Service implements MqttCallback {
    private static final String MQTT_CONTACT_TRACING_SERVICE_LOG = "MQTT_CONTACT_TRACING_SERVICE";

    private static final String CONTACTS_PRODUCER_SERVICE_LOG = "CONTACTS_PRODUCER_SERVICE";
   private static final int SEND_CONTACTS_INTERVAL = 30 * 60 * 1000; // 30 minutos
    // private static final int SEND_CONTACTS_INTERVAL = 60 * 1000;
    private static final String SEND_CONTACTS_TOPIC = "contact";
    private static final int SEND_CONTACTS_LIMIT = 30;
    private static final int id = 1;

    private static final String NOTIFICATION_CONSUMER_SERVICE_LOG = "NOTIFICATION_CONSUMER_SERVICE";
    private static final String RECEIVE_NOTIFICATION_BASE_TOPIC = "notification";

    MqttClientService client;
    UserContactsRepository repository;
    UserContactsManager helper = null;
    String userId = null;

    public static boolean isRunning;

    private IMqttActionListener onConnectionListener;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = false;

        onConnectionListener = new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(MQTT_CONTACT_TRACING_SERVICE_LOG, "Success on connecting to broker");

                listenToNotifications();
                startSendContactsTask();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.e(MQTT_CONTACT_TRACING_SERVICE_LOG, "Failed to connect to broker: " + exception.getMessage());
                isRunning = false;

                stopSelf();
                NotificationBroadcastCenter.sendNotification(
                        MqttContactTracingService.this,
                        NotificationBroadcastCenter.Event.MQTT_SERVICE_FAILED,
                        "Could not connect to mqtt broker"
                );
            }
        };

        try {
            client = DI.resolve(MqttClientService.class);
            repository = DI.resolve(UserContactsRepository.class);

            UserInformationsRepository infosRepo = DI.resolve(UserInformationsRepository.class);
            userId = infosRepo.getID();

            CryptoManager cryptoManager = DI.resolve(CryptoManager.class);
            helper = new UserContactsManager(repository, cryptoManager);

        } catch (InstanceNotRegisteredDIException e) {
            Log.e(MQTT_CONTACT_TRACING_SERVICE_LOG, "Failed to resolve dependency: " + e.getMessage());
        } catch (UserInformationNotFoundException e) {
            Log.e(MQTT_CONTACT_TRACING_SERVICE_LOG, e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = NotificationCreator.foregroundServiceNotification;

        if (helper == null || userId == null || notification == null) {
            Log.e(CONTACTS_PRODUCER_SERVICE_LOG, "Failed to start service");
            isRunning = false;

            NotificationBroadcastCenter.sendNotification(
                    MqttContactTracingService.this,
                    NotificationBroadcastCenter.Event.MQTT_SERVICE_FAILED,
                    "Could not connect to mqtt broker"
            );

            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(id, notification);

        client.setCallBack(this);

        try {
            client.connect(onConnectionListener);
        } catch(MqttException e) {
            Log.e(CONTACTS_PRODUCER_SERVICE_LOG, "Failed to start service: " + e.getMessage());

            isRunning = false;

            stopSelf();
            NotificationBroadcastCenter.sendNotification(
                    MqttContactTracingService.this,
                    NotificationBroadcastCenter.Event.MQTT_SERVICE_FAILED,
                    "Could not connect to mqtt broker"
            );
        }

        MqttContactTracingService.isRunning = true;
        Log.i(CONTACTS_PRODUCER_SERVICE_LOG, "Sending contacts service has started!");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(MQTT_CONTACT_TRACING_SERVICE_LOG, "Mqtt service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void listenToNotifications() {
        String topic = RECEIVE_NOTIFICATION_BASE_TOPIC + "/" + userId;

        try {
            client.subscribe(topic, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(NOTIFICATION_CONSUMER_SERVICE_LOG, "Success on subscribing to topic");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(NOTIFICATION_CONSUMER_SERVICE_LOG, "Failed to subscribe to topic: " + exception.getMessage());
                    stopSelf();

                    NotificationBroadcastCenter.sendNotification(
                            MqttContactTracingService.this,
                            NotificationBroadcastCenter.Event.MQTT_SERVICE_FAILED,
                            "Could not listen to notifications"
                    );
                }
            });
        } catch (MqttException e) {
            Log.e(MQTT_CONTACT_TRACING_SERVICE_LOG, "Could not listen to notifications: ");
            stopSelf();

            NotificationBroadcastCenter.sendNotification(
                    MqttContactTracingService.this,
                    NotificationBroadcastCenter.Event.MQTT_SERVICE_FAILED,
                    "Could not listen to notifications"
            );
        }
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
        Log.i(CONTACTS_PRODUCER_SERVICE_LOG, "Start sending contacts saved");
        try {
            ArrayList<Contact> lastContacts = repository.getContact(
                    null,
                    ORDER_BY_FIRST_CONTACT_ASC,
                    null,
                    SEND_CONTACTS_LIMIT);

            for (Contact contact : lastContacts) {
                String message = helper.makeContactMessageAsJson(contact, userId);
                Log.i(CONTACTS_PRODUCER_SERVICE_LOG, "Publishing message (contact) to mqtt broker: " + message);
                client.publish(SEND_CONTACTS_TOPIC, message.getBytes());
            }
        } catch (UserInformationNotFoundException | JSONException
                | NoSuchAlgorithmException | InvalidKeySpecException
                | SignatureException | InvalidKeyException e) {

            Log.e(CONTACTS_PRODUCER_SERVICE_LOG, "Failed to make message to send contact.");
        } catch (MqttException e) {
            Log.e(CONTACTS_PRODUCER_SERVICE_LOG, "Failed to send message to broker.");

        } finally {
            Log.i(MQTT_CONTACT_TRACING_SERVICE_LOG, "Stopping sending contacts saved for now");
            startSendContactsTask();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(MQTT_CONTACT_TRACING_SERVICE_LOG, "Lost connection with broker");
        stopSelf();

        NotificationBroadcastCenter.sendNotification(
                MqttContactTracingService.this,
                NotificationBroadcastCenter.Event.MQTT_SERVICE_FAILED,
                "Lost connection with MQTT broker"
        );
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String messageStr = new String(message.getPayload());
        Log.i(NOTIFICATION_CONSUMER_SERVICE_LOG, "Message received from topic '"+ topic + "': " + messageStr);

        try {
            JSONObject notification = new JSONObject(messageStr);
            System.out.println("Oi");

            boolean isUserAtRisk = notification.getBoolean("risk");
            String notificationMessage = notification.getString("message");

            if (isUserAtRisk) {
                RiskNotification risk = RiskNotificationAdapter.fromJSONObject(notification);
                repository.addNewNotification(risk);
                showRiskNotification(risk);
            } else {
                repository.deleteNotification(1);
            }

            NotificationBroadcastCenter.Event NOTIFICATION_TYPE = isUserAtRisk ? NotificationBroadcastCenter.Event.RISK_NOTIFICATION : NotificationBroadcastCenter.Event.NOT_RISK_NOTIFICATION;
            NotificationBroadcastCenter.sendNotification(this, NOTIFICATION_TYPE, notificationMessage);
        } catch(JSONException | ParseException e) {
            Log.e(NOTIFICATION_CONSUMER_SERVICE_LOG, "Failed to parse message received: " + e.getMessage());
            return;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(CONTACTS_PRODUCER_SERVICE_LOG, "Message sent successfully");

        try {
            MqttMessage mqttMessage = token.getMessage();
            JSONObject message = new JSONObject(mqttMessage.toString());

            int contactId = message.getInt("id");
            int deleted = repository.deleteContact(contactId);

            Log.i(CONTACTS_PRODUCER_SERVICE_LOG, "Deleted contacts with id " + contactId + ": " + deleted);
        } catch (MqttException | JSONException e) {
            Log.e(CONTACTS_PRODUCER_SERVICE_LOG, "Failed to get message delivered");
        }
    }

    private void showRiskNotification(RiskNotification risk){
        String userNotificationChannelId = getString(R.string.user_notification_channel_id);
        String userNotificationChannelName = getString(R.string.user_notification_channel_name);
        String userNotificationTitle = getString(R.string.user_notification_title);
        String userNotificationSubtitle = getString(R.string.user_notification_subtitle);

        NotificationCreator creator = new NotificationCreator();
        Notification notification = creator.createNotification(
                getApplicationContext(),
                userNotificationChannelId,
                userNotificationChannelName,
                userNotificationTitle,
                userNotificationSubtitle,
                R.drawable.ic_warning_svgrepo_com
        );

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NotificationCreator.pushNotificationId, notification);
    }
}
