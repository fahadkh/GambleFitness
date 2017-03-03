package io.github.fahadkh.gamblefitness;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

import java.util.Calendar;
import java.util.HashMap;

public class DailyGoal extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    static final String COINS = "coins";
    static final String GOAL = "goal";
    int coinss = 20;
    String goal_string = "0";
    Intent intent = getIntent();

    private GambleAPIManager apiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_daily_goal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        SessionManager session = new SessionManager(getApplicationContext());
        session.setGoalSet(false);

        if (savedInstanceState != null) {
            coinss = savedInstanceState.getInt(COINS);
            goal_string = savedInstanceState.getString(GOAL);
        }
        else{
            coinss = session.getActiCoins();
            int goal = session.getDailyGoal();
            goal_string = Integer.toString(goal);
        }

        TextView coins = (TextView)findViewById(R.id.acti_coins);
        coins.setText(coinss + " Acticoins");

        TextView myAwesomeTextView = (TextView)findViewById(R.id.goal_today);
        myAwesomeTextView.setText(goal_string + " min");

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

        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
        if (currentHour > 21){
            gotoGame();
        }
    }

    public void gotoGame(View view) {
        intent = new Intent(this, GamePage.class);
        startActivity(intent);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(COINS, coinss);
        savedInstanceState.putString(GOAL, goal_string);


        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void gotoGame() {
        intent = new Intent(this, GamePage.class);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
        if (currentHour > 21){
            gotoGame();
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
}
