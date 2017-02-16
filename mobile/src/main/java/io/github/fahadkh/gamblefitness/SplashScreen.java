package io.github.fahadkh.gamblefitness;

import android.app.Activity;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;

public class SplashScreen extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        new Handler().postDelayed(new Runnable() {

            // Using handler with postDelayed called runnable run method

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                SessionManager session = new SessionManager(getApplicationContext());


                Intent i;
                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour

                if(!session.isLoggedIn()){
                    i = new Intent(getApplicationContext(), LoginActivity.class);
                }
                else if (currentHour > 21 || currentHour < 9){
                    i = new Intent(getApplicationContext(), GamePage.class);
                }
                else{
                    i = new Intent(getApplicationContext(), DailyGoal.class);
                }

                startActivity(i);

                // close this activity
                finish();
            }
        }, 5*1000); // wait for 5 seconds

    }

}
