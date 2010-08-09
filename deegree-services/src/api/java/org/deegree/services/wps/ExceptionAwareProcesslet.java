//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.services.wps;

import org.deegree.services.controller.ows.OWSException;

/**
 * {@link Processlet} that provides customized exception generation.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface ExceptionAwareProcesslet extends Processlet {

    /**
     * Called by the {@link ProcessManager} if an exception occurred while parsing and/or validating an incoming request.
     * <p>
     * Implementations may supply an {@link ExceptionCustomizer} which will create an {@link OWSException} appropriate
     * for a number of exceptional events. If an implementations wants the {@link ProcessManager} to generate a 'standard'
     * exception, <code>null</code> should be returned.
     * 
     * @return an {@link ExceptionCustomizer} or <code>null</code> if the {@link Processlet} prefers standard exception
     *         handling
     */
    public ExceptionCustomizer getExceptionCustomizer();
}
