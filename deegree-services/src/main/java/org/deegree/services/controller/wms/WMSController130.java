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

package org.deegree.services.controller.wms;

import static org.deegree.services.controller.ows.OWSException.INVALID_CRS;
import static org.deegree.services.i18n.Messages.get;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wms.Utils;
import org.deegree.services.controller.AbstractOGCServiceController;
import org.deegree.services.controller.ows.OGCExceptionXMLAdapter;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.controller.wms.capabilities.Capabilities130XMLAdapter;
import org.deegree.services.jaxb.metadata.ServiceIdentificationType;
import org.deegree.services.jaxb.metadata.ServiceProviderType;
import org.deegree.services.wms.MapService;

/**
 * <code>WMSController130</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMSController130 extends WMSControllerBase {

    protected final static HashMap<String, String> CRSMAPPINGS = new HashMap<String, String>();

    static {
        CRSMAPPINGS.put( "EPSG:4326", "urn:ogc:def:crs:EPSG::4326" );
        CRSMAPPINGS.put( "EPSG:31466", "urn:ogc:def:crs:EPSG::31466" );
        CRSMAPPINGS.put( "EPSG:31467", "urn:ogc:def:crs:EPSG::31467" );
        CRSMAPPINGS.put( "EPSG:31468", "urn:ogc:def:crs:EPSG::31468" );
        CRSMAPPINGS.put( "EPSG:31469", "urn:ogc:def:crs:EPSG::31469" );
    }

    /**
     * 
     */
    public WMSController130() {
        EXCEPTION_DEFAULT = "XML";
        EXCEPTION_BLANK = "BLANK";
        EXCEPTION_INIMAGE = "INIMAGE";

        EXCEPTIONS = new OGCExceptionXMLAdapter();
    }

    public void sendException( OWSException ex, HttpResponseBuffer response )
                            throws ServletException {
        AbstractOGCServiceController.sendException( "text/xml", "UTF-8", null, 200, EXCEPTIONS, ex, response );
    }

    public void throwSRSException( String name )
                            throws OWSException {
        throw new OWSException( get( "WMS.INVALID_SRS", name ), INVALID_CRS );
    }

    /**
     * @param crs
     * @return a new CRS
     */
    public static CRS getCRS( String crs ) {
        String other = CRSMAPPINGS.get( crs );
        return new CRS( other == null ? crs : other );
    }

    /**
     * @param crs
     * @param bbox
     * @return a new CRS
     */
    public static Envelope getCRSAndEnvelope( String crs, double[] bbox ) {
        if ( crs.startsWith( "AUTO2:" ) ) {
            String[] cs = crs.split( ":" )[1].split( "," );
            int id = Integer.parseInt( cs[0] );
            // this is not supported
            double factor = Double.parseDouble( cs[1] );
            double lon0 = Double.parseDouble( cs[2] );
            double lat0 = Double.parseDouble( cs[3] );

            return new GeometryFactory().createEnvelope( factor * bbox[0], factor * bbox[1], factor * bbox[2],
                                                         factor * bbox[3], Utils.getAutoCRS( id, lon0, lat0 ) );
        }
        String other = CRSMAPPINGS.get( crs );
        return new GeometryFactory().createEnvelope( bbox[0], bbox[1], bbox[2], bbox[3], new CRS( other == null ? crs
                                                                                                               : other ) );
    }

    @Override
    protected void exportCapas( String getUrl, String postUrl, MapService service, HttpResponseBuffer response,
                                ServiceIdentificationType identification, ServiceProviderType provider,
                                WMSController controller )
                            throws IOException {
        response.setContentType( "text/xml" );
        try {
            XMLStreamWriter xmlWriter = response.getXMLWriter();
            new Capabilities130XMLAdapter( identification, provider, getUrl, postUrl, service, controller ).export( xmlWriter );
        } catch ( XMLStreamException e ) {
            throw new IOException( e );
        }
    }

}
