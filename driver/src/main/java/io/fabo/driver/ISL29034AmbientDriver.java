package io.fabo.driver;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.sensor.UserSensor;
import com.google.android.things.userdriver.sensor.UserSensorDriver;
import com.google.android.things.userdriver.sensor.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

public class ISL29034AmbientDriver implements AutoCloseable {

    private static final String TAG = ISL29034AmbientDriver.class.getSimpleName();
    private static final String DRIVER_NAME = "FaBoISL29034";
    private static final String DRIVER_VENDOR = "FaBo";
    private static final int DRIVER_VERSION = 1;
    private ISL29034 mDevice;
    private UserSensor mUserSensor;

    /**
     * Create a new framework accelerometer driver connected to the given I2C bus.
     * The driver emits {@link Sensor} with acceleration data when registered.
     * @param bus
     * @throws IOException
     * @see #register()
     */
    public ISL29034AmbientDriver(String bus) throws IOException {
        mDevice = new ISL29034(bus);
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

    static UserSensor build(final ISL29034 isl29034) {
        return new UserSensor.Builder()
                .setType(Sensor.TYPE_LIGHT)
                .setName(DRIVER_NAME)
                .setVendor(DRIVER_VENDOR)
                .setVersion(DRIVER_VERSION)
                .setUuid(UUID.randomUUID())
                .setDriver(new UserSensorDriver() {
                    @Override
                    public UserSensorReading read() throws IOException {
                        float lux = isl29034.readLux();
                        float data[] = {lux};
                        return new UserSensorReading(data);
                    }

                    @Override
                    public void setEnabled(boolean enabled) throws IOException {
                        if (enabled) {
                            if(isl29034.whoAmI()) {
                                isl29034.setRange(ISL29034.RANGE_3);
                                isl29034.setResolution(ISL29034.RES_16);
                                isl29034.setOperation(ISL29034.MODE_ALS_CONTINUS);
                            }
                        } else {
                            isl29034.setOperation(ISL29034.MODE_POWER_DOWN);
                        }
                    }
                })
                .build();
    }
}
