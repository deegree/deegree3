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

package org.deegree.ogcwebservices.wpvs.operation;

import java.util.Map;

import org.deegree.ogcwebservices.AbstractOGCWebServiceRequest;

/**
 * Conveniece class for all WPVS requests (with the exception of GetCapabilities), for the moment
 * only the GetViewRequest is supported but it might necessary to implement the GetDescription
 * operation for this purpose this class is Abstract.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
abstract class WPVSRequestBase extends AbstractOGCWebServiceRequest {

    /**
     * Default for <code>WPVSRequestBase</code>s.
     *
     * @param version
     *            the service vrsion
     * @param id
     *            the servce id
     * @param vendorSpecificParameter
     *            a <code>Map</code> containing vendor-specifc parameters
     */
    public WPVSRequestBase( String version, String id, Map<String, String> vendorSpecificParameter ) {
        super( version, id, vendorSpecificParameter );
    }

    /**
     * returns 'WPVS' as service name.
     */
    public String getServiceName() {
        return "WPVS";
    }

    /**
     * @return the requested SCALE, which is a vendorspecific propertie.
     */
    public double getScale(){
       String tmpScale =  getVendorSpecificParameter("SCALE");
       double scale = 1;
       if( tmpScale!= null ){
           //no try catch needed, is checked in the GetView.
           scale = Double.parseDouble(tmpScale);
       }
       return scale;
    }
}
