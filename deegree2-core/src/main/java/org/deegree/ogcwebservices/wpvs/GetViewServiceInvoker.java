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

package org.deegree.ogcwebservices.wpvs;

import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wpvs.utils.ResolutionStripe;

/**
 * Abstract super class for all invokers. Concrete implementations of this class call specific
 * services in order to request data from them.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public abstract class GetViewServiceInvoker {

    /**
     * The stripe to valid vor the invoked service.
     */
    protected ResolutionStripe resolutionStripe;

    /**
     * @param owner
     *            the ResolutionStripe (part of the users viewing area) which this service will be
     *            called/invoked for.
     */
    public GetViewServiceInvoker( ResolutionStripe owner ) {
        this.resolutionStripe = owner;
    }

    /**
     * The implementation of this method should invoke a webservice specified by the given
     * AbstractDataSource. The Services response to the Request shall be saved in the
     * ResolutionStripe.
     *
     * @param dataSource
     *            a class containing the necessary data to invoke a service.
     */
    public abstract void invokeService( AbstractDataSource dataSource );

}
