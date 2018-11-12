package io.fabo.driver;


import com.google.android.things.pio.I2cDevice;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class Si1132Test {

    /** Register address. */
    private final int REG_COMMAND = 0x18;
    private final int REG_WHO_AM_I = 0x00;
    private final int REG_HW_KEY = 0x07;
    private final int REG_UCOEF0 = 0x13;
    private final int REG_UCOEF1 = 0x14;
    private final int REG_UCOEF2 = 0x15;
    private final int REG_UCOEF3 = 0x16;
    private final int REG_PARAM_WR = 0x17;

    /** Enables flag */
    private final int EN_UV = 0b10000000;
    private final int EN_AUX = 0b01000000;
    private final int EN_ALS_IR  = 0b00100000;
    private final int EN_ALS_VIS = 0b00010000;

    /** Command Reset. */
    private final byte COMMAND_RESET = 0x01;
    /** Device check register. */
    private final byte DEVICE_ID = (byte)0x32;

    /** High single range. */
    private final byte PARAM_SET = (byte)0b10100000;
    /** Chip list Parameter RAM Offset. */
    private final byte CHIPLIST_PARAM_OFFSET = (byte)0x01;

    /** HW Key default. */
    private final byte HW_KEY_DEFAULT = (byte)0x17;

    @Mock
    private I2cDevice mI2c;

    @Rule
    public MockitoRule mMokitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();


    @Test
    public void whoAmI() {
        // When sensor boot,
        // reg  value
        // 0x00 0x32
        try {
            Si1132 driver = new Si1132(mI2c);
            Mockito.when(mI2c.readRegByte(REG_WHO_AM_I)).thenReturn((byte) DEVICE_ID);
            driver.whoAmI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReset()
    {
        try {
            Si1132 driver = new Si1132(mI2c);
            driver.reset();
            Mockito.verify(mI2c).writeRegByte(REG_COMMAND, COMMAND_RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConfigureUCONF()
    {
        try {
            Si1132 driver = new Si1132(mI2c);
            driver.configureUCONF();
            Mockito.verify(mI2c).writeRegByte(REG_UCOEF0, (byte)0x7B);
            Mockito.verify(mI2c).writeRegByte(REG_UCOEF1, (byte)0x6B);
            Mockito.verify(mI2c).writeRegByte(REG_UCOEF2, (byte)0x01);
            Mockito.verify(mI2c).writeRegByte(REG_UCOEF3, (byte)0x00);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetChiplist() {
        try {
            Si1132 driver = new Si1132(mI2c);
            driver.setChiplist();
            Mockito.verify(mI2c).writeRegByte(REG_PARAM_WR, (byte)(EN_UV|EN_AUX|EN_ALS_IR|EN_ALS_VIS));
            Mockito.verify(mI2c).writeRegByte(REG_COMMAND, (byte)(PARAM_SET|CHIPLIST_PARAM_OFFSET));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void initialize() {
        try {
            Si1132 driver = new Si1132(mI2c);
            driver.initialize();
            Mockito.verify(mI2c).writeRegByte(REG_HW_KEY, HW_KEY_DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}