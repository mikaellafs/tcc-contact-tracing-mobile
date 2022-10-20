package pg.contact_tracing.utils.adapters;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import pg.contact_tracing.models.User;

public class UserAdapter {
    public static JSONObject toJSONObject(User user) {
        JSONObject userJSON = new JSONObject();
        
        try {
            userJSON.put("userId", user.getId());
            userJSON.put("pk", user.getPublicKey());
            userJSON.put("password", user.getPassword());
        } catch (JSONException e) {
            Log.e("USER_ADAPTER","Failed to parse contact as json");
        }
        
        return userJSON;
    }
}
