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
package org.deegree.ogcwebservices.wcs.describecoverage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.CodeList;
import org.deegree.datatypes.time.TimeSequence;
import org.deegree.datatypes.values.Values;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.GMLDocument;
import org.deegree.ogcbase.InvalidGMLException;
import org.deegree.ogcbase.OGCDocument;
import org.deegree.ogcbase.OGCException;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.MetadataLink;
import org.deegree.ogcwebservices.MetadataType;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.SupportedFormats;
import org.deegree.ogcwebservices.SupportedSRSs;
import org.deegree.ogcwebservices.wcs.InterpolationMethod;
import org.deegree.ogcwebservices.wcs.SupportedInterpolations;
import org.deegree.ogcwebservices.wcs.WCSException;
import org.deegree.ogcwebservices.wcs.configuration.Extension;
import org.deegree.ogcwebservices.wcs.configuration.ExtensionDocument;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <ul>
 * <li> usage of srsName from gml:Envelope is not supoorted yet. deegree Envelope doesn't uses CRSs
 * <li> gml:Grid and gml:Polygon is not yet supported by the deegree WCS
 * </ul>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CoverageDescriptionDocument extends OGCDocument {

    public static final String XML_TEMPLATE = "CoverageDescriptionTemplate.xml";

    private static URI WCSNS = CommonNamespaces.WCSNS;

    private static URI GMLNS = CommonNamespaces.GMLNS;

    private static URI DGRNS = CommonNamespaces.DEEGREEWCS;

    /**
     * @throws IOException
     * @throws SAXException
     * @see "org.deegree.framework.xml.XMLFragment#createEmptyDocument()"
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = CoverageDescriptionDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     * returns the version of the CoverageDescription
     *
     * @return the version of the CoverageDescription
     * @throws InvalidCoverageDescriptionExcpetion
     */
    public String getVersion()
                            throws InvalidCoverageDescriptionExcpetion {
        try {
            return XMLTools.getRequiredAttrValue( "version", null, getRootElement() );
        } catch ( XMLParsingException e ) {
            String s = e.getMessage() + "\n" + StringTools.stackTraceToString( e );
            throw new InvalidCoverageDescriptionExcpetion( s );
        }
    }

    /**
     * creates a <tt>CoverageDescription</tt> instance from the DOM document encapsulated by this
     * class
     *
     * @return created <tt>CoverageDescription</tt> instance
     * @throws InvalidCoverageDescriptionExcpetion
     * @throws UnknownCRSException
     */
    public CoverageOffering[] getCoverageOfferings()
                            throws InvalidCoverageDescriptionExcpetion, UnknownCRSException {

        try {
            ElementList el = XMLTools.getChildElements( "CoverageOffering", WCSNS, getRootElement() );
            CoverageOffering[] co = getCoverageOfferings( el );
            return co;
        } catch ( XMLParsingException e ) {
            String s = e.getMessage() + "\n" + StringTools.stackTraceToString( e );
            throw new InvalidCoverageDescriptionExcpetion( s );
        } catch ( WCSException e ) {
            String s = e.getMessage() + "\n" + StringTools.stackTraceToString( e );
            throw new InvalidCoverageDescriptionExcpetion( s );
        } catch ( OGCException e ) {
            String s = e.getMessage() + "\n" + StringTools.stackTraceToString( e );
            throw new InvalidCoverageDescriptionExcpetion( s );
        }

    }

    /**
     * creates an array of <tt>CoverageOffering</tt> objects contained in the enclosing
     * CoverageDescrption element.
     *
     * @param el
     *            list of CoverageOffering elements
     * @return array of <tt>CoverageOffering</tt> objects
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private CoverageOffering[] getCoverageOfferings( ElementList el )
                            throws XMLParsingException, WCSException, OGCException, UnknownCRSException {
        CoverageOffering[] co = new CoverageOffering[el.getLength()];

        for ( int i = 0; i < co.length; i++ ) {
            co[i] = getCoverageOffering( el.item( i ) );
        }

        return co;
    }

    /**
     * @param element
     * @return
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    private CoverageOffering getCoverageOffering( Element element )
                            throws XMLParsingException, WCSException, OGCException, UnknownCRSException {

        String name = XMLTools.getRequiredStringValue( "name", WCSNS, element );
        String label = XMLTools.getStringValue( "label", WCSNS, element, name );
        String desc = XMLTools.getStringValue( "description", WCSNS, element, null );
        Element elem = XMLTools.getChildElement( "metadataLink", WCSNS, element );
        MetadataLink mLink = parseMetadataLink( elem );
        elem = XMLTools.getRequiredChildElement( "lonLatEnvelope", WCSNS, element );
        LonLatEnvelope lonLatEnvelope = parseLonLatEnvelope( elem );
        ElementList el = XMLTools.getChildElements( "keywords", WCSNS, element );
        Keywords[] keywords = parseKeywords( el, WCSNS );
        elem = XMLTools.getRequiredChildElement( "domainSet", WCSNS, element );
        DomainSet domainSet = getDomainSet( elem );
        RangeSet rangeSet = null;
        elem = XMLTools.getRequiredChildElement( "rangeSet", WCSNS, element );
        if ( elem != null ) {
            elem = XMLTools.getRequiredChildElement( "RangeSet", WCSNS, elem );
            rangeSet = getRangeSet( elem );
        }
        elem = XMLTools.getRequiredChildElement( "supportedCRSs", WCSNS, element );
        SupportedSRSs supportedCRSs = getSupportedCRSs( elem );
        elem = XMLTools.getRequiredChildElement( "supportedFormats", WCSNS, element );
        SupportedFormats supportedFormats = getSupportedFomarts( elem );
        elem = XMLTools.getRequiredChildElement( "supportedInterpolations", WCSNS, element );
        SupportedInterpolations supInterpol = getSupportedInterpolations( elem );
        elem = XMLTools.getChildElement( "Extension", DGRNS, element );

        Extension extension = null;
        if ( elem != null ) {
            ExtensionDocument eb = new ExtensionDocument( elem, getSystemId() );
            extension = eb.getExtension();
        }

        return new CoverageOffering( name, label, desc, mLink, lonLatEnvelope, keywords, domainSet, rangeSet,
                                     supportedCRSs, supportedFormats, supInterpol, extension );
    }

    /**
     * creates a <tt>MetadataLink</tt> object from the passed element.
     *
     * @param element
     * @return created <tt>MetadataLink</tt>
     * @throws XMLParsingException
     */
    private MetadataLink parseMetadataLink( Element element )
                            throws XMLParsingException {
        if ( element == null )
            return null;

        try {
            URL reference = new URL( XMLTools.getAttrValue( element, "xlink:href" ) );
            String title = XMLTools.getAttrValue( element, "xlink:title" );
            URI about = new URI( XMLTools.getAttrValue( element, null, "about", null ) );
            String tmp = XMLTools.getAttrValue( element, null, "metadataType", null );
            MetadataType metadataType = new MetadataType( tmp );

            return new MetadataLink( reference, title, about, metadataType );
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "Couldn't parse metadataLink reference\n"
                                           + StringTools.stackTraceToString( e ) );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "Couldn't parse metadataLink about\n" + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * creates a <tt>DomainSet</tt> from the passed element. Not all possible sub elements are
     * supported at the moment by deegree (see class comment)
     *
     * @param element
     * @return
     * @throws XMLParsingException
     * @throws InvalidCoverageDescriptionExcpetion
     * @throws WCSException
     * @throws UnknownCRSException
     */
    private DomainSet getDomainSet( Element element )
                            throws XMLParsingException, InvalidCoverageDescriptionExcpetion, WCSException,
                            UnknownCRSException {
        Element elem = XMLTools.getRequiredChildElement( "spatialDomain", WCSNS, element );
        SpatialDomain sd = getSpatialDomain( elem );
        elem = XMLTools.getChildElement( "temporalDomain", WCSNS, element );
        TimeSequence seq = null;
        if ( elem != null ) {
            try {
                seq = parseTimeSequence( elem, WCSNS );
            } catch ( OGCWebServiceException e ) {
                String s = e.getMessage() + "\n" + StringTools.stackTraceToString( e );
                throw new InvalidCoverageDescriptionExcpetion( s );
            }
        }
        return new DomainSet( sd, seq );
    }

    /**
     * creates a <tt>SpatialDomain</tt> object from the passe element. At the moment deegree
     * doesn't support gml:Grid and gml:Polygon elements for defining a spatial domain of a
     * coverage.
     *
     * @param element
     * @return created <tt>SpatialDomain</tt>
     * @throws XMLParsingException
     * @throws InvalidCoverageDescriptionExcpetion
     * @throws WCSException
     * @throws UnknownCRSException
     */
    private SpatialDomain getSpatialDomain( Element element )
                            throws InvalidCoverageDescriptionExcpetion, WCSException, UnknownCRSException {
        if ( XMLTools.getChildElement( "Grid", GMLNS, element ) != null ) {
            throw new InvalidCoverageDescriptionExcpetion( "GML Grid for SpatialDomain is not "
                                                           + "supported by the deegree WCS yet." );
        }
        if ( XMLTools.getChildElement( "Polygon", GMLNS, element ) != null ) {
            throw new InvalidCoverageDescriptionExcpetion( "GML Polygon for SpatialDomain is not "
                                                           + "supported by the deegree WCS yet." );
        }
        ElementList el = XMLTools.getChildElements( "Envelope", GMLNS, element );
        Envelope[] envelops = getEnvelopes( el );
        return new SpatialDomain( envelops );
    }

    /**
     * creates an array of <tt>Envelope</tt>s from the passed element list
     *
     * @param el
     * @return created array of <tt>Envelope</tt>s
     * @throws XMLParsingException
     * @throws InvalidCoverageDescriptionExcpetion
     * @throws UnknownCRSException
     */
    private Envelope[] getEnvelopes( ElementList el )
                            throws InvalidCoverageDescriptionExcpetion, UnknownCRSException {
        if ( el.getLength() == 0 ) {
            throw new InvalidCoverageDescriptionExcpetion( "at least one envelope must be"
                                                           + "defined in a spatialDomain" );
        }
        Envelope[] envelopes = new Envelope[el.getLength()];
        for ( int i = 0; i < envelopes.length; i++ ) {
            try {
                envelopes[i] = GMLDocument.parseEnvelope( el.item( i ) );
            } catch ( InvalidGMLException e ) {
                String s = e.getMessage() + "\n" + StringTools.stackTraceToString( e );
                throw new InvalidCoverageDescriptionExcpetion( s );
            }
        }
        return envelopes;
    }

    /**
     * creates a <tt>RangeSet</tt> object from the passed element
     *
     * @param element
     * @return created <tt>RangeSet</tt>
     * @throws XMLParsingException
     * @throws WCSException
     * @throws OGCException
     */
    private RangeSet getRangeSet( Element element )
                            throws XMLParsingException, WCSException, OGCException {
        try {
            String name = XMLTools.getRequiredStringValue( "name", WCSNS, element );
            String label = XMLTools.getStringValue( "label", WCSNS, element, name );
            String desc = XMLTools.getStringValue( "description", WCSNS, element, null );
            Element elem = XMLTools.getChildElement( "metadataLink", WCSNS, element );
            MetadataLink mLink = parseMetadataLink( elem );
            String tmp = XMLTools.getAttrValue( element, null, "semantic", null );
            URI semantic = null;
            if ( tmp != null ) {
                semantic = new URI( tmp );
            }

            tmp = XMLTools.getAttrValue( element, null, "refSys", null );
            URI refSys = null;
            if ( tmp != null ) {
                refSys = new URI( tmp );
            }

            String refSysLabel = XMLTools.getAttrValue( element, null, "refSysLabel", null );

            AxisDescription[] axisDescription = null;
            ElementList el = XMLTools.getChildElements( "axisDescription", WCSNS, element );
            if ( elem != null ) {
                elem = XMLTools.getChildElement( "AxisDescription", WCSNS, element );
                axisDescription = getAxisDescriptions( el );
            }
            elem = XMLTools.getChildElement( "nullValues", WCSNS, element );
            Values nullValues = parseValues( elem, WCSNS );
            return new RangeSet( name, label, desc, mLink, semantic, refSys, refSysLabel, nullValues, axisDescription );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "couldn't parse URI from typedLiteral\n"
                                           + StringTools.stackTraceToString( e ) );
        }

    }

    /**
     * creates an array of <tt>AxisDescription</tt>s from the passed element list
     *
     * @param el
     * @return created array of <tt>AxisDescription</tt>s
     * @throws XMLParsingException
     * @throws WCSException
     * @throws OGCException
     */
    private AxisDescription[] getAxisDescriptions( ElementList el )
                            throws XMLParsingException, WCSException, OGCException {
        AxisDescription[] ad = new AxisDescription[el.getLength()];
        for ( int i = 0; i < ad.length; i++ ) {
            Element elem = XMLTools.getRequiredChildElement( "AxisDescription", WCSNS, el.item( i ) );
            ad[i] = getAxisDescription( elem );
        }
        return ad;
    }

    /**
     * creates an <tt>AxisDescription</tt> object from the passed element
     *
     * @param element
     * @return created <tt>AxisDescription</tt>
     * @throws XMLParsingException
     * @throws WCSException
     * @throws OGCException
     */
    private AxisDescription getAxisDescription( Element element )
                            throws XMLParsingException, WCSException, OGCException {
        try {
            String tmp = XMLTools.getAttrValue( element, null, "semantic", null );
            URI semantic = null;
            if ( tmp != null ) {
                semantic = new URI( tmp );
            }

            tmp = XMLTools.getAttrValue( element, null, "refSys", null );
            URI refSys = null;
            if ( tmp != null ) {
                refSys = new URI( tmp );
            }

            String refSysLabel = XMLTools.getAttrValue( element, null, "refSysLabel", null );

            String name = XMLTools.getRequiredStringValue( "name", WCSNS, element );
            String label = XMLTools.getStringValue( "label", WCSNS, element, name );
            String desc = XMLTools.getStringValue( "description", WCSNS, element, null );
            Element elem = XMLTools.getChildElement( "metadataLink", WCSNS, element );
            MetadataLink mLink = parseMetadataLink( elem );
            elem = XMLTools.getRequiredChildElement( "values", WCSNS, element );
            Values values = parseValues( elem, WCSNS );
            return new AxisDescription( name, label, desc, mLink, semantic, refSys, refSysLabel, values );
        } catch ( URISyntaxException e ) {
            throw new XMLParsingException( "couldn't parse URI from AxisDescription\n"
                                           + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * creates a <tt>SupportedSRSs</tt> object from the passed element
     *
     * @param element
     * @return created <tt>SupportedSRSs</tt>
     * @throws XMLParsingException
     */
    private SupportedSRSs getSupportedCRSs( Element element )
                            throws XMLParsingException {
        ElementList el = XMLTools.getChildElements( "requestResponseCRSs", WCSNS, element );
        CodeList[] requestResponseCRSs = parseCodeListArray( el );
        el = XMLTools.getChildElements( "requestCRSs", WCSNS, element );
        CodeList[] requestCRSs = parseCodeListArray( el );
        el = XMLTools.getChildElements( "responseCRSs", WCSNS, element );
        CodeList[] responseCRSs = parseCodeListArray( el );
        el = XMLTools.getChildElements( "nativeCRSs", WCSNS, element );
        CodeList[] nativeCRSs = parseCodeListArray( el );
        return new SupportedSRSs( requestResponseCRSs, requestCRSs, responseCRSs, nativeCRSs );
    }

    /**
     * creates a <tt>SupportedFormats</tt> object from the passed element
     *
     * @param element
     * @return
     * @throws XMLParsingException
     */
    private SupportedFormats getSupportedFomarts( Element element )
                            throws XMLParsingException {
        String nativeFormat = XMLTools.getAttrValue( element, null, "nativeFormat", null );
        ElementList el = XMLTools.getChildElements( "formats", WCSNS, element );
        CodeList[] formats = parseCodeListArray( el );
        Code nativeF = new Code( nativeFormat );
        return new SupportedFormats( formats, nativeF );
    }

    /**
     * creates a <tt>SupportedInterpolations<tt> object from the passed element
     * @param element
     * @return created <tt>SupportedInterpolations<tt>
     * @throws XMLParsingException
     */
    private SupportedInterpolations getSupportedInterpolations( Element element ) {
        String tmp = XMLTools.getAttrValue( element, null, "default", "nearest neighbor" );
        InterpolationMethod def = new InterpolationMethod( tmp );

        ElementList el = XMLTools.getChildElements( "interpolationMethod", WCSNS, element );
        InterpolationMethod[] ims = new InterpolationMethod[el.getLength()];
        for ( int i = 0; i < ims.length; i++ ) {
            tmp = XMLTools.getStringValue( el.item( i ) );
            ims[i] = new InterpolationMethod( tmp );
        }
        return new SupportedInterpolations( ims, def );
    }

}
