package io.fabo.driver;

import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

import static java.lang.Math.pow;

public class MPL115 implements AutoCloseable {
    private static final String TAG = MPL115.class.getSimpleName();

    /**
     * I2C slave address of the MPL115.
     */
    public static final int I2C_ADDRESS = 0x60;

    // Register.
    /** 10-bit Pressure ADC output value MSB. */
    private final int REG_PADC_MSB = 0x00;
    /** 10-bit Pressure ADC output value LSB. */
    private final int REG_PADC_LSB = 0x01;
    /** 10-bit Temperature ADC output value MSB. */
    private final int REG_TADC_MSB = 0x02;
    /** 10-bit Temperature ADC output value LSB. */
    private final int REG_TACD_LSB = 0x03;
    /** a0 coefficient MSB. */
    private final int REG_A0_MSB = 0x04;
    /** a0 coefficient LSB. */
    private final int REG_A0_LSB = 0x05;
    /** b1 coefficient MSB. */
    private final int REG_B1_MSB = 0x06;
    /** b1 coefficient LSB. */
    private final int REG_B1_LSB = 0x07;
    /** b2 coefficient MSB. */
    private final int REG_B2_MSB = 0x08;
    /** b2 coefficient LSB. */
    private final int REG_B2_LSB = 0x09;
    /** b2 coefficient MSB. */
    private final int REG_C12_MSB = 0x0A;
    /** b2 coefficient LSB. */
    private final int REG_C12_LSB = 0x0B;
    /** Start Pressure and Temperature Conversion. */
    private final int REG_CONVERT = 0x12;

    private static float mA0;
    private static float mB1;
    private static float mB2;
    private static float mC12;


    private I2cDevice mDevice;

    /**
     * Create a new MPL115 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public MPL115(String bus) throws IOException {
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
     * Create a new MPL115 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ MPL115(I2cDevice device) throws IOException {
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
     @brief Read Coefficient Data
     */
    public void readCoef() {
        try {
            Log.i(TAG, "readCoef()");
            byte data_buff[] = new byte[8];
            mDevice.readRegBuffer(REG_A0_MSB, data_buff, data_buff.length);
            mA0  = ((float) ((data_buff[0] << 8) + data_buff[1]) / ((long)1 << 3));
            mB1  = ((float) ((data_buff[2] << 8) + data_buff[3]) / ((long)1 << 13));
            mB2  = ((float) ((data_buff[4] << 8) + data_buff[5]) / ((long)1 << 14));
            mC12 = ((float) ((data_buff[6] << 8) + data_buff[7]) / ((long)1 << 24));
            Log.i(TAG, "mA0=" + mA0);
            Log.i(TAG, "mB1=" + mB1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read an accelerometer sample.
     * @return hpa, temp.
     * @throws IOException
     * @throws IllegalStateException
     */
    public float[] readData() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        mDevice.writeRegByte(REG_CONVERT, (byte)0x01);

        byte data_buff[] = new byte[4];
        mDevice.readRegBuffer(REG_PADC_MSB, data_buff, data_buff.length);

        int padc = (((data_buff[0] & 0xff) << 8) | (data_buff[1] & 0xff)) >> 6;
        int tadc = (((data_buff[2] & 0xff) << 8) | (data_buff[3] & 0xff)) >> 6;

        float pcomp = mA0 + (mB1 + mC12 * tadc) * padc + mB2 * tadc;
        float hpa = (float) (pcomp * ((1150.0 - 500.0) / 1023.0) + 500.0);
        float temp = (float) (25.0 - ((float) tadc - 512.0) / 5.35);

        return new float[]{hpa, temp};
    }

    /**
     *  Get hpa from altitude.
     * @param hpa
     * @param altitude
     * @return
     */
    public static float hpaFromAltitude(float hpa, float altitude) {
        return (float)(hpa / pow(1.0 - (altitude/44330.0), 5.255));
    }

}
