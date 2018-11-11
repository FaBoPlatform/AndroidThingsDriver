package io.fabo.faboexample;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import io.fabo.driver.Adx345AccelerometerDriver;
import io.fabo.driver.BH1749ColorDriver;
import io.fabo.driver.ISL29034AmbientDriver;
import io.fabo.driver.S11059ColorDriver;
import io.fabo.driver.Si1132UVDriver;

public class MainActivity extends Activity implements SensorEventListener {
    private Adx345AccelerometerDriver mAdx345AccelerometerDriver;
    private ISL29034AmbientDriver mISL29034AmbientDriver;
    private S11059ColorDriver mS11059ColorDriver;
    private Si1132UVDriver mSi1132UVDriver;
    private BH1749ColorDriver mBH1749ColorDriver;


    private SensorManager mSensorManager;
    //private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG = "TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerDynamicSensorCallback(new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                Log.i(TAG, "Connected");
                Log.i(TAG, "sensor.getType():" + sensor.getType());
                Log.i(TAG, "sensor.getName():" + sensor.getName());
                if (sensor.getType() == Sensor.TYPE_DEVICE_PRIVATE_BASE) {
                    /*if(sensor.getName().startsWith("FaBoS11059")) {
                        Log.i(TAG, "Light sensor connected");
                        mSensorManager.registerListener(MainActivity.this, sensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    } else */if(sensor.getName().startsWith("FaBoSi1132")) {
                        Log.i(TAG, "UV sensor connected");
                        mSensorManager.registerListener(MainActivity.this, sensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    } else
                    if(sensor.getName().startsWith("FaBoBH1749")) {
                        Log.i(TAG, "FaBoBH1749 sensor connected");
                        mSensorManager.registerListener(MainActivity.this, sensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                } else if (sensor.getType() == Sensor.TYPE_LIGHT) {
                    Log.i(TAG, "sensor.getName():" + sensor.getName());
                    if(sensor.getName().startsWith("FaBoISL29034")) {
                        Log.i(TAG, "Light sensor connected");
                        mSensorManager.registerListener(MainActivity.this, sensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                }
            }
        });
        /*
        try {
            mAdx345AccelerometerDriver = new Adx345AccelerometerDriver(BoardDefaults.getI2CPort());
            mAdx345AccelerometerDriver.register();
            Log.i(TAG, "Accelerometer driver registered");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing accelerometer driver: ", e);
        }
        */


        try {
            mISL29034AmbientDriver = new ISL29034AmbientDriver(BoardDefaults.getI2CPort());
            mISL29034AmbientDriver.register();
            Log.i(TAG, "Accelerometer driver registered");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing accelerometer driver: ", e);
        }

        /*
        try {
            mS11059ColorDriver = new S11059ColorDriver(BoardDefaults.getI2CPort());
            mS11059ColorDriver.register();
            Log.i(TAG, "mS11059ColorDriver driver registered");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing accelerometer driver: ", e);
        }
        */
        try {
            mSi1132UVDriver = new Si1132UVDriver(BoardDefaults.getI2CPort());
            mSi1132UVDriver.register();
            Log.i(TAG, "mSi1132UVDriver driver registered");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing accelerometer driver: ", e);
        }

        try {
            mBH1749ColorDriver = new BH1749ColorDriver(BoardDefaults.getI2CPort());
            mBH1749ColorDriver.register();
            Log.i(TAG, "mBH1749ColorDriver driver registered");
        } catch (IOException e) {
            Log.e(TAG, "Error initializing accelerometer driver: ", e);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        if (mAdx345AccelerometerDriver != null) {
            mSensorManager.unregisterListener(this);
            mAdx345AccelerometerDriver.unregister();
            try {
                mAdx345AccelerometerDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing accelerometer driver: ", e);
            } finally {
                mAdx345AccelerometerDriver = null;
            }
        }
        */

        /*
        if (mISL29034AmbientDriver != null) {
            mSensorManager.unregisterListener(this);
            mISL29034AmbientDriver.unregister();
            try {
                mISL29034AmbientDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing accelerometer driver: ", e);
            } finally {
                mISL29034AmbientDriver = null;
            }
        }
        */
        /*
        if (mS11059ColorDriver != null) {
            mSensorManager.unregisterListener(this);
            mS11059ColorDriver.unregister();
            try {
                mS11059ColorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing accelerometer driver: ", e);
            } finally {
                mS11059ColorDriver = null;
            }
        }
        */
        if (mBH1749ColorDriver != null) {
            mSensorManager.unregisterListener(this);
            mBH1749ColorDriver.unregister();
            try {
                mBH1749ColorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing color driver: ", e);
            } finally {
                mBH1749ColorDriver = null;
            }
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.i(TAG, "event.sensor.getName()=" + event.sensor.getName());
        /*
        if(event.sensor.getName().startsWith("FaBoAdx345")) {
            Log.i(TAG, "Accelerometer event: " +
                    event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        } else if(event.sensor.getName().startsWith("FaBoISL29034")) {
            Log.i(TAG, "Ambient event: " + event.values[0]);
        } else if(event.sensor.getName().startsWith("FaBoS11059")) {
            Log.i(TAG, "Color event: " + event.values[0] + ", " + event.values[1] + ", "
                    + event.values[2] + ", " + event.values[3]);
        } else if(event.sensor.getName().startsWith("FaBoSi1132")) {
            Log.i(TAG, "Uv event: " + event.values[0] + ", " + event.values[1] + ", "
                    + event.values[2]);
        } else */
        if(event.sensor.getName().startsWith("FaBoISL29034")) {
            Log.i(TAG, "Ambient event: " + event.values[0]);
        } else if(event.sensor.getName().startsWith("FaBoBH1749")) {
            int minRGBValues[] = {53, 128, 37};
            int maxRGBValues[] = {206, 408, 213};
            int red = convertRaw2RGB(event.values[0], minRGBValues[0], maxRGBValues[0],0, 255);
            int green = convertRaw2RGB(event.values[1], minRGBValues[1], maxRGBValues[1],0, 255);
            int blue = convertRaw2RGB(event.values[2], minRGBValues[2], maxRGBValues[2],0, 255);

            Log.i(TAG, "Color event: " + red + ", " + green + ", "
                    + blue + ", " + event.values[3] + ", " + event.values[4]);
        } else if(event.sensor.getName().startsWith("FaBoSi1132")) {
            Log.i(TAG, "Uv event: " + event.values[0] + ", " + event.values[1] + ", "
                    + event.values[2]);
        }
    }

    public int convertRaw2RGB(float x, int in_min, int in_max, int out_min, int out_max)
    {
        if(x < in_min) return 0;
        else if(x > in_max) return 255;
        else return (int)(x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
