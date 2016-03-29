package tdt4240.lyvlyvtjuesyv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by RayTM on 28.03.2016.
 */
public class GameHub extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub_connecting);
        connect();
    }

    private void connect() {
    }

    /** Called once server is responding **/
    public void connected() {
        setContentView(R.layout.activity_hub);
    }
}
