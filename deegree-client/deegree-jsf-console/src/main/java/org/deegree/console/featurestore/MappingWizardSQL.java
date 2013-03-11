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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.config.ResourceState.StateType;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.commons.utils.FileUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.console.Config;
import org.deegree.console.ConfigManager;
import org.deegree.console.WorkspaceBean;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.config.SQLFeatureStoreConfigWriter;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.feature.persistence.sql.mapper.AppSchemaMapper;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.SQLDialectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSF bean that helps with creating configurations for the {@link SQLFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@ManagedBean
@SessionScoped
public class MappingWizardSQL {

    private static transient Logger LOG = LoggerFactory.getLogger( MappingWizardSQL.class );

    private String jdbcId;

    private String wizardMode = "template";

    private String storageMode = "blob";

    private String[] selectedAppSchemaFiles = new String[0];

    private AppSchema appSchema;

    private AppSchemaInfo appSchemaInfo;

    private MappedAppSchema mappedSchema;

    private String storageCrs = "EPSG:4326";

    // TODO
    private String storageSrid = "-1";

    private Integer columnNameLength = 16;

    private Integer tableNameLength = 16;

    private Type connectionType;

    private ResourceState resourceState;

    public String getFeatureStoreId()
                            throws ClassNotFoundException, SecurityException, NoSuchMethodException,
                            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        ConfigManager mgr = (ConfigManager) ctx.getSessionMap().get( "configManager" );
        return mgr.getNewConfigId();
    }

    private DeegreeWorkspace getWorkspace() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        return ws;
    }

    public SortedSet<String> getAvailableJdbcConns() {
        ConnectionManager mgr = getWorkspace().getSubsystemManager( ConnectionManager.class );
        SortedSet<String> conns = new TreeSet<String>();
        for ( ResourceState<?> rs : mgr.getStates() ) {
            // TODO remove this hack
            if ( rs.getId().equals( "LOCK_DB" ) ) {
                continue;
            }
            if ( rs.getType().equals( StateType.init_ok ) ) {
                conns.add( rs.getId() );
            }
        }
        return conns;
    }

    public String getSelectedJdbcConn() {
        return jdbcId;
    }

    public void setSelectedJdbcConn( String jdbcId ) {
        this.jdbcId = jdbcId;
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        ConnectionManager mgr = ws.getSubsystemManager( ConnectionManager.class );
        this.connectionType = mgr.getType( jdbcId );
        SQLDialectManager dialectMgr = ws.getSubsystemManager( SQLDialectManager.class );
        if ( dialectMgr != null ) {
            try {
                SQLDialect dialect = dialectMgr.create( jdbcId );
                columnNameLength = dialect.getMaxColumnNameLength();
                tableNameLength = dialect.getMaxTableNameLength();
            } catch ( Throwable t ) {
                FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "SQLDialect error: " + t.getMessage(), null );
                FacesContext.getCurrentInstance().addMessage( null, fm );
            }
        }
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
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
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
        this.selectedAppSchemaFiles = files;
    }

    public String getGmlVersion() {
        return appSchema.getGMLSchema().getVersion().name();
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
            GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaUrls );
            appSchema = xsdDecoder.extractAppSchema();
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
            DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();

            CRSRef storageCrs = CRSManager.getCRSRef( this.storageCrs );
            boolean createBlobMapping = storageMode.equals( "hybrid" ) || storageMode.equals( "blob" );
            boolean createRelationalMapping = storageMode.equals( "hybrid" ) || storageMode.equals( "relational" );
            GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, storageSrid,
                                                                              CoordinateDimension.DIM_2 );
            AppSchemaMapper mapper = new AppSchemaMapper( appSchema, createBlobMapping, createRelationalMapping,
                                                          geometryParams,
                                                          Math.min( tableNameLength, columnNameLength ), true, false );
            mappedSchema = mapper.getMappedSchema();
            SQLFeatureStoreConfigWriter configWriter = new SQLFeatureStoreConfigWriter( mappedSchema );
            File tmpConfigFile = File.createTempFile( "fsconfig", ".xml" );
            FileOutputStream fos = new FileOutputStream( tmpConfigFile );
            XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( fos );
            xmlWriter = new IndentingXMLStreamWriter( xmlWriter );

            List<String> schemaUrls = new ArrayList<String>( selectedAppSchemaFiles.length );
            for ( String schemaFile : selectedAppSchemaFiles ) {
                schemaUrls.add( ".." + File.separator + ".." + File.separator + "appschemas" + schemaFile );
            }
            configWriter.writeConfig( xmlWriter, jdbcId, schemaUrls );
            xmlWriter.close();
            IOUtils.closeQuietly( fos );
            System.out.println( "Wrote to file " + tmpConfigFile );

            // let the resource manager do the dirty work

            ConfigManager mgr = (ConfigManager) ctx.getSessionMap().get( "configManager" );

            // let the resource manager do the dirty work
            try {
                FeatureStoreManager fsMgr = ws.getSubsystemManager( FeatureStoreManager.class );
                this.resourceState = fsMgr.createResource( getFeatureStoreId(), new FileInputStream( tmpConfigFile ) );
                Config c = new Config( this.resourceState, mgr, fsMgr, "/console/featurestore/sql/wizard4", false );
                return c.edit();
            } catch ( Throwable t ) {
                LOG.error( t.getMessage(), t );
                FacesMessage fm = new FacesMessage( SEVERITY_ERROR, "Unable to create config: " + t.getMessage(), null );
                FacesContext.getCurrentInstance().addMessage( null, fm );
                return null;
            }
        } catch ( Throwable t ) {
            LOG.error( t.getMessage(), t );
            String msg = "Error generating feature store configuration.";
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, t.getMessage() );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return "/console/featurestore/sql/wizard3";
        }
    }

    public String createTables() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        DeegreeWorkspace ws = ( (WorkspaceBean) ctx.getApplicationMap().get( "workspace" ) ).getActiveWorkspace();
        FeatureStoreManager fsMgr = ws.getSubsystemManager( FeatureStoreManager.class );
        resourceState = fsMgr.activate( resourceState.getId() );
        SQLFeatureStore store = (SQLFeatureStore) resourceState.getResource();
        String[] createStmts = DDLCreator.newInstance( store.getSchema(), store.getDialect() ).getDDL();
        resourceState = fsMgr.deactivate( resourceState.getId() );
        SQLExecution execution = new SQLExecution( jdbcId, createStmts, "/console/featurestore/sql/wizard5", ws );
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

    public String activateFS() {
        try {
            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
            WorkspaceBean workspaceBean = (WorkspaceBean) ctx.getApplicationMap().get( "workspace" );
            DeegreeWorkspace ws = workspaceBean.getActiveWorkspace();
            FeatureStoreManager fsMgr = ws.getSubsystemManager( FeatureStoreManager.class );
            fsMgr.activate( getFeatureStoreId() );
            workspaceBean.setModified();
        } catch ( Throwable t ) {
            t.printStackTrace();
            String msg = "Error activating new feature store";
            FacesMessage fm = new FacesMessage( SEVERITY_ERROR, msg, t.getMessage() );
            FacesContext.getCurrentInstance().addMessage( null, fm );
            return null;
        }
        return "/console/featurestore/buttons";
    }
}