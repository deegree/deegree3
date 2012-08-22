// $HeadURL$
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

package org.deegree.portal.standard.csw;

import org.deegree.framework.util.StringTools;
import org.deegree.portal.PortalException;

/**
 * A <code>${type_name}</code> class.<br/>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CatalogClientException extends PortalException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>CatalogClientException</code> without detail message.
     */
    public CatalogClientException() {
        st = "CatalogClientException";
    }

    /**
     * Constructs an instance of <code>CatalogClientException</code> with the specified detail message.
     *
     * @param msg
     *            the detail message.
     */
    public CatalogClientException( String msg ) {
        super( msg );
    }

    /**
     * Constructs an instance of <code>CatalogClientException</code> with the specified detail message.
     *
     * @param msg
     *            the detail message.
     * @param e
     */
    public CatalogClientException( String msg, Exception e ) {
        this( msg );
        st = StringTools.stackTraceToString( e.getStackTrace() );
    }

}
