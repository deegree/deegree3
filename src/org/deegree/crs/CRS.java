//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.crs;

import org.deegree.crs.coordinatesystems.CoordinateSystem;

/**
 * The <code>CRSDeliverable</code> class wraps the CoordinateSystem created and the name with which it was requested.
 * This is necessary since the internal naming of CoordinateSystem's is simplified (and thus is different). 
 * So adding the original requested name assures the same identity for the return object as was the input request.    
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class CRS {
    
    /**
     * The CRS that is wrapped by the CRSDeliverable
     */
    private CoordinateSystem crs;
    
    /**
     * The String that may be used to denote the CRS 
     */
    private String requestedID;
     
    /**
     * The CRSCodeType that may be used to denote the CRS
     */
    private CRSCodeType requestedCode;
            
    public CRS( CoordinateSystem crs, String requestedID ) {
        this.crs = crs;
        this.setRequestedID( requestedID );
    }
    
    public CRS( CoordinateSystem crs, CRSCodeType requestedCode ) {
        this.crs = crs;
        this.setRequestedCode( requestedCode );
    }
    
    /**
     * Returns the Coordinate System encapsulated in the deliverable object
     * @return
     *      the Coordinate System
     */
    public CoordinateSystem getWrappedCRS() {
        return crs;
    }

    public void setRequestedID( String requestedID ) {
        this.requestedID = requestedID;
    }

    public String getRequestedID() {
        return requestedID;
    }

    public void setRequestedCode( CRSCodeType requestedCode ) {
        this.requestedCode = requestedCode;
    }

    public CRSCodeType getRequestedCode() {
        return requestedCode;
    }

}
