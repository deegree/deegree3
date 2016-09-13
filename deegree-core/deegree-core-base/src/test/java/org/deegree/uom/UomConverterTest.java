package org.deegree.uom;

import org.deegree.commons.uom.Measure;
import org.junit.Ignore;
import org.junit.Test;

import javax.measure.converter.ConversionException;
import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UomConverterTest {

    @Ignore("Mapping to symbol is currently not supported")
    @Test
    public void toMeter_fromMeter()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "meter" );
        double inMeter = UomConverter.toMeter( measure );

        assertThat( inMeter, is( 10.0 ) );
    }

    @Test
    public void toMeter_fromMeterUnit()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "m" );
        double inMeter = UomConverter.toMeter( measure );

        assertThat( inMeter, is( 10.0 ) );
    }

    @Ignore("Mapping to symbol is currently not supported")
    @Test
    public void toMeter_fromKiloMeter()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "kilometer" );
        double inMeter = UomConverter.toMeter( measure );

        assertThat( inMeter, is( 10000.0 ) );
    }

    @Test
    public void toMeter_fromKiloMeterUnit()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "km" );
        double inMeter = UomConverter.toMeter( measure );

        assertThat( inMeter, is( 10000.0 ) );
    }

    @Ignore("Mapping to symbol is currently not supported")
    @Test
    public void toMeter_fromYard()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "yard" );
        double inMeter = UomConverter.toMeter( measure );

        assertThat( inMeter, is( 9.144 ) );
    }

    @Test
    public void toMeter_fromYardUnit()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "yd" );
        double inMeter = UomConverter.toMeter( measure );

        assertThat( inMeter, is( 9.144 ) );
    }

    @Test
    public void toMeter_emptyUom()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "" );
        double inMeter = UomConverter.toMeter( measure );

        assertThat( inMeter, is( 10.0 ) );
    }

    @Test
    public void toMeter_nullUom()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), null );
        double inMeter = UomConverter.toMeter( measure );

        assertThat( inMeter, is( 10.0 ) );
    }

    @Test(expected = IllegalArgumentException.class)
    public void toMeter_unknownSymbol()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "abc" );
        UomConverter.toMeter( measure );
    }
    
    @Test(expected = ConversionException.class)
    public void toMeter_unexpectedType()
                            throws Exception {
        Measure measure = new Measure( new BigDecimal( 10 ), "min" );
        UomConverter.toMeter( measure );
    }

}