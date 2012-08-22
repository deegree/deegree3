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
package org.deegree.model.feature;

import static org.deegree.datatypes.Types.DATE;
import static org.deegree.datatypes.Types.FEATURE;
import static org.deegree.datatypes.Types.TIME;
import static org.deegree.datatypes.Types.TIMESTAMP;
import static org.deegree.framework.util.TimeTools.getISOFormattedTime;
import static org.deegree.framework.xml.XMLTools.escape;
import static org.deegree.model.spatialschema.GMLGeometryAdapter.swap;
import static org.deegree.ogcbase.CommonNamespaces.GMLNS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.XLinkPropertyPath;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Exports feature instances to their GML representation.
 * <p>
 * Has support for XLink output and to disable XLink output (which is generally not feasible).
 * <p>
 * Also responsible for "xlinking" features: if a feature occurs several times in a feature collection, it must be
 * exported only once - all other occurences must use xlink-attributes in the surrounding property element to reference
 * the feature.
 * 
 * TODO Handle FeatureCollections like ordinary Features (needs changes in feature model).
 * 
 * TODO Separate cycle check (for suppressXLinkOutput).
 * 
 * TODO Use a more straight-forward approach to export DOM representations.
 * 
 * TODO Handle multiple application schemas (in xsi:schemaLocation attribute).
 * 
 * TODO Handle WFS-schema-binding in a subclass in the WFS package?
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLFeatureAdapter {

    private static final ILogger LOG = LoggerFactory.getLogger( GMLFeatureAdapter.class );

    private String wfsSchemaBinding = "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd";

    // values: feature ids of already exported features (for XLinks)
    private Set<String> exportedFeatures = new HashSet<String>();

    // values: feature ids of all (sub-) features in a feature (to find cyclic features)
    private Set<String> localFeatures = new HashSet<String>();

    private boolean suppressXLinkOutput;

    private String schemaURL;

    // marks if namespace bindings have been appended already
    private boolean nsBindingsExported;

    private final DateFormat dateFormatter = new SimpleDateFormat( "yyyy-MM-dd" );

    private final DateFormat timeFormatter = new SimpleDateFormat( "HH:mm:ss" );

    private boolean printGeometryIds;

    private int xlinkDepth = -1;

    private HashMap<String, Integer> xlinkPropertyNames;

    private String baseUrl = "http://localhost:8080/wfs/services";

    /**
     * Creates a new <code>GMLFeatureAdapter</code> instance with enabled XLink output.
     */
    public GMLFeatureAdapter() {
        this.suppressXLinkOutput = false;
        this.xlinkPropertyNames = new HashMap<String, Integer>();
    }

    /**
     * @param xlinkdepth
     */
    public GMLFeatureAdapter( int xlinkdepth ) {
        xlinkDepth = xlinkdepth;
        this.xlinkPropertyNames = new HashMap<String, Integer>();
    }

    /**
     * Creates a new <code>GMLFeatureAdapter</code> instance with enabled XLink output and schema reference.
     * 
     * @param schemaURL
     *            URL of schema document (used as xsi:schemaLocation attribute in XML output)
     */
    public GMLFeatureAdapter( String schemaURL ) {
        this.suppressXLinkOutput = false;
        if ( schemaURL != null ) {
            this.schemaURL = StringTools.replace( schemaURL, "&", "&amp;", true );
        }
        this.xlinkPropertyNames = new HashMap<String, Integer>();
    }

    /**
     * @param suppressXLinkOutput
     * @param schemaURL
     * @param printGeometryIds
     */
    public GMLFeatureAdapter( boolean suppressXLinkOutput, String schemaURL, boolean printGeometryIds ) {
        this( suppressXLinkOutput, schemaURL );
        this.printGeometryIds = printGeometryIds;
    }

    /**
     * @param suppressXLinkOutput
     * @param schemaURL
     * @param printGeometryIds
     * @param depth
     *            the depth of xlinks to resolve
     */
    public GMLFeatureAdapter( boolean suppressXLinkOutput, String schemaURL, boolean printGeometryIds, int depth ) {
        this( suppressXLinkOutput, schemaURL, printGeometryIds );
        xlinkDepth = depth;
    }

    /**
     * Creates a new instance <code>GMLFeatureAdapter</code> with configurable XLink output.
     * 
     * @param suppressXLinkOutput
     *            set to true, if no XLinks shall be used
     */
    public GMLFeatureAdapter( boolean suppressXLinkOutput ) {
        this.suppressXLinkOutput = suppressXLinkOutput;
        this.xlinkPropertyNames = new HashMap<String, Integer>();
    }

    /**
     * Creates a new instance <code>GMLFeatureAdapter</code> with configurable XLink output.
     * 
     * @param suppressXLinkOutput
     *            set to true, if no XLinks shall be used
     * @param schemaURL
     *            URL of schema document (used as xsi:schemaLocation attribute in XML output)
     */
    public GMLFeatureAdapter( boolean suppressXLinkOutput, String schemaURL ) {
        this.suppressXLinkOutput = suppressXLinkOutput;
        if ( schemaURL != null ) {
            this.schemaURL = StringTools.replace( schemaURL, "&", "&amp;", true );
        }
        this.xlinkPropertyNames = new HashMap<String, Integer>();
    }

    /**
     * Creates a new instance <code>GMLFeatureAdapter</code> with configurable XLink output.
     * 
     * @param suppressXLinkOutput
     *            set to true, if no XLinks shall be used
     * @param schemaURL
     *            URL of schema document (used for "xsi:schemaLocation" attribute in XML output)
     * @param wfsSchemaBinding
     *            fragment for the "xsi:schemaLocation" attribute that binds the wfs namespace
     * @param printGeometryIds
     */
    public GMLFeatureAdapter( boolean suppressXLinkOutput, String schemaURL, String wfsSchemaBinding,
                              boolean printGeometryIds ) {
        this.printGeometryIds = printGeometryIds;
        this.suppressXLinkOutput = suppressXLinkOutput;
        if ( schemaURL != null ) {
            this.schemaURL = StringTools.replace( schemaURL, "&", "&amp;", true );
        }
        this.wfsSchemaBinding = wfsSchemaBinding;
        this.xlinkPropertyNames = new HashMap<String, Integer>();
    }

    /**
     * @param suppressXLinkOutput
     * @param schemaURL
     * @param wfsSchemaBinding
     * @param printGeometryIds
     * @param depth
     */
    public GMLFeatureAdapter( boolean suppressXLinkOutput, String schemaURL, String wfsSchemaBinding,
                              boolean printGeometryIds, int depth ) {
        this( suppressXLinkOutput, schemaURL, wfsSchemaBinding, printGeometryIds );
        xlinkDepth = depth;
    }

    /**
     * Sets the property paths for which special xlink traversal will be done.
     * 
     * @param paths
     */
    public void setPropertyPaths( Collection<PropertyPath[]> paths ) {
        this.xlinkPropertyNames = new HashMap<String, Integer>();

        if ( paths != null ) {
            for ( PropertyPath[] ps : paths ) {
                for ( PropertyPath p : ps ) {
                    if ( p instanceof XLinkPropertyPath ) {
                        xlinkPropertyNames.put( p.getStep( p.getSteps() - 1 ).getPropertyName().getPrefixedName(),
                                                ( (XLinkPropertyPath) p ).getXlinkDepth() );
                    }
                }
            }
        }
    }

    /**
     * @param url
     *            the base url to use for GetGmlObject references
     */
    public void setBaseURL( String url ) {
        this.baseUrl = url;
    }

    /**
     * Appends the DOM representation of the given feature to the also given <code>Node</code>.
     * <p>
     * TODO do this a better way (append nodes directly without serializing to string and parsing it again)
     * 
     * @param root
     * @param feature
     * @throws FeatureException
     * @throws IOException
     * @throws SAXException
     */
    public void append( Element root, Feature feature )
                            throws FeatureException, IOException, SAXException {

        GMLFeatureDocument doc = export( feature );
        XMLTools.insertNodeInto( doc.getRootElement(), root );
    }

    /**
     * Export a <code>Feature</code> to it's XML representation.
     * 
     * @param feature
     *            feature to export
     * @return XML representation of feature
     * @throws IOException
     * @throws FeatureException
     * @throws XMLException
     * @throws SAXException
     */
    public GMLFeatureDocument export( Feature feature )
                            throws IOException, FeatureException, XMLException, SAXException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream( 20000 );
        export( feature, bos );
        ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
        bos.close();

        GMLFeatureDocument doc = new GMLFeatureDocument();
        doc.load( bis, XMLFragment.DEFAULT_URL );
        return doc;
    }

    /**
     * Appends the DOM representation of the given <code>FeatureCollection</code> to the also given <code>Node</code>.
     * <p>
     * TODO do this a better way (append nodes directly without serializing to string and parsing it again)
     * 
     * @param root
     * @param fc
     * @throws FeatureException
     * @throws IOException
     * @throws SAXException
     */
    public void append( Element root, FeatureCollection fc )
                            throws FeatureException, IOException, SAXException {

        GMLFeatureCollectionDocument doc = export( fc );
        XMLTools.insertNodeInto( doc.getRootElement(), root );
    }

    /**
     * Export a <code>FeatureCollection</code> to it's XML representation.
     * 
     * @param fc
     *            feature collection
     * @return XML representation of feature collection
     * @throws IOException
     * @throws FeatureException
     * @throws XMLException
     * @throws SAXException
     */
    public GMLFeatureCollectionDocument export( FeatureCollection fc )
                            throws IOException, FeatureException, XMLException, SAXException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream( 20000 );
        export( fc, bos );
        ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
        bos.close();

        GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
        doc.load( bis, XMLFragment.DEFAULT_URL );
        return doc;
    }

    /**
     * Exports an instance of a <code>FeatureCollection</code> to the passed <code>OutputStream</code> formatted as GML.
     * Uses the deegree system character set for the XML header encoding information.
     * 
     * @param fc
     *            feature collection to export
     * @param os
     *            output stream to write to
     * 
     * @throws IOException
     * @throws FeatureException
     */
    public void export( FeatureCollection fc, OutputStream os )
                            throws IOException, FeatureException {
        export( fc, os, CharsetUtils.getSystemCharset() );
    }

    /**
     * Exports a <code>FeatureCollection</code> instance to the passed <code>OutputStream</code> formatted as GML.
     * 
     * @param fc
     *            feature collection to export
     * @param os
     *            output stream to write to
     * @param charsetName
     *            name of the used charset/encoding (for the XML header)
     * 
     * @throws IOException
     * @throws FeatureException
     */
    public void export( FeatureCollection fc, OutputStream os, String charsetName )
                            throws IOException, FeatureException {

        PrintWriter pw = new PrintWriter( new OutputStreamWriter( os, charsetName ) );
        pw.println( "<?xml version=\"1.0\" encoding=\"" + charsetName + "\"?>" );
        if ( fc instanceof FeatureTupleCollection ) {
            exportTupleCollection( (FeatureTupleCollection) fc, pw );
        } else {
            exportRootCollection( fc, pw );
        }
        pw.close();
    }

    /**
     * Exports a <code>FeatureCollection</code> instance to the passed <code>OutputStream</code> formatted as GML.
     * 
     * @param fc
     *            feature collection to print/export
     * @param pw
     *            target of the printing/export
     * @throws FeatureException
     */
    private void exportRootCollection( FeatureCollection fc, PrintWriter pw )
                            throws FeatureException {

        Set<Feature> additionalRootLevelFeatures = determineAdditionalRootLevelFeatures( fc );
        if ( this.suppressXLinkOutput && additionalRootLevelFeatures.size() > 0 ) {
            String msg = Messages.getString( "ERROR_REFERENCE_TYPE" );
            throw new FeatureException( msg );
        }

        if ( fc.getId() != null && !"".equals( fc.getId() ) ) {
            this.exportedFeatures.add( fc.getId() );
        }

        // open the feature collection element
        pw.print( "<" );
        pw.print( fc.getName().getPrefixedName() );

        // hack to correct the "numberOfFeatures" attribute (can not be set in any case, because
        // sometimes (resultType="hits") the collection contains no features at all -- but only the
        // attribute "numberOfFeatures"
        if ( fc.size() > 0 ) {
            int hackedFeatureCount = fc.size() + additionalRootLevelFeatures.size();
            fc.setAttribute( "numberOfFeatures", "" + hackedFeatureCount );
        }

        Map<String, String> attributes = fc.getAttributes();
        if ( attributes != null ) {
            for ( Iterator<String> iterator = attributes.keySet().iterator(); iterator.hasNext(); ) {
                String name = iterator.next();
                String value = attributes.get( name );
                pw.print( ' ' );
                pw.print( name );
                pw.print( "='" );
                pw.print( value );
                pw.print( "'" );
            }
        }

        // determine and add namespace bindings
        Map<String, URI> nsBindings = determineUsedNSBindings( fc );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( nsBindings.toString() );
        }
        nsBindings.put( "gml", CommonNamespaces.GMLNS );
        nsBindings.put( "xlink", CommonNamespaces.XLNNS );
        if ( this.schemaURL != null ) {
            nsBindings.put( "xsi", CommonNamespaces.XSINS );
        }
        appendNSBindings( nsBindings, pw );

        // add schema reference (if available)
        if ( this.schemaURL != null && fc.size() > 0 ) {
            pw.print( " xsi:schemaLocation=\"" + fc.getFeature( 0 ).getName().getNamespace() + " " );
            pw.print( this.schemaURL + " " );
            pw.print( wfsSchemaBinding + "\"" );
        }
        pw.print( '>' );

        Envelope env = null;
        try {
            env = fc.getBoundedBy();
        } catch ( GeometryException e ) {
            // omit gml:boundedBy-element if featureCollection contains features
            // with different SRS (and their envelopes cannot be merged)
        }
        if ( env != null ) {
            pw.print( "<gml:boundedBy><gml:Envelope" );
            if ( env.getCoordinateSystem() != null ) {
                pw.print( " srsName='" + env.getCoordinateSystem().getPrefixedName() + "'" );
            }

            boolean swap = swap( env );

            pw.print( "><gml:lowerCorner>" );
            pw.print( swap ? env.getMin().getY() : env.getMin().getX() );
            pw.print( ' ' );
            pw.print( swap ? env.getMin().getX() : env.getMin().getY() );
            pw.print( "</gml:lowerCorner><gml:upperCorner>" );
            pw.print( swap ? env.getMax().getY() : env.getMax().getX() );
            pw.print( ' ' );
            pw.print( swap ? env.getMax().getX() : env.getMax().getY() );
            pw.print( "</gml:upperCorner></gml:Envelope></gml:boundedBy>" );
        }

        // export all contained features
        for ( int i = 0; i < fc.size(); i++ ) {
            Feature feature = fc.getFeature( i );
            String fid = feature.getId();
            if ( fid != null && !fid.equals( "" ) && this.exportedFeatures.contains( fid ) && !this.suppressXLinkOutput ) {
                pw.print( "<gml:featureMember xlink:href=\"#" );
                pw.print( fid );
                pw.print( "\"/>" );
            } else {
                pw.print( "<gml:featureMember>" );
                export( feature, pw, xlinkDepth );
                pw.print( "</gml:featureMember>" );
            }
        }

        // export all additional root level features
        for ( Feature feature : additionalRootLevelFeatures ) {
            String fid = feature.getId();
            if ( fid != null && !fid.equals( "" ) && this.exportedFeatures.contains( fid ) ) {
                throw new RuntimeException();
            }
            pw.print( "<gml:featureMember>" );
            export( feature, pw, xlinkDepth );
            pw.print( "</gml:featureMember>" );
        }

        // close the feature collection element
        pw.print( "</" );
        pw.print( fc.getName().getPrefixedName() );
        pw.print( '>' );
    }

    /**
     * Determines features that don't originally belong to the root level of the given <code>FeatureCollection</code>,
     * but which need to be put there in the output, because they are subfeatures inside of properties that have the
     * content type "gml:ReferenceType" and wouldn't otherwise be exported anywhere.
     * 
     * @param fc
     * @return features to be added to the root level
     */
    private Set<Feature> determineAdditionalRootLevelFeatures( FeatureCollection fc ) {

        Set<Feature> embeddedFeatures = determineEmbeddedFeatures( fc );
        Set<Feature> additionalRootFeatures = new HashSet<Feature>();
        Set<Feature> checkedFeatures = new HashSet<Feature>();
        for ( int i = 0; i < fc.size(); i++ ) {
            Feature feature = fc.getFeature( i );
            determineAdditionalRootLevelFeatures( feature, additionalRootFeatures, embeddedFeatures, checkedFeatures );
        }
        return additionalRootFeatures;
    }

    /**
     * Determines the features that are embedded in the given feature collection on all levels.
     * <p>
     * NOTE: This *excludes* all subfeatures which are only values of "gml:ReferenceType" properties.
     * </p>
     * 
     * @param fc
     * @return all features that are embedded in the GML representation of the given collection
     */
    private Set<Feature> determineEmbeddedFeatures( FeatureCollection fc ) {
        Set<Feature> features = new HashSet<Feature>( fc.size() );
        for ( int i = 0; i < fc.size(); i++ ) {
            determineEmbeddedFeatures( features, fc.getFeature( i ) );
        }
        return features;
    }

    private void determineEmbeddedFeatures( Set<Feature> exportedFeatures, Feature feature ) {
        if ( !exportedFeatures.contains( feature ) ) {
            exportedFeatures.add( feature );
            for ( FeatureProperty property : feature.getProperties() ) {
                Object value = property.getValue();
                if ( value instanceof Feature ) {
                    Feature subFeature = (Feature) value;
                    if ( feature.getFeatureType() != null && feature.getFeatureType() instanceof MappedFeatureType ) {
                        MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();
                        MappedFeaturePropertyType pt = (MappedFeaturePropertyType) ft.getProperty( property.getName() );
                        assert pt != null;
                        if ( !pt.isReferenceType() ) {
                            determineEmbeddedFeatures( exportedFeatures, subFeature );
                        }
                    }
                }
            }
        }
    }

    /**
     * Determines features that don't originally belong to the root level of the given <code>FeatureCollection</code>,
     * but which need to be put there in the output, because they are subfeatures inside of properties that have the
     * content type "gml:ReferenceType".
     * 
     * @param feature
     * @param additionalFeatures
     *            to be added to the root level
     * @param embeddedFeatures
     *            to be added to the root level
     * @param checkedFeatures
     *            features that have already been checked
     */
    private void determineAdditionalRootLevelFeatures( Feature feature, Set<Feature> additionalFeatures,
                                                       Set<Feature> embeddedFeatures, Set<Feature> checkedFeatures ) {
        for ( FeatureProperty property : feature.getProperties() ) {
            Object value = property.getValue();
            if ( value instanceof Feature ) {
                Feature subFeature = (Feature) value;
                if ( !checkedFeatures.contains( subFeature ) ) {
                    if ( feature.getFeatureType() != null && feature.getFeatureType() instanceof MappedFeatureType ) {
                        MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();
                        MappedFeaturePropertyType pt = (MappedFeaturePropertyType) ft.getProperty( property.getName() );
                        assert pt != null;
                        if ( pt.isReferenceType() && !embeddedFeatures.contains( subFeature ) ) {
                            additionalFeatures.add( (Feature) value );
                        }
                    }
                    checkedFeatures.add( subFeature );
                    determineAdditionalRootLevelFeatures( subFeature, additionalFeatures, embeddedFeatures,
                                                          checkedFeatures );
                }
            }
        }
    }

    /**
     * Exports a {@link FeatureTupleCollection} instance to the passed {@link OutputStream} formatted as GML.
     * 
     * @param fc
     *            feature tuple collection to print/export
     * @param pw
     *            target of the printing/export
     * @throws FeatureException
     */
    private void exportTupleCollection( FeatureTupleCollection fc, PrintWriter pw )
                            throws FeatureException {

        if ( fc.getId() != null && !"".equals( fc.getId() ) ) {
            this.exportedFeatures.add( fc.getId() );
        }

        // open the feature collection element
        pw.print( "<" );
        pw.print( fc.getName().getPrefixedName() );

        Map<String, String> attributes = fc.getAttributes();
        if ( attributes != null ) {
            for ( Iterator<String> iterator = attributes.keySet().iterator(); iterator.hasNext(); ) {
                String name = iterator.next();
                String value = attributes.get( name );
                pw.print( ' ' );
                pw.print( name );
                pw.print( "='" );
                pw.print( value );
                pw.print( "'" );
            }
        }

        // determine and add namespace bindings
        Map<String, URI> nsBindings = determineUsedNSBindings( fc );
        nsBindings.put( "gml", CommonNamespaces.GMLNS );
        nsBindings.put( "xlink", CommonNamespaces.XLNNS );
        if ( this.schemaURL != null ) {
            nsBindings.put( "xsi", CommonNamespaces.XSINS );
        }
        appendNSBindings( nsBindings, pw );

        // add schema reference (if available)
        if ( this.schemaURL != null && fc.size() > 0 ) {
            pw.print( " xsi:schemaLocation=\"" + fc.getTuple( 0 )[0].getName().getNamespace() + " " );
            pw.print( this.schemaURL + " " );
            pw.print( wfsSchemaBinding + "\"" );
        }
        pw.print( '>' );

        Envelope env = null;
        try {
            env = fc.getBoundedBy();
        } catch ( GeometryException e ) {
            // omit gml:boundedBy-element if featureCollection contains features
            // with different SRS (and their envelopes cannot be merged)
        }
        if ( env != null ) {
            pw.print( "<gml:boundedBy><gml:Envelope" );
            if ( env.getCoordinateSystem() != null ) {
                pw.print( " srsName='" + env.getCoordinateSystem().getPrefixedName() + "'" );
            }

            boolean swap = swap( env );

            pw.print( "><gml:lowerCorner>" );
            pw.print( swap ? env.getMin().getY() : env.getMin().getX() );
            pw.print( ' ' );
            pw.print( swap ? env.getMin().getX() : env.getMin().getY() );
            pw.print( "</gml:lowerCorner><gml:upperCorner>" );
            pw.print( swap ? env.getMax().getY() : env.getMax().getX() );
            pw.print( ' ' );
            pw.print( swap ? env.getMax().getX() : env.getMax().getY() );
            pw.print( "</gml:upperCorner></gml:Envelope></gml:boundedBy>" );
        }

        // export all contained feature tuples
        for ( int i = 0; i < fc.numTuples(); i++ ) {
            Feature[] features = fc.getTuple( i );
            pw.print( "<gml:featureTuple>" );
            for ( Feature feature : features ) {
                export( feature, pw, xlinkDepth );
            }
            pw.print( "</gml:featureTuple>" );
        }

        // close the feature collection element
        pw.print( "</" );
        pw.print( fc.getName().getPrefixedName() );
        pw.print( '>' );
    }

    /**
     * Determines the namespace bindings that are used in the feature collection.
     * <p>
     * NOTE: Currently only the bindings for the feature collection's root element and the contained features are
     * considered. If a subfeature uses another bindings, this binding will be missing in the XML.
     * 
     * @param fc
     *            feature collection
     * @return the namespace bindings.
     */
    private Map<String, URI> determineUsedNSBindings( FeatureCollection fc ) {

        Map<String, URI> nsBindings = new HashMap<String, URI>();

        // process feature collection element
        QualifiedName name = fc.getName();
        nsBindings.put( name.getPrefix(), name.getNamespace() );

        if ( fc instanceof FeatureTupleCollection ) {
            // process contained feature tuples
            FeatureTupleCollection ftc = (FeatureTupleCollection) fc;
            for ( int i = 0; i < ftc.numTuples(); i++ ) {
                Feature[] features = ftc.getTuple( i );
                for ( Feature feature : features ) {
                    name = feature.getName();
                    nsBindings.put( name.getPrefix(), name.getNamespace() );
                }
            }
        } else {
            // process contained features
            // for ( int i = 0; i < fc.size(); i++ ) {
            // name = fc.getFeature( i ).getName();
            // nsBindings.put( name.getPrefix(), name.getNamespace() );
            // }
            for ( int i = 0; i < fc.size(); i++ ) {
                Feature feature = fc.getFeature( i );
                if ( feature != null ) {
                    nsBindings = determineUsedNSBindings( feature, nsBindings );
                }
            }
        }

        return nsBindings;
    }

    /**
     * Determines the namespace bindings that are used in the feature and it's properties
     * 
     * @param feature
     *            feature
     * @param nsBindings
     *            to add to.
     * @return the namespace bindings
     */
    private Map<String, URI> determineUsedNSBindings( Feature feature, Map<String, URI> nsBindings ) {
        if ( nsBindings == null ) {
            nsBindings = new HashMap<String, URI>();
        }
        if ( feature != null ) {
            // process feature element
            QualifiedName qName = feature.getName();
            if ( qName != null ) {
                String prefix = qName.getPrefix();
                URI ns = qName.getNamespace();
                if ( ns != null && !"".equals( ns.toASCIIString().trim() ) ) {
                    LOG.logDebug( "Adding qName: " + qName );
                    nsBindings.put( prefix, ns );
                }
            }

            // now check for properties which use a namespace
            FeatureProperty[] featureProperties = feature.getProperties();
            if ( featureProperties != null ) {
                for ( FeatureProperty fp : featureProperties ) {
                    if ( fp != null ) {
                        QualifiedName fpName = fp.getName();
                        if ( fpName != null ) {
                            String prefix = fpName.getPrefix();
                            if ( prefix != null && !"".equals( prefix.trim() ) ) {
                                if ( nsBindings.get( prefix ) == null ) {
                                    URI ns = fpName.getNamespace();
                                    if ( ns != null && !"".equals( ns.toASCIIString().trim() ) ) {
                                        LOG.logDebug( "Adding qname: " + fpName );
                                        nsBindings.put( prefix, ns );
                                    }
                                }
                            }
                        }
                        // Object value = fp.getValue();
                        // if ( value instanceof Feature ) {
                        // determineUsedNSBindings( (Feature) value, nsBindings );
                        // }
                    }
                }
            }
        }

        return nsBindings;
    }

    /**
     * Determines the namespace bindings that are used in the feature.
     * <p>
     * NOTE: Currently only the bindings for the feature's root element and the contained features are considered. If a
     * subfeature uses another bindings, this binding will be missing in the XML.
     * 
     * @param feature
     *            feature
     * @return the namespace bindings
     */
    private Map<String, URI> determineUsedNSBindings( Feature feature ) {

        // reset the counter.
        Map<String, URI> nsBindings = new HashMap<String, URI>();

        return determineUsedNSBindings( feature, nsBindings );
        // // process feature element
        // QualifiedName name = feature.getName();
        // nsBindings.put( name.getPrefix(), name.getNamespace() );

    }

    /**
     * Appends the given namespace bindings to the PrintWriter.
     * 
     * @param bindings
     *            namespace bindings to append
     * @param pw
     *            PrintWriter to write to
     */
    private void appendNSBindings( Map<String, URI> bindings, PrintWriter pw ) {

        Iterator<String> prefixIter = bindings.keySet().iterator();
        while ( prefixIter.hasNext() ) {
            String prefix = prefixIter.next();
            URI nsURI = bindings.get( prefix );
            if ( prefix == null ) {
                pw.print( " xmlns=\"" );
                pw.print( nsURI );
                pw.print( '\"' );
            } else {
                pw.print( " xmlns:" );
                pw.print( prefix );
                pw.print( "=\"" );
                pw.print( nsURI );
                pw.print( '\"' );
            }
        }
        // if more then one default namespaces were defined, each feature must (re)-determine the
        // default ns.
        this.nsBindingsExported = true;// ( defaultNamespaceCounter == 0 );
    }

    /**
     * Exports an instance of a <code>Feature</code> to the passed <code>OutputStream</code> formatted as GML. Uses the
     * deegree system character set for the XML header encoding information.
     * 
     * @param feature
     *            feature to export
     * @param os
     *            output stream to write to
     * 
     * @throws IOException
     * @throws FeatureException
     */
    public void export( Feature feature, OutputStream os )
                            throws IOException, FeatureException {
        export( feature, os, CharsetUtils.getSystemCharset() );
    }

    /**
     * Exports a <code>Feature</code> instance to the passed <code>OutputStream</code> formatted as GML.
     * 
     * @param feature
     *            feature to export
     * @param os
     *            output stream to write to
     * @param charsetName
     *            name of the used charset/encoding (for the XML header)
     * @throws IOException
     * @throws FeatureException
     */
    public void export( Feature feature, OutputStream os, String charsetName )
                            throws IOException, FeatureException {

        PrintWriter pw = new PrintWriter( new OutputStreamWriter( os, charsetName ) );
        pw.println( "<?xml version=\"1.0\" encoding=\"" + charsetName + "\"?>" );
        export( feature, pw, xlinkDepth );
        pw.close();
    }

    /**
     * Exports a <code>Feature</code> instance to the passed <code>PrintWriter</code> as GML.
     * 
     * @param feature
     *            feature to export
     * @param pw
     *            PrintWriter to write to
     * @throws FeatureException
     */
    private void export( Feature feature, PrintWriter pw, int currentDepth )
                            throws FeatureException {

        boolean isPseudoFt = false;
        if ( feature.getFeatureType() != null && feature.getFeatureType() instanceof MappedFeatureType ) {
            isPseudoFt = ( (MappedFeatureType) feature.getFeatureType() ).isPseudoFeatureType();
        }

        QualifiedName ftName = feature.getName();
        String fid = isPseudoFt ? null : feature.getId();

        if ( this.suppressXLinkOutput && fid != null && !"".equals( fid ) ) {
            if ( this.localFeatures.contains( fid ) ) {
                String msg = Messages.format( "ERROR_CYLIC_FEATURE", fid );
                throw new FeatureException( msg );
            }
            this.localFeatures.add( fid );
        }

        // open feature element (add gml:id attribute if feature has an id)
        pw.print( '<' );
        pw.print( ftName.getPrefixedName() );
        if ( fid != null && !fid.equals( "" ) ) {
            this.exportedFeatures.add( fid );
            pw.print( " gml:id=\"" );
            pw.print( fid );
            pw.print( '\"' );
        }

        // determine and add namespace bindings

        if ( !this.nsBindingsExported ) {

            Map<String, URI> nsBindings = determineUsedNSBindings( feature );
            nsBindings.put( "gml", CommonNamespaces.GMLNS );
            nsBindings.put( "xlink", CommonNamespaces.XLNNS );
            if ( this.schemaURL != null ) {
                nsBindings.put( "xsi", CommonNamespaces.XSINS );
            }
            appendNSBindings( nsBindings, pw );
        }

        pw.print( '>' );

        // to get the order right (gml default attributes come BEFORE the envelope, stupid...)
        boolean boundedByExported = false;

        // export all properties of the feature
        FeatureProperty[] properties = feature.getProperties();

        int geomProperties = -1; // this counter is actually counted up where geometries are exported (and it's used for
        // outputting geometry ids)
        // that's why the methods now all take the counter and return it also

        for ( int i = 0; i < properties.length; i++ ) {
            boolean exportEnv = !isPseudoFt && !boundedByExported;
            QualifiedName qn = properties[i].getName();
            String ln = qn.getLocalName();
            exportEnv = exportEnv
                        && !( GMLNS.equals( qn.getNamespace() ) && ( "description".equals( ln ) || "name".equals( ln ) ) );

            if ( exportEnv ) {
                exportBoundedBy( feature, pw );
                boundedByExported = true;
            }

            if ( properties[i] != null && properties[i].getValue() != null ) {
                geomProperties = exportProperty( feature, properties[i], pw, geomProperties, currentDepth );
            }
        }

        if ( properties.length == 0 ) {
            boolean exportEnv = !isPseudoFt && !boundedByExported;
            if ( exportEnv ) {
                exportBoundedBy( feature, pw );
                boundedByExported = true;
            }
        }

        // close feature element
        pw.print( "</" );
        pw.print( ftName.getPrefixedName() );
        pw.println( '>' );

        if ( this.suppressXLinkOutput || fid != null ) {
            this.localFeatures.remove( fid );
        }
    }

    private static void exportBoundedBy( Feature feature, PrintWriter pw ) {
        try {
            Envelope env = null;
            if ( ( env = feature.getBoundedBy() ) != null ) {
                pw.print( "<gml:boundedBy><gml:Envelope" );
                if ( env.getCoordinateSystem() != null ) {
                    pw.print( " srsName='" + env.getCoordinateSystem().getPrefixedName() + "'" );
                }

                boolean swap = swap( env );

                pw.print( "><gml:lowerCorner>" );
                pw.print( swap ? env.getMin().getY() : env.getMin().getX() );
                pw.print( ' ' );
                pw.print( swap ? env.getMin().getX() : env.getMin().getY() );
                pw.print( "</gml:lowerCorner><gml:upperCorner>" );
                pw.print( swap ? env.getMax().getY() : env.getMax().getX() );
                pw.print( ' ' );
                pw.print( swap ? env.getMax().getX() : env.getMax().getY() );
                pw.print( "</gml:upperCorner></gml:Envelope></gml:boundedBy>" );
            }
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
        }

    }

    /**
     * Exports a <code>FeatureProperty</code> instance to the passed <code>PrintWriter</code> as GML.
     * 
     * @param feature
     *            feature that the property belongs to
     * @param property
     *            property to export
     * @param pw
     *            PrintWriter to write to
     * @param geomProperties
     *            counter to indicate the number of the current geometry property
     * @param currentDepth
     *            counter to indicate how many levels of xlinks have already been resolved
     * @throws FeatureException
     */
    private int exportProperty( Feature feature, FeatureProperty property, PrintWriter pw, int geomProperties,
                                int currentDepth )
                            throws FeatureException {

        QualifiedName propertyName = property.getName();
        Object value = property.getValue();
        Integer d = xlinkPropertyNames.get( property.getName().getPrefixedName() );
        int customDepth = d == null ? currentDepth : d;
        boolean isReferenceType = false;
        if ( feature.getFeatureType() != null && feature.getFeatureType() instanceof MappedFeatureType ) {
            MappedFeatureType ft = (MappedFeatureType) feature.getFeatureType();
            if ( ft.getProperty( property.getName() ) instanceof MappedFeaturePropertyType ) {
                MappedFeaturePropertyType pt = (MappedFeaturePropertyType) ft.getProperty( property.getName() );
                isReferenceType = pt.isReferenceType();
            }
        }

        if ( value instanceof Feature ) {
            Feature subfeature = (Feature) value;

            if ( isReferenceType
                 || ( subfeature.getId() != null && !subfeature.getId().equals( "" )
                      && exportedFeatures.contains( subfeature.getId() ) && !this.suppressXLinkOutput ) ) {
                if ( exportedFeatures.contains( subfeature.getId() ) ) {
                    pw.print( "<!-- xlink:href=\"#" );
                    pw.print( subfeature.getId() );
                    pw.print( "\" -->" );
                    pw.print( '<' );
                    pw.print( propertyName.getPrefixedName() );
                    pw.print( " xlink:href=\"#" );
                    pw.print( subfeature.getId() );
                    pw.print( "\"/>" );
                } else {
                    pw.print( '<' );
                    pw.print( propertyName.getPrefixedName() );
                    if ( isReferenceType || currentDepth == 0 || customDepth == 0 ) {
                        pw.print( " xlink:href=\"#" );
                        pw.print( subfeature.getId() );
                        pw.print( "\"" );
                    }
                    pw.print( ">" );
                    if ( !isReferenceType && ( currentDepth != 0 || customDepth != 0 ) ) {
                        pw.print( "<!-- gml:id=\"" );
                        pw.print( subfeature.getId() );
                        pw.print( "\" -->" );
                        export( subfeature, pw, currentDepth - 1 );
                    }
                    pw.print( "</" );
                    pw.print( propertyName.getPrefixedName() );
                    pw.print( ">" );
                }
            } else {
                pw.print( '<' );
                pw.print( propertyName.getPrefixedName() );
                if ( currentDepth == 0 && customDepth == 0 ) {
                    pw.print( " xlink:href=\"" );
                    pw.print( baseUrl );
                    pw.print( "?request=GetGmlObject&amp;version=1.1.0&amp;service=WFS&amp;objectid=" );
                    pw.print( subfeature.getId() );
                    pw.print( "\"/>" );
                } else {
                    pw.print( '>' );
                    pw.print( "<!-- xlink:href=\"#" );
                    pw.print( subfeature.getId() );
                    pw.print( "\" -->" );
                    exportPropertyValue( subfeature, pw, FEATURE, null, 0, currentDepth - 1 );
                    pw.print( "</" );
                    pw.print( propertyName.getPrefixedName() );
                    pw.print( '>' );
                }
            }
        } else if ( value instanceof URL ) {
            if ( currentDepth == 0 || isReferenceType || customDepth == 0 ) {
                pw.print( '<' );
                pw.print( propertyName.getPrefixedName() );
                pw.print( " xlink:href=\"" );
                pw.print( escape( value.toString() ) );
                pw.print( "\"/>" );
            } else {
                pw.print( '<' );
                pw.print( propertyName.getPrefixedName() );
                pw.print( ">" );
                pw.print( "<!-- xlink:href=\"" );
                pw.print( value );
                pw.print( "\" -->" );

                try {
                    GMLFeatureDocument doc = new GMLFeatureDocument();
                    URLConnection conn = ( (URL) value ).openConnection();
                    conn.setReadTimeout( 5000 );
                    conn.setConnectTimeout( 1000 );
                    doc.load( conn.getInputStream(), ( (URL) value ).toExternalForm() );
                    export( doc.parseFeature(), pw, currentDepth - 1 );
                    pw.print( "</" );
                    pw.print( propertyName.getPrefixedName() );
                    pw.print( ">" );
                } catch ( IOException e ) {
                    e.printStackTrace();
                    LOG.logDebug( "Stack trace:", e );
                    throw new FeatureException( org.deegree.i18n.Messages.get( "DATASTORE_XLINK_IO_RETRIEVE",
                                                                               e.getLocalizedMessage() ) );
                } catch ( SAXException e ) {
                    e.printStackTrace();
                    LOG.logDebug( "Stack trace:", e );
                    throw new FeatureException( org.deegree.i18n.Messages.get( "DATASTORE_XLINK_PARSE_ERROR",
                                                                               e.getLocalizedMessage() ) );
                } catch ( XMLParsingException e ) {
                    e.printStackTrace();
                    LOG.logDebug( "Stack trace:", e );
                    throw new FeatureException( org.deegree.i18n.Messages.get( "DATASTORE_XLINK_PARSE_ERROR",
                                                                               e.getLocalizedMessage() ) );
                } catch ( UnknownCRSException e ) {
                    e.printStackTrace();
                    LOG.logDebug( "Stack trace:", e );
                    throw new FeatureException( org.deegree.i18n.Messages.get( "DATASTORE_XLINK_PARSE_ERROR",
                                                                               e.getLocalizedMessage() ) );
                }

            }
        } else {
            pw.print( '<' );
            pw.print( propertyName.getPrefixedName() );
            pw.print( '>' );
            if ( value != null ) {
                FeatureType ft = feature.getFeatureType();
                PropertyType pt = ft.getProperty( property.getName() );
                if ( pt == null || pt.getType() == Types.ANYTYPE ) {
                    pw.print( value );
                } else {
                    geomProperties = exportPropertyValue( value, pw, pt.getType(), printGeometryIds ? feature.getId()
                                                                                                   : null,
                                                          geomProperties, currentDepth - 1 );
                }
            }
            pw.print( "</" );
            pw.print( propertyName.getPrefixedName() );
            pw.print( '>' );
        }

        return geomProperties;
    }

    /**
     * Exports the value of a property to the passed <code>PrintWriter</code> as GML.
     * 
     * TODO make available and use property type information to determine correct output format (e.g. xs:date, xs:time,
     * xs:dateTime are all represented using Date objects and cannot be differentiated at the moment)
     * 
     * @param value
     *            property value to export
     * @param pw
     *            PrintWriter to write to
     * @param type
     *            the Types.java type code
     * @param geomProperties
     *            counter to indicate the number of the current geometry property
     * @param currentDepth
     *            counter to indicate how many levels of xlinks have already been resolved
     * @throws FeatureException
     */
    private int exportPropertyValue( Object value, PrintWriter pw, int type, String id, int geomProperties,
                                     int currentDepth )
                            throws FeatureException {
        if ( value instanceof Feature ) {
            export( (Feature) value, pw, currentDepth );
        } else if ( value instanceof Feature[] ) {
            Feature[] features = (Feature[]) value;
            for ( int i = 0; i < features.length; i++ ) {
                export( features[i], pw, currentDepth );
            }
        } else if ( value instanceof Envelope ) {
            exportEnvelope( (Envelope) value, pw );
        } else if ( value instanceof FeatureCollection ) {
            export( (FeatureCollection) value, pw, currentDepth );
        } else if ( value instanceof Geometry ) {
            id = id == null ? null : ( id + "_GEOM_" + ++geomProperties );
            exportGeometry( (Geometry) value, pw, id );
        } else if ( value instanceof Date ) {

            switch ( type ) {
            case DATE: {
                pw.print( dateFormatter.format( (Date) value ) );
                break;
            }
            case TIME: {
                pw.print( timeFormatter.format( (Date) value ) );
                break;
            }
            case TIMESTAMP: {
                pw.print( getISOFormattedTime( (Date) value ) );
                break;
            }
            }

        } else if ( value instanceof Calendar ) {
            pw.print( TimeTools.getISOFormattedTime( (Calendar) value ) );
        } else if ( value instanceof Timestamp ) {
            pw.print( TimeTools.getISOFormattedTime( (Timestamp) value ) );
        } else if ( value instanceof java.sql.Date ) {
            pw.print( TimeTools.getISOFormattedTime( (java.sql.Date) value ) );
        } else if ( value instanceof Number || value instanceof BigDecimal ) {
            pw.print( value.toString() );
        } else if ( value instanceof String ) {
            StringBuffer sb = DOMPrinter.validateCDATA( (String) value );
            pw.print( sb );
        } else if ( value instanceof Boolean ) {
            pw.print( value );
        } else {
            LOG.logInfo( "Unhandled property class '" + value.getClass() + "' in GMLFeatureAdapter." );
            StringBuffer sb = DOMPrinter.validateCDATA( value.toString() );
            pw.print( sb );
        }

        return geomProperties;
    }

    /**
     * prints the passed geometry to the also passed PrintWriter formatted as GML
     * 
     * @param geo
     *            geometry to print/extport
     * @param pw
     *            target of the printing/export
     * @throws FeatureException
     */
    private void exportGeometry( Geometry geo, PrintWriter pw, String id )
                            throws FeatureException {
        try {
            GMLGeometryAdapter.export( geo, pw, id );
        } catch ( Exception e ) {
            LOG.logError( "", e );
            throw new FeatureException( "Could not export geometry to GML: " + e.getMessage(), e );
        }
    }

    /**
     * prints the passed geometry to the also passed PrintWriter formatted as GML
     * 
     * @param geo
     *            geometry to print/extport
     * @param pw
     *            target of the printing/export
     * @throws FeatureException
     */
    private void exportEnvelope( Envelope geo, PrintWriter pw )
                            throws FeatureException {
        try {
            pw.print( GMLGeometryAdapter.exportAsBox( geo ) );
        } catch ( Exception e ) {
            throw new FeatureException( "Could not export envelope to GML: " + e.getMessage(), e );
        }
    }
}
