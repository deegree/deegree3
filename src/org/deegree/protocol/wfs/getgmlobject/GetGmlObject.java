//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/ogcwebservices/wfs/operation/DescribeFeatureType.java $
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
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstraße 19
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
package org.deegree.protocol.wfs.getgmlobject;

/**
 * The <code>GetGmlObject</code> operation allows retrieval of features and elements by ID from a WFS. 
 * A GetGmlObject request is processed by a WFS and an XML document fragment containing the result set is 
 * returned to the client. The GetGmlObject operation is optional and if supported it must be advertised 
 * on the getCapabilities operation.
 * <b>Supported by WFS specification version 1.1.0</b>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetGmlObject {
  
    private GmlObjectId id;
    
    private String outputFormat;
 
    private String traverseXlinkDepth;
        
    // Integer type used since it might be null (traverseXlinkExpiry attribute is optional)
    private Integer traverseXlinkExpiry;
    
    /**
     * @param id a GMLObjectId
     * @param outputFormat  a String format of the result set
     * @param traverseXlinkDepth    maximum depth of Xlink traversing, an integer as String or '*'  
     * @param traverseXlinkExpiry   time in minutes for which the WFS waits for a response  
     */
    public GetGmlObject( GmlObjectId id, String outputFormat, String traverseXlinkDepth, 
                         int traverseXlinkExpiry ) {
        this.id = id;
        this.outputFormat = outputFormat;
        this.traverseXlinkDepth = traverseXlinkDepth;
        this.traverseXlinkExpiry = traverseXlinkExpiry;
    }

    public GmlObjectId getId() {
        return id;
    }

    /**
     * Returns format of the result set 
     * @return
     *          the format of the result set; default value is 'text/xml; subtype=gml/3.1.1'; might be null 
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Returns the maximum depth at which the Xlinks references will be traversed
     * @return
     *          the maximum depth at which the Xlinks references will be traversed; '*' for unbounded
     */
    public String getTraverseXlinkDepth() {
        return traverseXlinkDepth;
    }

    // in minutes; indicates 
    /**
     * Returns how long a WFS should wait for a response
     * @return
     *          the time in minutes a WFS should wait for a response;  might be null
     */
    public int getTraverseXlinkExpiry() {
        return traverseXlinkExpiry;
    }

}
