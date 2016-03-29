package tdt4240.lyvlyvtjuesyv;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by RayTM on 28.03.2016.
 */
public class GameHub extends AppCompatActivity {

    private Intent intent;
    private String address;
    private int port;
    private boolean is_host;
    private Socket client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub_connecting);
        connect();
    }

    private void connect() {
        intent = getIntent();
        port = intent.getIntExtra(Menu.SERVER_PORT_ID, LocalServer.DEFAULT_PORT);
        address = intent.getStringExtra(Menu.SERVER_ADDRESS_ID);
        is_host = intent.getBooleanExtra(Menu.IS_HOST_ID, false);

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    client = new Socket(address, port);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result)
                    connected();
                else
                    disconnected();
            }
        }.execute();
    }

    private void disconnected() {
        setContentView(R.layout.activity_hub_failed);
    }

    private void connected() {
        System.out.println(client.isConnected());
        if (is_host)
            setContentView(R.layout.activity_hub_host);
        else
            setContentView(R.layout.activity_hub);
    }
}
