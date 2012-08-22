//$HeadURL$
/*----------------------------------------------------------------------------
 This file originated as a part of GeoAPI.

 GeoAPI is free software. GeoAPI may be used, modified and
 redistributed by anyone for any purpose requring only maintaining the
 copyright and license terms on the source code and derivative files.
 See the OGC legal page for details.

 The copyright to the GeoAPI interfaces is held by the Open Geospatial
 Consortium, see http://www.opengeospatial.org/ogc/legal
----------------------------------------------------------------------------*/
package org.deegree.model.coverage.grid;

// J2SE direct depencies
import java.io.IOException;

/**
 * Thrown when a {@linkplain GridCoverage grid coverage} can't be created.
 *
 * @UML exception GC_CannotCreateGridCoverage
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverage specification 1.0</A>
 *
 * @see GridCoverageReader#read
 *
 * @revisit In a J2SE 1.4 profile, this exception should extends {@link javax.imageio.IIOException}.
 */
public class CannotCreateGridCoverageException extends IOException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 3768704221879769389L;

    /**
     * Creates an exception with no message.
     */
    public CannotCreateGridCoverageException() {
        super();
    }

    /**
     * Creates an exception with the specified message.
     *
     * @param message
     *            The detail message. The detail message is saved for later retrieval by the
     *            {@link #getMessage()} method.
     */
    public CannotCreateGridCoverageException( String message ) {
        super( message );
    }
}
