package org.deegree.style.styling.wkn;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.function.Function;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader2;
import org.deegree.style.styling.mark.WellKnownNameLoader;
import org.deegree.style.styling.wkn.shape.ShapeConverterLinearize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.io.ParseException;

public class WKTLinearizeLoader implements WellKnownNameLoader {

    private static final Logger LOG = LoggerFactory.getLogger( WKTLinearizeLoader.class );

    public static final String PREFIX = "wktlin://";

    @Override
    public Shape parse( String wellKnownName, Function<String, URL> resolver ) {
        if ( wellKnownName == null || !wellKnownName.startsWith( PREFIX ) )
            return null;

        String wkn = wellKnownName.substring( PREFIX.length() );
        Shape s = null;
        try {
            WKTReader2 reader = new WKTReader2();
            Geometry geom = reader.read( wkn );

            ShapeConverterLinearize converter = new ShapeConverterLinearize( false, 500 );

            Shape orig = converter.convert( geom );
            AffineTransform at = AffineTransform.getScaleInstance( 1.0, -1.0 );
            s = at.createTransformedShape( orig );
        } catch ( ParseException ex ) {
            LOG.warn( "Could not Parse WKT {}: {}", wkn, ex.getMessage() );
            LOG.trace( "Exception", ex );
        }

        return s;
    }
}
