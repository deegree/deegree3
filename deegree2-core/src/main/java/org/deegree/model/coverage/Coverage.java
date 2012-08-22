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

import java.awt.image.renderable.RenderableImage;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;

/**
 * Provides access to a coverage. The essential property of coverage is to be able to generate a value for any point
 * within its domain. How coverage is represented internally is not a concern.
 *
 * For example consider the following different internal representations of coverage:<br>
 * <UL>
 * <li>A coverage may be represented by a set of polygons which exhaustively tile a plane (that is each point on the
 * plane falls in precisely one polygon). The value returned by the coverage for a point is the value of an attribute of
 * the polygon that contains the point.</li>
 * <li>A coverage may be represented by a grid of values. The value returned by the coverage for a point is that of the
 * grid value whose location is nearest the point.</li>
 * <li>Coverage may be represented by a mathematical function. The value returned by the coverage for a point is just
 * the return value of the function when supplied the coordinates of the point as arguments.</li>
 * <li>Coverage may be represented by combination of these. For example, coverage may be represented by a combination of
 * mathematical functions valid over a set of polynomials.</LI>
 * </UL>
 *
 * A coverage has a corresponding {@link SampleDimension} for each sample dimension in the coverage. <br>
 * <br>
 * <STRONG>Implementation note:</STRONG><BR>
 * We expect that many implementations of {@link "org.opengis.coverage.grid.GridCoverage"} will want to leverage the
 * rich set of <A HREF="http://java.sun.com/products/java-media/jai/">Java Advanced Imaging (JAI)</A> features. For
 * those implementations, it is recommended (but not required) to implement the {@link javax.media.jai.PropertySource}
 * interface as well. In this case, implementation of {@link javax.media.jai.PropertySource} methods must be consistent
 * with {@link #getMetadataNames} and {@link #getMetadataValue} methods.
 *
 * @UML abstract CV_Coverage
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverage specification 1.0</A>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @see RenderableImage
 * @see javax.media.jai.ImageFunction
 */
public interface Coverage {
    /**
     * Specifies the coordinate reference system used when accessing a coverage or grid coverage with the
     * <code>evaluate(...)</code> methods. It is also the coordinate reference system of the coordinates used with the
     * math transform
     *
     * This coordinate reference system is usually different than coordinate system of the grid. Grid coverage can be
     * accessed (re-projected) with new coordinate reference system with the
     * {@link "org.opengis.coverage.processing.GridCoverageProcessor"} component. In this case, a new instance of a grid
     * coverage is created. <br>
     * <br>
     * Note: If a coverage does not have an associated coordinate reference system, the returned value will be
     * <code>null</code>.
     *
     * @return The coordinate reference system used when accessing a coverage or grid coverage with the
     *         <code>evaluate(...)</code> methods, or <code>null</code>.
     * @UML mandatory coordinateSystem
     */
    CoordinateSystem getCoordinateReferenceSystem();

    /**
     * The bounding box for the coverage domain in {@linkplain #getCoordinateReferenceSystem coordinate reference
     * system} coordinates. For grid coverages, the grid cells are centered on each grid coordinate. The envelope for a
     * 2-D grid coverage includes the following corner positions.
     *
     * <blockquote>
     *
     * <pre>
     *  (Minimum row - 0.5, Minimum column - 0.5) for the minimum coordinates
     *  (Maximum row - 0.5, Maximum column - 0.5) for the maximum coordinates
     * </pre>
     *
     * </blockquote>
     *
     * If a grid coverage does not have any associated coordinate reference system, the minimum and maximum coordinate
     * points for the envelope will be empty sequences.
     *
     * @return The bounding box for the coverage domain in coordinate system coordinates.
     * @UML mandatory envelope
     */
    Envelope getEnvelope();

    /**
     * The names of each dimension in the coverage. Typically these names are <var>x</var>, <var>y</var>, <var>z</var>
     * and <var>t</var>. The number of items in the sequence is the number of dimensions in the coverage. Grid coverages
     * are typically 2D (<var>x</var>, <var>y</var>) while other coverages may be 3D (<var>x</var>, <var>y</var>,
     * <var>z</var>) or 4D (<var>x</var>, <var>y</var>, <var>z</var>, <var>t</var>). The number of dimensions of the
     * coverage is the number of entries in the list of dimension names.
     *
     * @return The names of each dimension in the coverage.
     * @UML mandatory dimensionNames
     */
    String[] getDimensionNames();

