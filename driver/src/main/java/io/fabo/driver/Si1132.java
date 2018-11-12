package io.fabo.driver;

import android.support.annotation.IntDef;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Si1132 implements AutoCloseable {
    private static final String TAG = Adx345.class.getSimpleName();

    /**
     * I2C slave address of the Si1132.
     */
    private final byte I2C_ADDRESS = (byte)0x60;

    // Register
    /** Who am I. */
    private final int REG_WHO_AM_I = 0x00;
    /** HW_KEY register. */
    private final int REG_HW_KEY = 0x07;
    /** MEASRATE0 register. */
    private final int REG_MEASRATE0 = 0x08;
    /** MEASRATE1 register. */
    private final int REG_MEASRATE1 = 0x09;
    /** UCOEF register. */
    private final int REG_UCOEF0 = 0x13;
    /** UCOEF1 register. */
    private final int REG_UCOEF1 = 0x14;
    /** UCOEF2 register. */
    private final int REG_UCOEF2 = 0x15;
    /** UCOEF3 register. */
    private final int REG_UCOEF3 = 0x16;
    /** PARAM_WR register. */
    private final int REG_PARAM_WR = 0x17;
    /** Command register. */
    private final int REG_COMMAND = 0x18;

    /** Visible Data register. */
    private final byte REG_VISIBLE_DATA = (byte)0x22;

    /** IR Data register. */
    private final byte REG_IR_DATA = (byte)0x24;

    /** Auxiliary Data register. */
    private final byte REG_AUX_DATA = (byte)0x2C;

    // Offset
    /** ALS Encoding Parameter RAM Offset. */
    private final byte OFFSET_ALS_ENCODING_PARAM = (byte)0x06;
    /** ALS VIS ADC Counter register. */
    private final byte OFFSET_ALS_VIS_ADC_COUNTER_PARAM = (byte)0x10;
    /** ALS VIS ADC Gain Parameter RAM Offset */
    private final byte OFFSET_ALS_VIS_ADC_GAIN_PARAM = (byte)0x11;
    /** ALS VIS ADC Misc Parameter RAM Offset. */
    private final byte OFFSET_ALS_VIS_ADC_MISC_PARAM = (byte)0x12;
    /** ALS IR ADC Counter  Parameter RAM Offset. */
    private final byte OFFSET_ALS_IR_ADC_COUNTER_PARAM = (byte)0x1D;
    /** ALS IR ADC Gain  Parameter RAM Offset. */
    private final byte OFFSET_ALS_IR_ADC_GAIN_PARAM = (byte)0x1E;
    /** ALS IR ADC Misc  Parameter RAM Offset. */
    private final byte OFFSET_ALS_IR_ADC_MISC_PARAM = (byte)0x1F;
    /** ALS IR Adcmux  Parameter RAM Offset. */
    private final byte OFFSET_ALS_IR_ADCMUX_PARAM = (byte)0x0E;
    /** Auxiliary Adcmux  Parameter RAM Offset. */
    private final byte OFFSET_AUX_ADCMUX_PARAM = (byte)0x0F;

    /** Enables UV Index register. */
    private final int EN_UV = 0b10000000;
    /** Enables Auxiliary Channel register. */
    private final int EN_AUX = 0b01000000;
    /** Enables ALS IR Channel register. */
    private final int EN_ALS_IR  = 0b00100000;
    /** Enables ALS Visible Channel register. */
    private final int EN_ALS_VIS = 0b00010000;
    /** ALS　VIS ALIGN register. */
    private final int ALS_VIS_ALIGN = 0b00010000;
    /** ALS　IR ALIGN register. */
    private final int ALS_IR_ALIGN = 0b00100000;

    /**
     * adc clock.
     */
    /** ADC Clock 1 : 50 ns times. */
    public final static int ADC_CLOCK_1 = 0b00000000;
    /** ADC Clock 7 : 350 ns times. */
    public final static int ADC_CLOCK_7 = 0b00010000;
    /** ADC Clock 15 : 750 ns time. */
    public final static int ADC_CLOCK_15 = 0b00100000;
    /** ADC Clock 31 : 1.15 us times. */
    public final static int ADC_CLOCK_31 = 0b00110000;
    /** ADC Clock 63 : 3.15 us times. */
    public final static int ADC_CLOCK_63 = 0b01000000;
    /** ADC Clock 127 : 6.35 us times. */
    public final static int ADC_CLOCK_127 = 0b10100000;
    /** ADC Clock 255 : 12.75 us times. */
    public final static int ADC_CLOCK_255 = 0b01100000;
    /** ADC Clock 511 : 25.55 us times. */
    public final static int ADC_CLOCK_511 = 0b01110000;
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ADC_CLOCK_1,ADC_CLOCK_7,ADC_CLOCK_15,ADC_CLOCK_31,ADC_CLOCK_63,ADC_CLOCK_127,ADC_CLOCK_255,ADC_CLOCK_511})
    public @interface AdcClock {}

    /**
     * Divided adc clock.
     */
    /** Divided ADC Clock 1. */
    private final static int DIVIDED_ADC_CLOCK_1 = 0b0000000;
    /** Divided ADC Clock 16. */
    private final static int DEVIDED_ADC_CLOCK_16 = 0b0000100;
    /** Divided ADC Clock 64. */
    private final static int DEVIDED_ADC_CLOCK_64 = 0b0000110;
    @IntDef({DIVIDED_ADC_CLOCK_1, DEVIDED_ADC_CLOCK_16, DEVIDED_ADC_CLOCK_64})
    public @interface DividedAdcClock {}

    /**
     * Single range.
     */
    /** Normal single range. */
    private final static int SIGNAL_RANGE_NORMAL = 0b00000000;
    /** High single range. */
    private final static int SIGNAL_RANGE_HIGH = 0b00100000;
    @IntDef({SIGNAL_RANGE_NORMAL, SIGNAL_RANGE_HIGH})
    public @interface SignalRange {}

    /** ALS IR Adcmux SMALLIR. */
    private final int SI1132_ALS_IR_ADCMUX_SMALLIR = 0x00;

    /**
     * Aux Meas
     */
    /** ALS IR Adcmux Temperature. */
    private final static int AUX_ADCMUX_TEMPERATURE = 0x65;
    /** Auxiliary ADCMUX VDD. */
    private final static int AUX_ADCMUX_VDD = 0x75;
    @IntDef({AUX_ADCMUX_TEMPERATURE, AUX_ADCMUX_VDD})
    public @interface AuxMeas {}

    /** Command ALS Auto. */
    private final byte COMMAND_ALS_AUTO = 0b00001110;
    /** Command ALS Pause. */
    private final byte COMMAND_ALS_PAUSE = 0b00001010;
    /** Command Reset. */
    private final byte COMMAND_RESET = 0x01;
    /** HW_KEY Default Value. */
    private final byte HW_KEY_DEFAULT = 0x17;

    /** Normal single range. */
    private final int PARAM_QUERY = 0b10000000;
    /** High single range. */
    private final int PARAM_SET = 0b10100000;
    /** Chip list Parameter RAM Offset. */
    private final int CHIPLIST_PARAM_OFFSET = 0x01;

    /** Device check register. */
    private final int DEVICE_ID = 0x32;

    private I2cDevice mDevice;

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
            byte value = mDevice.readRegByte(REG_WHO_AM_I);
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
    public void reset()
    {
        try {
            mDevice.writeRegByte(REG_COMMAND, COMMAND_RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure UCONF.
     */
    public void configureUCONF() {
        try {
            mDevice.writeRegByte(REG_UCOEF0, (byte)0x7B);
            mDevice.writeRegByte(REG_UCOEF1, (byte)0x6B);
            mDevice.writeRegByte(REG_UCOEF2, (byte)0x01);
            mDevice.writeRegByte(REG_UCOEF3, (byte)0x00);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set chiplist.
     */
    public void setChiplist() {
        try {
            // SET PARAM_WR(Chiplist)
            mDevice.writeRegByte(REG_PARAM_WR, (byte)(EN_UV|EN_AUX|EN_ALS_IR|EN_ALS_VIS));
            // COMMAND(Set Chiplist)
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET|CHIPLIST_PARAM_OFFSET));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initalize.
     */
    public void initialize() {
        try {
            // Select HW_KEY register
            mDevice.writeRegByte(REG_HW_KEY, HW_KEY_DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set measurement rate.
     * @param rate 0  turn off autonomous mode, non-zero autonomous mode(multiple 31.25 µs).
     */
    public void setMeasRate(int rate) {
        try {
            // Rate setting. ToDo
            mDevice.writeRegByte(REG_MEASRATE0, (byte)(rate & 0xff));
            mDevice.writeRegByte(REG_MEASRATE1, (byte)((rate & 0xff00) >> 8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set visual adv counter.
     */
    public void setVisialAdcCounter(@AdcClock int adcClock) {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)adcClock);
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_ALS_VIS_ADC_COUNTER_PARAM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set visual adc gain counter.
     */
    public void setVisialAdcGain(@DividedAdcClock int dividedAdcClock) {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)dividedAdcClock);
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_ALS_VIS_ADC_GAIN_PARAM));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set visual adc misc.
     */
    public void setVisialAdcMisc(@SignalRange int singleRange) {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)singleRange);
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_ALS_VIS_ADC_MISC_PARAM));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set IR adv counter.
     */
    public void setIRAdcCounter(@AdcClock int adcClock) {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)adcClock);
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_ALS_IR_ADC_COUNTER_PARAM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set divided IR adc gain counter.
     */
    public void setIRAdcGain(@DividedAdcClock int dividedAdcClock) {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)dividedAdcClock);
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_ALS_IR_ADC_GAIN_PARAM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set IR adc misc.
     */
    public void setIRAdcMisc(@SignalRange int singleRange) {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)singleRange);
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_ALS_IR_ADC_MISC_PARAM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Selects ADC Input for ALS_IR Measurement.
     */
    public void setIRAdmux() {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)SI1132_ALS_IR_ADCMUX_SMALLIR);
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_ALS_IR_ADCMUX_PARAM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Selects input for AUX Measurement.
     */
    public void setUVAdmux(@AuxMeas int auxMeas) {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)SI1132_ALS_IR_ADCMUX_SMALLIR);
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_AUX_ADCMUX_PARAM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mailbox register for passing parameters from the host to the sequencer.
     */
    public void setMailboxRegister() {
        try {
            mDevice.writeRegByte(REG_PARAM_WR, (byte)(ALS_VIS_ALIGN | ALS_IR_ALIGN));
            mDevice.writeRegByte(REG_COMMAND, (byte)(PARAM_SET | OFFSET_ALS_ENCODING_PARAM));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pause
     */
    public void pause() {
        try {
            // Command of automate mode.
            mDevice.writeRegByte(REG_COMMAND, COMMAND_ALS_PAUSE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start
     */
    public void start() {
        try {
            // Command of automate mode.
            mDevice.writeRegByte(REG_COMMAND, COMMAND_ALS_AUTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configure.
     */
    public void configuration() {
        configureUCONF();
        setChiplist();
        initialize();
        setMeasRate(1);
        setMailboxRegister();

        // Visual.
        setVisialAdcCounter(Si1132.ADC_CLOCK_511);
        setVisialAdcGain(Si1132.DIVIDED_ADC_CLOCK_1);
        setVisialAdcMisc(Si1132.SIGNAL_RANGE_HIGH);

        // IR
        setIRAdcCounter(Si1132.ADC_CLOCK_511);
        setIRAdcGain(Si1132.DIVIDED_ADC_CLOCK_1);
        setIRAdcMisc(Si1132.SIGNAL_RANGE_HIGH);
        setIRAdmux();

        // UV
        setUVAdmux(Si1132.AUX_ADCMUX_TEMPERATURE);
    }

    /**
     * Read UV Index
     * @return
     */
    public int readUV() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }
        Short uv = mDevice.readRegWord(REG_AUX_DATA);
        return Short.toUnsignedInt(uv);
    }

    /**
     * Read Infrared
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
     * Read Visible
     * @return
     */
    public int readVisible() throws IOException, IllegalStateException {
        if (mDevice == null) {
            throw new IllegalStateException("device not connected");
        }

        Short visible = mDevice.readRegWord(REG_VISIBLE_DATA);
        return Short.toUnsignedInt(visible);
    }
}
