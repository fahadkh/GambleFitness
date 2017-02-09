package io.github.fahadkh.gamblefitness;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

/**
 * Background sensor polling
 *
 */

public class FitnessPollService extends Service implements SensorEventListener{
    private final String TAG = "Poll Service";

    private SensorManager sensorManager;
    private Sensor mHeart;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service started");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mHeart = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorManager.registerListener(this, mHeart,
                SensorManager.SENSOR_DELAY_NORMAL);

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
        // stop the service
        stopSelf();
    }

    private class SensorEventLoggerTask extends
            AsyncTask<SensorEvent, Void, Void> {
        @Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];
            // log the value

            Log.v(TAG, event.toString());

            return null;
        }
    }

}
