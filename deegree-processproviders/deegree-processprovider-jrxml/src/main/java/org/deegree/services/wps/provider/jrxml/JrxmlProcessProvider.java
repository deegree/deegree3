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
package org.deegree.services.wps.provider.jrxml;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.Pair;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.provider.ProcessProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ProcessProvider} which offers {@link JrxmlWPSProcess}s
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class JrxmlProcessProvider implements ProcessProvider {

    private static final Logger LOG = LoggerFactory.getLogger( JrxmlProcessProviderProvider.class );

    private final Map<CodeType, WPSProcess> idToProcess = new HashMap<CodeType, WPSProcess>();

    public JrxmlProcessProvider( List<Pair<String, URL>> idToURL ) {
        for ( Pair<String, URL> p : idToURL ) {
            LOG.debug( "add process with id " + p.first + " from " + p.second );
            idToProcess.put( new CodeType( p.first ), new JrxmlWPSProcess( p.first, p.second ) );
        }
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        LOG.info( "init jrxml process provider" );
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
    public Map<CodeType, ? extends WPSProcess> getProcesses() {
        return idToProcess;
    }

    @Override
    public WPSProcess getProcess( CodeType id ) {
        return idToProcess.get( id );
    }

}
