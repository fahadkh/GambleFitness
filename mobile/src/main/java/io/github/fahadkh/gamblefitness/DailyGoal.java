package io.github.fahadkh.gamblefitness;

import android.content.Intent;
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

import java.util.HashMap;

public class DailyGoal extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GambleAPIManager apiManager;

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
