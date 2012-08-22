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
package org.deegree.ogcwebservices.wms.operation;

import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;

/**
 *
 * @author Katharina Lupp <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */
public class GetFeatureInfoResult extends DefaultOGCWebServiceResponse {

    private String featureInfo;

    /**
     * constructor initializing the class with the &lt;WMSFeatureInfoResponse&gt;
     */
    public GetFeatureInfoResult( OGCWebServiceRequest request, OGCWebServiceException exception) {
         super( request, exception );
         setFeatureInfo( featureInfo );
     }

    /**
     * constructor initializing the class with the &lt;WMSFeatureInfoResponse&gt;
     */
    public GetFeatureInfoResult( OGCWebServiceRequest request, String featureInfo ) {
        super( request );
        setFeatureInfo( featureInfo );
    }

    /**
     * @return the feature info the corresponds to an feature info request. The
     * format of the result is determined by the <tt>INFO_FORMAT</tt> parameter
     * of the request. If an excption raised during the processing of the request
     * or the request has been invald <tt>null</tt> will be returned.
     */
    public String getFeatureInfo() {
        return featureInfo;
    }

    /**
     * sets the feature info the corresponds to an feature info request.
     * @param featureInfo
     */
    public void setFeatureInfo( String featureInfo ) {
        this.featureInfo = featureInfo;
    }
}
