//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.protocol.wfs.describefeaturetype;

import javax.xml.namespace.QName;

import org.deegree.commons.types.ows.Version;
import org.deegree.protocol.wfs.AbstractWFSRequest;

/**
 * Represents a <code>DescribeFeatureType</code> request to a WFS.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeFeatureType extends AbstractWFSRequest {

    private String outputFormat;

    private QName[] typeNames;

    /**
     * Creates a new {@link DescribeFeatureType} request.
     * 
     * @param version
     *            protocol version, may not be null
     * @param handle
     *            client-generated identifier, may be null
     * @param outputFormat
     *            requested output format, may be null
     * @param typeNames
     *            requested type names, may be null
     */
    public DescribeFeatureType( Version version, String handle, String outputFormat, QName[] typeNames ) {
        super( version, handle );
        this.outputFormat = outputFormat;
        this.typeNames = typeNames;
    }

    /**
     * Returns the requested output format.
     * 
     * @return the requested output format, or null if unspecified
     */
    public String getOutputFormat() {
        return this.outputFormat;
    }

    /**
     * Returns the names of the feature types for which the schema is requested.
     * 
     * @return the names of the feature types for which the schema is requested, or null if unspecified
     */
    public QName[] getTypeNames() {
        return typeNames;
    }
}
