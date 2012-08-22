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

import org.deegree.datatypes.CodeList;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class SupportedSRSs {
    private CodeList[] requestResponseSRSs = null;

    private CodeList[] requestSRSs = null;

    private CodeList[] responseSRSs = null;

    private CodeList[] nativeSRSs = null;

    /**
     * @param requestResponseCRSs
     * @param requestCRSs
     * @param responseCRSs
     * @param nativeCRSs
     */
    public SupportedSRSs( CodeList[] requestResponseCRSs, CodeList[] requestCRSs, CodeList[] responseCRSs,
                          CodeList[] nativeCRSs ) {
        setRequestSRSs( requestCRSs );
        setResponseSRSs( responseCRSs );
        setRequestResponseSRSs( requestResponseCRSs );
        setNativeSRSs( nativeCRSs );
    }

    /**
     * @return Returns the nativeSRSs.
     */
    public CodeList[] getNativeSRSs() {
        return nativeSRSs;
    }

    /**
     * @param nativeSRSs
     *            The nativeSRSs to set.
     */
    public void setNativeSRSs( CodeList[] nativeSRSs ) {
        if ( nativeSRSs == null ) {
            nativeSRSs = new CodeList[0];
        }
        this.nativeSRSs = nativeSRSs;
    }

    /**
     * @return Returns the requestSRSs.
     */
    public CodeList[] getRequestSRSs() {
        return requestSRSs;
    }

    /**
     * @param requestSRSs
     *            The requestSRSs to set.
     */
    public void setRequestSRSs( CodeList[] requestSRSs ) {
        if ( requestSRSs == null ) {
            requestSRSs = new CodeList[0];
        }
        this.requestSRSs = requestSRSs;
    }

    /**
     * @return Returns the requestResponseSRSs.
     */
    public CodeList[] getRequestResponseSRSs() {
        return requestResponseSRSs;
    }

    /**
     * @param requestResponseSRSs
     *            The requestResponseSRSs to set.
     */
    public void setRequestResponseSRSs( CodeList[] requestResponseSRSs ) {
        if ( requestResponseSRSs == null ) {
            requestResponseSRSs = new CodeList[0];
        }
        this.requestResponseSRSs = requestResponseSRSs;
    }

    /**
     * @return Returns the responseSRSs.
     */
    public CodeList[] getResponseSRSs() {
        return responseSRSs;
    }

    /**
     * @param responseSRSs
     *            The responseSRSs to set.
     */
    public void setResponseSRSs( CodeList[] responseSRSs ) {
        if ( responseSRSs == null ) {
            responseSRSs = new CodeList[0];
        }
        this.responseSRSs = responseSRSs;
    }

}
