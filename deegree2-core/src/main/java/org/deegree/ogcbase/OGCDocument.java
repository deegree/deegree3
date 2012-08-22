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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.datatypes.CodeList;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.time.TimeDuration;
import org.deegree.datatypes.time.TimePeriod;
import org.deegree.datatypes.time.TimePosition;
import org.deegree.datatypes.time.TimeSequence;
import org.deegree.datatypes.values.Closure;
import org.deegree.datatypes.values.Interval;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.datatypes.values.Values;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.Linkage;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcwebservices.LonLatEnvelope;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcs.describecoverage.InvalidCoverageDescriptionExcpetion;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 1.1
 */
public abstract class OGCDocument extends XMLFragment {

    private static final long serialVersionUID = 6474662629175208201L;

    protected static final URI GMLNS = CommonNamespaces.GMLNS;

    private static ILogger LOG = LoggerFactory.getLogger( OGCDocument.class );

    /**
     * creates a <tt>LonLatEnvelope</tt> object from the passed element
     *
     * @param element
     * @return created <tt>LonLatEnvelope</tt>
     * @throws XMLParsingException
     * @throws OGCWebServiceException
     * @throws InvalidCoverageDescriptionExcpetion
     */
    public static LonLatEnvelope parseLonLatEnvelope( Element element )
                            throws XMLParsingException, OGCWebServiceException {

        String srs = XMLTools.getRequiredAttrValue( "srsName", null, element );
        if ( !( "WGS84(DD)".equals( srs ) ) && !("urn:ogc:def:crs:OGC:1.3:CRS84".equals( srs )) ) {
            throw new OGCWebServiceException( "srsName must be WGS84(DD) for lonLatEnvelope." );
        }

        ElementList el = XMLTools.getChildElements( "pos", GMLNS, element );
        if ( el == null || el.getLength() != 2 ) {
            throw new OGCWebServiceException( "A lonLatEnvelope must contain two gml:pos elements" );
        }

        Point min = GMLDocument.parsePos( el.item( 0 ) );
        Point max = GMLDocument.parsePos( el.item( 1 ) );

        el = XMLTools.getChildElements( "timePosition", GMLNS, element );
        TimePosition[] timePositions = parseTimePositions( el );

        return new LonLatEnvelope( min, max, timePositions, srs );
    }

    /**
     * creates an array of <tt>TimePosition</tt> s from the passed element
     *
     * @param el
     * @return created array of <tt>TimePosition</tt> s
     * @throws XMLParsingException
     * @throws InvalidCoverageDescriptionExcpetion
     */
    protected static TimePosition[] parseTimePositions( ElementList el )
                            throws XMLParsingException, OGCWebServiceException {
        TimePosition[] timePos = new TimePosition[el.getLength()];
        for ( int i = 0; i < timePos.length; i++ ) {
            timePos[i] = GMLDocument.parseTimePosition( el.item( i ) );
        }
        return timePos;
    }

    /**
     * Creates an array of <code>Keywords</code> from the passed list of <code>keyword</code> -elements.
     *
     * This appears to be pretty superfluous (as one <code>keywords</code>- element may contain several
     * <code>keyword</code> -elements). However, the schema in the OGC document "Web Coverage Service (WCS), Version
     * 1.0.0", contains the following line (in the definition of the CoverageOfferingBriefType):
     *
     * <code>&lt;xs:element ref="keywords" minOccurs="0" maxOccurs="unbounded"/&gt;</code>
     *
     * @param el
     * @return created array of <tt>Keywords</tt>
     */
    protected Keywords[] parseKeywords( ElementList el, URI namespaceURI ) {
        Keywords[] kws = new Keywords[el.getLength()];
        for ( int i = 0; i < kws.length; i++ ) {
            kws[i] = parseKeywords( el.item( i ), namespaceURI );
        }
        return kws;
    }

    /**
     * Creates a <code>Keywords</code> instance from the given <code>keywords</code> -element.
     *
     * @param element
     * @param namespaceURI
     * @return created <code>Keywords</code>
     */
    protected Keywords parseKeywords( Element element, URI namespaceURI ) {
        ElementList el = XMLTools.getChildElements( "keyword", namespaceURI, element );
        String[] kws = new String[el.getLength()];
        for ( int i = 0; i < kws.length; i++ ) {
            kws[i] = XMLTools.getStringValue( el.item( i ) );
        }
        return new Keywords( kws );
    }

