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

public class ISL29034Test {

    /** ISL29034 Device ID(xx101xxx). */
    private final byte DEVICE_ID = 0x28;

    /** Register Addresses. */
    private final byte REG_CMD1 = 0x00;
    private final byte REG_CMD2 = 0x01;
    private final byte REG_DATA_L = 0x02;
    private final byte REG_DATA_H = 0x03;
    private final byte REG_ID = 0x0F;

    /** Regiser address. */

    @Mock
    private I2cDevice mI2c;

    @Rule
    public MockitoRule mMokitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();


    @Test
    public void whoAmI() {
        try {
            ISL29034 driver = new ISL29034(mI2c);
            Mockito.when(mI2c.readRegByte(REG_ID)).thenReturn((byte)DEVICE_ID);
            driver.whoAmI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetRange() {
        try {
            ISL29034 driver = new ISL29034(mI2c);
            driver.setRange(ISL29034.RANGE_3);
            Mockito.when(mI2c.readRegByte(REG_CMD2)).thenReturn((byte) 0b00000011);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetResolution() {
        try {
            ISL29034 driver = new ISL29034(mI2c);
            driver.setResolution(ISL29034.RES_16);
            Mockito.when(mI2c.readRegByte(REG_CMD2)).thenReturn((byte)0b00001100);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetOperation() {
        try {
            ISL29034 driver = new ISL29034(mI2c);
            driver.setOperation(ISL29034.MODE_ALS_CONTINUS);
            Mockito.when(mI2c.readRegByte(REG_CMD1)).thenReturn((byte)0b10100000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}