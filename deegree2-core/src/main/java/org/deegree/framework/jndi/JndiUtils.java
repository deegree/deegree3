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
package org.deegree.framework.jndi;

// J2EE 1.3

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * Utility class for retrieving home interfaces and environment values.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe</A>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public final class JndiUtils {

    /**
     * Default constructor is hidden to protect this class from unwanted instantiation.
     */
    private JndiUtils() {
        // nothing to do
    }

    /**
     * Lookup using <b>no</b> caching functionality.
     *
     * @param name
     *            the environment name
     * @param classForNarrow
     *            the class to narrow
     * @return if an entry exists, an instance of the given class with a value retrieved from the JNDI tree otherwise
     *         <code>null</code>.
     * @throws NamingException
     */
    public static Object lookup( String name, Class<?> classForNarrow )
                            throws NamingException {
        return PortableRemoteObject.narrow( JndiUtils.getInitialContext().lookup( name ), classForNarrow );
    }

    /**
     * Lookup at Enterprise Java Beans environment value. For home interfaces use EJBRemoteFactory instead.
     *
     * @param name
     *            the environment name
     * @param classForNarrow
     *            the class to narrow
     * @return if an entry exists, an instance of the given class with a value retrieved from the JNDI tree otherwise
     *         <code>null</code>.
     * @throws NamingException
     *             if the given lookup name is not in the JNDI tree
     */
    public static Object lookupEnv( String name, Class<?> classForNarrow )
                            throws NamingException {
        return lookup( "java:/comp/env/" + name, classForNarrow );
    }

    /**
     * Returns a list of naming entries for the given root node.
     *
     * @param rootNode
     * @return list of naming entries
     * @throws NamingException
     *             when the given root node doesn't exists
     */
    public static NamingEnumeration<?> getNamingList( String rootNode )
                            throws NamingException {
        return JndiUtils.getInitialContext().listBindings( rootNode );
    }

    /**
     * Returns the initial context for this application.
     *
     * @return the inital context for this appication. This depends on the system environment.
     *
     * @throws NamingException
     *             if the context is not available
     */
    private static Context getInitialContext()
                            throws NamingException {
        return ( new InitialContext() );
    }
}
