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
package org.deegree.model.coverage;

/**
 * Thrown when a <code>org.opengis.coverage.Coverage.evaluate</code> method is invoked with a
 * point outside coverage.
 *
 * @UML exception CV_PointOutsideCoverage
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverage specification 1.0</A>
 */
public class PointOutsideCoverageException extends CannotEvaluateException {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -8718412090539227101L;

    /**
     * Creates an exception with no message.
     */
    public PointOutsideCoverageException() {
        super();
    }

    /**
     * Creates an exception with the specified message.
     *
     * @param message
     *            The detail message. The detail message is saved for later retrieval by the
     *            {@link #getMessage()} method.
     */
    public PointOutsideCoverageException( String message ) {
        super( message );
    }
}
