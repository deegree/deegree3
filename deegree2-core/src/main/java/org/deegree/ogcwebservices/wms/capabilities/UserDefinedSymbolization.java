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
package org.deegree.ogcwebservices.wms.capabilities;



/**
 * The interface defines the access to optional user-defined symbolization
 * that are only used by SLD-enabled WMSes.
 * <p>----------------------------------------------------------------------</p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$
 */
public class UserDefinedSymbolization {
    private boolean remoteWFS = false;
    private boolean sldSupported = false;
    private boolean userLayer = false;
    private boolean userStyle = false;


    /**
    * constructor initializing the class with the <UserDefinedSymbolization>
     * @param sldSupported
     * @param userLayer
     * @param remoteWFSEnabled
     * @param userStyle
    */
    public UserDefinedSymbolization( boolean sldSupported, boolean userLayer,
                              boolean remoteWFSEnabled, boolean userStyle ) {
        setSldSupported( sldSupported );
        setUserLayerSupported( userLayer );
        setRemoteWFSSupported( remoteWFSEnabled );
        setUserStyleSupported( userStyle );
    }

    /**
     * @return true if layer and/or style definition conform to SLD
     * are supported.
     */
    public boolean isSldSupported() {
        return sldSupported;
    }

    /**
    * sets true if layer and/or style definition conform to SLD
    * are supported.
     * @param sldSupported
    */
    public void setSldSupported( boolean sldSupported ) {
        this.sldSupported = sldSupported;
    }

    /**
     * @return true if the WMS has user defined layers.
     */
    public boolean isUserLayerSupported() {
        return userLayer;
    }

    /**
    * sets true if the WMS has user defined layers.
     * @param userLayer
    */
    public void setUserLayerSupported( boolean userLayer ) {
        this.userLayer = userLayer;
    }

    /**
     * @return true if the WMS has user defined styles.
     */
    public boolean isUserStyleSupported() {
        return userStyle;
    }

    /**
    * sets true if the WMS has user defined styles.
     * @param userStyle
    */
    public void setUserStyleSupported( boolean userStyle ) {
        this.userStyle = userStyle;
    }

    /**
     * @return true if the WMS enables the use of a remote (user defined)
     * WFS.
     */
    public boolean isRemoteWFSSupported() {
        return remoteWFS;
    }

    /**
    * sets true if the WMS enables the use of a remote (user defined)
    * WFS.
     * @param remoteWFSEnabled
    */
    public void setRemoteWFSSupported( boolean remoteWFSEnabled ) {
        this.remoteWFS = remoteWFSEnabled;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "sldSupported = " + sldSupported + "\n";
        ret += ( "userLayer = " + userLayer + "\n" );
        ret += ( "remoteWFSEnabled = " + remoteWFS + "\n" );
        ret += ( "userStyle = " + userStyle + "\n" );
        return ret;
    }

}
