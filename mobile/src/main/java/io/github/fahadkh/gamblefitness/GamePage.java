package io.github.fahadkh.gamblefitness;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

import java.lang.reflect.Field;

public class GamePage extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public final static String USER_SELECT = "com.example.GambleFitness.USER_SELECT";
    String user_selection;
    private GambleAPIManager apiManager;
    private boolean dataSent = false;
    private boolean wifiCheck = false;
    Intent intent;
    static final String COINS = "coins";
    int coinss = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = getIntent();
        setContentView(R.layout.activity_game_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SessionManager session = new SessionManager(getApplicationContext());
        session.setGoalSet(false);

        if(savedInstanceState != null){
            coinss = savedInstanceState.getInt(COINS);
        }
        else{
            coinss = session.getActiCoins();
        }
        TextView coins = (TextView)findViewById(R.id.acti_coins);
        coins.setText(coinss + " Acticoins");

        Spinner gamespin = (Spinner) findViewById(R.id.activity_guesser_spinner);
        String[] items = new String[120];
        int j = 0;
        for (int i = 0; i < 120; i++) {
            int temp = j+10;
            String item = Integer.toString(j) + " - " + Integer.toString(temp);
            j+=10;
            items[i] = item;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items);
        gamespin.setAdapter(adapter);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(gamespin);

            // Set popupWindow height to 200px
            popupWindow.setHeight(200);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        gamespin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                user_selection = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });

        GoogleApiClient mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .build();

        apiManager = new GambleAPIManager(mApiClient, this, session.getUserDetails().get("name"));
        apiManager.initSubscriptions();
        apiManager.pushGoogleFitDataInBackground();
        dataSent = true;
    }
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(COINS, coinss);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
    public void gotoReveal(View view) {
        if (checkWifiOnAndConnected() && dataSent) {
            intent = new Intent(this, Reveal.class);
            intent.putExtra(USER_SELECT, user_selection);
            startActivity(intent);
        }
    }
    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        apiManager.onConnected(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiManager.onConnectionSuspended(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        apiManager.onConnectionFailed(connectionResult);
    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if(wifiInfo == null || wifiInfo.getNetworkId() == -1){
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        }
        else {
            return false; // Wi-Fi adapter is OFF
        }
    }

}
