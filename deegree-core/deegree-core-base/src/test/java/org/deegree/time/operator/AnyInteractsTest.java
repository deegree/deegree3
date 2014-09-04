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

public class AnyInteractsTest {

    private final AnyInteracts anyInteracts = new AnyInteracts();

    @Test
    public void instantInstant() {
        assertAnyInteracts( instant( "2014-01-01T00:00:01" ), instant( "2014-01-01T00:00:01" ) );
        assertNotAnyInteracts( instant( "2014-01-01T00:00:01" ), instant( "2014-01-01T00:00:02" ) );
        assertNotAnyInteracts( instant( "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:01" ) );
    }

    @Test
    public void instantPeriod() {
        assertNotAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:00" ) );
        assertAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:01" ) );
        assertNotAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:02" ) );
        assertNotAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ), instant( "2014-01-01T00:00:03" ) );
        assertNotAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ), instant( "2014-01-01T00:00:00" ) );
        assertAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ), instant( "2014-01-01T00:00:01" ) );
        assertAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ), instant( "2014-01-01T00:00:02" ) );
        assertNotAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ), instant( "2014-01-01T00:00:03" ) );
        assertNotAnyInteracts( period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ), instant( "2014-01-01T00:00:04" ) );
        assertAnyInteracts( instant( "2014-01-01T00:00:02" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        assertNotAnyInteracts( instant( "2014-01-01T00:00:04" ), period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
    }

    @Test
    public void instantPeriodIndeterminate() {
        assertNotAnyInteracts( period( "2014-01-01T00:00:01", "INDETERMINATE" ), instant( "2014-01-01T00:00:00" ) );
        assertAnyInteracts( period( "2014-01-01T00:00:01", "INDETERMINATE" ), instant( "2014-01-01T00:00:01" ) );
        assertAnyInteracts( period( "2014-01-01T00:00:01", "INDETERMINATE" ), instant( "2014-01-01T00:00:02" ) );
    }

    @Test
    public void periodPeriod() {
        // Begins
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:02", "2014-01-01T00:00:06" ) );
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:02", "INDETERMINATE" ) );
        // BegunBy
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:02", "2014-01-01T00:00:03" ) );
        // Ends
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:01", "2014-01-01T00:00:05" ) );
        // EndedBy
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:04", "2014-01-01T00:00:05" ) );
        // TContains
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:03", "2014-01-01T00:00:04" ) );
        // During
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:01", "2014-01-01T00:00:06" ) );
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:01", "INDETERMINATE" ) );
        // TEquals
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ) );
        // TOverlaps
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:04", "2014-01-01T00:00:06" ) );
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:04", "INDETERMINATE" ) );
        // OverlappedBy
        assertAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                            period( "2014-01-01T00:00:01", "2014-01-01T00:00:03" ) );
        // After
        assertNotAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               period( "2014-01-01T00:00:00", "2014-01-01T00:00:01" ) );
        // Before
        assertNotAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               period( "2014-01-01T00:00:06", "2014-01-01T00:00:07" ) );
        assertNotAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               period( "2014-01-01T00:00:06", "INDETERMINATE" ) );
        // Meets
        assertNotAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               period( "2014-01-01T00:00:05", "2014-01-01T00:00:06" ) );
        assertNotAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               period( "2014-01-01T00:00:05", "INDETERMINATE" ) );
        // MetBy
        assertNotAnyInteracts( period( "2014-01-01T00:00:02", "2014-01-01T00:00:05" ),
                               period( "2014-01-01T00:00:01", "2014-01-01T00:00:02" ) );
    }

    private void assertAnyInteracts( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        Assert.assertTrue( anyInteracts( a, b ) );
    }

    private void assertNotAnyInteracts( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        Assert.assertFalse( anyInteracts( a, b ) );
    }

    private boolean anyInteracts( TimeGeometricPrimitive a, TimeGeometricPrimitive b ) {
        return anyInteracts.anyInteracts( a, b );
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
