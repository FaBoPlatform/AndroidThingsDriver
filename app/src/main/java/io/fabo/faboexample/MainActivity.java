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

import io.fabo.driver.ADC121AnalogDriver;
import io.fabo.driver.Adx345AccelerometerDriver;
import io.fabo.driver.BH1749ColorDriver;
import io.fabo.driver.CCS811Co2Driver;
import io.fabo.driver.CDM7160Co2Driver;
import io.fabo.driver.ISL29034AmbientDriver;
import io.fabo.driver.MPL115;
import io.fabo.driver.MPL115BarometerDriver;
import io.fabo.driver.S11059ColorDriver;
import io.fabo.driver.SPS30PMDriver;
import io.fabo.driver.Si1132UVDriver;

public class MainActivity extends Activity implements SensorEventListener {
    private Adx345AccelerometerDriver mAdx345AccelerometerDriver;
    private ISL29034AmbientDriver mISL29034AmbientDriver;
    private S11059ColorDriver mS11059ColorDriver;
    private Si1132UVDriver mSi1132UVDriver;
    private BH1749ColorDriver mBH1749ColorDriver;
    private CCS811Co2Driver mCCS811Co2Driver;
    private ADC121AnalogDriver mADC121AnalogDriver;
    private CDM7160Co2Driver mCDM7160Co2Driver;
    private SPS30PMDriver mSPS30PMDriver;
    private MPL115BarometerDriver mMPL115BarometerDriver;

