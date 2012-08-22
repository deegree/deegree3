//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.portal.standard.wmps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.Constants;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.ViewContext;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PrintWMPSListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( PrintWMPSListener.class );

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        String wmpsAddr = getInitParameter( "WMPS" );
        List<Layer> visibleLayers = getVisibleLayers( event );
        // read parameters from request
        Map<String, Object> parameters = event.getParameter();
        int dpi = Integer.parseInt( (String) parameters.get( "dpi" ) );
        boolean legend = new Boolean( (String) parameters.get( "legend" ) );
        boolean scaleBar = new Boolean( (String) parameters.get( "scale" ) );
        String template = (String) parameters.get( "template" );
        String email = (String) parameters.get( "email" );
        int scaleValue = Integer.parseInt( (String) parameters.get( "scaleValue" ) );
        String tmp = (String) parameters.get( "bbox" );
        Envelope bbox = GeometryFactory.createEnvelope( tmp, null );
        String srs = (String) parameters.get( "srs" );
        List<Map<String, String>> fields = (List<Map<String, String>>) parameters.get( "fields" );
        Map<String, String> fieldMap = (Map<String, String>) fields.get( 0 );

        // create PrintMap request using printmap_template.xml as template
        XMLFragment printMap = createRequest( visibleLayers, dpi, legend, scaleBar, template, email, scaleValue, bbox,
                                              srs, fieldMap );

        LOG.logDebug( printMap.getAsPrettyString() );

        // reading initial response from WMPS
        InputStream is = HttpUtils.performHttpPost( wmpsAddr, printMap.getAsString(), timeout, null, null, "text/xml",
                                                    null, null ).getResponseBodyAsStream();
        XMLFragment xml = new XMLFragment();
        try {
            xml.load( is, wmpsAddr );
        } catch ( Exception e ) {
            LOG.logError( e );
        }
        if ( CommonNamespaces.DEEGREEWMPS.toASCIIString().equals( xml.getRootElement().getNamespaceURI() ) ) {
            responseHandler.writeAndClose( Messages.getMessage( "IGEO_STD_WMPS_REQ_RECEIVED" ) );
        } else {
            LOG.logError( xml.getAsPrettyString() );
            responseHandler.writeAndClose( Messages.getMessage( "IGEO_STD_WMPS_REQ_ERROR" ) );
        }
    }

    /**
     * @param visibleLayers
     * @param dpi
     * @param legend
     * @param scaleBar
     * @param template
     * @param email
     * @param scaleValue
     * @param bbox
     * @param srs
     * @param fieldMap
     * @return
     * @throws IOException
     */
    private XMLFragment createRequest( List<Layer> visibleLayers, int dpi, boolean legend, boolean scaleBar,
                                       String template, String email, int scaleValue, Envelope bbox, String srs,
                                       Map<String, String> fieldMap )
                            throws IOException {
        URL url = PrintWMPSListener.class.getResource( "printmap_template.xml" );
        XMLFragment printMap = null;
        try {
            printMap = new XMLFragment( url );
        } catch ( SAXException e ) {
            LOG.logError( e );
            throw new IOException( "can not parse PrintMap template" );
        }

        try {
            // set bounding box and SRS
            Element element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(),
                                                                  "deegreewmps:Center/gml:pos", nsc );
            Point center = bbox.getCentroid();
            XMLTools.setNodeValue( element, center.getX() + " " + center.getY() );
            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:Center", nsc );
            element.setAttribute( "srsName", srs );

            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:ScaleDenominator",
                                                          nsc );
            XMLTools.setNodeValue( element, Integer.toString( scaleValue ) );

            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:Legend", nsc );
            XMLTools.setNodeValue( element, Boolean.toString( legend ) );

            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:ScaleBar", nsc );
            XMLTools.setNodeValue( element, Boolean.toString( scaleBar ) );

            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:Template", nsc );
            XMLTools.setNodeValue( element, template );

            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:EMailAddress", nsc );
            XMLTools.setNodeValue( element, email );

            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:DPI", nsc );
            XMLTools.setNodeValue( element, Integer.toString( dpi ) );

            // set layers to be printed
            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:Layers", nsc );
            for ( Layer layer : visibleLayers ) {
                Element nl = XMLTools.appendElement( element, CommonNamespaces.SLDNS, "NamedLayer" );
                Element l = XMLTools.appendElement( nl, CommonNamespaces.SLDNS, "Name" );
                XMLTools.setNodeValue( l, layer.getName() );
                Element nst = XMLTools.appendElement( nl, CommonNamespaces.SLDNS, "NamedStyle" );
                Element st = XMLTools.appendElement( nst, CommonNamespaces.SLDNS, "Name" );
                XMLTools.setNodeValue( st, layer.getStyleList().getCurrentStyle().getName() );
            }

            // set free defined text areas
            element = (Element) XMLTools.getRequiredNode( printMap.getRootElement(), "deegreewmps:TextAreas", nsc );
            Iterator<String> iter = fieldMap.keySet().iterator();
            while ( iter.hasNext() ) {
                String key = (String) iter.next();
                String value = fieldMap.get( key );
                if ( value != null && value.trim().length() > 0 ) {
                    Element ta = XMLTools.appendElement( element, CommonNamespaces.DEEGREEWMPS, "TextArea" );
                    Element name = XMLTools.appendElement( ta, CommonNamespaces.DEEGREEWMPS, "Name" );
                    XMLTools.setNodeValue( name, key );
                    Element text = XMLTools.appendElement( ta, CommonNamespaces.DEEGREEWMPS, "Text" );
                    XMLTools.setNodeValue( text, value );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e );
            throw new IOException( "can not set request values" );
        }

        return printMap;
    }

    private List<Layer> getVisibleLayers( WebEvent event ) {
        ViewContext vc = (ViewContext) event.getSession().getAttribute( Constants.CURRENTMAPCONTEXT );
        Layer[] layers = vc.getLayerList().getLayers();
        List<Layer> visibleLayers = new ArrayList<Layer>();
        for ( Layer layer : layers ) {
            if ( !layer.isHidden() ) {
                visibleLayers.add( layer );
            }
        }
        Collections.reverse( visibleLayers );
        return visibleLayers;
    }

}
