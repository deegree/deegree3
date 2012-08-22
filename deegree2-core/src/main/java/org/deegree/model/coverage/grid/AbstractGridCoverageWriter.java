//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/
package org.deegree.model.coverage.grid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public abstract class AbstractGridCoverageWriter implements GridCoverageWriter {

    protected Object destination = null;

    protected Map<String, Object> metadata = new HashMap<String, Object>();

    private String[] subNames = null;

    private String currentSubname = null;

    protected Format format = null;

    /**
     * @param destination
     * @param metadata
     * @param subNames
     * @param currentSubname
     * @param format
     */
    public AbstractGridCoverageWriter( Object destination, Map<String, Object> metadata, String[] subNames,
                                       String currentSubname, Format format ) {
        this.destination = destination;
        this.metadata = metadata;
        this.subNames = subNames;
        this.currentSubname = currentSubname;
        this.format = format;
    }

    /**
     * Returns the format handled by this <code>GridCoverageWriter</code>.
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Returns the output destination. This is the object passed to the !unknown! method. It can be a
     * {@link java.lang.String}, an {@link java.io.OutputStream}, a
     * {@link java.nio.channels.FileChannel}, etc.
     */
    public Object getDestination() {
        return destination;
    }

    /**
     * Returns the list of metadata keywords associated with the {@linkplain #getDestination output
     * destination} as a whole (not associated with any particular grid coverage). If no metadata is
     * allowed, the array will be empty.
     *
     * @return The list of metadata keywords for the output destination.
     *
     * @revisit This javadoc may not apply thats well in the iterator scheme.
     */
    public String[] getMetadataNames() {
        return metadata.keySet().toArray( new String[metadata.size()] );
    }

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
    public Object getMetadataValue( String name )
                            throws IOException, MetadataNameNotFoundException {
        return metadata.get( name );
    }

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
    public void setMetadataValue( String name, String value )
                            throws IOException, MetadataNameNotFoundException {
        metadata.put( name, value );
    }

    /**
     * Retrieve the list of grid coverages contained within the {@linkplain #getDestination() input
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
    public String[] listSubNames()
                            throws IOException {
        return subNames;
    }

    /**
     * Returns the name for the next grid coverage to be read from the
     * {@linkplain #getDestination() output destination}.
     *
     * @throws IOException
     *             if an error occurs during reading.
     * @revisit Do we need a special method for that, or should it be a metadata?
     */
    public String getCurrentSubname()
                            throws IOException {
        return currentSubname;
    }

    /**
     * Set the name for the next grid coverage to GridCoverageWriter.write(GridCoverage,
     * GeneralParameterValue[]) within the{@linkplain #getDestination output destination}. The
     * subname can been fetch later at reading time.
     *
     * @throws IOException
     *             if an error occurs during writing.
     * @revisit Do we need a special method for that, or should it be a metadata?
     */
    public void setCurrentSubname( String name )
                            throws IOException {
        currentSubname = name;
    }

}
