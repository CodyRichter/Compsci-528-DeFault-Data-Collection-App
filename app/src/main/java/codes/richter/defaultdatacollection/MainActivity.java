package codes.richter.defaultdatacollection;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager manager;

    Sensor accelerometer;
    Sensor barometer;

    boolean badSwingMode = false;
    boolean currentlyTracking = false;
    String currentStroke = "Forehand";
    String currentFile = "";


    TextView selectedSwingView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        barometer = manager.getDefaultSensor(Sensor.TYPE_PRESSURE);


        selectedSwingView = findViewById(R.id.selectedSwingType);

        Button toggleRecordButton = findViewById(R.id.toggleRecordButton);
        Button deletePreviousButton = findViewById(R.id.deletePreviousButton);

        Button forehandButton = findViewById(R.id.forehandButton);
        Button backhandButton = findViewById(R.id.backhandButton);
        Button overheadButton = findViewById(R.id.overheadButton);

        SwitchMaterial badSwingModeSwitch = findViewById(R.id.badSwingModeSwitch);

        badSwingModeSwitch.setOnClickListener(l -> {
            badSwingMode = badSwingModeSwitch.isChecked();
        });

        forehandButton.setOnClickListener( view -> {
            currentStroke = "Forehand";
            selectedSwingView.setText("Forehand");
        });

        forehandButton.setOnClickListener( view -> {
            currentStroke = "Forehand";
            selectedSwingView.setText("Forehand");
        });
        backhandButton.setOnClickListener( view -> {
            currentStroke = "Backhand";
            selectedSwingView.setText("Backhand");
        });
        overheadButton.setOnClickListener( view -> {
            currentStroke = "Overhead";
            selectedSwingView.setText("Overhead");
        });

        deletePreviousButton.setEnabled(false);
        deletePreviousButton.setOnClickListener( view -> {
            if (!currentFile.equals("")) { // If file exists
                File dd = getApplicationContext().getExternalFilesDir("external_files");
                File barFile = new File(dd, "bar_" + currentFile);
                File accFile = new File(dd, "acc_" + currentFile);

                barFile.delete();
                accFile.delete();
                currentFile = "";
                deletePreviousButton.setEnabled(false);
            }
        });


        toggleRecordButton.setOnClickListener(view -> {

            if (currentlyTracking) {
                toggleRecordButton.setText("Start");
                currentlyTracking = false;
                forehandButton.setEnabled(true);
                backhandButton.setEnabled(true);
                overheadButton.setEnabled(true);
                deletePreviousButton.setEnabled(true);
                badSwingModeSwitch.setEnabled(true);
            } else {
                toggleRecordButton.setText("Stop");
                if (!badSwingMode) {
                    currentFile = currentStroke + "_" + String.valueOf(System.currentTimeMillis()) + ".csv";
                } else {
                    currentFile = currentStroke + "_" + String.valueOf(System.currentTimeMillis()) + "_bad" + ".csv";
                }
                currentlyTracking = true;
                forehandButton.setEnabled(false);
                backhandButton.setEnabled(false);
                overheadButton.setEnabled(false);
                deletePreviousButton.setEnabled(false);
                badSwingModeSwitch.setEnabled(false);
            }


        });
    }

    @Override
    public void onSensorChanged(SensorEvent e){

        // Accelerometer
        if (e.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = e.values[0];
            float y = e.values[1];
            float z = e.values[2];

            if (currentlyTracking) {
                try {
                    writeAccelDataToFile(x, y, z);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }

        // Barometer
        } else if (e.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float barometerReading = e.values[0];
            if (currentlyTracking) {
                try {
                    writeBarometerDataToFile(barometerReading);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor s, int acc){}

    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }


    protected void onResume() {
        super.onResume();
        manager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        manager.registerListener(this, barometer, SensorManager.SENSOR_DELAY_GAME);
    }


    private void writeBarometerDataToFile(float barVal) throws IOException {
        File dd = getApplicationContext().getExternalFilesDir("external_files");
        String tStamp = String.valueOf(System.currentTimeMillis());

        File f = new File(dd, "bar_" + currentFile);
        f.createNewFile();

        String lineToWrite = tStamp + "," + barVal + "\n";
        FileOutputStream fileOutputStream = new FileOutputStream(f, true);

        fileOutputStream.write(lineToWrite.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
    }


    private void writeAccelDataToFile(float x, float y, float z) throws IOException {
        File dd = getApplicationContext().getExternalFilesDir("external_files");
        String tStamp = String.valueOf(System.currentTimeMillis());

        File f = new File(dd, "acc_" + currentFile);
        f.createNewFile();

        String lineToWrite = tStamp + "," + x + "," + y + "," + z + "\n";
        FileOutputStream fileOutputStream = new FileOutputStream(f, true);

        fileOutputStream.write(lineToWrite.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
    }


}