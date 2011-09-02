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
package org.deegree.protocol.wfs.query;

import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ResolveMode;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter200XMLDecoder;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.getfeature.XLinkPropertyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class QueryXMLAdapter extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( QueryXMLAdapter.class );

    public StandardPresentationParams parseStandardPresentationParameters200( OMElement requestEl ) {

        // <xsd:attribute name="startIndex" type="xsd:nonNegativeInteger" default="0"/>
        BigInteger startIndex = getNodeAsBigInt( rootElement, new XPath( "@startIndex", nsContext ), null );

        // <xsd:attribute name="count" type="xsd:nonNegativeInteger"/>
        BigInteger count = getNodeAsBigInt( rootElement, new XPath( "@count", nsContext ), null );

        // <xsd:attribute name="resultType" type="wfs:ResultTypeType" default="results"/>
        ResultType resultType = null;
        String resultTypeStr = getNodeAsString( rootElement, new XPath( "@resultType", nsContext ), null );
        if ( resultTypeStr != null ) {
            if ( resultTypeStr.equalsIgnoreCase( "results" ) ) {
                resultType = ResultType.RESULTS;
            } else if ( resultTypeStr.equalsIgnoreCase( "hits" ) ) {
                resultType = ResultType.HITS;
            } else {
                LOG.warn( "Invalid value (='{}') for resultType attribute.", resultTypeStr );
            }
        }

        // <xsd:attribute name="outputFormat" type="xsd:string" default="application/gml+xml; version=3.2"/>
        String outputFormat = getNodeAsString( rootElement, new XPath( "@outputFormat", nsContext ), null );

        return new StandardPresentationParams( startIndex, count, resultType, outputFormat );
    }

    public StandardResolveParams parseStandardResolveParameters200( OMElement requestEl ) {

        // <xsd:attribute name="resolve" type="wfs:ResolveValueType" default="none"/>
        ResolveMode resolve = null;
        String resolveString = getNodeAsString( rootElement, new XPath( "@resolve", nsContext ), null );
        if ( resolveString != null ) {
            if ( resolveString.equalsIgnoreCase( "local" ) ) {
                resolve = ResolveMode.LOCAL;
            } else if ( resolveString.equalsIgnoreCase( "remote" ) ) {
                resolve = ResolveMode.REMOTE;
            } else if ( resolveString.equalsIgnoreCase( "none" ) ) {
                resolve = ResolveMode.NONE;
            } else if ( resolveString.equalsIgnoreCase( "all" ) ) {
                resolve = ResolveMode.ALL;
            } else {
                LOG.warn( "Invalid value (='{}') for resolve attribute.", resolveString );
            }
        }

        // <xsd:attribute name="resolveDepth" type="wfs:positiveIntegerWithStar" default="*"/>
        String resolveDepth = getNodeAsString( rootElement, new XPath( "@resolveDepth", nsContext ), null );

        // <xsd:attribute name="resolveTimeout" type="xsd:positiveInteger" default="300"/>
        BigInteger resolveTimeout = getNodeAsBigInt( rootElement, new XPath( "@resolveTimeout", nsContext ), null );

        return new StandardResolveParams( resolve, resolveDepth, resolveTimeout );
    }

    /**
     * Parses a <code>fes:AbstractQueryExpression</code> (in the context of a WFS 2.0.0 XML request).
     * 
     * @param queryEl
     *            element substitutable for <code>fes:AbstractQueryExpression</code>, must not be <code>null</code>
     * @return parsed {@link Query}, never <code>null</code>
     */
    public Query parseAbstractQuery200( OMElement queryEl ) {
        QName elName = queryEl.getQName();
        if ( new QName( WFS_200_NS, "Query" ).equals( elName ) ) {
            return parseQuery200( queryEl );
        } else if ( new QName( WFS_200_NS, "StoredQuery" ).equals( elName ) ) {
            return parseStoredQuery200( queryEl );
        }
        String msg = "Unknown query element '" + elName + "'.";
        throw new XMLParsingException( this, queryEl, msg );
    }

    // <xsd:element name="Query" type="wfs:QueryType" substitutionGroup="fes:AbstractAdhocQueryExpression"/>
    private Query parseQuery200( OMElement queryEl ) {

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = getNodeAsString( queryEl, new XPath( "@handle", nsContext ), null );

        // <xsd:attribute name="typeNames" type="fes:TypeNamesListType" use="required"/>
        String typeNameStr = getRequiredNodeAsString( queryEl, new XPath( "@typeNames", nsContext ) );
        TypeName[] typeNames = TypeName.valuesOf( queryEl, typeNameStr );

        // <xsd:attribute name="aliases" type="fes:TypeNamesListType" use="required"/>

        // <xsd:element ref="fes:AbstractProjectionClause" minOccurs="0" maxOccurs="unbounded"/>

        // <xsd:element ref="fes:AbstractSelectionClause" minOccurs="0"/>
        Filter filter = null;
        OMElement filterEl = queryEl.getFirstChildWithName( new QName( FES_20_NS, "Filter" ) );
        if ( filterEl != null ) {
            try {
                // TODO remove usage of wrapper (necessary at the moment to work around problems with AXIOM's
                // XMLStreamReader)
                XMLStreamReader xmlStream = new XMLStreamReaderWrapper( filterEl.getXMLStreamReaderWithoutCaching(),
                                                                        null );
                // skip START_DOCUMENT
                xmlStream.nextTag();
                filter = Filter200XMLDecoder.parse( xmlStream );
            } catch ( XMLStreamException e ) {
                e.printStackTrace();
                throw new XMLParsingException( this, filterEl, e.getMessage() );
            }
        }

        // <xsd:element ref="fes:AbstractSortingClause" minOccurs="0"/>

        String featureVersion = null;
        ICRS crs = null;
        ValueReference[] propNamesArray = null;
        XLinkPropertyName[] xlinkPropNamesArray = null;
        Function[] functionsArray = null;
        SortProperty[] sortPropsArray = null;

        return new FilterQuery( handle, typeNames, featureVersion, crs, propNamesArray, xlinkPropNamesArray,
                                functionsArray, sortPropsArray, filter );
    }

    // <xsd:element name="StoredQuery" type="wfs:StoredQueryType" substitutionGroup="fes:AbstractQueryExpression"/>
    private StoredQuery parseStoredQuery200( OMElement queryEl ) {

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = getNodeAsString( queryEl, new XPath( "@handle", nsContext ), null );

        // <xsd:attribute name="id" type="xsd:anyURI" use="required"/>
        String id = getRequiredNodeAsString( queryEl, new XPath( "@id", nsContext ) );

        // <xsd:element name="Parameter" type="wfs:ParameterType" minOccurs="0" maxOccurs="unbounded"/>
        Map<String, OMElement> paramToValue = new HashMap<String, OMElement>();
        List<OMElement> paramEls = getElements( queryEl, new XPath( "wfs200:Parameter", nsContext ) );
        for ( OMElement paramEl : paramEls ) {
            String paramName = getRequiredNodeAsString( paramEl, new XPath( "@name", nsContext ) );
            paramToValue.put( paramName, paramEl );
        }
        return new StoredQuery( handle, id, paramToValue );
    }
}
