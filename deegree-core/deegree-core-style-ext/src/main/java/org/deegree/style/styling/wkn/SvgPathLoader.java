package org.deegree.style.styling.wkn;

import java.awt.Shape;
import java.net.URL;
import java.util.function.Function;

import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathParser;
import org.deegree.style.styling.mark.WellKnownNameLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvgPathLoader implements WellKnownNameLoader {

    private static final Logger LOG = LoggerFactory.getLogger( SvgPathLoader.class );

    public static final String PREFIX = "svgpath://";

    @Override
    public Shape parse( String wellKnownName, Function<String, URL> resolver ) {
        if ( wellKnownName == null || !wellKnownName.startsWith( PREFIX ) )
            return null;

        String wkn = wellKnownName.substring( PREFIX.length() );

        Shape s = null;
        AWTPathProducer pathProducer = new AWTPathProducer();
        PathParser pp = new PathParser();
        pp.setPathHandler( pathProducer );
        try {
            pp.parse( wkn );
            s = pathProducer.getShape();
        } catch ( ParseException ex ) {
            LOG.warn( "Could not Parse SVGPath {}: {}", wkn, ex.getMessage() );
            LOG.trace( "Exception", ex );
        }

        return s;
    }
}
