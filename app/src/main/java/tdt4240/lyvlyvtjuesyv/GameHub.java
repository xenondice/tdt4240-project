package tdt4240.lyvlyvtjuesyv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by RayTM on 28.03.2016.
 */
public class GameHub extends AppCompatActivity {

    private static GameHub instance = null;
    private Intent intent;
    private String address;
    private int port;
    private boolean is_host;
    private Socket client;
    private BroadcastReceiver serverReceiver = null;

    public static GameHub getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (instance != null) return;
        instance = this;
        intent = getIntent();
        port = intent.getIntExtra(Constants.SERVER_PORT_ADD, Constants.DEFAULT_PORT);
        address = intent.getStringExtra(Constants.SERVER_ADDRESS_ADD);
        is_host = intent.getBooleanExtra(Constants.IS_HOST_ADD, false);
        setContentView(R.layout.activity_hub_connecting);

        if (is_host)
            createServer();
        else
            connect();
    }

    private void createServer() {
/*
        // Setup communication channel
        serverReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(Constants.SERVER_STATUS_ADD, 0);
                if (status == Constants.SERVER_STATUS_ACTIVE) connect();
                Toast.makeText(context, "Host received " + status, Toast.LENGTH_LONG).show();
            }
        };
        IntentFilter serverReceiverFilter = new IntentFilter(Constants.HOST_BROADCAST_ADD);
        registerReceiver(
                serverReceiver,
                serverReceiverFilter);
*/
        // Start server in its own process thread
        Intent server = new Intent(this, LocalServer.class);
        startService(server);
    }

    public void connect() {
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
        if (is_host)
            setContentView(R.layout.activity_hub_host);
        else
            setContentView(R.layout.activity_hub);
    }

    @Override
    public void finish() {
        super.finish();
        instance = null;
        LocalServer.getInstance().close();
        /*Intent intent = new Intent(Constants.SERVER_BROADCAST_ADD)
                .putExtra(Constants.SERVER_STATUS_ADD, Constants.HOST_STATUS_STOP);
        sendBroadcast(intent);*/
        //if (serverReceiver != null)
        //    LocalBroadcastManager.getInstance(this).unregisterReceiver(serverReceiver);
    }
}
