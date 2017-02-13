package io.github.fahadkh.gamblefitness;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DailyGoal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_goal);

        Intent intent = getIntent();
        Integer goal = intent.getIntExtra(MainActivity.DAILY_GOAL_NUM, 60);
        String goal_string = Integer.toString(goal);
        TextView myAwesomeTextView = (TextView)findViewById(R.id.goal_today);
        myAwesomeTextView.setText(goal_string + " min");

    }

    public void gotoGame(View view) {
        Intent intent = new Intent(this, GamePage.class);
        startActivity(intent);
    }
}
