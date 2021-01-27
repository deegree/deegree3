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
package org.deegree.protocol.wfs.query.xml;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;

import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.ResolveMode;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.projection.TimeSliceProjection;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter200XMLDecoder;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.Query;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.StoredQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides parsing methods for WFS <code>Query</code> elements and related constructs.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class QueryXMLAdapter extends AbstractWFSRequestXMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( QueryXMLAdapter.class );

    public StandardPresentationParams parseStandardPresentationParameters100( OMElement requestEl ) {

        String resultTypeStr = getNodeAsString( rootElement, new XPath( "@resultType", nsContext ), null );
        ResultType resultType = null;
        if ( resultTypeStr != null ) {
            if ( resultTypeStr.equalsIgnoreCase( ResultType.RESULTS.toString() ) ) {
                resultType = ResultType.RESULTS;
            } else if ( resultTypeStr.equalsIgnoreCase( ResultType.HITS.toString() ) ) {
                resultType = ResultType.HITS;
            }
        }

        String outputFormat = getNodeAsString( rootElement, new XPath( "@outputFormat", nsContext ), null );
        BigInteger maxFeatures = getNodeAsBigInt( rootElement, new XPath( "@maxFeatures", nsContext ), null );

        return new StandardPresentationParams( null, maxFeatures, resultType, outputFormat );
    }

    public StandardPresentationParams parseStandardPresentationParameters110( OMElement requestEl ) {
        return parseStandardPresentationParameters100( requestEl );
    }

    /**
     * Parses the <code>wfs:StandardPresentationParameters</code> attribute group.
     * 
     * @param requestEl
     * @return
     */
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

    public ResolveParams parseStandardResolveParameters110( OMElement requestEl ) {

        String traverseXlinkDepth = getNodeAsString( rootElement, new XPath( "@traverseXlinkDepth", nsContext ), null );

        String traverseXlinkExpiryStr = getNodeAsString( rootElement, new XPath( "@traverseXlinkExpiry", nsContext ),
                                                         null );
        BigInteger resolveTimeout = null;
        if ( traverseXlinkExpiryStr != null ) {
            resolveTimeout = new BigInteger( traverseXlinkExpiryStr ).multiply( BigInteger.valueOf( 60 ) );
        }

        return new ResolveParams( null, traverseXlinkDepth, resolveTimeout );
    }

    public ResolveParams parseStandardResolveParameters200( OMElement element ) {

        // <xsd:attribute name="resolve" type="wfs:ResolveValueType" default="none"/>
        ResolveMode resolve = null;
        String resolveString = getNodeAsString( element, new XPath( "@resolve", nsContext ), null );
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
        String resolveDepth = getNodeAsString( element, new XPath( "@resolveDepth", nsContext ), null );

        // <xsd:attribute name="resolveTimeout" type="xsd:positiveInteger" default="300"/>
        BigInteger resolveTimeout = getNodeAsBigInt( element, new XPath( "@resolveTimeout", nsContext ), null );

        return new ResolveParams( resolve, resolveDepth, resolveTimeout );
    }

    /**
     * Parses a <code>fes:AbstractQueryExpression</code> element (in the context of a WFS 2.0.0 XML request).
     * 
     * @param queryEl
     *            element substitutable for <code>fes:AbstractQueryExpression</code>, must not be <code>null</code>
     * @return parsed {@link Query}, never <code>null</code>
     * @throws OWSException
     */
    public Query parseAbstractQuery200( OMElement queryEl )
                            throws OWSException {
        QName elName = queryEl.getQName();
        if ( new QName( WFS_200_NS, "Query" ).equals( elName ) ) {
            return parseAdHocQuery200( queryEl );
        } else if ( new QName( WFS_200_NS, "StoredQuery" ).equals( elName ) ) {
            return parseStoredQuery200( queryEl );
        } else if ( new QName( WFS_TE_10_NS, "DynamicFeatureQuery" ).equals( elName ) ) {
            return parseDynamicFeatureQueryTe100( queryEl );
        }
        String msg = "Unsupported query expression element '" + elName + "'.";
        throw new XMLParsingException( this, queryEl, msg );
    }

    // <xsd:element name="Query" type="wfs:QueryType" substitutionGroup="fes:AbstractAdhocQueryExpression"/>
    private Query parseAdHocQuery200( OMElement queryEl )
                            throws OWSException {

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = getNodeAsString( queryEl, new XPath( "@handle", nsContext ), null );

        // <xsd:attribute name="aliases" type="fes:AliasesType"/>
        String[] aliases = null;
        String aliasesStr = getNodeAsString( queryEl, new XPath( "@aliases", nsContext ), null );
        if ( aliasesStr != null ) {
            aliases = StringUtils.split( aliasesStr, " " );
        }

        // <xsd:attribute name="typeNames" type="fes:TypeNamesListType" use="required"/>
        String typeNameStr = getRequiredNodeAsString( queryEl, new XPath( "@typeNames", nsContext ) );
        String[] tokens = StringUtils.split( typeNameStr, " " );
        if ( aliases != null && aliases.length != tokens.length ) {
            String msg = "Number of entries in 'aliases' and 'typeNames' attributes does not match.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "aliases" );
        }
        TypeName[] typeNames = new TypeName[tokens.length];
        for ( int i = 0; i < tokens.length; i++ ) {
            String alias = aliases != null ? aliases[i] : null;
            String token = tokens[i];
            if ( token.startsWith( "schema-element(" ) && token.endsWith( ")" ) ) {
                String prefixedName = token.substring( 15, token.length() - 1 );
                QName qName = resolveQName( queryEl, prefixedName );
                typeNames[i] = new TypeName( qName, alias, true );
            } else {
                QName qName = resolveQName( queryEl, token );
                typeNames[i] = new TypeName( qName, alias, false );
            }
        }

        // <xsd:attribute name="srsName" type="xsd:anyURI"/>
        ICRS crs = null;
        String srsName = getNodeAsString( queryEl, new XPath( "@srsName", nsContext ), null );
        if ( srsName != null ) {
            try {
                CRSRef ref = CRSManager.getCRSRef( srsName );
                ref.getReferencedObject(); // test if referenced object exists
                crs = ref; // use reference as that prefers the requested name/code/alias
            } catch ( ReferenceResolvingException e ) {
                throw new InvalidParameterValueException( e.getMessage(), "srsName" );
            }
        }

        // <xsd:attribute name="featureVersion" type="xsd:string"/>
        String featureVersion = getNodeAsString( queryEl, new XPath( "@featureVersion", nsContext ), null );

        // <xsd:element ref="fes:AbstractProjectionClause" minOccurs="0" maxOccurs="unbounded"/>
        List<OMElement> propertyNameEls = getElements( queryEl, new XPath( "wfs200:PropertyName", nsContext ) );
        List<PropertyName> projectionClauses = new ArrayList<PropertyName>( propertyNameEls.size() );
        for ( OMElement propertyNameEl : propertyNameEls ) {
            PropertyName propName = parsePropertyName200( propertyNameEl );
            projectionClauses.add( propName );
        }

        // <xsd:element ref="fes:AbstractSelectionClause" minOccurs="0"/>
        Filter filter = null;
        OMElement filterEl = queryEl.getFirstChildWithName( new QName( FES_20_NS, "Filter" ) );
        if ( filterEl != null ) {
            filter = parseFilter200( filterEl );
        }

        // <xsd:element ref="fes:AbstractSortingClause" minOccurs="0"/>
        List<SortProperty> sortProps = new ArrayList<SortProperty>();
        // <xsd:element name="SortBy" type="fes:SortByType" substitutionGroup="fes:AbstractSortingClause"/>
        OMElement sortByEl = getElement( queryEl, new XPath( "fes:SortBy", nsContext ) );
        if ( sortByEl != null ) {
            List<OMElement> sortPropertyEls = getRequiredElements( sortByEl, new XPath( "fes:SortProperty", nsContext ) );
            for ( OMElement sortPropertyEl : sortPropertyEls ) {
                OMElement propNameEl = getRequiredElement( sortPropertyEl, new XPath( "fes:ValueReference", nsContext ) );
                ValueReference valRef = new ValueReference( propNameEl.getText(), getNamespaceContext( propNameEl ) );
                String sortOrder = getNodeAsString( sortPropertyEl, new XPath( "fes:SortOrder", nsContext ), "ASC" );
                SortProperty sortProp = new SortProperty( valRef, sortOrder.equals( "ASC" ) );
                sortProps.add( sortProp );
            }
        }

        PropertyName[] projection = projectionClauses.toArray( new PropertyName[projectionClauses.size()] );
        SortProperty[] sortPropsArray = sortProps.toArray( new SortProperty[sortProps.size()] );

        return new FilterQuery( handle, typeNames, featureVersion, crs, projection, sortPropsArray, filter );
    }

    private PropertyName parsePropertyName200( OMElement propertyNameEl ) {
        ResolveParams resolveParams = parseStandardResolveParameters200( propertyNameEl );
        ValueReference resolvePath = null;
        String resolvePathStr = propertyNameEl.getAttributeValue( new QName( "resolvePath" ) );
        NamespaceBindings propNameNsContext = getNamespaceContext( propertyNameEl );
        if ( resolvePathStr != null ) {
            resolvePath = new ValueReference( resolvePathStr, propNameNsContext );
        }
        ValueReference propName = new ValueReference( propertyNameEl.getText(), propNameNsContext );
        return new PropertyName( propName, resolveParams, resolvePath );
    }

    // <element name="DynamicFeatureQuery" type="wfs-te:DynamicFeatureQueryType" substitutionGroup="wfs:Query"/>
    private Query parseDynamicFeatureQueryTe100( OMElement queryEl )
                            throws OWSException {

        // <xsd:attribute name="handle" type="xsd:string"/>
        String handle = getNodeAsString( queryEl, new XPath( "@handle", nsContext ), null );

        // <xsd:attribute name="aliases" type="fes:AliasesType"/>
        String[] aliases = null;
        String aliasesStr = getNodeAsString( queryEl, new XPath( "@aliases", nsContext ), null );
        if ( aliasesStr != null ) {
            aliases = StringUtils.split( aliasesStr, " " );
        }

        // <xsd:attribute name="typeNames" type="fes:TypeNamesListType" use="required"/>
        String typeNameStr = getRequiredNodeAsString( queryEl, new XPath( "@typeNames", nsContext ) );
        String[] tokens = StringUtils.split( typeNameStr, " " );
        if ( aliases != null && aliases.length != tokens.length ) {
            String msg = "Number of entries in 'aliases' and 'typeNames' attributes does not match.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "aliases" );
        }
        TypeName[] typeNames = new TypeName[tokens.length];
        for ( int i = 0; i < tokens.length; i++ ) {
            String alias = aliases != null ? aliases[i] : null;
            String token = tokens[i];
            if ( token.startsWith( "schema-element(" ) && token.endsWith( ")" ) ) {
                String prefixedName = token.substring( 15, token.length() - 1 );
                QName qName = resolveQName( queryEl, prefixedName );
                typeNames[i] = new TypeName( qName, alias, true );
            } else {
                QName qName = resolveQName( queryEl, token );
                typeNames[i] = new TypeName( qName, alias, false );
            }
        }

        // <xsd:attribute name="srsName" type="xsd:anyURI"/>
        ICRS crs = null;
        String srsName = getNodeAsString( queryEl, new XPath( "@srsName", nsContext ), null );
        if ( srsName != null ) {
            crs = CRSManager.getCRSRef( srsName );
        }

        // <xsd:attribute name="featureVersion" type="xsd:string"/>
        String featureVersion = getNodeAsString( queryEl, new XPath( "@featureVersion", nsContext ), null );

        // <xsd:element ref="fes:AbstractProjectionClause" minOccurs="0" maxOccurs="unbounded"/>
        List<ProjectionClause> projectionClauses = new ArrayList<ProjectionClause>();
        List<OMElement> propertyNameEls = getElements( queryEl, new XPath( "wfs200:PropertyName", nsContext ) );
        for ( OMElement propertyNameEl : propertyNameEls ) {
            PropertyName propName = parsePropertyName200( propertyNameEl );
            projectionClauses.add( propName );
        }
        List<OMElement> timeSliceProjectionEls = getElements( queryEl, new XPath( "fes-te:TimeSliceProjection",
                                                                                  nsContext ) );
        for ( OMElement timeSliceProjectionEl : timeSliceProjectionEls ) {
            TimeSliceProjection timeSliceProjection = parseTimeSliceProjectionTe100( timeSliceProjectionEl );
            projectionClauses.add( timeSliceProjection );
        }

        // <xsd:element ref="fes:AbstractSelectionClause" minOccurs="0"/>
        Filter filter = null;
        OMElement filterEl = queryEl.getFirstChildWithName( new QName( FES_20_NS, "Filter" ) );
        if ( filterEl != null ) {
            filter = parseFilter200( filterEl );
        }

        // <xsd:element ref="fes:AbstractSortingClause" minOccurs="0"/>
        List<SortProperty> sortProps = new ArrayList<SortProperty>();
        // <xsd:element name="SortBy" type="fes:SortByType" substitutionGroup="fes:AbstractSortingClause"/>
        OMElement sortByEl = getElement( queryEl, new XPath( "fes:SortBy", nsContext ) );
        if ( sortByEl != null ) {
            List<OMElement> sortPropertyEls = getRequiredElements( sortByEl, new XPath( "fes:SortProperty", nsContext ) );
            for ( OMElement sortPropertyEl : sortPropertyEls ) {
                OMElement propNameEl = getRequiredElement( sortPropertyEl, new XPath( "fes:ValueReference", nsContext ) );
                ValueReference valRef = new ValueReference( propNameEl.getText(), getNamespaceContext( propNameEl ) );
                String sortOrder = getNodeAsString( sortPropertyEl, new XPath( "fes:SortOrder", nsContext ), "ASC" );
                SortProperty sortProp = new SortProperty( valRef, sortOrder.equals( "ASC" ) );
                sortProps.add( sortProp );
            }
        }

        ProjectionClause[] projections = projectionClauses.toArray( new ProjectionClause[projectionClauses.size()] );
        SortProperty[] sortPropsArray = sortProps.toArray( new SortProperty[sortProps.size()] );

        return new FilterQuery( handle, typeNames, featureVersion, crs, projections, sortPropsArray, filter );
    }

    private Filter parseFilter200( OMElement filterEl ) {
        Filter filter = null;
        try {
            // TODO remove usage of wrapper (necessary at the moment to work around problems with AXIOM's
            // XMLStreamReader)
            XMLStreamReader xmlStream = new XMLStreamReaderWrapper( filterEl.getXMLStreamReaderWithoutCaching(), null );
            // skip START_DOCUMENT
            xmlStream.nextTag();
            filter = Filter200XMLDecoder.parse( xmlStream );
        } catch ( XMLStreamException e ) {
            e.printStackTrace();
            throw new XMLParsingException( this, filterEl, e.getMessage() );
        }
        return filter;
    }

    // <element name="TimeSliceProjection" type="fes-te:TimeSliceProjectionType"
    // substitutionGroup="fes:AbstractProjectionClause"/>
    private TimeSliceProjection parseTimeSliceProjectionTe100( OMElement timeSliceProjectionEl ) {

        // OMElement relevantTimeEl = getElement( timeSliceProjectionEl, new XPath( "fes-te:relevantTime", nsContext )
        // );

        Filter filter = null;
        OMElement timeSliceFilterEl = getElement( timeSliceProjectionEl,
                                                  new XPath( "fes-te:timeSliceFilter", nsContext ) );
        if ( timeSliceFilterEl != null ) {
            OMElement filterEl = timeSliceFilterEl.getFirstChildWithName( new QName( FES_20_NS, "Filter" ) );
            if ( filterEl != null ) {
                filter = parseFilter200( filterEl );
            }
        }

        return new TimeSliceProjection( filter );
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
            paramToValue.put( paramName.toUpperCase(), paramEl );
        }
        return new StoredQuery( handle, id, paramToValue );
    }

    private static QName resolveQName( OMElement context, String name ) {
        QName qName = null;
        int colonIdx = name.indexOf( ":" );
        if ( colonIdx != -1 ) {
            qName = context.resolveQName( name );
            if ( qName == null ) {
                // AXIOM appears to return null for context.resolveQName( name ) for unbound prefices!?
                String prefix = name.substring( 0, colonIdx );
                String localPart = name.substring( colonIdx + 1 );
                qName = new QName( "", localPart, prefix );
            }
        } else {
            qName = new QName( name );
        }
        return qName;
    }
}
