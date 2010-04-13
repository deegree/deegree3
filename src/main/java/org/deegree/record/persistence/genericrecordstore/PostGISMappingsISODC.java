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
import static org.deegree.protocol.csw.CSWConstants.DCT_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.deegree.protocol.ows.OWSCommonXMLAdapter.OWS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.postgis.PostGISMapping;
import org.deegree.filter.sql.postgis.PropertyNameMapping;
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

    private static Map<QName, PropertyNameMapping> propToTableAndCol = new HashMap<QName, PropertyNameMapping>();

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
        propToTableAndCol.put( new QName( APISO_NS, "title" ), new PropertyNameMapping( "isoqp_title", "title", STRING ) );
        propToTableAndCol.put( new QName( APISO_NS, "Title" ), new PropertyNameMapping( "isoqp_title", "title", STRING ) );
        propToTableAndCol.put( new QName( DC_NS, "title" ), new PropertyNameMapping( "isoqp_title", "title", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "abstract" ), new PropertyNameMapping( "isoqp_abstract",
                                                                                           "abstract", STRING ) );
        propToTableAndCol.put( new QName( APISO_NS, "Abstract" ), new PropertyNameMapping( "isoqp_abstract",
                                                                                           "abstract", STRING ) );
        propToTableAndCol.put( new QName( DCT_NS, "abstract" ), new PropertyNameMapping( "isoqp_abstract", "abstract",
                                                                                         STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "BoundingBox" ), new PropertyNameMapping( "isoqp_BoundingBox",
                                                                                              "bbox" ) );
        propToTableAndCol.put( new QName( DC_NS, "coverage" ), new PropertyNameMapping( "isoqp_BoundingBox", "bbox" ) );
        propToTableAndCol.put( new QName( OWS_NS, "BoundingBox" ),
                               new PropertyNameMapping( "isoqp_BoundingBox", "bbox" ) );
        propToTableAndCol.put( new QName( OWS_NS, "boundingBox" ),
                               new PropertyNameMapping( "isoqp_BoundingBox", "bbox" ) );
        propToTableAndCol.put( new QName( APISO_NS, "boundingBox" ), new PropertyNameMapping( "isoqp_BoundingBox",
                                                                                              "bbox" ) );

        propToTableAndCol.put( new QName( APISO_NS, "type" ), new PropertyNameMapping( "isoqp_type", "type", STRING ) );
        propToTableAndCol.put( new QName( APISO_NS, "Type" ), new PropertyNameMapping( "isoqp_type", "type", STRING ) );
        propToTableAndCol.put( new QName( DC_NS, "type" ), new PropertyNameMapping( "isoqp_type", "type", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "format" ), new PropertyNameMapping( "isoqp_format", "format",
                                                                                         STRING ) );
        propToTableAndCol.put( new QName( APISO_NS, "Format" ), new PropertyNameMapping( "isoqp_format", "format",
                                                                                         STRING ) );
        propToTableAndCol.put( new QName( DC_NS, "format" ), new PropertyNameMapping( "isoqp_format", "format", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "subject" ), new PropertyNameMapping( "isoqp_keyword", "keyword",
                                                                                          STRING ) );
        propToTableAndCol.put( new QName( APISO_NS, "Subject" ), new PropertyNameMapping( "isoqp_keyword", "keyword",
                                                                                          STRING ) );
        propToTableAndCol.put( new QName( DC_NS, "subject" ), new PropertyNameMapping( "isoqp_keyword", "keyword",
                                                                                       STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "anyText" ),
                               new PropertyNameMapping( "datasets", "anytext", STRING ) );
        propToTableAndCol.put( new QName( APISO_NS, "AnyText" ),
                               new PropertyNameMapping( "datasets", "anytext", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "identifier" ), new PropertyNameMapping( "qp_identifier",
                                                                                             "identifier", STRING ) );
        propToTableAndCol.put( new QName( APISO_NS, "Identifier" ), new PropertyNameMapping( "qp_identifier",
                                                                                             "identifier", STRING ) );
        propToTableAndCol.put( new QName( DC_NS, "identifier" ), new PropertyNameMapping( "qp_identifier",
                                                                                          "identifier", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "modified" ),
                               new PropertyNameMapping( "datasets", "modified", DATE ) );
        propToTableAndCol.put( new QName( APISO_NS, "Modified" ),
                               new PropertyNameMapping( "datasets", "modified", DATE ) );
        propToTableAndCol.put( new QName( DCT_NS, "modified" ), new PropertyNameMapping( "datasets", "modified", DATE ) );

        propToTableAndCol.put( new QName( APISO_NS, "CRS" ), new PropertyNameMapping( "isoqp_crs", "crs" ) );
        propToTableAndCol.put( new QName( DC_NS, "CRS" ), new PropertyNameMapping( "isoqp_crs", "crs" ) );

        propToTableAndCol.put( new QName( APISO_NS, "association" ), new PropertyNameMapping( "isoqp_association",
                                                                                              "relation" ) );
        propToTableAndCol.put( new QName( APISO_NS, "Association" ), new PropertyNameMapping( "isoqp_association",
                                                                                              "relation" ) );
        propToTableAndCol.put( new QName( DC_NS, "relation" ),
                               new PropertyNameMapping( "isoqp_association", "relation" ) );
        // ----------------------</common queryable properties>------------------------------------
        // ----------------------------------------------------------------------------------------

        // ----------------------------------------------------------------------------------------
        // ----------------------<additional common queryable properties>--------------------------
        propToTableAndCol.put( new QName( APISO_NS, "Language" ), new PropertyNameMapping( "datasets", "language",
                                                                                           STRING ) );
        propToTableAndCol.put( new QName( APISO_NS, "language" ), new PropertyNameMapping( "datasets", "language",
                                                                                           STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "RevisionDate" ), new PropertyNameMapping( "isoqp_revisiondate",
                                                                                               "revisiondate", DATE ) );

        propToTableAndCol.put( new QName( APISO_NS, "AlternateTitle" ),
                               new PropertyNameMapping( "isoqp_alternatetitle", "alternatetitle", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "CreationDate" ), new PropertyNameMapping( "isoqp_creationdate",
                                                                                               "creationdate", DATE ) );

        propToTableAndCol.put( new QName( APISO_NS, "PublicationDate" ),
                               new PropertyNameMapping( "isoqp_publicationdate", "publicationdate", DATE ) );

        propToTableAndCol.put( new QName( APISO_NS, "OrganisationName" ),
                               new PropertyNameMapping( "isoqp_organisationname", "organisationname", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "HasSecurityConstraint" ),
                               new PropertyNameMapping( "datasets", "hassecurityconstraint", BOOLEAN ) );

        propToTableAndCol.put( new QName( APISO_NS, "ResourceIdentifier" ),
                               new PropertyNameMapping( "isoqp_resourceidentifier", "resourceidentifier", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "ParentIdentifier" ), new PropertyNameMapping( "datasets",
                                                                                                   "parentidentifier",
                                                                                                   STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "KeywordType" ), new PropertyNameMapping( "isoqp_keyword",
                                                                                              "keywordType", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "TopicCategory" ),
                               new PropertyNameMapping( "isoqp_topiccategory", "topiccategory", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "ResourceLanguage" ), new PropertyNameMapping( "datasets",
                                                                                                   "resourcelanguage",
                                                                                                   STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "GeographicDescriptionCode" ),
                               new PropertyNameMapping( "isoqp_geographicdescriptioncode", "geographicdescriptioncode",
                                                        STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "Denominator" ),
                               new PropertyNameMapping( "isoqp_spatialresolution", "denominator", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "DistanceValue" ),
                               new PropertyNameMapping( "isoqp_spatialresolution", "distancevalue" ) );

        propToTableAndCol.put( new QName( APISO_NS, "DistanceUOM" ),
                               new PropertyNameMapping( "isoqp_spatialresolution", "distanceuom" ) );

        propToTableAndCol.put( new QName( APISO_NS, "TempExtent_begin" ),
                               new PropertyNameMapping( "isoqp_temporalextent", "tempextent_begin", DATE ) );

        propToTableAndCol.put( new QName( APISO_NS, "TempExtent_end" ),
                               new PropertyNameMapping( "isoqp_temporalextent", "tempextent_end", DATE ) );

        propToTableAndCol.put( new QName( APISO_NS, "ServiceType" ), new PropertyNameMapping( "isoqp_servicetype",
                                                                                              "servicetype", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "ServiceTypeVersion" ),
                               new PropertyNameMapping( "isoqp_servicetypeversion", "servicetypeversion", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "Operation" ), new PropertyNameMapping( "isoqp_operation",
                                                                                            "operation", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "OperatesOn" ), new PropertyNameMapping( "isoqp_operatesondata",
                                                                                             "operateson", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "OperatesOnIdentifier" ),
                               new PropertyNameMapping( "isoqp_operatesondata", "operatesonidentifier", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "OperatesOnName" ),
                               new PropertyNameMapping( "isoqp_operatesondata", "operatesonname", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "CouplingType" ), new PropertyNameMapping( "isoqp_couplingtype",
                                                                                               "couplingtype", STRING ) );

        // ----------------------</additional common queryable properties>-------------------------
        // ----------------------------------------------------------------------------------------

        // ----------------------------------------------------------------------------------------
        // ----------------------<additional queryable properties for INSPIRE>--------------------------
        propToTableAndCol.put( new QName( APISO_NS, "Degree" ), new PropertyNameMapping( "addqp_degree", "degree",
                                                                                         BOOLEAN ) );

        propToTableAndCol.put( new QName( APISO_NS, "AccessConstraints" ),
                               new PropertyNameMapping( "addqp_accessconstraint", "accessconstraint", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "OtherConstraints" ),
                               new PropertyNameMapping( "addqp_otherconstraint", "otherconstraint", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "Classification" ),
                               new PropertyNameMapping( "addqp_classification", "classification", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "ConditionApplyingToAccessAndUse" ),
                               new PropertyNameMapping( "addqp_limitation", "limitation", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "Lineage" ), new PropertyNameMapping( "addqp_lineage", "lineage",
                                                                                          STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "SpecificationTitle" ),
                               new PropertyNameMapping( "addqp_specification", "specificationtitle", STRING ) );

        propToTableAndCol.put( new QName( APISO_NS, "SpecificationDate" ),
                               new PropertyNameMapping( "addqp_specification", "specificationdate", DATE ) );

        propToTableAndCol.put( new QName( APISO_NS, "SpecificationDateType" ),
                               new PropertyNameMapping( "addqp_specification", "specificationdatetype", STRING ) );

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
        format

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
        isoqp_resourceIdentifier,

        /**
         * alternate title of the record
         */
        isoqp_alternatetitle,

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
        isoqp_boundingbox,

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
        addqp_accessConstraint,

        /**
         * legal prerequisites for accessing and using the resource or metadata
         */
        addqp_otherConstraint,

        /**
         * name of the handling restrictions on the resource.
         */
        addqp_classification,

        /**
         * general explanation of the data producer's knowledge about the lineage of a dataset.
         */
        addqp_lineage

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.filter.sql.postgis.PostGISMapping#getMapping(org.deegree.filter.expression.PropertyName)
     */
    @Override
    public PropertyNameMapping getMapping( PropertyName propName )
                            throws FilterEvaluationException {

        for ( QName matchingPropertyName : propToTableAndCol.keySet() ) {
            LOG.debug( matchingPropertyName + " - " + propName.getAsQName() );

            // TODO handle the case that PropertyName is *not* a QName, but a more complex XPath
            if ( propName.getAsQName().equals( matchingPropertyName ) ) {

                return new PropertyNameMapping( propToTableAndCol.get( matchingPropertyName ).getTable(),
                                                propToTableAndCol.get( matchingPropertyName ).getColumn(),
                                                propToTableAndCol.get( matchingPropertyName ).getDbColumnType() );

            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.filter.sql.postgis.PostGISMapping#getPostGISValue(org.deegree.filter.expression.Literal,
     * org.deegree.filter.expression.PropertyName)
     */
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

            String column = getMapping( new PropertyName( requestedProperty ) ).getColumn();

            if ( column == null ) {
                throw new FilterEvaluationException( column + " doesn't exist!" );
                // pgValue = literal.getValue().toString();
            }

            Object internalValue = XMLValueMangler.xmlToInternal(
                                                                  literal.getValue().toString(),
                                                                  getMapping( new PropertyName( requestedProperty ) ).getDbColumnType() );

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

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.filter.sql.postgis.PostGISMapping#getPostGISValue(org.deegree.geometry.Geometry,
     * org.deegree.filter.expression.PropertyName)
     */
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

        String column = getMapping( new PropertyName( requestedProperty ) ).getColumn();

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
    public Map<QName, PropertyNameMapping> getPropToTableAndCol() {

        return propToTableAndCol;
    }

    /**
     * @return the tableRecordType
     */
    public static Map<String, String> getTableRecordType() {
        return tableRecordType;
    }

}
