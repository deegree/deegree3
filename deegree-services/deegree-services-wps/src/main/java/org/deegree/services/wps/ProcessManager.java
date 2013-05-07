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

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.DefaultResourceManagerMetadata;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceManagerMetadata;
import org.deegree.commons.config.ResourceState;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.services.wps.provider.ProcessProviderProvider;

/**
 * Manages the available {@link WPSProcess} instances and {@link ProcessProvider}s for the {@link WPService}
 * 
 * @see WPService
 * 
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ProcessManager extends AbstractResourceManager<ProcessProvider> {

    private ProcessManagerMetadata metadata;

    /**
     * Returns all available processes.
     * 
     * @return available process, may be empty, but never <code>null</code>
     */
    public Map<CodeType, WPSProcess> getProcesses() {
        Map<CodeType, WPSProcess> processes = new HashMap<CodeType, WPSProcess>();
        for ( ResourceState<ProcessProvider> state : getStates() ) {
            if ( state.getResource() != null ) {
                Map<CodeType, ? extends WPSProcess> idToProcess = state.getResource().getProcesses();
                if ( idToProcess != null ) {
                    processes.putAll( state.getResource().getProcesses() );
                }
            }
        }
        return processes;
    }

    /**
     * Returns the process with the specified identifier.
     * 
     * @param id
     *            identifier of the process, must not be <code>null</code>
     * @return process with the specified identifier or <code>null</code> if no such process exists
     */
    public WPSProcess getProcess( CodeType id ) {
        WPSProcess process = null;
        for ( ResourceState<ProcessProvider> state : getStates() ) {
            if ( state.getResource() != null ) {
                process = state.getResource().getProcess( id );
                if ( process != null ) {
                    break;
                }
            }
        }
        return process;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    public ResourceManagerMetadata<ProcessProvider> getMetadata() {
        return metadata;
    }

    @Override
    public void initMetadata( DeegreeWorkspace workspace ) {
        this.metadata = new ProcessManagerMetadata( workspace );
    }

    static class ProcessManagerMetadata extends DefaultResourceManagerMetadata<ProcessProvider> {
        ProcessManagerMetadata( DeegreeWorkspace workspace ) {
            super( "processes", "processes", ProcessProviderProvider.class, workspace );
        }
    }

}
