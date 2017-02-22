package io.github.fahadkh.gamblefitness;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends WearableActivity {
    private final String TAG = "Main Activity";

    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;

    private static final long CONNECTION_TIME_OUT_MS = 100;
    private GoogleApiClient client;
    private String nodeId;
    private static final String
            DATA_CAPABILITY_NAME = "data_replicate";
    private MyResultReceiver resultReceiver;
    private Intent intent;
    private TextView status;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);

        client = getGoogleApiClient(this);
        Log.v(TAG, "client acquired: " + client.toString());

        setupDataTransfer();
        //retrieveDeviceNode();

        Log.v(TAG, "setup complete with node id: " + nodeId);

        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        status = (TextView) findViewById(R.id.text);
        btn = (Button) findViewById(R.id.button2);

    }

    public void StartLogging(View v) {
        //Log.v()
        //AlarmManager scheduler = (AlarmManager) getSystemService(this.ALARM_SERVICE);
        status.setText(R.string.active);
        btn.setEnabled(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        intent = new Intent(this, FitnessPollService.class );
        resultReceiver = new MyResultReceiver(null);
        intent.putExtra("receiver", resultReceiver);

        startService(intent);
        //PendingIntent scheduledIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //scheduler.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, scheduledIntent);
    }

    public void StopLogging(View v) {
        //AlarmManager scheduler = (AlarmManager) getSystemService(this.ALARM_SERVICE);
        if (intent == null) {

            Intent intent = new Intent(this, FitnessPollService.class);
        }
        //PendingIntent scheduledIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //scheduler.cancel(scheduledIntent);
        status.setText(R.string.inactive);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        btn.setEnabled(true);
        stopService(intent);
    }

    class MyResultReceiver extends ResultReceiver {
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.v(TAG, "Sending replicate request with TEST");
            boolean err = true;
            while (err) {
                err = requestReplicate(resultData.getString("data"));
            }
        }
    }

    public void Replicate(View v) {
        Log.v(TAG, "Sending messages");
        requestReplicate("TEST");
        //Send message for each file item
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));

        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
        }
    }

    private void setupDataTransfer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                CapabilityApi.GetCapabilityResult result =
                        Wearable.CapabilityApi.getCapability(
                                client, DATA_CAPABILITY_NAME,
                                CapabilityApi.FILTER_REACHABLE).await();

                updateDataCapability(result.getCapability());
            }
        });
    }

    private void updateDataCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        nodeId = pickBestNodeId(connectedNodes);
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    private void retrieveDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    nodeId = nodes.get(0).getId();
                }
                client.disconnect();
            }
        }).start();
    }


    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private boolean requestReplicate(final String senseData) {
        Log.v(TAG, "Request initiated to node: " + nodeId);
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, senseData, null);
                    client.disconnect();
                }
            }).start();

            return false;
        } else {
            retrieveDeviceNode();
            //requestReplicate(senseData);
            return true;
        }
    }
}
