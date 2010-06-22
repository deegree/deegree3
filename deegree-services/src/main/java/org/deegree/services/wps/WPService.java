//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.services.wps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.jaxb.wps.ProcessletInputDefinition;
import org.deegree.services.jaxb.wps.ProcessletOutputDefinition;
import org.deegree.services.jaxb.wps.ProcessDefinition.InputParameters;
import org.deegree.services.jaxb.wps.ProcessDefinition.OutputParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WPService {

    private static final Logger LOG = LoggerFactory.getLogger( WPService.class );

    private Map<CodeType, Processlet> idToProcess = new HashMap<CodeType, Processlet>();

    private Map<CodeType, ProcessDefinition> idToProcessDefinition = new HashMap<CodeType, ProcessDefinition>();

    private Map<CodeType, ExceptionCustomizer> idToExceptionCustomizer = new HashMap<CodeType, ExceptionCustomizer>();

    /**
     * Creates a new {@link WPService} instance with the given configuration.
     * 
     * @param processDefinitions
     * @throws ServiceInitException
     *             if a process class in the configuration could not be initialized
     */
    public WPService( Collection<ProcessDefinition> processDefinitions ) throws ServiceInitException {
        for ( ProcessDefinition processDefinition : processDefinitions ) {
            CodeType processId = new CodeType( processDefinition.getIdentifier().getValue(),
                                               processDefinition.getIdentifier().getCodeSpace() );
            String className = processDefinition.getJavaClass();
            try {
                LOG.info( "Initializing process with id '" + processId + "'" );
                LOG.info( "- process class: " + className );
                Processlet processlet = (Processlet) Class.forName( className ).newInstance();
                processlet.init();
                idToProcess.put( processId, processlet );
                idToProcessDefinition.put( processId, processDefinition );
                if ( processlet instanceof ExceptionAwareProcesslet ) {
                    ExceptionCustomizer customizer = ( (ExceptionAwareProcesslet) processlet ).getExceptionCustomizer();
                    if ( customizer != null ) {
                        idToExceptionCustomizer.put( processId, customizer );
                    }
                }
            } catch ( Exception e ) {
                String msg = "Could not create a process instance. Class name ('" + className
                             + "') was not found on the classpath. "
                             + "Hint: spelling in configuration file might be incorrect.";
                throw new ServiceInitException( msg, e );
            }

            InputParameters inputParams = processDefinition.getInputParameters();
            if ( inputParams != null ) {
                for ( JAXBElement<? extends ProcessletInputDefinition> el : inputParams.getProcessInput() ) {
                    LOG.info( "- input parameter: " + el.getValue().getIdentifier().getValue() );
                }
            }

            if ( processDefinition.getOutputParameters() != null ) {
                OutputParameters outputParams = processDefinition.getOutputParameters();
                for ( JAXBElement<? extends ProcessletOutputDefinition> el : outputParams.getProcessOutput() ) {
                    LOG.info( "- output parameter: " + el.getValue().getIdentifier().getValue() );
                }
            }
        }
    }

    /**
     * Returns all registered processes.
     * 
     * @return all registered processes
     */
    public Processlet[] getAllProcesses() {
        return idToProcess.values().toArray( new Processlet[idToProcess.size()] );
    }

    /**
     * Returns the process with the given identifier.
     * 
     * @param identifier
     * @return the process with the given identifier
     */
    public Processlet getProcess( CodeType identifier ) {
        return idToProcess.get( identifier );
    }

    public Map<CodeType, ProcessDefinition> getProcessDefinitions() {
        return idToProcessDefinition;
    }

    public ProcessDefinition[] getAllProcessDefinitions() {
        return idToProcessDefinition.values().toArray( new ProcessDefinition[idToProcessDefinition.size()] );
    }

    public ProcessDefinition getProcessDefinition( CodeType identifier ) {
        return idToProcessDefinition.get( identifier );
    }

    /**
     *
     */
    public void destroy() {
        for ( Processlet processlet : idToProcess.values() ) {
            LOG.debug( "Taking processlet '" + processlet.getClass().getName() + "' out of service." );
            processlet.destroy();
        }
    }

    /**
     * @return the map containing all process and their respective id's. This is the live map, altering the map will
     *         result in an inconsistent state of the WPService.
     */
    public Map<CodeType, Processlet> getProcesses() {
        return idToProcess;
    }

    /**
     * @return the map containing all processlet (id's) which supplied an {@link ExceptionCustomizer} . This is the live
     *         map, altering the map will result in an inconsistent state of the WPService. The result maybe empty, but
     *         never <code>null</code>
     */
    public Map<CodeType, ExceptionCustomizer> getCustomExceptionHandlers() {
        return idToExceptionCustomizer;
    }
}
