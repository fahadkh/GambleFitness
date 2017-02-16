package io.github.fahadkh.gamblefitness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

public class DailyGoal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_goal);



        SessionManager session = new SessionManager(getApplicationContext());

        session.setGoalSet(false);
        TextView coins = (TextView)findViewById(R.id.acti_coins);
        int n = session.getActiCoins();
        coins.setText(n + " Acticoins");

        Intent intent = getIntent();
        int goal = session.getDailyGoal();
        String goal_string = Integer.toString(goal);
        TextView myAwesomeTextView = (TextView)findViewById(R.id.goal_today);
        myAwesomeTextView.setText(goal_string + " min");
    }

    public void gotoGame(View view) {
        Intent intent = new Intent(this, GamePage.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }
}
