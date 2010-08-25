//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wps.client.input;

import java.net.URL;

import org.deegree.commons.tom.ows.CodeType;

/**
 * Abstract base class for input parameters provided for a process execution.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class ExecutionInput {

    private CodeType id;

    /**
     * Creates a new {@link ExecutionInput} instance.
     * 
     * @param id
     *            parameter identifier, must not be <code>null</code>
     */
    protected ExecutionInput( CodeType id ) {
        this.id = id;
    }

    /**
     * Returns the parameter identifier.
     * 
     * @return the parameter identifier, never <code>null</code>
     */
    public CodeType getId() {
        return id;
    }

    /**
     * Returns the web-accessible URL for retrieving the input value.
     * 
     * @return web-accessible URL, can be <code>null</code> (not web-acessible)
     */
    public URL getWebAccessibleURL() {
        return null;
    }
}
