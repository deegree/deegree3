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
package org.deegree.record.persistence.genericrecordstore;

import static org.deegree.commons.tom.primitive.PrimitiveType.BOOLEAN;
import static org.deegree.commons.tom.primitive.PrimitiveType.DATE;
import static org.deegree.commons.tom.primitive.PrimitiveType.STRING;
import static org.deegree.protocol.csw.CSWConstants.APISO_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.DCT_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.deegree.protocol.ows.OWSCommonXMLAdapter.OWS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.commons.utils.Triple;
import org.deegree.feature.persistence.mapping.DBField;
import org.deegree.feature.persistence.mapping.Join;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.TableAliasManager;
import org.deegree.filter.sql.postgis.PostGISMapping;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBWriter;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.slf4j.Logger;

import com.vividsolutions.jts.io.ParseException;

/**
 * Implementation of the {@link PostGISMapping}. It's the base class for access to the backend. Is there any change in
 * the database schema for the {@link ISORecordStore} then in this class should be changed the binding, as well. TODO
 * boundingbox, crs and association, denominator, distanceUOM, distanceValue put a type in
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class PostGISMappingsISODC implements PostGISMapping {

    private static final Logger LOG = getLogger( PostGISMappingsISODC.class );

    private static Map<QName, Triple<String, String, PrimitiveType>> propToTableAndCol = new HashMap<QName, Triple<String, String, PrimitiveType>>();

    /**
     * Tablename in backend
     */
    public final static String RECORDBRIEF = "recordbrief";

    /**
     * Tablename in backend
     */
    public final static String RECORDSUMMARY = "recordsummary";

    /**
     * Tablename in backend
     */
    public final static String RECORDFULL = "recordfull";

    /**
     * XML element name in the representation of the response
     */
    public final static String BRIEFRECORD = "BriefRecord";

    /**
     * XML element name in the representation of the response
     */
    public final static String SUMMARYRECORD = "SummaryRecord";

    /**
     * XML element name in the representation of the response
     */
    public final static String RECORD = "Record";

    /**
     * Mapping between tablename and XML element name
     */
    private static Map<String, String> tableRecordType = new HashMap<String, String>();

    static {

        tableRecordType.put( RECORDBRIEF, BRIEFRECORD );
        tableRecordType.put( RECORDSUMMARY, SUMMARYRECORD );
        tableRecordType.put( RECORDFULL, RECORD );

        // ----------------------------------------------------------------------------------------
        // ----------------------<common queryable properties>-------------------------------------

        addStringProp( APISO_NS, "title", DatabaseTables.isoqp_title, "title" );
        addStringProp( APISO_NS, "Title", DatabaseTables.isoqp_title, "title" );
        addStringProp( DC_NS, "Title", DatabaseTables.isoqp_title, "title" );
        addStringProp( CSW_202_NS, "Title", DatabaseTables.isoqp_title, "title" );
        addStringProp( APISO_NS, "abstract", DatabaseTables.isoqp_abstract, "abstract" );
        addStringProp( APISO_NS, "Abstract", DatabaseTables.isoqp_abstract, "abstract" );
        addStringProp( DCT_NS, "Abstract", DatabaseTables.isoqp_abstract, "abstract" );
        addStringProp( CSW_202_NS, "Abstract", DatabaseTables.isoqp_abstract, "abstract" );
        addStringProp( APISO_NS, "BoundingBox", DatabaseTables.isoqp_BoundingBox, "bbox" );
        addStringProp( DC_NS, "coverage", DatabaseTables.isoqp_BoundingBox, "bbox" );
        addStringProp( OWS_NS, "BoundingBox", DatabaseTables.isoqp_BoundingBox, "bbox" );
        addStringProp( OWS_NS, "boundingBox", DatabaseTables.isoqp_BoundingBox, "bbox" );
        addStringProp( CSW_202_NS, "BoundingBox", DatabaseTables.isoqp_BoundingBox, "bbox" );
        addStringProp( APISO_NS, "type", DatabaseTables.isoqp_type, "type" );
        addStringProp( APISO_NS, "Type", DatabaseTables.isoqp_type, "type" );
        addStringProp( DC_NS, "Type", DatabaseTables.isoqp_type, "type" );
        addStringProp( CSW_202_NS, "Type", DatabaseTables.isoqp_type, "type" );
        addStringProp( APISO_NS, "format", DatabaseTables.isoqp_format, "format" );
        addStringProp( APISO_NS, "Format", DatabaseTables.isoqp_format, "format" );
        addStringProp( DC_NS, "Format", DatabaseTables.isoqp_format, "format" );
        addStringProp( CSW_202_NS, "Format", DatabaseTables.isoqp_format, "format" );
        addStringProp( APISO_NS, "Subject", DatabaseTables.isoqp_keyword, "keyword" );
        addStringProp( APISO_NS, "subject", DatabaseTables.isoqp_keyword, "keyword" );
        addStringProp( DC_NS, "Subject", DatabaseTables.isoqp_keyword, "keyword" );
        addStringProp( CSW_202_NS, "Subject", DatabaseTables.isoqp_keyword, "keyword" );
        addStringProp( APISO_NS, "AnyText", DatabaseTables.datasets, "anytext" );
        addStringProp( APISO_NS, "anyText", DatabaseTables.datasets, "anytext" );
        addStringProp( CSW_202_NS, "AnyText", DatabaseTables.datasets, "anytext" );
        addStringProp( APISO_NS, "identifier", DatabaseTables.qp_identifier, "identifier" );
        addStringProp( APISO_NS, "Identifier", DatabaseTables.qp_identifier, "identifier" );
        addStringProp( DC_NS, "Identifier", DatabaseTables.qp_identifier, "identifier" );
        addStringProp( CSW_202_NS, "Identifier", DatabaseTables.qp_identifier, "identifier" );
        addDateProp( APISO_NS, "modified", DatabaseTables.datasets, "modified" );
        addDateProp( APISO_NS, "Modified", DatabaseTables.datasets, "modified" );
        addDateProp( DCT_NS, "Modified", DatabaseTables.datasets, "modified" );
        addDateProp( CSW_202_NS, "Modified", DatabaseTables.datasets, "modified" );
        addStringProp( APISO_NS, "CRS", DatabaseTables.isoqp_crs, "crs" );
        addStringProp( DC_NS, "CRS", DatabaseTables.isoqp_crs, "crs" );
        addStringProp( APISO_NS, "association", DatabaseTables.isoqp_association, "relation" );
        addStringProp( APISO_NS, "Association", DatabaseTables.isoqp_association, "relation" );
        addStringProp( CSW_202_NS, "Association", DatabaseTables.isoqp_association, "relation" );
        addStringProp( DC_NS, "Relation", DatabaseTables.isoqp_association, "relation" );

        // ----------------------</common queryable properties>------------------------------------
        // ----------------------------------------------------------------------------------------

        // ----------------------------------------------------------------------------------------
        // ----------------------<additional common queryable properties>--------------------------
        addStringProp( APISO_NS, "Language", DatabaseTables.datasets, "language" );
        addStringProp( APISO_NS, "language", DatabaseTables.datasets, "language" );
        addDateProp( APISO_NS, "RevisionDate", DatabaseTables.isoqp_revisiondate, "revisiondate" );
        addStringProp( APISO_NS, "AlternateTitle", DatabaseTables.isoqp_alternatetitle, "alternatetitle" );
        // addDateProp( APISO_NS, "RevisionDate", "isoqp_revisiondate", "revisiondate" );
        addDateProp( APISO_NS, "PublicationDate", DatabaseTables.isoqp_publicationdate, "publicationdate" );
        addStringProp( APISO_NS, "OrganisationName", DatabaseTables.isoqp_organisationname, "organisationname" );
        addBooleanProp( APISO_NS, "HasSecurityConstraint", DatabaseTables.datasets, "hassecurityconstraint" );
        addStringProp( APISO_NS, "ResourceIdentifier", DatabaseTables.isoqp_resourceidentifier, "resourceidentifier" );
        addStringProp( APISO_NS, "ParentIdentifier", DatabaseTables.datasets, "parentidentifier" );
        addStringProp( APISO_NS, "KeywordType", DatabaseTables.isoqp_keyword, "keywordType" );
        addStringProp( APISO_NS, "TopicCategory", DatabaseTables.isoqp_topiccategory, "topiccategory" );
        addStringProp( APISO_NS, "ResourceLanguage", DatabaseTables.datasets, "resourcelanguage" );
        addStringProp( APISO_NS, "GeographicDescriptionCode", DatabaseTables.isoqp_geographicdescriptioncode,
                       "geographicdescriptioncode" );
        addStringProp( APISO_NS, "Denominator", DatabaseTables.isoqp_spatialresolution, "denominator" );
        addStringProp( APISO_NS, "DistanceValue", DatabaseTables.isoqp_spatialresolution, "distancevalue" );
        addStringProp( APISO_NS, "DistanceUOM", DatabaseTables.isoqp_spatialresolution, "distanceuom" );
        addStringProp( APISO_NS, "Denominator", DatabaseTables.isoqp_spatialresolution, "denominator" );
        addDateProp( APISO_NS, "TempExtent_begin", DatabaseTables.isoqp_temporalextent, "tempextent_begin" );
        addDateProp( APISO_NS, "TempExtent_end", DatabaseTables.isoqp_temporalextent, "tempextent_end" );
        addStringProp( APISO_NS, "ServiceType", DatabaseTables.isoqp_servicetype, "servicetype" );
        addStringProp( APISO_NS, "ServiceTypeVersion", DatabaseTables.isoqp_servicetypeversion, "servicetypeversion" );
        addStringProp( APISO_NS, "Operation", DatabaseTables.isoqp_operation, "operation" );
        addStringProp( APISO_NS, "OperatesOn", DatabaseTables.isoqp_operatesondata, "operateson" );
        addStringProp( APISO_NS, "OperatesOnIdentifier", DatabaseTables.isoqp_operatesondata, "operatesonidentifier" );
        addStringProp( APISO_NS, "OperatesOnName", DatabaseTables.isoqp_operatesondata, "operatesonname" );
        addStringProp( APISO_NS, "CouplingType", DatabaseTables.isoqp_couplingtype, "couplingtype" );

        // ----------------------</additional common queryable properties>-------------------------
        // ----------------------------------------------------------------------------------------

        // ----------------------------------------------------------------------------------------
        // ----------------------<additional queryable properties for INSPIRE>--------------------------

        addBooleanProp( APISO_NS, "Degree", DatabaseTables.addqp_degree, "degree" );
        addStringProp( APISO_NS, "AccessConstraints", DatabaseTables.addqp_accessconstraint, "accessconstraint" );
        addStringProp( APISO_NS, "OtherConstraints", DatabaseTables.addqp_otherconstraint, "otherconstraint" );
        addStringProp( APISO_NS, "Classification", DatabaseTables.addqp_classification, "classification" );
        addStringProp( APISO_NS, "ConditionApplyingToAccessAndUse", DatabaseTables.addqp_limitation, "limitation" );
        addStringProp( APISO_NS, "Lineage", DatabaseTables.addqp_lineage, "lineage" );
        addStringProp( APISO_NS, "SpecificationTitle", DatabaseTables.addqp_specification, "specificationtitle" );
        addStringProp( APISO_NS, "SpecificationDateType", DatabaseTables.addqp_specification, "specificationdatetype" );
        addDateProp( APISO_NS, "SpecificationDate", DatabaseTables.addqp_specification, "specificationdate" );

        // ----------------------</additional queryable properties for INSPIRE>-------------------------
        // ----------------------------------------------------------------------------------------

    }

    /**
     * 
     * The common column names that are used in the backend for each databasetable.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum CommonColumnNames {

        /**
         * the primarykey of a databasetable
         */
        id,

        /**
         * the foreignkey of a databasetable
         */
        fk_datasets,

        /**
         * the BLOB data of the record
         */
        data,

        /**
         * the format of the record, 1 == DC, 2 == ISO
         */
        format,

        /**
         * the identifier of the record
         */
        identifier;
    }

    /**
     * The names of the databasetables that are used in the backend.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum DatabaseTables {
        /**
         * main databasetable, all of the other tables derive from this table
         */
        datasets,

        /**
         * record identifier
         */
        qp_identifier,

        /**
         * organisationname which is responsible of the content
         */
        isoqp_organisationname,

        /**
         * temporal extent of the record
         */
        isoqp_temporalextent,

        /**
         * spatial resolution of the record
         */
        isoqp_spatialresolution,

        /**
         * couplingtype of the service record
         */
        isoqp_couplingtype,

        /**
         * tightly coupled dataset relation
         */
        isoqp_operatesondata,

        /**
         * name of the service operation
         */
        isoqp_operation,

        /**
         * the geographicdescriptioncode of the record
         */
        isoqp_geographicdescriptioncode,

        /**
         * the version of the service type
         */
        isoqp_servicetypeversion,

        /**
         * name of the service type, e.g. WFS
         */
        isoqp_servicetype,

        /**
         * language of the record
         */
        isoqp_resourcelanguage,

        /**
         * revision date of the record
         */
        isoqp_revisiondate,

        /**
         * creation date of the record
         */
        isoqp_creationdate,

        /**
         * publication date of the record
         */
        isoqp_publicationdate,

        /**
         * identifier of the resource that can be coupled with a service record
         */
        isoqp_resourceidentifier,

        /**
         * alternate title of the record
         */
        isoqp_alternatetitle,
        /**
         * the relation of the record
         */
        isoqp_association,

        /**
         * the title of the record
         */
        isoqp_title,

        /**
         * the nature of the record, one of dataset, datasetcollection, service, application
         */
        isoqp_type,

        /**
         * the topic or content of the record
         */
        isoqp_keyword,

        /**
         * main theme(s) of the record
         */
        isoqp_topiccategory,

        /**
         * the physical or digital manifestation of the record
         */
        isoqp_format,

        /**
         * the abstract of the record
         */
        isoqp_abstract,

        /**
         * the bounding box that encapsulates the record by spatial boundaries
         */
        isoqp_BoundingBox,

        /**
         * the coordinate reference system of the bounding box(es)
         */
        isoqp_crs,

        /**
         * Boolean value - indication of conformance result
         */
        addqp_degree,

        /**
         * citation of the product specification or user requirement against which data is being evaluated
         */
        addqp_specification,

        /**
         * restrictions on the access and use of a resource or metadata
         */
        addqp_limitation,

        /**
         * assures the protection of privacy or intellectual property. Regarding special restrictions or limitations on
         * obtaining the resource.
         */
        addqp_accessconstraint,

        /**
         * legal prerequisites for accessing and using the resource or metadata
         */
        addqp_otherconstraint,

        /**
         * name of the handling restrictions on the resource.
         */
        addqp_classification,

        /**
         * general explanation of the data producer's knowledge about the lineage of a dataset.
         */
        addqp_lineage

    }

    @Override
    public PropertyNameMapping getMapping( PropertyName propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException {

        PropertyNameMapping mapping = null;

        QName qName = propName.getAsQName();
        if ( qName == null ) {
            String msg = "Cannot map property name '" + propName + "'. Not a simple QName.";
            LOG.debug( msg );
        } else {
            Triple<String, String, PrimitiveType> tableColumn = propToTableAndCol.get( qName );
            if ( tableColumn != null ) {
                List<Join> joins = new ArrayList<Join>();
                if ( !tableColumn.first.equals( "datasets" ) ) {
                    DBField from = new DBField( "datasets", "id" );
                    DBField to = new DBField( tableColumn.first, "fk_datasets" );
                    joins.add( new Join( from, to, null, 0 ) );
                }
                // TODO primitive type
                DBField valueField = new DBField( tableColumn.first, tableColumn.second );
                mapping = new PropertyNameMapping( aliasManager, valueField, joins );
            }
        }
        return mapping;
    }

    private static void addBooleanProp( String propNs, String propName, DatabaseTables table, String column ) {
        QName qName = new QName( propNs, propName );
        Triple<String, String, PrimitiveType> mapping = new Triple<String, String, PrimitiveType>( table.name(),
                                                                                                   column, BOOLEAN );
        propToTableAndCol.put( qName, mapping );

    }

    private static void addDateProp( String propNs, String propName, DatabaseTables table, String column ) {
        QName qName = new QName( propNs, propName );
        Triple<String, String, PrimitiveType> mapping = new Triple<String, String, PrimitiveType>( table.name(),
                                                                                                   column, DATE );
        propToTableAndCol.put( qName, mapping );

    }

    private static void addStringProp( String propNs, String propName, DatabaseTables table, String column ) {
        QName qName = new QName( propNs, propName );
        Triple<String, String, PrimitiveType> mapping = new Triple<String, String, PrimitiveType>( table.name(),
                                                                                                   column, STRING );
        propToTableAndCol.put( qName, mapping );
    }

    @Override
    public Object getPostGISValue( Literal literal, PropertyName propName )
                            throws FilterEvaluationException {

        Object pgValue = null;

        if ( propName == null ) {
            pgValue = literal.getValue().toString();
        } else {

            Expr xpath = propName.getAsXPath();

            if ( !( xpath instanceof LocationPath ) ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                           + "': the root expression is not a LocationPath." );
                return null;
            }
            List<QName> steps = new ArrayList<QName>();

            for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
                if ( !( step instanceof NameStep ) ) {
                    LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                               + "': contains an expression that is not a NameStep." );
                    return null;
                }
                NameStep namestep = (NameStep) step;
                if ( namestep.getPredicates() != null && !namestep.getPredicates().isEmpty() ) {
                    LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                               + "': contains a NameStep with a predicate (needs implementation)." );
                    return null;
                }
                String prefix = namestep.getPrefix();
                String localPart = namestep.getLocalName();
                String namespace = propName.getNsContext().translateNamespacePrefixToUri( prefix );
                steps.add( new QName( namespace, localPart, prefix ) );
            }
            if ( steps.size() < 1 || steps.size() > 2 ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                           + "': must contain one or two NameSteps (needs implementation)." );
                return null;
            }

            QName requestedProperty = null;
            if ( steps.size() == 1 ) {
                // step must be equal to a property name of the queried feature
                // if ( ft.getPropertyDeclaration( steps.get( 0 ) ) == null ) {
                // String msg = "Filter contains an invalid PropertyName '" + propName.getPropertyName()
                // + "'. The queried feature type '" + ft.getName()
                // + "' does not have a property with this name.";
                // throw new FilterEvaluationException( msg );
                // }
                requestedProperty = steps.get( 0 );
            } else {
                // 1. step must be equal to the name or alias of the queried feature
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
                requestedProperty = steps.get( 1 );
            }

            String column = getMapping( new PropertyName( requestedProperty ), null ).getTargetField().getColumn();

            if ( column == null ) {
                throw new FilterEvaluationException( column + " doesn't exist!" );
                // pgValue = literal.getValue().toString();
            }

            Object internalValue = XMLValueMangler.xmlToInternal( literal.getValue().toString(),
                                                                  getMapping( new PropertyName( requestedProperty ),
                                                                              null ).getTargetFieldType() );

            pgValue = SQLValueMangler.internalToSQL( internalValue );

            // // TODO implement properly
            // PropertyType pt = mapping.first;
            // if ( pt instanceof SimplePropertyType ) {
            // Object internalValue = XMLValueMangler.xmlToInternal( literal.getValue().toString(),
            // ( (SimplePropertyType) pt ).getPrimitiveType() );
            // pgValue = SQLValueMangler.internalToSQL( internalValue );
            // } else {
            // pgValue = literal.getValue().toString();
            // }
            LOG.info( "pg_value in mapping: " + pgValue );

        }

        return pgValue;
    }

    @Override
    public byte[] getPostGISValue( Geometry literal, PropertyName propName )
                            throws FilterEvaluationException {
        byte[] pgValue = null;

        Expr xpath = propName.getAsXPath();

        if ( !( xpath instanceof LocationPath ) ) {
            LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                       + "': the root expression is not a LocationPath." );
            return null;
        }
        List<QName> steps = new ArrayList<QName>();

        for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
            if ( !( step instanceof NameStep ) ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                           + "': contains an expression that is not a NameStep." );
                return null;
            }
            NameStep namestep = (NameStep) step;
            if ( namestep.getPredicates() != null && !namestep.getPredicates().isEmpty() ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                           + "': contains a NameStep with a predicate (needs implementation)." );
                return null;
            }
            String prefix = namestep.getPrefix();
            String localPart = namestep.getLocalName();
            String namespace = propName.getNsContext().translateNamespacePrefixToUri( prefix );
            steps.add( new QName( namespace, localPart, prefix ) );
        }
        if ( steps.size() < 1 || steps.size() > 2 ) {
            LOG.debug( "Unable to map PropertyName '" + propName.getPropertyName()
                       + "': must contain one or two NameSteps (needs implementation)." );
            return null;
        }

        QName requestedProperty = null;
        if ( steps.size() == 1 ) {
            // step must be equal to a property name of the queried feature
            // if ( ft.getPropertyDeclaration( steps.get( 0 ) ) == null ) {
            // String msg = "Filter contains an invalid PropertyName '" + propName.getPropertyName()
            // + "'. The queried feature type '" + ft.getName()
            // + "' does not have a property with this name.";
            // throw new FilterEvaluationException( msg );
            // }
            requestedProperty = steps.get( 0 );
        } else {
            // 1. step must be equal to the name or alias of the queried feature
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
            requestedProperty = steps.get( 1 );
        }

        String column = getMapping( new PropertyName( requestedProperty ), null ).getTargetField().getColumn();

        if ( column == null ) {
            throw new FilterEvaluationException( column + " doesn't exist!" );
            // pgValue = literal.getValue().toString();
        }

        // pgValue = literal.getValue().toString();
        // // TODO implement properly
        // PropertyType pt = mapping.first;
        // if ( pt instanceof SimplePropertyType<?> ) {
        // Object internalValue = XMLValueMangler.xmlToInternal(
        // literal.getValue().toString(),
        // ( (SimplePropertyType<?>) pt ).getPrimitiveType() );
        // pgValue = SQLValueMangler.internalToSQL( internalValue );
        // } else {
        // pgValue = literal.getValue().toString();
        // }

        try {
            pgValue = WKBWriter.write( literal );
        } catch ( ParseException e ) {
            throw new FilterEvaluationException( e.getMessage() );
        }
        return pgValue;

    }

    /**
     * 
     * @return a map&lang;QName, PropertyNameMapping&rang; can not be <Code>null</Code>
     */
    public Map<QName, Triple<String, String, PrimitiveType>> getPropToTableAndCol() {
        return propToTableAndCol;
    }

    /**
     * @return the tableRecordType
     */
    public static Map<String, String> getTableRecordType() {
        return tableRecordType;
    }
}
