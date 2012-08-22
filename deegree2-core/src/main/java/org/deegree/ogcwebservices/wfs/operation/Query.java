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
import java.util.Arrays;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.filterencoding.ComparisonOperation;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Function;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.PropertyIsBetweenOperation;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyIsInstanceOfOperation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyIsNullOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.ogcbase.ElementStep;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureDocument.BBoxTest;
import org.w3c.dom.Element;

/**
 * Represents a <code>Query</code> operation as a part of a {@link GetFeature} request.
 * 
 * Each individual query packaged in a {@link GetFeature} request is defined using the query value. The query value
 * defines which feature type to query, what properties to retrieve and what constraints (spatial and non-spatial) to
 * apply to those properties.
 * <p>
 * The mandatory <code>typeName</code> attribute is used to indicate the name of one or more feature type instances or
 * class instances to be queried. Its value is a list of namespace-qualified names (XML Schema type QName, e.g.
 * myns:School) whose value must match one of the feature types advertised in the Capabilities document of the WFS.
 * Specifying more than one typename indicates that a join operation is being performed. All the names in the typeName
 * list must be valid types that belong to this query's feature content as defined by the GML Application Schema.
 * Optionally, individual feature type names in the typeName list may be aliased using the format QName=Alias. The
 * following is an example typeName value that indicates that a join operation is to be performed and includes aliases:
 * <BR>
 * <code>typeName="ns1:InwaterA_1m=A,ns1:InwaterA_1m=B,ns2:CoastL_1M=C"</code><BR>
 * This example encodes a join between three feature types aliased as A, B and C. The join between feature type A and B
 * is a self-join.
 * </p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Query {

    private static ILogger LOG = LoggerFactory.getLogger( Query.class );

    private String handle;

    private QualifiedName[] typeNames;

    private String[] aliases;

    private String featureVersion;

    private String srsName;

    private PropertyPath[] propertyNames;

    private Function[] functions;

    private Filter filter;

    private SortProperty[] sortProperties;

    // deegree specific extension ("inherited" from GetFeature container)
    private RESULT_TYPE resultType;

    // deegree specific extension ("inherited" from GetFeature container)
    private int maxFeatures = -1;

    // deegree specific extension ("inherited" from GetFeature container)
    private int startPosition = 1;

    private BBoxTest test;

    /**
     * @param propertyNames
     * @param functions
     * @param sortProperties
     * @param handle
     * @param featureVersion
     * @param typeNames
     * @param aliases
     * @param srsName
     * @param filter
     * @param resultType
     * @param maxFeatures
     * @param startPosition
     * @param test
     */
    public Query( PropertyPath[] propertyNames, Function[] functions, SortProperty[] sortProperties, String handle,
                  String featureVersion, QualifiedName[] typeNames, String[] aliases, String srsName, Filter filter,
                  RESULT_TYPE resultType, int maxFeatures, int startPosition, BBoxTest test ) {
        this( propertyNames, functions, sortProperties, handle, featureVersion, typeNames, aliases, srsName, filter,
              resultType, maxFeatures, startPosition );

        this.test = test;
    }

    /**
     * Creates a new <code>Query</code> instance.
     * 
     * @param propertyNames
     *            names of the requested properties, may be null or empty
     * @param functions
     *            names of the requested functions, may be null or empty
     * @param sortProperties
     *            sort criteria, may be null or empty
     * @param handle
     *            client-generated identifier for the query, may be null
     * @param featureVersion
     *            version of the feature instances to fetched, may be null
     * @param typeNames
     *            list of requested feature types
     * @param aliases
     *            list of aliases for the feature types, must either be null or have the same length as the typeNames
     *            array
     * @param srsName
     *            name of the spatial reference system
     * @param filter
     *            spatial and none-spatial constraints
     * @param resultType
     *            deegree specific extension ("inherited" from GetFeature container)
     * @param maxFeatures
     *            deegree specific extension ("inherited" from GetFeature container)
     * @param startPosition
     *            deegree specific extension ("inherited" from GetFeature container)
     */
    public Query( PropertyPath[] propertyNames, Function[] functions, SortProperty[] sortProperties, String handle,
                  String featureVersion, QualifiedName[] typeNames, String[] aliases, String srsName, Filter filter,
                  RESULT_TYPE resultType, int maxFeatures, int startPosition ) {
        if ( propertyNames == null ) {
            this.propertyNames = new PropertyPath[0];
            // this.propertyNames[0] = new PropertyPath( typeNames[0] );
        } else {
            this.propertyNames = propertyNames;
        }
        this.functions = functions;
        this.sortProperties = sortProperties;
        this.handle = handle;
        this.featureVersion = featureVersion;
        this.typeNames = typeNames;
        this.aliases = aliases;
        assert aliases == null || aliases.length == typeNames.length;
        if ( LOG.isDebug() ) {
            LOG.logDebug( "The query contains following aliases: " + Arrays.toString( aliases ) );
        }
        this.srsName = srsName;
        this.filter = filter;
        this.resultType = resultType;
        this.maxFeatures = maxFeatures;
        this.startPosition = startPosition;
    }

  

    /**
     * Creates a new <code>Query</code> instance.
     * 
     * @param propertyNames
     *            names of the requested properties, may be null or empty
     * @param functions
     *            names of the requested functions, may be null or empty
     * @param sortProperties
     *            sort criteria, may be null or empty
     * @param handle
     *            client-generated identifier for the query, may be null
     * @param featureVersion
     *            version of the feature instances to fetched, may be null
     * @param typeNames
     *            list of requested feature types. if more than one feature types is set a JOIN will be created (not yet
     *            supported)
     * @param aliases
     *            list of aliases for the feature types, must either be null or have the same length as the typeNames
     *            array
     * @param srsName
     *            name of the spatial reference system
     * @param filter
     *            spatial and none-spatial constraints
     * @param resultType
     *            deegree specific extension ("inherited" from GetFeature container)
     * @param maxFeatures
     *            deegree specific extension ("inherited" from GetFeature container)
     * @param startPosition
     *            deegree specific extension ("inherited" from GetFeature container)
     * @return new <code>Query</code> instance
     */
    public static Query create( PropertyPath[] propertyNames, Function[] functions, SortProperty[] sortProperties,
                                String handle, String featureVersion, QualifiedName[] typeNames, String[] aliases,
                                String srsName, Filter filter, int maxFeatures, int startPosition,
                                RESULT_TYPE resultType ) {
        return new Query( propertyNames, functions, sortProperties, handle, featureVersion, typeNames, aliases,
                          srsName, filter, resultType, maxFeatures, startPosition );
    }

    /**
     * Creates a new simple <code>Query</code> instance that selects the whole feature type.
     * 
     * @param typeName
     *            name of the feature to be queried
     * @return new <code>Query</code> instance
     */
    public static Query create( QualifiedName typeName ) {
        return new Query( null, null, null, null, null, new QualifiedName[] { typeName }, null, null, null,
                          RESULT_TYPE.RESULTS, -1, 0 );
    }

    /**
     * Creates a new simple <code>Query</code> instance that selects the whole feature type.
     * 
     * @param typeName
     *            name of the feature to be queried
     * @param filter
     *            spatial and none-spatial constraints
     * @return new <code>Query</code> instance
     */
    public static Query create( QualifiedName typeName, Filter filter ) {
        return new Query( null, null, null, null, null, new QualifiedName[] { typeName }, null, null, filter,
                          RESULT_TYPE.RESULTS, -1, 0 );
    }

    /**
     * Creates a <code>Query</code> instance from a document that contains the DOM representation of the request, using
     * the 1.1.0 filter encoding.
     * <p>
     * Note that the following attributes from the surrounding element are also considered (if it present):
     * <ul>
     * <li>resultType</li>
     * <li>maxFeatures</li>
     * <li>startPosition</li>
     * </ul>
     * 
     * @param element
     * @return corresponding <code>Query</code> instance
     * @throws XMLParsingException
     */
    public static Query create( Element element )
                            throws XMLParsingException {
        return create( element, false );
    }

    /**
     * Creates a <code>Query</code> instance from a document that contains the DOM representation of the request.
     * <p>
     * Note that the following attributes from the surrounding element are also considered (if it present):
     * <ul>
     * <li>resultType</li>
     * <li>maxFeatures</li>
     * <li>startPosition</li>
     * </ul>
     * 
     * @param element
     * @param useVersion_1_0_0
     *            if the filterencoding 1.0.0 rules should be applied.
     * @return corresponding <code>Query</code> instance
     * @throws XMLParsingException
     */
    public static Query create( Element element, boolean useVersion_1_0_0 )
                            throws XMLParsingException {

        GetFeatureDocument doc = new GetFeatureDocument();
        Query query = doc.parseQuery( element, useVersion_1_0_0 );
        return query;
    }

    /**
     * Returns the handle attribute.
     * <p>
     * The handle attribute is included to allow a client to associate a mnemonic name to the query. The purpose of the
     * handle attribute is to provide an error handling mechanism for locating a statement that might fail.
     * 
     * @return the handle attribute
     */
    public String getHandle() {
        return this.handle;
    }

    /**
     * Returns the names of the requested feature types.
     * 
     * @return the names of the requested feature types
     */
    public QualifiedName[] getTypeNames() {
        return this.typeNames;
    }

    /**
     * Returns the aliases for the requested feature types.
     * <p>
     * The returned array is either null or has the same length as the array returned by {@link #getTypeNames()}.
     * 
     * @see #getTypeNames()
     * @return the aliases for the requested feature types, or null if no aliases are used
     */
    public String[] getAliases() {
        return this.aliases;
    }

    /**
     * Returns the srsName attribute.
     * 
     * @return the srsName attribute
     */
    public String getSrsName() {
        return this.srsName;
    }

    /**
     * Sets the srsName attribute to given value.
     * 
     * @param srsName
     *            name of the requested SRS
     */
    public void setSrsName( String srsName ) {
        this.srsName = srsName;
    }

    /**
     * @throws InvalidParameterValueException
     */
    public void performBBoxTest()
                            throws InvalidParameterValueException {
        if ( test != null ) {
            test.performTest();
        }
    }

    /**
     * Sets the test to null, thus enabling the gc.
     */
    public void deleteBBoxTest() {
        test = null;
    }

    /**
     * Returns the featureVersion attribute.
     * 
     * The version attribute is included in order to accommodate systems that support feature versioning. A value of ALL
     * indicates that all versions of a feature should be fetched. Otherwise an integer can be specified to return the n
     * th version of a feature. The version numbers start at '1' which is the oldest version. If a version value larger
     * than the largest version is specified then the latest version is return. The default action shall be for the
     * query to return the latest version. Systems that do not support versioning can ignore the parameter and return
     * the only version that they have.
     * 
     * @return the featureVersion attribute
     */
    public String getFeatureVersion() {
        return this.featureVersion;
    }

    /**
     * Returns all requested properties.
     * 
     * @return all requested properties
     * 
     * @see #getFunctions()
     */
    public PropertyPath[] getPropertyNames() {
        return this.propertyNames;
    }

    /**
     * Beside property names a query may contains 0 to n functions modifying the values of one or more original
     * properties. E.g. instead of area and population the density of a country can be requested by using a function
     * instead:
     * 
     * <pre>
     *  &lt;ogc:Div&gt;
     *   &lt;ogc:PropertyName&gt;population&lt;/ogc:PropertyName&gt;
     *   &lt;ogc:PropertyName&gt;area&lt;/ogc:PropertyName&gt;
     *  &lt;/ogc:Div&gt;
     * </pre>
     * 
     * <p>
     * If no functions and no property names are specified all properties should be fetched.
     * </p>
     * 
     * @return requested functions
     * 
     * @see #getPropertyNames()
     */
    public Function[] getFunctions() {
        return this.functions;
    }

    /**
     * Returns the filter that limits the query.
     * 
     * @return the filter that limits the query
     */
    public Filter getFilter() {
        return this.filter;
    }

    /**
     * Returns the sort criteria for the result.
     * 
     * @return the sort criteria for the result
     */
    public SortProperty[] getSortProperties() {
        return this.sortProperties;
    }

    /**
     * Returns the value of the resultType attribute ("inherited" from the GetFeature container).
     * 
     * @return the value of the resultType attribute
     */
    public RESULT_TYPE getResultType() {
        return this.resultType;
    }

    /**
     * Returns the value of the maxFeatures attribute ("inherited" from the GetFeature container).
     * 
     * The optional maxFeatures attribute can be used to limit the number of features that a GetFeature request
     * retrieves. Once the maxFeatures limit is reached, the result set is truncated at that point. If not limit is set
     * -1 will be returned
     * 
     * @return the value of the maxFeatures attribute
     */
    public int getMaxFeatures() {
        return this.maxFeatures;
    }

    /**
     * @param maxFeatures
     */
    public void setMaxFeatures( int maxFeatures ) {
        this.maxFeatures = maxFeatures;
    }

    /**
     * Returns the value of the startPosition attribute ("inherited" from the GetFeature container).
     * <p>
     * The startPosition parameter identifies the first result set entry to be returned. If no startPosition is set
     * explicitly, 1 will be returned.
     * 
     * @return the value of the startPosition attribute, 1 if undefined
     */
    public int getStartPosition() {
        return this.startPosition;
    }

    /**
     * @see #getStartPosition()
     * @param startPosition
     */
    public void setStartPosition( int startPosition ) {
        this.startPosition = startPosition;
    }

    /**
     * Adds missing namespaces in the names of requested feature types.
     * <p>
     * If the {@link QualifiedName} of a requested type has a null namespace, the first qualified feature type name of
     * the given {@link WFService} with the same local name is used instead.
     * <p>
     * Note: The method changes this request part (the feature type names) and should only be called by the
     * <code>WFSHandler</code> class.
     * 
     * @param wfs
     *            {@link WFService} instance that is used for the lookup of proper (qualified) feature type names
     */
    public void guessMissingTypeNameNamespace( WFService wfs ) {
        for ( int i = 0; i < typeNames.length; i++ ) {
            QualifiedName typeName = typeNames[i];
            if ( typeName.getNamespace() == null ) {
                if ( typeName.getLocalName().equals( typeName.getLocalName() ) ) {
                    LOG.logWarning( "Requested feature type name has no namespace information. Guessing namespace for feature type '"
                                    + typeName.getLocalName() + "' (quirks lookup mode)." );
                    for ( QualifiedName ftName : wfs.getMappedFeatureTypes().keySet() ) {
                        if ( ftName.getLocalName().equals( typeName.getLocalName() ) ) {
                            LOG.logWarning( "Using feature type '" + ftName + "'." );
                            typeNames[i] = ftName;
                            break;
                        }
                    }
                }
            }
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
        guessMissingTypeNameNamespace( wfs );

        URI defaultNamespace = typeNames[0].getNamespace();
        augmentFilterWithNamespace( defaultNamespace );
        augmentSortPropertiesWithNamespace( defaultNamespace );
        augmentQueriedProperties( defaultNamespace );
    }

    private void augmentQueriedProperties( URI defaultNamespace ) {
        if ( propertyNames != null ) {
            for ( PropertyPath propertyPath : propertyNames ) {
                augmentPropertyPath( propertyPath, defaultNamespace );
            }
        }
    }

    private void augmentSortPropertiesWithNamespace( URI defaultNamespace ) {
        if ( sortProperties != null ) {
            for ( SortProperty sortCriterion : sortProperties ) {
                augmentPropertyPath( sortCriterion.getSortProperty(), defaultNamespace );
            }
        }
    }

    private void augmentFilterWithNamespace( URI defaultNamespace ) {
        if ( filter != null ) {
            if ( filter instanceof ComplexFilter ) {
                Operation operation = ( (ComplexFilter) filter ).getOperation();
                augmentFilterOperationWithNamespace( operation, defaultNamespace );
            }
        }
    }

    private void augmentFilterOperationWithNamespace( Operation operation, URI defaultNamespace ) {
        if ( operation instanceof ComparisonOperation ) {
            if ( operation instanceof PropertyIsBetweenOperation ) {
                PropertyIsBetweenOperation propOperation = (PropertyIsBetweenOperation) operation;
                augmentPropertyPath( propOperation.getPropertyName().getValue(), defaultNamespace );
                if ( propOperation.getLowerBoundary() instanceof PropertyName ) {
                    augmentPropertyPath( ( (PropertyName) propOperation.getLowerBoundary() ).getValue(),
                                         defaultNamespace );
                }
                if ( propOperation.getUpperBoundary() instanceof PropertyName ) {
                    augmentPropertyPath( ( (PropertyName) propOperation.getUpperBoundary() ).getValue(),
                                         defaultNamespace );
                }
            } else if ( operation instanceof PropertyIsCOMPOperation ) {
                PropertyIsCOMPOperation propOperation = (PropertyIsCOMPOperation) operation;
                if ( propOperation.getFirstExpression() instanceof PropertyName ) {
                    augmentPropertyPath( ( (PropertyName) propOperation.getFirstExpression() ).getValue(),
                                         defaultNamespace );
                }
                if ( propOperation.getSecondExpression() instanceof PropertyName ) {
                    augmentPropertyPath( ( (PropertyName) propOperation.getSecondExpression() ).getValue(),
                                         defaultNamespace );
                }
            } else if ( operation instanceof PropertyIsInstanceOfOperation ) {
                PropertyIsInstanceOfOperation propOperation = (PropertyIsInstanceOfOperation) operation;
                augmentPropertyPath( propOperation.getPropertyName().getValue(), defaultNamespace );
            } else if ( operation instanceof PropertyIsLikeOperation ) {
                PropertyIsLikeOperation propOperation = (PropertyIsLikeOperation) operation;
                augmentPropertyPath( propOperation.getPropertyName().getValue(), defaultNamespace );
            } else if ( operation instanceof PropertyIsNullOperation ) {
                PropertyIsNullOperation propOperation = (PropertyIsNullOperation) operation;
                augmentPropertyPath( propOperation.getPropertyName().getValue(), defaultNamespace );
            }
        } else if ( operation instanceof LogicalOperation ) {
            LogicalOperation logicalOperation = (LogicalOperation) operation;
            for ( Operation argument : logicalOperation.getArguments() ) {
                augmentFilterOperationWithNamespace( argument, defaultNamespace );
            }
        } else if ( operation instanceof SpatialOperation ) {
            SpatialOperation spatialOperation = (SpatialOperation) operation;
            PropertyName propertyName = spatialOperation.getPropertyName();
            if ( propertyName != null ) {
                augmentPropertyPath( propertyName.getValue(), defaultNamespace );
            }
        }

    }

    private void augmentPropertyPath( PropertyPath propertyPath, URI defaultNamespace ) {
        for ( PropertyPathStep step : propertyPath.getAllSteps() ) {
            QualifiedName name = step.getPropertyName();
            if ( name.getNamespace() == null && step instanceof ElementStep ) {
                LOG.logWarning( "Augmenting missing namespace: '" + name + "' -> '" + defaultNamespace + "'" );
                step.setPropertyName( new QualifiedName( name.getPrefix(), name.getLocalName(), defaultNamespace ) );
            }
        }
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        String ret = null;
        ret = "propertyNames = " + propertyNames + "\n";
        ret += ( "handle = " + handle + "\n" );
        ret += ( "version = " + featureVersion + "\n" );
        ret += ( "typeName = " + typeNames + "\n" );
        ret += ( "filter = " + filter + "\n" );
        return ret;
    }
}
