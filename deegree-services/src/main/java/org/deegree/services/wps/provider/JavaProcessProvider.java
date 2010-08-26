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
package org.deegree.services.wps.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.wps.ProcessDefinition;
import org.deegree.services.wps.ExceptionAwareProcesslet;
import org.deegree.services.wps.ExceptionCustomizer;
import org.deegree.services.wps.GenericWPSProcess;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.WPSProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ProcessProvider} for hand-crafted Java processes with hand-crafted process descriptions.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JavaProcessProvider implements ProcessProvider {

    private static final Logger LOG = LoggerFactory.getLogger( JavaProcessProvider.class );

    private final Collection<ProcessDefinition> processDefs;

    private final Map<CodeType, WPSProcess> idToProcess = new HashMap<CodeType, WPSProcess>();

    /**
     * @param processDef
     */
    JavaProcessProvider( ProcessDefinition processDef ) {
        processDefs = Collections.singletonList( processDef );
    }

    @Override
    public void init()
                            throws ServiceInitException {
        for ( ProcessDefinition processDefinition : processDefs ) {
            CodeType processId = new CodeType( processDefinition.getIdentifier().getValue(),
                                               processDefinition.getIdentifier().getCodeSpace() );
            String className = processDefinition.getJavaClass();
            try {
                LOG.info( "Initializing process with id '" + processId + "'" );
                LOG.info( "- process class: " + className );
                Processlet processlet = (Processlet) Class.forName( className ).newInstance();
                processlet.init();

                ExceptionCustomizer customizer = null;
                if ( processlet instanceof ExceptionAwareProcesslet ) {
                    customizer = ( (ExceptionAwareProcesslet) processlet ).getExceptionCustomizer();
                }
                WPSProcess process = new GenericWPSProcess( processDefinition, processlet, customizer );
                idToProcess.put( processId, process );
            } catch ( Exception e ) {
                String msg = "Could not create process instance. Class name ('" + className
                             + "') was not found on the classpath. "
                             + "Hint: spelling in configuration file might be incorrect.";
                throw new ServiceInitException( msg, e );
            }
        }
    }

    @Override
    public void destroy() {
        for ( WPSProcess process : idToProcess.values() ) {
            process.getProcesslet().destroy();
        }
    }

    @Override
    public WPSProcess getProcess( CodeType id ) {
        return idToProcess.get( id );
    }

    @Override
    public Map<CodeType, WPSProcess> getProcesses() {
        return idToProcess;
    }
}
