//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.mdeditor.configuration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.client.mdeditor.configuration.codelist.CodeListParser;
import org.deegree.client.mdeditor.configuration.form.FormConfigurationParser;
import org.deegree.client.mdeditor.model.CodeList;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormConfigurationDescription;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class Configuration {

    private URL dataDirUrl;

    private URL exportDirUrl;

    private List<FormConfigurationDescription> formConfigurationDescriptions;

    private Map<String, FormConfiguration> globalConfigurations = new HashMap<String, FormConfiguration>();

    private List<FormConfigurationDescription> globalConfigurationDescriptions;

    private Map<String, FormConfiguration> formConfigurations = new HashMap<String, FormConfiguration>();

    private List<URL> codeListUrls;

    private List<CodeList> codeLists = new ArrayList<CodeList>();

    private boolean codeListParsed = false;

    private String selectedConfiguration;

    /**
     * @param dataDirUrl
     * @param exportDirUrl
     * @param formConfigurations
     * @param globalConfigurations
     * @param codeLists
     */
    public Configuration( URL dataDirUrl, URL exportDirUrl,
                          List<FormConfigurationDescription> formConfigurationDescriptions,
                          List<FormConfigurationDescription> globalConfigurationDescriptions, List<URL> codeListUrls ) {
        this.dataDirUrl = dataDirUrl;
        this.exportDirUrl = exportDirUrl;
        this.formConfigurationDescriptions = formConfigurationDescriptions;
        this.globalConfigurationDescriptions = globalConfigurationDescriptions;
        this.codeListUrls = codeListUrls;
    }

    public URL getDataDirURL() {
        return dataDirUrl;
    }

    public URL getExportDirURL() {
        return exportDirUrl;
    }

    public File getDataDir() {
        return new File( dataDirUrl.getPath() );
    }

    public File getExportDir() {
        return new File( exportDirUrl.getPath() );
    }

    public List<FormConfigurationDescription> getFormConfigurations() {
        return formConfigurationDescriptions;
    }

    public List<FormConfigurationDescription> getGlobalConfigurations() {
        return globalConfigurationDescriptions;
    }

    public CodeList getCodeList( String id )
                            throws ConfigurationException {
        parseCodeLists();
        System.out.println( id );
        for ( CodeList cl : codeLists ) {
            System.out.println( cl.getId() );
            if ( cl.getId().equals( id ) ) {
                return cl;
            }
        }
        return null;
    }

    public List<CodeList> getCodeLists()
                            throws ConfigurationException {
        parseCodeLists();
        return codeLists;
    }

    private void parseCodeLists()
                            throws ConfigurationException {
        if ( !codeListParsed ) {
            for ( URL url : codeListUrls ) {
                codeLists.addAll( CodeListParser.parseConfiguration( url ) );
            }
            codeListParsed = true;
        }
    }

    /**
     * @return the form configuration wich is currently selected
     * @throws ConfigurationException
     */
    public FormConfiguration getSelectedFormConfiguration()
                            throws ConfigurationException {
        return getFormConfiguration( selectedConfiguration );
    }

    /**
     * @param id
     *            identifier of the configuration
     * @return the form configuration with the given key or null, if the configuration does nor contain a form
     *         configuration with the given id
     * @throws ConfigurationException
     */
    public FormConfiguration getFormConfiguration( String id )
                            throws ConfigurationException {
        if ( !formConfigurations.containsKey( id ) ) {
            for ( FormConfigurationDescription conf : formConfigurationDescriptions ) {
                if ( conf.getId().equals( id ) ) {
                    FormConfigurationParser parser = new FormConfigurationParser();
                    formConfigurations.put( id, parser.parseConfiguration( conf.getConfUrl() ) );
                }
            }
        }
        return formConfigurations.get( id );
    }

    /**
     * @param id
     *            identifier of the configuration
     * @return the global configuration with the given key or null, if the configuration does nor contain a global
     *         configuration with the given id
     * @throws ConfigurationException
     */
    public FormConfiguration getGlobalConfiguration( String id )
                            throws ConfigurationException {
        if ( !globalConfigurations.containsKey( id ) ) {
            FormConfigurationParser parser = new FormConfigurationParser();
            for ( FormConfigurationDescription conf : globalConfigurationDescriptions ) {
                FormConfiguration configuration = parser.parseConfiguration( conf.getConfUrl() );
                globalConfigurations.put( id, configuration );
            }
        }
        return globalConfigurations.get( id );
    }

    /**
     * @param id
     *            identifier of the configuration to reload
     * @return reloads the form configuration with the given key
     * @throws ConfigurationException
     */
    public void reloadFormConfigurations()
                            throws ConfigurationException {
        formConfigurations.clear();
    }

    public void setSelectedConfiguration( String id ) {
        this.selectedConfiguration = id;
    }

}
