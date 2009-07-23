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
package org.deegree.commons.xml.schema;

import java.io.IOException;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * Xerces entity resolver that performs redirection of requests for OpenGIS core schemas (e.g. GML) to a local copy.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class RedirectingEntityResolver implements XMLEntityResolver {

    @Override
    public XMLInputSource resolveEntity( XMLResourceIdentifier identifier )
                            throws XNIException, IOException {

        // String systemId = identifier.getExpandedSystemId();
        // String redirectedSystemId = systemId.replace( "http://schemas.opengis.net/gml/3.1.1/", "file:/tmp/gml/3.1.1/"
        // );
        // redirectedSystemId = redirectedSystemId.replace( "http://schemas.opengis.net/gml/3.2.1/",
        // "file:/tmp/gml/3.2.1/" );
        // System.out.println( "'" + systemId + "' -> '" + redirectedSystemId + "'" );
        // return new XMLInputSource( null, redirectedSystemId, null );
        return new XMLInputSource( identifier );
    }

}
