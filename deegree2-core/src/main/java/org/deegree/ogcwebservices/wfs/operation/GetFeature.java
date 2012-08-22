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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.WFService;
import org.w3c.dom.Element;

/**
 * Represents a <code>GetFeature</code> request to a web feature service.
 * <p>
 * The GetFeature operation allows the retrieval of features from a web feature service. A GetFeature request is
 * processed by a WFS and when the value of the outputFormat attribute is set to text/gml; subtype=gml/3.1.1, a GML
 * instance document, containing the result set, is returned to the client.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetFeature extends AbstractWFSRequest {

    private static final ILogger LOG = LoggerFactory.getLogger( GetFeature.class );

    private static final long serialVersionUID = 8885456550385433051L;

    /** Serialized java object format (deegree specific extension) * */
    public static final String FORMAT_FEATURECOLLECTION = "FEATURECOLLECTION";

    /**
     * Known result types.
     */
    public static enum RESULT_TYPE {

        /** A full response should be generated. */
        RESULTS,

        /** Only a count of the number of features should be returned. */
        HITS
    }

    protected RESULT_TYPE resultType = RESULT_TYPE.RESULTS;

    protected String outputFormat;

    protected int maxFeatures;

    private int traverseXLinkDepth;

    private int traverseXLinkExpiry;

    protected List<Query> queries;

    // deegree specific extension, default: 1 (start at first feature)
    protected int startPosition;

    /**
     * Creates a new <code>GetFeature</code> instance.
     *
     * @param version
     *            request version
     * @param id
     *            id of the request
     * @param handle
     * @param resultType
     *            desired result type (results | hits)
     * @param outputFormat
     *            requested result format
     * @param maxFeatures
     * @param startPosition
     *            deegree specific parameter defining where to start considering features
     * @param traverseXLinkDepth
     *            indicates the depth to which nested property XLink linking element locator attribute (href) XLinks are
     *            traversed and resolved if possible (not implemented yet, use -1 as default)
     * @param traverseXLinkExpiry
     *            indicates how long a Web Feature Service should wait to receive a response to a nested GetGmlObject
     *            request (not implemented yet, use 0 as default)
     * @param queries
     * @param vendorSpecificParam
     */
    GetFeature( String version, String id, String handle, RESULT_TYPE resultType, String outputFormat, int maxFeatures,
                int startPosition, int traverseXLinkDepth, int traverseXLinkExpiry, Query[] queries,
                Map<String, String> vendorSpecificParam ) {
        super( version, id, handle, vendorSpecificParam );
        this.setQueries( queries );
        this.outputFormat = outputFormat;
        this.maxFeatures = maxFeatures;
        this.startPosition = startPosition;
        this.resultType = resultType;
        this.traverseXLinkDepth = traverseXLinkDepth;
        this.traverseXLinkExpiry = traverseXLinkExpiry;
    }

    /**
     * Creates an uninitialized {@link GetFeature} instance.
     */
    protected GetFeature() {
        super( null, null, null, null );
    }

    /**
     * Creates a new <code>GetFeature</code> instance from the given parameters.
     *
     * @param version
     *            request version
     * @param id
     *            id of the request
     * @param resultType
     *            desired result type (results | hits)
     * @param outputFormat
     *            requested result format
     * @param handle
     * @param maxFeatures
     *            default = -1 (all features)
     * @param startPosition
     *            default = 0 (starting at the first feature)
     * @param traverseXLinkDepth
     * @param traverseXLinkExpiry
     * @param queries
     *            a set of Query objects that describes the query to perform
     * @return new <code>GetFeature</code> request
     */
    public static GetFeature create( String version, String id, RESULT_TYPE resultType, String outputFormat,
                                     String handle, int maxFeatures, int startPosition, int traverseXLinkDepth,
                                     int traverseXLinkExpiry, Query[] queries ) {
        return new GetFeature( version, id, handle, resultType, outputFormat, maxFeatures, startPosition,
                               traverseXLinkDepth, traverseXLinkExpiry, queries, null );
    }

    /**
     * Creates a new <code>GetFeature</code> instance from a document that contains the DOM representation of the
     * request.
     *
     * @param id
     *            of the request
     * @param root
     *            element that contains the DOM representation of the request
     * @return new <code>GetFeature</code> request
     * @throws OGCWebServiceException
     */
    public static GetFeature create( String id, Element root )
                            throws OGCWebServiceException {
        GetFeatureDocument doc = new GetFeatureDocument();
        doc.setRootElement( root );
        GetFeature request;
        try {
            request = doc.parse( id );
        } catch ( InvalidParameterValueException e ) {
            throw e;
        } catch ( XMLParsingException e ) {
            // have to wrap it, to set exception code
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( "GetFeature", e.getMessage() );
        }
        return request;
    }

    /**
     * Creates a new <code>GetFeature</code> instance from the given key-value pair encoded request.
     *
     * @param id
     *            request identifier
     * @param request
     * @return new <code>GetFeature</code> request
     * @throws InvalidParameterValueException
     * @throws InconsistentRequestException
     * @throws MissingParameterValueException
     */
    public static GetFeature create( String id, String request )
                            throws InconsistentRequestException, InvalidParameterValueException,
                            MissingParameterValueException {
        Map<String, String> map = KVP2Map.toMap( request );
        map.put( "ID", id );
        return create( map );
    }

    /**
     * Creates a new <code>GetFeature</code> request from the given map.
     *
     * @param kvp
     *            key-value pairs, keys have to be uppercase
     * @return new <code>GetFeature</code> request
     * @throws InvalidParameterValueException
     * @throws InconsistentRequestException
     * @throws MissingParameterValueException
     */
    public static GetFeature create( Map<String, String> kvp )
                            throws InconsistentRequestException, InvalidParameterValueException,
                            MissingParameterValueException {

        // SERVICE
        checkServiceParameter( kvp );

        // ID (deegree specific)
        String id = kvp.get( "ID" );

        // VERSION
        String version = checkVersionParameter( kvp );

        boolean is100 = version.equals( "1.0.0" );

        // OUTPUTFORMAT
        String outputFormat = getParam( "OUTPUTFORMAT", kvp, is100 ? FORMAT_GML2_WFS100 : FORMAT_GML3 );

        // RESULTTYPE
        RESULT_TYPE resultType = RESULT_TYPE.RESULTS;
        String resultTypeString = kvp.get( "RESULTTYPE" );
        if ( "hits".equals( resultTypeString ) ) {
            resultType = RESULT_TYPE.HITS;
        }

        // FEATUREVERSION
        String featureVersion = kvp.get( "FEATUREVERSION" );

        // MAXFEATURES
        String maxFeaturesString = kvp.get( "MAXFEATURES" );
        // -1: fetch all features
        int maxFeatures = -1;
        if ( maxFeaturesString != null ) {
            try {
                maxFeatures = Integer.parseInt( maxFeaturesString );
                if ( maxFeatures < 1 ) {
                    throw new NumberFormatException();
                }
            } catch ( NumberFormatException e ) {
                LOG.logError( e.getMessage(), e );
                String msg = Messages.getMessage( "WFS_PARAMETER_INVALID_INT", maxFeaturesString, "MAXFEATURES" );
                throw new InvalidParameterValueException( msg );
            }
        }

        // STARTPOSITION (deegree specific)
        String startPosString = getParam( "STARTPOSITION", kvp, "1" );
        int startPosition = 1;
        try {
            startPosition = Integer.parseInt( startPosString );
            if ( startPosition < 1 ) {
                throw new NumberFormatException();
            }
        } catch ( NumberFormatException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "WFS_PARAMETER_INVALID_INT", startPosString, "STARTPOSITION" );
            throw new InvalidParameterValueException( msg );
        }

        // SRSNAME
        String srsName = kvp.get( "SRSNAME" );

        // SORTBY
        SortProperty[] sortProperties = SortProperty.create( kvp.get( "SORTBY" ),
                                                             extractNamespaceParameter( kvp ).getNamespaceMap() );

        // TRAVERSEXLINKDEPTH
        int traverseXLinkDepth = -1;

        // TRAVERSEXLINKEXPIRY
        int traverseXLinkExpiry = -1;

        Map<QualifiedName, Filter> filterMap = null;

        // TYPENAME
        QualifiedName[] typeNames = extractTypeNames( kvp );
        if ( typeNames.length == 0 ) {
            // check if FEATUREID is present
            String featureId = kvp.get( "FEATUREID" );
            if ( featureId != null ) {
                // no TYPENAME parameter -> request needs to be augmented later (with configuration)
                return new AugmentableGetFeature( version, id, null, resultType, outputFormat, maxFeatures,
                                                  startPosition, traverseXLinkDepth, traverseXLinkExpiry, new Query[0],
                                                  kvp );
            }
            String msg = Messages.getMessage( "WFS_TYPENAME+FID_PARAMS_MISSING" );
            throw new InvalidParameterValueException( msg );
        }

        // check if FEATUREID is present
        String featureId = kvp.get( "FEATUREID" );
        if ( featureId != null ) {
            String[] featureIds = featureId.split( "," );
            if ( typeNames.length != 1 && featureIds.length != typeNames.length ) {
                String msg = Messages.getMessage( "WFS_TYPENAME+FID_COUNT_MISMATCH", typeNames.length,
                                                  featureIds.length );
                throw new InvalidParameterValueException( msg );
            } else if ( typeNames.length == 1 ) {
                // build one filter
                ArrayList<FeatureId> fids = new ArrayList<FeatureId>( featureIds.length );
                for ( String fid : featureIds ) {
                    fids.add( new FeatureId( fid ) );
                }
                Filter filter = new FeatureFilter( fids );
                filterMap = new HashMap<QualifiedName, Filter>();
                filterMap.put( typeNames[0], filter );
            } else {
                throw new InvalidParameterValueException(
                                                          "Usage of FEATUREID with multiple TYPENAME values is not supported yet." );
            }
        }

        // BBOX
        Filter bboxFilter = extractBBOXFilter( kvp );

        // FILTER (mutually exclusive with FEATUREID or BBOX, prequisite: TYPENAME)
        if ( filterMap != null || bboxFilter != null ) {
            if ( kvp.containsKey( "FILTER" ) ) {
                String msg = Messages.getMessage( "WFS_GET_FEATURE_FEATUREID_BBOX_AND_FILTER" );
                throw new InvalidParameterValueException( msg );
            }
        } else {
            filterMap = extractFilters( kvp, typeNames );
        }

        // PROPERTYNAME
        Map<QualifiedName, PropertyPath[]> propertyNameMap = extractPropNames( kvp, typeNames );

        // build a Query instance for each requested feature type (later also for each featureid...)
        Query[] queries = new Query[typeNames.length];
        for ( int i = 0; i < queries.length; i++ ) {
            QualifiedName ftName = typeNames[i];
            PropertyPath[] properties = propertyNameMap.get( ftName );
            Filter filter;
            if ( filterMap != null ) {
                filter = filterMap.get( ftName );
            } else {
                filter = bboxFilter;
            }
            QualifiedName[] ftNames = new QualifiedName[] { ftName };
            queries[i] = new Query( properties, null, sortProperties, null, featureVersion, ftNames, null, srsName,
                                    filter, resultType, maxFeatures, startPosition );
        }

        // build a GetFeature request that contains all queries
        GetFeature request = new GetFeature( version, id, null, resultType, outputFormat, maxFeatures, startPosition,
                                             traverseXLinkDepth, traverseXLinkExpiry, queries, kvp );
        return request;
    }

    /**
     * Extracts the PROPERTYNAME parameter and assigns them to the requested type names.
     *
     * @param kvp
     * @param typeNames
     * @return map with the assignments of type names to property names
     * @throws InvalidParameterValueException
     */
    protected static Map<QualifiedName, PropertyPath[]> extractPropNames( Map<String, String> kvp,
                                                                          QualifiedName[] typeNames )
                            throws InvalidParameterValueException {
        Map<QualifiedName, PropertyPath[]> propMap = new HashMap<QualifiedName, PropertyPath[]>();
        String propNameString = kvp.get( "PROPERTYNAME" );
        if ( propNameString != null ) {
            String[] propNameLists = propNameString.split( "\\)" );
            if ( propNameLists.length != typeNames.length ) {
                String msg = Messages.getMessage( "WFS_PROPNAME_PARAM_WRONG_COUNT",
                                                  Integer.toString( propNameLists.length ),
                                                  Integer.toString( typeNames.length ) );
                throw new InvalidParameterValueException( msg );
            }
            NamespaceContext nsContext = extractNamespaceParameter( kvp );
            for ( int i = 0; i < propNameLists.length; i++ ) {
                String propNameList = propNameLists[i];
                if ( propNameList.startsWith( "(" ) ) {
                    propNameList = propNameList.substring( 1 );
                }
                String[] propNames = propNameList.split( "," );
                PropertyPath[] paths = new PropertyPath[propNames.length];
                for ( int j = 0; j < propNames.length; j++ ) {
                    PropertyPath path = transformToPropertyPath( propNames[j], nsContext );
                    paths[j] = ( path );
                }
                propMap.put( typeNames[i], paths );
            }
        }
        return propMap;
    }

    /**
     * Transforms the given property name to a (qualified) <code>PropertyPath</code> object by using the specified
     * namespace bindings.
     *
     * @param propName
     * @param nsContext
     * @return (qualified) <code>PropertyPath</code> object
     * @throws InvalidParameterValueException
     */
    private static PropertyPath transformToPropertyPath( String propName, NamespaceContext nsContext )
                            throws InvalidParameterValueException {
        String[] steps = propName.split( "/" );
        List<PropertyPathStep> propertyPathSteps = new ArrayList<PropertyPathStep>( steps.length );

        for ( int i = 0; i < steps.length; i++ ) {
            PropertyPathStep propertyStep = null;
            QualifiedName propertyName = null;
            String step = steps[i];
            boolean isAttribute = false;
            boolean isIndexed = false;
            int selectedIndex = -1;

            // check if step begins with '@' -> must be the final step then
            if ( step.startsWith( "@" ) ) {
                if ( i != steps.length - 1 ) {
                    String msg = "PropertyName '" + propName + "' is illegal: the attribute specifier may only "
                                 + "be used for the final step.";
                    throw new InvalidParameterValueException( msg );
                }
                step = step.substring( 1 );
                isAttribute = true;
            }

            // check if the step ends with brackets ([...])
            if ( step.endsWith( "]" ) ) {
                if ( isAttribute ) {
                    String msg = "PropertyName '" + propName
                                 + "' is illegal: if the attribute specifier ('@') is used, "
                                 + "index selection ('[...']) is not possible.";
                    throw new InvalidParameterValueException( msg );
                }
                int bracketPos = step.indexOf( '[' );
                if ( bracketPos < 0 ) {
                    String msg = "PropertyName '" + propName + "' is illegal. No opening brackets found for step '"
                                 + step + "'.";
                    throw new InvalidParameterValueException( msg );
                }
                try {
                    selectedIndex = Integer.parseInt( step.substring( bracketPos + 1, step.length() - 1 ) );
                } catch ( NumberFormatException e ) {
                    LOG.logError( e.getMessage(), e );
                    String msg = "PropertyName '" + propName + "' is illegal. Specified index '"
                                 + step.substring( bracketPos + 1, step.length() - 1 ) + "' is not a number.";
                    throw new InvalidParameterValueException( msg );
                }
                step = step.substring( 0, bracketPos );
                isIndexed = true;
            }

            // determine namespace prefix and binding (if any)
            int colonPos = step.indexOf( ':' );
            String prefix = "";
            String localName = step;
            if ( colonPos > 0 ) {
                prefix = step.substring( 0, colonPos );
                localName = step.substring( colonPos + 1 );
            }
            URI nsURI = nsContext.getURI( prefix );
            propertyName = new QualifiedName( prefix, localName, nsURI );

            if ( isAttribute ) {
                propertyStep = PropertyPathFactory.createAttributePropertyPathStep( propertyName );
            } else if ( isIndexed ) {
                propertyStep = PropertyPathFactory.createPropertyPathStep( propertyName, selectedIndex );
            } else {
                propertyStep = PropertyPathFactory.createPropertyPathStep( propertyName );
            }
            propertyPathSteps.add( propertyStep );
        }
        return PropertyPathFactory.createPropertyPath( propertyPathSteps );
    }

    /**
     * Returns the output format.
     * <p>
     * The outputFormat attribute defines the format to use to generate the result set. Vendor specific formats,
     * declared in the capabilities document are possible. The WFS-specs implies GML as default output format.
     *
     * @return the output format.
     */
    public String getOutputFormat() {
        return this.outputFormat;
    }

    /**
     * The query defines which feature type to query, what properties to retrieve and what constraints (spatial and
     * non-spatial) to apply to those properties.
     * <p>
     * only used for xml-coded requests
     *
     * @return contained queries
     */
    public Query[] getQuery() {
        return queries.toArray( new Query[queries.size()] );
    }

    /**
     * sets the <Query>
     *
     * @param query
     */
    public void setQueries( Query[] query ) {
        if ( query != null ) {
            this.queries = new ArrayList<Query>( query.length );
            for ( int i = 0; i < query.length; i++ ) {
                this.queries.add( query[i] );
            }
        } else {
            this.queries = new ArrayList<Query>();
        }
    }

    /**
     * The optional maxFeatures attribute can be used to limit the number of features that a GetFeature request
     * retrieves. Once the maxFeatures limit is reached, the result set is truncated at that point. If not limit is set
     * -1 will be returned.
     *
     * @return number of feature to fetch, -1 if no limit is set
     */
    public int getMaxFeatures() {
        return maxFeatures;
    }

    /**
     * @see #getMaxFeatures()
     * @param max
     */
    public void setMaxFeatures( int max ) {
        this.maxFeatures = max;
        for ( int i = 0; i < queries.size(); i++ ) {
            queries.get( i ).setMaxFeatures( max );
        }
    }

    /**
     * The startPosition parameter identifies the first result set entry to be returned specified the default is the
     * first record. If not startposition is set 0 will be returned
     *
     * @return the first result set entry to be returned
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * Returns the desired result type of the GetFeature operation. Possible values are 'results' and 'hits'.
     *
     * @return the desired result type
     */
    public RESULT_TYPE getResultType() {
        return this.resultType;
    }

    /**
     * The optional traverseXLinkDepth attribute indicates the depth to which nested property XLink linking element
     * locator attribute (href) XLinks in all properties of the selected feature(s) are traversed and resolved if
     * possible. A value of "1" indicates that one linking element locator attribute (href) XLink will be traversed and
     * the referenced element returned if possible, but nested property XLink linking element locator attribute (href)
     * XLinks in the returned element are not traversed. A value of "*" indicates that all nested property XLink linking
     * element locator attribute (href) XLinks will be traversed and the referenced elements returned if possible. The
     * range of valid values for this attribute consists of positive integers plus "*".
     *
     * @return the depth to which nested property XLinks are traversed and resolved
     */
    public int getTraverseXLinkDepth() {
        return traverseXLinkDepth;
    }

    /**
     * The traverseXLinkExpiry attribute is specified in minutes. It indicates how long a Web Feature Service should
     * wait to receive a response to a nested GetGmlObject request. If no traverseXLinkExpiry attribute is present for a
     * GetGmlObject request, the WFS wait time is implementation dependent.
     *
     * @return how long to wait to receive a response to a nested GetGmlObject request
     */
    public int getTraverseXLinkExpiry() {
        return traverseXLinkExpiry;
    }

    /**
     * Adds missing namespaces in the names of requested feature types.
     * <p>
     * If the {@link QualifiedName} of a requested type has a null namespace, the first qualified feature type name of
     * the given {@link WFService} with the same local name is used instead.
     * <p>
     * Note: The method changes this request (the feature type names) and should only be called by the
     * <code>WFSHandler</code> class.
     *
     * @param wfs
     *            {@link WFService} instance that is used for the lookup of proper (qualified) feature type names
     */
    public void guessMissingTypeNameNamespaces( WFService wfs ) {
        for ( Query query : queries ) {
            query.guessMissingTypeNameNamespace( wfs );
        }
    }

    /**
     * Adds missing namespaces to requested feature type names, property names, filter properties and sort properties.
     * <p>
     * Note: The method changes the request and should only be called by the <code>WFSHandler</code> class.
     *
     * @param wfs
     *            {@link WFService} instance that is used for the lookup of proper (qualified) feature and property
     *            names
     */
    public void guessAllMissingNamespaces( WFService wfs ) {
        for ( Query query : queries ) {
            query.guessAllMissingNamespaces( wfs );
        }
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "WFSGetFeatureRequest: { \n ";
        ret += "outputFormat = " + outputFormat + "\n";
        ret += ( "handle = " + getHandle() + "\n" );
        ret += ( "query = " + queries + "\n" );
        ret += "}\n";
        return ret;
    }
}
