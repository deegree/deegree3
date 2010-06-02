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
package org.deegree.client.mdeditor.gui.components;

import static org.deegree.client.mdeditor.gui.components.HtmlInputTextItems.EVENT_IC;
import static org.deegree.client.mdeditor.gui.components.HtmlInputTextItems.EVENT_VC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@FacesRenderer(componentFamily = "javax.faces.Input", rendererType = "org.deegree.HtmlInputTextItemsRenderer")
public class HtmlInputTextItemsRenderer extends Renderer {

    private static String INDEX_PARAM = "itemIndex";

    private static String ITEM_ID_PARAM = "itemId";

    @Override
    public void decode( FacesContext context, UIComponent component ) {
        ExternalContext external = context.getExternalContext();
        Map<String, String> params = external.getRequestParameterMap();
        String behaviorEvent = params.get( "javax.faces.behavior.event" );

        int itemIndex = Integer.parseInt( params.get( INDEX_PARAM ) );

        if ( EVENT_IC.equals( behaviorEvent ) ) {
            updateItems( ( (UIInput) component ), itemIndex );
        } else {
            String itemId = params.get( ITEM_ID_PARAM );
            valueChange( ( (UIInput) component ), params.get( itemId ), itemIndex );
        }
    }

    @SuppressWarnings("unchecked")
    private void valueChange( UIInput item, String value, int index ) {
        try {
            Object v = item.getValue();
            if ( v instanceof List<?> ) {
                if ( index < ( (List) v ).size() ) {
                    ( (List<Object>) v ).remove( index );
                }
                ( (List<Object>) v ).add( index, value );
            } else {
                v = value;
            }
            item.setSubmittedValue( v );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private void updateItems( UIInput item, int index ) {
        List<?> values = (List<?>) item.getValue();
        if ( index < 0 ) {
            // add new item
            values.add( null );
        } else if ( index < values.size() - 1 ) {
            // remove item
            values.remove( index );
        }
    }

    @Override
    public void encodeBegin( FacesContext context, UIComponent component )
                            throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        // hidden div for ajax handling
        writer.startElement( "div", component );
        writer.writeAttribute( "id", component.getClientId(), null );
    }

    @Override
    public void encodeEnd( FacesContext context, UIComponent component )
                            throws IOException {
        HtmlInputTextItems comp = (HtmlInputTextItems) component;
        String clientId = comp.getClientId();
        Object v = comp.getValue();

        ResponseWriter writer = context.getResponseWriter();

        String contextName = context.getExternalContext().getContextName();
        if ( v instanceof List<?> && ( (List<?>) v ).size() > 1 ) {
            List<?> values = (List<?>) v;
            for ( int index = 0; index < values.size(); index++ ) {
                boolean isLast = index == values.size() - 1;
                writeTD( context, writer, comp, values.get( index ), clientId + index, contextName, isLast, index );
            }
        } else {
            Object value = v;
            if ( v instanceof List<?> && ( (List<?>) v ).size() == 1 ) {
                value = ( (List<?>) v ).get( 0 );
            } else if ( v instanceof List<?> && ( (List<?>) v ).size() == 0 ) {
                value = null;
            }
            writeTD( context, writer, comp, value, clientId + 0, contextName, true, 0 );
        }

        // close hidden div for ajax handling
        writer.endElement( "div" );
    }

    private void writeTD( FacesContext context, ResponseWriter writer, HtmlInputTextItems component,
                          Object value, String id, String contextName, boolean isLast, int index )
                            throws IOException {
        writer.startElement( "tr", component );
        writer.startElement( "td", component );
        writer.startElement( "input", component );
        writer.writeAttribute( "type", "text", null );
        writer.writeAttribute( "id", id, null );
        writer.writeAttribute( "name", id, null );
        if ( value != null )
            writer.writeAttribute( "value", String.valueOf( value ), null );
        addValueChangedBehavior( context, writer, component, index, id );
        writer.endElement( "input" );

        writer.endElement( "td" );

        writer.startElement( "td", component );
        writer.startElement( "a", component );
        addItemChangedBehaviour( context, writer, component, isLast ? -1 : index );

        writer.startElement( "div", component );
        writer.writeAttribute( "class", isLast ? "add" : "delete", null );
        writer.endElement( "div" );
        writer.endElement( "a" );
        writer.endElement( "td" );
        writer.endElement( "tr" );
    }

    private void addValueChangedBehavior( FacesContext context, ResponseWriter writer,
                                          HtmlInputTextItems component, int index, String id )
                            throws IOException {
        Map<String, List<ClientBehavior>> behaviors = component.getClientBehaviors();
        if ( behaviors.containsKey( EVENT_VC ) ) {

            List<ClientBehaviorContext.Parameter> params = new ArrayList<ClientBehaviorContext.Parameter>();
            params.add( new ClientBehaviorContext.Parameter( INDEX_PARAM, index ) );
            params.add( new ClientBehaviorContext.Parameter( ITEM_ID_PARAM, id ) );
            ClientBehaviorContext behaviorContext = ClientBehaviorContext.createClientBehaviorContext(
                                                                                                       context,
                                                                                                       component,
                                                                                                       EVENT_VC,
                                                                                                       component.getClientId(),
                                                                                                       params );
            String jsfJsCode = behaviors.get( EVENT_VC ).get( 0 ).getScript( behaviorContext );
            writer.writeAttribute( "onkeyup", jsfJsCode, null );
        }
    }

    private void addItemChangedBehaviour( FacesContext context, ResponseWriter writer,
                                          HtmlInputTextItems component, int index )
                            throws IOException {
        String options = "{'javax.faces.behavior.event':'" + EVENT_IC + "', " + "'execute':'@this', " + "'render':'"
                         + component.getParent().getClientId() + "', " + "'itemIndex':'" + index + "'}";
        String request = "jsf.ajax.request('" + component.getClientId() + "', event, " + options + ")";
        writer.writeAttribute( "onclick", request, null );
    }
}
