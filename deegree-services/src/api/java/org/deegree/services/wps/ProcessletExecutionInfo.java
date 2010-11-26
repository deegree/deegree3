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

/**
 * Allows the {@link Processlet} to provide execution information, i.e. percentage completed and start/success messages
 * that it wants to make known to clients.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public interface ProcessletExecutionInfo {

    /**
     * Allows the {@link Processlet} to indicate the percentage of the process that has been completed, where 0 means
     * the process has just started, and 99 means the process is almost complete. This value is expected to be accurate
     * to within ten percent.
     *
     * @param percentCompleted
     *            the percentage value to be set, a number between 0 and 99
     */
    public void setPercentCompleted( int percentCompleted );

    /**
     * Allows the {@link Processlet} to provide a start message for the client.
     *
     * @param message
     */
    public void setStartedMessage( String message );

    /**
     * Allows the {@link Processlet} to provide a finish message for the client.
     *
     * @param message
     */
    public void setSucceededMessage( String message );
}
