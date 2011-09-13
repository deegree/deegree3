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

import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wfs.query.StoredQuery;

/**
 * Defines the template for a {@link StoredQuery}.
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
        nsContext.addNamespace( WFS_200_PREFIX, WFS_200_NS );
    }

    private final String id;

    private final List<LanguageString> titles;

    private final List<LanguageString> abstracts;

    private final List<QueryExpressionText> queryExpressionTexts;

    public StoredQueryDefinition( OMElement el ) {
        setRootElement( el );

        // <xsd:attribute name="id" type="xsd:anyURI" use="required"/>
        this.id = getRequiredNodeAsString( el, new XPath( "@id", nsContext ) );

        // <xsd:element ref="wfs:Title" minOccurs="0" maxOccurs="unbounded"/>
        List<OMElement> titleEls = getElements( el, new XPath( "wfs200:Title", nsContext ) );
        titles = new ArrayList<LanguageString>( titleEls.size() );
        for ( OMElement titleEl : titleEls ) {
            String lang = getNodeAsString( titleEl, new XPath( "@xml:lang", nsContext ), null );
            String value = titleEl.getText();
            titles.add( new LanguageString( value, lang ) );
        }

        // <xsd:element ref="wfs:Abstract" minOccurs="0" maxOccurs="unbounded"/>
        List<OMElement> abstractEls = getElements( el, new XPath( "wfs200:Abstract", nsContext ) );
        abstracts = new ArrayList<LanguageString>( abstractEls.size() );
        for ( OMElement abstractEl : abstractEls ) {
            String lang = getNodeAsString( abstractEl, new XPath( "@xml:lang", nsContext ), null );
            String value = abstractEl.getText();
            abstracts.add( new LanguageString( value, lang ) );
        }

        // <xsd:element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded"/>

        // <xsd:element name="Parameter" type="wfs:ParameterExpressionType" minOccurs="0" maxOccurs="unbounded"/>

        // <xsd:element name="QueryExpressionText" type="wfs:QueryExpressionTextType" minOccurs="1"
        // maxOccurs="unbounded"/>
        List<OMElement> queryExprEls = getRequiredElements( el, new XPath( "wfs200:QueryExpressionText", nsContext ) );
        queryExpressionTexts = new ArrayList<QueryExpressionText>( queryExprEls.size() );
        for ( OMElement queryExprEl : queryExprEls ) {
            queryExpressionTexts.add( new QueryExpressionText( queryExprEl ) );
        }

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
        return titles;
    }

    public List<LanguageString> getAbstracts() {
        return abstracts;
    }

    public List<QueryExpressionText> getQueryExpressionTextEls() {
        return queryExpressionTexts;
    }
}
