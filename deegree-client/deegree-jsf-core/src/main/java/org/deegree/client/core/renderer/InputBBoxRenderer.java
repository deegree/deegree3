//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.core.renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;
import javax.faces.render.FacesRenderer;

import org.deegree.client.core.component.HtmlInputBBox;
import org.deegree.client.core.model.BBox;
import org.deegree.client.core.utils.MessageUtils;

import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.renderkit.html_basic.MenuRenderer;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */

@FacesRenderer(componentFamily = "javax.faces.SelectOne", rendererType = "org.deegree.InputBBox")
public class InputBBoxRenderer extends MenuRenderer {

    private static final String CRS_ID_SUFFIX = "crs";

    private static final String MINX_ID_SUFFIX = "miny";

    private static final String MINY_ID_SUFFIX = "minx";

    private static final String MAXX_ID_SUFFIX = "maxx";

    private static final String MAXY_ID_SUFFIX = "maxy";

    @Override
    public void decode( FacesContext context, UIComponent component ) {
        String clientId = component.getClientId();
        HtmlInputBBox bbox = (HtmlInputBBox) component;
        Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
        String crs = null;
        double minx = Double.NaN;
        double miny = Double.NaN;
        double maxx = Double.NaN;
        double maxy = Double.NaN;

        for ( String key : requestMap.keySet() ) {
            try {
                if ( ( clientId + ":" + CRS_ID_SUFFIX ).equals( key ) ) {
                    crs = (String) requestMap.get( key );
                } else if ( ( clientId + ":" + MINX_ID_SUFFIX ).equals( key ) ) {
                    minx = Double.parseDouble( requestMap.get( key ) );
                } else if ( ( clientId + ":" + MINY_ID_SUFFIX ).equals( key ) ) {
                    miny = Double.parseDouble( requestMap.get( key ) );
                } else if ( ( clientId + ":" + MAXX_ID_SUFFIX ).equals( key ) ) {
                    maxx = Double.parseDouble( requestMap.get( key ) );
                } else if ( ( clientId + ":" + MAXY_ID_SUFFIX ).equals( key ) ) {
                    maxy = Double.parseDouble( requestMap.get( key ) );
                }
            } catch ( NumberFormatException e ) {
                // NOTHING TO DO
            }
        }
        bbox.setSubmittedValue( new BBox( crs, new double[] { minx, miny }, new double[] { maxx, maxy } ) );
    }

    @Override
    public Object getConvertedValue( FacesContext context, UIComponent component, Object submittedValue )
                            throws ConverterException {
        return submittedValue;
    }

    @Override
    public void encodeEnd( FacesContext context, UIComponent component )
                            throws IOException {
        HtmlInputBBox bbox = (HtmlInputBBox) component;
        ResponseWriter writer = context.getResponseWriter();
        String clientId = component.getClientId();

        writer.startElement( "table", component );
        writer.writeAttribute( "id", clientId, "id" );
        writer.writeAttribute( "name", clientId, "id" );
        String styleClass = bbox.getStyleClass();
        if ( styleClass != null ) {
            writer.writeAttribute( "class", styleClass, "styleClass" );
        }

        encodeCRSSelect( writer, bbox, clientId, context );
        encodeCoordFields( writer, bbox, clientId );

        writer.endElement( "table" );
    }

    private void encodeCRSSelect( ResponseWriter writer, HtmlInputBBox bbox, String clientId, FacesContext context )
                            throws IOException {
        writer.startElement( "tr", null );

        writer.startElement( "td", null );
        String crsText = bbox.getCrsLabel();
        if ( crsText == null ) {
            crsText = MessageUtils.getResourceText( null, "org.deegree.client.core.renderer.InputBBoxRenderer.CRSLABEL" );
        }
        writer.writeText( crsText, null );
        writer.endElement( "td" );

        writer.startElement( "td", null );

        writer.startElement( "select", null );
        writer.writeAttribute( "id", clientId + ":" + CRS_ID_SUFFIX, "id" );
        writer.writeAttribute( "name", clientId + ":" + CRS_ID_SUFFIX, "id" );
        if ( bbox.getCrsSize() > 0 ) {
            writer.writeAttribute( "size", bbox.getCrsSize(), "crsSize" );
        }

        Iterator<SelectItem> items = RenderKitUtils.getSelectItems( context, bbox );
        renderOptions( context, bbox, items );

        writer.endElement( "select" );
        writer.endElement( "td" );
        writer.endElement( "tr" );
    }

    private void encodeCoordFields( ResponseWriter writer, HtmlInputBBox bbox, String clientId )
                            throws IOException {
        BBox value = bbox.getValue();
        // min X
        double minx = 0;
        if ( value != null && value.getLower() != null && value.getLower().length > 0 ) {
            minx = value.getLower()[0];
        }
        String minxLabel = bbox.getMinxLabel();
        if ( minxLabel == null ) {
            minxLabel = MessageUtils.getResourceText( null,
                                                      "org.deegree.client.core.renderer.InputBBoxRenderer.MINXLABEL" );
        }
        addFieldRow( writer, clientId + ":" + MINX_ID_SUFFIX, minxLabel, minx );

        // min y
        double minY = 0;
        if ( value != null && value.getLower() != null && value.getLower().length > 1 ) {
            minY = value.getLower()[1];
        }
        String minyLabel = bbox.getMinyLabel();
        if ( minyLabel == null ) {
            minyLabel = MessageUtils.getResourceText( null,
                                                      "org.deegree.client.core.renderer.InputBBoxRenderer.MINYLABEL" );
        }
        addFieldRow( writer, clientId + ":" + MINY_ID_SUFFIX, minyLabel, minY );

        // max x
        double maxx = 0;
        if ( value != null && value.getUpper() != null && value.getUpper().length > 0 ) {
            maxx = value.getLower()[0];
        }
        String maxxLabel = bbox.getMaxxLabel();
        if ( maxxLabel == null ) {
            maxxLabel = MessageUtils.getResourceText( null,
                                                      "org.deegree.client.core.renderer.InputBBoxRenderer.MAXXLABEL" );
        }
        addFieldRow( writer, clientId + ":" + MAXX_ID_SUFFIX, maxxLabel, maxx );

        // max y
        double maxy = 0;
        if ( value != null && value.getUpper() != null && value.getUpper().length > 1 ) {
            maxy = value.getLower()[1];
        }
        String maxyLabel = bbox.getMaxxLabel();
        if ( maxyLabel == null ) {
            maxyLabel = MessageUtils.getResourceText( null,
                                                      "org.deegree.client.core.renderer.InputBBoxRenderer.MAXYLABEL" );
        }
        addFieldRow( writer, clientId + ":" + MAXY_ID_SUFFIX, maxyLabel, maxy );
    }

    private void addFieldRow( ResponseWriter writer, String id, String label, double value )
                            throws IOException {
        writer.startElement( "tr", null );

        writer.startElement( "td", null );
        writer.writeText( label, null );
        writer.endElement( "td" );

        writer.startElement( "td", null );
        writer.startElement( "input", null );
        writer.writeAttribute( "id", id, "id" );
        writer.writeAttribute( "name", id, "id" );
        writer.writeAttribute( "type", "text", "text" );
        writer.writeAttribute( "value", value, "value" );
        writer.endElement( "input" );

        writer.endElement( "td" );
        writer.endElement( "tr" );
    }

    @Override
    protected Object getCurrentSelectedValues( UIComponent component ) {
        BBox bbox = ( (HtmlInputBBox) component ).getValue();
        if ( bbox != null && bbox.getCrs() == null ) {
            return new Object[] { bbox.getCrs() };
        }
        return null;
    }
}
