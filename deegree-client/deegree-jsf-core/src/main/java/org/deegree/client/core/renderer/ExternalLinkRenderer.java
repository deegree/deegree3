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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

/**
 * <code>ExternalLinkRenderer</code> renders a link ignoring the JSF navigation
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */

@FacesRenderer(componentFamily = "javax.faces.Command", rendererType = "org.deegree.ExternalLink")
public class ExternalLinkRenderer extends Renderer {

    @Override
    public void encodeBegin( FacesContext context, UIComponent component )
                            throws IOException {
        ResponseWriter responseWriter = context.getResponseWriter();
        String clientId = component.getClientId();

        responseWriter.startElement( "a", null );
        responseWriter.writeAttribute( "id", clientId, "id" );
        responseWriter.writeAttribute( "name", clientId, "clientId" );
        Object href = component.getAttributes().get( "href" );
        responseWriter.writeAttribute( "href", href, null );

        String styleClass = (String) component.getAttributes().get( "styleClass" );
        if ( styleClass != null ) {
            responseWriter.writeAttribute( "class", styleClass, "styleClass" );
        }

        String style = (String) component.getAttributes().get( "style" );
        if ( style != null ) {
            responseWriter.writeAttribute( "style", style, "style" );
        }
        String target = (String) component.getAttributes().get( "target" );
        if ( target != null ) {
            responseWriter.writeAttribute( "target", target, "target" );
        }

        Object title = (String) component.getAttributes().get( "title" );
        responseWriter.writeText( title != null ? title : ( href != null ? href : "" ), null );

        responseWriter.endElement( "a" );
        responseWriter.flush();
    }

}
