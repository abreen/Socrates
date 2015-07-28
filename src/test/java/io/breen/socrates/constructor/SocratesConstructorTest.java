package io.breen.socrates.constructor;

import org.junit.Test;

import static org.junit.Assert.*;

public class SocratesConstructorTest {

    @Test
    public void coerceShouldCoerceInteger() {
        Integer i = 6;
        Double d = 6.0;

        assertEquals(d, SocratesConstructor.coerceToDouble(i));
        assertEquals(6.0, SocratesConstructor.coerceToDouble(6), 0);
    }

    @Test
    public void coerceShouldNotAffectDouble() {
        Double d = 6.5;

        assertEquals(d, SocratesConstructor.coerceToDouble(d));
        assertEquals(6.0, SocratesConstructor.coerceToDouble(6.0), 0);
    }

    @Test
    public void coerceShouldNotAffectNull() {
        assertEquals(null, SocratesConstructor.coerceToDouble(null));
    }

    @Test(expected = ClassCastException.class)
    public void coerceShouldThrowExceptionWithString() {
        SocratesConstructor.coerceToDouble("");
    }
}
