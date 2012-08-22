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
package org.deegree.graphics.sld;

import static java.lang.Float.parseFloat;
import static java.lang.StrictMath.sqrt;
import static org.deegree.framework.xml.XMLTools.getElement;
import static org.deegree.framework.xml.XMLTools.getNodeAsDouble;
import static org.deegree.ogcbase.CommonNamespaces.SLDNS;
import static org.deegree.ogcbase.CommonNamespaces.SLD_PREFIX;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.FalseFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.OGCDocument;
import org.deegree.ogcbase.PropertyPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Factory class for all mapped SLD-elements.
 * <p>
 * TODO: Default values for omitted elements (such as fill color) should better not be used in the construction of the
 * corresponding objects (Fill), but marked as left out (to make it possible to differentiate between explicitly given
 * values and default values).
 * <p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$ $Date$
 */
public class SLDFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( SLDFactory.class );

    private static URI ogcNS = CommonNamespaces.OGCNS;

    private static URI xlnNS = CommonNamespaces.XLNNS;

    private static final String PSE = CommonNamespaces.SE_PREFIX + ":";

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private static XMLFragment sldDoc = null;

    /**
     * Creates a <tt>StyledLayerDescriptor</tt>-instance from the given XML-representation.
     * <p>
     * 
     * @param s
     *            contains the XML document
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the XML document is encountered
     * @return the constructed <tt>StyledLayerDescriptor</tt>-instance
     */
    public static synchronized StyledLayerDescriptor createSLD( String s )
                            throws XMLParsingException {
        StyledLayerDescriptor sld = null;
        try {
            sldDoc = new XMLFragment();
            sldDoc.load( new StringReader( s.trim() ), XMLFragment.DEFAULT_URL );
            sld = createSLD( sldDoc );
        } catch ( IOException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new XMLParsingException( "IOException encountered while parsing SLD-Document" );
        } catch ( SAXException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new XMLParsingException( "SAXException encountered while parsing SLD-Document" );
        }

        return sld;
    }

    /**
     * Creates a <tt>StyledLayerDescriptor</tt>-instance from a SLD document read from the passed URL
     * 
     * @param url
     * @return the SLD bean
     * @throws XMLParsingException
     */
    public static synchronized StyledLayerDescriptor createSLD( URL url )
                            throws XMLParsingException {
        StyledLayerDescriptor sld = null;

        try {
            sldDoc = new XMLFragment();
            sldDoc.load( url );
            sld = createSLD( sldDoc );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( "IOException encountered while parsing SLD-Document" );
        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( "SAXException encountered while parsing SLD-Document" );
        }

        return sld;
    }

    /**
     * Creates a <tt>StyledLayerDescriptor</tt>-instance according to the contents of the DOM-subtree starting at the
     * given 'StyledLayerDescriptor'-<tt>Element</tt>.
     * <p>
     * 
     * @param sldDoc
     * 
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>StyledLayerDescriptor</tt>-instance
     */
    public static StyledLayerDescriptor createSLD( XMLFragment sldDoc )
                            throws XMLParsingException {

        Element element = sldDoc.getRootElement();

        // optional: <Name>
        String name = XMLTools.getStringValue( "Name", CommonNamespaces.SLDNS, element, null );

        // optional: <Title>
        String title = XMLTools.getStringValue( "Title", CommonNamespaces.SLDNS, element, null );
        // optional: <Abstract>
        String abstract_ = XMLTools.getStringValue( "Abstract", CommonNamespaces.SLDNS, element, null );
        // required: version-Attribute
        String version = XMLTools.getRequiredAttrValue( "version", null, element );

        // optional: <NamedLayer>(s) / <UserLayer>(s)
        NodeList nodelist = element.getChildNodes();
        ArrayList<AbstractLayer> layerList = new ArrayList<AbstractLayer>( 100 );

        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            if ( nodelist.item( i ) instanceof Element ) {
                Element child = (Element) nodelist.item( i );
                String namespace = child.getNamespaceURI();
                if ( !CommonNamespaces.SLDNS.toASCIIString().equals( namespace ) ) {
                    continue;
                }

                String childName = child.getLocalName();
                if ( childName.equals( "NamedLayer" ) ) {
                    layerList.add( createNamedLayer( child ) );
                } else if ( childName.equals( "UserLayer" ) ) {
                    layerList.add( createUserLayer( child ) );
                }
            }
        }

        AbstractLayer[] al = new AbstractLayer[layerList.size()];
        AbstractLayer[] layers = layerList.toArray( al );

        return new StyledLayerDescriptor( name, title, version, abstract_, layers );
    }

    private static Categorize createCategorize( Element root )
                            throws XMLParsingException {
        // ignore fallback value, we really implement it
        // String fallbackValue = root.getAttribute( "fallbackValue" );

        Categorize categorize = new Categorize();

        // ignore lookup value element, should be set to "Rasterdata"
        // Element lv = XMLTools.getElement( root, PSE + "LookupValue", nsContext );
        // ParameterValueType lookupValue = lv == null ? null : createParameterValueType( lv );
        //
        // if ( lookupValue != null ) {
        // categorize.setLookupValue( lookupValue );
        // }

        List<Element> valueElements = XMLTools.getElements( root, PSE + "Value", nsContext );
        List<Element> thresholdElements = XMLTools.getElements( root, PSE + "Threshold", nsContext );

        LinkedList<ParameterValueType> values = new LinkedList<ParameterValueType>();
        LinkedList<ParameterValueType> thresholds = new LinkedList<ParameterValueType>();

        for ( Element e : valueElements ) {
            values.add( createParameterValueType( e ) );
        }

        for ( Element e : thresholdElements ) {
            thresholds.add( createParameterValueType( e ) );
        }

        categorize.setValues( values );
        categorize.setThresholds( thresholds );

        String tbt = root.getAttribute( "threshholdsBelongTo" );
        if ( tbt == null ) {
            tbt = root.getAttribute( "thresholdsBelongTo" );
        }

        ThresholdsBelongTo thresholdsBelongTo = null;

        if ( tbt != null ) {
            if ( tbt.equalsIgnoreCase( "succeeding" ) ) {
                thresholdsBelongTo = ThresholdsBelongTo.SUCCEEDING;
            }
            if ( tbt.equalsIgnoreCase( "preceding" ) ) {
                thresholdsBelongTo = ThresholdsBelongTo.PRECEDING;
            }
        }

        if ( thresholdsBelongTo != null ) {
            categorize.setThresholdsBelongTo( thresholdsBelongTo );
        }

        return categorize;
    }

    /**
     * 
     * @param root
     * @param min
     * @param max
     * @return a raster symbolizer
     * @throws XMLParsingException
     */
    private static RasterSymbolizer createRasterSymbolizer( Element root, double min, double max )
                            throws XMLParsingException {
        RasterSymbolizer symbolizer = new RasterSymbolizer( min, max );

        if ( root.getAttribute( "shaded" ) != null && root.getAttribute( "shaded" ).equals( "true" ) ) {
            symbolizer.setShaded( true );

            String kernelAtt = root.getAttribute( "kernel" );
            if ( kernelAtt == null || kernelAtt.trim().length() == 0 ) {
                kernelAtt = "0, 1, 2, -1, 1, 1, -2, -1, 0";
            }
            String[] vals = kernelAtt.split( "[,]" );
            float[] kernel = new float[vals.length];
            for ( int i = 0; i < vals.length; ++i ) {
                kernel[i] = parseFloat( vals[i] );
            }

            symbolizer.setShadeKernel( (int) sqrt( kernel.length ), kernel );
        }

        Element opacity = XMLTools.getElement( root, PSE + "Opacity", nsContext );
        if ( opacity != null ) {
            symbolizer.setOpacity( createParameterValueType( opacity ) );
        }

        Element colorMap = XMLTools.getElement( root, PSE + "ColorMap", nsContext );
        if ( colorMap != null ) {
            Element categorize = XMLTools.getElement( colorMap, PSE + "Categorize", nsContext );

            if ( categorize != null ) {
                symbolizer.setCategorize( createCategorize( categorize ) );
            }

            Element interpolate = XMLTools.getElement( colorMap, PSE + "Interpolate", nsContext );

            if ( interpolate != null ) {
                symbolizer.setInterpolate( createInterpolate( interpolate ) );
            }
        }

        Element contrastEnhancement = getElement( root, PSE + "ContrastEnhancement", nsContext );
        if ( contrastEnhancement != null ) {
            symbolizer.setGamma( getNodeAsDouble( contrastEnhancement, PSE + "GammaValue", nsContext, 0d ) );
        }

        return symbolizer;
    }

    /**
     * @param root
     * @return an Interpolate object
     * @throws XMLParsingException
     */
    private static Interpolate createInterpolate( Element root )
                            throws XMLParsingException {
        String fallbackValue = root.getAttribute( "fallbackValue" );

        Interpolate interpolate = new Interpolate( fallbackValue );

        Element elem = XMLTools.getElement( root, PSE + "lookupValue", nsContext );
        if ( elem != null ) {
            interpolate.setLookupValue( createParameterValueType( elem ) );
        }

        String mode = root.getAttribute( "mode" );
        if ( mode != null ) {
            if ( mode.equalsIgnoreCase( "linear" ) ) {
                interpolate.setMode( Mode.LINEAR );
            }
            if ( mode.equalsIgnoreCase( "cosine" ) ) {
                LOG.logWarning( "Cosine interpolation is not supported." );
                interpolate.setMode( Mode.COSINE );
            }
            if ( mode.equalsIgnoreCase( "cubic" ) ) {
                LOG.logWarning( "Cubic interpolation is not supported." );
                interpolate.setMode( Mode.CUBIC );
            }
        }

        String method = root.getAttribute( "method" );
        if ( method != null ) {
            if ( method.equalsIgnoreCase( "numeric" ) ) {
                LOG.logWarning( "Numeric method is not supported, using color method anyway." );
                interpolate.setMethod( Method.NUMERIC );
            }
            if ( method.equalsIgnoreCase( "color" ) ) {
                interpolate.setMethod( Method.COLOR );
            }
        }

        List<Element> ips = XMLTools.getElements( root, PSE + "InterpolationPoint", nsContext );

        interpolate.setInterpolationPoints( createInterpolationPoints( ips ) );

        return interpolate;
    }

    private static List<InterpolationPoint> createInterpolationPoints( List<Element> ips )
                            throws XMLParsingException {
        List<InterpolationPoint> ps = new ArrayList<InterpolationPoint>( ips.size() );

        for ( Element elem : ips ) {
            double data = XMLTools.getRequiredNodeAsDouble( elem, PSE + "Data", nsContext );
            Element e = XMLTools.getRequiredElement( elem, PSE + "Value", nsContext );
            try {
                String val = createParameterValueType( e ).evaluate( null ).substring( 1 );
                ps.add( new InterpolationPoint( data, val ) );
            } catch ( NumberFormatException e1 ) {
                LOG.logError( "A 'Value' in an 'InterpolationPoint' could not be parsed.", e1 );
            } catch ( FilterEvaluationException e1 ) {
                LOG.logError( "A 'Value' in an 'InterpolationPoint' could not be parsed.", e1 );
            }
        }

        return ps;
    }

    /**
     * Creates a <tt>TextSymbolizer</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'TextSymbolizer'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'TextSymbolizer'-<tt>Element</tt>
     * @param min
     *            scale-constraint to be used
     * @param max
     *            scale-constraint to be used
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>TextSymbolizer</tt>-instance
     */
    private static TextSymbolizer createTextSymbolizer( Element element, double min, double max )
                            throws XMLParsingException {

        // optional: <Geometry>
        Geometry geometry = null;
        Element geometryElement = XMLTools.getChildElement( "Geometry", CommonNamespaces.SLDNS, element );

        if ( geometryElement != null ) {
            geometry = createGeometry( geometryElement );
        }

        // optional: <Label>
        ParameterValueType label = null;
        Element labelElement = XMLTools.getChildElement( "Label", CommonNamespaces.SLDNS, element );

        if ( labelElement != null ) {
            label = createParameterValueType( labelElement );
        }

        // optional: <Font>
        Font font = null;
        Element fontElement = XMLTools.getChildElement( "Font", CommonNamespaces.SLDNS, element );

        if ( fontElement != null ) {
            font = createFont( fontElement );
        }

        // optional: <LabelPlacement>
        LabelPlacement labelPlacement = null;
        Element lpElement = XMLTools.getChildElement( "LabelPlacement", CommonNamespaces.SLDNS, element );

        if ( lpElement != null ) {
            labelPlacement = createLabelPlacement( lpElement );
        } else {
            PointPlacement pp = StyleFactory.createPointPlacement();
            labelPlacement = StyleFactory.createLabelPlacement( pp );
        }

        // optional: <Halo>
        Halo halo = null;
        Element haloElement = XMLTools.getChildElement( "Halo", SLDNS, element );

        if ( haloElement != null ) {
            halo = createHalo( haloElement );
        }

        // optional: <Fill>
        Fill fill = null;

        TextSymbolizer ps = null;

        // deegree specific extension:
        Element bbox = getElement( element, SLD_PREFIX + ":BoundingBox", nsContext );
        if ( bbox != null ) {
            try {
                ParameterValueType minx = createParameterValueType( getElement( bbox, SLD_PREFIX + ":Minx", nsContext ) );
                ParameterValueType miny = createParameterValueType( getElement( bbox, SLD_PREFIX + ":Miny", nsContext ) );
                ParameterValueType maxx = createParameterValueType( getElement( bbox, SLD_PREFIX + ":Maxx", nsContext ) );
                ParameterValueType maxy = createParameterValueType( getElement( bbox, SLD_PREFIX + ":Maxy", nsContext ) );

                ps = new TextSymbolizer( geometry, label, font, halo, minx, miny, maxx, maxy );
            } catch ( NullPointerException npe ) {
                npe.printStackTrace();
                LOG.logError( "One of Minx, Miny, Maxx, Maxy was missing in a BoundingBox." );
            }
        } else {

            String respClass = XMLTools.getAttrValue( element, null, "responsibleClass", null );
            if ( respClass == null ) {
                ps = new TextSymbolizer( geometry, label, font, labelPlacement, halo, fill, min, max );
            } else {
                ps = new TextSymbolizer( geometry, respClass, label, font, labelPlacement, halo, fill, min, max );
            }
        }

        return ps;
    }

    /**
     * Creates a <tt>Halo</tt>-instance according to the contents of the DOM-subtree starting at the given 'Halo'-
     * <tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'Halo'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Halo</tt>-instance
     */
    private static Halo createHalo( Element element )
                            throws XMLParsingException {
        // optional: <Radius>
        ParameterValueType radius = null;
        Element radiusElement = XMLTools.getChildElement( "Radius", CommonNamespaces.SLDNS, element );

        if ( radiusElement != null ) {
            radius = createParameterValueType( radiusElement );
        }

        // optional: <Fill>
        Fill fill = null;
        Element fillElement = XMLTools.getChildElement( "Fill", CommonNamespaces.SLDNS, element );

        if ( fillElement != null ) {
            fill = createFill( fillElement );
        }

        // optional: <Stroke>
        Stroke stroke = null;
        Element strokeElement = XMLTools.getChildElement( "Stroke", CommonNamespaces.SLDNS, element );

        if ( strokeElement != null ) {
            stroke = createStroke( strokeElement );
        }

        return new Halo( radius, fill, stroke );
    }

    /**
     * Creates a <tt>LabelPlacement</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'LabelPlacement'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'LabelPlacement'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>LabelPlacement</tt>-instance
     */
    private static LabelPlacement createLabelPlacement( Element element )
                            throws XMLParsingException {
        LabelPlacement labelPlacement = null;

        // required: <PointPlacement> / <LinePlacement>
        NodeList nodelist = element.getChildNodes();
        PointPlacement pPlacement = null;
        LinePlacement lPlacement = null;

        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            if ( nodelist.item( i ) instanceof Element ) {
                Element child = (Element) nodelist.item( i );
                String namespace = child.getNamespaceURI();

                if ( !CommonNamespaces.SLDNS.toASCIIString().equals( namespace ) ) {
                    continue;
                }

                String childName = child.getLocalName();

                if ( childName.equals( "PointPlacement" ) ) {
                    pPlacement = createPointPlacement( child );
                } else if ( childName.equals( "LinePlacement" ) ) {
                    lPlacement = createLinePlacement( child );
                }
            }
        }

        if ( ( pPlacement != null ) && ( lPlacement == null ) ) {
            labelPlacement = new LabelPlacement( pPlacement );
        } else if ( ( pPlacement == null ) && ( lPlacement != null ) ) {
            labelPlacement = new LabelPlacement( lPlacement );
        } else {
            throw new XMLParsingException( "Element 'LabelPlacement' must contain exactly one "
                                           + "'PointPlacement'- or one 'LinePlacement'-element!" );
        }

        return labelPlacement;
    }

    /**
     * Creates a <tt>PointPlacement</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'PointPlacement'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'PointPlacement'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>PointPlacement</tt>-instance
     */
    private static PointPlacement createPointPlacement( Element element )
                            throws XMLParsingException {

        // optional: auto-Attribute (this is deegree-specific)
        boolean auto = false;
        String autoStr = XMLTools.getAttrValue( element, null, "auto", null );

        if ( autoStr != null && autoStr.equals( "true" ) ) {
            auto = true;
        }

        // optional: <AnchorPoint>
        ParameterValueType[] anchorPoint = null;
        Element apElement = XMLTools.getChildElement( "AnchorPoint", CommonNamespaces.SLDNS, element );

        if ( apElement != null ) {
            anchorPoint = new ParameterValueType[2];

            Element apXElement = XMLTools.getChildElement( "AnchorPointX", CommonNamespaces.SLDNS, apElement );
            Element apYElement = XMLTools.getChildElement( "AnchorPointY", CommonNamespaces.SLDNS, apElement );

            if ( ( apXElement == null ) || ( apYElement == null ) ) {
                throw new XMLParsingException( "Element 'AnchorPoint' must contain exactly one "
                                               + "'AnchorPointX'- and one 'AnchorPointY'-element!" );
            }

            anchorPoint[0] = createParameterValueType( apXElement );
            anchorPoint[1] = createParameterValueType( apYElement );
        }

        // optional: <Displacement>
        ParameterValueType[] displacement = null;
        Element dElement = XMLTools.getChildElement( "Displacement", CommonNamespaces.SLDNS, element );

        if ( dElement != null ) {
            displacement = new ParameterValueType[2];

            Element dXElement = XMLTools.getChildElement( "DisplacementX", CommonNamespaces.SLDNS, dElement );
            Element dYElement = XMLTools.getChildElement( "DisplacementY", CommonNamespaces.SLDNS, dElement );

            if ( ( dXElement == null ) || ( dYElement == null ) ) {
                throw new XMLParsingException( "Element 'Displacement' must contain exactly one "
                                               + "'DisplacementX'- and one 'DisplacementY'-element!" );
            }

            displacement[0] = createParameterValueType( dXElement );
            displacement[1] = createParameterValueType( dYElement );
        }

        // optional: <Rotation>
        ParameterValueType rotation = null;
        Element rElement = XMLTools.getChildElement( "Rotation", CommonNamespaces.SLDNS, element );

        if ( rElement != null ) {
            rotation = createParameterValueType( rElement );
        }

        return new PointPlacement( anchorPoint, displacement, rotation, auto );
    }

    /**
     * Creates a <tt>LinePlacement</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'LinePlacement'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'LinePlacement'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>LinePlacement</tt>-instance
     */
    private static LinePlacement createLinePlacement( Element element )
                            throws XMLParsingException {

        // optional: <PerpendicularOffset>
        ParameterValueType pOffset = null;
        Element pOffsetElement = XMLTools.getChildElement( "PerpendicularOffset", CommonNamespaces.SLDNS, element );

        if ( pOffsetElement != null ) {
            pOffset = createParameterValueType( pOffsetElement );
        }

        // optional: <Gap> (this is deegree-specific)
        ParameterValueType gap = null;
        Element gapElement = XMLTools.getChildElement( "Gap", CommonNamespaces.SLDNS, element );

        if ( gapElement != null ) {
            gap = createParameterValueType( gapElement );
        }

        // optional: <LineWidth> (this is deegree-specific)
        ParameterValueType lineWidth = null;
        Element lineWidthElement = XMLTools.getChildElement( "LineWidth", CommonNamespaces.SLDNS, element );

        if ( lineWidthElement != null ) {
            lineWidth = createParameterValueType( lineWidthElement );
        }

        return new LinePlacement( pOffset, lineWidth, gap );
    }

    /**
     * Creates a <tt>Font</tt>-instance according to the contents of the DOM-subtree starting at the given 'Font'-
     * <tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'Font'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Font</tt>-instance
     */
    private static Font createFont( Element element )
                            throws XMLParsingException {

        // optional: <CssParameter>s
        ElementList nl = XMLTools.getChildElements( "CssParameter", CommonNamespaces.SLDNS, element );
        HashMap<String, CssParameter> cssParams = new HashMap<String, CssParameter>( nl.getLength() );

        for ( int i = 0; i < nl.getLength(); i++ ) {
            CssParameter cssParam = createCssParameter( nl.item( i ) );
            cssParams.put( cssParam.getName(), cssParam );
        }

        return new Font( cssParams );
    }

    /**
     * Creates a <tt>ParameterValueType</tt>-instance according to the contents of the DOM-subtree starting at the given
     * <tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the <tt>Element</tt> (must be of the type sld:ParameterValueType)
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>ParameterValueType</tt>-instance
     */
    private static ParameterValueType createParameterValueType( Element element )
                            throws XMLParsingException {
        // mix of text nodes and <wfs:Expression>-elements
        ArrayList<Object> componentList = new ArrayList<Object>();
        NodeList nl = element.getChildNodes();

        for ( int i = 0; i < nl.getLength(); i++ ) {
            Node node = nl.item( i );

            switch ( node.getNodeType() ) {
            case Node.TEXT_NODE: {
                componentList.add( node.getNodeValue() );
                break;
            }
            case Node.ELEMENT_NODE: {
                Expression expression = Expression.buildFromDOM( (Element) node );
                componentList.add( expression );
                break;
            }
            default:
                throw new XMLParsingException( "Elements of type 'ParameterValueType' may only "
                                               + "consist of CDATA and 'ogc:Expression'-elements!" );
            }
        }

        Object[] components = componentList.toArray( new Object[componentList.size()] );
        return new ParameterValueType( components );
    }

    /**
     * Creates a <tt>NamedStyle</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'NamedStyle'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'NamedStyle'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>NamedStyle</tt>-instance
     */
    private static NamedStyle createNamedStyle( Element element )
                            throws XMLParsingException {
        // required: <Name>
        String name = XMLTools.getRequiredStringValue( "Name", CommonNamespaces.SLDNS, element );

        return new NamedStyle( name );
    }

    /**
     *
     */
    public static NamedStyle createNamedStyle( String name ) {
        return new NamedStyle( name );
    }

    /**
     * Creates a <tt>RemoteOWS</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'RemoteOWS'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'RemoteOWS'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>RemoteOWS</tt>-instance
     */
    private static RemoteOWS createRemoteOWS( Element element )
                            throws XMLParsingException {
        // required: <Service>
        String service = XMLTools.getRequiredStringValue( "Service", CommonNamespaces.SLDNS, element );

        if ( !( service.equals( "WFS" ) || service.equals( "WCS" ) ) ) {
            throw new XMLParsingException( "Value ('" + service + "') of element 'service' is invalid. "
                                           + "Allowed values are: 'WFS' and 'WCS'." );
        }

        // required: <OnlineResource>
        Element onlineResourceElement = XMLTools.getRequiredChildElement( "OnlineResource", CommonNamespaces.SLDNS,
                                                                          element );
        String href = XMLTools.getRequiredAttrValue( "xlink:href", null, onlineResourceElement );
        URL url = null;

        try {
            url = new URL( href );
        } catch ( MalformedURLException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new XMLParsingException( "Value ('" + href + "') of attribute 'href' of "
                                           + "element 'OnlineResoure' does not denote a valid URL" );
        }

        return new RemoteOWS( service, url );
    }

    /**
     * Creates a <tt>NamedLayer</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'UserLayer'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'NamedLayer'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>NamedLayer</tt>-instance
     */
    private static NamedLayer createNamedLayer( Element element )
                            throws XMLParsingException {
        // required: <Name>
        String name = XMLTools.getRequiredStringValue( "Name", CommonNamespaces.SLDNS, element );

        // optional: <LayerFeatureConstraints>
        LayerFeatureConstraints lfc = null;
        Element lfcElement = XMLTools.getChildElement( "LayerFeatureConstraints", CommonNamespaces.SLDNS, element );

        if ( lfcElement != null ) {
            lfc = createLayerFeatureConstraints( lfcElement );
        }

        // optional: <NamedStyle>(s) / <UserStyle>(s)
        NodeList nodelist = element.getChildNodes();
        ArrayList<AbstractStyle> styleList = new ArrayList<AbstractStyle>();

        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            if ( nodelist.item( i ) instanceof Element ) {
                Element child = (Element) nodelist.item( i );
                String namespace = child.getNamespaceURI();

                if ( !CommonNamespaces.SLDNS.toASCIIString().equals( namespace ) ) {
                    continue;
                }

                String childName = child.getLocalName();

                if ( childName.equals( "NamedStyle" ) ) {
                    styleList.add( createNamedStyle( child ) );
                } else if ( childName.equals( "UserStyle" ) ) {
                    styleList.add( createUserStyle( child ) );
                }
            }
        }

        AbstractStyle[] styles = styleList.toArray( new AbstractStyle[styleList.size()] );

        return new NamedLayer( name, lfc, styles );
    }

    /**
     *
     */
    public static NamedLayer createNamedLayer( String name, LayerFeatureConstraints layerFeatureConstraints,
                                               AbstractStyle[] styles ) {
        return new NamedLayer( name, layerFeatureConstraints, styles );
    }

    /**
     * Creates a <tt>UserLayer</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'UserLayer'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'UserLayer'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>UserLayer</tt>-instance
     */
    private static UserLayer createUserLayer( Element element )
                            throws XMLParsingException {
        // optional: <Name>
        String name = XMLTools.getStringValue( "Name", CommonNamespaces.SLDNS, element, null );

        // optional: <RemoteOWS>
        RemoteOWS remoteOWS = null;
        Element remoteOWSElement = XMLTools.getChildElement( "RemoteOWS", CommonNamespaces.SLDNS, element );

        if ( remoteOWSElement != null ) {
            remoteOWS = createRemoteOWS( remoteOWSElement );
        }

        // required: <LayerFeatureConstraints>
        LayerFeatureConstraints lfc = null;
        Element lfcElement = XMLTools.getRequiredChildElement( "LayerFeatureConstraints", CommonNamespaces.SLDNS,
                                                               element );
        lfc = createLayerFeatureConstraints( lfcElement );

        // optional: <UserStyle>(s)
        ElementList nodelist = XMLTools.getChildElements( "UserStyle", CommonNamespaces.SLDNS, element );
        UserStyle[] styles = new UserStyle[nodelist.getLength()];
        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            styles[i] = createUserStyle( nodelist.item( i ) );
        }

        return new UserLayer( name, lfc, styles, remoteOWS );
    }

    /**
     * Creates a <tt>FeatureTypeConstraint</tt>-instance according to the contents of the DOM-subtree starting at the
     * given 'FeatureTypeConstraint'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'FeatureTypeConstraint'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>FeatureTypeConstraint</tt>-instance
     */
    private static FeatureTypeConstraint createFeatureTypeConstraint( Element element )
                            throws XMLParsingException {
        // optional: <Name>
        Node node = XMLTools.getNode( element, "sld:FeatureTypeName/text()", CommonNamespaces.getNamespaceContext() );

        QualifiedName name;
        if ( node == null ) {
            name = null;
        } else {
            name = XMLTools.getQualifiedNameValue( node );
        }

        // optional: <Filter>
        Filter filter = null;
        Element filterElement = XMLTools.getChildElement( "Filter", ogcNS, element );

        if ( filterElement != null ) {
            filter = AbstractFilter.buildFromDOM( filterElement );
        }

        // optional: <Extent>(s)
        ElementList nodelist = XMLTools.getChildElements( "Extent", CommonNamespaces.SLDNS, element );
        Extent[] extents = new Extent[nodelist.getLength()];

        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            extents[i] = createExtent( nodelist.item( i ) );
        }

        return new FeatureTypeConstraint( name, filter, extents );
    }

    /**
     * Creates an <tt>Extent</tt>-instance according to the contents of the DOM-subtree starting at the given 'Extent'-
     * <tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'Extent'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Extent</tt>-instance
     */
    private static Extent createExtent( Element element )
                            throws XMLParsingException {
        // required: <Name>
        String name = XMLTools.getRequiredStringValue( "Name", CommonNamespaces.SLDNS, element );
        // required: <Value>
        String value = XMLTools.getRequiredStringValue( "Value", CommonNamespaces.SLDNS, element );

        return new Extent( name, value );
    }

    /**
     * Creates a <tt>LayerFeatureConstraints</tt>-instance according to the contents of the DOM-subtree starting at the
     * given 'LayerFeatureConstraints'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'LayerFeatureConstraints'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>LayerFeatureConstraints</tt>-instance
     */
    public static LayerFeatureConstraints createLayerFeatureConstraints( Element element )
                            throws XMLParsingException {
        // required: <FeatureTypeConstraint>(s)
        ElementList nodelist = XMLTools.getChildElements( "FeatureTypeConstraint", CommonNamespaces.SLDNS, element );
        FeatureTypeConstraint[] ftcs = new FeatureTypeConstraint[nodelist.getLength()];

        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            ftcs[i] = createFeatureTypeConstraint( nodelist.item( i ) );
        }

        return new LayerFeatureConstraints( ftcs );
    }

    /**
     * Creates a <tt>UserStyle</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'UserStyle'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'UserStyle'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>UserStyle</tt>-instance
     */
    public static UserStyle createUserStyle( Element element )
                            throws XMLParsingException {
        // optional: <Name>
        String name = XMLTools.getStringValue( "Name", CommonNamespaces.SLDNS, element, null );
        // optional: <Title>
        String title = XMLTools.getStringValue( "Title", CommonNamespaces.SLDNS, element, null );
        // optional: <Abstract>
        String abstract_ = XMLTools.getStringValue( "Abstract", CommonNamespaces.SLDNS, element, null );

        // optional: <IsDefault>
        String defaultString = XMLTools.getStringValue( "IsDefault", CommonNamespaces.SLDNS, element, null );
        boolean isDefault = false;

        if ( defaultString != null ) {
            if ( defaultString.equals( "1" ) ) {
                isDefault = true;
            }
        }

        // required: <FeatureTypeStyle> (s)
        ElementList nl = XMLTools.getChildElements( "FeatureTypeStyle", CommonNamespaces.SLDNS, element );
        FeatureTypeStyle[] styles = new FeatureTypeStyle[nl.getLength()];

        if ( styles.length == 0 ) {
            throw new XMLParsingException( "Required child-element 'FeatureTypeStyle' of element "
                                           + "'UserStyle' is missing!" );
        }

        for ( int i = 0; i < nl.getLength(); i++ ) {
            styles[i] = createFeatureTypeStyle( nl.item( i ) );
        }

        return new UserStyle( name, title, abstract_, isDefault, styles );
    }

    /**
     * Creates a <tt>FeatureTypeStyle</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'FeatureTypeStyle'-<tt>Element</tt>.
     * <p>
     * TODO: The ElseFilter currently does not work correctly with FeatureFilters.
     * <p>
     * 
     * @param element
     *            the 'FeatureTypeStyle'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>FeatureTypeStyle</tt>-instance
     */
    public static FeatureTypeStyle createFeatureTypeStyle( Element element )
                            throws XMLParsingException {
        // optional: <Name>
        String name = XMLTools.getStringValue( "Name", CommonNamespaces.SLDNS, element, null );
        // optional: <Title>
        String title = XMLTools.getStringValue( "Title", CommonNamespaces.SLDNS, element, null );
        // optional: <Abstract>
        String abstract_ = XMLTools.getStringValue( "Abstract", CommonNamespaces.SLDNS, element, null );
        // optional: <FeatureTypeName>
        String featureTypeName = XMLTools.getStringValue( "FeatureTypeName", CommonNamespaces.SLDNS, element, null );

        // optional: several <Rule> / <SemanticTypeIdentifier>
        NodeList nodelist = element.getChildNodes();
        ArrayList<Rule> ruleList = new ArrayList<Rule>();
        ArrayList<String> typeIdentifierList = new ArrayList<String>();

        // collect Filters of all Rules
        ArrayList<Filter> filters = new ArrayList<Filter>();
        // collect all Rules that have an ElseFilter
        ArrayList<Rule> elseRules = new ArrayList<Rule>();

        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            if ( nodelist.item( i ) instanceof Element ) {
                Element child = (Element) nodelist.item( i );
                String namespace = child.getNamespaceURI();
                if ( !CommonNamespaces.SLDNS.toString().equals( namespace ) ) {
                    continue;
                }

                String childName = child.getLocalName();

                if ( childName.equals( "Rule" ) ) {
                    Rule rule = createRule( child );
                    if ( rule.hasElseFilter() ) {
                        elseRules.add( rule );
                    } else if ( rule.getFilter() == null || rule.getFilter() instanceof ComplexFilter ) {
                        filters.add( rule.getFilter() );
                    }
                    ruleList.add( rule );
                } else if ( childName.equals( "SemanticTypeIdentifier" ) ) {
                    typeIdentifierList.add( XMLTools.getStringValue( child ) );
                }
            }
        }

        // compute and set the ElseFilter for all ElseFilter-Rules
        Filter elseFilter = null;
        // a Rule exists with no Filter at all -> elseFilter = false
        if ( filters.contains( null ) ) {
            elseFilter = new FalseFilter();
            // one Rule with a Filter exists -> elseFilter = NOT Filter
        } else if ( filters.size() == 1 ) {
            elseFilter = new ComplexFilter( OperationDefines.NOT );
            List<Operation> arguments = ( (LogicalOperation) ( (ComplexFilter) elseFilter ).getOperation() ).getArguments();
            ComplexFilter complexFilter = (ComplexFilter) filters.get( 0 );
            arguments.add( complexFilter.getOperation() );
            // several Rules with Filters exist -> elseFilter = NOT (Filter1 OR Filter2 OR...)
        } else if ( filters.size() > 1 ) {
            ComplexFilter innerFilter = new ComplexFilter( OperationDefines.OR );
            elseFilter = new ComplexFilter( innerFilter, null, OperationDefines.NOT );
            List<Operation> arguments = ( (LogicalOperation) innerFilter.getOperation() ).getArguments();
            Iterator<Filter> it = filters.iterator();
            while ( it.hasNext() ) {
                ComplexFilter complexFilter = (ComplexFilter) it.next();
                arguments.add( complexFilter.getOperation() );
            }
        }
        Iterator<Rule> it = elseRules.iterator();
        while ( it.hasNext() ) {
            it.next().setFilter( elseFilter );
        }

        Rule[] rules = ruleList.toArray( new Rule[ruleList.size()] );
        String[] typeIdentifiers = typeIdentifierList.toArray( new String[typeIdentifierList.size()] );

        return new FeatureTypeStyle( name, title, abstract_, featureTypeName, typeIdentifiers, rules );
    }

    /**
     * Creates a <tt>Rule</tt>-instance according to the contents of the DOM-subtree starting at the given 'Rule'-
     * <tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'Rule'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Rule</tt>-instance
     */
    private static Rule createRule( Element element )
                            throws XMLParsingException {
        // optional: <Name>
        String name = XMLTools.getStringValue( "Name", CommonNamespaces.SLDNS, element, null );
        // optional: <Title>
        String title = XMLTools.getStringValue( "Title", CommonNamespaces.SLDNS, element, null );
        // optional: <Abstract>
        String abstract_ = XMLTools.getStringValue( "Abstract", CommonNamespaces.SLDNS, element, null );

        // optional: <LegendGraphic>
        LegendGraphic legendGraphic = null;
        Element legendGraphicElement = XMLTools.getChildElement( "LegendGraphic", CommonNamespaces.SLDNS, element );

        if ( legendGraphicElement != null ) {
            legendGraphic = createLegendGraphic( legendGraphicElement );
        }

        // optional: <Filter>
        boolean isAnElseFilter = false;
        Filter filter = null;
        Element filterElement = XMLTools.getChildElement( "Filter", ogcNS, element );
        if ( filterElement != null ) {
            filter = AbstractFilter.buildFromDOM( filterElement );
        }

        // optional: <ElseFilter>
        Element elseFilterElement = XMLTools.getChildElement( "ElseFilter", CommonNamespaces.SLDNS, element );
        if ( elseFilterElement != null ) {
            isAnElseFilter = true;
        }

        if ( ( filterElement != null ) && ( elseFilterElement != null ) ) {
            throw new XMLParsingException( "Element 'Rule' may contain a 'Filter'- or "
                                           + "an 'ElseFilter'-element, but not both!" );
        }

        // optional: <MinScaleDenominator>
        double min = XMLTools.getNodeAsDouble( element, "sld:MinScaleDenominator", nsContext, 0.0 );
        // optional: <MaxScaleDenominator>
        double max = XMLTools.getNodeAsDouble( element, "sld:MaxScaleDenominator", nsContext, 9E99 );

        // optional: different Symbolizer-elements
        NodeList symbolizerNL = element.getChildNodes();
        ArrayList<Symbolizer> symbolizerList = new ArrayList<Symbolizer>( symbolizerNL.getLength() );

        for ( int i = 0; i < symbolizerNL.getLength(); i++ ) {
            if ( symbolizerNL.item( i ) instanceof Element ) {
                Element symbolizerElement = (Element) symbolizerNL.item( i );
                String namespace = symbolizerElement.getNamespaceURI();

                if ( !CommonNamespaces.SLDNS.toString().equals( namespace )
                     && !CommonNamespaces.SENS.toString().equals( namespace ) ) {
                    continue;
                }

                String symbolizerName = symbolizerElement.getLocalName();

                if ( symbolizerName.equals( "LineSymbolizer" ) ) {
                    symbolizerList.add( createLineSymbolizer( symbolizerElement, min, max ) );
                } else if ( symbolizerName.equals( "PointSymbolizer" ) ) {
                    symbolizerList.add( createPointSymbolizer( symbolizerElement, min, max ) );
                } else if ( symbolizerName.equals( "PolygonSymbolizer" ) ) {
                    symbolizerList.add( createPolygonSymbolizer( symbolizerElement, min, max ) );
                } else if ( symbolizerName.equals( "TextSymbolizer" ) ) {
                    symbolizerList.add( createTextSymbolizer( symbolizerElement, min, max ) );
                } else if ( symbolizerName.equals( "RasterSymbolizer" ) ) {
                    symbolizerList.add( createRasterSymbolizer( symbolizerElement, min, max ) );
                }
            }
        }

        Symbolizer[] symbolizers = symbolizerList.toArray( new Symbolizer[symbolizerList.size()] );

        return new Rule( symbolizers, name, title, abstract_, legendGraphic, filter, isAnElseFilter, min, max );
    }

    /**
     * 
     * @param symbolizerElement
     * @return {@link Symbolizer}
     * @throws XMLParsingException
     */
    public static Symbolizer createSymbolizer( Element symbolizerElement )
                            throws XMLParsingException {
        String symbolizerName = symbolizerElement.getLocalName();

        double min = 0;
        double max = Double.MAX_VALUE;
        Symbolizer symbolizer = null;
        if ( symbolizerName.equals( "LineSymbolizer" ) ) {
            symbolizer = createLineSymbolizer( symbolizerElement, min, max );
        } else if ( symbolizerName.equals( "PointSymbolizer" ) ) {
            symbolizer = createPointSymbolizer( symbolizerElement, min, max );
        } else if ( symbolizerName.equals( "PolygonSymbolizer" ) ) {
            symbolizer = createPolygonSymbolizer( symbolizerElement, min, max );
        } else if ( symbolizerName.equals( "TextSymbolizer" ) ) {
            symbolizer = createTextSymbolizer( symbolizerElement, min, max );
        } else if ( symbolizerName.equals( "RasterSymbolizer" ) ) {
            symbolizer = createRasterSymbolizer( symbolizerElement, min, max );
        }
        return symbolizer;
    }

    /**
     * Creates a <tt>PointSymbolizer</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'PointSymbolizer'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'PointSymbolizer'-<tt>Element</tt>
     * @param min
     *            scale-constraint to be used
     * @param max
     *            scale-constraint to be used
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>PointSymbolizer</tt>-instance
     */
    private static PointSymbolizer createPointSymbolizer( Element element, double min, double max )
                            throws XMLParsingException {

        // optional: <Geometry>
        Geometry geometry = null;
        Element geometryElement = XMLTools.getChildElement( "Geometry", CommonNamespaces.SLDNS, element );

        if ( geometryElement != null ) {
            geometry = createGeometry( geometryElement );
        }

        // optional: <Graphic>
        Graphic graphic = null;
        Element graphicElement = XMLTools.getChildElement( "Graphic", CommonNamespaces.SLDNS, element );

        if ( graphicElement != null ) {
            graphic = createGraphic( graphicElement );
        }

        PointSymbolizer ps = null;
        String respClass = XMLTools.getAttrValue( element, null, "responsibleClass", null );
        if ( respClass == null ) {
            ps = new PointSymbolizer( graphic, geometry, min, max );
        } else {
            ps = new PointSymbolizer( graphic, geometry, respClass, min, max );
        }

        return ps;
    }

    /**
     * Creates a <tt>LineSymbolizer</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'LineSymbolizer'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'LineSymbolizer'-<tt>Element</tt>
     * @param min
     *            scale-constraint to be used
     * @param max
     *            scale-constraint to be used
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>LineSymbolizer</tt>-instance
     */
    private static LineSymbolizer createLineSymbolizer( Element element, double min, double max )
                            throws XMLParsingException {

        // optional: <Geometry>
        Geometry geometry = null;
        Element geometryElement = XMLTools.getChildElement( "Geometry", CommonNamespaces.SLDNS, element );

        if ( geometryElement != null ) {
            geometry = createGeometry( geometryElement );
        }

        // optional: <Stroke>
        Stroke stroke = null;
        Element strokeElement = XMLTools.getChildElement( "Stroke", CommonNamespaces.SLDNS, element );

        if ( strokeElement != null ) {
            stroke = createStroke( strokeElement );
        }

        LineSymbolizer ls = null;
        String respClass = XMLTools.getAttrValue( element, null, "responsibleClass", null );
        if ( respClass == null ) {
            ls = new LineSymbolizer( stroke, geometry, min, max );
        } else {
            ls = new LineSymbolizer( stroke, geometry, respClass, min, max );
        }
        return ls;
    }

    /**
     * Creates a <tt>PolygonSymbolizer</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'PolygonSymbolizer'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'PolygonSymbolizer'-<tt>Element</tt>
     * @param min
     *            scale-constraint to be used
     * @param max
     *            scale-constraint to be used
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>PolygonSymbolizer</tt>-instance
     */
    private static PolygonSymbolizer createPolygonSymbolizer( Element element, double min, double max )
                            throws XMLParsingException {
        // optional: <Geometry>
        Geometry geometry = null;
        Element geometryElement = XMLTools.getChildElement( "Geometry", CommonNamespaces.SLDNS, element );

        if ( geometryElement != null ) {
            geometry = createGeometry( geometryElement );
        }

        // optional: <Fill>
        Fill fill = null;
        Element fillElement = XMLTools.getChildElement( "Fill", CommonNamespaces.SLDNS, element );

        if ( fillElement != null ) {
            fill = createFill( fillElement );
        }

        // optional: <Stroke>
        Stroke stroke = null;
        Element strokeElement = XMLTools.getChildElement( "Stroke", CommonNamespaces.SLDNS, element );

        if ( strokeElement != null ) {
            stroke = createStroke( strokeElement );
        }

        PolygonSymbolizer ps = null;
        String respClass = XMLTools.getAttrValue( element, null, "responsibleClass", null );
        if ( respClass == null ) {
            ps = new PolygonSymbolizer( fill, stroke, geometry, min, max );
        } else {
            ps = new PolygonSymbolizer( fill, stroke, geometry, respClass, min, max );
        }

        return ps;
    }

    /**
     * Creates a <tt>Geometry</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'Geometry'-<tt>Element</tt>.
     * 
     * @param element
     *            the 'Geometry'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Geometry</tt>-instance
     */
    private static Geometry createGeometry( Element element )
                            throws XMLParsingException {
        Geometry geometry = null;

        // required: <PropertyName>
        Element propertyNameElement = XMLTools.getRequiredChildElement( "PropertyName", ogcNS, element );

        // optional: <Function>
        Element functionElement = XMLTools.getChildElement( "Function", ogcNS, propertyNameElement );

        // just a property name exists
        if ( functionElement == null ) {
            Node node = XMLTools.getNode( propertyNameElement, "/text()", nsContext );
            PropertyPath pp = OGCDocument.parsePropertyPath( (Text) node );
            geometry = new Geometry( pp, null );
        } else {
            // expressions are not supported
        }

        return geometry;
    }

    /**
     * Creates a <tt>Fill</tt>-instance according to the contents of the DOM-subtree starting at the given 'Fill'-
     * <tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'Fill'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Fill</tt>-instance
     */
    private static Fill createFill( Element element )
                            throws XMLParsingException {
        // optional: <GraphicFill>
        GraphicFill graphicFill = null;
        Element graphicFillElement = XMLTools.getChildElement( "GraphicFill", CommonNamespaces.SLDNS, element );

        if ( graphicFillElement != null ) {
            graphicFill = createGraphicFill( graphicFillElement );
        }

        // optional: <CssParameter>s
        ElementList nl = XMLTools.getChildElements( "CssParameter", CommonNamespaces.SLDNS, element );
        HashMap<String, Object> cssParams = new HashMap<String, Object>( nl.getLength() );

        for ( int i = 0; i < nl.getLength(); i++ ) {
            CssParameter cssParam = createCssParameter( nl.item( i ) );
            cssParams.put( cssParam.getName(), cssParam );
        }

        return new Fill( cssParams, graphicFill );
    }

    /**
     * Creates a <tt>LegendGraphic</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'LegendGraphic'-element.
     * <p>
     * 
     * @param element
     *            the 'LegendGraphic'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Graphic</tt>-instance
     */
    private static LegendGraphic createLegendGraphic( Element element )
                            throws XMLParsingException {
        // required: <Graphic>
        Element graphicElement = XMLTools.getRequiredChildElement( "Graphic", CommonNamespaces.SLDNS, element );
        Graphic graphic = createGraphic( graphicElement );

        return new LegendGraphic( graphic );
    }

    /**
     * Creates an <tt>ExternalGraphic</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'ExternalGraphic'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'ExternalGraphic'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>ExternalGraphic</tt>-instance
     */
    private static ExternalGraphic createExternalGraphic( Element element )
                            throws XMLParsingException {
        // required: <OnlineResource>
        Element onlineResourceElement = XMLTools.getRequiredChildElement( "OnlineResource", CommonNamespaces.SLDNS,
                                                                          element );

        // required: href-Attribute (in <OnlineResource>)
        String href = XMLTools.getRequiredAttrValue( "href", xlnNS, onlineResourceElement );
        String title = XMLTools.getAttrValue( onlineResourceElement, xlnNS, "title", null );
        URL url = null;
        try {
            url = sldDoc.resolve( href );
        } catch ( MalformedURLException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new XMLParsingException( "Value ('" + href + "') of attribute 'href' of "
                                           + "element 'OnlineResoure' does not denote a valid URL" );
        }

        // required: <Format> (in <OnlineResource>)
        String format = XMLTools.getRequiredStringValue( "Format", CommonNamespaces.SLDNS, element );
        

        return new ExternalGraphic( format, url, title );
    }

    /**
     * Creates a <tt>Mark</tt>-instance according to the contents of the DOM-subtree starting at the given 'Mark'-
     * <tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'Mark'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Mark</tt>-instance
     */
    private static Mark createMark( Element element )
                            throws XMLParsingException {
        Stroke stroke = null;
        Fill fill = null;

        // optional: <WellKnownName>
        String wkn = XMLTools.getStringValue( "WellKnownName", CommonNamespaces.SLDNS, element, null );

        // optional: <Stroke>
        Element strokeElement = XMLTools.getChildElement( "Stroke", CommonNamespaces.SLDNS, element );

        if ( strokeElement != null ) {
            stroke = createStroke( strokeElement );
        }

        // optional: <Fill>
        Element fillElement = XMLTools.getChildElement( "Fill", CommonNamespaces.SLDNS, element );

        if ( fillElement != null ) {
            fill = createFill( fillElement );
        }

        return new Mark( wkn, stroke, fill );
    }

    /**
     * Creates a <tt>Stroke</tt>-instance according to the contents of the DOM-subtree starting at the given 'Stroke'-
     * <tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'Stroke'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Stroke</tt>-instance
     */
    private static Stroke createStroke( Element element )
                            throws XMLParsingException {
        GraphicFill gf = null;
        GraphicStroke gs = null;

        // optional: <GraphicFill>
        Element gfElement = XMLTools.getChildElement( "GraphicFill", CommonNamespaces.SLDNS, element );

        if ( gfElement != null ) {
            gf = createGraphicFill( gfElement );
        }

        // optional: <GraphicStroke>
        Element gsElement = XMLTools.getChildElement( "GraphicStroke", CommonNamespaces.SLDNS, element );

        if ( gsElement != null ) {
            gs = createGraphicStroke( gsElement );
        }

        // optional: <CssParameter>s
        ElementList nl = XMLTools.getChildElements( "CssParameter", CommonNamespaces.SLDNS, element );
        HashMap<String, Object> cssParams = new HashMap<String, Object>( nl.getLength() );

        for ( int i = 0; i < nl.getLength(); i++ ) {
            CssParameter cssParam = createCssParameter( nl.item( i ) );
            cssParams.put( cssParam.getName(), cssParam );
        }

        return new Stroke( cssParams, gs, gf );
    }

    /**
     * Creates a <tt>GraphicFill</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'GraphicFill'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'GraphicFill'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>GraphicFill</tt>-instance
     */
    private static GraphicFill createGraphicFill( Element element )
                            throws XMLParsingException {
        // required: <Graphic>
        Element graphicElement = XMLTools.getRequiredChildElement( "Graphic", CommonNamespaces.SLDNS, element );
        Graphic graphic = createGraphic( graphicElement );

        return new GraphicFill( graphic );
    }

    /**
     * Creates a <tt>GraphicStroke</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'GraphicStroke'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'GraphicStroke'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>GraphicStroke</tt>-instance
     */
    private static GraphicStroke createGraphicStroke( Element element )
                            throws XMLParsingException {
        // required: <Graphic>
        Element graphicElement = XMLTools.getRequiredChildElement( "Graphic", CommonNamespaces.SLDNS, element );
        Graphic graphic = createGraphic( graphicElement );

        return new GraphicStroke( graphic );
    }

    /**
     * Creates a <tt>Graphic</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'Graphic'-element.
     * <p>
     * 
     * @param element
     *            the 'Graphic'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Graphic</tt>-instance
     */
    private static Graphic createGraphic( Element element )
                            throws XMLParsingException {

        // optional: <Opacity>
        ParameterValueType opacity = null;
        // optional: <Size>
        ParameterValueType size = null;
        // optional: <Rotation>
        ParameterValueType rotation = null;
        // optional: <Displacement>
        ParameterValueType[] displacement = null;

        // optional: <ExternalGraphic>s / <Mark>s
        NodeList nodelist = element.getChildNodes();
        ArrayList<Object> marksAndExtGraphicsList = new ArrayList<Object>();

        for ( int i = 0; i < nodelist.getLength(); i++ ) {
            if ( nodelist.item( i ) instanceof Element ) {
                Element child = (Element) nodelist.item( i );
                String namespace = child.getNamespaceURI();

                if ( !CommonNamespaces.SLDNS.toString().equals( namespace ) ) {
                    continue;
                }

                String childName = child.getLocalName();

                if ( childName.equals( "ExternalGraphic" ) ) {
                    marksAndExtGraphicsList.add( createExternalGraphic( child ) );
                } else if ( childName.equals( "Mark" ) ) {
                    marksAndExtGraphicsList.add( createMark( child ) );
                } else if ( childName.equals( "Opacity" ) ) {
                    opacity = createParameterValueType( child );
                } else if ( childName.equals( "Size" ) ) {
                    size = createParameterValueType( child );
                } else if ( childName.equals( "Rotation" ) ) {
                    rotation = createParameterValueType( child );
                } else if ( childName.equals( "Displacement" ) ) {
                    displacement = new ParameterValueType[2];
                    Element dXElement = XMLTools.getRequiredElement( child, "sld:DisplacementX",
                                                                     CommonNamespaces.getNamespaceContext() );
                    Element dYElement = XMLTools.getRequiredElement( child, "sld:DisplacementY",
                                                                     CommonNamespaces.getNamespaceContext() );
                    if ( ( dXElement == null ) || ( dYElement == null ) ) {
                        throw new XMLParsingException( "Element 'Displacement' must contain exactly one "
                                                       + "'DisplacementX'- and one 'DisplacementY'-element!" );
                    }
                    displacement[0] = createParameterValueType( dXElement );
                    displacement[1] = createParameterValueType( dYElement );
                }
            }
        }

        Object[] marksAndExtGraphics = marksAndExtGraphicsList.toArray( new Object[marksAndExtGraphicsList.size()] );
        return new Graphic( marksAndExtGraphics, opacity, size, rotation, displacement );
    }

    /**
     * Creates a <tt>CssParameter</tt>-instance according to the contents of the DOM-subtree starting at the given
     * 'CssParameter'-<tt>Element</tt>.
     * <p>
     * 
     * @param element
     *            the 'CssParamter'-<tt>Element</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>CssParameter</tt>-instance
     */
    private static CssParameter createCssParameter( Element element )
                            throws XMLParsingException {
        // required: name-Attribute
        String name = XMLTools.getRequiredAttrValue( "name", null, element );
        ParameterValueType pvt = createParameterValueType( element );

        return ( new CssParameter( name, pvt ) );
    }

    /**
     * <code>TresholdsBelongTo</code> enumerates values possibly belonging to <code>ThreshholdsBelongToType</code>.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum ThresholdsBelongTo {
        /**
         * <code>"succeeding"</code>
         */
        SUCCEEDING,
        /**
         * <code>"preceding"</code>
         */
        PRECEDING
    }

    /**
     * <code>Mode</code> is the ModeType from the Symbology Encoding Schema.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum Mode {
        /**
         * <code>"linear"</code>
         */
        LINEAR,
        /**
         * <code>"cosine"</code>
         */
        COSINE,
        /**
         * <code>"cubic"</code>
         */
        CUBIC
    }

    /**
     * <code>Method</code> is the MethodType from the Symbology encoding Schema.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum Method {
        /**
         * <code>"numeric"</code>
         */
        NUMERIC,
        /**
         * <code>"color"</code>
         */
        COLOR
    }

}
