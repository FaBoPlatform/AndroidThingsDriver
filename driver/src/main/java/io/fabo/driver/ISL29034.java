package io.fabo.driver;

import android.support.annotation.IntDef;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class ISL29034 implements AutoCloseable {
    private static final String TAG = ISL29034.class.getSimpleName();

    /**
     * I2C slave address of the ISL29034.
     */
    public final int I2C_ADDRESS = 0x44;

    /** ISL29034 Device ID(xx101xxx). */
    private final byte DEVICE_ID = 0x28;

    /** Register Addresses. */
    private final byte REG_CMD1 = 0x00;
    private final byte REG_CMD2 = 0x01;
    private final byte REG_DATA_L = 0x02;
    private final byte REG_DATA_H = 0x03;
    private final byte REG_ID = 0x0F;

    /**
     * Operation Mode.
     */
    public final static int MODE_POWER_DOWN = 0b000<<5;
    public final static int MODE_ALS_ONCE = 0b001<<5;
    public final static int MODE_IR_ONCE = 0b010<<5;
    public final static int MODE_ALS_CONTINUS = 0b101<<5;
    public final static int MODE_IR_CONTINUS = 0b110<<5;
    @IntDef({MODE_POWER_DOWN, MODE_ALS_ONCE, MODE_IR_ONCE, MODE_ALS_CONTINUS, MODE_IR_CONTINUS})
    public @interface ControlMode {}

    /**
     * Full scale lux range.
     */
    public final static int RANGE_0 = 0b00; //< 1,000(Default)
    public final static int RANGE_1 = 0b01; //< 4,000
    public final static int RANGE_2 = 0b10; //< 16,000
    public final static int RANGE_3 = 0b11; //< 64,000
    @IntDef({RANGE_0, RANGE_1, RANGE_2, RANGE_3})
    public @interface luxRange {}

    /**
     * ADC RESOLUTION.
     */
    public final static int RES_16 = 0b00<<2; //< 16bit(Default)
    public final static int RES_12 = 0b01<<2; //< 12bit
    public final static int RES_8 = 0b10<<2; //< 8bit
    public final static int  RES_4 = 0b11<<2; //< 4bit
    @IntDef({RES_16, RES_12, RES_8, RES_4})
    public @interface adcResolution {}


    private final int OFFSET_RANGE = 0b00000011;
    private final int OFFSET_RES = 0b00001100;
    public  final int OFFSET_DEVICE_ID = 0b00111000;
    private final int OFFSET_BIT_8 = 0b11111111;
    private final int OFFSET_BIT_4 = 0b00001111;

    private I2cDevice mDevice;

    private static int mRange;
    private static int mResolution;

    /**
     * Create a new ISL29034 driver connected to the given I2C bus.
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
     * Create a new ISL29034 driver connected to the given I2C device.
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
            byte value = mDevice.readRegByte(REG_ID);
            if((value & OFFSET_DEVICE_ID) == DEVICE_ID) {
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
     * @param controlMode Operation Mode
     */
    public void setOperation(@ControlMode int controlMode) {
        try {
            mDevice.writeRegByte(REG_CMD1, (byte)controlMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set FullScale Range.
     * @param range FullScale Range
     */
    public void setRange(@luxRange int range) {
        mRange = range;
        try {
            byte data = mDevice.readRegByte(REG_CMD2);
            data &= OFFSET_RANGE;
            data |= range;
            mDevice.writeRegByte(REG_CMD2, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set ADC Resolution.
     * @param resolution Resolution
     */
    public void setResolution(@adcResolution int resolution) {
        mResolution = resolution;
        try {
            byte data = mDevice.readRegByte(REG_CMD2);
            data &= OFFSET_RES;
            data |= resolution;
            mDevice.writeRegByte(REG_CMD2, data);
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
        int adc = 0;
        try {
            switch (mResolution) {
                case RES_16:
                    Thread.sleep(105);
                    adc = (int)(data[1]&OFFSET_BIT_8)<<8 | (int)(data[0]&OFFSET_BIT_8);
                    break;
                case RES_12:
                    Thread.sleep(6);
                    adc = (int)(data[1]&OFFSET_BIT_4)<<8 | (int)(data[0]&OFFSET_BIT_8);
                    break;
                case RES_8:
                    Thread.sleep(0,352);
                    adc = (int)(data[0]&OFFSET_BIT_8);
                    break;
                case RES_4:
                    Thread.sleep(0,22);
                    adc = (int)(data[0]&OFFSET_BIT_4);
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            mDevice.readRegBuffer(REG_DATA_L, data, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            case RANGE_0:
                range = 1000;
                break;
            case RANGE_1:
                range = 4000;
                break;
            case RANGE_2:
                range = 16000;
                break;
            case RANGE_3:
                range = 64000;
                break;
        }

        switch (mResolution) {
            case RES_16:
                count = 2^16;
                break;
            case RES_12:
                count = 2^12;
                break;
            case RES_8:
                count = 2^8;
                break;
            case RES_4:
                count = 2^4;
                break;
        }

        return ((float)range / (float)count) * (float)adc;
    }

}
