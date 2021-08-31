/*
 * Copyright lat/lon GmbH 2011
 * All rights reserved.
 */
package de.latlon.fme.processprovider;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.provider.ProcessProvider;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

import java.util.Map;

/**
 * Example {@link ProcessProvider} implementation for process provider tutorial.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: goltz $
 * @version $Revision: 91 $, $Date: 2016-12-14 13:00:39 +0100 (Mi, 14. Dez 2016) $
 */
public class FMEProcessProvider implements ProcessProvider {

    private final Map<CodeType, FMEProcess> idToProcess;

    private final FMEProcessMetadata metadata;

    public FMEProcessProvider( Map<CodeType, FMEProcess> processes, FMEProcessMetadata metadata ) {
        idToProcess = processes;
        this.metadata = metadata;
    }

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
    }

    @Override
    public void init() {
        for ( WPSProcess process : idToProcess.values() ) {
            process.getProcesslet().init();
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
    public Map<CodeType, FMEProcess> getProcesses() {
        return idToProcess;
    }
}
