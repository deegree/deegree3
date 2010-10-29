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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.StreamFeatureCollection;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.gml.dictionary.Dictionary;
import org.deegree.gml.dictionary.GMLDictionaryReader;
import org.deegree.gml.feature.GMLFeatureReader;
import org.deegree.gml.geometry.GML2GeometryReader;
import org.deegree.gml.geometry.GML3GeometryReader;
import org.deegree.gml.geometry.GMLGeometryReader;

/**
 * Stream-based reader for GML instance documents or GML fragments.
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

    private GMLReferenceResolver resolver;

    private ApplicationSchema schema;

    private CRS defaultCRS;

    private int defaultCoordDim = 2;

    private GMLGeometryReader geometryReader;

    private GMLFeatureReader featureReader;

    private GMLDictionaryReader dictReader;

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
     * Controls the application schema that is assumed when features or feature collections are parsed.
     * 
     * @param schema
     *            application schema, can be <code>null</code> (use xsi:schemaLocation attribute to build the
     *            application schema)
     */
    public void setApplicationSchema( ApplicationSchema schema ) {
        this.schema = schema;
        idContext.setApplicationSchema( schema );
    }

    /**
     * Controls the default CRS that is assumed when GML objects (especially geometries) without SRS information are
     * parsed.
     * 
     * @param defaultCRS
     *            default CRS, can be <code>null</code>
     */
    public void setDefaultCRS( CRS defaultCRS ) {
        this.defaultCRS = defaultCRS;
    }

    /**
     * Controls the {@link GeometryFactory} instance to be used for creating geometries.
     * 
     * @param geomFac
     *            geometry factory, can be <code>null</code> (use a default factory)
     */
    public void setGeometryFactory( GeometryFactory geomFac ) {
        switch ( version ) {
        case GML_2: {
            geometryReader = new GML2GeometryReader( geomFac, idContext );
            break;
        }
        case GML_30:
        case GML_31:
        case GML_32: {
            geometryReader = new GML3GeometryReader( version, geomFac, idContext, defaultCoordDim );
            break;
        }
        }
    }

    /**
     * @return true if the stream's event is an {@link XMLStreamConstants#START_ELEMENT} && the current element's name
     *         is a known geometry (with respect to it's gml version).
     */
    public boolean isGeometryElement() {
        GMLGeometryReader geomReader = getGeometryReader();
        return geomReader.isGeometryElement( getXMLReader() );
    }

    /**
     * @return true if the stream's event is an {@link XMLStreamConstants#START_ELEMENT} && the current element's name
     *         is a known geometry (with respect to it's gml version).
     */
    public boolean isGeometryOrEnvelopeElement() {
        GMLGeometryReader geomReader = getGeometryReader();
        return geomReader.isGeometryOrEnvelopeElement( getXMLReader() );
    }

    /**
     * Sets the {@link GMLReferenceResolver} that the generated {@link GMLReference}s will use for resolving
     * themselves.
     * 
     * @param resolver
     */
    public void setResolver( GMLReferenceResolver resolver ) {
        this.resolver = resolver;
    }

    /**
     * Returns the deegree model representation for the GML object element event that the cursor of the underlying xml
     * stream points to.
     * 
     * @return deegree model representation for the current GML object element, never <code>null</code>
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws XMLParsingException
     */
    public GMLObject read()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        GMLObject object = null;
        QName elName = xmlStream.getName();
        if ( schema != null && schema.getFeatureType( elName ) != null ) {
            object = readFeature();
        } else {
            // TODO
            object = readGeometry();
        }
        return object;
    }

    /**
     * Returns the deegree model representation for the GML feature element event that the cursor of the underlying xml
     * stream points to.
     * @return deegree model representation for the current GML feature element, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public Feature readFeature()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        return getFeatureReader().parseFeature( xmlStream, defaultCRS );
    }

    /**
     * Returns the deegree model representation for the GML feature collection element event that the cursor of the
     * underlying xml stream points to.
     * <p>
     * Please note that {@link #readStreamFeatureCollection()} should be preferred (especially for large feature
     * collections), because it does not build and store all features in memory at once.
     * </p>
     * 
     * @return deegree model representation for the current GML feature collection element, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public FeatureCollection readFeatureCollection()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        return (FeatureCollection) getFeatureReader().parseFeature( xmlStream, defaultCRS );
    }

    /**
     * Returns the deegree model representation for the GML feature collection element event that the cursor of the
     * underlying xml stream points to.
     * <p>
     * This method does not automatically consume all events from the underlying XML stream. Instead, it allows the
     * caller to control the consumption by iterating over the features in the returned collection.
     * </p>
     * 
     * @return deegree model representation for the current GML feature collection element, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public StreamFeatureCollection readStreamFeatureCollection()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        return null;
    }

    /**
     * Returns the deegree model representation for the GML geometry element event that the cursor of the underlying xml
     * stream points to.
     * 
     * @return deegree model representation for the current GML geometry element, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public Geometry readGeometryOrEnvelope()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        return getGeometryReader().parseGeometryOrEnvelope( xmlStream, defaultCRS );
    }

    /**
     * Returns the deegree model representation for the GML geometry element event that the cursor of the underlying xml
     * stream points to.
     * 
     * @return deegree model representation for the current GML geometry element, never <code>null</code>
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public Geometry readGeometry()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {
        return getGeometryReader().parse( xmlStream, defaultCRS );
    }

    /**
     * Returns the deegree model representation for the GML dictionary element event that the cursor of the underlying
     * xml stream points to.
     * 
     * @return deegree model representation for the current GML dictionary element, never <code>null</code>
     * @throws XMLStreamException
     */
    public Dictionary readDictionary()
                            throws XMLStreamException {
        return getDictionaryReader().readDictionary();
    }

    /**
     * Returns the deegree model representation for the GML crs element event that the cursor of the underlying xml
     * stream points to.
     * 
     * @return deegree model representation for the current GML crs element, never <code>null</code>
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
     * Returns the underlying {@link XMLStreamReader}.
     * 
     * @return the underlying {@link XMLStreamReader}, never <code>null</code>
     */
    public XMLStreamReader getXMLReader() {
        return xmlStream;
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

    /**
     * Returns a configured {@link GMLFeatureReader} instance for calling specific feature parsing methods.
     * 
     * @return a configured {@link GMLFeatureReader} instance, never <code>null</code>
     */
    public GMLFeatureReader getFeatureReader() {
        if ( featureReader == null ) {
            featureReader = new GMLFeatureReader( version, schema, idContext, defaultCoordDim, resolver );
            if ( geometryReader != null ) {
                featureReader.setGeometryReader( geometryReader );
            }
        }
        return featureReader;
    }

    /**
     * Returns a configured {@link GMLGeometryReader} instance for calling specific geometry parsing methods.
     * 
     * @return a configured {@link GMLGeometryReader} instance, never <code>null</code>
     */
    public GMLGeometryReader getGeometryReader() {
        if ( geometryReader == null ) {
            switch ( version ) {
            case GML_2: {
                geometryReader = new GML2GeometryReader( new GeometryFactory(), idContext );
                break;
            }
            case GML_30:
            case GML_31:
            case GML_32: {
                geometryReader = new GML3GeometryReader( version, new GeometryFactory(), idContext, defaultCoordDim );
                break;
            }
            }
        }
        return geometryReader;
    }

    /**
     * Returns a configured {@link GMLDictionaryReader} instance for calling specific dictionary parsing methods.
     * 
     * @return a configured {@link GMLDictionaryReader} instance, never <code>null</code>
     */
    public GMLDictionaryReader getDictionaryReader() {
        if ( dictReader == null ) {
            dictReader = new GMLDictionaryReader( version, xmlStream, idContext );
        }
        return dictReader;
    }
}