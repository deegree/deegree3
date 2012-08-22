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
package org.deegree.ogcwebservices.wms.configuration;

import java.net.URL;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;

/**
 * name of the data source where the WMS can find the data of a layer. the filterServiceClassName element identifies the
 * filter servive that's responsible for accessing the data.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$, $Date$
 */
public abstract class AbstractDataSource {

    /**
     * A constant indicating a local wcs.
     */
    public static final int LOCALWCS = 0;

    /**
     * A constant indicating a local wfs.
     */
    public static final int LOCALWFS = 1;

    /**
     * A constant indicating a remote wms.
     */
    public static final int REMOTEWMS = 2;

    /**
     * A constant indicating a remote wcs.
     */
    public static final int REMOTEWCS = 3;

    /**
     * A constant indicating a remote wfs.
     */
    public static final int REMOTEWFS = 4;

    /**
     * A constant indicating a database
     */
    public static final int DATABASE = 5;

    /**
     * A constant indicating an external data access
     */
    public static final int EXTERNALDATAACCESS = 6;

    protected OGCWebService ows = null;

    private URL capabilitiesURL;

    private ScaleHint scaleHint = null;

    private QualifiedName name = null;

    private boolean queryable = false;

    private boolean failOnException = true;

    private URL featureInfoTransform = null;

    private Geometry validArea = null;

    private int reqTimeLimit = 30;

    private int type = 0;

    private URL featureInfoURL;

    private Map<String, String> dimProps;

    /**
     * Creates a new DataSource object.
     *
     * @param queryable
     * @param failOnException
     * @param name
     * @param type
     * @param ows
     * @param capabilitiesURL
     * @param scaleHint
     * @param featureInfoTransform
     */
    protected AbstractDataSource( boolean queryable, boolean failOnException, QualifiedName name, int type,
                                  OGCWebService ows, URL capabilitiesURL, ScaleHint scaleHint, Geometry validArea,
                                  URL featureInfoTransform, int reqTimeLimit, Map<String, String> dimProps ) {
        this.scaleHint = scaleHint;
        this.name = name;
        this.type = type;
        this.ows = ows;
        this.capabilitiesURL = capabilitiesURL;
        this.failOnException = failOnException;
        this.queryable = queryable;
        this.featureInfoTransform = featureInfoTransform;
        this.validArea = validArea;
        this.reqTimeLimit = reqTimeLimit;
        this.dimProps = dimProps;
    }

    /**
     * @return the scale interval the data source is valid
     */
    public ScaleHint getScaleHint() {
        return scaleHint;
    }

    /**
     * @return the name of the data source. The method may returns <tt>null</tt> if so no name is defined and a online
     *         resource or WFS filter have shall be returned.
     */
    public QualifiedName getName() {
        return name;
    }

    /**
     * @return an instance of the <tt>OGCWebService</tt> that represents the datasource. Notice: if more than one layer
     *         uses data that are offered by the same OWS the deegree WMS shall just use one instance for accessing the
     *         OWS
     * @throws OGCWebServiceException
     *
     */
    public abstract OGCWebService getOGCWebService()
                            throws OGCWebServiceException;

    /**
     * @return the type of the data source. possible values are:
     *         <ul>
     *         <li>LOCALWFS</li>
     *         <li>LOCALWCS</li>
     *         <li>REMOTEWFS</li>
     *         <li>REMOTEWCS</li>
     *         <li>REMOTEWMS</li>
     *         </ul>
     *         the values are defined as constants in <tt>DataSource</tt>
     *
     */
    public int getType() {
        return type;
    }

    /**
     * @return true if the requesting of the complete layer and so of the complete request shall fail if access a
     *         datasource fails.
     */
    public boolean isFailOnException() {
        return failOnException;
    }

    /**
     * @return true i a datasource is queryable (considered for GetFeatureInfo requests)
     */
    public boolean isQueryable() {
        return queryable;
    }

    /**
     * @return the URL of the capabilities document describing access to a datasource
     */
    public URL getCapabilitiesURL() {
        return capabilitiesURL;
    }

    /**
     * @return the URL of a XSLT script to transform the GML that internaly will be returned as result to a GetFeature
     *         or cascaded GetFeatureInfo request. The return is null if no transformation shall be performed.
     *
     */
    public URL getFeatureInfoTransform() {
        return featureInfoTransform;
    }

    /**
     * @return the area a datasource is valid
     */
    public Geometry getValidArea() {
        return validArea;
    }

    /**
     * @return the maximum time in seconds a datasource shall be able to return a result.
     */
    public int getRequestTimeLimit() {
        return reqTimeLimit;
    }

    /**
     * @param url
     */
    public void setFeatureInfoURL( URL url ) {
        featureInfoURL = url;
    }

    /**
     * @return the feature info URL or null, if not set
     */
    public URL getFeatureInfoURL() {
        return featureInfoURL;
    }

    @Override
    public String toString() {
        String ret = getClass().getName() + ":\n";
        ret += ( "scaleHint = " + scaleHint + "\n" );
        ret += ( "name = " + name + "\n" );
        ret += ( "type = " + type + "\n" );
        ret += ( "queryable = " + queryable + "\n" );
        ret += ( "failOnException = " + failOnException + "\n" );
        ret += ( "capabilitiesURL = " + capabilitiesURL + "\n" );
        ret += ( "validArea = " + validArea + "\n" );
        return ret;
    }

    /**
     * @return the dimProps
     */
    public Map<String, String> getDimProps() {
        return dimProps;
    }

}
