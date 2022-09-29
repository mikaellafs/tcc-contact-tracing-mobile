package pg.contact_tracing.services.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import pg.contact_tracing.ui.fragments.PasswordDialog;

public class MqttClientService {
    private static String MQTT_CLIENT_SERVICE_LOG = "MQTT_CLIENT_SERVICE";
    private static int QOS = 1;

    private MqttAndroidClient client;
    private String serverURI;

    public MqttClientService(Context context) throws ClassCastException {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(
                context, serverURI, clientId);
    }

    public void connect(IMqttActionListener listener) throws MqttException {
        IMqttToken token = client.connect();
        token.setActionCallback(listener);
    }

    public void publish(String topic, byte[] messageBytes) throws MqttException {
        MqttMessage message = new MqttMessage(messageBytes);
        client.publish(topic, message);
    }

    public void subscribe(String topic, IMqttActionListener listener) throws MqttException {
        IMqttToken subToken = client.subscribe(topic, QOS);
        subToken.setActionCallback(listener);
    }
}
