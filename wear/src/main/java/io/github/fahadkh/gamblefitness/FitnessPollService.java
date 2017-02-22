package io.github.fahadkh.gamblefitness;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Background sensor polling
 *
 */

public class FitnessPollService extends Service implements SensorEventListener{
    private final String TAG = "Poll Service";

    private SensorManager sensorManager;
    private Sensor mHeart;
    private ResultReceiver resultReceiver;
    private ArrayList<double[]> datapoints = new ArrayList<>();
    private Timer timer = new Timer();
    private MyTimerTask timerTask = new MyTimerTask();
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service started");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeart = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, mHeart, 1000000);

        resultReceiver = intent.getParcelableExtra("receiver");

        timer.scheduleAtFixedRate(timerTask, 0, 15000);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // grab the values and timestamp -- off the main thread
        new SensorEventLoggerTask().execute(event);
        //Log.v(TAG, "Logging Sensor Change");
        // stop the service
        //this.stopSelf();
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this, mHeart);
        Log.v(TAG, "Logging stopped");
        wakeLock.release();
        timer.cancel();
        super.onDestroy();
    }

    private class SensorEventLoggerTask extends
            AsyncTask<SensorEvent, Void, Void> {

        @Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];
            // log the value

            //Long tsLong = System.currentTimeMillis()/1000;
            //String ts = tsLong.toString();

            String message = Float.toString(event.values[0]) + "," +
                    Float.toString(event.values[1]) + "," + Float.toString(event.values[2]);

            Log.v(TAG, message);

            //Send data to main activity for replication
            //Bundle bundle = new Bundle();
            //bundle.putString("data", message);
            //resultReceiver.send(100, bundle);
            double[] datapoint = {event.values[0], event.values[1], event.values[2]};
            datapoints.add(datapoint);

            return null;
        }
    }


    class MyTimerTask extends TimerTask
    {
        public MyTimerTask() {
            //Bundle bundle = new Bundle();
            //bundle.putString("start", "Timer Started....");
            //resultReceiver.send(100, bundle);
        }
        @Override
        public void run() {

            Log.v(TAG, "Processing accel data");
            //collate accel info
            double px = 0;
            double py = 0;
            double pz = 0;
            double qx = 0;
            double qy = 0;
            double qz = 0;

            for (double[] datapoint : datapoints) {
                px += datapoint[0];
                qx += datapoint[0] * datapoint[0];
                py += datapoint[1];
                qy += datapoint[1] * datapoint[1];
                pz += datapoint[2];
                qz += datapoint[2] * datapoint[2];
            }

            double P = px + py + pz;
            double Q = qx + qy + qz;
            double n = datapoints.size();

            double ee = Math.sqrt((1 / (n - 1)) * (Q - ((1 / n) * P)));

            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            String message = ts + "," + ee;

            Bundle bundle = new Bundle();
            bundle.putString("data", message);
            resultReceiver.send(100, bundle);
            datapoints = new ArrayList<>();
        }
    }

}
