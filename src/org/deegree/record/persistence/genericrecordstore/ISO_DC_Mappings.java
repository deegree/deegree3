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

import static org.deegree.record.persistence.MappingInfo.ColumnType.DATE;
import static org.deegree.record.persistence.MappingInfo.ColumnType.FLOAT;
import static org.deegree.record.persistence.MappingInfo.ColumnType.INTEGER;
import static org.deegree.record.persistence.MappingInfo.ColumnType.STRING;

import java.util.HashMap;
import java.util.Map;

import org.deegree.record.persistence.MappingInfo;
import org.deegree.record.persistence.Profile_DB_Mappings;

/**
 * Implementation of the {@link Profile_DB_Mappings}. It's the base class for access to the backend. Is there any change
 * in the database schema for the {@link ISORecordStore} then in this class should be changed the binding, as well.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ISO_DC_Mappings implements Profile_DB_Mappings {

    private Map<String, MappingInfo> propToTableAndCol = new HashMap<String, MappingInfo>();

    /**
     * 
     * Enumeration of the common column names that are used in the backend for each databasetable.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum commonColumnNames {

        /**
         * the primarykey of the databasetables
         */
        id,

        /**
         * the foreignkey of the databasetables
         */
        fk_datasets

    }

    /**
     * Enumeration of the databasetables.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author: thomas $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum databaseTables {
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
        isoqp_crs

    }

    /**
     * Private constructor for no instantiating from outside.
     */
    public ISO_DC_Mappings() {

        // ----------------------------------------------------------------------------------------
        // ----------------------<common queryable properties>-------------------------------------
        propToTableAndCol.put( "apiso:title", new MappingInfo( "isoqp_title", "title", STRING ) );
        propToTableAndCol.put( "dc:title", new MappingInfo( "isoqp_title", "title", STRING ) );
        propToTableAndCol.put( "Title", new MappingInfo( "isoqp_title", "title", STRING ) );

        propToTableAndCol.put( "apiso:abstract", new MappingInfo( "isoqp_abstract", "abstract", STRING ) );
        propToTableAndCol.put( "dct:abstract", new MappingInfo( "isoqp_abstract", "abstract", STRING ) );
        propToTableAndCol.put( "Abstract", new MappingInfo( "isoqp_abstract", "abstract", STRING ) );

        propToTableAndCol.put( "apiso:BoundingBox", new MappingInfo( "isoqp_BoundingBox", "bbox", STRING ) );
        propToTableAndCol.put( "dc:coverage", new MappingInfo( "isoqp_BoundingBox", "bbox", STRING ) );
        propToTableAndCol.put( "ows:BoundingBox", new MappingInfo( "isoqp_BoundingBox", "bbox", STRING ) );

        propToTableAndCol.put( "apiso:type", new MappingInfo( "isoqp_type", "type", STRING ) );
        propToTableAndCol.put( "dc:type", new MappingInfo( "isoqp_type", "type", STRING ) );
        propToTableAndCol.put( "Type", new MappingInfo( "isoqp_type", "type", STRING ) );

        propToTableAndCol.put( "apiso:format", new MappingInfo( "isoqp_format", "format", STRING ) );
        propToTableAndCol.put( "dc:format", new MappingInfo( "isoqp_format", "format", STRING ) );
        propToTableAndCol.put( "Format", new MappingInfo( "isoqp_format", "format", STRING ) );

        propToTableAndCol.put( "apiso:subject", new MappingInfo( "isoqp_keyword", "keyword", STRING ) );
        propToTableAndCol.put( "dc:subject", new MappingInfo( "isoqp_keyword", "keyword", STRING ) );
        propToTableAndCol.put( "Subject", new MappingInfo( "isoqp_keyword", "keyword", STRING ) );

        propToTableAndCol.put( "apiso:anyText", new MappingInfo( "datasets", "anytext", STRING ) );
        propToTableAndCol.put( "AnyText", new MappingInfo( "datasets", "anytext", STRING ) );

        propToTableAndCol.put( "apiso:identifier", new MappingInfo( "qp_identifier", "identifier", STRING ) );
        propToTableAndCol.put( "dc:identifier", new MappingInfo( "qp_identifier", "identifier", STRING ) );
        propToTableAndCol.put( "Identifier", new MappingInfo( "qp_identifier", "identifier", STRING ) );

        propToTableAndCol.put( "apiso:modified", new MappingInfo( "datasets", "modified", DATE ) );
        propToTableAndCol.put( "dct:modified", new MappingInfo( "datasets", "modified", DATE ) );
        propToTableAndCol.put( "Modified", new MappingInfo( "datasets", "modified", DATE ) );

        propToTableAndCol.put( "apiso:CRS", new MappingInfo( "isoqp_crs", "crs", STRING ) );
        propToTableAndCol.put( "CRS", new MappingInfo( "isoqp_crs", "crs", STRING ) );
        propToTableAndCol.put( "dc:CRS", new MappingInfo( "isoqp_crs", "crs", STRING ) );

        propToTableAndCol.put( "apiso:association", new MappingInfo( "isoqp_association", "relation", STRING ) );
        propToTableAndCol.put( "dc:relation", new MappingInfo( "isoqp_association", "relation", STRING ) );
        propToTableAndCol.put( "Association", new MappingInfo( "isoqp_association", "relation", STRING ) );
        // ----------------------</common queryable properties>------------------------------------
        // ----------------------------------------------------------------------------------------

        // ----------------------------------------------------------------------------------------
        // ----------------------<additional common queryable properties>--------------------------
        propToTableAndCol.put( "apiso:Language", new MappingInfo( "datasets", "language", STRING ) );

        propToTableAndCol.put( "apiso:RevisionDate", new MappingInfo( "isoqp_revisiondate", "revisiondate", DATE ) );

        propToTableAndCol.put( "apiso:AlternateTitle", new MappingInfo( "isoqp_alternatetitle", "alternatetitle",
                                                                        STRING ) );

        propToTableAndCol.put( "apiso:CreationDate", new MappingInfo( "isoqp_creationdate", "creationdate", DATE ) );

        propToTableAndCol.put( "apiso:PublicationDate", new MappingInfo( "isoqp_publicationdate", "publicationdate",
                                                                         DATE ) );

        propToTableAndCol.put( "apiso:OrganisationName", new MappingInfo( "isoqp_organisationname", "organisationname",
                                                                          STRING ) );

        propToTableAndCol.put( "apiso:HasSecurityConstraint", new MappingInfo( "datasets", "hassecurityconstraint",
                                                                               STRING ) );

        propToTableAndCol.put( "apiso:ResourceIdentifier", new MappingInfo( "isoqp_resourceidentifier",
                                                                            "resourceidentifier", STRING ) );

        propToTableAndCol.put( "apiso:ParentIdentifier", new MappingInfo( "datasets", "parentidentifier", STRING ) );

        propToTableAndCol.put( "apiso:KeywordType", new MappingInfo( "isoqp_keyword", "keywordType", STRING ) );

        propToTableAndCol.put( "apiso:TopicCategory", new MappingInfo( "isoqp_topiccategory", "topiccategory", STRING ) );

        propToTableAndCol.put( "apiso:ResourceLanguage", new MappingInfo( "datasets", "resourcelanguage", STRING ) );

        propToTableAndCol.put( "apiso:GeographicDescriptionCode", new MappingInfo( "isoqp_geographicdescriptioncode",
                                                                                   "geographicdescriptioncode", STRING ) );

        propToTableAndCol.put( "apiso:Denominator", new MappingInfo( "isoqp_spatialresolution", "denominator", INTEGER ) );

        propToTableAndCol.put( "apiso:DistanceValue", new MappingInfo( "isoqp_spatialresolution", "distancevalue",
                                                                       FLOAT ) );

        propToTableAndCol.put( "apiso:DistanceUOM", new MappingInfo( "isoqp_spatialresolution", "distanceuom", STRING ) );

        propToTableAndCol.put( "apiso:TempExtent_begin", new MappingInfo( "isoqp_temporalextent", "tempextent_begin",
                                                                          DATE ) );

        propToTableAndCol.put( "apiso:TempExtent_end", new MappingInfo( "isoqp_temporalextent", "tempextent_end", DATE ) );

        propToTableAndCol.put( "apiso:ServiceType", new MappingInfo( "isoqp_servicetype", "servicetype", STRING ) );

        propToTableAndCol.put( "apiso:ServiceTypeVersion", new MappingInfo( "isoqp_servicetypeversion",
                                                                            "servicetypeversion", STRING ) );

        propToTableAndCol.put( "apiso:Operation", new MappingInfo( "isoqp_operation", "operation", STRING ) );

        propToTableAndCol.put( "apiso:OperatesOn", new MappingInfo( "isoqp_operatesondata", "operateson", STRING ) );

        propToTableAndCol.put( "apiso:OperatesOnIdentifier", new MappingInfo( "isoqp_operatesondata",
                                                                              "operatesonidentifier", STRING ) );

        propToTableAndCol.put( "apiso:OperatesOnName", new MappingInfo( "isoqp_operatesondata", "operatesonname",
                                                                        STRING ) );

        propToTableAndCol.put( "apiso:CouplingType", new MappingInfo( "isoqp_couplingtype", "couplingtype", STRING ) );

        // ----------------------</additional common queryable properties>-------------------------
        // ----------------------------------------------------------------------------------------

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.Profile_DB_Mappings#getPropToTableAndCol()
     */
    @Override
    public Map<String, MappingInfo> getPropToTableAndCol() {

        return propToTableAndCol;
    }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.deegree.filter.sql.postgis.PostGISMapping#getMapping(org.deegree.filter.expression.PropertyName)
    // */
    // @Override
    // public PropertyNameMapping getMapping( PropertyName propName )
    // throws FilterEvaluationException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.deegree.filter.sql.postgis.PostGISMapping#getPostGISValue(org.deegree.filter.expression.Literal,
    // * org.deegree.filter.expression.PropertyName)
    // */
    // @Override
    // public Object getPostGISValue( Literal literal, PropertyName propName )
    // throws FilterEvaluationException {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.deegree.filter.sql.postgis.PostGISMapping#getPostGISValue(org.deegree.geometry.Geometry,
    // * org.deegree.filter.expression.PropertyName)
    // */
    // @Override
    // public byte[] getPostGISValue( Geometry literal, PropertyName propName )
    // throws FilterEvaluationException {
    // // TODO Auto-generated method stub
    // return null;
    // }

}