    /**
     * creates an <tt>TimeSequence</tt> from the passed element
     *
     * @param element
     * @return created <tt>TimeSequence</tt>
     * @throws XMLParsingException
     * @throws InvalidCoverageDescriptionExcpetion
     */
    protected TimeSequence parseTimeSequence( Element element, URI namespaceURI )
                            throws XMLParsingException, OGCWebServiceException {
        ElementList el = XMLTools.getChildElements( "timePerdiod", namespaceURI, element );
        TimePeriod[] timePerdiods = parseTimePeriods( el, namespaceURI );
        el = XMLTools.getChildElements( "timePosition", GMLNS, element );
        TimePosition[] timePositions = parseTimePositions( el );

        return new TimeSequence( timePerdiods, timePositions );
    }

    /**
     * creates an array of <tt>TimePeriod</tt> s from the passed element
     *
     * @param el
     * @return created array of <tt>TimePeriod</tt> s
     * @throws XMLParsingException
     * @throws InvalidCoverageDescriptionExcpetion
     */
    protected TimePeriod[] parseTimePeriods( ElementList el, URI namespaceURI )
                            throws XMLParsingException, OGCWebServiceException {
        TimePeriod[] timePeriods = new TimePeriod[el.getLength()];
        for ( int i = 0; i < timePeriods.length; i++ ) {
            timePeriods[i] = parseTimePeriod( el.item( i ), namespaceURI );
        }
        return timePeriods;
    }

    /**
     * creates a <tt>TimePeriod</tt> from the passed element
     *
     * @param element
     * @return created <tt>TimePeriod</tt>
     * @throws XMLParsingException
     * @throws InvalidCoverageDescriptionExcpetion
     */
    protected TimePeriod parseTimePeriod( Element element, URI namespaceURI )
                            throws XMLParsingException, OGCWebServiceException {
        try {
            Element begin = XMLTools.getRequiredChildElement( "beginPosition", namespaceURI, element );
            TimePosition beginPosition = GMLDocument.parseTimePosition( begin );
            Element end = XMLTools.getRequiredChildElement( "endPosition", namespaceURI, element );
            TimePosition endPosition = GMLDocument.parseTimePosition( end );
            String dur = XMLTools.getRequiredStringValue( "timeResolution", namespaceURI, element );
            TimeDuration resolution = TimeDuration.createTimeDuration( dur );

            return new TimePeriod( beginPosition, endPosition, resolution );
        } catch ( InvalidGMLException e ) {
            LOG.logError( e.getMessage(), e );
            String s = e.getMessage() + "\n" + StringTools.stackTraceToString( e );
            throw new OGCWebServiceException( s );
        }

    }

