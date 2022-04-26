package codes.richter.defaultdatacollection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class ClassifyActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager manager;
    Sensor accelerometer;
    Sensor barometer;

    TextView currentActivity;

    boolean classifyEnabled = false;

    ArrayList<Float> currentAccelDataX = new ArrayList<>();
    ArrayList<Float> currentAccelDataY = new ArrayList<>();
    ArrayList<Float> currentAccelDataZ = new ArrayList<>();
    ArrayList<Float> currentBarData = new ArrayList<>();
    float startTime;
    float currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classify);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        barometer = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        startTime = System.currentTimeMillis();


        Button toggleContextButton = findViewById(R.id.enableCollectionMode);
        currentActivity = findViewById(R.id.activityName);


        toggleContextButton.setOnClickListener(view -> {
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
        });


        Button toggleClassifyButton = findViewById(R.id.toggleClassifyButton);

        toggleClassifyButton.setOnClickListener(view -> {
            classifyEnabled = !classifyEnabled;
            toggleClassifyButton.setText(classifyEnabled ? "Stop Detection" : "Start Detection");
        });


    }

    @Override
    public void onSensorChanged(SensorEvent e) {
        currentTime = System.currentTimeMillis();
        // Accelerometer
        if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            currentAccelDataX.add(e.values[0]);
            currentAccelDataY.add(e.values[1]);
            currentAccelDataZ.add(e.values[2]);

            // Barometer
        } else if (e.sensor.getType() == Sensor.TYPE_PRESSURE) {
            currentBarData.add(e.values[0]);
        }

        if (currentTime - startTime >= 3000) {
            classifyWindow(
                    new ArrayList<>(currentAccelDataX),
                    new ArrayList<>(currentAccelDataY),
                    new ArrayList<>(currentAccelDataZ),
                    new ArrayList<>(currentBarData)
            );

            // Wipe Lists
            currentAccelDataX = new ArrayList<>();
            currentAccelDataY = new ArrayList<>();
            currentAccelDataZ = new ArrayList<>();
            currentBarData = new ArrayList<>();

            // Reset Start Time
            startTime = System.currentTimeMillis();
        }

    }


    private void classifyWindow(
            ArrayList<Float> accelWindowDataX,
            ArrayList<Float> accelWindowDataY,
            ArrayList<Float> accelWindowDataZ,
            ArrayList<Float> barWindowData
    ) {
        ArrayList<Float> accelMag = new ArrayList<Float>();

        for (int i = 0; i < accelWindowDataX.size(); i++) {
            float mag = (float) Math.sqrt(Math.pow(accelWindowDataX.get(i), 2) + Math.pow(accelWindowDataY.get(i), 2) + Math.pow(accelWindowDataZ.get(i), 2));
            accelMag.add(mag);
        }

        float meanX = mean(accelWindowDataX);
        float meanY = mean(accelWindowDataY);
        float meanZ = mean(accelWindowDataZ);
        float meanMag = mean(accelMag);

        float stdX = std(accelWindowDataX);
        float stdY = std(accelWindowDataY);
        float stdZ = std(accelWindowDataZ);
        float stdMag = std(accelMag);

        final long[] shape = new long[]{1, 8};
        final float[] inputData = new float[]{meanX, meanY, meanZ, meanMag, stdX, stdY, stdZ, stdMag};
    }


    private float mean (ArrayList<Float> table)
    {
        int total = 0;
        for (float item : table) {
            total += item;
        }
        return total/((float) table.size());
    }

    private float std(ArrayList<Float> table) {
        float mean = mean(table);
        float temp = 0;

        for (float val : table) {
            temp += (float) Math.pow(val - mean, 2);
        }

        float meanOfDiffs = (float) temp / (float) (table.size());
        return (float) Math.sqrt(meanOfDiffs);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }


    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }


    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        manager.registerListener(this, barometer, SensorManager.SENSOR_DELAY_GAME);
    }
}