//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/svn_classfile_header_template.xml $
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

package org.deegree.services.controller.exception;

import org.deegree.services.controller.AbstractOGCServiceController;

/**
 * Indicates that the initialization of a {@link AbstractOGCServiceController} failed (usually due to a configuration error).
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
 */
public class ControllerInitException extends Exception {

    private static final long serialVersionUID = 1349092248017136937L;

    /**
     * Creates a new {@link ControllerInitException} instance with the given detail message.
     *
     * @param msg
     *            describes the reason of the exception
     */
    public ControllerInitException( String msg ) {
        super( msg );
    }

    /**
     * Creates a new {@link ControllerInitException} instance with the given detail message and causing exception.
     *
     * @param msg
     *            describes the reason of the exception
     * @param cause
     *            causing exception
     */
    public ControllerInitException( String msg, Throwable cause ) {
        super( msg, cause );
    }
}
