//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.storedquery;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;

/**
 * Actual code of a {@link StoredQueryDefinition}.
 * 
 * @see StoredQueryDefinition
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class QueryExpressionText extends XMLAdapter {

    private final List<QName> returnFtNames;

    private final String language;

    private final boolean isPrivate;

    QueryExpressionText( OMElement el ) {
        setRootElement( el );

        // <xsd:attribute name="returnFeatureTypes" type="wfs:ReturnFeatureTypesListType" use="required"/>
        String returnFtsStr = getRequiredNodeAsString( el, new XPath( "@returnFeatureTypes", nsContext ) );
        String[] tokens = StringUtils.split( returnFtsStr, " " );
        returnFtNames = new ArrayList<QName>( tokens.length );
        for ( String token : tokens ) {
            returnFtNames.add( parseQName( token, el ) );
        }

        // <xsd:attribute name="language" type="xsd:anyURI" use="required"/>
        language = getRequiredNodeAsString( el, new XPath( "@language", nsContext ) );

        // <xsd:attribute name="isPrivate" type="xsd:boolean" default="false"/>
        isPrivate = getNodeAsBoolean( el, new XPath( "@isPrivate", nsContext ), false );
    }

    public List<QName> getReturnFeatureTypes() {
        return returnFtNames;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}
