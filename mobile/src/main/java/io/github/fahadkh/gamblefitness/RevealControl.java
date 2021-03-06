package io.github.fahadkh.gamblefitness;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.vision.text.Text;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class RevealControl extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MVPA Request";
    int pStatus = 0;
    private Handler handler = new Handler();
    private GambleAPIManager apiManager;

    int gmvpa = 0;
    int goal= 0;
    String announcement = "";

    Intent intent;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_reveal_control);
        intent = getIntent();



        SessionManager session = new SessionManager(getApplicationContext());
        String uid = session.getUserDetails().get("name");
           String url = "http://murphy.wot.eecs.northwestern.edu/~djd809/mvpaGateway.py?mode=api&request=mvpa&uid=" + uid;
           url += "&post=true&goal=";
           url += Integer.toString(session.getDailyGoal());

           Log.e("QUERY:", url);


        goal = session.getDailyGoal();

        TextView goalline = (TextView) findViewById(R.id.daily_goal);
        goalline.setText("Your goal for today was " + goal + " min.");

        generateMVPA(url, session);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    public void generateMVPA(final String url, final SessionManager session) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
       StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.v(TAG, "Response: " + response);
                        JSONObject resp;
                        int mvpa = -1;
                        try {
                            resp = new JSONObject(response);
                            mvpa = resp.getInt("mvpa");
                            Log.v(TAG, Integer.toString(mvpa));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (mvpa == -1) {
                            generateMVPA(url, session);
                        }
                        else {
                            gmvpa = mvpa;
                            session.setMVPA(gmvpa);
                            announcement = "Here is how well you did today!";
                            Log.w(TAG, Integer.toString(gmvpa));
                            setMVPA();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);


        /*gmvpa = 20;
        session.setMVPA(gmvpa);
        announcement = "Here is how well you did today!";*/
    }

    public void setMVPA(){

        TextView useselect = (TextView) findViewById(R.id.user_selection);
        useselect.setText(announcement);
        final TextView tv = (TextView) findViewById(R.id.txtProgress);
        tv.setText(pStatus + "min");

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.custom_progressbar_drawable);
        final ProgressBar mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setProgress(gmvpa);   // Main Progress
        mProgress.setSecondaryProgress(goal); // Secondary Progress
        mProgress.setMax(goal); // Maximum Progress
        mProgress.setProgressDrawable(drawable);

        /*ObjectAnimator animation = ObjectAnimator.ofInt(mProgress, "progress", 0, daily_goal);
        animation.setDuration(10000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();*/

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (pStatus < gmvpa) {
                    pStatus += 1;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setProgress(pStatus);
                            tv.setText(pStatus + "min");
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(200); //thread will take approx 1.5 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

   /* public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(MVPA, gmvpa);
        savedInstanceState.putString(ANNOUNCE,announcement);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }*/
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Reveal Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void onResume(){
        super.onResume();
        SessionManager session = new SessionManager(getApplicationContext());
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY); //Current hour
        if (currentHour < 22 && currentHour >4){
            session.setDataSent(false);
            session.setInfoCollected(false);
            intent = new Intent(this, ControlUser.class);
            startActivity(intent);
        }

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
