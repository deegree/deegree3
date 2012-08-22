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
package org.deegree.portal.standard.security.control;

import java.util.Map;

import org.deegree.security.drm.model.SecuredObject;

/**
 * Container that encapsulates information about a certain <code>Right</code> on a certain <code>SecuredObject</code>.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SecuredObjectRight {

    private boolean accessible;

    private SecuredObject securedObject;

    private Map<?, ?> constraints;

    /**
     * @param accessible
     * @param securedObject
     * @param constraints
     */
    public SecuredObjectRight( boolean accessible, SecuredObject securedObject, Map<?, ?> constraints ) {
        this.accessible = accessible;
        this.securedObject = securedObject;
        this.constraints = constraints;
    }

    /**
     * @return boolean
     */
    public boolean isAccessible() {
        return accessible;
    }

    /**
     * @return SecuredObject
     */
    public SecuredObject getSecuredObject() {
        return securedObject;
    }

    /**
     * @return Map
     */
    public Map<?, ?> getConstraints() {
        return constraints;
    }

    @Override
    public String toString() {
        return "SecuredObjectRight: accessible=" + accessible + ", securedObject=" + securedObject + " , constraints="
               + constraints;
    }
}