    /**
     * The number of sample dimensions in the coverage. For grid coverages, a sample dimension is a band.
     *
     * @return The number of sample dimensions in the coverage.
     * @UML mandatory numSampleDimensions
     */
    int getNumSampleDimensions();

    /**
     * Retrieve sample dimension information for the coverage. For a grid coverage a sample dimension is a band. The
     * sample dimension information include such things as description, data type of the value (bit, byte, integer...),
     * the no data values, minimum and maximum values and a color table if one is associated with the dimension. A
     * coverage must have at least one sample dimension.
     *
     * @param index
     *            Index for sample dimension to retrieve. Indices are numbered 0 to (<var>
     *            {@linkplain #getNumSampleDimensions n}</var>-1).
     * @return Sample dimension information for the coverage.
     * @throws IndexOutOfBoundsException
     *             if <code>index</code> is out of bounds.
     * @UML operation getSampleDimension
     */
    SampleDimension getSampleDimension( int index )
                            throws IndexOutOfBoundsException;

    /**
     * Number of grid coverages which the grid coverage was derived from. This implementation specification does not
     * include interfaces for creating collections of coverages therefore this value will usually be one indicating an
     * adapted grid coverage, or zero indicating a raw grid coverage.
     *
     * @return The number of grid coverages which the grid coverage was derived from.
     * @UML mandatory numSource
     */
    int getNumSources();

    /**
     * Returns the source data for a coverage. This is intended to allow applications to establish what
     * <code>Coverage</code>s will be affected when others are updated, as well as to trace back to the "raw data".
     *
     * @param sourceDataIndex
     *            Source coverage index. Indexes start at 0.
     * @return The source data for a coverage.
     * @throws IndexOutOfBoundsException
     *             if <code>sourceDataIndex</code> is out of bounds.
     * @UML operation getSource
     *
     * @see #getNumSources
     * @see "org.opengis.coverage.grid.GridCoverage#getSource"
     */
    Coverage getSource( int sourceDataIndex )
                            throws IndexOutOfBoundsException;

    /**
     * List of metadata keywords for a coverage. If no metadata is available, the sequence will be empty.
     *
     * @return the list of metadata keywords for a coverage.
     * @UML mandatory metadataNames
     *
     * @see #getMetadataValue
     * @see javax.media.jai.PropertySource#getPropertyNames()
     */
    String[] getMetadataNames();

    /**
     * Retrieve the metadata value for a given metadata name.
     *
     * @param name
     *            Metadata keyword for which to retrieve data.
     * @return the metadata value for a given metadata name.
     * @throws MetadataNameNotFoundException
     *             if there is no value for the specified metadata name.
     * @UML operation getMetadataValue
     *
     * @see #getMetadataNames
     * @see javax.media.jai.PropertySource#getProperty
     */
    String getMetadataValue( String name )
                            throws MetadataNameNotFoundException;

    /**
     * Returns 2D view of this coverage as a renderable image. This optional operation allows interoperability with <A
     * HREF="http://java.sun.com/products/java-media/2D/">Java2D</A>. If this coverage is a
     * {@link "org.opengis.coverage.grid.GridCoverage"} backed by a {@link java.awt.image.RenderedImage}, the underlying
     * image can be obtained with:
     *
     * <code>getRenderableImage(0,1).{@linkplain RenderableImage#createDefaultRendering()
     * createDefaultRendering()}</code>
     *
     * @param xAxis
     *            Dimension to use for the <var>x</var> axis.
     * @param yAxis
     *            Dimension to use for the <var>y</var> axis.
     * @return A 2D view of this coverage as a renderable image.
     * @throws UnsupportedOperationException
     *             if this optional operation is not supported.
     * @throws IndexOutOfBoundsException
     *             if <code>xAxis</code> or <code>yAxis</code> is out of bounds.
     */
    RenderableImage getRenderableImage( int xAxis, int yAxis )
                            throws UnsupportedOperationException, IndexOutOfBoundsException;

}