    /**
     * creates a <tt>Values</tt> object from the passed element
     *
     * @param element
     * @return created <tt>Values</tt>
     * @throws XMLParsingException
     */
    protected Values parseValues( Element element, URI namespaceURI )
                            throws XMLParsingException {

        String type = XMLTools.getAttrValue( element, namespaceURI, "type", null );
        String semantic = XMLTools.getAttrValue( element, namespaceURI, "semantic", null );

        ElementList el = XMLTools.getChildElements( "interval", namespaceURI, element );
        Interval[] intervals = new Interval[el.getLength()];
        for ( int i = 0; i < intervals.length; i++ ) {
            intervals[i] = parseInterval( el.item( i ), namespaceURI );
        }

        el = XMLTools.getChildElements( "singleValue", namespaceURI, element );
        TypedLiteral[] singleValues = new TypedLiteral[el.getLength()];
        for ( int i = 0; i < singleValues.length; i++ ) {
            singleValues[i] = parseTypedLiteral( el.item( i ) );
        }

        Element elem = XMLTools.getChildElement( "default", namespaceURI, element );
        TypedLiteral def = null;
        if ( elem != null ) {
            def = parseTypedLiteral( elem );
        }

        try {
            URI sem = null;
            if ( semantic != null )
                sem = new URI( semantic );
            URI tp = null;
            if ( type != null )
                tp = new URI( type );
            return new Values( intervals, singleValues, tp, sem, def );
        } catch ( URISyntaxException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( "couldn't parse URI from valuesl\n" + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * creates an <tt>Interval</tt> object from the passed element
     *
     * @param element
     * @return created <tt>Interval</tt>
     * @throws XMLParsingException
     */
    protected Interval parseInterval( Element element, URI namespaceURI )
                            throws XMLParsingException {

        try {
            String tmp = XMLTools.getAttrValue( element, namespaceURI, "type", null );
            URI type = null;
            if ( tmp != null )
                type = new URI( tmp );
            String semantic = XMLTools.getAttrValue( element, namespaceURI, "semantic", null );
            tmp = XMLTools.getAttrValue( element, null, "atomic", null );
            boolean atomic = "true".equals( tmp ) || "1".equals( tmp );
            String clos = XMLTools.getAttrValue( element, namespaceURI, "closure", null );

            Closure closure = new Closure( clos );

            Element elem = XMLTools.getRequiredChildElement( "min", namespaceURI, element );
            TypedLiteral min = parseTypedLiteral( elem );

            elem = XMLTools.getRequiredChildElement( "min", namespaceURI, element );
            TypedLiteral max = parseTypedLiteral( elem );

            elem = XMLTools.getRequiredChildElement( "res", namespaceURI, element );
            TypedLiteral res = parseTypedLiteral( elem );

            URI sem = null;
            if ( semantic != null )
                sem = new URI( semantic );

            return new Interval( min, max, type, sem, atomic, closure, res );
        } catch ( URISyntaxException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( "couldn't parse URI from interval\n" + StringTools.stackTraceToString( e ) );
        }

    }

    /**
     * creates a <tt>TypedLiteral</tt> from the passed element
     *
     * @param element
     * @return created <tt>TypedLiteral</tt>
     * @throws XMLParsingException
     */
    protected TypedLiteral parseTypedLiteral( Element element )
                            throws XMLParsingException {
        try {
            String tmp = XMLTools.getStringValue( element );
            String mtype = XMLTools.getAttrValue( element, null, "type", null );
            URI mt = null;
            if ( mtype != null )
                mt = new URI( mtype );
            return new TypedLiteral( tmp, mt );
        } catch ( URISyntaxException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( "couldn't parse URI from typedLiteral\n"
                                           + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * creates an array of <tt>CodeList</tt> objects from the passed element list
     *
     * @param el
     * @return created array of <tt>CodeList</tt>
     * @throws XMLParsingException
     */
    protected CodeList[] parseCodeListArray( ElementList el )
                            throws XMLParsingException {
        CodeList[] cl = new CodeList[el.getLength()];
        for ( int i = 0; i < cl.length; i++ ) {
            cl[i] = parseCodeList( el.item( i ) );
        }
        return cl;
    }

    /**
     * creates a <tt>CodeList</tt> object from the passed element
     *
     * @param element
     * @return created <tt>CodeList</tt>
     * @throws XMLParsingException
     */
    protected CodeList parseCodeList( Element element )
                            throws XMLParsingException {
        if ( element == null ) {
            return null;
        }
        try {
            String tmp = XMLTools.getAttrValue( element, null, "codeSpace", null );
            URI codeSpace = null;
            if ( tmp != null ) {
                codeSpace = new URI( tmp );
            }
            tmp = XMLTools.getStringValue( element );
            String[] ar = StringTools.toArray( tmp, " ,;", true );
            return new CodeList( element.getNodeName(), ar, codeSpace );
        } catch ( URISyntaxException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( "couldn't parse URI from CodeList\n" + StringTools.stackTraceToString( e ) );
        }
    }

    /**
     * Creates an <tt>OnLineResource</tt> instance from the passed element. The element contains an OnlineResourse as it
     * is used in the OGC Web XXX CapabilitiesService specifications.
     *
     * TODO Compare with XMLFragment#parseSimpleLink
     *
     * @param element
     * @return the link
     * @throws XMLParsingException
     */
    protected OnlineResource parseOnLineResource( Element element )
                            throws XMLParsingException {

        OnlineResource olr = null;
        String attrValue = XMLTools.getRequiredAttrValue( "href", XLNNS, element );
        URL href = null;
        try {
            href = resolve( attrValue );
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( "Given value '" + attrValue + "' in attribute 'href' " + "(namespace: "
                                           + XLNNS + ") of element '" + element.getLocalName() + "' (namespace: "
                                           + element.getNamespaceURI() + ") is not a valid URL." );
        }
        Linkage linkage = new Linkage( href, Linkage.SIMPLE );
        String title = XMLTools.getAttrValue( element, XLNNS, "title", null );
        olr = new OnlineResource( null, null, linkage, null, title, href.getProtocol() );
        return olr;
    }

    /**
     * @param e
     * @return a new property path, possibly an xlinked one
     * @throws XMLParsingException
     */
    public static PropertyPath parseExtendedPropertyPath( Element e )
                            throws XMLParsingException {
        PropertyPath path = parsePropertyPath( (Text) XMLTools.getNode( e, "text()", null ) );

        if ( e.getLocalName().equals( "PropertyName" ) ) {
            return path;
        }

        String depth = e.getAttribute( "traverseXlinkDepth" );
        int idepth = depth == null ? -1 : ( depth.equals( "*" ) ? -1 : Integer.parseInt( depth ) );
        // TODO do this for expiry as well

        return new XLinkPropertyPath( path.getAllSteps(), idepth );
    }

    /**
     * Creates a new instance of <code>PropertyPath</code> from the given text node.
     * <p>
     * NOTE: Namespace prefices used in the property path should be bound using XML namespace mechanisms (i.e. using
     * xmlns attributes in the document). However, to enable processing of partly broken requests, unbound prefices are
     * accepted as well.
     *
     * @param textNode
     *            string representation of the property path
     * @return new PropertyPath instance
     * @throws XMLParsingException
     * @see PropertyPath
     */
    public static PropertyPath parsePropertyPath( Text textNode )
                            throws XMLParsingException {

        String path = XMLTools.getStringValue( textNode );
        String[] steps = StringTools.toArray( path, "/", false );
        List<PropertyPathStep> propertyPathSteps = new ArrayList<PropertyPathStep>( steps.length );

        for ( int i = 0; i < steps.length; i++ ) {
            PropertyPathStep propertyStep = null;
            QualifiedName propertyName = null;
            String step = steps[i];
            boolean isAttribute = false;
            boolean isIndexed = false;
            int selectedIndex = -1;

            // check if step begins with '@' -> must be the final step then
            if ( step.startsWith( "@" ) ) {
                if ( i != steps.length - 1 ) {
                    String msg = "PropertyName '" + path + "' is illegal: the attribute specifier may only "
                                 + "be used for the final step.";
                    throw new XMLParsingException( msg );
                }
                step = step.substring( 1 );
                isAttribute = true;
            }

            // check if the step ends with brackets ([...])
            if ( step.endsWith( "]" ) ) {
                if ( isAttribute ) {
                    String msg = "PropertyName '" + path + "' is illegal: if the attribute specifier ('@') is used, "
                                 + "index selection ('[...']) is not possible.";
                    throw new XMLParsingException( msg );
                }
                int bracketPos = step.indexOf( '[' );
                if ( bracketPos < 0 ) {
                    String msg = "PropertyName '" + path + "' is illegal. No opening brackets found for step '" + step
                                 + "'.";
                    throw new XMLParsingException( msg );
                }

                // workaround for really silly compliance tests as we're not supporting XPath anyway
                String inBrackets = step.substring( bracketPos + 1, step.length() - 1 );
                if ( inBrackets.indexOf( "position()" ) != -1 ) {
                    inBrackets = inBrackets.replace( "position()=", "" );
                    inBrackets = inBrackets.replace( "=position()", "" );
                }

                try {
                    selectedIndex = Integer.parseInt( inBrackets );
                } catch ( NumberFormatException e ) {
                    String msg = "PropertyName '" + path + "' is illegal. Specified index '"
                                 + step.substring( bracketPos + 1, step.length() - 1 ) + "' is not a number.";
                    throw new XMLParsingException( msg );
                }
                step = step.substring( 0, bracketPos );
                isIndexed = true;
            }

            // determine namespace prefix and binding (if any)
            int colonPos = step.indexOf( ':' );
            if ( colonPos < 0 ) {
                propertyName = new QualifiedName( step );
            } else {
                String prefix = step.substring( 0, colonPos );
                step = step.substring( colonPos + 1 );
                URI namespace = null;
                try {
                    namespace = XMLTools.getNamespaceForPrefix( prefix, textNode );
                } catch ( URISyntaxException e ) {
                    throw new XMLParsingException( "Error parsing PropertyName: " + e.getMessage() );
                }
                if ( namespace == null ) {
                    LOG.logWarning( "PropertyName '" + path + "' uses an unbound namespace prefix: " + prefix );
                }
                propertyName = new QualifiedName( prefix, step, namespace );
            }

            // hack for "*" is here (AnyStep with or without index)
            // this probably only works wor SQL datastores!
            if ( step.equals( "*" ) ) {
                if ( isIndexed ) {
                    propertyStep = PropertyPathFactory.createAnyStep( selectedIndex );
                } else {
                    propertyStep = PropertyPathFactory.createAnyStep();
                }
            } else {
                if ( isAttribute ) {
                    propertyStep = PropertyPathFactory.createAttributePropertyPathStep( propertyName );
                } else if ( isIndexed ) {
                    propertyStep = PropertyPathFactory.createPropertyPathStep( propertyName, selectedIndex );
                } else {
                    propertyStep = PropertyPathFactory.createPropertyPathStep( propertyName );
                }
            }
            propertyPathSteps.add( propertyStep );
        }
        return PropertyPathFactory.createPropertyPath( propertyPathSteps );
    }
}
