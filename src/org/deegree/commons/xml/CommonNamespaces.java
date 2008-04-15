// $HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/src/org/deegree/ogcbase/CommonNamespaces.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.commons.xml;

import java.net.URI;
import java.net.URISyntaxException;

import org.deegree.commons.logging.BootLogger;

/**
 * Definitions for OGC related namespace bindings.
 * <p>
 * NOTE: Don't put project specific bindings here -- subclass it and override
 * {@link #getNamespaceContext()} instead.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:tfriebe@sf.net">Torsten Friebe</a>
 * @author last edited by: $Author: apoth $
 * 
 * @version $Revision: 10660 $, $Date: 2008-03-24 22:39:54 +0100 (Mo, 24 Mrz 2008) $
 */
public class CommonNamespaces {

    // namespaces
    /**
     * The XMLNS namespace is currently bound to: "http://www.w3.org/2000/xmlns/"
     */
    public static final URI XMLNS = buildNSURI( "http://www.w3.org/2000/xmlns/" );

    /**
     * The GMLNS namespace is currently bound to: "http://www.opengis.net/gml"
     */
    public static final URI GMLNS = buildNSURI( "http://www.opengis.net/gml" );

    /**
     * The OGCNS namespace is currently bound to: "http://www.opengis.net/ogc"
     */
    public static final URI OGCNS = buildNSURI( "http://www.opengis.net/ogc" );

    /**
     * The XLNNS namespace is currently bound to: "http://www.w3.org/1999/xlink"
     */
    public static final URI XLNNS = buildNSURI( "http://www.w3.org/1999/xlink" );

    /**
     * The ISO19115NS namespace is currently bound to: "http://schemas.opengis.net/iso19115full"
     */
    public static final URI ISO19115NS = buildNSURI( "http://schemas.opengis.net/iso19115full" );

    /**
     * The ISO19115BRIEFNS namespace is currently bound to:
     * "http://schemas.opengis.net/iso19115brief"
     */
    public static final URI ISO19115BRIEFNS = buildNSURI( "http://schemas.opengis.net/iso19115brief" );

    /**
     * The ISO19119NS namespace is currently bound to: "http://schemas.opengis.net/iso19119"
     */
    public static final URI ISO19119NS = buildNSURI( "http://schemas.opengis.net/iso19119" );

    /**
     * The DCNS namespace is currently bound to: "http://purl.org/dc/elements/1.1/"
     */
    public static final URI DCNS = buildNSURI( "http://purl.org/dc/elements/1.1/" );

    /**
     * The XSNS namespace is currently bound to: "http://www.w3.org/2001/XMLSchema"
     */
    public static final URI XSNS = buildNSURI( "http://www.w3.org/2001/XMLSchema" );

    /**
     * The XSINS namespace is currently bound to: "http://www.w3.org/2001/XMLSchema-instance"
     */
    public static final URI XSINS = buildNSURI( "http://www.w3.org/2001/XMLSchema-instance" );

    /**
     * The SMXMLNS namespace is currently bound to: "http://metadata.dgiwg.org/smXML"
     */
    public static final URI SMXMLNS = buildNSURI( "http://metadata.dgiwg.org/smXML" );

    /**
     * The ISOAP10GMDNS namespace is currently bound to: "http://www.isotc211.org/2005/gmd"
     */
    public static final URI ISOAP10GMDNS = buildNSURI( "http://www.isotc211.org/2005/gmd" );

    /**
     * The ISOAP10GCONS namespace is currently bound to: "http://www.isotc211.org/2005/gco"
     */
    public static final URI ISOAP10GCONS = buildNSURI( "http://www.isotc211.org/2005/gco" );

    /**
     * The APISO namespace is currently bound to: "http://www.opengis.net/cat/csw/apiso/1.0"
     */
    public static final URI APISO = buildNSURI( "http://www.opengis.net/cat/csw/apiso/1.0" );