    private boolean Adx345Enable = false;
    private boolean S11059Enable = false;
    private boolean BH1749Enable = false;
    private boolean CCS811Enable = false;
    private boolean ADC121Enable = false;
    private boolean ISL29034Enable = true;
    private boolean Si1132Enable = false;
    private boolean CDM7160Enable = false;
    private boolean SPS30Enable = false;
    private boolean MPL115Enable = false;

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
                Log.i(TAG, "[SYS] Connected");
                Log.i(TAG, "[SYS] sensor.getType():" + sensor.getType());
                Log.i(TAG, "[SYS] sensor.getName():" + sensor.getName());
                if (sensor.getType() == Sensor.TYPE_DEVICE_PRIVATE_BASE) {
                    if(sensor.getName().startsWith("FaBoS11059") ||
                            sensor.getName().startsWith("FaBoSi1132") ||
                            sensor.getName().startsWith("FaBoBH1749") ||
                            sensor.getName().startsWith("FaBoCCS811") ||
                            sensor.getName().startsWith("FaBoADC121") ||
                            sensor.getName().startsWith("FaBoCDM7160") ||
                            sensor.getName().startsWith("FaBoSPS30") ||
                            sensor.getName().startsWith("FaBoMPL115")) {
                            Log.i(TAG, sensor.getName() + " connected!");
                            mSensorManager.registerListener(MainActivity.this, sensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                } else if (sensor.getType() == Sensor.TYPE_LIGHT) {
                    if(sensor.getName().startsWith("FaBoISL29034")) {
                        Log.i(TAG, "FaBoISL29034 connected");
                        mSensorManager.registerListener(MainActivity.this, sensor,
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }
                }
            }
        });
        if(Adx345Enable) {
            try {
                mAdx345AccelerometerDriver = new Adx345AccelerometerDriver(BoardDefaults.getI2CPort());
                mAdx345AccelerometerDriver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error: ", e);
            }
        }
        if(ISL29034Enable) {
            try {
                mISL29034AmbientDriver = new ISL29034AmbientDriver(BoardDefaults.getI2CPort());
                mISL29034AmbientDriver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error: ", e);
            }
        }
        if(S11059Enable) {
            try {
                mS11059ColorDriver = new S11059ColorDriver(BoardDefaults.getI2CPort());
                mS11059ColorDriver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error: ", e);
            }
        }
        if(Si1132Enable) {
            try {
                mSi1132UVDriver = new Si1132UVDriver(BoardDefaults.getI2CPort());
                mSi1132UVDriver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error: ", e);
            }
        }
        if(BH1749Enable) {
            try {
                mBH1749ColorDriver = new BH1749ColorDriver(BoardDefaults.getI2CPort());
                mBH1749ColorDriver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error initializing accelerometer driver: ", e);
            }
        }
        if(CCS811Enable) {
            try {
                mCCS811Co2Driver = new CCS811Co2Driver(BoardDefaults.getI2CPort());
                mCCS811Co2Driver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error initializing accelerometer driver: ", e);
            }
        }
        if(ADC121Enable) {
            try {
                mADC121AnalogDriver = new ADC121AnalogDriver(BoardDefaults.getI2CPort());
                mADC121AnalogDriver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error initializing accelerometer driver: ", e);
            }
        }
        if(CDM7160Enable) {
            try {
                mCDM7160Co2Driver = new CDM7160Co2Driver(BoardDefaults.getI2CPort());
                mCDM7160Co2Driver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error initializing accelerometer driver: ", e);
            }
        }
        if(SPS30Enable) {
            try {
                mSPS30PMDriver = new SPS30PMDriver(BoardDefaults.getI2CPort());
                mSPS30PMDriver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error initializing accelerometer driver: ", e);
            }
        }
        if(MPL115Enable) {
            try {
                mMPL115BarometerDriver = new MPL115BarometerDriver(BoardDefaults.getI2CPort());
                mMPL115BarometerDriver.register();
            } catch (IOException e) {
                Log.e(TAG, "Error initializing accelerometer driver: ", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSensorManager.unregisterListener(this);

        if (mAdx345AccelerometerDriver != null) {
            mAdx345AccelerometerDriver.unregister();
            try {
                mAdx345AccelerometerDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mAdx345AccelerometerDriver = null;
            }
        }
        if (mISL29034AmbientDriver != null) {
            mISL29034AmbientDriver.unregister();
            try {
                mISL29034AmbientDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mISL29034AmbientDriver = null;
            }
        }
        if (mS11059ColorDriver != null) {
            mS11059ColorDriver.unregister();
            try {
                mS11059ColorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mS11059ColorDriver = null;
            }
        }
        if (mBH1749ColorDriver != null) {
            mBH1749ColorDriver.unregister();
            try {
                mBH1749ColorDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mBH1749ColorDriver = null;
            }
        }
        if (mCCS811Co2Driver != null) {
            mCCS811Co2Driver.unregister();
            try {
                mCCS811Co2Driver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mCCS811Co2Driver = null;
            }
        }
        if (mADC121AnalogDriver != null) {
            mADC121AnalogDriver.unregister();
            try {
                mADC121AnalogDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mADC121AnalogDriver = null;
            }
        }
        if (mCDM7160Co2Driver != null) {
            mCDM7160Co2Driver.unregister();
            try {
                mCDM7160Co2Driver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mCDM7160Co2Driver = null;
            }
        }
        if (mSPS30PMDriver != null) {
            mSPS30PMDriver.unregister();
            try {
                mSPS30PMDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mSPS30PMDriver = null;
            }
        }
        if (mMPL115BarometerDriver != null) {
            mMPL115BarometerDriver.unregister();
            try {
                mMPL115BarometerDriver.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing driver: ", e);
            } finally {
                mMPL115BarometerDriver = null;
            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, event.sensor.getName());

        if(event.sensor.getName().startsWith("FaBoAdx345")) {
            Log.i(TAG, "Accelerometer event: " +
                    event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        } else if(event.sensor.getName().startsWith("FaBoISL29034")) {
            Log.i(TAG, "Ambient: " + event.values[0] + "(lux)");
        } else if(event.sensor.getName().startsWith("FaBoS11059")) {
            Log.i(TAG, "Color event: " + event.values[0] + ", " + event.values[1] + ", "
                    + event.values[2] + ", " + event.values[3]);
        } else if(event.sensor.getName().startsWith("FaBoSi1132")) {
            Log.i(TAG, "UV Index(0-11): " + event.values[0] +
                    ",IR: " + event.values[1] + "(count)" +
                    ",Visible: " + event.values[2] +"(lux)");
        } else if(event.sensor.getName().startsWith("FaBoBH1749")) {
            int minRGBValues[] = {53, 128, 37};
            int maxRGBValues[] = {206, 408, 213};
            int red = convertRaw2RGB(event.values[0], minRGBValues[0], maxRGBValues[0],0, 255);
            int green = convertRaw2RGB(event.values[1], minRGBValues[1], maxRGBValues[1],0, 255);
            int blue = convertRaw2RGB(event.values[2], minRGBValues[2], maxRGBValues[2],0, 255);
            Log.i(TAG, "Red(0-255):" + red +
                        ", Green(0-255):" + green +
                        ", Blue(0-255):"  + blue +
                        ", IR(0-):" + event.values[3] + "(lux?)" +
                        ", Green2(0-):" + event.values[4] + "(lux?)");
        } else if(event.sensor.getName().startsWith("FaBoCCS811")) {
            Log.i(TAG, "CO2(400-8192):" + event.values[0] + "(ppm) " +
                            "TVOC(0-1187):" + event.values[1] + "(ppb)");
        } else if(event.sensor.getName().startsWith("FaBoADC121")) {
            float ppm = (float) ((1.99 * event.values[0]) / 4095.0 + 0.01);
            Log.i(TAG, "ppm(0.01ppm-2ppm):" + ppm + "(ppm)");
        } else if(event.sensor.getName().startsWith("FaBoCDM7160")) {
            Log.i(TAG, "ppm(0.01ppm-2ppm):" + event.values[0] + "(ppm)");
        } else if(event.sensor.getName().startsWith("FaBoSPS30")) {
            Log.i(TAG, "pm1.0:" + event.values[0] + "μg/m3");
            Log.i(TAG, "pm2.5:" + event.values[1] + "μg/m3");
            Log.i(TAG, "pm5:" + event.values[2] + "μg/m3");
            Log.i(TAG, "pm10:" + event.values[3] + "μg/m53");
        } else if(event.sensor.getName().startsWith("FaBoMPL115")) {
            Log.i(TAG, "hpm:" + event.values[0]);
            float aizuAltitude = 212.5f;
            Log.i(TAG, "hpm(Aizu):" + MPL115.hpaFromAltitude(event.values[0], aizuAltitude));
            Log.i(TAG, "temp:" + event.values[1]);
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
