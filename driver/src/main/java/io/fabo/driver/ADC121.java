package io.fabo.driver;

import android.support.annotation.IntDef;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class ADC121 implements AutoCloseable {
    private static final String TAG = ADC121.class.getSimpleName();

    /**
     * I2C slave address of the ADC121.
     */
    public static final int I2C_ADDRESS = 0x50;

    // Register.
    /** Address pointer register. */
    private final int REG_CONVERSION = 0x00;
    /** Alert status register. */
    private final int REG_ALERT_STATUS = 0x01;
    /** Configuration register. */
    private final int REG_CONFIGURATION = 0x02;
    /** Alert limit register(High). */
    private final int REG_ALERT_LIMIT_LW = 0x03;
    /** Alert limit register(Low). */
    private final int REG_ALERT_LIMIT_HG = 0x04;
    /** - Alert hysteresis register. */
    private final int REG_ALERT_HYSTERESIS = 0x06;
    /** Lowest conversion register. */
    private final int REG_LOW_CONVERSION = 0x07;
    /** Highest conversion register. */
    private final int REG_HIGH_CONVERSION = 0x08;

    // Offset
    private final int OFFSET_CYCLE_TIME = 0b11100000;
    private final int OFFSET_ALERT_HOLD = 0b00010000;
    private final int OFFSET_ALERT_FLAG = 0b00001000;
    private final int OFFSET_ALERT_PIN = 0b00000100;
    private final int OFFSET_PRIORITY = 0b00000001;
    /**
     * Cycle time.
     */
    public final static int INTERVAL_0 = 0b000;
    public final static int INTERVAL_27 = 0b001;
    public final static int INTERVAL_13_5 = 0b010;
    public final static int INTERVAL_6_7 = 0b011;
    public final static int INTERVAL_3_4 = 0b100;
    public final static int INTERVAL_1_7 = 0b101;
    public final static int INTERVAL_0_9 = 0b110;
    public final static int INTERVAL_0_4 = 0b111;
    @IntDef({INTERVAL_0, INTERVAL_27, INTERVAL_13_5, INTERVAL_6_7, INTERVAL_3_4, INTERVAL_1_7, INTERVAL_0_9, INTERVAL_0_4})
    public @interface CycleTime {}

    /**
     * Priority.
     */
    public final static int ACTIVE_LOW = 0;
    public final static int ACTIVE_HIGH = 1;
    @IntDef({ACTIVE_LOW, ACTIVE_HIGH})
    public @interface PriorityValue {}

    private I2cDevice mDevice;

    /**
     * Create a new ADC121 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public ADC121(String bus) throws IOException {
        PeripheralManager pioService = PeripheralManager.getInstance();
        I2cDevice device = pioService.openI2cDevice(bus, I2C_ADDRESS);
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
     * Create a new ADC121 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ ADC121(I2cDevice device) throws IOException {
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
     * Set cycle time.
     * @param cycleTime
     */
    public void setCycleTime(@CycleTime int cycleTime) {
        try {
            byte value = mDevice.readRegByte(REG_CONFIGURATION);
            value = (byte)((value & ~OFFSET_CYCLE_TIME) | cycleTime);
            mDevice.writeRegByte(REG_CONFIGURATION, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert flag enable.
     */
    public void alertFlagEnable(boolean flag) {
        try {
            byte value = mDevice.readRegByte(REG_CONFIGURATION);
            value = (byte)((value & ~OFFSET_ALERT_FLAG) | (flag ? 1<<3:0));
            mDevice.writeRegByte(REG_CONFIGURATION, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert pin enable.
     */
    public void alertPinEnable(boolean flag) {
        try {
            byte value = mDevice.readRegByte(REG_CONFIGURATION);
            value = (byte)((value & ~OFFSET_ALERT_PIN) | (flag ? 1<<2:0));
            mDevice.writeRegByte(REG_CONFIGURATION, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Alert hold.
     */
    public void alertHold(@PriorityValue int priorityValue) {
        try {
            byte value = mDevice.readRegByte(REG_CONFIGURATION);
            value = (byte)((value & ~OFFSET_ALERT_HOLD) | priorityValue << 4);
            mDevice.writeRegByte(REG_CONFIGURATION, value);
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
    public float readAdc() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        byte analog_buff[] = new byte[2];
        mDevice.readRegBuffer(REG_CONVERSION, analog_buff, analog_buff.length);
        return ((analog_buff[1] & 0x0f)<< 8) | (analog_buff[0] & 0xff);
    }
}
