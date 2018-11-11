package io.fabo.driver;

import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BH1749 implements AutoCloseable {
    private static final String TAG = BH1749.class.getSimpleName();

    /**
     * I2C slave address of the BH1749.
     */
    private final int I2C_ADDRESS = 0x38;

    /** Regiser address. */
    private final int REG_SYSTEM_CONTROL = 0x40;
    private final int REG_MODE_CONTROL1  = 0x41;
    private final int REG_MODE_CONTROL2  = 0x42;
    private final int REG_RED_DATA = 0x50;
    private final int REG_GREEN_DATA = 0x52;
    private final int REG_BLUE_DATA = 0x54;
    private final int REG_IR_DATA = 0x58;
    private final int REG_GREEN2_DATA = 0x5A;
    private final int REG_INTERRUPT = 0x60;
    private final int REG_PERSISTENCE = 0x61;
    private final int REG_TH_HIGH = 0x62;
    private final int REG_TH_LOW = 0x64;
    private final int REG_MANUFACTURER_ID = 0x92;

    // SYSTEM CONTROL
    private final byte PART_ID = 0x0D;
    private final byte PART_ID_MASK = 0b00111111;

    /**
     * ir gain.
     */
    public final static int IR_GAIN_X1 = 0b01;
    public final static int IR_GAIN_X32 = 0b11;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({IR_GAIN_X1, IR_GAIN_X32})
    public @interface IrGain {}

    public final int IR_GAIN_SHIFT = 5;
    public final int IR_GAIN_MASK = 0b01100000;

    /**
     * rgb gain.
     */
    public final static int RGB_GAIN_X1 = 0b01;
    public final static int RGB_GAIN_X32 = 0b11;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RGB_GAIN_X1, RGB_GAIN_X32})
    public @interface RgbGain {}

    public final int RGB_GAIN_SHIFT = 3;
    public final int RGB_GAIN_MASK = 0b00011000;

    /**
     * Measurement.
     */
    public final static int MEAS_35MS  = (byte)0b101;
    public final static int MEAS_120MS = (byte)0b010;
    public final static int MEAS_240MS = (byte)0b011;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEAS_35MS, MEAS_120MS, MEAS_240MS})
    public @interface MeasurementTime {}

    public final int MEAS_MASK = 0b00000111;

    // Mode Control 1

    private final int VALID_SHIFT = 7;
    private final int VALID_MASK = 0b1 << VALID_SHIFT;
    private final int RGB_EN_SHIFT = 4;
    private final int RGB_EN_MASK = 0b1 << RGB_EN_SHIFT;

    // Manufacturer ID
    private final int MANUFACTURER_ID = 0xE0;

    private I2cDevice mDevice;

    private byte mRange;
    private byte mResolution;

    /**
     * Create a new BH1749 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public BH1749(String bus) throws IOException {
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
     * Create a new BH1749 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ BH1749(I2cDevice device) throws IOException {
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

        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }


        try {
            byte value = mDevice.readRegByte(REG_SYSTEM_CONTROL);
              if((value & PART_ID_MASK) != PART_ID) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        try {
            byte value = mDevice.readRegByte(REG_MANUFACTURER_ID);
            if((value & 0xff) == MANUFACTURER_ID) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Reset.
     */
    public void reset() {
        try {
            mDevice.writeRegByte(REG_SYSTEM_CONTROL, (byte)0b10000000);
        } catch (IOException e) {
        }
    }

    /**
     * Get the gain of ir.
     * @return gain of ir.
     */
    public int getIRGain() {
        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL1);
            value &= IR_GAIN_MASK;
            return (value >> IR_GAIN_SHIFT);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Set the gain of ir.
     * @param irGain
     */
    public boolean setIRGain(@IrGain int irGain) {
        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL1);
            value &= ~(IR_GAIN_MASK);
            value |= (irGain << IR_GAIN_SHIFT);
            mDevice.writeRegByte(REG_MODE_CONTROL1, value);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Read the gain of RGB.
     * @return rgb gain.
     */
    public int getRGBGain() {
        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL1);
            value &= RGB_GAIN_MASK;
            return value >> RGB_GAIN_SHIFT;
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Set RGB Gain.
     * @param rgbGain gain RGB Gain.
     */
    public boolean setRGBGain(@RgbGain int rgbGain) {
        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL1);
            value &= ~(RGB_GAIN_MASK);
            value |= (rgbGain << RGB_GAIN_SHIFT);
            mDevice.writeRegByte(REG_MODE_CONTROL1, value);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get Measurement time.
     * @return gain Measurement
     */
    public byte getMeasurement() {
        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL1);
            value &= MEAS_MASK;
            return value;
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Set Measurement time.
     * @param meas gain Measurement mode
     */
    public boolean setMeasurement(@MeasurementTime int meas) {
        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL1);
            value &= ~(MEAS_MASK);
            value |= meas;
            mDevice.writeRegByte(REG_MODE_CONTROL1, value);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Read Measurement Enable
     * @return Measurement enable
     */
    public boolean getMeasurementEnable() {

        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL1);
            if((value & RGB_EN_MASK) == RGB_EN_MASK) {
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
     * Set measurement enable
     * @param enable Measurement enable
     */
    public void setMeasurementEnable(boolean enable) {
        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL2);
            value &= ~(RGB_EN_MASK);
            value |= (enable ? 1 : 0) << RGB_EN_SHIFT;
            mDevice.writeRegByte(REG_MODE_CONTROL2, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read VALID Register
     * @return
     */
    public boolean readValid() {
        try {
            byte value = mDevice.readRegByte(REG_MODE_CONTROL2);

            if((value & VALID_MASK) == VALID_MASK) {
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
     * Read red data
     * @return
     */
    public int readRed() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        Short red = mDevice.readRegWord(REG_RED_DATA);
        return Short.toUnsignedInt(red);
    }

    /**
     * Read green data
     * @return
     */
    public int readGreen() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        Short green = mDevice.readRegWord(REG_GREEN_DATA);
        return Short.toUnsignedInt(green);
    }


    /**
     * Read blue data
     * @return
     */
    public int readBlue() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        Short blue = mDevice.readRegWord(REG_BLUE_DATA);
        return Short.toUnsignedInt(blue);
    }

    /**
     * Read ir data
     * @return
     */
    public int readIR() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        Short ir = mDevice.readRegWord(REG_IR_DATA);
        return Short.toUnsignedInt(ir);
    }

    /**
     * Read green2 data
     * @return
     */
    public int readGreen2() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        Short green2 = mDevice.readRegWord(REG_GREEN2_DATA);
        return Short.toUnsignedInt(green2);
    }
}
