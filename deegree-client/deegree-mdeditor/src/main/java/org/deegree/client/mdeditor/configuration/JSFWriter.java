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
package org.deegree.client.mdeditor.configuration;

import java.io.FileNotFoundException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class JSFWriter {

    public static final String HTML_NS = "http://www.w3.org/1999/xhtml";

    public static final String H_NS = "http://java.sun.com/jsf/html";

    public static final String F_NS = "http://java.sun.com/jsf/core";

    public static final String UI_NS = "http://java.sun.com/jsf/facelets";

    public static final String H_PREF = "h";

    public static final String F_PREF = "f";

    public static final String UI_PREF = "ui";

    protected boolean isEntry = true;

    public JSFWriter( boolean isEntry ) {
        this.isEntry = isEntry;
    }

    abstract FormGroupWriter addFormGroup( String id, String label, String title, boolean isEmbedded )
                            throws XMLStreamException, FileNotFoundException;

    protected void startWriting( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartDocument();
        writer.writeStartElement( "html" );
        writer.writeNamespace( null, HTML_NS );
        writer.writeNamespace( F_PREF, F_NS );
        writer.writeNamespace( H_PREF, H_NS );
        writer.writeNamespace( UI_PREF, UI_NS );

        writer.writeStartElement( H_PREF, "head", H_NS );
        writer.writeEndElement();

        writer.writeStartElement( H_PREF, "body", H_NS );

        if ( isEntry ) {
            writer.writeStartElement( UI_PREF, "composition", UI_NS );
            writer.writeAttribute( "template", "/page/templates/formLayout.xhtml" );

            writer.writeStartElement( UI_PREF, "define", UI_NS );
            writer.writeAttribute( "name", "content" );
        }

    }

    protected void finishWriting( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeEndElement();
        writer.writeEndElement();
        if ( isEntry ) {
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.close();
    }

    public boolean isEntry() {
        return isEntry;
    }

}
