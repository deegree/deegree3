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
package org.deegree.console.featurestore;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.deegree.gml.GMLVersion.GML_32;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.CRSUtils;
import org.deegree.feature.persistence.postgis.config.PostGISFeatureStoreConfigWriter;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.mapper.AppSchemaMapper;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@SessionScoped
public class MappingWizardSQL {

    private String mode = "template";

    private GMLVersion gmlVersion = GML_32;

    private String schemaLocation = "";

    private ApplicationSchema appSchema;

    private AppSchemaInfo appSchemaInfo;

    private final String fsId = "inspire-au";

    private final String jdbcId = "testconn";

    public String getMode() {
        return mode;
    }

    public void setMode( String mode ) {
        this.mode = mode;
    }

    public void setGmlVersion( String gmlVersion ) {
        this.gmlVersion = GMLVersion.valueOf( gmlVersion );
    }

    public String getGmlVersion() {
        return gmlVersion.name();
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation( String schemaLocation ) {
        this.schemaLocation = schemaLocation;
    }

    public String[] getAvailableGmlVersions() {
        String[] gmlVersions = new String[GMLVersion.values().length];
        int i = 0;
        for ( GMLVersion version : GMLVersion.values() ) {
            gmlVersions[i++] = version.name();
        }
        return gmlVersions;
    }

    public AppSchemaInfo getAppSchemaInfo() {
        return appSchemaInfo;
    }

    public String selectMode() {
        if ( "template".equals( mode ) ) {
            return "/console/jsf/wizard";
        } else if ( "schema".equals( mode ) ) {
            return "/console/featurestore/sql/wizard2";
        }
        return "/console/jsf/wizard";
    }

    public String analyzeSchema() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = (DeegreeWorkspace) ctx.getApplicationMap().get( "workspace" );
        File schemaFile = new File( ws.getLocation(), schemaLocation );
        if ( !schemaFile.exists() ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "No file at location: " + schemaFile.getPath()
                                                                + " found.", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return null;
        }
        try {
            ApplicationSchemaXSDDecoder xsdDecoder = new ApplicationSchemaXSDDecoder( gmlVersion, null, schemaFile );
            appSchema = xsdDecoder.extractFeatureTypeSchema();
            appSchemaInfo = new AppSchemaInfo( appSchema );
        } catch ( Throwable t ) {
            String msg = "Unable to parse GML application schema.";
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, t.getMessage() );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
        return "/console/featurestore/sql/wizard3";
    }

    public String generateConfig() {

        try {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            DeegreeWorkspace ws = (DeegreeWorkspace) ctx.getApplicationMap().get( "workspace" );

            AppSchemaMapper mapper = new AppSchemaMapper( appSchema, false, true, CRSUtils.EPSG_4326, "-1" );
            MappedApplicationSchema mappedSchema = mapper.getMappedSchema();
            PostGISFeatureStoreConfigWriter configWriter = new PostGISFeatureStoreConfigWriter( mappedSchema );
            File file = new File( ws.getLocation(), "datasources/feature/" + fsId + ".xml" );
            FileOutputStream fos = new FileOutputStream( file );
            XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( fos );
            xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
            String schemaPath = "../../" + schemaLocation;
            configWriter.writeConfig( xmlWriter, jdbcId, Collections.singletonList( schemaPath ) );
            xmlWriter.close();
            IOUtils.closeQuietly( fos );
            System.out.println( "Wrote to file " + file );
        } catch ( Throwable t ) {
            String msg = "Error generating feature store configuration.";
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, t.getMessage() );
            FacesContext.getCurrentInstance().addMessage( null, fm );
        }
        return "/console/featurestore/sql/wizard4";
    }
}