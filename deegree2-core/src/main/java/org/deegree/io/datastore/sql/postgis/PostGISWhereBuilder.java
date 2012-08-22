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

package org.deegree.io.datastore.sql.postgis;

import static org.deegree.i18n.Messages.getMessage;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.deegree.i18n.Messages;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.sql.StatementBuffer;
import org.deegree.io.datastore.sql.TableAliasGenerator;
import org.deegree.io.datastore.sql.VirtualContentProvider;
import org.deegree.io.datastore.sql.wherebuilder.SpecialCharString;
import org.deegree.io.datastore.sql.wherebuilder.WhereBuilder;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.filterencoding.DBFunction;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.Function;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.SortProperty;
import org.postgis.PGboxbase;
import org.postgis.PGgeometry;

/**
 * {@link WhereBuilder} implementation for PostGIS databases.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </A>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class PostGISWhereBuilder extends WhereBuilder {

    private PostGISDatastore ds;

    /**
     * Creates a new instance of <code>PostGISWhereBuilder</code> from the given parameters.
     * 
     * @param rootFts
     *            selected feature types, more than one type means that the types are joined
     * @param aliases
     *            aliases for the feature types, may be null (must have same length as rootFts otherwise)
     * @param filter
     *            filter that restricts the matched features
     * @param sortProperties
     *            sort criteria for the result, may be null or empty
     * @param aliasGenerator
     *            used to generate unique table aliases
     * @param vcProvider
     * @throws DatastoreException
     */
    public PostGISWhereBuilder( MappedFeatureType[] rootFts, String[] aliases, Filter filter,
                                SortProperty[] sortProperties, TableAliasGenerator aliasGenerator,
                                VirtualContentProvider vcProvider ) throws DatastoreException {
        super( rootFts, aliases, filter, sortProperties, aliasGenerator, vcProvider );
        this.ds = (PostGISDatastore) rootFts[0].getGMLSchema().getDatastore();
    }

    /**
     * Appends an SQL fragment for the given object to the given sql statement.
     * 
     * NOTE: Currently, the method uses a quirk and appends the generated argument inline, i.e. not using
     * query.addArgument(). This is because of a problem that occurred in PostgreSQL; the execution of the inline
     * version is *much* faster (at least with version 8.0).
     * 
     * @param query
     * @param operation
     * @throws FilterEvaluationException
     */
    @Override
    protected void appendPropertyIsLikeOperationAsSQL( StatementBuffer query, PropertyIsLikeOperation operation )
                            throws FilterEvaluationException {

        String literal = operation.getLiteral().getValue();
        String escape = "" + operation.getEscapeChar();
        String wildCard = "" + operation.getWildCard();
        String singleChar = "" + operation.getSingleChar();

        SpecialCharString specialString = new SpecialCharString( literal, wildCard, singleChar, escape );
        String sqlEncoded = specialString.toSQLStyle( !operation.isMatchCase() );

        int targetSqlType = getPropertyNameSQLType( operation.getPropertyName() );

        // if isMatchCase == false surround first argument with LOWER (...) and convert characters
        // in second argument to lower case
        if ( operation.isMatchCase() ) {
            appendPropertyNameAsSQL( query, operation.getPropertyName() );
        } else {
            List<Expression> list = new ArrayList<Expression>();
            list.add( operation.getPropertyName() );
            Function func = new DBFunction( getFunctionName( "LOWER" ), list );
            appendFunctionAsSQL( query, func, targetSqlType );
        }

        query.append( "::VARCHAR LIKE '" );
        query.append( sqlEncoded );
        query.append( "'" );
    }

    /**
     * Generates an SQL-fragment for the given object.
     * 
     * TODO: Implement BBOX faster using explicit B0X-constructor
     * 
     * @throws DatastoreException
     */
    @Override
    protected void appendSpatialOperationAsSQL( StatementBuffer query, SpatialOperation operation )
                            throws DatastoreException {

        try {
            switch ( operation.getOperatorId() ) {
            case OperationDefines.BBOX: {
                appendBBOXOperationAsSQL( query, operation );
                break;
            }
            case OperationDefines.INTERSECTS: {
                appendIntersectsOperationAsSQL( query, operation );
                break;
            }
            case OperationDefines.CROSSES: {
                appendSimpleOperationAsSQL( query, operation, "crosses" );
                break;
            }
            case OperationDefines.EQUALS: {
                appendSimpleOperationAsSQL( query, operation, "equals" );
                break;
            }
            case OperationDefines.WITHIN: {
                appendSimpleOperationAsSQL( query, operation, "within" );
                break;
            }
            case OperationDefines.OVERLAPS: {
                appendSimpleOperationAsSQL( query, operation, "overlaps" );
                break;
            }
            case OperationDefines.TOUCHES: {
                appendSimpleOperationAsSQL( query, operation, "touches" );
                break;
            }
            case OperationDefines.DISJOINT: {
                appendSimpleOperationAsSQL( query, operation, "disjoint" );
                break;
            }
            case OperationDefines.CONTAINS: {
                appendSimpleOperationAsSQL( query, operation, "contains" );
                break;
            }
            case OperationDefines.DWITHIN: {
                appendDWithinOperationAsSQL( query, operation );
                break;
            }
            case OperationDefines.BEYOND: {
                appendBeyondOperationAsSQL( query, operation );
                break;
            }
            default: {
                String msg = "Spatial operator " + OperationDefines.getNameById( operation.getOperatorId() )
                             + " is not supported by '" + this.getClass().toString() + "'.";
                throw new DatastoreException( msg );
            }
            }
        } catch ( GeometryException e ) {
            throw new DatastoreException( e );
        }

    }

    private void appendSimpleOperationAsSQL( StatementBuffer query, SpatialOperation operation, String operationName )
                            throws GeometryException, DatastoreException {
        query.append( operationName );
        query.append( "(" );
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( ',' );
        appendGeometryArgument( query, this.getGeometryProperty( operation.getPropertyName() ), operation.getGeometry() );
        query.append( ')' );
    }

    private void appendIntersectsOperationAsSQL( StatementBuffer query, SpatialOperation operation )
                            throws GeometryException, DatastoreException {

        Envelope env = operation.getGeometry().getEnvelope();
        MappedGeometryPropertyType geoProperty = this.getGeometryProperty( operation.getPropertyName() );

        String argumentSRS = null;
        if ( env.getCoordinateSystem() != null ) {
            argumentSRS = env.getCoordinateSystem().getIdentifier();
        }
        String propertySRS = geoProperty.getCS().getIdentifier();
        int internalSRS = geoProperty.getMappingField().getSRS();

        int createSRSCode = getArgumentSRSCode( argumentSRS, propertySRS, internalSRS );
        PGboxbase box = PGgeometryAdapter.export( env );
        StringBuffer bbox = new StringBuffer( 323 );
        bbox.append( "SetSRID(?," + createSRSCode + ")" );

        int targetSRSCode = getTargetSRSCode( argumentSRS, propertySRS, internalSRS );
        if ( targetSRSCode != SRS_UNDEFINED ) {
            bbox = new StringBuffer( this.ds.buildSRSTransformCall( bbox.toString(), targetSRSCode ) );
        }

        // use the bbox operator (&&) to filter using the spatial index
        query.append( "(" );
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( " && " );
        query.append( bbox.toString() );
        query.addArgument( box, Types.OTHER );

        query.append( " AND intersects (" );
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( ',' );
        appendGeometryArgument( query, getGeometryProperty( operation.getPropertyName() ), operation.getGeometry() );
        query.append( "))" );
    }

    private void appendBBOXOperationAsSQL( StatementBuffer query, SpatialOperation operation )
                            throws DatastoreException, GeometryException {

        Envelope env = operation.getGeometry().getEnvelope();
        MappedGeometryPropertyType geoProperty = this.getGeometryProperty( operation.getPropertyName() );

        String argumentSRS = null;
        if ( env.getCoordinateSystem() != null ) {
            argumentSRS = env.getCoordinateSystem().getIdentifier();
        }
        String propertySRS = geoProperty.getCS().getIdentifier();
        int internalSRS = geoProperty.getMappingField().getSRS();
        int createSRSCode = getArgumentSRSCode( argumentSRS, propertySRS, internalSRS );
        PGboxbase box = PGgeometryAdapter.export( env );
        StringBuffer bbox = new StringBuffer( 326 );
        bbox.append( "SetSRID(?," + createSRSCode + ")" );

        int targetSRSCode = getTargetSRSCode( argumentSRS, propertySRS, internalSRS );
        if ( argumentSRS != null && !argumentSRS.equals( propertySRS ) ) {
            bbox = new StringBuffer( this.ds.buildSRSTransformCall( bbox.toString(), targetSRSCode ) );
        }

        // only the && operator uses the spatial index
        // intersects, contains etc. do not use spatial indexing!!!!
        query.append( "(" );
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( " && " );
        query.append( bbox.toString() );
        query.addArgument( box, Types.OTHER );

        // it is necessary to add an explicit intersects as well, because the && operator only
        // checks for intersection of the bbox with the bboxes of the geometries (and not the
        // geometries themselves)
        query.append( " AND intersects (" );
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( ',' );
        query.append( bbox.toString() );
        query.addArgument( box, Types.OTHER );
        query.append( "))" );
    }

    private void appendDWithinOperationAsSQL( StatementBuffer query, SpatialOperation operation )
                            throws GeometryException, DatastoreException {
        query.append( "distance(" );
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( ',' );
        appendGeometryArgument( query, this.getGeometryProperty( operation.getPropertyName() ), operation.getGeometry() );
        query.append( ")<=" );
        query.append( "" + operation.getDistance() );
    }

    private void appendBeyondOperationAsSQL( StatementBuffer query, SpatialOperation operation )
                            throws GeometryException, DatastoreException {
        query.append( "distance(" );
        appendPropertyNameAsSQL( query, operation.getPropertyName() );
        query.append( ',' );
        appendGeometryArgument( query, this.getGeometryProperty( operation.getPropertyName() ), operation.getGeometry() );
        query.append( ")>" );
        query.append( "" + operation.getDistance() );
    }

    /**
     * Construct and append the geometry argument using the correct internal SRS and perform a transform call to the
     * internal SRS of the {@link MappedGeometryPropertyType} if necessary.
     * 
     * @param query
     * @param geoProperty
     * @param geometry
     * @throws DatastoreException
     * @throws GeometryException
     */
    private void appendGeometryArgument( StatementBuffer query, MappedGeometryPropertyType geoProperty,
                                         Geometry geometry )
                            throws DatastoreException, GeometryException {

        String argumentSRS = null;
        if ( geometry.getCoordinateSystem() != null ) {
            argumentSRS = geometry.getCoordinateSystem().getIdentifier();
        }
        String propertySRS = geoProperty.getCS().getIdentifier();
        int internalSRS = geoProperty.getMappingField().getSRS();

        int createSRSCode = getArgumentSRSCode( argumentSRS, propertySRS, internalSRS );
        PGgeometry argument = PGgeometryAdapter.export( geometry, createSRSCode );

        int targetSRSCode = getTargetSRSCode( argumentSRS, propertySRS, internalSRS );
        if ( argumentSRS != null && !argumentSRS.equals( propertySRS ) ) {
            query.append( ds.buildSRSTransformCall( "?", targetSRSCode ) );
        } else {
            query.append( '?' );
        }
        query.addArgument( argument, Types.OTHER );
    }

    /**
     * Returns the internal SRS code that must be used for the creation of a geometry argument used in a spatial
     * operator.
     * 
     * @param argumentSRS
     * @param propertySRS
     * @param internalSrs
     * @return the internal SRS code that must be used for the creation of a geometry argument
     * @throws DatastoreException
     */
    private int getArgumentSRSCode( String argumentSRS, String propertySRS, int internalSrs )
                            throws DatastoreException {
        int argumentSRSCode = internalSrs;
        if ( argumentSRS == null ) {
            argumentSRSCode = internalSrs;
        } else if ( !propertySRS.equals( argumentSRS ) ) {
            // normalize SRS to first identifier
            try {
                argumentSRS = CRSFactory.create( argumentSRS ).getCRS().getIdentifier();
            } catch ( UnknownCRSException e ) {
                throw new DatastoreException( getMessage( "DATASTORE_SRS_UNKNOWN", argumentSRS ) );
            }
            argumentSRSCode = this.ds.getNativeSRSCode( argumentSRS );
            if ( argumentSRSCode == SRS_UNDEFINED ) {
                String msg = Messages.getMessage( "DATASTORE_SQL_NATIVE_CT_UNKNOWN_SRS",
                                                  PostGISDatastore.class.getName(), argumentSRS );
                throw new DatastoreException( msg );
            }
        }
        return argumentSRSCode;
    }

    /**
     * Returns the internal SRS code that must be used for the transform call for a geometry argument used in a spatial
     * operator.
     * 
     * @param argumentSRS
     * @param propertySRS
     * @param internalSrs
     * @return the internal SRS code that must be used for the transform call of a geometry argument, or -1 if no
     *         transformation is necessary
     */
    private int getTargetSRSCode( String argumentSRS, String propertySRS, int internalSrs )
                            throws DatastoreException {
        int targetSRS = SRS_UNDEFINED;
        if ( argumentSRS != null && !argumentSRS.equals( propertySRS ) ) {
            if ( internalSrs == SRS_UNDEFINED ) {
                String msg = Messages.getMessage( "DATASTORE_SRS_NOT_SPECIFIED2", argumentSRS, propertySRS );
                throw new DatastoreException( msg );
            }
            targetSRS = internalSrs;
        }
        return targetSRS;
    }
}
