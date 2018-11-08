package io.fabo.driver;


import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class S11059 implements AutoCloseable {
    private static final String TAG = S11059.class.getSimpleName();

    /**
     * I2C slave address of the S11059.
     */
    public final int I2C_ADDRESS = 0x2A;

    /**
     * Control register.
     */
    private byte S11059_CONTROL = 0x00;
    /**
     * Data Format Control.
     */
    private byte S11059_DATA_RED_H = 0x03;
    private byte S11059_CTRL_GAIN = 0x08;
    private byte S11059_CTRL_MODE = 0x04;
    private byte S11059_CTRL_TIME_224M = 0x2;

    private I2cDevice mDevice;

    /**
     * Create a new S11059 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public S11059(String bus) throws IOException {
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
     * Create a new S11059 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ S11059(I2cDevice device) throws IOException {
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


    public void setControl() {
        try {
            Log.i("TEST", "setControl");

            // Setting
            byte value = (byte) (mDevice.readRegByte(S11059_CONTROL) & 0xff);
            Log.i("TEST", "value=" + value);
            value |= S11059_CTRL_GAIN;
            value &= ~(S11059_CTRL_MODE);
            value &= 0xFC;
            value |= S11059_CTRL_TIME_224M;
            value &= 0x3F; // RESET off,SLEEP off
            mDevice.writeRegByte(S11059_CONTROL, value);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Read an color sample.
     * @return RGBI.
     * @throws IOException
     * @throws IllegalStateException
     */
    public float[] readSample() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        int length = 8;
        byte color_buff[] = new byte[length];
        mDevice.readRegBuffer(S11059_DATA_RED_H, color_buff, color_buff.length);
        int r = (((int)color_buff[0] & 0xff) << 8) | color_buff[1] & 0xff;
        int g = (((int)color_buff[2] & 0xff) << 8) | color_buff[3] & 0xff;
        int b = (((int)color_buff[4] & 0xff) << 8) | color_buff[5] & 0xff;
        int i = (((int)color_buff[6] & 0xff) << 8) | color_buff[7] & 0xff;
        return new float[]{
                r, g, b, i
        };
    }



}