    // prefixes
    /**
     * The GML prefix is currently assigned to: "gml"
     */
    public static final String GML_PREFIX = "gml";

    /**
     * The OGC prefix is currently assigned to: "ogc"
     */
    public static final String OGC_PREFIX = "ogc";

    /**
     * The XLINK prefix is currently assigned to: "xlink"
     */
    public static final String XLINK_PREFIX = "xlink";

    /**
     * The XMLNS prefix is currently assigned to: "xmlns"
     */
    public static final String XMLNS_PREFIX = "xmlns";

    /**
     * The XS prefix is currently assigned to: "xs"
     */
    public static final String XS_PREFIX = "xs";

    /**
     * The XSI prefix is currently assigned to: "xsi"
     */
    public static final String XSI_PREFIX = "xsi";

    /**
     * The ISO19115 prefix is currently assigned to: "iso19115"
     */
    public static final String ISO19115_PREFIX = "iso19115";

    /**
     * The ISO19115BRIEF prefix is currently assigned to: "iso19115brief"
     */
    public static final String ISO19115BRIEF_PREFIX = "iso19115brief";

    /**
     * The ISO19119 prefix is currently assigned to: "iso19119"
     */
    public static final String ISO19119_PREFIX = "iso19119";

    /**
     * The DC prefix is currently assigned to: "dc"
     */
    public static final String DC_PREFIX = "dc";

    /**
     * The SMXML prefix is currently assigned to: "smXML"
     */
    public static final String SMXML_PREFIX = "smXML";

    /**
     * The ISOAP10GMD_PREFIX is currrently assigned to: "gmd"
     */
    public static final String ISOAP10GMD_PREFIX = "gmd";

    /**
     * The ISOAP10GCO_PREFIX is currrently assigned to: "gco"
     */
    public static final String ISOAP10GCO_PREFIX = "gco";

    /**
     * The APISO_PREFIX is currrently assigned to: "apiso"
     */
    public static final String APISO_PREFIX = "apiso";

    private static NamespaceContext nsContext = null;

    /**
     * @param namespace
     * @return Returns the uri for the passed namespace.
     */
    public static URI buildNSURI( String namespace ) {
        URI uri = null;
        try {
            uri = new URI( namespace );
        } catch ( URISyntaxException e ) {
            BootLogger.logError( "Invalid common namespace URI '" + namespace + "':" + e.getMessage(), e );
        }
        return uri;
    }

    /**
     * Returns the <code>NamespaceContext</code> for common namespaces known be deegree.
     * 
     * @return the NamespaceContext for all common namespaces
     */
    public static synchronized NamespaceContext getNamespaceContext() {
        if ( nsContext == null ) {
            nsContext = new NamespaceContext();
            nsContext.addNamespace( GML_PREFIX, GMLNS );
            nsContext.addNamespace( OGC_PREFIX, OGCNS );
            nsContext.addNamespace( XLINK_PREFIX, XLNNS );
            nsContext.addNamespace( XS_PREFIX, XSNS );
            nsContext.addNamespace( XSI_PREFIX, XSINS );
            nsContext.addNamespace( ISO19115_PREFIX, ISO19115NS );
            nsContext.addNamespace( ISO19115BRIEF_PREFIX, ISO19115BRIEFNS );
            nsContext.addNamespace( ISO19119_PREFIX, ISO19119NS );
            nsContext.addNamespace( DC_PREFIX, DCNS );
            nsContext.addNamespace( SMXML_PREFIX, SMXMLNS );
            nsContext.addNamespace( ISOAP10GMD_PREFIX, ISOAP10GMDNS );
            nsContext.addNamespace( ISOAP10GCO_PREFIX, ISOAP10GCONS );
            nsContext.addNamespace( APISO_PREFIX, APISO );

        }
        return nsContext;
    }

    @Override
    public String toString() {
        return nsContext.toString();
    }

}