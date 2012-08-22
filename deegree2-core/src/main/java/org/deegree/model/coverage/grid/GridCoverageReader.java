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

import java.io.IOException;

import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.datatypes.parameter.InvalidParameterValueException;
import org.deegree.datatypes.parameter.ParameterNotFoundException;

/**
 * Support for reading grid coverages out of a persisten store. Instance of
 * <code>GridCoverageReader</code> are obtained through a call to
 * {@link GridCoverageExchange#getReader(Object)}. Grid coverages are usually read from the input
 * stream in a sequential order.
 *
 * @author <A HREF="http://www.opengis.org">OpenGIS&reg; consortium</A>
 * @version 2.0
 *
 * @see GridCoverageExchange#getReader(Object)
 * @see javax.imageio.ImageReader
 */
public interface GridCoverageReader {
    /**
     * @return the format handled by this <code>GridCoverageReader</code>.
     */
    Format getFormat();

    /**
     * @return the input source. This is the object passed to the
     *         {@link GridCoverageExchange#getReader(Object)} method. It can be a
     *         {@link java.lang.String}, an {@link java.io.InputStream}, a
     *         {@link java.nio.channels.FileChannel}, whatever.
     */
    Object getSource();

    /**
     * Returns the list of metadata keywords associated with the {@linkplain #getSource input
     * source} as a whole (not associated with any particular grid coverage). If no metadata is
     * available, the array will be empty.
     *
     * @return The list of metadata keywords for the input source.
     * @throws IOException
     *             if an error occurs during reading.
     *
     * @revisit This javadoc may not apply thats well in the iterator scheme.
     */
    String[] getMetadataNames()
                            throws IOException;

    /**
     * Retrieve the metadata value for a given metadata name.
     *
     * @param name
     *            Metadata keyword for which to retrieve metadata.
     * @return The metadata value for the given metadata name. Should be one of the name returned by
     *         {@link #getMetadataNames}.
     * @throws IOException
     *             if an error occurs during reading.
     * @throws MetadataNameNotFoundException
     *             if there is no value for the specified metadata name.
     *
     * @revisit This javadoc may not apply thats well in the iterator scheme.
     */
    String getMetadataValue( String name )
                            throws IOException, MetadataNameNotFoundException;

    /**
     * Sets the metadata value for a given metadata name.
     *
     * @param name
     *            Metadata keyword for which to set the metadata.
     * @param value
     *            The metadata value for the given metadata name.
     * @throws IOException
     *             if an error occurs during writing.
     * @throws MetadataNameNotFoundException
     *             if the specified metadata name is not handled for this format.
     *
     * @revisit This javadoc may not apply thats well in the iterator scheme.
     */
    void setMetadataValue( String name, String value )
                            throws IOException, MetadataNameNotFoundException;

    /**
     * @param name
     *            for the next grid coverage to {@linkplain #read read} within the
     *            {@linkplain #getSource() input }. The subname can been fetch later at reading
     *            time.
     *
     * @throws IOException
     *             if an error occurs during writing.
     * @revisit Do we need a special method for that, or should it be a metadata?
     */
    void setCurrentSubname( String name )
                            throws IOException;

    /**
     * Retrieve the list of grid coverages contained within the {@linkplain #getSource input
     * source}. Each grid can have a different coordinate system, number of dimensions and grid
     * geometry. For example, a HDF-EOS file (GRID.HDF) contains 6 grid coverages each having a
     * different projection. An empty array will be returned if no sub names exist.
     *
     * @return The list of grid coverages contained within the input source.
     * @throws IOException
     *             if an error occurs during reading.
     *
     * @revisit The javadoc should also be more explicit about hierarchical format. Should the names
     *          be returned as paths? Explain what to return if the GridCoverage are accessible by
     *          index only. A proposal is to name them "grid1", "grid2", etc.
     */
    String[] listSubNames()
                            throws IOException;

    /**
     * @return the name for the next grid coverage to be {@linkplain #read read} from the
     *         {@linkplain #getSource input source}.
     *
     * @throws IOException
     *             if an error occurs during reading.
     * @revisit Do we need a special method for that, or should it be a metadata?
     */
    String getCurrentSubname()
                            throws IOException;

    /**
     * Read the grid coverage from the current stream position, and move to the next grid coverage.
     *
     * @param parameters
     *            An optional set of parameters. Should be any or all of the parameters returned by
     *            {@link Format#getReadParameters}.
     * @return A new {@linkplain GridCoverage grid coverage} from the input source.
     * @throws InvalidParameterNameException
     *             if a parameter in <code>parameters</code> doesn't have a recognized name.
     * @throws InvalidParameterValueException
     *             if a parameter in <code>parameters</code> doesn't have a valid value.
     * @throws ParameterNotFoundException
     *             if a parameter was required for the operation but was not provided in the
     *             <code>parameters</code> list.
     * @throws CannotCreateGridCoverageException
     *             if the coverage can't be created for a logical reason (for example an unsupported
     *             format, or an inconsistency found in the data).
     * @throws IOException
     *             if a read operation failed for some other input/output reason, including
     *             {@link java.io.FileNotFoundException} if no file with the given <code>name</code>
     *             can be found, or {@link javax.imageio.IIOException} if an error was thrown by the
     *             underlying image library.
     */
    GridCoverage read( GeneralParameterValueIm[] parameters )
                            throws InvalidParameterNameException, InvalidParameterValueException,
                            ParameterNotFoundException, IOException;

    /**
     * Allows any resources held by this object to be released. The result of calling any other
     * method subsequent to a call to this method is undefined. It is important for applications to
     * call this method when they know they will no longer be using this
     * <code>GridCoverageReader</code>. Otherwise, the reader may continue to hold on to
     * resources indefinitely.
     *
     * @throws IOException
     *             if an error occured while disposing resources (for example while closing a file).
     */
    void dispose()
                            throws IOException;
}
