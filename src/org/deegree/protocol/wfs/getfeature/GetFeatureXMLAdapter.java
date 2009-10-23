//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wfs.getfeature;

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.types.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.Function;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.WFSConstants;

/**
 * Adapter between XML <code>GetFeature</code> requests and {@link GetFeature} objects.
 * <p>
 * TODO code for exporting to XML
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetFeatureXMLAdapter extends AbstractWFSRequestXMLAdapter {

    /**
     * Parses a WFS <code>GetFeature</code> document into a {@link GetFeature} object.
     * <p>
     * Supported versions:
     * <ul>
     * <li>WFS 1.0.0</li>
     * <li>WFS 1.1.0</li>
     * </ul>
     * 
     * @param version
     *            version of the request, may be <code>null</code> (in that case, a version attribute must be present in
     *            the root element)
     * @return parsed {@link GetFeature} request
     * @throws Exception
     * @throws XMLParsingException
     *             if a syntax error occurs in the XML
     * @throws MissingParameterException
     *             if the request version is unsupported
     * @throws InvalidParameterValueException
     *             if a parameter contains a syntax error
     */
    public GetFeature parse( Version version )
                            throws Exception {

        if ( version == null ) {
            version = Version.parseVersion( getNodeAsString( rootElement, new XPath( "@version", nsContext ), null ) );
        }

        GetFeature result = null;

        if ( VERSION_100.equals( version ) )
            result = parse100();
        else if ( VERSION_110.equals( version ) )
            result = parse110();
        else {
            throw new Exception(
                                 "Version "
                                                         + version
                                                         + " is not supported for parsing (for now). Only 1.0.0 and 1.1.0 versions are supported." );
        }

        return result;
    }

    /**
     * Parses a WFS 1.0.0 <code>GetFeature</code> document into a {@link GetFeature} object.
     * 
     * @return a GetFeature instance
     */
    @SuppressWarnings("boxing")
    public GetFeature parse100() {

        String handle = getNodeAsString( rootElement, new XPath( "@handle", nsContext ), null );

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

        String maxFeaturesStr = getNodeAsString( rootElement, new XPath( "@maxFeatures", nsContext ), null );
        Integer maxFeatures = null;
        if ( maxFeaturesStr != null ) {
            maxFeatures = Integer.parseInt( maxFeaturesStr );
        }

        List<OMElement> queryElements = getRequiredElements( rootElement, new XPath( "*", nsContext ) );
        // check if all child elements are 'wfs:Query' elements (required for CITE)
        for ( OMElement omElement : queryElements ) {
            if ( !new QName( WFSConstants.WFS_NS, "Query" ).equals( omElement.getQName() ) ) {
                String msg = "Child element '" + omElement.getQName() + "' is not allowed.";
                throw new XMLParsingException( this, omElement, msg );
            }
        }

        List<Query> queries = new ArrayList<Query>();

        for ( OMElement queryEl : queryElements ) {
            List<PropertyName> propNames = new ArrayList<PropertyName>();
            List<OMElement> propertyNameElements = getElements( queryEl, new XPath( "ogc:PropertyName", nsContext ) );

            for ( OMElement propertyNameEl : propertyNameElements ) {
                PropertyName propertyName = new PropertyName( propertyNameEl.getText(),
                                                              getNamespaceContext( propertyNameEl ) );
                propNames.add( propertyName );
            }

            Filter filter = null;
            OMElement filterEl = queryEl.getFirstChildWithName( new QName( OGCNS, "Filter" ) );
            if ( filterEl != null ) {
                try {
                    // TODO remove usage of wrapper (necessary at the moment to work around problems with AXIOM's
                    // XMLStreamReader)
                    XMLStreamReader xmlStream = new XMLStreamReaderWrapper(
                                                                            filterEl.getXMLStreamReaderWithoutCaching(),
                                                                            null );
                    // skip START_DOCUMENT
                    xmlStream.nextTag();

                    filter = Filter100XMLDecoder.parse( xmlStream );
                } catch ( XMLStreamException e ) {
                    e.printStackTrace();
                    throw new XMLParsingException( this, filterEl, e.getMessage() );
                }
            }

            String queryHandle = getNodeAsString( queryEl, new XPath( "@handle", nsContext ), null );

            String typeNameStr = getRequiredNodeAsString( queryEl, new XPath( "@typeName", nsContext ) );
            TypeName[] typeNames = TypeName.valuesOf( queryEl, typeNameStr );

            String featureVersion = getNodeAsString( queryEl, new XPath( "@featureVersion", nsContext ), null );

            // convert some lists to arrays to conform the FilterQuery constructor signature
            PropertyName[] propNamesArray = new PropertyName[propNames.size()];
            propNames.toArray( propNamesArray );

            // build Query
            Query filterQuery = new FilterQuery( queryHandle, typeNames, featureVersion, null, propNamesArray, null,
                                                 null, null, filter );
            queries.add( filterQuery );
        }

        Query[] queryArray = new FilterQuery[queries.size()];
        queries.toArray( queryArray );

        return new GetFeature( VERSION_100, handle, resultType, outputFormat, maxFeatures, null, null, queryArray );
    }

    /**
     * Parses a WFS 1.1.0 <code>GetFeature</code> document into a {@link GetFeature} object.
     * 
     * @return a GetFeature instance
     */
    public GetFeature parse110() {

        String handle = getNodeAsString( rootElement, new XPath( "@handle", nsContext ), null );

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

        String maxFeaturesStr = getNodeAsString( rootElement, new XPath( "@maxFeatures", nsContext ), null );
        Integer maxFeatures = null;
        if ( maxFeaturesStr != null ) {
            maxFeatures = Integer.parseInt( maxFeaturesStr );
        }

        String traverseXlinkDepth = getNodeAsString( rootElement, new XPath( "@traverseXlinkDepth", nsContext ), null );

        String traverseXlinkExpiryStr = getNodeAsString( rootElement, new XPath( "@traverseXlinkExpiry", nsContext ),
                                                         null );
        Integer traverseXlinkExpiry = null;
        if ( traverseXlinkExpiryStr != null ) {
            traverseXlinkExpiry = Integer.parseInt( traverseXlinkExpiryStr );
        }

        List<OMElement> queryElements = getRequiredElements( rootElement, new XPath( "*", nsContext ) );
        // check if all child elements are 'wfs:Query' elements (required for CITE)
        for ( OMElement omElement : queryElements ) {
            if ( !new QName( WFSConstants.WFS_NS, "Query" ).equals( omElement.getQName() ) ) {
                String msg = "Child element '" + omElement.getQName() + "' is not allowed.";
                throw new XMLParsingException( this, omElement, msg );
            }
        }

        List<Query> queries = new ArrayList<Query>();

        for ( OMElement queryEl : queryElements ) {
            List<PropertyName> propNames = new ArrayList<PropertyName>();
            List<OMElement> propertyNameElements = getElements( queryEl, new XPath( "wfs:PropertyName", nsContext ) );
            for ( OMElement propertyNameEl : propertyNameElements ) {
                PropertyName propertyName = new PropertyName( propertyNameEl.getText(),
                                                              getNamespaceContext( propertyNameEl ) );
                propNames.add( propertyName );
            }

            List<XLinkPropertyName> xlinkPropNames = new ArrayList<XLinkPropertyName>();
            List<OMElement> xlinkPropertyElements = getElements( queryEl,
                                                                 new XPath( "wfs:XlinkPropertyName", nsContext ) );
            for ( OMElement xlinkPropertyEl : xlinkPropertyElements ) {
                PropertyName xlinkProperty = new PropertyName( xlinkPropertyEl.getText(),
                                                               getNamespaceContext( xlinkPropertyEl ) );
                String xlinkDepth = getRequiredNodeAsString( xlinkPropertyEl, new XPath( "@traverseXlinkDepth",
                                                                                         nsContext ) );
                String xlinkExpiry = getNodeAsString( xlinkPropertyEl, new XPath( "@traverseXlinkExpiry", nsContext ),
                                                      null );
                Integer xlinkExpiryInt = null;
                try {
                    if ( xlinkExpiry != null )
                        xlinkExpiryInt = Integer.parseInt( xlinkExpiry );
                } catch ( NumberFormatException e ) {
                    // TODO string provided as time in minutes is not an integer
                }
                XLinkPropertyName xlinkPropName = new XLinkPropertyName( xlinkProperty, xlinkDepth, xlinkExpiryInt );
                xlinkPropNames.add( xlinkPropName );
            }

            List<Function> functions = new ArrayList<Function>();
            List<OMElement> functionElements = getElements( queryEl, new XPath( "ogc:Function", nsContext ) );
            for ( OMElement functionEl : functionElements ) {
                try {
                    XMLStreamReaderWrapper xmlStream = new XMLStreamReaderWrapper(
                                                                                   functionEl.getXMLStreamReaderWithoutCaching(),
                                                                                   getSystemId() );
                    // skip START_DOCUMENT
                    xmlStream.nextTag();
                    Function function = Filter110XMLDecoder.parseFunction( xmlStream );
                    functions.add( function );
                } catch ( XMLStreamException e ) {
                    throw new XMLParsingException( this, functionEl, e.getMessage() );
                }
            }

            Filter filter = null;
            OMElement filterEl = queryEl.getFirstChildWithName( new QName( OGCNS, "Filter" ) );
            if ( filterEl != null ) {
                try {
                    // TODO remove usage of wrapper (necessary at the moment to work around problems with AXIOM's
                    // XMLStreamReader)
                    XMLStreamReader xmlStream = new XMLStreamReaderWrapper(
                                                                            filterEl.getXMLStreamReaderWithoutCaching(),
                                                                            null );
                    // skip START_DOCUMENT
                    xmlStream.nextTag();
                    filter = Filter110XMLDecoder.parse( xmlStream );
                } catch ( XMLStreamException e ) {
                    e.printStackTrace();
                    throw new XMLParsingException( this, filterEl, e.getMessage() );
                }
            }

            List<SortProperty> sortProps = new ArrayList<SortProperty>();
            OMElement sortByEl = getElement( queryEl, new XPath( "ogc:SortBy", nsContext ) );
            if ( sortByEl != null ) {
                List<OMElement> sortPropertyElements = getRequiredElements( sortByEl, new XPath( "ogc:SortProperty",
                                                                                                 nsContext ) );
                for ( OMElement sortPropertyEl : sortPropertyElements ) {
                    OMElement propNameEl = getRequiredElement( sortPropertyEl,
                                                               new XPath( "ogc:PropertyName", nsContext ) );
                    String sortOrder = getNodeAsString( sortPropertyEl, new XPath( "ogc:SortOrder", nsContext ), "ASC" );
                    SortProperty sortProp = new SortProperty( new PropertyName( propNameEl.getText(),
                                                                                getNamespaceContext( propNameEl ) ),
                                                              sortOrder.equals( "ASC" ) );
                    sortProps.add( sortProp );
                }
            }

            String queryHandle = getNodeAsString( queryEl, new XPath( "@handle", nsContext ), null );

            String typeNameStr = getRequiredNodeAsString( queryEl, new XPath( "@typeName", nsContext ) );
            TypeName[] typeNames = TypeName.valuesOf( queryEl, typeNameStr );

            String featureVersion = getNodeAsString( queryEl, new XPath( "@featureVersion", nsContext ), null );

            CRS crs = null;
            String srsName = getNodeAsString( queryEl, new XPath( "@srsName", nsContext ), null );
            if ( srsName != null ) {
                crs = new CRS( srsName );
            }

            // convert some lists to arrays to conform the FilterQuery constructor signature
            PropertyName[] propNamesArray = new PropertyName[propNames.size()];
            propNames.toArray( propNamesArray );

            XLinkPropertyName[] xlinkPropNamesArray = new XLinkPropertyName[xlinkPropNames.size()];
            xlinkPropNames.toArray( xlinkPropNamesArray );

            Function[] functionsArray = new Function[functions.size()];
            functions.toArray( functionsArray );

            SortProperty[] sortPropsArray = new SortProperty[sortProps.size()];
            sortProps.toArray( sortPropsArray );

            // build Query
            Query filterQuery = new FilterQuery( queryHandle, typeNames, featureVersion, crs, propNamesArray,
                                                 xlinkPropNamesArray, functionsArray, sortPropsArray, filter );
            queries.add( filterQuery );
        }

        Query[] queryArray = new FilterQuery[queries.size()];
        queries.toArray( queryArray );

        return new GetFeature( VERSION_110, handle, resultType, outputFormat, maxFeatures, traverseXlinkDepth,
                               traverseXlinkExpiry, queryArray );
    }
}
