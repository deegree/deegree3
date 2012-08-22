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
package org.deegree.ogcwebservices.wfs.operation;

import static org.deegree.framework.xml.XMLTools.getNodeAsString;
import static org.deegree.framework.xml.XMLTools.getNodesAsQualifiedNames;
import static org.deegree.ogcwebservices.wfs.operation.AbstractWFSRequest.FORMAT_GML2_WFS100;
import static org.deegree.ogcwebservices.wfs.operation.AbstractWFSRequest.FORMAT_GML3;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.w3c.dom.Element;

/**
 * Parser for "wfs:DescribeFeatureType" requests.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DescribeFeatureTypeDocument extends AbstractWFSRequestDocument {

    private static final long serialVersionUID = -3330169803468922836L;

    /**
     * Parses the underlying document into a {@link DescribeFeatureType} request object.
     *
     * @param id
     * @return corresponding <code>DescribeFeatureType</code> object
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     */
    public DescribeFeatureType parse( String id )
                            throws XMLParsingException, InvalidParameterValueException {

        checkServiceAttribute();
        String version = checkVersionAttribute();
        boolean is100 = version.equals( "1.0.0" );

        Element root = this.getRootElement();
        String handle = getNodeAsString( root, "@handle", nsContext, null );
        String outputFormat = getNodeAsString( root, "@outputFormat", nsContext, is100 ? FORMAT_GML2_WFS100
                                                                                      : FORMAT_GML3 );
        if ( outputFormat.equalsIgnoreCase( "xmlschema" ) ) {
            outputFormat = FORMAT_GML2_WFS100;
        }
        QualifiedName[] typeNames = getNodesAsQualifiedNames( root, "wfs:TypeName/text()", nsContext );
        return new DescribeFeatureType( version, id, handle, outputFormat, typeNames, null );
    }
}
