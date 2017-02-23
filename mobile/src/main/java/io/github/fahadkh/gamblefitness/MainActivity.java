package io.github.fahadkh.gamblefitness;

import android.app.Activity;
import android.content.Intent;
import java.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;

public class MainActivity extends Activity {
    private Spinner mspin;
    private int weekly_goal;
    private int daily_goal;
    public final static String DAILY_GOAL_NUM = "com.example.GambleFitness.DAILY_GOAL_NUM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SessionManager session = new SessionManager(getApplicationContext());

        mspin = (Spinner) findViewById(R.id.goal_spinner);
        Integer[] items = new Integer[120];
        for (int i = 0; i < 120; i++) {
            items[i] = 240 + (5*i);
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, items);
        mspin.setAdapter(adapter);

        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(mspin);

            // Set popupWindow height to 200px
            popupWindow.setHeight(200);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        mspin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                weekly_goal = (int) parent.getItemAtPosition(position);
                daily_goal = (int) parent.getItemAtPosition(position) / 7;
                session.setWeeklyGoal(weekly_goal);
                session.setDailyGoal(daily_goal);
                String goal_string = Integer.toString(daily_goal);
                TextView myAwesomeTextView = (TextView)findViewById(R.id.daily_goal);
                myAwesomeTextView.setText(goal_string + " min");

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }

        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void gotoDaily(View view) {
        Intent intent;
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour

        if (currentHour > 21 || currentHour < 5){
            intent = new Intent(this, NightMode.class);
        }
        else {intent = new Intent(this, DailyGoal.class);}
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }
}

