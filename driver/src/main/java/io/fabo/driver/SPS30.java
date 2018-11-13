package io.fabo.driver;

import android.support.annotation.IntDef;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class SPS30 implements AutoCloseable {
    private static final String TAG = SPS30.class.getSimpleName();

    /**
     * I2C slave address of the ADC121.
     */
    public static final int I2C_ADDRESS = 0x69;

    // Register.
    /** Start Measurement. */
    private final int REG_START_MEAS = 0x0010;
    /** Stop Measurement. */
    private final int REG_STOP = 0x0104;
    /** Read Data-Ready Flag. */
    private final int REG_READY = 0x0202;
    /** Read Measured Values. */
    private final int REG_READ_MEAS = 0x0300;
    /** Read/Write Auto Cleaning Interval. */
    private final int REG_RW_CELAN_INTERVAL = 0x8004;
    /** Start Fan Cleaning. */
    private final int REG_FAN_CLEANING = 0x5607;
    /** Read Article Code. */
    private final int REG_READ_ARTICLE = 0xD025;
    /** Read Serial Number. */
    private final int REG_READ_SERIAL = 0xD033;
    /** Reset. */
    private final int REG_RESET = 0xD304;
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
    public SPS30(String bus) throws IOException {
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
    /*package*/ SPS30(I2cDevice device) throws IOException {
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
     * Start.
     */
    public void start() {
        byte command[] = {0x03,0x00};
        byte checkSum = checkSum(command);
        byte data[] = new byte[3];
        data[0] = command[0];
        data[1] = command[1];
        data[2] = checkSum;

        try {
            mDevice.writeRegBuffer(REG_START_MEAS, command, command.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop.
     */
    public void stop() {
        try {
            mDevice.write(new byte[]{(byte) REG_STOP}, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset.
     */
    public void reset() {
        try {
            mDevice.write(new byte[]{(byte) REG_RESET}, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start fan cleaning.
     */
    public void startFanCleaning() {
        try {
            mDevice.write(new byte[]{(byte) REG_FAN_CLEANING}, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check ready.
     * @return
     */
    public boolean checkReady() {
        try {
            byte result_buff[] = new byte[3];
            mDevice.readRegBuffer(REG_READY, result_buff, result_buff.length);
            if(result_buff[1] == 0x01) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check sum
     * @param data
     * @return
     */
    public byte checkSum(byte[] data) {
        byte crc = (byte)0xff;
        for (int i = 0; i < data.length; i++) {
            crc ^= data[i];
            for (int bit = 8; bit > 0; --bit) {
                if ((crc & 0x80) == 0x80) {
                    crc = (byte)((crc << 1) ^ 0x31);
                } else {
                    crc = (byte)(crc << 1);
                }
            }
        }
        return crc;
    }

    /**
     * Read data
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    public float[] readData() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        byte data_buff[] = new byte[59];
        mDevice.readRegBuffer(REG_READ_MEAS, data_buff, data_buff.length);
        float pm1_0 = ((data_buff[0] & 0xff) << 24 + data_buff[1] & 0xff) << 16 + (data_buff[3] & 0xff) << 8 + data_buff[4] & 0xff;
        float pm2_5 = ((data_buff[6] & 0xff) << 24 + data_buff[7] & 0xff) << 16 + (data_buff[9] & 0xff) << 8 + data_buff[10] & 0xff;
        float pm4_0 = ((data_buff[12] & 0xff) << 24 + data_buff[13] & 0xff) << 16 + (data_buff[15] & 0xff) << 8 + data_buff[16] & 0xff;
        float pm10 = ((data_buff[18] & 0xff) << 24 + data_buff[19] & 0xff) << 16 + (data_buff[21] & 0xff) << 8 + data_buff[22] & 0xff;
        return new float[]{
                pm1_0, pm2_5, pm4_0, pm10
        };
    }
}
