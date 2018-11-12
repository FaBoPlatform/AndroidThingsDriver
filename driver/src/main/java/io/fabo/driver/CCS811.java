package io.fabo.driver;

import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class CCS811 implements AutoCloseable {
    private static final String TAG = CCS811.class.getSimpleName();

    /**
     * I2C slave address of the CCS811.
     */
    public static final int I2C_ADDRESS = 0x5B;

    // Register.
    /** Status register. */
    private final int REG_STATUS = 0x00;
    /** Measurement mode and conditions register. */
    private final int REG_MEAS_MODE = 0x01;
    /** Algorithm result.*/
    private final int REG_ALG_RESULT_DATA = 0x02;
    /** Raw ADC data. */
    private final int REG_RAW_DATA = 0x03;
    /** Temperature and Humidity data. */
    private final int REG_ENV_DATA = 0x05;
    /** Provides the voltage. */
    private final int REG_NTC = 0x06;
    /** Thresholds for operation. */
    private final int REG_THRESHOLDS = 0x10;
    /** The encoded current baseline value. */
    private final int REG_BASELINE = 0x11;
    /**  Hardware ID. CSS811 is 0x81. */
    private final int REG_HW_ID = 0x20;
    /** Hardware Version. The value is 0x1X. */
    private final int REG_HW_VERSION = 0x21;
    /** Firmware Boot Version. */
    private final int REG_FW_BOOT_VERSION = 0x23;
    /** Firmware Application Version. */
    private final int REG_FW_APP_VERSION = 0x24;
    /** Error ID. */
    private final int REG_ERROR_ID = 0xE0;
    /** Software reset. */
    private final int REG_SW_RESET = 0xFF;


    private final int REG_APP_ERASE = 0xF1;
    private final int REG_APP_DATA = 0xF2;
    private final int REG_APP_VALIFY = 0xF3;
    private final int REG_APP_START = 0xF4;


    /** Device id of CCS811. */
    private final int DEVICE_ID = 0x81;

    private final int STATUS_MODE_BOOT = 0b00000000;
    private final int STATUS_MODE_APP = 0b10000000;
    private final int STATUS_APP_NO_LOAD = 0b00000000;
    private final int STATUS_APP_VALID_LOAD = 0b00010000;
    private final int STATUS_DATA_NO_READY = 0b00000000;
    private final int STATUS_DATA_READY = 0b00001000;
    private final int STATUS_NO_ERR = 0b00000000;
    private final int STATUS_ERR = 0b00000001;

    /**
     * Meas mode.
     */
    public final static int MEAS_DRIVE_MODE_0 = 0b00000000;   // Idle mode  (Measurements are disabled in this mode)
    public final static int MEAS_DRIVE_MODE_1 = 0b00010000;   // Constant power mode  IAQ measurement every second
    public final static int MEAS_DRIVE_MODE_2 = 0b00100000;   // Pulse heating mode  IAQ measurement every 10 seconds
    public final static int MEAS_DRIVE_MODE_3 = 0b00110000;   // Low power pulse heating mode IAQ measurement every 60 seconds
    public final static int MEAS_DRIVE_MODE_4 = 0b01000000;   // Constant power mode, sensor measurement every 250ms
    @IntDef({MEAS_DRIVE_MODE_0, MEAS_DRIVE_MODE_1, MEAS_DRIVE_MODE_2, MEAS_DRIVE_MODE_3, MEAS_DRIVE_MODE_4})
    public @interface MeasMode {}

    private final int MEAS_DRIVE_MODE_MASK = 0b01110000;


    private I2cDevice mDevice;

    /**
     * Create a new CCS811 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public CCS811(String bus) throws IOException {
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
     * Create a new CCS811 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ CCS811(I2cDevice device) throws IOException {
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
     * Who am I .
     * @return find or not find.
     */
    public boolean whoAmI() {
        try {
            byte value = mDevice.readRegByte(REG_HW_ID);
            if((value & 0xff) == DEVICE_ID) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Reset.
     */
    public void reset() {
        try {
            byte reset_cmd[] = {(byte)0x11, (byte)0xE5, (byte)0x72, (byte)0x8A};
            mDevice.writeRegBuffer(REG_SW_RESET, reset_cmd, reset_cmd.length);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start.
     */
    public void start() {
        try {
            // Only write address.
            mDevice.writeRegBuffer(REG_APP_START, new byte[]{0}, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set mode.
     * @param mode
     */
    public void setDriveMode(@MeasMode int mode) {
        try {
            byte status = mDevice.readRegByte(REG_MEAS_MODE);
            status = (byte)((status & ~MEAS_DRIVE_MODE_MASK) | mode);
            mDevice.writeRegByte(REG_MEAS_MODE, status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get baseline.
     * @return
     */
    public int getBaseLine() {
        try {
            byte base_buff[] = new byte[2];
            mDevice.readRegBuffer(REG_BASELINE, base_buff, base_buff.length);
            return (int)((base_buff[0] << 8) | base_buff[1]);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Check status
     * @return status.
     */
    public boolean checkStatus() {
        try {
            byte status = mDevice.readRegByte(REG_STATUS);
            if ((status & STATUS_DATA_READY) == STATUS_DATA_READY) {
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
     * Get baseline.
     * @return
     */
    public boolean setBaseline(int baseline) {
        byte setData[] = new byte[2];
        setData[0] = (byte)((baseline >> 8) & 0xFF);
        setData[1] = (byte)(baseline & 0xFF);
        try {
            mDevice.writeRegBuffer(REG_BASELINE, setData, setData.length);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get alg result data.
     * @return co2, voc
     * @throws IOException
     * @throws IllegalStateException
     */
    public float[] getAlgResultData() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        byte alg_buff[] = new byte[4];
        mDevice.readRegBuffer(REG_ALG_RESULT_DATA, alg_buff, alg_buff.length);
        int co2Value = (alg_buff[0]<< 8) | alg_buff[1];
        int vocValue = (alg_buff[2]<< 8) | alg_buff[3];
        return new float[]{
                co2Value, vocValue
        };
    }
}
