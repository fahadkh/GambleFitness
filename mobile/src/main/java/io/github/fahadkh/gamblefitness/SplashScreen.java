package io.github.fahadkh.gamblefitness;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;


public class SplashScreen extends Activity {

    private static final String SPLASH = "splash";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        new Handler().postDelayed(new Runnable() {

            // Using handler with postDelayed called runnable run method


            @Override
            public void run() {
                SessionManager session = new SessionManager(getApplicationContext());

                Intent i = null;

                if (checkWifiOnAndConnected()) {

                    int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
                    if (!session.isLoggedIn()) {
                        Log.w(SPLASH, "gotoLogin");
                        i = new Intent(getApplicationContext(), LoginActivity.class);
                    }
                    else if(!session.getUserType()) {
                        if (currentHour > 21 || currentHour < 5) {
                            Log.w(SPLASH, "ControlReveal");
                            i = new Intent(getApplicationContext(), RevealControl.class);
                        } else {
                            Log.w(SPLASH, "ControlDay");
                           i =  new Intent(getApplicationContext(), ControlUser.class);
                        }
                    }
                    else if (currentHour > 21 && currentHour < 25) {
                        boolean bool = session.getGoalSet();
                        if (!bool) {
                            Log.w(SPLASH, "GamePlay");
                            i = new Intent(getApplicationContext(), GamePage.class);
                        } else {
                            Log.w(SPLASH, "GameNight");
                            i = new Intent(getApplicationContext(), NightMode.class);
                        }
                    } else if (currentHour >= 0 && currentHour < 6) {
                        Log.w(SPLASH, "GameNight");
                        i = new Intent(getApplicationContext(), NightMode.class);
                    } else {
                        Log.w(SPLASH, "GameDay");
                        i = new Intent(getApplicationContext(), DailyGoal.class);
                    }


                }

            else{
                    i = new Intent(getApplicationContext(), wifiMissing.class);
                }

                startActivity(i);

                // close this activity
                finish();
            }
        }, 5*1000); // wait for 5 seconds

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
