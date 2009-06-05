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
package org.deegree.protocol.wfs;

import org.deegree.commons.types.ows.Version;

/**
 * Represents a <code>DescribeFeatureType</code> request to a WFS.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class AbstractWFSRequest {
    
    private Version version;
    
    private String handle;

    /**
     * Creates a new {@link AbstractWFSRequest} request.
     * 
     * @param version
     *            protocol version, may not be null
     * @param handle
     *            client-generated identifier, may be null
     */
    public AbstractWFSRequest( Version version, String handle ) {
        this.version = version;
        this.handle = handle;
    }

    /**
     * Returns the protocol version of the request.
     * 
     * @return the protocol version of the request, never null
     */
    public Version getVersion() {
        return this.version;
    }
    
    /**
     * Returns the client-generated identifier supplied with the request.
     * 
     * @return the client-generated identifier, may be null
     */
    public String getHandle() {
        return this.handle;
    }    
 }
