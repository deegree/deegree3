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

import org.deegree.model.coverage.Coverage;

/**
 * Represent the basic implementation which provides access to grid coverage data. A
 * <code>GridCoverage</code> implementation may provide the ability to update grid values.
 *
 * @UML abstract CV_GridCoverage
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverage specification 1.0</A>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @see java.awt.image.RenderedImage
 * @see javax.media.jai.PixelAccessor
 */
public interface GridCoverage extends Coverage {
    /**
     * Returns <code>true</code> if grid data can be edited.
     *
     * @return <code>true</code> if grid data can be edited.
     * @UML mandatory dataEditable
     */
    boolean isDataEditable();

    /**
     * Information for the grid coverage geometry. Grid geometry includes the valid range of grid
     * coordinates and the georeferencing.
     *
     * @return The information for the grid coverage geometry.
     * @UML mandatory gridGeometry
     */
    GridGeometry getGridGeometry();

    /**
     * Returns the source data for a grid coverage. If the <code>GridCoverage</code> was produced
     * from an underlying dataset (by {@link GridCoverageReader#read read(...)} for instance) the
     * getNumSources() method should returns zero, and this method should not be called.
     *
     * If the <code>GridCoverage</code> was produced using {link
     * org.opengis.coverage.processing.GridCoverageProcessor} then it should return the source grid
     * coverage of the one used as input to <code>GridCoverageProcessor</code>. In general the
     * <code>getSource(i)</code> method is intended to return the original
     * <code>GridCoverage</code> on which it depends.
     *
     * This is intended to allow applications to establish what <code>GridCoverage</code>s will
     * be affected when others are updated, as well as to trace back to the "raw data".
     *
     * @param sourceDataIndex
     *            Source grid coverage index. Indexes start at 0.
     * @return The source data for a grid coverage.
     * @throws IndexOutOfBoundsException
     *             if <code>sourceDataIndex</code> is out of bounds.
     * @UML operation Coverage.getSource
     */
    Coverage getSource( int sourceDataIndex )
                            throws IndexOutOfBoundsException;

}
