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

import java.util.Map;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.wps.WPSProcess;
import org.deegree.services.wps.ProcessManager;

/**
 * Implementations are responsible for making {@link WPSProcess} instances available to the {@link ProcessManager}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface ProcessProvider {

    /**
     * Called by the container to indicate that this {@link ProcessProvider} instance is being placed into service.
     * 
     * @throws ServiceInitException
     */
    public void init()
                            throws ServiceInitException;

    /**
     * Called by the container to indicate that this {@link ProcessProvider} instance is being taken out of service.
     */
    public void destroy();

    /**
     * Returns all currently available processes.
     * 
     * @return all currently available processes, may be <code>null</code> or empty
     */
    public Map<CodeType, WPSProcess> getProcesses();

    /**
     * Returns the process with the specified identifier.
     * 
     * @param id
     *            identifier of the requested process, never <code>null</code>
     * @return the process with the specified identifier, or <code>null</code> if no such process exists (anymore)
     */
    public WPSProcess getProcess( CodeType id );
}