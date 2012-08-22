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
package org.deegree.ogcwebservices.getcapabilities;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.Marshallable;
import org.w3c.dom.Document;

/**
 * The purpose of the GetCapabilities operation is described in the Basic CapabilitiesService
 * Elements section, above. In the particular case of a Web Map CapabilitiesService, the response of
 * a GetCapabilities request is general information about the service itself and specific
 * information about the available maps.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @version 2002-03-01, $Revision$, $Date$
 * @since 1.0
 */

public abstract class OGCWebServiceCapabilities implements Marshallable {

    protected static final ILogger LOG = LoggerFactory.getLogger( OGCWebServiceCapabilities.class );

    private CapabilitiesService service = null;

    private String updateSequence = null;

    private String version = null;

    /**
     * constructor initializing the class with the OGCWebServiceCapabilities
     *
     * @param version
     * @param updateSequence
     * @param service
     */
    public OGCWebServiceCapabilities( String version, String updateSequence, CapabilitiesService service ) {
        setVersion( version );
        setUpdateSequence( updateSequence );
        setService( service );
    }

    /**
     * returns the version of the service
     *
     * @return the version of the service
     */
    public String getVersion() {
        return version;
    }

    /**
     * sets the version of the service
     *
     * @param version
     */
    public void setVersion( String version ) {
        this.version = version;
    }

    /**
     * The UPDATESEQUENCE parameter is for maintaining cache consistency. Its value can be an
     * integer, a timestamp in [ISO 8601:1988(E)] format , or any other number or string. The server
     * may include an UpdateSequence value in its Capabilities XML. If present, this value should be
     * increased when changes are made to the Capabilities (e.g., when new maps are added to the
     * service). The server is the sole judge of lexical ordering sequence. The client may include
     * this parameter in its GetCapabilities request.
     *
     * @return the update sequence value
     */
    public String getUpdateSequence() {
        return updateSequence;
    }

    /**
     * sets the update sequence
     *
     * @param updateSequence
     */
    public void setUpdateSequence( String updateSequence ) {
        this.updateSequence = updateSequence;
    }

    /**
     * this returns a general describtion of the service described by the Capabilities XML document.
     *
     * @return a general describtion of the service described by the Capabilities XML document.
     */
    public CapabilitiesService getService() {
        return service;
    }

    /**
     * this sets a general describtion of the service described by the Capabilities XML document.
     *
     * @param service
     */
    public void setService( CapabilitiesService service ) {
        this.service = service;
    }

    /**
     * Must be overridden by subclass. Replaces abstract method exportAsXML.
     *
     * @return null!
     *
     */
    public Document export() {
        return null;
    }
}
