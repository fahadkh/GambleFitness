package io.github.fahadkh.gamblefitness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Calendar;

public class NightMode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_night_mode);
        Intent intent;
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
        if (currentHour > 4 && currentHour < 22){
            intent = new Intent(this, DailyGoal.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Intent intent;
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
        if (currentHour > 4 && currentHour < 22){
            intent = new Intent(this, DailyGoal.class);
            startActivity(intent);
        }
    }

    public void onBackPressed()
    {
        moveTaskToBack(true);
    }
}
