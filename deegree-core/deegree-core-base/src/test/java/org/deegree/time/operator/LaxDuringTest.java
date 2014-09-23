package org.deegree.time.operator;

import static java.util.Collections.emptyList;
import static org.deegree.time.position.IndeterminateValue.UNKNOWN;

import java.util.List;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.GenericTimeInstant;
import org.deegree.time.primitive.GenericTimePeriod;
import org.deegree.time.primitive.RelatedTime;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;
import org.junit.Assert;
import org.junit.Test;

public class LaxDuringTest {

    private final LaxDuring laxDuring = new LaxDuring();

    @Test
    public void instantInstant() {
        assertLaxDuring( instant( "2014-01-01T00:00:01" ), instant( "2014-01-01T00:00:01" ) );
        assertNotLaxDuring( instant( "2014-01-01T00:00:01" ), instant( "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( instant( "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:01" ) );
    }

    @Test
    public void periodInstant() {
        assertNotLaxDuring( period( "2014-01-01T00:00:01", "INDETERMINATE" ), instant( "2014-01-01T00:00:00" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:01", "INDETERMINATE" ), instant( "2014-01-01T00:00:01" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:01", "INDETERMINATE" ), instant( "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:00" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:01" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:03" ) );
    }

    @Test
    public void instantPeriod() {
        assertNotLaxDuring( instant( "2014-01-01T00:00:00" ), period( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        assertLaxDuring( instant( "2014-01-01T00:00:01" ), period( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        assertLaxDuring( instant( "2014-01-01T00:00:02" ), period( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        assertNotLaxDuring( instant( "2014-01-01T00:00:00" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
        assertLaxDuring( instant( "2014-01-01T00:00:01" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( instant( "2014-01-01T00:00:02" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( instant( "2014-01-01T00:00:03" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
        assertNotLaxDuring( instant( "2014-01-01T00:00:00" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertLaxDuring( instant( "2014-01-01T00:00:01" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertLaxDuring( instant( "2014-01-01T00:00:02" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertNotLaxDuring( instant( "2014-01-01T00:00:03" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertNotLaxDuring( instant( "2014-01-01T00:00:04" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
    }

    @Test
    public void periodPeriod() {
        // Begins
        assertLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         period( "2014-01-01T00:00:02", "2014-01-01T00:00:06" ) );
        assertLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         period( "2014-01-01T00:00:02", "INDETERMINATE" ) );
        // Ends
        assertLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         period( "2014-01-01T00:00:01", "2014-01-01T00:00:05" ) );
        // During
        assertLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         period( "2014-01-01T00:00:01", "2014-01-01T00:00:06" ) );
        assertLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         period( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        // TEquals
        assertLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                         period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ) );
        // BegunBy
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:02", "2014-01-01T00:00:03" ) );
        // EndedBy
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:04", "2014-01-01T00:00:05" ) );
        // After
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:00", "2014-01-01T00:00:01" ) );
        // Before
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:06", "2014-01-01T00:00:07" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:06", "INDETERMINATE" ) );
        // TContains
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:03", "2014-01-01T00:00:04" ) );
        // TOverlaps
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:04", "2014-01-01T00:00:06" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:04", "INDETERMINATE" ) );
        // OverlappedBy
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        // Meets
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:05", "2014-01-01T00:00:06" ) );
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:05", "INDETERMINATE" ) );
        // MetBy
        assertNotLaxDuring( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
    }

    private void assertLaxDuring( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        Assert.assertTrue( laxDuring( a, b ) );
    }

    private void assertNotLaxDuring( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        Assert.assertFalse( laxDuring( a, b ) );
    }

    private boolean laxDuring( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        return laxDuring.evaluate( a, b );
    }

    private TimeInstant instant( final String s ) {
        final List<Property> props = emptyList();
        final List<RelatedTime> relatedTimes = emptyList();
        TimePosition pos = null;
        if ( "INDETERMINATE".equals( s ) ) {
            pos = new TimePosition( null, null, UNKNOWN, "" );
        } else {
            pos = new TimePosition( null, null, null, s );
        }
        return new GenericTimeInstant( null, props, relatedTimes, null, pos );
    }

    private TimePeriod period( final String t1, final String t2 ) {
        final TimeInstant begin = instant( t1 );
        final TimeInstant end = instant( t2 );
        final List<Property> props = emptyList();
        final List<RelatedTime> relatedTimes = emptyList();
        return new GenericTimePeriod( null, props, relatedTimes, null, begin, end );
    }
}
