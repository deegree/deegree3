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

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.gml.feature.GMLFeatureDecoder;
import org.deegree.gml.geometry.GML21GeometryDecoder;
import org.deegree.gml.geometry.GML311GeometryDecoder;
import org.deegree.gml.geometry.GMLGeometryDecoder;

/**
 * Stream-based reader for all kinds of GML objects supported by deegree.
 * <p>
 * Instances of this class are not thread-safe.
 * </p>
 * 
 * @see GMLObject
 * @see GMLInputFactory
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLStreamReader {

    private final XMLStreamReaderWrapper xmlStream;

    private final GMLVersion version;
    
    private final GMLDocumentIdContext idContext;

    private ApplicationSchema schema;

    private CRS defaultCRS;

    private GMLGeometryDecoder geometryDecoder;

    private GMLFeatureDecoder featureDecoder;

    /**
     * Creates a new {@link GMLStreamReader} instance.
     * 
     * @param version
     *            GML version of the input, must not be <code>null</code>
     * @param xmlStream
     *            XML stream used to read the input, must not be <code>null</code>
     */
    GMLStreamReader( GMLVersion version, XMLStreamReaderWrapper xmlStream ) {
        this.version = version;
        this.xmlStream = xmlStream;
        this.idContext = new GMLDocumentIdContext( version );
    }

    /**
     * Sets the application schema that is assumed when features or feature collections are parsed.
     * 
     * @param schema
     *            application schema
     */
    public void setApplicationSchema( ApplicationSchema schema ) {
        this.schema = schema;
    }

    /**
     * Sets the default CRS that is assumed when GML objects (especially geometries) without SRS information are parsed.
     * 
     * @param defaultCRS
     *            default CRS
     */
    public void setDefaultCRS( CRS defaultCRS ) {
        this.defaultCRS = defaultCRS;
    }

    /**
     * Returns the deegree model representation for the GML object element event that the cursor of the underlying xml
     * stream points to.
     * 
     * @return deegree model representation for the current GML object element
     * @throws XMLStreamException
     */
    public GMLObject read()
                            throws XMLStreamException {
        throw new UnsupportedOperationException( "Automatic determination of GML objects types is not implemented yet." );
    }

    /**
     * Returns the deegree model representation for the GML object element event that the cursor of the underlying xml
     * stream points to.
     * 
     * @return deegree model representation for the current GML object element
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public Feature readFeature()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        return getFeatureDecoder().parseFeature( xmlStream, defaultCRS );
    }

    /**
     * Returns the deegree model representation for the GML object element event that the cursor of the underlying xml
     * stream points to.
     * 
     * @return deegree model representation for the current GML object element
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public Geometry readGeometry()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        return getGeometryDecoder().parse( xmlStream, defaultCRS );
    }

    /**
     * Returns the deegree model representation for the GML object element event that the cursor of the underlying xml
     * stream points to.
     * 
     * @return deegree model representation for the current GML object element
     * @throws XMLStreamException
     */
    public CRS readCRS()
                            throws XMLStreamException {
        throw new UnsupportedOperationException( "Reading of crs is not implemented yet." );
    }

    /**
     * Returns the {@link GMLDocumentIdContext} that keeps track of objects, identifiers and references.
     * 
     * @return the {@link GMLDocumentIdContext}, never <code>null</code>
     */
    public GMLDocumentIdContext getIdContext() {
        return idContext;
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

    private GMLFeatureDecoder getFeatureDecoder() {
        if ( featureDecoder == null ) {
            featureDecoder = new GMLFeatureDecoder( schema, idContext, version );
        }
        return featureDecoder;
    }

    private GMLGeometryDecoder getGeometryDecoder() {
        if ( geometryDecoder == null ) {
            switch ( version ) {
            case GML_2: {
                geometryDecoder = new GML21GeometryDecoder( new GeometryFactory(), idContext );
                break;
            }
            case GML_30:
            case GML_31:
            case GML_32: {
                geometryDecoder = new GML311GeometryDecoder( new GeometryFactory(), idContext );
                break;
            }
            }
        }
        return geometryDecoder;
    }
}
