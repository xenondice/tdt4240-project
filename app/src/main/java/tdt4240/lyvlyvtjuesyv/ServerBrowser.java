package tdt4240.lyvlyvtjuesyv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Created by RayTM on 28.03.2016.
 */
public class ServerBrowser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
    }

    /** Called once user presses connect **/
    public void connect(View view) {
        EditText textbox = (EditText) findViewById(R.id.editText);
        Intent hub = new Intent(this, GameHub.class);
        hub.putExtra(Constants.IS_HOST_ADD, false);
        hub.putExtra(Constants.SERVER_ADDRESS_ADD, textbox.getText());
        hub.putExtra(Constants.SERVER_PORT_ADD, Constants.DEFAULT_PORT);
        startActivity(hub);
    }
}
