// $HeadURL$
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
package org.deegree.ogcwebservices;

import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcbase.OGCException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class OGCWebServiceException extends OGCException {

    /**
     *
     */
    private static final long serialVersionUID = -2081577799241168634L;

    private String locator = "unknown";

    /**
     * creates an OGCWebServiceException from a DOM object as defined in the OGC common
     * implementation specification
     *
     * @param doc
     * @return an {@link OGCWebServiceException} with the message, code and locator set to the xml
     *         inside the document.
     */
    public static OGCWebServiceException create( Document doc ) {
        Element root = doc.getDocumentElement();
        return create( root );
    }

    /**
     * creates an OGCWebServiceException from a DOM Element object as defined in the OGC common
     * implementation specification
     *
     * @param root
     * @return an {@link OGCWebServiceException} with the message, code and locator set to the xml
     *         inside the document.
     *
     */
    public static OGCWebServiceException create( Element root ) {

        String code = XMLTools.getAttrValue( root, null, "code", null );
        String lo = XMLTools.getAttrValue( root, null, "locator", null );

        String me = XMLTools.getStringValue( root );

        ExceptionCode ec = new ExceptionCode( code );
        return new OGCWebServiceException( me, lo, ec );
    }

    /**
     * @param message
     */
    public OGCWebServiceException( String message ) {
        super( message );
    }

    /**
     * @param message
     * @param code
     *            of the exception, often defined in the web service spec.
     */
    public OGCWebServiceException( String message, ExceptionCode code ) {
        super( message, code );
    }

    /**
     * @param locator
     * @param message
     */
    public OGCWebServiceException( String locator, String message ) {
        super( message );
        this.locator = locator;
    }

    /**
     * @param locator
     * @param message
     * @param code
     */
    public OGCWebServiceException( String locator, String message, ExceptionCode code ) {
        super( message, code );
        this.locator = locator;
    }

    /**
     * @return the class/service that has caused the exception
     *
     */
    public String getLocator() {
        return locator;
    }

    /**
     * @param locator
     *            sets the class/service that has caused the exception
     *
     */
    public void setLocator( String locator ) {
        this.locator = locator;
    }
}
