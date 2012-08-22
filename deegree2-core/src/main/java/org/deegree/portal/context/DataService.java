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
package org.deegree.portal.context;

/**
 * describes the service lying behind a WMS layer. This can be a WFS, a WCS or a cascaded WMS. If the dataservice is a
 * WFS an instance of this class also provides informations about the geometry type delivered by the WFS for this
 * assigned feature type. If the service is a WCS the geometry type attribute contains the type of coverage assigned to
 * the layer (Grid, TIN, Thiessen polygon ...)
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class DataService {
    private Server server = null;

    private String featureType = null;

    private String geometryType = null;

    /**
     * Creates a new DataService object.
     *
     * @param server
     *            server description
     * @param featureType
     *            feature type provided by the server if it's a WFS
     * @param geometryType
     *            geometry type or coverage type if the server is a WFS or a WCS
     */
    public DataService( Server server, String featureType, String geometryType ) {
        setServer( server );
        setFeatureType( featureType );
        setGeometryType( geometryType );
    }

    /**
     * returns the an instance of an object describing the service/server behind a WMS layer
     *
     * @return instance of <tt>Server</tt>
     */
    public Server getServer() {
        return server;
    }

    /**
     * sets the an instance of an object describing the service/server behind a WMS layer
     *
     * @param server
     *            server description
     */
    public void setServer( Server server ) {
        this.server = server;
    }

    /**
     * @return the featuretype assigned to the WMS layer if the server behind it is a WFS
     */
    public String getFeatureType() {
        return featureType;
    }

    /**
     * sets the featuretype assigned to the WMS layer if the server behind it is a WFS
     *
     * @param featureType
     *            featuretype assigned to the WMS layer if the server behind it is a WFS
     */
    public void setFeatureType( String featureType ) {
        this.featureType = featureType;
    }

    /**
     * @return the geometry type or coverage type provided by the server behind a WMS layer if the server is a WFS or a
     *         WCS
     */
    public String getGeometryType() {
        return geometryType;
    }

    /**
     * sets the geometry type or coverage type provided by the server behind a WMS layer if the server is a WFS or a WCS
     *
     * @param geometryType
     */
    public void setGeometryType( String geometryType ) {
        this.geometryType = geometryType;
    }

}
