package tdt4240.lyvlyvtjuesyv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Menu extends AppCompatActivity {
    public static final String IS_HOST_ID = "tdt4240.lyvlyvtjuesyv.IS_HOST";
    public static final String SERVER_ADDRESS_ID = "tdt4240.lyvlyvtjuesyv.SERVER_ADDRESS";
    public static final String SERVER_PORT_ID = "tdt4240.lyvlyvtjuesyv.SERVER_PORT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    /** Called when Start Game button is pressed **/
    public void startGame(View view) {
        Intent server = new Intent(this, LocalServer.class);
        startService(server);

        Intent hub = new Intent(this, GameHub.class);
        hub.putExtra(IS_HOST_ID, true);
        hub.putExtra(SERVER_ADDRESS_ID, "localhost");
        hub.putExtra(SERVER_PORT_ID, LocalServer.DEFAULT_PORT);
        startActivity(hub);
    }

    /** Called when Browse Games button is pressed **/
    public void openBrowser(View view) {
        Intent browser = new Intent(this, ServerBrowser.class);
        startActivity(browser);
    }
}
