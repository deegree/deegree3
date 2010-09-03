//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.persistence.postgis;

import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.MappedXPath;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.TableAliasManager;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.filter.sql.postgis.PostGISMapping;
import org.deegree.geometry.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PostGISMapping} for the {@link PostGISFeatureStore} that's based on {@link FeatureTypeMapping} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class PostGISFeatureMapping implements PostGISMapping {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureMapping.class );

    private final FeatureType ft;

    private final FeatureTypeMapping ftMapping;

    private final PostGISFeatureStore fs;

    private final MappedApplicationSchema schema;

    PostGISFeatureMapping( MappedApplicationSchema schema, FeatureType ft, FeatureTypeMapping ftMapping,
                           PostGISFeatureStore fs ) {
        this.schema = schema;
        this.ft = ft;
        this.ftMapping = ftMapping;
        this.fs = fs;
    }

    @Override
    public PropertyNameMapping getMapping( PropertyName propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException {

        if ( ftMapping == null ) {
            return null;
        }

        MappedXPath mapping = null;
        try {
            mapping = new MappedXPath( schema, ftMapping, propName );
        } catch ( UnmappableException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if ( mapping == null ) {
            return null;
        }

        PropertyNameMapping propMapping = new PropertyNameMapping( aliasManager, mapping.getValueField(),
                                                                   mapping.getJoins() );
        return propMapping;
    }

    @Override
    public Object getPostGISValue( Literal literal, PropertyName propName )
                            throws FilterEvaluationException {

        Object pgValue = null;

        // if ( propName == null ) {
        // pgValue = literal.getValue().toString();
        // } else {
        // Pair<PropertyType, PropertyMappingType> mapping = findMapping( propName );
        // if ( mapping == null || mapping.second == null ) {
        // pgValue = literal.getValue().toString();
        // } else {
        // // TODO implement properly
        // PropertyType pt = mapping.first;
        // if ( pt instanceof SimplePropertyType ) {
        // Object internalValue = XMLValueMangler.xmlToInternal(
        // literal.getValue().toString(),
        // ( (SimplePropertyType) pt ).getPrimitiveType() );
        // pgValue = SQLValueMangler.internalToSQL( internalValue );
        // } else {
        // pgValue = literal.getValue().toString();
        // }
        // }
        // }

        return pgValue;
    }

    @Override
    public byte[] getPostGISValue( Geometry literal, PropertyName propName )
                            throws FilterEvaluationException {

        byte[] pgValue = null;
        // Pair<PropertyType, PropertyMappingType> mapping = findMapping( propName );
        // if ( mapping.first == null || !( mapping.first instanceof GeometryPropertyType ) ) {
        // throw new FilterEvaluationException( "Property '" + propName + "' is not known or not a geometry property."
        // );
        // }
        //
        // // TODO srs conversion?
        // GeometryPropertyType pt = (GeometryPropertyType) mapping.first;
        // try {
        // pgValue = WKBWriter.write( fs.getCompatibleGeometry( literal ) );
        // } catch ( ParseException e ) {
        // throw new FilterEvaluationException( e.getMessage() );
        // }
        return pgValue;
    }

    // private Pair<PropertyType, PropertyMappingType> findMapping( PropertyName propName )
    // throws FilterEvaluationException {
    // if ( propName == null ) {
    // // for BBOX queries, this may be null
    // GeometryPropertyType pt = ft.getDefaultGeometryPropertyDeclaration();
    // if ( pt == null ) {
    // throw new FilterEvaluationException(
    // "Cannot evaluate BBOX: FeatureType does not have a geometry property." );
    // }
    // PropertyMappingType ptMapping = ftMapping.getPropertyHints( pt.getName() );
    // if ( ptMapping == null ) {
    // return null;
    // }
    // return new Pair<PropertyType, PropertyMappingType>( pt, ptMapping );
    // }
    // Expr xpath = propName.getAsXPath();
    // if ( !( xpath instanceof LocationPath ) ) {
    // LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
    // + "': the root expression is not a LocationPath." );
    // return null;
    // }
    // List<QName> steps = new ArrayList<QName>();
    // for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
    // if ( !( step instanceof NameStep ) ) {
    // LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
    // + "': contains an expression that is not a NameStep." );
    // return null;
    // }
    // NameStep namestep = (NameStep) step;
    // if ( namestep.getPredicates() != null && !namestep.getPredicates().isEmpty() ) {
    // LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
    // + "': contains a NameStep with a predicate (needs implementation)." );
    // return null;
    // }
    // String prefix = namestep.getPrefix();
    // String localPart = namestep.getLocalName();
    // String namespace = propName.getNsContext().translateNamespacePrefixToUri( prefix );
    // steps.add( new QName( namespace, localPart, prefix ) );
    // }
    //
    // if ( steps.size() < 1 || steps.size() > 2 ) {
    // LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
    // + "': must contain one or two NameSteps (needs implementation)." );
    // return null;
    // }
    //
    // QName requestedProperty = null;
    // if ( steps.size() == 1 ) {
    // // step must be equal to a property name of the queried feature
    // if ( ft.getPropertyDeclaration( steps.get( 0 ) ) == null ) {
    // String msg = "Filter contains an invalid PropertyName '" + propName.getPropertyName()
    // + "'. The queried feature type '" + ft.getName()
    // + "' does not have a property with this name.";
    // throw new FilterEvaluationException( msg );
    // }
    // requestedProperty = steps.get( 0 );
    // } else {
    // // 1. step must be equal to the name or alias of the queried feature
    // if ( !ft.getName().equals( steps.get( 0 ) ) ) {
    // String msg = "Filter contains an invalid PropertyName '" + propName.getPropertyName()
    // + "'. The first step does not equal the queried feature type '" + ft.getName() + "'.";
    // throw new FilterEvaluationException( msg );
    // }
    // // 2. step must be equal to a property name of the queried feature
    // if ( ft.getPropertyDeclaration( steps.get( 1 ) ) == null ) {
    // String msg = "Filter contains an invalid PropertyName '" + propName.getPropertyName()
    // + "'. The second step does not equal any property of the queried feature type '"
    // + ft.getName() + "'.";
    // throw new FilterEvaluationException( msg );
    // }
    // requestedProperty = steps.get( 1 );
    // }
    // PropertyMappingType ptMapping = ftMapping.getPropertyHints( requestedProperty );
    // if ( ptMapping == null ) {
    // return null;
    // }
    //
    // return new Pair<PropertyType, PropertyMappingType>( ft.getPropertyDeclaration( requestedProperty ), ptMapping );
    // }
}
