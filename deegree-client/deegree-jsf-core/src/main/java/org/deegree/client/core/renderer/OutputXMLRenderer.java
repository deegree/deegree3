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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.core.component.HtmlOutputXML;
import org.deegree.client.core.utils.Utils;
import org.slf4j.Logger;

/**
 * Renderer for {@link HtmlOutputXML}. Renders the value xml indentent and colored, if possible, otherwise as simple
 * String. If the value should be downloadable, a file will be created and a download linked rendered.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@FacesRenderer(componentFamily = "javax.faces.Output", rendererType = "org.deegree.OutputXML")
public class OutputXMLRenderer extends Renderer {

    private static final Logger LOG = getLogger( OutputXMLRenderer.class );

    @Override
    public void encodeBegin( FacesContext context, UIComponent component )
                            throws IOException {
        HtmlOutputXML xmlComponent = (HtmlOutputXML) component;
        String clientId = xmlComponent.getClientId();
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement( "div", xmlComponent );
        writer.writeAttribute( "id", clientId, "clientId" );
        writer.writeAttribute( "name", clientId, "clientId" );
        writer.writeAttribute( "class", "outputXML", "styleClass" );

        if ( xmlComponent.isDownloadable() ) {
            encodeDownload( writer, xmlComponent );
        }

        encodeXML( writer, xmlComponent.getValue(), xmlComponent );
        writer.endElement( "div" );
    }

    private void encodeDownload( ResponseWriter writer, HtmlOutputXML xmlComponent )
                            throws IOException {
        String downloadDir = xmlComponent.getDownloadDir();
        String fileName = Utils.getFileName( "xml" );
        URL webAccessibleUrl = Utils.getWebAccessibleUrl( downloadDir, fileName );
        try {
            File f = new File( Utils.getAbsolutePath( downloadDir, fileName ) );
            if ( !f.createNewFile() ) {
                LOG.info( "Could not create file for download" );
                return;
            }
            FileWriter fw = new FileWriter( f );
            fw.write( xmlComponent.getValue() );
            fw.close();
            DeleteThread thread = new DeleteThread( f, xmlComponent.getMinutesUntilDelete() * 3600 );
            thread.start();
        } catch ( Exception e ) {
            LOG.warn( "Could not write file for download: " + e.getMessage() );
            return;
        }
        writer.startElement( "div", null );
        
        writer.write( xmlComponent.getDownloadLabel() );
        writer.startElement( "a", null );
        writer.writeAttribute( "href", webAccessibleUrl.toExternalForm(), null );
        writer.writeAttribute( "target", "_blank", null );
        writer.write( fileName );
        writer.endElement( "a" );
        writer.endElement( "div" );

    }

    private void encodeXML( ResponseWriter writer, String value, HtmlOutputXML xmlComponent )
                            throws IOException {
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( new StringReader( value ) );
            int depth = 0;
            boolean lastWasEndElement = false;
            boolean lastWasComment = false;
            while ( reader.hasNext() ) {
                switch ( reader.getEventType() ) {
                case XMLStreamConstants.START_ELEMENT:
                    if ( !lastWasComment ) {
                        writer.startElement( "br", xmlComponent );
                        writer.endElement( "br" );
                    }
                    writer.write( getSpaces( depth ) );
                    writer.startElement( "span", xmlComponent );
                    writer.writeAttribute( "class", "sign", null );
                    writer.write( "&lt;" );
                    writer.endElement( "span" );

                    writer.startElement( "span", xmlComponent );
                    writer.writeAttribute( "class", "tag", null );
                    String prefix = reader.getPrefix();
                    writer.write( ( prefix != null && prefix.length() > 0 ? prefix + ":" : "" ) + reader.getLocalName() );
                    writer.endElement( "span" );

                    if ( reader.getAttributeCount() > 0 ) {
                        for ( int i = 0; i < reader.getAttributeCount(); i++ ) {
                            writer.startElement( "span", xmlComponent );
                            writer.writeAttribute( "class", "attributeName", null );
                            writer.write( "&#160;" );
                            String attributePrefix = reader.getAttributePrefix( i );
                            writer.write( ( attributePrefix != null && attributePrefix.length() > 0 ? attributePrefix
                                                                                                      + ":" : "" )
                                          + reader.getAttributeName( i ) );
                            writer.endElement( "span" );

                            writer.startElement( "span", xmlComponent );
                            writer.writeAttribute( "class", "sign", null );
                            writer.write( "=\"" );
                            writer.endElement( "span" );

                            writer.startElement( "span", xmlComponent );
                            writer.writeAttribute( "class", "text", null );
                            writer.write( reader.getAttributeValue( i ) );
                            writer.endElement( "span" );

                            writer.startElement( "span", xmlComponent );
                            writer.writeAttribute( "class", "sign", null );
                            writer.write( "\"" );
                            writer.endElement( "span" );
                        }
                    }
                    writer.startElement( "span", xmlComponent );
                    writer.writeAttribute( "class", "sign", null );
                    writer.write( "&gt;" );
                    writer.endElement( "span" );
                    depth++;
                    lastWasEndElement = false;
                    lastWasComment = false;
                    break;
                case XMLStreamConstants.CHARACTERS:
                    writer.startElement( "span", xmlComponent );
                    writer.writeAttribute( "class", "text", null );
                    writer.write( reader.getText() );
                    writer.endElement( "span" );
                    lastWasEndElement = false;
                    lastWasComment = false;
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    depth--;
                    if ( lastWasEndElement ) {
                        writer.startElement( "br", xmlComponent );
                        writer.endElement( "br" );
                        writer.write( getSpaces( depth ) );
                    }
                    writer.startElement( "span", xmlComponent );
                    writer.writeAttribute( "class", "sign", null );
                    writer.write( "&lt;/" );
                    writer.endElement( "span" );

                    writer.startElement( "span", xmlComponent );
                    writer.writeAttribute( "class", "tag", null );
                    writer.write( reader.getLocalName() );
                    writer.endElement( "span" );

                    writer.startElement( "span", xmlComponent );
                    writer.writeAttribute( "class", "sign", null );
                    writer.write( "&gt;" );
                    writer.endElement( "span" );
                    lastWasEndElement = true;
                    lastWasComment = false;
                    break;
                case XMLStreamConstants.COMMENT:
                    writer.startElement( "div", xmlComponent );
                    writer.writeAttribute( "class", "comment", null );
                    writer.write( "&lt;/!--" + reader.getText() + "--&gt;" );
                    writer.endElement( "div" );
                    lastWasEndElement = false;
                    lastWasComment = true;
                    break;
                default:
                    break;
                }
                reader.next();

            }
            reader.close();
        } catch ( XMLStreamException e ) {
            writer.writeText( value, null );
        }
    }

    private String getSpaces( int depth ) {
        int indent = 4;
        int n = depth * indent;
        char[] chars = new char[n];
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < chars.length; i++ ) {
            sb.append( "&#160;" );
        }
        return sb.toString();
    }

    private class DeleteThread extends Thread {

        private final File fileToDelete;

        private final int secondesUntilDelete;

        public DeleteThread( File fileToDelete, int secondesUntilDelete ) {
            this.fileToDelete = fileToDelete;
            this.secondesUntilDelete = secondesUntilDelete;
        }

        @Override
        public void run() {
            if ( secondesUntilDelete > 0 )
                try {
                    DeleteThread.sleep( secondesUntilDelete );
                } catch ( InterruptedException e ) {
                    LOG.debug( "Could not sleep delete thread: " + e.getMessage() );
                }
            if ( fileToDelete != null ) {
                try {
                    if ( fileToDelete.delete() ) {
                        LOG.debug( "Successfully deleted file " + fileToDelete.getName() );
                    } else {
                        LOG.debug( "Could not delete file " + fileToDelete.getName() );
                    }
                } catch ( Exception e ) {
                    LOG.error( "Could not delete file " + fileToDelete.getName() + ": " + e.getMessage() );
                }
            }
        }
    }

}
