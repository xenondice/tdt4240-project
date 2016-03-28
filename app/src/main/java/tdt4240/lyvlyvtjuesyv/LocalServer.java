package tdt4240.lyvlyvtjuesyv;

import android.app.IntentService;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by RayTM on 28.03.2016.
 */
public class LocalServer extends IntentService {
    public static final int DEFAULT_PORT = 9000;

    public LocalServer() {
        super("Lyv Lyv, Tjuesyv - Server");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        String dataString = workIntent.getDataString();
    }
}
