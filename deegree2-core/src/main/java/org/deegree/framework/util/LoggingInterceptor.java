//$HeadURL$
//$Id$
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

package org.deegree.framework.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Logging interceptor to log the entering and exiting of a method call.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </A>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @see <a href="http://www.dofactory.com/patterns/PatternChain.aspx">Chain of Responsibility Design
 *      Pattern </a>
 *
 * @since 2.0
 */
public class LoggingInterceptor extends Interceptor {

    /**
     * @param nextInterceptor pointing to the next intersection
     */
    public LoggingInterceptor( Interceptor nextInterceptor ) {
        this.nextInterceptor = nextInterceptor;
    }

    @Override
    protected Object handleInvocation( Method method, Object[] params )
                            throws IllegalAccessException, InvocationTargetException {
        LOG.logDebug( ">Invoke " + method.getName() + " on " + getTarget() );
        Object result = nextInterceptor.handleInvocation( method, params );
        LOG.logDebug( "<Finish " + method.getName() + " on " + getTarget() );
        return result;
    }

}
