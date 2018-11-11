package io.fabo.driver;


import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;
import static org.junit.Assert.*;
import static org.mockito.Matchers.byteThat;
import static org.mockito.Matchers.eq;

public class BH1749Test {

    /** Regiser address. */
    private final int REG_SYSTEM_CONTROL = 0x40;
    private final int REG_MODE_CONTROL1  = 0x41;
    private final int REG_MODE_CONTROL2  = 0x42;

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
        // 0x00 0x0d
        // 0x92 0xe0
        try {
            BH1749 driver = new BH1749(mI2c);
            Mockito.when(mI2c.readRegByte(0x00)).thenReturn((byte) 0x0d);
            Mockito.when(mI2c.readRegByte(0x92)).thenReturn((byte) 0xe0);
            driver.whoAmI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIRGainMode() {
        try {
            BH1749 driver = new BH1749(mI2c);

            // IR_GAIN_X1
            driver.setIRGain(BH1749.IR_GAIN_X1);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL1, (byte)0b00100000);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL1)).thenReturn((byte)0b00100000);
            assertEquals(driver.getIRGain(), BH1749.IR_GAIN_X1);

            // IR_GAIN_X32
            driver.setIRGain(BH1749.IR_GAIN_X32);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL1, (byte)0b01100000);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL1)).thenReturn((byte)0b01100000);
            assertEquals(driver.getIRGain(), BH1749.IR_GAIN_X32);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRGBGainMode() {
        try {
            BH1749 driver = new BH1749(mI2c);

            // RGB_GAIN_X1
            driver.setRGBGain(BH1749.RGB_GAIN_X1);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL1, (byte)0b00001000);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL1)).thenReturn((byte)0b00001000);
            assertEquals(driver.getRGBGain(), BH1749.RGB_GAIN_X1);

            // RGB_GAIN_X32
            driver.setRGBGain(BH1749.RGB_GAIN_X32);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL1, (byte)0b00011000);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL1)).thenReturn((byte)0b00011000);
            assertEquals(driver.getRGBGain(), BH1749.RGB_GAIN_X32);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMeasurement() {
        try {
            BH1749 driver = new BH1749(mI2c);

            // MEAS_35MS
            driver.setMeasurement(BH1749.MEAS_35MS);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL1, (byte)0b00000101);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL1)).thenReturn((byte)0b00000101);
            assertEquals(driver.getMeasurement(), BH1749.MEAS_35MS);

            // MEAS_120MS
            driver.setMeasurement(BH1749.MEAS_120MS);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL1, (byte)0b00000010);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL1)).thenReturn((byte)0b00000010);
            assertEquals(driver.getMeasurement(), BH1749.MEAS_120MS);

            // MEAS_240MS
            driver.setMeasurement(BH1749.MEAS_240MS);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL1, (byte)0b00000011);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL1)).thenReturn((byte)0b00000011);
            assertEquals(driver.getMeasurement(), BH1749.MEAS_240MS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testMeasurementEnable() {
        try {
            BH1749 driver = new BH1749(mI2c);

            // Enable
            driver.setMeasurementEnable(true);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL2, (byte)0b00010000);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL2)).thenReturn((byte)0b00010000);
            assertEquals(driver.getMeasurementEnable(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMeasurementDisable() {
        try {
            BH1749 driver = new BH1749(mI2c);

            // Disable
            driver.setMeasurementEnable(false);
            Mockito.verify(mI2c).writeRegByte(REG_MODE_CONTROL2, (byte)0b00000000);
            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL2)).thenReturn((byte)0b00000000);
            assertEquals(driver.getMeasurementEnable(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testValid() {
        try {
            BH1749 driver = new BH1749(mI2c);

            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL2)).thenReturn((byte)0b10000000);
            assertEquals(driver.readValid(), true);

            Mockito.when(mI2c.readRegByte(REG_MODE_CONTROL2)).thenReturn((byte)0b00000000);
            assertEquals(driver.readValid(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readRed() {

    }

    @Test
    public void readGreen() {
    }

    @Test
    public void readBlue() {
    }

    @Test
    public void readIR() {
    }

    @Test
    public void readGreen2() {
    }
}