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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.IOUtils;
import org.deegree.client.core.utils.SQLExecution;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.persistence.postgis.config.PostGISDDLCreator;
import org.deegree.feature.persistence.postgis.config.PostGISFeatureStoreConfigWriter;
import org.deegree.feature.persistence.postgis.jaxb.PostGISFeatureStoreConfig;
import org.deegree.feature.persistence.sql.MappedApplicationSchema;
import org.deegree.feature.persistence.sql.mapper.AppSchemaMapper;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;

/**
 * JSF bean that helps with creating {@link PostGISFeatureStoreConfig} instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@SessionScoped
public class MappingWizardSQL {

    private String jdbcId;

    private String wizardMode = "template";

    private String storageMode = "blob";

    private String[] selectedAppSchemaFiles = new String[0];

    private ApplicationSchema appSchema;

    private AppSchemaInfo appSchemaInfo;

    private MappedApplicationSchema mappedSchema;

    private String storageCrs = "urn:ogc:def:crs:EPSG::4326";

    // TODO
    private String storageSrid = "-1";

    private Integer columnNameLength = 16;

    private Integer tableNameLength = 16;

    public String getFeatureStoreId()
                            throws ClassNotFoundException, SecurityException, NoSuchMethodException,
                            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        Object o = ctx.getSessionMap().get( "configManager" );

        // TODO clean up modularization
        Class<?> c = Class.forName( "org.deegree.console.ConfigManager" );
        Method m = c.getDeclaredMethod( "getNewConfigId" );
        return (String) m.invoke( o );
    }

    public SortedSet<String> getAvailableJdbcConns() {
        SortedSet<String> conns = new TreeSet<String>( ConnectionManager.getConnectionIds() );
        // TODO remove this hack
        conns.remove( "LOCK_DB" );
        return conns;
    }

    public String getSelectedJdbcConn() {
        return jdbcId;
    }

    public void setSelectedJdbcConn( String jdbcId ) {
        this.jdbcId = jdbcId;
    }

    public String getMode() {
        return wizardMode;
    }

    public void setMode( String mode ) {
        this.wizardMode = mode;
    }

    public File getAppSchemaDirectory()
                            throws IOException {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = (DeegreeWorkspace) ctx.getApplicationMap().get( "workspace" );
        File appSchemaDirectory = new File( ws.getLocation(), "appschemas" );
        return appSchemaDirectory;
    }

    public TreeSet<String> getAvailableAppSchemaFiles()
                            throws IOException {
        List<File> files = FileUtils.findFilesForExtensions( getAppSchemaDirectory(), true, "xsd" );
        TreeSet<String> fileNames = new TreeSet<String>();
        String appDirFileName = getAppSchemaDirectory().getCanonicalPath();
        for ( File file : files ) {
            String canonicalFileName = file.getCanonicalPath();
            if ( canonicalFileName.startsWith( appDirFileName ) ) {
                fileNames.add( canonicalFileName.substring( appDirFileName.length() ) );
            }
        }
        return fileNames;
    }

    public String[] getSelectedAppSchemaFiles() {
        return selectedAppSchemaFiles;
    }

    public void setSelectedAppSchemaFiles( String[] files ) {
        System.out.println( "App schema files: " + files );
        this.selectedAppSchemaFiles = files;
    }

    public String getGmlVersion() {
        return appSchema.getXSModel().getVersion().name();
    }

    public AppSchemaInfo getAppSchemaInfo() {
        return appSchemaInfo;
    }

    public String getStorageMode() {
        return storageMode;
    }

    public void setStorageMode( String storageMode ) {
        this.storageMode = storageMode;
    }

    public String getStorageCrs() {
        return storageCrs;
    }

    public void setStorageCrs( String storageCrs ) {
        this.storageCrs = storageCrs;
    }

    public String getStorageSrid() {
        return storageSrid;
    }

    public void setStorageSrid( String storageSrid ) {
        this.storageSrid = storageSrid;
    }

    public String selectMode() {
        if ( "template".equals( wizardMode ) ) {
            return "/console/jsf/wizard";
        } else if ( "schema".equals( wizardMode ) ) {
            return "/console/featurestore/sql/wizard2";
        }
        return "/console/jsf/wizard";
    }

    public String analyzeSchema()
                            throws IOException, ClassNotFoundException, SecurityException, NoSuchMethodException,
                            IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        if ( selectedAppSchemaFiles.length == 0 ) {
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "At least one schema file must be selected.", null );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return null;
        }

        String[] schemaUrls = new String[selectedAppSchemaFiles.length];
        int i = 0;
        for ( String schemaFile : selectedAppSchemaFiles ) {
            File fullFile = new File( getAppSchemaDirectory(), schemaFile );
            URL schemaUrl = fullFile.toURI().toURL();
            schemaUrls[i++] = schemaUrl.toString();
        }

        try {
            ApplicationSchemaXSDDecoder xsdDecoder = new ApplicationSchemaXSDDecoder( null, null, schemaUrls );
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

            ICRS storageCrs = CRSManager.lookup( this.storageCrs );
            boolean createBlobMapping = storageMode.equals( "hybrid" ) || storageMode.equals( "blob" );
            boolean createRelationalMapping = storageMode.equals( "hybrid" ) || storageMode.equals( "relational" );
            AppSchemaMapper mapper = new AppSchemaMapper( appSchema, createBlobMapping, createRelationalMapping,
                                                          storageCrs, storageSrid );
            mappedSchema = mapper.getMappedSchema();
            PostGISFeatureStoreConfigWriter configWriter = new PostGISFeatureStoreConfigWriter( mappedSchema );
            File file = new File( ws.getLocation(), "datasources/feature/" + getFeatureStoreId() + ".xml" );
            FileOutputStream fos = new FileOutputStream( file );
            XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( fos );
            xmlWriter = new IndentingXMLStreamWriter( xmlWriter );

            List<String> schemaUrls = new ArrayList<String>( selectedAppSchemaFiles.length );
            for ( String schemaFile : selectedAppSchemaFiles ) {
                schemaUrls.add( ".." + File.separator + ".." + File.separator + "appschemas" + schemaFile );
            }
            configWriter.writeConfig( xmlWriter, jdbcId, schemaUrls );
            xmlWriter.close();
            IOUtils.closeQuietly( fos );
            System.out.println( "Wrote to file " + file );
        } catch ( Throwable t ) {
            String msg = "Error generating feature store configuration.";
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, t.getMessage() );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return "/console/featurestore/sql/wizard3";
        }
        return "/console/featurestore/sql/wizard4";
    }

    public String createTables() {
        String[] createStmts = new PostGISDDLCreator( mappedSchema ).getDDL();
        SQLExecution execution = new SQLExecution( jdbcId, createStmts, "/console/featurestore/buttons" );
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put( "execution", execution );
        return "/console/generic/sql.jsf?faces-redirect=true";
    }

    public void setColumnNameLength( Integer columnNameLength ) {
        this.columnNameLength = columnNameLength;
    }

    public Integer getColumnNameLength() {
        return columnNameLength;
    }

    public void setTableNameLength( Integer tableNameLength ) {
        this.tableNameLength = tableNameLength;
    }

    public Integer getTableNameLength() {
        return tableNameLength;
    }
}