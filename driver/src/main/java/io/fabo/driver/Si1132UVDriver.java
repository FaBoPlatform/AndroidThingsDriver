package io.fabo.driver;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensor;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

public class Si1132UVDriver implements AutoCloseable {

    private static final String TAG = Si1132UVDriver.class.getSimpleName();
    private static final String DRIVER_NAME = "FaBoSi1132";
    private static final String DRIVER_VENDOR = "FaBo";
    private static final int DRIVER_VERSION = 1;
    private Si1132 mDevice;
    private UserSensor mUserSensor;

    /**
     * Create a new framework accelerometer driver connected to the given I2C bus.
     * The driver emits {@link Sensor} with acceleration data when registered.
     * @param bus
     * @throws IOException
     * @see #register()
     */
    public Si1132UVDriver(String bus) throws IOException {
        mDevice = new Si1132(bus);
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

    static UserSensor build(final Si1132 si1132) {
        return new UserSensor.Builder()
                .setCustomType(Sensor.TYPE_DEVICE_PRIVATE_BASE,
                        "fabo.io.uv",
                        Sensor.REPORTING_MODE_CONTINUOUS)
                .setName(DRIVER_NAME)
                .setVendor(DRIVER_VENDOR)
                .setVersion(DRIVER_VERSION)
                .setUuid(UUID.randomUUID())
                .setDriver(new UserSensorDriver() {
                    @Override
                    public UserSensorReading read() throws IOException {
                        float uv = si1132.readUV();
                        float ir = si1132.readIR();
                        float visible = si1132.readVisible();

                        float data[] = {uv, ir, visible};
                        return new UserSensorReading(data);
                    }

                    @Override
                    public void setEnabled(boolean enabled) throws IOException {
                        if (enabled) {
                            si1132.reset();
                            si1132.configuration();
                            si1132.start();
                        } else {
                            si1132.setMeasRate(0);
                            si1132.pause();
                        }
                    }
                })
                .build();
    }
}
