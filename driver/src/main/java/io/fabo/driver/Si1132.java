package io.fabo.driver;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

public class Si1132 implements AutoCloseable {
    private static final String TAG = Adx345.class.getSimpleName();

    /**
     * I2C slave address of the Si1132.
     */
    private final byte I2C_ADDRESS = (byte)0x60;

    /** Who am I. */
    private final byte SI1132_WHO_AM_I_REG = (byte)0x00;

    /** Device check register. */
    private final byte SI1132_DEVICE_ID = (byte)0x32;

    /** HW_KEY register. */
    private final byte SI1132_HW_KEY_REG = (byte)0x07;

    /** MEASRATE0 register. */
    private final byte SI1132_MEASRATE0_REG = (byte)0x08;

    /** UCOEF0 register. */
    private final byte SI1132_UCOEF0_REG = (byte)0x13;

    /** UCOEF1 register. */
    private final byte SI1132_UCOEF1_REG = (byte)0x14;

    /** UCOEF2 register. */
    private final byte SI1132_UCOEF2_REG = (byte)0x15;

    /** UCOEF3 register. */
    private final byte SI1132_UCOEF3_REG = (byte)0x16;

    /** PARAM_WR register. */
    private final byte SI1132_PARAM_WR_REG = (byte)0x17;

    /** Command register. */
    private final byte SI1132_COMMAND_REG = (byte)0x18;

    /** Visible Data register. */
    private final byte SI1132_VISIBLE_DATA_REG = (byte)0x22;

    /** IR Data register. */
    private final byte SI1132_IR_DATA_REG = (byte)0x24;

    /** Auxiliary Data register. */
    private final byte SI1132_AUX_DATA_REG = (byte)0x2C;

    /** Chip list Parameter RAM Offset. */
    private final byte SI1132_CHIPLIST_PARAM_OFFSET = (byte)0x01;

    /** ALS Encoding Parameter RAM Offset. */
    private final byte SI1132_ALS_ENCODING_PARAM_OFFSET = (byte)0x06;

    /** ALS VIS ADC Counter register. */
    private final byte SI1132_ALS_VIS_ADC_COUNTER_PARAM_OFFSET = (byte)0x10;

    /** ALS VIS ADC Gain Parameter RAM Offset */
    private final byte SI1132_ALS_VIS_ADC_GAIN_PARAM_OFFSET = (byte)0x11;

    /** ALS VIS ADC Misc Parameter RAM Offset. */
    private final byte SI1132_ALS_VIS_ADC_MISC_PARAM_OFFSET = (byte)0x12;

    /** ALS IR ADC Counter  Parameter RAM Offset. */
    private final byte SI1132_ALS_IR_ADC_COUNTER_PARAM_OFFSET = (byte)0x1D;

    /** ALS IR ADC Gain  Parameter RAM Offset. */
    private final byte SI1132_ALS_IR_ADC_GAIN_PARAM_OFFSET = (byte)0x1E;

    /** ALS IR ADC Misc  Parameter RAM Offset. */
    private final byte SI1132_ALS_IR_ADC_MISC_PARAM_OFFSET = (byte)0x1F;

    /** ALS IR Adcmux  Parameter RAM Offset. */
    private final byte SI1132_ALS_IR_ADCMUX_PARAM_OFFSET = (byte)0x0E;

    /** Auxiliary Adcmux  Parameter RAM Offset. */
    private final byte SI1132_AUX_ADCMUX_PARAM_OFFSET = (byte)0x0F;

    /** Enables UV Index register. */
    private final byte SI1132_EN_UV = (byte)0b10000000;

    /** Enables Auxiliary Channel register. */
    private final byte SI1132_EN_AUX = (byte)0b01000000;

    /** Enables ALS IR Channel register. */
    private final byte SI1132_EN_ALS_IR  = (byte)0b00100000;

    /** Enables ALS Visible Channel register. */
    private final byte SI1132_EN_ALS_VIS = (byte)0b00010000;

