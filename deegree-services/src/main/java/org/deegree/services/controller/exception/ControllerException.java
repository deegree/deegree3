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

package org.deegree.services.controller.exception;

/**
 * The <code>ControllerException</code> class wraps an exception code.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class ControllerException extends Exception {

    private static final long serialVersionUID = -4068113276306511364L;

    /**
     * exception code for all not known exceptions
     */
    public static final String NO_APPLICABLE_CODE = "NoApplicableCode";

    private final String exceptionCode;

    /**
     * @param message
     *            human readable exception
     * @param exceptionCode
     *            defining a machine readable code.
     */
    public ControllerException( String message, String exceptionCode ) {
        super( message );
        this.exceptionCode = exceptionCode;
    }

    /**
     * @param cause
     *            of the exception
     * @param exceptionCode
     *            defining a machine readable code.
     */
    public ControllerException( Throwable cause, String exceptionCode ) {
        super( cause );
        this.exceptionCode = exceptionCode;
    }

    /**
     * @param message
     *            human readable exception
     * @param cause
     *            of the exception
     * @param exceptionCode
     *            defining a machine readable code.
     */
    public ControllerException( String message, Throwable cause, String exceptionCode ) {
        super( message, cause );
        this.exceptionCode = exceptionCode;
    }

    /**
     * @return the exceptionCode
     */
    public final String getExceptionCode() {
        return exceptionCode;
    }

}
