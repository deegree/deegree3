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
package org.deegree.metadata.iso.persistence;

import static org.deegree.commons.tom.primitive.BaseType.BOOLEAN;
import static org.deegree.commons.tom.primitive.BaseType.DATE;
import static org.deegree.commons.tom.primitive.BaseType.DECIMAL;
import static org.deegree.commons.tom.primitive.BaseType.INTEGER;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
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

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.SQLValueMangler;
import org.deegree.commons.tom.primitive.XMLValueMangler;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.Join;
import org.deegree.filter.sql.PrimitivePropertyNameMapping;
import org.deegree.filter.sql.PropertyNameMapper;
import org.deegree.filter.sql.PropertyNameMapping;
import org.deegree.filter.sql.TableAliasManager;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBWriter;
import org.deegree.metadata.i18n.Messages;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.slf4j.Logger;

import com.vividsolutions.jts.io.ParseException;

/**
 * Implementation of the {@link PropertyNameMapper}. It's the base class for access to the backend. Is there any change
 * in the database schema for the {@link ISOMetadataStore} then in this class should be changed the binding, as well.
 * <p>
 * TODO denominator, distanceUOM, distanceValue put a type in
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MSSQLMappingsISODC implements PropertyNameMapper {

    private static final Logger LOG = getLogger( MSSQLMappingsISODC.class );

    private static Map<QName, Triple<Pair<String, String>, Boolean, BaseType>> propToTableAndCol = new HashMap<QName, Triple<Pair<String, String>, Boolean, BaseType>>();

    /**
     * XML element name in the representation of the response
     */
    public final static String RECORD = "Record";

    static {

        // ----------------------------------------------------------------------------------------
        // ----------------------<common queryable properties>-------------------------------------

        addStringProp( APISO_NS, "title", DatabaseTables.idxtb_main, "title", true );
        addStringProp( APISO_NS, "Title", DatabaseTables.idxtb_main, "title", true );
        addStringProp( "", "Title", DatabaseTables.idxtb_main, "title", true );
        addStringProp( DC_NS, "Title", DatabaseTables.idxtb_main, "title", true );
        addStringProp( CSW_202_NS, "Title", DatabaseTables.idxtb_main, "title", true );
        addStringProp( APISO_NS, "abstract", DatabaseTables.idxtb_main, "abstract", true );
        addStringProp( APISO_NS, "Abstract", DatabaseTables.idxtb_main, "abstract", true );
        addStringProp( DCT_NS, "Abstract", DatabaseTables.idxtb_main, "abstract", true );
        addStringProp( "", "Abstract", DatabaseTables.idxtb_main, "abstract", true );
        addStringProp( CSW_202_NS, "Abstract", DatabaseTables.idxtb_main, "abstract", true );
        addStringProp( APISO_NS, "BoundingBox", DatabaseTables.idxtb_main, "bbox", false );
        addStringProp( DC_NS, "coverage", DatabaseTables.idxtb_main, "bbox", false );
        addStringProp( OWS_NS, "BoundingBox", DatabaseTables.idxtb_main, "bbox", false );
        addStringProp( OWS_NS, "boundingBox", DatabaseTables.idxtb_main, "bbox", false );
        addStringProp( "", "boundingBox", DatabaseTables.idxtb_main, "bbox", false );
        addStringProp( CSW_202_NS, "BoundingBox", DatabaseTables.idxtb_main, "bbox", false );
        addStringProp( APISO_NS, "type", DatabaseTables.idxtb_main, "type", false );
        addStringProp( APISO_NS, "Type", DatabaseTables.idxtb_main, "type", false );
        addStringProp( DC_NS, "Type", DatabaseTables.idxtb_main, "type", false );
        addStringProp( "", "Type", DatabaseTables.idxtb_main, "type", false );
        addStringProp( CSW_202_NS, "Type", DatabaseTables.idxtb_main, "type", false );
        addStringProp( APISO_NS, "format", DatabaseTables.idxtb_main, "formats", true );
        addStringProp( APISO_NS, "Format", DatabaseTables.idxtb_main, "formats", true );
        addStringProp( DC_NS, "Format", DatabaseTables.idxtb_main, "formats", true );
        addStringProp( "", "Format", DatabaseTables.idxtb_main, "formats", true );
        addStringProp( CSW_202_NS, "Format", DatabaseTables.idxtb_main, "formats", true );
        addStringProp( APISO_NS, "Subject", DatabaseTables.idxtb_keyword, "keywords", true );
        addStringProp( APISO_NS, "subject", DatabaseTables.idxtb_keyword, "keywords", true );
        addStringProp( DC_NS, "Subject", DatabaseTables.idxtb_keyword, "keywords", true );
        addStringProp( "", "Subject", DatabaseTables.idxtb_keyword, "keywords", true );
        addStringProp( CSW_202_NS, "Subject", DatabaseTables.idxtb_keyword, "keywords", true );
        addStringProp( APISO_NS, "AnyText", DatabaseTables.idxtb_main, "anytext", false );
        addStringProp( APISO_NS, "anyText", DatabaseTables.idxtb_main, "anytext", false );
        addStringProp( CSW_202_NS, "AnyText", DatabaseTables.idxtb_main, "anytext", false );
        addStringProp( "", "AnyText", DatabaseTables.idxtb_main, "anytext", false );
        addStringProp( APISO_NS, "identifier", DatabaseTables.idxtb_main, "fileidentifier", false );
        addStringProp( APISO_NS, "Identifier", DatabaseTables.idxtb_main, "fileidentifier", false );
        addStringProp( DC_NS, "Identifier", DatabaseTables.idxtb_main, "fileidentifier", false );
        addStringProp( "", "Identifier", DatabaseTables.idxtb_main, "fileidentifier", false );
        addStringProp( CSW_202_NS, "Identifier", DatabaseTables.idxtb_main, "fileidentifier", false );
        addDateProp( APISO_NS, "modified", DatabaseTables.idxtb_main, "modified" );
        addDateProp( APISO_NS, "Modified", DatabaseTables.idxtb_main, "modified" );
        addDateProp( DCT_NS, "Modified", DatabaseTables.idxtb_main, "modified" );
        addDateProp( "", "Modified", DatabaseTables.idxtb_main, "modified" );
        addDateProp( CSW_202_NS, "Modified", DatabaseTables.idxtb_main, "modified" );
        addStringProp( APISO_NS, "CRS", DatabaseTables.idxtb_crs, "crsid", false );
        addStringProp( DC_NS, "CRS", DatabaseTables.idxtb_crs, "crsid", false );
        addStringProp( "", "CRS", DatabaseTables.idxtb_crs, "crsid", false );

        // ----------------------</common queryable properties>------------------------------------
        // ----------------------------------------------------------------------------------------

        // ----------------------------------------------------------------------------------------
        // ----------------------<additional common queryable properties>--------------------------
        addStringProp( APISO_NS, "Language", DatabaseTables.idxtb_main, "language", false );
        addStringProp( APISO_NS, "language", DatabaseTables.idxtb_main, "language", false );
        addDateProp( APISO_NS, "RevisionDate", DatabaseTables.idxtb_main, "revisiondate" );
        addDateProp( APISO_NS, "CreationDate", DatabaseTables.idxtb_main, "creationdate" );
        addStringProp( APISO_NS, "AlternateTitle", DatabaseTables.idxtb_main, "alternatetitles", true );
        addDateProp( APISO_NS, "PublicationDate", DatabaseTables.idxtb_main, "publicationdate" );
        addStringProp( APISO_NS, "OrganisationName", DatabaseTables.idxtb_main, "organisationname", false );
        addBooleanProp( APISO_NS, "HasSecurityConstraints", DatabaseTables.idxtb_main, "hassecurityconstraint" );
        addStringProp( APISO_NS, "ResourceIdentifier", DatabaseTables.idxtb_main, "resourceid", false );
        addStringProp( APISO_NS, "ParentIdentifier", DatabaseTables.idxtb_main, "parentid", false );
        addStringProp( APISO_NS, "KeywordType", DatabaseTables.idxtb_keyword, "keywordtype", false );
        addStringProp( APISO_NS, "TopicCategory", DatabaseTables.idxtb_main, "topicCategories", true );
        addStringProp( APISO_NS, "ResourceLanguage", DatabaseTables.idxtb_main, "resourcelanguage", false );
        addStringProp( APISO_NS, "GeographicDescriptionCode", DatabaseTables.idxtb_main, "geographicdescriptioncode",
                       true );
        addIntProp( APISO_NS, "Denominator", DatabaseTables.idxtb_main, "denominator" );
        addDecimalProp( APISO_NS, "DistanceValue", DatabaseTables.idxtb_main, "distancevalue" );
        addStringProp( APISO_NS, "DistanceUOM", DatabaseTables.idxtb_main, "distanceuom", false );
        addDateProp( APISO_NS, "TempExtent_begin", DatabaseTables.idxtb_main, "tempextent_begin" );
        addDateProp( APISO_NS, "TempExtent_end", DatabaseTables.idxtb_main, "tempextent_end" );
        addStringProp( APISO_NS, "ServiceType", DatabaseTables.idxtb_main, "servicetype", false );
        addStringProp( APISO_NS, "ServiceTypeVersion", DatabaseTables.idxtb_main, "servicetypeversion", true );

        addStringProp( APISO_NS, "Operation", DatabaseTables.idxtb_main, "operations", true );
        addStringProp( APISO_NS, "OperatesOn", DatabaseTables.idxtb_operatesondata, "operateson", false );
        addStringProp( APISO_NS, "OperatesOnIdentifier", DatabaseTables.idxtb_operatesondata, "operatesonid", false );
        addStringProp( APISO_NS, "OperatesOnName", DatabaseTables.idxtb_operatesondata, "operatesonname", false );
        addStringProp( APISO_NS, "CouplingType", DatabaseTables.idxtb_main, "couplingtype", false );

        // ----------------------</additional common queryable properties>-------------------------
        // ----------------------------------------------------------------------------------------

        // ----------------------------------------------------------------------------------------
        // ----------------------<additional queryable properties for INSPIRE>--------------------------

        addBooleanProp( APISO_NS, "Degree", DatabaseTables.idxtb_main, "degree" );
        addStringProp( APISO_NS, "AccessConstraints", DatabaseTables.idxtb_constraint, "accessconstraints", true );
        addStringProp( APISO_NS, "OtherConstraints", DatabaseTables.idxtb_constraint, "otherconstraints", true );
        addStringProp( APISO_NS, "Classification", DatabaseTables.idxtb_constraint, "classification", false );
        addStringProp( APISO_NS, "ConditionApplyingToAccessAndUse", DatabaseTables.idxtb_constraint,
                       "conditionapptoacc", true );
        addStringProp( APISO_NS, "Lineage", DatabaseTables.idxtb_main, "lineage", true );
        addStringProp( APISO_NS, "SpecificationTitle", DatabaseTables.idxtb_main, "spectitle", true );
        addStringProp( APISO_NS, "SpecificationDateType", DatabaseTables.idxtb_main, "specdatetype", false );
        addDateProp( APISO_NS, "SpecificationDate", DatabaseTables.idxtb_main, "specdate" );

        // ----------------------</additional queryable properties for INSPIRE>-------------------------
        // ----------------------------------------------------------------------------------------

    }

    /**
     * 
     * The common column names that are used in the backend for each databasetable.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum CommonColumnNames {

        /**
         * the primarykey of a databasetable
         */
        id,

        /**
         * the identifier of the record
         */
        fileidentifier,

        /**
         * the resourceIdentifier of the record
         */
        resourceid,

        /**
         * the BLOB data for the reecord
         */
        recordfull,

        /**
         * the foreign key from sub idx to idx_main
         */
        fk_main;

    }

    /**
     * The names of the databasetables that are used in the backend.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum DatabaseTables {
        /**
         * main databasetable, all of the other tables derive from this table
         */
        idxtb_main, idxtb_constraint, idxtb_crs, idxtb_keyword, idxtb_operatesondata
    }

    @Override
    public PropertyNameMapping getMapping( PropertyName propName, TableAliasManager aliasManager )
                            throws FilterEvaluationException {

        PropertyNameMapping mapping = null;

        QName qName = propName.getAsQName();
        if ( qName == null ) {
            String msg = Messages.getMessage( "WARN_PROPNAME_MAPPING", propName );
            LOG.debug( msg );
        } else {

            Triple<Pair<String, String>, Boolean, BaseType> tableColumn = propToTableAndCol.get( qName );

            if ( tableColumn != null ) {
                String mainTable = DatabaseTables.idxtb_main.name();
                String id = CommonColumnNames.id.name();
                String fk_main = CommonColumnNames.fk_main.name();
                List<Join> joins = new ArrayList<Join>();
                if ( !tableColumn.first.first.equals( mainTable ) ) {
                    DBField from = new DBField( mainTable, id );
                    DBField to = new DBField( tableColumn.first.first, fk_main );
                    to.setAlias( aliasManager.generateNew() );
                    joins.add( new Join( from, to, null, 0 ) );
                }
                DBField valueField = new DBField( tableColumn.first.first, tableColumn.first.second );
                mapping = new PrimitivePropertyNameMapping( valueField, tableColumn.third.getSQLType(), joins,
                                                            new PrimitiveType( tableColumn.third ), tableColumn.second );
            } else {
                String msg = Messages.getMessage( "ERROR_PROPNAME_MAPPING", qName );
                LOG.debug( msg );
                throw new FilterEvaluationException( msg );
            }
        }
        return mapping;
    }

    private static void addBooleanProp( String propNs, String propName, DatabaseTables table, String column ) {
        QName qName = new QName( propNs, propName );
        Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
                                                                                                                       new Pair<String, String>(
                                                                                                                                                 table.name(),
                                                                                                                                                 column ),
                                                                                                                       false,
                                                                                                                       BOOLEAN );
        propToTableAndCol.put( qName, mapping );

    }

    private static void addDateProp( String propNs, String propName, DatabaseTables table, String column ) {
        QName qName = new QName( propNs, propName );
        Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
                                                                                                                       new Pair<String, String>(
                                                                                                                                                 table.name(),
                                                                                                                                                 column ),
                                                                                                                       false,
                                                                                                                       DATE );
        propToTableAndCol.put( qName, mapping );

    }

    private static void addStringProp( String propNs, String propName, DatabaseTables table, String column,
                                       boolean concatenated ) {
        QName qName = new QName( propNs, propName );
        Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
                                                                                                                       new Pair<String, String>(
                                                                                                                                                 table.name(),
                                                                                                                                                 column ),
                                                                                                                       concatenated,
                                                                                                                       STRING );
        propToTableAndCol.put( qName, mapping );
    }

    private static void addIntProp( String propNs, String propName, DatabaseTables table, String column ) {
        QName qName = new QName( propNs, propName );
        Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
                                                                                                                       new Pair<String, String>(
                                                                                                                                                 table.name(),
                                                                                                                                                 column ),
                                                                                                                       false,
                                                                                                                       INTEGER );
        propToTableAndCol.put( qName, mapping );
    }

    private static void addDecimalProp( String propNs, String propName, DatabaseTables table, String column ) {
        QName qName = new QName( propNs, propName );
        Triple<Pair<String, String>, Boolean, BaseType> mapping = new Triple<Pair<String, String>, Boolean, BaseType>(
                                                                                                                       new Pair<String, String>(
                                                                                                                                                 table.name(),
                                                                                                                                                 column ),
                                                                                                                       false,
                                                                                                                       DECIMAL );
        propToTableAndCol.put( qName, mapping );
    }

    @Override
    public Object getSQLValue( Literal<?> literal, PropertyName propName )
                            throws FilterEvaluationException {

        Object sqlValue = null;

        if ( propName == null ) {
            sqlValue = literal.getValue().toString();
        } else {

            Expr xpath = propName.getAsXPath();

            if ( !( xpath instanceof LocationPath ) ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getAsText()
                           + "': the root expression is not a LocationPath." );
                return null;
            }
            List<QName> steps = new ArrayList<QName>();

            for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
                if ( !( step instanceof NameStep ) ) {
                    LOG.debug( "Unable to map PropertyName '" + propName.getAsText()
                               + "': contains an expression that is not a NameStep." );
                    return null;
                }
                NameStep namestep = (NameStep) step;
                if ( namestep.getPredicates() != null && !namestep.getPredicates().isEmpty() ) {
                    LOG.debug( "Unable to map PropertyName '" + propName.getAsText()
                               + "': contains a NameStep with a predicate (needs implementation)." );
                    return null;
                }
                String prefix = namestep.getPrefix();
                String localPart = namestep.getLocalName();
                String namespace = propName.getNsContext().translateNamespacePrefixToUri( prefix );
                steps.add( new QName( namespace, localPart, prefix ) );
            }
            if ( steps.size() < 1 || steps.size() > 2 ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getAsText()
                           + "': must contain one or two NameSteps (needs implementation)." );
                return null;
            }

            QName requestedProperty = null;
            if ( steps.size() == 1 ) {
                requestedProperty = steps.get( 0 );
            } else {
                requestedProperty = steps.get( 1 );
            }

            String column = getMapping( new PropertyName( requestedProperty ), null ).getTargetField().getColumn();

            if ( column == null ) {
                throw new FilterEvaluationException( Messages.getMessage( "ERROR_COLUMN_NOT_EXISTS", column ) );
            }

            PropertyNameMapping mapping = getMapping( new PropertyName( requestedProperty ), null );
            if ( mapping instanceof PrimitivePropertyNameMapping ) {
                PrimitivePropertyNameMapping primitiveMapping = (PrimitivePropertyNameMapping) mapping;
                Object internalValue = XMLValueMangler.xmlToInternal( literal.getValue().toString(),
                                                                      primitiveMapping.getType().getBaseType() );
                sqlValue = SQLValueMangler.internalToSQL( internalValue );
                LOG.debug( "sqlValue in mapping: " + sqlValue );
            } else {
                throw new FilterEvaluationException( "Cannot treat geometry column as literal." );
            }
        }
        return sqlValue;
    }

    @Override
    public byte[] getSQLValue( Geometry literal, PropertyName propName )
                            throws FilterEvaluationException {
        byte[] pgValue = null;

        Expr xpath = propName.getAsXPath();

        if ( !( xpath instanceof LocationPath ) ) {
            LOG.debug( "Unable to map PropertyName '" + propName.getAsText()
                       + "': the root expression is not a LocationPath." );
            return null;
        }
        List<QName> steps = new ArrayList<QName>();

        for ( Object step : ( (LocationPath) xpath ).getSteps() ) {
            if ( !( step instanceof NameStep ) ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getAsText()
                           + "': contains an expression that is not a NameStep." );
                return null;
            }
            NameStep namestep = (NameStep) step;
            if ( namestep.getPredicates() != null && !namestep.getPredicates().isEmpty() ) {
                LOG.debug( "Unable to map PropertyName '" + propName.getAsText()
                           + "': contains a NameStep with a predicate (needs implementation)." );
                return null;
            }
            String prefix = namestep.getPrefix();
            String localPart = namestep.getLocalName();
            String namespace = propName.getNsContext().translateNamespacePrefixToUri( prefix );
            steps.add( new QName( namespace, localPart, prefix ) );
        }
        if ( steps.size() < 1 || steps.size() > 2 ) {
            LOG.debug( "Unable to map PropertyName '" + propName.getAsText()
                       + "': must contain one or two NameSteps (needs implementation)." );
            return null;
        }

        QName requestedProperty = null;
        if ( steps.size() == 1 ) {
            requestedProperty = steps.get( 0 );
        } else {
            requestedProperty = steps.get( 1 );
        }

        String column = getMapping( new PropertyName( requestedProperty ), null ).getTargetField().getColumn();

        if ( column == null ) {
            throw new FilterEvaluationException( Messages.getMessage( "ERROR_COLUMN_NOT_EXISTS", column ) );
        }

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
    public Map<QName, Triple<Pair<String, String>, Boolean, BaseType>> getPropToTableAndCol() {
        return propToTableAndCol;
    }
}