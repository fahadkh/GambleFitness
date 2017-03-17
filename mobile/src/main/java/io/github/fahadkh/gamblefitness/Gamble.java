package io.github.fahadkh.gamblefitness;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;

public class Gamble extends AppCompatActivity {

    private Spinner newgoal_spin;
    private static final String COINS = "coins";
    private static final String TM_GOAL = "tmrw_goal";
    private static final String USED_GOAL = "used_goal";
    int coinss = 0;
    double daily_goal_set = 0;
    String tm_goal = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamble);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        final SessionManager session = new SessionManager(getApplicationContext());
        TextView tomorrow_goal = (TextView) findViewById(R.id.tomorrowgoal);
        TextView coins = (TextView)findViewById(R.id.acti_coins);

        final double daily_goal_from_ytday = (double) session.getDailyGoal();
        final double daily_goal_from_start = (double) (session.getWeeklyGoal() / 7);

        int actualMVPA = session.getMVPA();

        if(savedInstanceState != null){
            coinss = savedInstanceState.getInt(COINS);
            tm_goal = savedInstanceState.getString(TM_GOAL);
            daily_goal_set = savedInstanceState.getDouble(USED_GOAL);
        }
        else {
            if (actualMVPA < daily_goal_from_ytday) {
                tm_goal = Integer.toString((int) daily_goal_from_start) + " min";
                daily_goal_set = daily_goal_from_start;
            } else {
                tm_goal = Integer.toString((int) daily_goal_from_ytday) + " min";
                daily_goal_set = daily_goal_from_ytday;
            }

            coinss = session.getActiCoins();
        }

        tomorrow_goal.setText(tm_goal);
        coins.setText(coinss + " Acticoins");

        newgoal_spin = (Spinner) findViewById(R.id.gamble_spinner);
        Integer[] items = new Integer[120];
        for (int i = 0; i < 120; i++) {
            items[i] =(5*i);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, items);
        newgoal_spin.setAdapter(adapter);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(newgoal_spin);

            // Set popupWindow height to 200px
            popupWindow.setHeight(200);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        newgoal_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                double gamblegoal = (double)(int)parent.getItemAtPosition(position);
                int wager;

                if (gamblegoal == daily_goal_set){
                    wager = 10;
                }
                else if (gamblegoal > daily_goal_from_ytday){

                     wager = (int)(10+ (((gamblegoal - daily_goal_from_start)/daily_goal_from_start) * 10)
                            + (((gamblegoal - daily_goal_from_ytday)/daily_goal_from_ytday) * 5));
                }
                else if (gamblegoal > daily_goal_from_start){
                     wager = (int)((((gamblegoal - daily_goal_from_start)/daily_goal_from_start) * 10) + 10);
                }
                else{
                     wager = (int)(10 -(((daily_goal_from_ytday-gamblegoal)/daily_goal_from_ytday) * 10));
                }

                session.setDailyGoal((int)gamblegoal);
                session.setWager(wager);

                TextView wagertext = (TextView) findViewById(R.id.wager);
                wagertext.setText(wager + " Acticoins");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(COINS, coinss);
        savedInstanceState.putString(TM_GOAL,tm_goal);
        savedInstanceState.putDouble(USED_GOAL, daily_goal_set);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void gotoNightHolding(View view) {
        Intent intent;
        intent = new Intent(this, NightMode.class);
        final SessionManager session = new SessionManager(getApplicationContext());
        session.setGoalSet(true);
        startActivity(intent);
    }

    public void gotoNightHoldingNoWager(View view){
        Intent intent;
        intent = new Intent(this, NightMode.class);
        final SessionManager session = new SessionManager(getApplicationContext());
        session.setGoalSet(true);
        session.setWager(10);
        startActivity(intent);
    }

    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

    }
