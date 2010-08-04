// $HeadURL$
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
package org.deegree.commons.xml;

/**
 * Definitions for OGC related namespace bindings.
 * <p>
 * NOTE: Don't put project specific bindings here -- subclass it and override {@link #getNamespaceContext()} instead.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CommonNamespaces {

    // namespaces
    /**
     * The XMLNS namespace is currently bound to: "http://www.w3.org/2000/xmlns/"
     */
    public static final String XMLNS = "http://www.w3.org/2000/xmlns/";

    /**
     * The GMLNS namespace is currently bound to: "http://www.opengis.net/gml"
     */
    public static final String GMLNS = "http://www.opengis.net/gml";

    /**
     * The OGCNS namespace is currently bound to: "http://www.opengis.net/ogc"
     */
    public static final String OGCNS = "http://www.opengis.net/ogc";
    
    /**
     * The OWS_11_NS namespace is currently bound to: "http://www.opengis.net/ows/1.1"
     */
    public static final String OWS_11_NS = "http://www.opengis.net/ows/1.1";

    /**
     * The XLNNS namespace is currently bound to: "http://www.w3.org/1999/xlink"
     */
    public static final String XLNNS = "http://www.w3.org/1999/xlink";

    /**
     * The ISO19115NS namespace is currently bound to: "http://schemas.opengis.net/iso19115full"
     */
    public static final String ISO19115NS = "http://schemas.opengis.net/iso19115full";

    /**
     * The ISO19115BRIEFNS namespace is currently bound to: "http://schemas.opengis.net/iso19115brief"
     */
    public static final String ISO19115BRIEFNS = "http://schemas.opengis.net/iso19115brief";

    /**
     * The ISO19119NS namespace is currently bound to: "http://schemas.opengis.net/iso19119"
     */
    public static final String ISO19119NS = "http://schemas.opengis.net/iso19119";

    /**
     * The DCNS namespace is currently bound to: "http://purl.org/dc/elements/1.1/"
     */
    public static final String DCNS = "http://purl.org/dc/elements/1.1/";

    /**
     * The XSNS namespace is currently bound to: "http://www.w3.org/2001/XMLSchema"
     */
    public static final String XSNS = "http://www.w3.org/2001/XMLSchema";

    /**
     * The XSINS namespace is currently bound to: "http://www.w3.org/2001/XMLSchema-instance"
     */
    public static final String XSINS = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * The SMXMLNS namespace is currently bound to: "http://metadata.dgiwg.org/smXML"
     */
    public static final String SMXMLNS = "http://metadata.dgiwg.org/smXML";

    /**
     * The ISOAP10GMDNS namespace is currently bound to: "http://www.isotc211.org/2005/gmd"
     */
    public static final String ISOAP10GMDNS = "http://www.isotc211.org/2005/gmd";

    /**
     * The ISOAP10GCONS namespace is currently bound to: "http://www.isotc211.org/2005/gco"
     */
    public static final String ISOAP10GCONS = "http://www.isotc211.org/2005/gco";

    /**
     * The APISO namespace is currently bound to: "http://www.opengis.net/cat/csw/apiso/1.0"
     */
    public static final String APISO = "http://www.opengis.net/cat/csw/apiso/1.0";

    /**
     * The GML3_2_NS namespace is currently bound to: "http://www.opengis.net/gml/3.2"
     */
    public static final String GML3_2_NS = "http://www.opengis.net/gml/3.2";

    /**
     * The CRSNS namespace --used for the crs package-- is currently bound to: "http://www.deegree.org/crs"
     */
    public static final String CRSNS = "http://www.deegree.org/crs";

    /**
     * The SE namespace --used for the 2d rendering-- is currently bound to: "http://www.opengis.net/se"
     */
    public static final String SENS = "http://www.opengis.net/se";

    /**
     * The SLD namespace is currently bound to "http://www.opengis.net/sld"
     */
    public static final String SLDNS = "http://www.opengis.net/sld";

    /**
     * The WMS namespace is currently bound to: "http://www.opengis.net/wms"
     */
    public static final String WMSNS = "http://www.opengis.net/wms";

    // prefixes
    /**
     * The GML3_2 prefix is currently assigned to: "gml3_2"
     */
    public static final String GML3_2_PREFIX = "gml3_2";

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
     * The ISOAP10GMD_PREFIX is currently assigned to: "gmd"
     */
    public static final String ISOAP10GMD_PREFIX = "gmd";

    /**
     * The ISOAP10GCO_PREFIX is currently assigned to: "gco"
     */
    public static final String ISOAP10GCO_PREFIX = "gco";

    /**
     * The APISO_PREFIX is currently assigned to: "apiso"
     */
    public static final String APISO_PREFIX = "apiso";

    /**
     * The CRS_PREFIX is currently assigned to: "crs"
     */
    public static final String CRS_PREFIX = "crs";

    /**
     * The SE_PREFIX is currently assigned to: "se"
     */
    public static final String SE_PREFIX = "se";

    /**
     * The SLD_PREFIX is currently assigned to: "sld"
     */
    public static final String SLD_PREFIX = "sld";

    /**
     * The WMS_PREFIX is currently assigned to: "wms"
     */
    public static final String WMS_PREFIX = "wms";

    private static NamespaceContext nsContext = null;

    /**
     * Returns the <code>NamespaceContext</code> for common namespaces known be deegree.
     * 
     * @return the NamespaceContext for all common namespaces
     */
    public static synchronized NamespaceContext getNamespaceContext() {
        if ( nsContext == null ) {
            nsContext = new NamespaceContext();
            nsContext.addNamespace( GML_PREFIX, GMLNS );
            nsContext.addNamespace( GML3_2_PREFIX, GML3_2_NS );
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
            nsContext.addNamespace( CRS_PREFIX, CRSNS );
            nsContext.addNamespace( SE_PREFIX, SENS );
            nsContext.addNamespace( WMS_PREFIX, WMSNS );

        }
        return nsContext;
    }

    @Override
    public String toString() {
        return nsContext.toString();
    }
}
