package tdt4240.lyvlyvtjuesyv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    /** Called when Start Game button is pressed **/
    public void startGame(View view) {
        Intent hub = new Intent(this, GameHub.class);
        hub.putExtra(Constants.IS_HOST_ADD, true);
        hub.putExtra(Constants.SERVER_ADDRESS_ADD, Constants.HOME_ADDRESS);
        hub.putExtra(Constants.SERVER_PORT_ADD, Constants.DEFAULT_PORT);
        startActivity(hub);
    }

    /** Called when Browse Games button is pressed **/
    public void openBrowser(View view) {
        Intent browser = new Intent(this, ServerBrowser.class);
        startActivity(browser);
    }
}
