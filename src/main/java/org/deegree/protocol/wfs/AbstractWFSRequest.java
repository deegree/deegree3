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
package org.deegree.protocol.wfs;

import org.deegree.commons.tom.ows.Version;

/**
 * Abstract base class for WFS request beans.
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
