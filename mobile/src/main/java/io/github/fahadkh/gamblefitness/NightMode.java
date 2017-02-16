package io.github.fahadkh.gamblefitness;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NightMode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_night_mode);
    }

    public void onBackPressed()
    {
        moveTaskToBack(true);
    }
}
