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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.deegree.client.mdeditor.configuration.codelist.CodeListParser;
import org.deegree.client.mdeditor.configuration.form.FormConfigurationParser;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormConfigurationDescription;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.StringPair;
import org.deegree.gml.dictionary.Definition;
import org.deegree.gml.dictionary.Dictionary;

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

    private Map<String, FormConfiguration> formConfigurations = new HashMap<String, FormConfiguration>();

    private List<FormConfigurationDescription> globalConfigurationDescriptions;

    private Map<String, FormConfiguration> globalConfigurations = new HashMap<String, FormConfiguration>();

    private List<URL> codeListUrls;

    private List<Dictionary> codeLists = new ArrayList<Dictionary>();

    private boolean glConfsParsed = false;

    private boolean codeListParsed = false;

    private static final String CODESPACE_ROOT = "urn:org:deegree:mdeditor";

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

    public List<FormConfigurationDescription> getDescribtions( boolean global ) {
        if ( global ) {
            return globalConfigurationDescriptions;
        }
        return formConfigurationDescriptions;
    }

    /**
     * @param id
     * @param global
     * @return
     */
    public FormConfigurationDescription getDescribtion( String id, boolean global ) {
        if ( id != null ) {
            for ( FormConfigurationDescription desc : getDescribtions( global ) ) {
                if ( id.equals( desc.getId() ) ) {
                    return desc;
                }
            }
        }
        return null;
    }

    public Map<String, StringPair> getCodeListLabels( String id, Locale locale )
                            throws ConfigurationException {
        Map<String, StringPair> codelist = new HashMap<String, StringPair>();
        parseCodeLists();
        for ( Dictionary dict : codeLists ) {
            for ( Definition def : dict ) {
                if ( def instanceof Dictionary && def.getId().equals( id ) ) {
                    Dictionary codeList = (Dictionary) def;
                    for ( Definition code : codeList ) {
                        CodeType[] names = code.getNames();
                        String cd = null;
                        String label = null;
                        String description = code.getDescription() != null ? code.getDescription().getString() : null;
                        String labelDef = null;
                        for ( int i = 0; i < names.length; i++ ) {
                            if ( ( CODESPACE_ROOT + ":code" ).equals( names[i].getCodeSpace() ) ) {
                                cd = names[i].getCode();
                            } else if ( names[i].getCodeSpace() == null ) {
                                labelDef = names[i].getCode();
                            } else if ( ( CODESPACE_ROOT + ":" + locale.getLanguage() ).equals( names[i].getCodeSpace() ) ) {
                                label = names[i].getCode();
                            }
                        }
                        if ( cd == null ) {
                            break;
                        }
                        if ( label == null ) {
                            if ( labelDef != null ) {
                                label = labelDef;
                            } else {
                                label = cd;
                            }
                        }
                        codelist.put( cd, new StringPair( label, description ) );
                    }
                }

            }
        }
        return codelist;
    }

    /**
     * @param string
     * @return
     * @throws ConfigurationException
     */
    public String getCodeListValue( String id, String code )
                            throws ConfigurationException {
        parseCodeLists();
        for ( Dictionary dict : codeLists ) {
            for ( Definition def : dict ) {
                if ( def instanceof Dictionary && def.getId().equals( id ) ) {
                    Dictionary codeList = (Dictionary) def;
                    for ( Definition codeEntry : codeList ) {
                        CodeType[] names = codeEntry.getNames();
                        boolean found = false;
                        String value = null;
                        for ( int i = 0; i < names.length; i++ ) {
                            if ( ( CODESPACE_ROOT + ":code" ).equals( names[i].getCodeSpace() )
                                 && code.equals( names[i].getCode() ) ) {
                                found = true;
                            } else if ( ( CODESPACE_ROOT + ":value" ).equals( names[i].getCodeSpace() ) ) {
                                value = names[i].getCode();
                            }
                        }
                        if ( found ) {
                            if ( value != null ) {
                                return value;
                            } else {
                                return code;
                            }
                        }
                    }
                }

            }
        }
        return code;
    }

    /**
     * @return reloads the form configuration with the given key
     * @throws ConfigurationException
     */
    public void reloadFormConfigurations()
                            throws ConfigurationException {
        formConfigurations.clear();
    }

    /**
     * @return
     * @throws ConfigurationException
     */
    public FormConfiguration getConfiguration( String id )
                            throws ConfigurationException {
        FormConfiguration globalConf = getGlobalConfiguration( id );
        if ( globalConf != null ) {
            return globalConf;
        }
        return getFormConfiguration( id );
    }

    /**
     * @param confId
     * @return
     * @throws ConfigurationException
     */
    public boolean isGlobal( String id )
                            throws ConfigurationException {
        FormConfiguration globalConf = getGlobalConfiguration( id );
        if ( globalConf != null ) {
            return true;
        }
        return false;
    }

    private FormConfiguration getFormConfiguration( String id )
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

    private FormConfiguration getGlobalConfiguration( String id )
                            throws ConfigurationException {
        parseGlobalConfigurations();
        return globalConfigurations.get( id );
    }

    private void parseGlobalConfigurations()
                            throws ConfigurationException {
        if ( !glConfsParsed ) {
            for ( FormConfigurationDescription conf : globalConfigurationDescriptions ) {
                FormConfigurationParser parser = new FormConfigurationParser();
                FormConfiguration configuration = parser.parseConfiguration( conf.getConfUrl() );
                globalConfigurations.put( conf.getId(), configuration );
            }
        }
        glConfsParsed = true;
    }

    private void parseCodeLists()
                            throws ConfigurationException {
        if ( !codeListParsed ) {
            for ( URL url : codeListUrls ) {
                codeLists.add( CodeListParser.parseDictionary( url ) );
            }
            codeListParsed = true;
        }
    }

    /**
     * @return
     * @throws ConfigurationException
     * 
     */
    public Collection<FormConfiguration> getGlobalConfigurations()
                            throws ConfigurationException {
        parseGlobalConfigurations();
        return globalConfigurations.values();
    }

}