    /** ALS　VIS ALIGN register. */
    private final byte SI1132_ALS_VIS_ALIGN = (byte)0b00010000;

    /** ALS　IR ALIGN register. */
    private final byte SI1132_ALS_IR_ALIGN  = (byte)0b00100000;

    /** ADC Clock 1 : 50 ns times. */
    private final byte SI1132_1_ADC_CLOCK = (byte)0b00000000;

    /** ADC Clock 7 : 350 ns times. */
    private final byte  SI1132_7_ADC_CLOCK = (byte)0b00010000;

    /** ADC Clock 15 : 750 ns time. */
    private final byte  SI1132_15_ADC_CLOCK = (byte)0b00100000;

    /** ADC Clock 31 : 1.15 us times. */
    private final byte  SI1132_31_ADC_CLOCK = (byte)0b00110000;

    /** ADC Clock 63 : 3.15 us times. */
    private final byte  SI1132_63_ADC_CLOCK = (byte)0b01000000;

    /** ADC Clock 127 : 6.35 us times. */
    private final byte SI1132_127_ADC_CLOCK = (byte)0b10100000;

    /** ADC Clock 255 : 12.75 us times. */
    private final byte SI1132_255_ADC_CLOCK = (byte)0b01100000;

    /** ADC Clock 511 : 25.55 us times. */
    private final byte  SI1132_511_ADC_CLOCK = (byte)0b01110000;

    /** Divided ADC Clock 1. */
    private final byte SI1132_1_DIVIDED_ADC_CLOCK = (byte) 0b0000000;
    /** Divided ADC Clock 16. */
    private final byte SI1132_16_DEVIDED_ADC_CLOCK = (byte)0b0000100;
    /** Divided ADC Clock 64. */
    private final byte SI1132_64_DEVIDED_ADC_CLOCK = (byte)0b0000110;

    /** Normal single range. */
    private final byte SI1132_NORMAL_SIGNAL_RANGE = (byte)0b00000000;
    /** High single range. */
    private final byte SI1132_HIGH_SIGNAL_RANGE = (byte) 0b00100000;

    /** ALS IR Adcmux SMALLIR. */
    private final byte SI1132_ALS_IR_ADCMUX_SMALLIR = (byte)0x00;

    /** ALS IR Adcmux Temperature. */
    private final byte SI1132_AUX_ADCMUX_TEMPERATURE = (byte)0x65;
    /** Auxiliary ADCMUX VDD. */
    private final byte SI1132_AUX_ADCMUX_VDD = (byte)0x75;

    /** Command ALS Auto. */
    private final byte SI1132_COMMAND_ALS_AUTO = (byte)0x0E;
    /** Command Reset. */
    private final byte SI1132_COMMAND_RESET = (byte)0x01;
    /** HW_KEY Default Value. */
    private final byte SI1132_HW_KEY_DEFAULT = (byte)0x17;

    /** Normal single range. */
    private final byte SI1132_PARAM_QUERY = (byte)0b10000000;
    /** High single range. */
    private final byte SI1132_PARAM_SET = (byte)0b10100000;

    private I2cDevice mDevice;

    private byte mRange;
    private byte mResolution;

