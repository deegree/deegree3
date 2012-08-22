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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Upon receiving an invalid operation request, each OWS shall respond to the client using an
 * Exception Report message to describe to the client application and/or its human user the
 * reason(s) that the request is invalid. Whenever a server detects an exception condition while
 * responding to a valid operation request, and cannot produce a normal response to that operation,
 * the server shall also respond to the client using an Exception Report.
 * </p>
 * <p>
 * Each Exception Report shall contain one or more Exception elements, with each such element
 * signalling detection of an independent error. Each Exception element shall contain the parameters
 * ExceptionText, exceptionCode and locator.
 * </p>
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class ExceptionReport {

    private List<OGCWebServiceException> exceptions = new ArrayList<OGCWebServiceException>();

    private String version = "1.0.0";

    private String language = "en";

    /**
     * @param exceptions
     */
    public ExceptionReport( OGCWebServiceException[] exceptions ) {
        setExceptions( exceptions );
    }

    /**
     * @param exceptions
     * @param version
     */
    public ExceptionReport( OGCWebServiceException[] exceptions, String version ) {
        setExceptions( exceptions );
        setVersion( version );
    }

    /**
     * @param exceptions
     * @param version
     * @param language
     */
    public ExceptionReport( OGCWebServiceException[] exceptions, String version, String language ) {
        setExceptions( exceptions );
        setVersion( version );
        setLanguage( language );
    }

    /**
     * @return Returns the exceptions.
     *
     */
    public OGCWebServiceException[] getExceptions() {
        OGCWebServiceException[] owse = new OGCWebServiceException[exceptions.size()];
        return exceptions.toArray( owse );
    }

    /**
     * @param exceptions
     *            The exceptions to set.
     */
    public void setExceptions( OGCWebServiceException[] exceptions ) {
        this.exceptions.clear();
        for ( int i = 0; i < exceptions.length; i++ ) {
            this.exceptions.add( exceptions[i] );
        }
    }

    /**
     * @return Returns the language.
     *
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language
     *            The language to set.
     *
     */
    public void setLanguage( String language ) {
        this.language = language;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            The version to set.
     */
    public void setVersion( String version ) {
        this.version = version;
    }

}
