package io.fabo.driver;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class Adx345 implements AutoCloseable {
    private static final String TAG = Adx345.class.getSimpleName();

    /**
     * I2C slave address of the Adx345.
     */
    public static final int I2C_ADDRESS = 0x53;

    /**
     * Sampling rate of the measurement.
     */
    /** Who_am_i register */
    private byte ADXL345_DEVID_REG = 0x00;
    /** name of adx345 */
    private byte ADXL345_DEVICE_NAME = (byte) 0xE5;
    /** Data Format Control */
    private byte ADXL345_DATA_FORMAT_REG = 0x31;
    /** Power-saving features control */
    private byte ADXL345_POWER_CTL_REG = 0x2D;
    /** Power-saving features control */
    private byte ADXL345_3AIXS = 0x32;
    /** SELF Test ON */
    private byte ADXL345_SELF_TEST_ON = (byte) 0b10000000;
    /** SELF Test OFF */
    private byte ADXL345_SELF_TEST_OFF = 0b00000000;
    /** SELF SPI ON */
    private byte ADXL345_SPI_ON = 0b01000000;
    /** SELF SPI OFF */
    private byte ADXL345_SPI_OFF = 0b00000000;
    /** INT_INVERT ON */
    private byte ADXL345_INT_INVERT_ON = 0b00100000;
    /** INT_INVERT OFF */
    private byte ADXL345_INT_INVERT_OFF = 0b00000000;
    /** FULL_RES ON */
    private byte ADXL345_FULL_RES_ON = 0b00001000;
    /** FULL_RES OFF */
    private byte ADXL345_FULL_RES_OFF = 0b00000000;
    /** JUSTIFY ON */
    private byte ADXL345_JUSTIFY_ON = 0b00000100;
    /** JUSTIFY OFF */
    private byte ADXL345_JUSTIFY_OFF = 0b00000000;
    /** RANGE 2G */
    private byte ADXL345_RANGE_2G = 0b00;
    /** RANGE 4G */
    private byte ADXL345_RANGE_4G = 0b01;
    /** RANGE 8G */
    private byte ADXL345_RANGE_8G = 0b10;
    /** RANGE 16G */
    private byte ADXL345_RANGE_16G = 0b11;
    /** AUTO SLEEP ON */
    private byte ADXL345_AUTO_SLEEP_ON = 0b00010000;
    /** AUTO SLEEP OFF */
    private byte ADXL345_AUTO_SLEEP_OFF = 0b00000000;
    /** AUTO MEASURE ON */
    private byte ADXL345_MEASURE_ON = 0b00001000;
    /** AUTO MEASURE OFF */
    private byte ADXL345_MEASURE_OFF = 0b00000000;
    /** SLEEP ON */
    private byte ADXL345_SLEEP_ON = 0b00000100;
    /** SLEEP OFF */
    private byte ADXL345_SLEEP_OFF = 0b00000000;
    /** WAKEUP 8Hz */
    private byte ADXL345_WAKEUP_8HZ = 0b00;
    /** WAKEUP 4Hz */
    private byte ADXL345_WAKEUP_4HZ = 0b01;
    /** WAKEUP 2Hz */
    private byte ADXL345_WAKEUP_2HZ = 0b10;
    /** WAKEUP 1Hz */
    private byte ADXL345_WAKEUP_1HZ = 0b11;

    private I2cDevice mDevice;

    /**
     * Create a new Adx345 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public Adx345(String bus) throws IOException {
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
     * Create a new Adx345 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ Adx345(I2cDevice device) throws IOException {
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
            byte value = mDevice.readRegByte(ADXL345_DEVID_REG);
            if((value & 0xff) == ADXL345_DEVICE_NAME) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Set configure.
     */
    public void setConfigure() {
        byte conf = ADXL345_SELF_TEST_OFF;
        conf |= ADXL345_SPI_OFF;
        conf |= ADXL345_INT_INVERT_OFF;
        conf |= ADXL345_FULL_RES_OFF;
        conf |= ADXL345_JUSTIFY_OFF;
        conf |= ADXL345_RANGE_16G;
        try {
            mDevice.writeRegByte(ADXL345_DATA_FORMAT_REG, conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Power on.
     */
    public void powerOn() {
        byte power = ADXL345_AUTO_SLEEP_OFF;
        power |= ADXL345_MEASURE_ON;
        power |= ADXL345_SLEEP_OFF;
        power |= ADXL345_WAKEUP_8HZ;
        try {
            mDevice.writeRegByte(ADXL345_POWER_CTL_REG, power);
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
    public float[] readSample() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        int length = 6;
        byte axis_buff[] = new byte[length];
        mDevice.readRegBuffer(ADXL345_3AIXS, axis_buff, axis_buff.length);
        int x = (((int)axis_buff[1]) << 8) | axis_buff[0];
        int y = (((int)axis_buff[3]) << 8) | axis_buff[2];
        int z = (((int)axis_buff[5]) << 8) | axis_buff[4];
        return new float[]{
                x, y, z
        };
    }



}