    /**
     * Create a new Si1132 driver connected to the given I2C bus.
     * @param bus
     * @throws IOException
     */
    public Si1132(String bus) throws IOException {
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
     * Create a new Si1132 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ Si1132(I2cDevice device) throws IOException {
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
            byte value = mDevice.readRegByte(SI1132_WHO_AM_I_REG);
            if((value & 0xff) == SI1132_DEVICE_ID) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Reset
     */
    public void reset()
    {
        try {
            mDevice.writeRegByte(SI1132_COMMAND_REG, SI1132_COMMAND_RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure.
     */
    public void configuration() {
        try {
            mDevice.writeRegByte(SI1132_UCOEF0_REG, (byte)0x7B);
            mDevice.writeRegByte(SI1132_UCOEF1_REG, (byte)0x6B);
            mDevice.writeRegByte(SI1132_UCOEF2_REG, (byte)0x01);
            mDevice.writeRegByte(SI1132_UCOEF3_REG, (byte)0x00);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // SET PARAM_WR(Chiplist)
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, (byte)(SI1132_EN_UV|SI1132_EN_AUX|SI1132_EN_ALS_IR|SI1132_EN_ALS_VIS));
            // COMMAND(Set Chiplist)
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_CHIPLIST_PARAM_OFFSET));
            // Select HW_KEY register
            mDevice.writeRegByte(SI1132_HW_KEY_REG, SI1132_HW_KEY_DEFAULT);

            // Rate setting. ToDo
            mDevice.writeRegByte(SI1132_MEASRATE0_REG, (byte)0xff);

            // SET PARAM_WR(ALS_ENCODING)
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, (byte)(SI1132_ALS_VIS_ALIGN | SI1132_ALS_IR_ALIGN));
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_ALS_ENCODING_PARAM_OFFSET));

            /* Visible */
            // SET ALS_VIS_ADC_COUNTER
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, SI1132_511_ADC_CLOCK);
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_ALS_VIS_ADC_COUNTER_PARAM_OFFSET));

            // SET ALS_VIS_ADC_GAIN
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, SI1132_1_DIVIDED_ADC_CLOCK);
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_ALS_VIS_ADC_GAIN_PARAM_OFFSET));

            // SET ALS_VIS_ADC_MISC
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, SI1132_HIGH_SIGNAL_RANGE);
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_ALS_VIS_ADC_MISC_PARAM_OFFSET));

            /* IR */
            // SET ALS_IR_ADC_COUNTER
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, SI1132_511_ADC_CLOCK);
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_ALS_IR_ADC_COUNTER_PARAM_OFFSET));

            // SET ALS_IR_ADC_GAIN
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, SI1132_1_DIVIDED_ADC_CLOCK);
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_ALS_IR_ADC_GAIN_PARAM_OFFSET));

            // SET ALS_IR_ADC_MISC
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, SI1132_HIGH_SIGNAL_RANGE);
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_ALS_IR_ADC_MISC_PARAM_OFFSET));

            // SET ALS_IR_ADCMUX
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, SI1132_ALS_IR_ADCMUX_SMALLIR);
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_ALS_IR_ADCMUX_PARAM_OFFSET));

            // SET AUX_ADCMUX
            mDevice.writeRegByte(SI1132_PARAM_WR_REG, SI1132_AUX_ADCMUX_TEMPERATURE);
            mDevice.writeRegByte(SI1132_COMMAND_REG, (byte)(SI1132_PARAM_SET|SI1132_AUX_ADCMUX_PARAM_OFFSET));

            // COMMAND
            mDevice.writeRegByte(SI1132_COMMAND_REG, SI1132_COMMAND_ALS_AUTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read UV Index
     * @return
     */
    public int readUV() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }

        int length = 2;
        byte uv_buff[] = new byte[length];

        mDevice.readRegBuffer(SI1132_AUX_DATA_REG, uv_buff, uv_buff.length);
        int uv_index = (uv_buff[1] & 0xff)<<8 | uv_buff[0] & 0xff;

        return uv_index;
    }

    /**
     * Read Infrared
     * @return
     */
    public int readIR() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }

        int length = 2;
        byte ir_buff[] = new byte[length];

        mDevice.readRegBuffer(SI1132_IR_DATA_REG, ir_buff, ir_buff.length);
        int ir = (ir_buff[1] & 0xff)<<8 | ir_buff[0] & 0xff;

        return ir;
    }

    /**
     * Read Visible
     * @return
     */
    public int readVisible() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }

        int length = 2;
        byte visible_buff[] = new byte[length];

        mDevice.readRegBuffer(SI1132_VISIBLE_DATA_REG, visible_buff, visible_buff.length);
        int visible = visible_buff[1] & 0xff<<8 | visible_buff[0] & 0xff;

        return visible;
    }
}
