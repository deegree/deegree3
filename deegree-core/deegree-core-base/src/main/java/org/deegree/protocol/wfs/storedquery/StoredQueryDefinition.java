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

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wfs.WFSConstants;

/**
 * Definition of a <code>StoredQuery</code>.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StoredQueryDefinition extends XMLAdapter {

    /** Namespace context with predefined bindings "wfs200" */
    protected static final NamespaceBindings nsContext;

    /** Namespace binding for WFS 2.0.0 constructs */
    protected final static String WFS_200_PREFIX = "wfs200";

    static {
        nsContext = new NamespaceBindings( XMLAdapter.nsContext );
        nsContext.addNamespace( WFS_200_PREFIX, WFSConstants.WFS_200_NS );
    }

    private final String id;

    public StoredQueryDefinition( OMElement el ) {
        setRootElement( el );

        // <xsd:attribute name="id" type="xsd:anyURI" use="required"/>
        this.id = getRequiredNodeAsString( el, new XPath( "id", nsContext ) );

        // <xsd:element ref="wfs:Title" minOccurs="0" maxOccurs="unbounded"/>
        
        // TODO: for every       
        // <xsd:attribute name="returnFeatureTypes" type="wfs:ReturnFeatureTypesListType" use="required"/>        
        // <xsd:attribute name="isPrivate" type="xsd:boolean" default="false"/>
    }

    /**
     * 
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @return
     */
    public List<LanguageString> getTitles() {
        return null;
    }

    public List<QName> getReturnFeatureTypes() {
        return null;
    }

    public boolean isPrivate() {
        return true;
    }
}
