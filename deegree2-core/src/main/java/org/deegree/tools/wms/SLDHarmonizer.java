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

package org.deegree.tools.wms;

import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.framework.xml.XMLTools.getRequiredNodeAsString;
import static org.deegree.ogcbase.CommonNamespaces.SLDNS;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import javax.xml.transform.TransformerException;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <code>SLDHarmonizer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SLDHarmonizer {

    private static final ILogger LOG = getLogger( SLDHarmonizer.class );

    private static final NamespaceContext nsContext = getNamespaceContext();

    private File sldFile;

    private XMLFragment doc;

    private SLDHarmonizer( String file ) throws MalformedURLException, IOException {
        sldFile = new File( file );

        if ( !sldFile.exists() ) {
            LOG.logInfo( "SLD file does not exist: " + sldFile );
            return;
        }

        try {
            doc = new XMLFragment( sldFile );
        } catch ( SAXException e ) {
            LOG.logDebug( "Stack trace: ", e );
            LOG.logInfo( "SLD file cannot be parsed: " + sldFile );
        }
    }

    private void harmonize()
                            throws UnsupportedEncodingException, FileNotFoundException, TransformerException,
                            XMLParsingException {
        if ( doc == null ) {
            return;
        }

        Element root = doc.getRootElement();
        root.setAttribute( "xmlns:app", "http://www.deegree.org/app" );

        for ( Element e : getElements( root, "//sld:NamedLayer/sld:UserStyle[count(sld:Name) = 0]", nsContext ) ) {
            String name = "default:" + getRequiredNodeAsString( e, "../sld:Name", nsContext );
            Element elem = root.getOwnerDocument().createElementNS( SLDNS.toASCIIString(), "Name" );
            elem.setTextContent( name );
            e.insertBefore( elem, e.getChildNodes().item( 0 ) );
        }

        for ( Element e : getElements( root, "//ogc:PropertyName", nsContext ) ) {
            String name = e.getTextContent().toLowerCase();
            name = name.startsWith( "app:" ) ? name : "app:" + name;
            e.setTextContent( name );
        }

        for ( Element e : getElements( root, "//sld:CssParameter[@name = 'stroke-width' and text() = '-1']", nsContext ) ) {
            e.setTextContent( "1" );
        }

        doc.prettyPrint( new OutputStreamWriter( new FileOutputStream( sldFile ), "UTF-8" ) );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        if ( args.length == 0 ) {
            LOG.logInfo( "Usage:" );
            LOG.logInfo( "java -cp deegree2.jar org.deegree.tools.wms.SLDHarmonizer <sldfile> [<sldfiles>]" );
            return;
        }

        for ( String file : args ) {
            try {
                new SLDHarmonizer( file ).harmonize();
            } catch ( UnsupportedEncodingException e ) {
                LOG.logError( "Unknown error", e );
            } catch ( FileNotFoundException e ) {
                LOG.logError( "Unknown error", e );
            } catch ( MalformedURLException e ) {
                LOG.logError( "Unknown error", e );
            } catch ( TransformerException e ) {
                LOG.logError( "Unknown error", e );
            } catch ( XMLParsingException e ) {
                LOG.logError( "Unknown error", e );
            } catch ( IOException e ) {
                LOG.logError( "Unknown error", e );
            }
        }
    }

}
