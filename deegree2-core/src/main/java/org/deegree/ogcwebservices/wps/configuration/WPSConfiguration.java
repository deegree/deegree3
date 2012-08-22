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
/**
 * asdfasd
 */
package org.deegree.ogcwebservices.wps.configuration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ConvenienceFileFilter;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wps.ProcessBrief;
import org.deegree.ogcwebservices.wps.capabilities.ProcessOfferings;
import org.deegree.ogcwebservices.wps.capabilities.WPSCapabilities;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescription;
import org.deegree.ogcwebservices.wps.describeprocess.ProcessDescriptionDocument;
import org.deegree.ogcwebservices.wps.execute.Process;
import org.xml.sax.SAXException;

/**
 * WPSConfiguration.java
 *
 * Created on 08.03.2006. 17:58:55h
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WPSConfiguration extends WPSCapabilities {

    private static final ILogger LOG = LoggerFactory.getLogger( WPSConfiguration.class );

    private WPSDeegreeParams deegreeParams = null;

    private Map<String, Process> registeredProcesses;

    private static final String SPEC_PROC_DIR = "Specified process directory '";

    /**
     * Constructor initializes WPSConfiguration from passed parameters
     *
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param contents
     * @throws InvalidConfigurationException
     */
    protected WPSConfiguration( String version, String updateSequence, ServiceIdentification serviceIdentification,
                                ServiceProvider serviceProvider, OperationsMetadata operationsMetadata,
                                Contents contents, WPSDeegreeParams wpsDeegreeParams )
                            throws InvalidConfigurationException {
        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata, contents );
        this.deegreeParams = wpsDeegreeParams;
        try {
            loadProcessConfigs();
            setConfiguredProcessOfferings();
        } catch ( InvalidConfigurationException e ) {
            LOG.logError( e.getMessage(), e );
            throw e;
        }
    }

    private void setConfiguredProcessOfferings() {

        List<ProcessBrief> processBriefList = new ArrayList<ProcessBrief>();

        Iterator<Process> registeredProcessesValuesIterator = registeredProcesses.values().iterator();

        while ( registeredProcessesValuesIterator.hasNext() ) {
            Process process = registeredProcessesValuesIterator.next();

            ProcessDescription processDescription = process.getProcessDescription();

            processBriefList.add( new ProcessBrief( processDescription.getIdentifier(), processDescription.getTitle(),
                                                    processDescription.getAbstract(),
                                                    processDescription.getProcessVersion(),
                                                    processDescription.getMetadata() ) );
        }

        ProcessOfferings processOfferings = new ProcessOfferings( processBriefList );

        super.setProcessOfferings( processOfferings );
    }

    /**
     * @return Returns the registeredProcesses.
     */
    public Map<String, Process> getRegisteredProcesses() {
        return registeredProcesses;
    }

    /**
     * loads and initializes processes configured in the process directory.
     *
     * @throws InvalidConfigurationException
     */
    private void loadProcessConfigs()
                            throws InvalidConfigurationException {
        this.registeredProcesses = scanForRegisteredProcesses();

    }

    /**
     * Scans for process configuration documents located in process directory of current WPS configuration.
     *
     * @return Map<String, Process>
     * @throws InvalidConfigurationException
     */
    private Map<String, Process> scanForRegisteredProcesses()
                            throws InvalidConfigurationException {
        List<String> fileNameList = new ArrayList<String>();
        String[] processDirectories = getDeegreeParams().getProcessDirectories();

        for ( int i = 0; i < processDirectories.length; i++ ) {
            File file = new File( processDirectories[i] );
            LOG.logInfo( "Directory '" + file.getAbsolutePath() + "' will be scanned for process configuration files." );
            String[] list = file.list( new ConvenienceFileFilter( false, "XML" ) );
            if ( list != null ) {
                if ( list.length == 0 ) {
                    String msg = SPEC_PROC_DIR + processDirectories[i] + "' does not contain any '.xml' files.";
                    LOG.logError( msg );
                    throw new InvalidConfigurationException( msg );
                }
                for ( int j = 0; j < list.length; j++ ) {
                    fileNameList.add( processDirectories[i] + '/' + list[j] );
                }
            } else {
                String msg = SPEC_PROC_DIR + processDirectories[i] + "' does not denote a directory.";
                LOG.logError( msg );
                throw new InvalidConfigurationException( msg );
            }
        }
        String[] fileNames = fileNameList.toArray( new String[fileNameList.size()] );
        return extractProcessDescriptions( fileNames );
    }

    /**
     * Extracts a <code>ProcessDescription</code> for each provided files in <code>String[]</code> fileNames.
     *
     * @param fileNames
     * @return
     * @throws InvalidConfigurationException
     */
    private Map<String, Process> extractProcessDescriptions( String[] fileNames )
                            throws InvalidConfigurationException {
        int size = fileNames.length;
        Map<String, Process> processMap = new HashMap<String, Process>( size );
        for ( int i = 0; i < size; i++ ) {
            LOG.logInfo( "Parsing process configuration file: '" + fileNames[i] + "'." );
            Process process = null;
            try {
                URL fileURL = new File( fileNames[i] ).toURL();
                ProcessDescriptionDocument processDescriptionDocument = new ProcessDescriptionDocument();
                processDescriptionDocument.load( fileURL );
                ProcessDescription processDescription = processDescriptionDocument.parseProcessDescription();

                String className = processDescription.getResponsibleClass();
                try {
                    Class<?> processClass = Class.forName( className );
                    Constructor<?> con = processClass.getConstructor( ProcessDescription.class );
                    process = (Process) con.newInstance( processDescription );
                } catch ( ClassNotFoundException cnfEx ) {
                    String msg = "Responsible class for process execution: '" + className + "' not found.";
                    // LOG.logError( msg, cnfEx );
                    throw new XMLParsingException( msg, cnfEx );
                } catch ( NoSuchMethodException nsmEx ) {
                    String msg = "Responsible class for process execution: '" + className
                                 + "' can not be instantiated.";
                    // LOG.logError( msg, nsmEx );
                    throw new XMLParsingException( msg, nsmEx );
                } catch ( InstantiationException iEx ) {
                    String msg = "Responsible class for process execution: '" + className
                                 + "' can not be instantiated.";
                    // LOG.logError( msg, iEx );
                    throw new XMLParsingException( msg, iEx );
                } catch ( InvocationTargetException itEx ) {
                    String msg = "Responsible class for process execution: '" + className
                                 + "' can not be instantiated.";
                    // LOG.logError( msg, itEx );
                    throw new XMLParsingException( msg, itEx );
                } catch ( IllegalAccessException iaEx ) {
                    String msg = "Responsible class for process execution: '" + className
                                 + "' can not be instantiated.";
                    // LOG.logError( msg, iaEx );
                    throw new XMLParsingException( msg, iaEx );
                }

                String processKey = processDescription.getIdentifier().getCode().toUpperCase();
                if ( !processMap.containsKey( processKey ) ) {
                    processMap.put( processKey, process );
                    LOG.logDebug( "Process '" + processKey + "' registered to server." );
                } else {
                    String msg = "Multiple definition of process '" + processKey + "' not allowed! Process '"
                                 + processKey + "' is already defined.";
                    LOG.logError( msg );
                    throw new InvalidConfigurationException( msg );
                }

            } catch ( IOException ioe ) {
                String msg = "Error loading '" + fileNames[i] + "': " + ioe.getMessage();
                LOG.logError( msg );
                throw new InvalidConfigurationException( msg, ioe );
            } catch ( XMLParsingException e ) {
                String msg = "Error parsing '" + fileNames[i] + "': " + e.getMessage();
                LOG.logError( msg, e );
                throw new InvalidConfigurationException( msg, e );
            } catch ( SAXException e ) {
                String msg = "Error parsing '" + fileNames[i] + "': " + e.getMessage();
                LOG.logError( msg, e );
                throw new InvalidConfigurationException( msg, e );
            }

        }

        return processMap;
    }

    /**
     * @return Returns the deegreeParams.
     */
    public WPSDeegreeParams getDeegreeParams() {
        return deegreeParams;
    }

    /**
     * @param deegreeParams
     *            The deegreeParams to set.
     */
    public void setDeegreeParams( WPSDeegreeParams deegreeParams ) {
        this.deegreeParams = deegreeParams;
    }

    /**
     * Returns a {@link WPSConfiguration} created from the configuration document that is loaded from the given URL.
     *
     * @param url
     * @return {@link WPSConfiguration} created from the specified configuration document
     * @throws IOException
     * @throws SAXException
     * @throws InvalidConfigurationException
     */
    public static WPSConfiguration createConfiguration( URL url )
                            throws IOException, SAXException, InvalidConfigurationException {
        WPSConfigurationDocument confDoc = new WPSConfigurationDocument();
        confDoc.load( url );
        WPSConfiguration configuration = confDoc.getConfiguration();

        return configuration;
    }

}
