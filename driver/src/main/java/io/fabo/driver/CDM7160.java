package io.fabo.driver;

import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class CDM7160 implements AutoCloseable {
    private static final String TAG = CDM7160.class.getSimpleName();

    /**
     * I2C slave address of the CDM7160.
     */
    public static final int I2C_ADDRESS = 0x69;

    // Register.
    /** Resets the module.. */
    private final int REG_RESET = 0x00;
    /** Sets the operation mode. */
    private final int REG_CONTROL = 0x01;
    /** Monitors the operation state. Read-only. */
    private final int REG_STATUS = 0x02;
    /** Lower CO2 concentration data.*/
    private final int REG_CO2_LO = 0x03;
    /** Upper CO2 concentration data.*/
    private final int REG_CO2_HI = 0x04;
    /** Set ttmospheric pressure data. */
    private final int REG_SET_ATMOS = 0x09;
    /** Sets the altitude value . */
    private final int REG_SET_ALTITUDE = 0x0A;
    /** Sets the upper limit concentration for the alarm signal. */
    private final int REG_SET_ALARM_HI = 0x0C;
    /** Sets the lower limit concentration for the alarm signal. */
    private final int REG_SET_ALARM_LP = 0x0D;
    /** User calibration w. */
    private final int REG_CALIBRATION = 0x0E;
    /** Sets correction for PWM output, atmospheric pressure and altitude. */
    private final int REG_FUNC = 0x0F;
    /** Error. */
    private final int REG_ERROR = 0x10;
    /** Sets the CO2 concentration. */
    private final int REG_SET_CONCENTRATION = 0x12;

    // Offset
    private final int OFFSET_BUSY = 0b10000000;
    private final int OFFSET_ALARM = 0b01000000;
    private final int OFFSET_CAD0 = 0b00000010;
    private final int OFFSET_MSEL = 0b00000001;

    /**
     * Control Mode.
     */
    public final static int MODE_POWER_DOWN = 0b000;
    public final static int MODE_CONTINUS = 0b110;
    @IntDef({MODE_POWER_DOWN, MODE_CONTINUS})
    public @interface ControlMode {}

    private I2cDevice mDevice;

    /**
     * Create a new CDM7160 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public CDM7160(String bus) throws IOException {
        PeripheralManager pioService = PeripheralManager.getInstance();
        I2cDevice device = pioService.openI2cDevice(bus, (byte)I2C_ADDRESS);
        try {
            connect(device);
        } catch (IOException|RuntimeException e) {
            try {
                close();
            } catch (IOException|RuntimeException ignored) {
            }
            throw e;
        }
    }

    /**
     * Create a new CDM7160 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ CDM7160(I2cDevice device) throws IOException {
        connect(device);
    }

    private void connect(I2cDevice device) throws IOException {
        if (mDevice != null) {
            throw new IllegalStateException("device already connected");
        }
        mDevice = device;
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }

    /**
     * Set controel.
     * @param controlMode
     */
    public void setControl(@ControlMode int controlMode) {
        try {
            Log.i("TEST3", "REG_CONTROL"+REG_CONTROL);
            Log.i("TEST3", "controlMode"+controlMode);
            mDevice.writeRegByte(REG_CONTROL, (byte)controlMode);
        } catch (IOException e) {
            Log.i("TEST3", "Error" + e);
            e.printStackTrace();
        }
    }

    /**
     * Get status value.
     */
    public byte getStatus() {
        try {
            return  mDevice.readRegByte(REG_STATUS);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Check busy.
     * @return
     */
    public boolean checkBusy() {
        if((getStatus() & OFFSET_BUSY) == getStatus()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Reset
     */
    public void reset() {
        try {
            mDevice.writeRegByte(REG_RESET, (byte)1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Read an accelerometer sample.
     * @return acceleration over xyz axis in G.
     * @throws IOException
     * @throws IllegalStateException
     */
    public float readCo2() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        byte co2_buff[] = new byte[2];
        mDevice.readRegBuffer(REG_CO2_LO, co2_buff, co2_buff.length);
        return ((co2_buff[1] & 0xff)<< 8) | (co2_buff[0] & 0xff);
    }
}
