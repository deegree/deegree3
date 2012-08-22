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
package org.deegree.framework.xml.schema;

/**
 * Thrown when a reference to an XML type is encountered that is unknown in the context (i.e. in the
 * XML Schema document).
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @since 2.0
 */
public class UndefinedXSDTypeException extends UnresolvableReferenceException {

    private static final long serialVersionUID = -9138320617620027534L;

    /**
     * Constructs an instance of <code>UndefinedTypeException</code> with the specified detail
     * message.
     *
     * @param msg
     *            the detail message
     */
    public UndefinedXSDTypeException( String msg ) {
        super( msg );
    }

    /**
     * Constructs an instance of <code>UndefinedTypeException</code> with the specified cause.
     *
     * @param cause
     *            the Throwable that caused this SchemaException
     *
     */
    public UndefinedXSDTypeException( Throwable cause ) {
        super( cause );
    }

    /**
     * Constructs an instance of <code>UndefinedTypeException</code> with the specified detail
     * message and cause.
     *
     * @param msg
     *            the detail message
     * @param cause
     *            the Throwable that caused this UndefinedTypeException
     *
     */
    public UndefinedXSDTypeException( String msg, Throwable cause ) {
        super( msg, cause );
    }
}
