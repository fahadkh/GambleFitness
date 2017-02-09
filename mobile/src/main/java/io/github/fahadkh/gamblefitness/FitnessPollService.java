package io.github.fahadkh.gamblefitness;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * Created by Fahad on 2/8/17.
 * Service for querying sensor data in background
 */

public class FitnessPollService extends IntentService {

    private Sensor mAccel;
    private SensorManager mManager;

    private boolean accelIsRegistered = false;

    public FitnessPollService() {
        super("FitnessPollService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        mManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccel = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }
}
