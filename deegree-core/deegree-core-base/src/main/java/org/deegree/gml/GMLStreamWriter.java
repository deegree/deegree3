//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.feature.property.ExtraProps.EXTRA_PROP_NS;
import static org.deegree.feature.property.ExtraProps.EXTRA_PROP_NS_GEOMETRY;
import static org.deegree.feature.property.ExtraProps.EXTRA_PROP_NS_STRING;
import static org.deegree.gml.GMLVersion.GML_32;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.property.ExtraProps;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.SFSProfiler;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.gml.dictionary.Definition;
import org.deegree.gml.dictionary.GMLDictionaryWriter;
import org.deegree.gml.feature.GMLFeatureWriter;
import org.deegree.gml.geometry.GML2GeometryWriter;
import org.deegree.gml.geometry.GML3GeometryWriter;
import org.deegree.gml.geometry.GMLGeometryWriter;
import org.deegree.gml.reference.DefaultGmlXlinkStrategy;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.gml.reference.GmlXlinkStrategy;

/**
 * Stream-based writer for GML instance documents or GML document fragments.
 * <p>
 * Instances of this class are not thread-safe.
 * </p>
 * <p>
 * TODO: Refactor, so configuration settings cannot be modified after creation (e.g. like in XMLOutputFactory).
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

    private final XMLStreamWriter xmlStream;

    private GmlXlinkStrategy referenceExportStrategy;

    private ICRS crs;

    private CoordinateFormatter formatter;

    private SFSProfiler geometrySimplifier;

    private GMLGeometryWriter geometryWriter;

    private GMLFeatureWriter featureWriter;

    private GMLDictionaryWriter dictionaryWriter;

    private Map<QName, List<ProjectionClause>> projections;

    private final Map<String, String> prefixToNs = new HashMap<String, String>();

    private boolean exportExtraProps;

    private boolean outputGeometries = true;

    private boolean exportBoundedByForFeatures;

    /**
     * Creates a new {@link GMLStreamWriter} instance.
     * 
     * @param version
     *            GML version of the output, must not be <code>null</code>
     * @param xmlStream
     *            XML stream used to write the output, must not be <code>null</code>
     * @throws XMLStreamException
     */
    GMLStreamWriter( GMLVersion version, XMLStreamWriter xmlStream ) throws XMLStreamException {
        this.version = version;
        this.xmlStream = xmlStream;
        referenceExportStrategy = new DefaultGmlXlinkStrategy( "#{}", new GmlXlinkOptions() );
        prefixToNs.put( "ogc", OGCNS );
        prefixToNs.put( "gml", version != GML_32 ? GMLNS : GML3_2_NS );
        prefixToNs.put( "xlink", XLNNS );
        prefixToNs.put( "xsi", XSINS );
        prefixToNs.put( "dxtra", EXTRA_PROP_NS );
        prefixToNs.put( "dxtra-string", EXTRA_PROP_NS_STRING );
        prefixToNs.put( "dxtra-geometry", EXTRA_PROP_NS_GEOMETRY );
    }

    /**
     * Returns the GML version of the generated output.
     * 
     * @return GML version of the generated output, never <code>null</code>
     */
    public GMLVersion getVersion() {
        return version;
    }

    /**
     * Returns the CRS used for writing geometries.
     * 
     * @return CRS used for writing geometries, can be <code>null</code> (keeps the original CRS of the geometries)
     */
    public ICRS getOutputCrs() {
        return crs;
    }

    /**
     * Controls the output CRS for written geometries.
     * 
     * @param crs
     *            crs to be used for the geometries, can be <code>null</code> (keeps the original CRS)
     */
    public void setOutputCrs( ICRS crs ) {
        this.crs = crs;
    }

    /**
     * Returns the {@link CoordinateFormatter} used for writing geometry coordinates.
     * 
     * @return the coordinate formatter, can be <code>null</code> (use default formatting)
     */
    public CoordinateFormatter getCoordinateFormatter() {
        return formatter;
    }

    /**
     * Controls the format (e.g. number of decimal places) for written geometry coordinates.
     * 
     * @param formatter
     *            formatter to use, may be <code>null</code> (use default formatting)
     */
    public void setCoordinateFormatter( CoordinateFormatter formatter ) {
        this.formatter = formatter;
    }

    /**
     * Returns the namespace bindings that are used whenever a qualified element or attribute is written (and no
     * namespace prefix has been bound for the namespace on the stream already).
     * 
     * @return keys: prefix, value: namespace, may be <code>null</code>
     */
    public Map<String, String> getNamespaceBindings() {
        return prefixToNs;
    }

    /**
     * Controls the namespace bindings that are used whenever a qualified element or attribute is written (and no
     * namespace prefix has been bound for the namespace on the stream already).
     * 
     * @param prefixToNs
     *            keys: prefix, value: namespace, may be <code>null</code>
     */
    public void setNamespaceBindings( Map<String, String> prefixToNs ) {
        this.prefixToNs.putAll( prefixToNs );
    }

    /**
     * Returns the projections that are applied to exported {@link Feature} instances.
     * 
     * @return projections, or <code>null</code> (include all feature properties)
     */
    public Map<QName, List<ProjectionClause>> getProjections() {
        return projections;
    }

    /**
     * Sets the projections to be applied to exported {@link Feature} instances.
     *
     * @param projections
     *            projections, or <code>null</code> (include all feature properties)
     */
    public void setProjections(Map<QName, List<ProjectionClause>> projections ) {
        this.projections = projections;
    }

    /**
     * Returns the {@link GmlXlinkStrategy} that copes with creating xlinks.
     * 
     * @return strategy, never <code>null</code>
     */
    public GmlXlinkStrategy getReferenceResolveStrategy() {
        return referenceExportStrategy;
    }

    /**
     * Sets an {@link GmlXlinkStrategy} that copes with creating xlinks.
     * 
     * @param strategy
     *            handler, must not be <code>null</code>
     */
    public void setReferenceResolveStrategy( GmlXlinkStrategy strategy ) {
        this.referenceExportStrategy = strategy;
    }

    public boolean getOutputGeometries() {
        return outputGeometries;
    }

    public void setExportGeometries( boolean exportGeometries ) {
        this.outputGeometries = exportGeometries;
    }

    public boolean getExportExtraProps() {
        return exportExtraProps;
    }

    /**
     * Controls whether {@link ExtraProps} associated with feature objects should be generated as property elements.
     * 
     * @param exportExtraProps
     *            <code>true</code>, if extra props should be calculated, <code>false</code> otherwise
     */
    public void setExportExtraProps( boolean exportExtraProps ) {
        this.exportExtraProps = exportExtraProps;
    }

    /**
     * Returns whether <code>gml:boundedBy</code> properties should be generated for written {@link Feature} instances.
     * 
     * @return <code>true</code>, if <code>gml:boundedBy</code> shall be calculated, <code>false</code> otherwise
     */
    public boolean getGenerateBoundedByForFeatures() {
        return exportBoundedByForFeatures;
    }

    /**
     * Controls whether <code>gml:boundedBy</code> properties should be generated for written {@link Feature} instances.
     * 
     * @param exportBoundedBy
     *            <code>true</code>, if <code>gml:boundedBy</code> should be exported, <code>false</code> otherwise
     */
    public void setGenerateBoundedByForFeatures( boolean exportBoundedBy ) {
        this.exportBoundedByForFeatures = exportBoundedBy;
    }

    /**
     * Sets the {@link SFSProfiler} to be applied to exported {@link Geometry} instances.
     * <p>
     * NOTE: Simplification is performed before coordinate transformation, so constraints have to be expressed in the
     * native CRS of the geometries.
     * </p>
     * 
     * @param simplifier
     *            simplifier to use, can be <code>null</code> (no simplification performed)
     */
    public void setGeometrySimplifier( SFSProfiler simplifier ) {
        this.geometrySimplifier = simplifier;
    }

    /**
     * Returns the {@link SFSProfiler} that is be applied to exported {@link Geometry} instances.
     * <p>
     * NOTE: Simplification is performed before coordinate transformation, so constraints have to be expressed in the
     * native CRS of the geometries.
     * </p>
     * 
     * @return simplifier, can be <code>null</code> (no simplification performed)
     */
    public SFSProfiler getGeometrySimplifier() {
        return geometrySimplifier;
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
        } else if ( object instanceof Definition ) {
            write( (Definition) object );
        } else {
            throw new XMLStreamException( "Unhandled GMLObject: " + object );
        }
    }

    /**
     * Writes a GML representation of the given {@link Feature} to the stream.
     * 
     * @param feature
     *            feature to be written, must not be <code>null</code>
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void write( Feature feature )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        getFeatureWriter().export( feature );
    }

    /**
     * Writes a GML representation of the given {@link Geometry} to the stream.
     * 
     * @param geometry
     *            geometry to be written, must not be <code>null</code>
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    public void write( Geometry geometry )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        getGeometryWriter().export( geometry );
    }

    /**
     * Writes a GML representation of the given {@link Definition} to the stream.
     * 
     * @param definition
     *            object to be written, must not be <code>null</code>
     * @throws XMLStreamException
     */
    public void write( Definition definition )
                            throws XMLStreamException {
        getDictionaryWriter().write( definition );
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
     * Returns the underlying XML stream.
     * 
     * @return the underlying XML stream, never <code>null</code>
     */
    public XMLStreamWriter getXMLStream() {
        return xmlStream;
    }

    public GMLFeatureWriter getFeatureWriter() {
        if ( featureWriter == null ) {
            featureWriter = new GMLFeatureWriter( this );
        }
        return featureWriter;
    }

    public GMLGeometryWriter getGeometryWriter() {
        if ( geometryWriter == null ) {
            switch ( version ) {
            case GML_2: {
                geometryWriter = new GML2GeometryWriter( this );
                break;
            }
            case GML_30:
            case GML_31:
            case GML_32: {
                geometryWriter = new GML3GeometryWriter( this );
                break;
            }
            }
        }
        return geometryWriter;
    }

    private GMLDictionaryWriter getDictionaryWriter() {
        if ( dictionaryWriter == null ) {
            dictionaryWriter = new GMLDictionaryWriter( version, xmlStream );
        }
        return dictionaryWriter;
    }

}
