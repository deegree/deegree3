//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.gml;

import java.util.HashSet;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Geometry;
import org.deegree.gml.feature.GML2FeatureEncoder;
import org.deegree.gml.feature.GML3FeatureEncoder;
import org.deegree.gml.feature.GMLFeatureEncoder;
import org.deegree.gml.geometry.GML2GeometryEncoder;
import org.deegree.gml.geometry.GML3GeometryEncoder;
import org.deegree.gml.geometry.GMLGeometryEncoder;

/**
 * Stream-based writer for all kinds of GML objects supported by deegree.
 * <p>
 * Instances of this class are not thread-safe.
 * </p>
 * 
 * @see GMLObject
 * @see GMLOutputFactory
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLStreamWriter {

    private final GMLVersion version;

    private XMLStreamWriter xmlStream;

    private String indentString;

    private int inlineXLinklevels;

    private String localXLinkTemplate;

    private CRS crs;

    private String schemaLocation;

    private GMLGeometryEncoder geometryEncoder;

    private GMLFeatureEncoder featureEncoder;

    private PropertyName[] requestedFeatureProps;

    private int traverseXLinkExpiry;

    /**
     * Creates a new {@link GMLStreamWriter} instance.
     * 
     * @param version
     *            GML version of the output, must not be <code>null</code>
     * @param xmlStream
     *            XML stream used to write the output, must not be <code>null</code>
     */
    GMLStreamWriter( GMLVersion version, XMLStreamWriter xmlStream ) {
        this.version = version;
        this.xmlStream = xmlStream;
    }

    /**
     * Controls the indentation of the generated XML.
     * 
     * @param indentString
     *            string to be used for one level of indentation (must be some combination of whitespaces), can be
     *            <code>null</code> (turns off indentation)
     */
    public void setIndentation( String indentString ) {
        this.indentString = indentString;
    }

    /**
     * Controls the number of xlink levels that will be expanded inside property elements.
     * 
     * @param inlineXLinklevels
     *            number of xlink levels to be expanded, -1 expands to any depth
     */
    public void setXLinkExpansion( int inlineXLinklevels ) {
        this.inlineXLinklevels = inlineXLinklevels;
    }

    /**
     * Controls the representation of local xlinks.
     * 
     * @param localXLinkTemplate
     *            template used to create references to local objects, e.g.
     *            <code>http://localhost:8080/d3_wfs_lab/services?SERVICE=WFS&REQUEST=GetGmlObject&VERSION=1.1.0&TRAVERSEXLINKDEPTH=1&GMLOBJECTID={}</code>
     *            , the substring <code>{}</code> is replaced by the object id
     */
    public void setLocalXLinkTemplate( String localXLinkTemplate ) {
        this.localXLinkTemplate = localXLinkTemplate;
    }

    /**
     * Controls the output CRS for written geometries.
     * 
     * @param crs
     *            crs to be used for the geometries, can be <code>null</code> (keeps the original CRS)
     */
    public void setOutputCRS( CRS crs ) {
        this.crs = crs;
    }

    /**
     * Controls the value of the <code>xsi:schemaLocation</code> attribute in the root element.
     * 
     * @param schemaLocation
     *            value to be exported in the <code>xsi:schemaLocation</code> attribute in the root element, or
     *            <code>null</code> (no <code>xsi:schemaLocation</code> attribute will be exported)
     */
    public void setSchemaLocation( String schemaLocation ) {
        this.schemaLocation = schemaLocation;
    }

    /**
     * Controls the formatting of written coordinates in geometries.
     * 
     * @param formatter
     */
    public void setCoordinateFormatter( Object formatter ) {
        // TODO
    }

    /**
     * Writes a GML representation of the given {@link GMLObject} to the stream.
     * 
     * @param object
     *            object to be written, must not be <code>null</code>
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void write( GMLObject object )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( object instanceof Feature ) {
            write( (Feature) object );
        } else if ( object instanceof Geometry ) {
            write( (Geometry) object );
        } else {
            throw new XMLStreamException( "Unhandled GMLObject: " + object );
        }
    }

    /**
     * Writes a GML representation of the given {@link Feature} to the stream.
     * 
     * @param feature
     *            object to be written, must not be <code>null</code>
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */    
    public void write( Feature feature )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        getFeatureEncoder().export( feature );
    }

    /**
     * Writes a GML representation of the given {@link Geometry} to the stream.
     * 
     * @param geometry
     *            object to be written, must not be <code>null</code>
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */    
    public void write( Geometry geometry )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        getGeometryEncoder().export( geometry );
    }

    /**
     * Writes a GML representation of the given {@link CRS} to the stream.
     * 
     * @param crs
     *            object to be written, must not be <code>null</code>
     * @throws XMLStreamException
     */    
    public void write( CRS crs )
                            throws XMLStreamException {
        throw new UnsupportedOperationException( "Writing of crs is not implemented yet." );
    }

    /**
     * Closes the underlying XML stream.
     * 
     * @throws XMLStreamException
     */
    public void close()
                            throws XMLStreamException {
        xmlStream.close();
    }

    private GMLFeatureEncoder getFeatureEncoder() {
        if ( featureEncoder == null ) {
            switch ( version ) {
            case GML_2: {
                // TODO
                featureEncoder = new GML2FeatureEncoder( xmlStream, crs );
                break;
            }
            case GML_30:
            case GML_31:
            case GML_32: {
                // TODO
                featureEncoder = new GML3FeatureEncoder( version, xmlStream, crs, localXLinkTemplate,
                                                         requestedFeatureProps, inlineXLinklevels, traverseXLinkExpiry,
                                                         false );
                break;
            }
            }
        }
        return featureEncoder;
    }

    private GMLGeometryEncoder getGeometryEncoder() {
        if ( geometryEncoder == null ) {
            switch ( version ) {
            case GML_2: {
                // TODO
                geometryEncoder = new GML2GeometryEncoder( xmlStream );
                break;
            }
            case GML_30:
            case GML_31:
            case GML_32: {
                // TODO
                geometryEncoder = new GML3GeometryEncoder( version, xmlStream, crs, false, new HashSet<String>() );
                break;
            }
            }
        }
        return geometryEncoder;
    }
}
