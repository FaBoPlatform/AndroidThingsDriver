package io.fabo.driver;

import android.hardware.Sensor;
import android.util.Log;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensor;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

public class BH1749ColorDriver implements AutoCloseable {

    private static final String TAG = BH1749ColorDriver.class.getSimpleName();
    private static final String DRIVER_NAME = "FaBoBH1749";
    private static final String DRIVER_VENDOR = "FaBo";
    private static final int DRIVER_VERSION = 1;
    private BH1749 mDevice;
    private UserSensor mUserSensor;

    /**
     * Create a new framework accelerometer driver connected to the given I2C bus.
     * The driver emits {@link Sensor} with acceleration data when registered.
     * @param bus
     * @throws IOException
     * @see #register()
     */
    public BH1749ColorDriver(String bus) throws IOException {
        mDevice = new BH1749(bus);
    }

    /**
     * Close the driver and the underlying device.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        unregister();
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Register the driver in the framework.
     * @see #unregister()
     */
    public void register() {
        if (mDevice == null) {
            throw new IllegalStateException("cannot registered closed driver");
        }
        if (mUserSensor == null) {
            mUserSensor = build(mDevice);
            UserDriverManager.getInstance().registerSensor(mUserSensor);
        }
    }

    /**
     * Unregister the driver from the framework.
     */
    public void unregister() {
        if (mUserSensor != null) {
            UserDriverManager.getInstance().unregisterSensor(mUserSensor);
            mUserSensor = null;
        }
    }

    static UserSensor build(final BH1749 bh1749) {
        return new UserSensor.Builder()
                .setCustomType(Sensor.TYPE_DEVICE_PRIVATE_BASE,
                        "fabo.io.color",
                        Sensor.REPORTING_MODE_CONTINUOUS)
                .setName(DRIVER_NAME)
                .setVendor(DRIVER_VENDOR)
                .setVersion(DRIVER_VERSION)
                .setUuid(UUID.randomUUID())
                .setDriver(new UserSensorDriver() {
                    @Override
                    public UserSensorReading read() throws IOException {
                        float data[] = {};

                        if(bh1749.readValid()) {
                            float red = bh1749.readRed();
                            float green = bh1749.readGreen();
                            float blue = bh1749.readBlue();
                            float ir = bh1749.readIR();
                            float green2 = bh1749.readGreen2();
                            data = new float[]{red, green, blue, ir, green2};
                            return new UserSensorReading(data);
                        }

                        return null;
                    }

                    @Override
                    public void setEnabled(boolean enabled) throws IOException {
                        if (enabled) {

                            bh1749.reset();
                            if(bh1749.whoAmI()) {
                                bh1749.setIRGain(BH1749.IR_GAIN_X1);
                                bh1749.setRGBGain(BH1749.RGB_GAIN_X1);
                                bh1749.setMeasurement(BH1749.MEAS_240MS);
                                bh1749.setMeasurementEnable(true);
                            }

                        } else {
                            bh1749.setMeasurementEnable(false);
                            bh1749.reset();
                        }
                    }
                })
                .build();
    }
}
