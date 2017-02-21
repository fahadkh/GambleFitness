package io.github.fahadkh.gamblefitness;

import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;


/**
 * Created by nikhil on 2/21/17.
 */

public class GambleAPIManager implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GambleAPIManager";
    private static final String BACKEND = "Backend";
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private static final String PERMISSIONS_RATIONALE = "Body Sensor permission is needed to run this app";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 17;

    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;
    private AppCompatActivity context;
    private ResultCallback<ListSubscriptionsResult> mListSubscriptionsResultCallback;

    GambleAPIManager (GoogleApiClient client, AppCompatActivity c){
        mApiClient = client;
        context = c;
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.BODY_SENSORS);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(context,
                        android.Manifest.permission.BODY_SENSORS);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    context.findViewById(R.id.tomorrowgoal), // Not sure about this...
                    PERMISSIONS_RATIONALE,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Settings", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(context,
                                    new String[]{android.Manifest.permission.BODY_SENSORS},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(context,
                    new String[]{android.Manifest.permission.BODY_SENSORS},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void obtainTodayData() {

        if (!checkPermissions()) {
            requestPermissions();
            return;
        }

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.HOUR_OF_DAY, -24);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.e("History", "Range Start: " + dateFormat.format(startTime));
        Log.e("History", "Range End: " + dateFormat.format(endTime));


        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_BASAL_METABOLIC_RATE, DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(5, TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mApiClient, dataReadRequest).await(1, TimeUnit.MINUTES);


        Log.d(TAG, dataReadResult.toString());

        //Used for aggregated data
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e("History", "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    processDataSet(dataSet);
                }
            }
        }
        //Used for non-aggregated data
        else if (dataReadResult.getDataSets().size() > 0) {
            Log.e("History", "Number of returned DataSets: " + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                processDataSet(dataSet);

            }
        }
    }

    private static void processDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();

        Log.d(TAG, dataSet.getDataPoints().toString());

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                uploadTableEntry(dp, field);
            }
        }
    }

    private static void uploadTableEntry(DataPoint dp, Field f) {
        try {

            DateFormat timeFormat = getTimeInstance();
            DateFormat dateFormat = getDateInstance();
            long stTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);

            String uid = "userId";
            String start_time = timeFormat.format(stTime) + dateFormat.format(stTime);
            String end_time = timeFormat.format(endTime) + dateFormat.format(endTime);;
            String field= f.getName();
            String value = dp.getValue(f).toString();

            String data = URLEncoder.encode("uid", "UTF-8")
                    + "=" + URLEncoder.encode(uid, "UTF-8");

            data += "&" + URLEncoder.encode("start_time", "UTF-8") + "="
                    + URLEncoder.encode(start_time, "UTF-8");

            data += "&" + URLEncoder.encode("end_time", "UTF-8")
                    + "=" + URLEncoder.encode(end_time, "UTF-8");

            data += "&" + URLEncoder.encode("field", "UTF-8")
                    + "=" + URLEncoder.encode(field, "UTF-8");

            data += "&" + URLEncoder.encode("value", "UTF-8")
                    + "=" + URLEncoder.encode(value, "UTF-8");

            Log.d(BACKEND, data);

            /*int dataLength = data.length();
            String request = "http://murphy.wot.eecs.northwestern.edu/~nsc969/SQLGateway.py";
            URL url = new URL(request);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(dataLength));
            conn.setUseCaches(false);
            try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
                wr.write(data);
            }


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            conn.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                Log.w(TAG, decodedString);
            }*/

        } /*catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } */catch (IOException e) {
            e.printStackTrace();
        } finally {
            /*try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }
    }

    private class TransferGoogleFitToBackend extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            obtainTodayData();
            return null;
        }
    }

    protected void pushGoogleFitDataInBackground() {
        new TransferGoogleFitToBackend().execute();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Gamble", "mApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        return;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if( !authInProgress ) {
            try {
                authInProgress = true;
                Log.e( "GoogleFit", "Error: " + connectionResult.toString());
                connectionResult.startResolutionForResult( context, REQUEST_OAUTH );
            } catch(IntentSender.SendIntentException e ) {
                Log.e(TAG, "Error in onConnectionFailed");
            }
        } else {
            Log.e( "GoogleFit", "authInProgress" );
        }
    }

    protected void initSubscriptions() {
        mListSubscriptionsResultCallback = new ResultCallback<ListSubscriptionsResult>() {
            @Override
            public void onResult(@NonNull ListSubscriptionsResult listSubscriptionsResult) {
                for (Subscription subscription : listSubscriptionsResult.getSubscriptions()) {
                    DataType dataType = subscription.getDataType();
                    Log.e( "RecordingAPI", dataType.getName() );
                    for (Field field : dataType.getFields() ) {
                        Log.e( "RecordingAPI", field.toString() );
                    }
                }
            }
        };


        Fitness.RecordingApi.listSubscriptions(mApiClient)
                .setResultCallback(mListSubscriptionsResultCallback);

        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for step count detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed to step count!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing to step count.");
                        }
                    }
                });

        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_HEART_RATE_BPM)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for heart rate detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed to heart rate!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing to heart rate.");
                        }
                    }
                });

        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_BASAL_METABOLIC_RATE)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for basal metabolic rate detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed to basal metabolic rate!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing to basal metabolic rate.");
                        }
                    }
                });

        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for calories expended detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed to calories expended!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing to calories expended.");
                        }
                    }
                });

    }

}
