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

import org.deegree.services.controller.ows.OWSException;

/**
 * Indicates that the execution of a {@link Processlet} failed.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ProcessletException extends Exception {

    private static final long serialVersionUID = 8603163109165490956L;

    private final OWSException exception;

    /**
     * Creates a new {@link ProcessletException} with the given detail message.
     * 
     * @param msg
     *            detail message (visible to the client)
     */
    public ProcessletException( String msg ) {
        super( msg );
        this.exception = null;
    }

    /**
     * @param owsException
     *            which should be thrown instead of a 'new' {@link OWSException}.
     */
    public ProcessletException( OWSException owsException ) {
        this.exception = owsException;
    }

    /**
     * @return an OWSException the {@link Processlet} created while executing.
     */
    public OWSException getOWSException() {
        return this.exception;
    }

    /**
     * @return true if the {@link Processlet} supplied an {@link OWSException} while executing.
     */
    public boolean hasOWSException() {
        return this.exception != null;
    }
}
