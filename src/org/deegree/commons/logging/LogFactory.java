//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

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
package org.deegree.commons.logging;

import org.apache.commons.logging.LogConfigurationException;

/**
 * Factory for creating deegree {@link Log} instances. deegree Log instances wraps
 * {@link org.apache.commons.logging.Log} and adds some methods to log direct into files.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class LogFactory {
    /**
     * Returns a logger for class.
     * 
     * @param name
     *            the name od the class in which the log will be used
     * @return a Log implementation with deegree enhancements
     * 
     * @exception LogConfigurationException
     *                if a suitable <code>Log</code> instance cannot be returned
     */
    public static Log getLog( String name ) {
        org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog( name );
        return new Log( log );
    }

    /**
     * Returns a logger for class.
     * 
     * @param cls
     *            the class in which the log will be used
     * @return a Log implementation with deegree enhancements
     * 
     * @exception LogConfigurationException
     *                if a suitable <code>Log</code> instance cannot be returned
     */
    public static Log getLog( Class cls ) {
        org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog( cls );
        return new Log( log );
    }

}
