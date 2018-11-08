package io.fabo.driver;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class ISL29034 implements AutoCloseable {
    private static final String TAG = Adx345.class.getSimpleName();

    /**
     * I2C slave address of the ISL29034.
     */
    public final int I2C_ADDRESS = 0x44;

    /** ISL29034 Device ID(xx101xxx). */
    private final byte ISL29034_DEVICE_ID = 0x28;

    /** Register Addresses. */
    private final byte ISL29034_REG_CMD1 = 0x00;
    private final byte ISL29034_REG_CMD2 = 0x01;
    private final byte ISL29034_REG_DATA_L = 0x02;
    private final byte ISL29034_REG_DATA_H = 0x03;
    private final byte ISL29034_REG_ID = 0x0F;

    // Operation Mode
    /** Power-down the device(Default). */
    private final byte ISL29034_OP_PWR_DOWN = 0x00;
    /** Measures ALS continuously. */
    public  final byte ISL29034_OP_ALS_CONT = (byte)0xA0;

    /** FULL SCALE LUX RANGE. */
    public  final byte ISL29034_FS_0 = 0x00; //< 1,000(Default)
    public  final byte ISL29034_FS_1 = 0x01; //< 4,000
    public  final byte ISL29034_FS_2 = 0x02; //< 16,000
    public  final byte ISL29034_FS_3 = 0x03; //< 64,000

    /** ADC RESOLUTION. */
    public  final byte ISL29034_RES_16 = 0x00; //< 16bit(Default)
    public  final byte ISL29034_RES_12 = 0x04; //< 12bit
    public  final byte ISL29034_RES_8 = 0x08; //< 8bit
    public  final byte ISL29034_RES_4 = 0x0C; //< 4bit

    /** ISL29034 Device ID Mask(00111000). */
    public  final byte ISL29034_ID_MASK = 0x38;

    private I2cDevice mDevice;

    private byte mRange;
    private byte mResolution;

    /**
     * Create a new Adx345 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public ISL29034(String bus) throws IOException {
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
    /*package*/ ISL29034(I2cDevice device) throws IOException {
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
            byte value = mDevice.readRegByte(ISL29034_REG_ID);
            if((value & ISL29034_ID_MASK) == ISL29034_DEVICE_ID) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Set Operation Mode.
     * @param operation Operation Mode
     */
    public void setOperation(byte operation) {
        try {
            mDevice.writeRegByte(ISL29034_REG_CMD1, operation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set FullScale Range.
     * @param range FullScale Range
     */
    public void setRange(byte range) {
        mRange = range;
        try {
            byte data = mDevice.readRegByte(ISL29034_REG_CMD2);
            data &= 0xFC; // 11111100
            data |= range;
            mDevice.writeRegByte(ISL29034_REG_CMD2, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set ADC Resolution.
     * @param resolution Resolution
     */
    public void setResolution(byte resolution) {
        mResolution = resolution;
        try {
            byte data = mDevice.readRegByte(ISL29034_REG_CMD2);
            data &= 0xF3; // 11111100
            data |= resolution;
            mDevice.writeRegByte(ISL29034_REG_CMD2, data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * read ADC.
     * @return adc.
     */
    private int readADC() {
        byte data[] = new byte[2];
        try {
            switch (mResolution) {
                case ISL29034_RES_16:
                    Thread.sleep(90);
                    break;
                case ISL29034_RES_12:
                    Thread.sleep(6);
                    break;
                case ISL29034_RES_8:
                    Thread.sleep(0,352);
                    break;
                case ISL29034_RES_4:
                    Thread.sleep(0,22);
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            mDevice.readRegBuffer(ISL29034_REG_DATA_L, data, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int adc = (((int)data[1]&0xff)<<8) | (int)data[0]&0xff;
        return adc;
    }

    /**
     * Read lux.
     * @return lux
     */
    public float readLux() {
        int adc = readADC();
        int range = 0;
        int count = 0;

        switch (mRange) {
            case ISL29034_FS_0:
                range = 1000;
                break;
            case ISL29034_FS_1:
                range = 4000;
                break;
            case ISL29034_FS_2:
                range = 16000;
                break;
            case ISL29034_FS_3:
                range = 64000;
                break;
        }

        switch (mResolution) {
            case ISL29034_RES_16:
                count = 65535;
                break;
            case ISL29034_RES_12:
                count = 4095;
                break;
            case ISL29034_RES_8:
                count = 255;
                break;
            case ISL29034_RES_4:
                count = 15;
                break;
        }

        return ((float)range / (float)count) * (float)adc;
    }
}
