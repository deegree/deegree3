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
package org.deegree.ogcbase;

import java.net.URI;
import java.net.URISyntaxException;

import org.deegree.framework.util.BootLogger;
import org.deegree.framework.xml.NamespaceContext;

/**
 * Definitions for OGC related namespace bindings.
 * <p>
 * NOTE: Don't put project specific bindings here -- subclass it and override {@link #getNamespaceContext()} instead.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:tfriebe@sf.net">Torsten Friebe</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

// FIXME change ows namespace uri to http://www.opengeospatial.net/ows
// output bound to the current ows namesspace uri (http://www.opengis.net/ows)
// is not valid against up to date ogc schemas!!!!
public class CommonNamespaces {

    // namespaces
    /**
     * The XMLNS namespace is currently bound to: "http://www.w3.org/2000/xmlns/"
     */
    public static final URI XMLNS = buildNSURI( "http://www.w3.org/2000/xmlns/" );

    /**
     * The SMLNS namespace is currently bound to: "http://www.opengis.net/sensorML"
     */
    public static final URI SMLNS = buildNSURI( "http://www.opengis.net/sensorML" );

    /**
     * The SOSNS namespace is currently bound to: "http://www.opengis.net/sos"
     */
    public static final URI SOSNS = buildNSURI( "http://www.opengis.net/sos" );

    /**
     * The CSWNS namespace is currently bound to: "http://www.opengis.net/cat/csw"
     */
    public static final URI CSWNS = buildNSURI( "http://www.opengis.net/cat/csw" );

    /**
     * The CSWNS 2.0.2 namespace is currently bound to: "http://www.opengis.net/cat/csw/2.0.2"
     */
    public static final URI CSW202NS = buildNSURI( "http://www.opengis.net/cat/csw/2.0.2" );

    /**
     * The GMLNS namespace is currently bound to: "http://www.opengis.net/gml"
     */
    public static final URI GMLNS = buildNSURI( "http://www.opengis.net/gml" );

    /**
     * The GML3_2_NS namespace is currently bound to: "http://www.opengis.net/gml/3.2"
     */
    public static final URI GML3_2_NS = buildNSURI( "http://www.opengis.net/gml/3.2" );

    /**
     * The CITYGMLNS namespace is currently bound to: "http://www.citygml.org/citygml/1/0/0".
     */
    public static final URI CITYGMLNS = buildNSURI( "http://www.citygml.org/citygml/1/0/0" );

    /**
     * The WFSNS namespace is currently bound to: "http://www.opengis.net/wfs"
     */
    public static final URI WFSNS = buildNSURI( "http://www.opengis.net/wfs" );

    /**
     * The WFSGNS namespace is currently bound to: "http://www.opengis.net/wfs-g"
     */
    public static final URI WFSGNS = buildNSURI( "http://www.opengis.net/wfs-g" );

    /**
     * The WCSNS namespace is currently bound to: "http://www.opengis.net/wcs"
     */
    public static final URI WCSNS = buildNSURI( "http://www.opengis.net/wcs" );

    /**
     * The WMSNS namespace is currently bound to: "http://www.opengis.net/wms"
     */
    public static final URI WMSNS = buildNSURI( "http://www.opengis.net/wms" );

    /**
     * The WMPSNS namespace is currently bound to: "http://www.opengis.net/wmps"
     */
    public static final URI WMPSNS = buildNSURI( "http://www.opengis.net/wmps" );

    /**
     * The WPVSNS namespace is currently bound to: "http://www.opengis.net/wpvs"
     */
    public static final URI WPVSNS = buildNSURI( "http://www.opengis.net/wpvs" );

    /**
     * The WPSNS namespace is currently bound to: "http://www.opengeospatial.net/wps"
     */
    public static final URI WPSNS = buildNSURI( "http://www.opengeospatial.net/wps" );

    /**
     * The OGCNS namespace is currently bound to: "http://www.opengis.net/ogc"
     */
    public static final URI OGCNS = buildNSURI( "http://www.opengis.net/ogc" );

    /**
     * The OWSNS namespace is currently bound to: "http://www.opengis.net/ows"
     */
    public static final URI OWSNS = buildNSURI( "http://www.opengis.net/ows" );

    /**
     * The SLDNS namespace is currently bound to: "http://www.opengis.net/sld"
     */
    public static final URI SLDNS = buildNSURI( "http://www.opengis.net/sld" );

    /**
     * The SENS namespace is currently bound to: "http://www.opengis.net/se"
     */
    public static final URI SENS = buildNSURI( "http://www.opengis.net/se" );

    /**
     * The OMNS namespace is currently bound to: "http://www.opengis.net/om"
     */
    public static final URI OMNS = buildNSURI( "http://www.opengis.net/om" );

    /**
     * The XLNNS namespace is currently bound to: "http://www.w3.org/1999/xlink"
     */
    public static final URI XLNNS = buildNSURI( "http://www.w3.org/1999/xlink" );

    /**
     * The CNTXTNS namespace is currently bound to: "http://www.opengis.net/context"
     */
    public static final URI CNTXTNS = buildNSURI( "http://www.opengis.net/context" );

    /**
     * The DGCNTXTNS namespace is currently bound to: "http://www.deegree.org/context"
     */
    public static final URI DGCNTXTNS = buildNSURI( "http://www.deegree.org/context" );

    /**
     * The DEEGREEWFS namespace is currently bound to: "http://www.deegree.org/wfs"
     */
    public static final URI DEEGREEWFS = buildNSURI( "http://www.deegree.org/wfs" );

    /**
     * The DEEGREEWMS namespace is currently bound to: "http://www.deegree.org/wms"
     */
    public static final URI DEEGREEWMS = buildNSURI( "http://www.deegree.org/wms" );

    /**
     * The DEEGREEWCS namespace is currently bound to: "http://www.deegree.org/wcs"
     */
    public static final URI DEEGREEWCS = buildNSURI( "http://www.deegree.org/wcs" );

    /**
     * The DEEGREECSW namespace is currently bound to: "http://www.deegree.org/csw"
     */
    public static final URI DEEGREECSW = buildNSURI( "http://www.deegree.org/csw" );

    /**
     * The DEEGREESOS namespace is currently bound to: "http://www.deegree.org/sos"
     */
    public static final URI DEEGREESOS = buildNSURI( "http://www.deegree.org/sos" );

    /**
     * The DEEGREEWAS namespace is currently bound to: "http://www.deegree.org/was"
     */
    public static final URI DEEGREEWAS = buildNSURI( "http://www.deegree.org/was" );

    /**
     * The DEEGREEWSS namespace is currently bound to: "http://www.deegree.org/wss"
     */
    public static final URI DEEGREEWSS = buildNSURI( "http://www.deegree.org/wss" );

    /**
     * The DEEGREEWMPS namespace is currently bound to: "http://www.deegree.org/wmps"
     */
    public static final URI DEEGREEWMPS = buildNSURI( "http://www.deegree.org/wmps" );

    /**
     * The DEEGREEWPVS namespace is currently bound to: "http://www.deegree.org/wpvs"
     */
    public static final URI DEEGREEWPVS = buildNSURI( "http://www.deegree.org/wpvs" );

    /**
     * The DEEGREEOGC namespace is currently bound to: "http://www.deegree.org/ogc"
     */
    public static final URI DEEGREEOGC = buildNSURI( "http://www.deegree.org/ogc" );

    /**
     * The DEEGREEWPS namespace is currently bound to: "http://www.deegree.org/wps"
     */
    public static final URI DEEGREEWPS = buildNSURI( "http://www.deegree.org/wps" );

    /**
     * The DEEGREEWCTS namespace is currently bound to: "http://www.deegree.org/wcts"
     */
    public static final URI DEEGREEWCTS = buildNSURI( "http://www.deegree.org/wcts" );

    /**
     * The DGJDBC namespace is currently bound to: "http://www.deegree.org/jdbc"
     */
    public static final URI DGJDBC = buildNSURI( "http://www.deegree.org/jdbc" );

    /**
     * The DGSECNS namespace is currently bound to: "http://www.deegree.org/security"
     */
    public static final URI DGSECNS = buildNSURI( "http://www.deegree.org/security" );

    /**
     * The ISO19112NS namespace is currently bound to: "http://www.opengis.net/iso19112"
     */
    public static final URI ISO19112NS = buildNSURI( "http://www.opengis.net/iso19112" );

    /**
     * The ISO19115NS namespace is currently bound to: "http://schemas.opengis.net/iso19115full"
     */
    public static final URI ISO19115NS = buildNSURI( "http://schemas.opengis.net/iso19115full" );

    /**
     * The ISO19115BRIEFNS namespace is currently bound to: "http://schemas.opengis.net/iso19115brief"
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
     * The GDINRW_WSS namespace is currently bound to: "http://www.gdi-nrw.org/wss"
     */
    public static final URI GDINRW_WSS = buildNSURI( "http://www.gdi-nrw.org/wss" );

    /**
     * The GDINRW_WAS namespace is currently bound to: "http://www.gdi-nrw.org/was"
     */
    public static final URI GDINRW_WAS = buildNSURI( "http://www.gdi-nrw.org/was" );

    /**
     * The WSSSESSIONNS namespace is currently bound to: "http://www.gdi-nrw.org/session"
     */
    public static final URI WSSSESSIONNS = buildNSURI( "http://www.gdi-nrw.org/session" );

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
     * THE CTL namespace is currently bound to: "http://www.occamlab.com/ctl".
     */
    public static final URI CTLNS = buildNSURI( "http://www.occamlab.com/ctl" );

    /**
     * The XSL namespace is currently bound to: "http://www.w3.org/1999/XSL/Transform".
     */
    public static final URI XSLNS = buildNSURI( "http://www.w3.org/1999/XSL/Transform" );

    /**
     * The PARSERS namespace is currently bound to: "http://www.occamlab.com/te/parsers".
     */
    public static final URI PARSERSNS = buildNSURI( "http://www.occamlab.com/te/parsers" );

    /**
     * The GDINRW_AUTH namespace is currently bound to: "http://www.gdi-nrw.org/authentication"
     */
    public static final URI GDINRW_AUTH = buildNSURI( "http://www.gdi-nrw.org/authentication" );

    /**
     * The GDINRW_SESSION namespace is currently bound to: "http://www.gdi-nrw.org/session"
     */
    public static final URI GDINRW_SESSION = buildNSURI( "http://www.gdi-nrw.org/session" );

    /**
     * The WRS namespace is currently bound to: "http://www.opengis.net/cat/wrs"
     */
    public static final URI WRS_EBRIMNS = buildNSURI( "http://www.opengis.net/cat/wrs" );

    /**
     * The OASIS namespace is currently bound to: "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
     */
    public static final URI OASIS_EBRIMNS = buildNSURI( "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0" );

    /**
     * The W3SOAP_ENVELOPE namespace is currently bound to: "http://www.w3.org/2003/05/soap-envelope"
     */
    public static final URI W3SOAP_ENVELOPE = buildNSURI( "http://www.w3.org/2003/05/soap-envelope" );

    /**
     * The W3SOAP_ENVELOPE_1_1namespace is currently bound to: http://schemas.xmlsoap.org/soap/envelope/
     */
    public static final URI W3SOAP_ENVELOPE_1_1 = buildNSURI( "http://schemas.xmlsoap.org/soap/envelope/" );

    /**
     * The XPLANNS namespace is currently bound to: "http://www.xplanung.de/xplangml"
     */
    public static final URI XPLANNS = buildNSURI( "http://www.xplanung.de/xplangml" );

    /**
     * The ISOAP10GMDNS namespace is currently bound to: "http://www.isotc211.org/2005/gmd"
     */
    public static final URI ISOAP10GMDNS = buildNSURI( "http://www.isotc211.org/2005/gmd" );

    /**
     * The ISOAP10SRVNS namespace is currently bound to: "http://www.isotc211.org/2005/srv"
     */
    public static final URI ISOAP10SRVNS = buildNSURI( "http://www.isotc211.org/2005/srv" );

    /**
     * The CRSNS namespace --used for the crs package-- is currently bound to: "http://www.deegree.org/crs"
     */
    public static final URI CRSNS = buildNSURI( "http://www.deegree.org/crs" );

    /**
     * The ISOAP10GCONS namespace is currently bound to: "http://www.isotc211.org/2005/gco"
     */
    public static final URI ISOAP10GCONS = buildNSURI( "http://www.isotc211.org/2005/gco" );

    /**
     * The OWSNS_1_1_0 namespace is currently bound to: "http://www.opengis.net/ows/1.1"
     */
    public static final URI OWSNS_1_1_0 = buildNSURI( "http://www.opengis.net/ows/1.1" );

    /**
     * The WCTSNS namespace is currently bound to: "http://www.opengis.net/wcts/0.0"
     */
    public static final URI WCTSNS = buildNSURI( "http://www.opengis.net/wcts/0.0" );

    /**
     * The WCSNS_1_2_0 namespace is currently bound to: "http://www.opengis.net/wcs/1.2"
     */
    public static final URI WCSNS_1_2_0 = buildNSURI( "http://www.opengis.net/wcs/1.2" );

    /**
     * The APISO namespace is currently bound to: "http://www.opengis.net/cat/csw/apiso/1.0"
     */
    public static final URI APISO = buildNSURI( "http://www.opengis.net/cat/csw/apiso/1.0" );

    // prefixes
    /**
     * The SML prefix is currently assigned to: "sml"
     */
    public static final String SML_PREFIX = "sml";

    /**
     * The SOS prefix is currently assigned to: "sos"
     */
    public static final String SOS_PREFIX = "sos";

    /**
     * The CSW prefix is currently assigned to: "csw"
     */
    public static final String CSW_PREFIX = "csw";

    /**
     * The CSW 2.0.2 prefix is currently assigned to: "csw202"
     */
    public static final String CSW202_PREFIX = "csw202";

    /**
     * The GML prefix is currently assigned to: "gml"
     */
    public static final String GML_PREFIX = "gml";

    /**
     * The GML3_2 prefix is currently assigned to: "gml3_2"
     */
    public static final String GML3_2_PREFIX = "gml3_2";

    /**
     * The CITYGML prefix is currently assigned to: "citygml".
     */
    public static final String CITYGML_PREFIX = "citygml";

    /**
     * The WFS prefix is currently assigned to: "wfs"
     */
    public static final String WFS_PREFIX = "wfs";

    /**
     * The WFSG prefix is currently assigned to: "wfsg"
     */
    public static final String WFSG_PREFIX = "wfsg";

    /**
     * The WCS prefix is currently assigned to: "wcs"
     */
    public static final String WCS_PREFIX = "wcs";

    /**
     * The WMS prefix is currently assigned to: "wms"
     */
    public static final String WMS_PREFIX = "wms";

    /**
     * The WPVS prefix is currently assigned to: "wpvs"
     */
    public static final String WPVS_PREFIX = "wpvs";

    /**
     * The WMPS prefix is currently assigned to: "wmps"
     */
    public static final String WMPS_PREFIX = "wmps";

    /**
     * The WPS prefix is currently assigned to: "wps"
     */
    public static final String WPS_PREFIX = "wps";

    /**
     * The OGC prefix is currently assigned to: "ogc"
     */
    public static final String OGC_PREFIX = "ogc";

    /**
     * The OWS prefix is currently assigned to: "ows"
     */
    public static final String OWS_PREFIX = "ows";

    /**
     * The SLD prefix is currently assigned to: "sld"
     */
    public static final String SLD_PREFIX = "sld";

    /**
     * The SE prefix is currently assigned to: "se".
     */
    public static final String SE_PREFIX = "se";

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
     * The CNTXT prefix is currently assigned to: "cntxt"
     */
    public static final String CNTXT_PREFIX = "cntxt";

    /**
     * The DGCNTXT prefix is currently assigned to: "dgcntxt"
     */
    public static final String DGCNTXT_PREFIX = "dgcntxt";

    /**
     * The DEEGREEWFS prefix is currently assigned to: "deegreewfs"
     */
    public static final String DEEGREEWFS_PREFIX = "deegreewfs";

    /**
     * The DEEGREEWMS prefix is currently assigned to: "deegreewms"
     */
    public static final String DEEGREEWMS_PREFIX = "deegreewms";

    /**
     * The DEEGREEWCS prefix is currently assigned to: "deegreewcs"
     */
    public static final String DEEGREEWCS_PREFIX = "deegreewcs";

    /**
     * The DEEGREECSW prefix is currently assigned to: "deegreecsw"
     */
    public static final String DEEGREECSW_PREFIX = "deegreecsw";

    /**
     * The DEEGREESOS prefix is currently assigned to: "deegreesos"
     */
    public static final String DEEGREESOS_PREFIX = "deegreesos";

    /**
     * The DEEGREEWAS prefix is currently assigned to: "deegreewas"
     */
    public static final String DEEGREEWAS_PREFIX = "deegreewas";

    /**
     * The DEEGREEWSS prefix is currently assigned to: "deegreewss"
     */
    public static final String DEEGREEWSS_PREFIX = "deegreewss";

    /**
     * The DEEGREEWMPS prefix is currently assigned to: "deegreewmps"
     */
    public static final String DEEGREEWMPS_PREFIX = "deegreewmps";

    /**
     * The DEEGREEWPS prefix is currently assigned to: "deegreewps"
     */
    public static final String DEEGREEWPS_PREFIX = "deegreewps";

    /**
     * The DEEGREEWPVS prefix is currently assigned to: "deegreewpvs"
     */
    public static final String DEEGREEWPVS_PREFIX = "deegreewpvs";

    /**
     * The DEEGREEOGC prefix is currently assigned to: "deegreeogc"
     */
    public static final String DEEGREEOGC_PREFIX = "deegreeogc";

    /**
     * The DEEGREEWCTS_PREFIX prefix is currently assigned to: "d_wcts"
     */
    public static final String DEEGREEWCTS_PREFIX = "d_wcts";

    /**
     * The DGJDBC prefix is currently assigned to: "dgjdbc"
     */
    public static final String DGJDBC_PREFIX = "dgjdbc";

    /**
     * The DGSEC prefix is currently assigned to: "dgsec"
     */
    public static final String DGSEC_PREFIX = "dgsec";

    /**
     * The ISO19112 prefix is currently assigned to: "iso19112"
     */
    public static final String ISO19112_PREFIX = "iso19112";

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
     * The GDINRWWSS prefix is currently assigned to: "wss"
     */
    public static final String GDINRWWSS_PREFIX = "wss";

    /**
     * The GDINRWWAS prefix is currently assigned to: "was"
     */
    public static final String GDINRWWAS_PREFIX = "was";

    /**
     * The WSSSESSION prefix is currently assigned to: "wsssession"
     */
    public static final String WSSSESSION_PREFIX = "wsssession";

    /**
     * The OMNS prefix is currently assigned to: "om"
     */
    public static final String OMNS_PREFIX = "om";

    /**
     * The SMXML prefix is currently assigned to: "smXML"
     */
    public static final String SMXML_PREFIX = "smXML";

    /**
     * The CTL prefix is currently assigned to: "ctl"
     */
    public static final String CTL_PREFIX = "ctl";

    /**
     * The CTL prefix is currently assigned to: "xsl"
     */
    public static final String XSL_PREFIX = "xsl";

    /**
     * The CTL prefix is currently assigned to: "parsers"
     */
    public static final String PARSERS_PREFIX = "parsers";

    /**
     * The GDINRW_AUTH prefix is currently assigned to: "authn"
     */
    public static final String GDINRW_AUTH_PREFIX = "authn";

    /**
     * The GDINRW_SESSION prefix is currently assigned to: "sessn"
     */
    public static final String GDINRW_SESSION_PREFIX = "sessn";

    /**
     * The GDINRW_SESSION prefix is currently assigned to: "wrs"
     */
    public static final String WRS_EBRIM_PREFIX = "wrs";

    /**
     * The OASIS_EBRIM prefix is currently assigned to: "rim"
     */
    public static final String OASIS_EBRIM_PREFIX = "rim";

    /**
     * The W3SOAP_ENVELOPE prefix is currently assigned to: "soap"
     */
    public static final String W3SOAP_ENVELOPE_PREFIX = "soap";

    /**
     * The W3SOAP_1_1 prefix is currently assigned to: "SOAP-ENV"
     */
    public static final String W3SOAP_1_1_PREFIX = "SOAP-ENV";

    /**
     * The XPLAN_PREFIX is currrently assigned to: "xplan"
     */
    public static final String XPLAN_PREFIX = "xplan";

    /**
     * The ISOAP10GMD_PREFIX is currrently assigned to: "gmd"
     */
    public static final String ISOAP10GMD_PREFIX = "gmd";

    /**
     * The ISOAP10SRV_PREFIX is currrently assigned to: "srv"
     */
    public static final String ISOAP10SRV_PREFIX = "srv";

    /**
     * The ISOAP10GCO_PREFIX is currrently assigned to: "gco"
     */
    public static final String ISOAP10GCO_PREFIX = "gco";

    /**
     * The CRS_PREFIX is currrently assigned to: "crs"
     */
    public static final String CRS_PREFIX = "crs";

    /**
     * The OWS_1_1_0PREFIX is currrently assigned to: "ows_1_1_0"
     */
    public static final String OWS_1_1_0PREFIX = "ows_1_1_0";

    /**
     * The WCTS_PREFIX is currrently assigned to: "wcts"
     */
    public static final String WCTS_PREFIX = "wcts";

    /**
     * The WCS_1_2_0_PREFIX is currrently assigned to: "wcs_1_2_0"
     */
    public static final String WCS_1_2_0_PREFIX = "wcs_1_2_0";

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
            nsContext.addNamespace( APISO_PREFIX, APISO );
            nsContext.addNamespace( CITYGML_PREFIX, CITYGMLNS );
            nsContext.addNamespace( CNTXT_PREFIX, CNTXTNS );
            nsContext.addNamespace( CRS_PREFIX, CRSNS );
            nsContext.addNamespace( CSW202_PREFIX, CSW202NS );
            nsContext.addNamespace( CSW_PREFIX, CSWNS );
            nsContext.addNamespace( CTL_PREFIX, CTLNS );
            nsContext.addNamespace( DC_PREFIX, DCNS );
            nsContext.addNamespace( DEEGREECSW_PREFIX, DEEGREECSW );
            nsContext.addNamespace( DEEGREESOS_PREFIX, DEEGREESOS );
            nsContext.addNamespace( DEEGREEWAS_PREFIX, DEEGREEWAS );
            nsContext.addNamespace( DEEGREEWCS_PREFIX, DEEGREEWCS );
            nsContext.addNamespace( DEEGREEWCTS_PREFIX, DEEGREEWCTS );
            nsContext.addNamespace( DEEGREEWFS_PREFIX, DEEGREEWFS );
            nsContext.addNamespace( DEEGREEWMPS_PREFIX, DEEGREEWMPS );
            nsContext.addNamespace( DEEGREEWMS_PREFIX, DEEGREEWMS );
            nsContext.addNamespace( DEEGREEWPS_PREFIX, DEEGREEWPS );
            nsContext.addNamespace( DEEGREEWPVS_PREFIX, DEEGREEWPVS );
            nsContext.addNamespace( DEEGREEOGC_PREFIX, DEEGREEOGC );
            nsContext.addNamespace( DEEGREEWSS_PREFIX, DEEGREEWSS );
            nsContext.addNamespace( DGCNTXT_PREFIX, DGCNTXTNS );
            nsContext.addNamespace( DGJDBC_PREFIX, DGJDBC );
            nsContext.addNamespace( DGSEC_PREFIX, DGSECNS );
            nsContext.addNamespace( GDINRWWAS_PREFIX, GDINRW_WAS );
            nsContext.addNamespace( GDINRWWSS_PREFIX, GDINRW_WSS );
            nsContext.addNamespace( GDINRW_AUTH_PREFIX, GDINRW_AUTH );
            nsContext.addNamespace( GML_PREFIX, GMLNS );
            nsContext.addNamespace( GML3_2_PREFIX, GML3_2_NS );
            nsContext.addNamespace( ISO19112_PREFIX, ISO19112NS );
            nsContext.addNamespace( ISO19115BRIEF_PREFIX, ISO19115BRIEFNS );
            nsContext.addNamespace( ISO19115_PREFIX, ISO19115NS );
            nsContext.addNamespace( ISO19119_PREFIX, ISO19119NS );
            nsContext.addNamespace( ISOAP10GCO_PREFIX, ISOAP10GCONS );
            nsContext.addNamespace( ISOAP10GMD_PREFIX, ISOAP10GMDNS );
            nsContext.addNamespace( ISOAP10SRV_PREFIX, ISOAP10SRVNS );
            nsContext.addNamespace( OASIS_EBRIM_PREFIX, OASIS_EBRIMNS );
            nsContext.addNamespace( OGC_PREFIX, OGCNS );
            nsContext.addNamespace( OMNS_PREFIX, OMNS );
            nsContext.addNamespace( OWS_1_1_0PREFIX, OWSNS_1_1_0 );
            nsContext.addNamespace( OWS_PREFIX, OWSNS );
            nsContext.addNamespace( PARSERS_PREFIX, PARSERSNS );
            nsContext.addNamespace( SE_PREFIX, SENS );
            nsContext.addNamespace( SLD_PREFIX, SLDNS );
            nsContext.addNamespace( SML_PREFIX, SMLNS );
            nsContext.addNamespace( SMXML_PREFIX, SMXMLNS );
            nsContext.addNamespace( SOS_PREFIX, SOSNS );
            nsContext.addNamespace( W3SOAP_1_1_PREFIX, W3SOAP_ENVELOPE_1_1 );
            nsContext.addNamespace( W3SOAP_ENVELOPE_PREFIX, W3SOAP_ENVELOPE );
            nsContext.addNamespace( WCS_1_2_0_PREFIX, WCSNS_1_2_0 );
            nsContext.addNamespace( WCS_PREFIX, WCSNS );
            nsContext.addNamespace( WCTS_PREFIX, WCTSNS );
            nsContext.addNamespace( WFSG_PREFIX, WFSGNS );
            nsContext.addNamespace( WFS_PREFIX, WFSNS );
            nsContext.addNamespace( WMS_PREFIX, WMSNS );
            nsContext.addNamespace( WPS_PREFIX, WPSNS );
            nsContext.addNamespace( WPVS_PREFIX, WMPSNS );
            nsContext.addNamespace( WPVS_PREFIX, WPVSNS );
            nsContext.addNamespace( WRS_EBRIM_PREFIX, WRS_EBRIMNS );
            nsContext.addNamespace( WSSSESSION_PREFIX, WSSSESSIONNS );
            nsContext.addNamespace( XLINK_PREFIX, XLNNS );
            nsContext.addNamespace( XPLAN_PREFIX, XPLANNS );
            nsContext.addNamespace( XSI_PREFIX, XSINS );
            nsContext.addNamespace( XSL_PREFIX, XSLNS );
            nsContext.addNamespace( XS_PREFIX, XSNS );
        }
        return nsContext;
    }

    @Override
    public String toString() {
        return nsContext.getURI( WPS_PREFIX ).toString();
    }

}
