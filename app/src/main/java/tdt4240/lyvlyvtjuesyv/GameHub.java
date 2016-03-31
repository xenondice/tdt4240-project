package tdt4240.lyvlyvtjuesyv;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

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
    private Thread responseThread;
    private BufferedReader input;
    private BufferedWriter output;

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
        Intent server = new Intent(this, LocalServer.class);
        startService(server);
    }

    /** Called once server is up **/
    public void connect() {
        new AsyncTask<Void, Void, Boolean>() {

            /** This is done on its own thread **/
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

            /** Once completed, UIThread calls this method **/
            @Override
            protected void onPostExecute(Boolean result) {
                if (result)
                    connected();
                else
                    disconnected();
            }
        }.execute();
    }

    public void startWaitingForMessages() {
        responseThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long lastResponse = System.currentTimeMillis();
                while (System.currentTimeMillis() - lastResponse <= Constants.RESPONSE_TIMEOUT + Constants.PING_INTERVAL) {
                    try {
                        if (input.ready()) {
                            String message = input.readLine();
                            gotMessage(message.isEmpty() ? Constants.STATUS_EMPTY_RESPONSE + "" : message);
                            lastResponse = System.currentTimeMillis();
                        }
                        Thread.sleep(Constants.RESPONSE_CHECK_INTERVAL);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(),"Server timed out!",Toast.LENGTH_LONG).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                disconnected();
                            }
                        });
                        e.printStackTrace();
                        return;
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                disconnected();
            }
        });
        responseThread.start();
    }

    /** Called once the client gets something from the server **/
    private void gotMessage(String message) {
        char header = message.charAt(0);
        String body = message.substring(1);
        switch (header) {
            case Constants.REQUEST_STATUS:
                tellServer(Constants.STATUS_ACTIVE);
                break;
            default:
        }
    }

    private void tellServer(char header) {
        tellServer(header, "");
    }

    private void tellServer(char header, String body) {
        try {
            output.write(header + body + '\n');
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            responseThread.interrupt();
            Toast.makeText(getApplicationContext(), "Lost connection!", Toast.LENGTH_LONG).show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    disconnected();
                }
            });
        }
    }

    private void disconnected() {
        setContentView(R.layout.activity_hub_failed);
    }

    private void connected() {
        try {
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        } catch (IOException e) {
            disconnected();
            e.printStackTrace();
            return;
        }
        startWaitingForMessages();
        if (is_host) {
            setContentView(R.layout.activity_hub_host);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String address = "";
                    try {
                        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                        while (interfaces.hasMoreElements()) {
                            Enumeration<InetAddress> adresses = interfaces.nextElement().getInetAddresses();
                            while (adresses.hasMoreElements())
                                address += adresses.nextElement().getHostAddress() + "\n";
                        }
                    } catch (SocketException e) {
                        address = "UNKNOWN";
                    }
                    final String finalAddress = address;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView ipField = (TextView) findViewById(R.id.textView7);
                            ipField.setText(finalAddress);
                        }
                    });
                }
            }).start();
        } else
            setContentView(R.layout.activity_hub);
    }

    @Override
    public void finish() {
        super.finish();
        instance = null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (LocalServer.getInstance() != null)
                    LocalServer.getInstance().close();
                if (responseThread != null)
                    responseThread.interrupt();
            }
        }).start();
    }
}
