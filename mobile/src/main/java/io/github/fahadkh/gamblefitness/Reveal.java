package io.github.fahadkh.gamblefitness;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class Reveal extends AppCompatActivity {
    private static final String TAG = "MVPA Request";
    int pStatus = 0;
    private Handler handler = new Handler();

    private static final String COINS = "coins";
    private static final String MVPA = "mvpa";
    private static final String ANNOUNCE = "announcement";
    int coinss = 0;
    int gmvpa = 0;
    String announcement = "";

    //SessionManager session = new SessionManager(getApplicationContext());
    TextView tv;
    Intent intent;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reveal);
        intent = getIntent();
        String user_selection = intent.getStringExtra(GamePage.USER_SELECT);

        SessionManager session = new SessionManager(getApplicationContext());
        TextView coins = (TextView) findViewById(R.id.acti_coins);
        TextView uselect = (TextView) findViewById(R.id.user_selection);

        String uid = session.getUserDetails().get("name");
        String url = "http://murphy.wot.eecs.northwestern.edu/~djd809/mvpaGateway.py?mode=api&request=mvpa&uid=" + uid;
        url += "&post=true&goal=";
        url += Integer.toString(session.getDailyGoal());

        int wager = session.getWager();
        int daily_goal = session.getDailyGoal();

        TextView goalline = (TextView) findViewById(R.id.daily_goal);
        goalline.setText("Your goal for today was " + daily_goal + " min.");

        if (savedInstanceState != null){
            coinss = savedInstanceState.getInt(COINS);
            gmvpa = savedInstanceState.getInt(MVPA);
            announcement = savedInstanceState.getString(ANNOUNCE);
        }
        else {
            generateMVPA(url, session, wager, user_selection);
        }

        coins.setText(coinss + " Acticoins");
        uselect.setText(announcement);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.custom_progressbar_drawable);
        final ProgressBar mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mProgress.setProgress(gmvpa);   // Main Progress
        mProgress.setSecondaryProgress(daily_goal); // Secondary Progress
        mProgress.setMax(daily_goal); // Maximum Progress
        mProgress.setProgressDrawable(drawable);

      /*  ObjectAnimator animation = ObjectAnimator.ofInt(mProgress, "progress", 0, 100);
        animation.setDuration(50000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();*/

        tv = (TextView) findViewById(R.id.txtProgress);
        new Thread(new Runnable() {

            @Override
            public void run() {
                tv.setText(pStatus + "min");
                while (pStatus < gmvpa) {
                    pStatus += 1;

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            mProgress.setProgress(pStatus);
                            tv.setText(pStatus + "min");

                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(8); //thread will take approx 1.5 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void generateMVPA(final String url, final SessionManager session, final int wager, final String user_selection) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.v(TAG, "Reponse: " + response);
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
                            generateMVPA(url, session, wager, user_selection);
                        }
                        else {
                            gmvpa = mvpa;
                            session.setMVPA(gmvpa);
                            setMVPA(mvpa, session, wager, user_selection);
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
    }

    //new mvpaRetrieve().execute(uid);

    //while (!mvpaFlag){}
    public void setMVPA(final int actualMVPA, SessionManager session, int wager, String user_selection) {

        String[] nums = user_selection.split(" - ");
        int num1 = Integer.parseInt(nums[0]);
        int num2 = Integer.parseInt(nums[1]);
        boolean inRange = false;
        boolean inOneSD = false;
        int wagerloss = 0;
        int consolationPrize = 0;

        if (actualMVPA >= num1 && actualMVPA <= num2) {
            inRange = true;
        } else {
            int absdiff = Math.min(Math.abs(actualMVPA - num1), Math.abs(actualMVPA - num2));
            //set a standardclass as 10 minutes. For each standard class away, the player loses a 5% of their wager
            int standardclassesaway = absdiff / 10;
            if (standardclassesaway == 1){
                inOneSD = true;
                consolationPrize = (int)(0.5 * wager);
            }
            wagerloss = (int) (standardclassesaway * 0.05 * wager);
        }

        if (inRange) {
            announcement = "You guessed in the right range! You win " + Integer.toString(wager) + " Acticoins!";
            session.addActiCoins(wager);
            coinss = session.getActiCoins();
        } else if(inOneSD){
            announcement= "You were close! You win " + Integer.toString(consolationPrize) + " Acticoins!";
            session.addActiCoins(consolationPrize);
            coinss = session.getActiCoins();
        }
        else{
            announcement = "You guessed wrongly! You lose " + Integer.toString(wagerloss) + " Acticoins!";
            session.minusActiCoins(wagerloss);
            coinss = session.getActiCoins();
        }
    }

    public void gotoSetTmrw(View view) {
        Intent intent = new Intent(this, Gamble.class);
        startActivity(intent);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(COINS, coinss);
        savedInstanceState.putInt(MVPA, gmvpa);
        savedInstanceState.putString(ANNOUNCE,announcement);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
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
}
