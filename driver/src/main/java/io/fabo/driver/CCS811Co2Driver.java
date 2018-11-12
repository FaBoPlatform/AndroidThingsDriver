package io.fabo.driver;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensor;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

public class CCS811Co2Driver implements AutoCloseable {

    private static final String TAG = CCS811Co2Driver.class.getSimpleName();
    private static final String DRIVER_NAME = "CCS811";
    private static final String DRIVER_VENDOR = "GClue";
    private static final int DRIVER_VERSION = 1;
    private CCS811 mDevice;
    private UserSensor mUserSensor;

    /**
     * Create a new framework accelerometer driver connected to the given I2C bus.
     * The driver emits {@link Sensor} with acceleration data when registered.
     * @param bus
     * @throws IOException
     * @see #register()
     */
    public CCS811Co2Driver(String bus) throws IOException {
        mDevice = new CCS811(bus);
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

    static UserSensor build(final CCS811 ccs811) {
        return new UserSensor.Builder()
                .setCustomType(Sensor.TYPE_DEVICE_PRIVATE_BASE,
                        "fabo.io.co2",
                        Sensor.REPORTING_MODE_CONTINUOUS)
                .setName(DRIVER_NAME)
                .setVendor(DRIVER_VENDOR)
                .setVersion(DRIVER_VERSION)
                .setUuid(UUID.randomUUID())
                .setDriver(new UserSensorDriver() {
                    @Override
                    public UserSensorReading read() throws IOException {
                        float[] sample = ccs811.getAlgResultData();
                        return new UserSensorReading(sample);
                    }

                    @Override
                    public void setEnabled(boolean enabled) throws IOException {
                        if (enabled) {
                            ccs811.reset();
                            ccs811.start();
                            ccs811.setDriveMode(CCS811.MEAS_DRIVE_MODE_1);
                        } else {
                            // ToDo
                        }
                    }
                })
                .build();
    }
}
