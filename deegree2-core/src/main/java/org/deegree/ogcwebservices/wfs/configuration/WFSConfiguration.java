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
package org.deegree.ogcwebservices.wfs.configuration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedGMLSchemaDocument;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.capabilities.FeatureTypeList;
import org.deegree.ogcwebservices.wfs.capabilities.FormatType;
import org.deegree.ogcwebservices.wfs.capabilities.GMLObject;
import org.deegree.ogcwebservices.wfs.capabilities.Operation;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;

/**
 * Represents the configuration for a deegree {@link WFService} instance.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSConfiguration extends WFSCapabilities {

    private static final long serialVersionUID = -8929822028461025018L;

    protected static final ILogger LOG = LoggerFactory.getLogger( WFSConfiguration.class );

    private WFSDeegreeParams deegreeParams;

    private Map<QualifiedName, MappedFeatureType> ftMap = new HashMap<QualifiedName, MappedFeatureType>();

    private boolean hasUniquePrefixMapping = true;

    private Map<String, MappedFeatureType> prefixMap = new HashMap<String, MappedFeatureType>();

    /**
     * Generates a new <code>WFSConfiguration</code> instance from the given parameters.
     *
     * @param version
     * @param updateSequence
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param featureTypeList
     * @param servesGMLObjectTypeList
     * @param supportsGMLObjectTypeList
     * @param contents
     *            TODO field not verified! Check spec.
     * @param filterCapabilities
     * @param deegreeParams
     * @throws InvalidConfigurationException
     */
    public WFSConfiguration( String version, String updateSequence, ServiceIdentification serviceIdentification,
                             ServiceProvider serviceProvider, OperationsMetadata operationsMetadata,
                             FeatureTypeList featureTypeList, GMLObject[] servesGMLObjectTypeList,
                             GMLObject[] supportsGMLObjectTypeList, Contents contents,
                             FilterCapabilities filterCapabilities, WFSDeegreeParams deegreeParams )
                            throws InvalidConfigurationException {
        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata, featureTypeList,
               servesGMLObjectTypeList, supportsGMLObjectTypeList, contents, filterCapabilities );
        this.deegreeParams = deegreeParams;
        try {
            validateFeatureTypeDefinitions();
            buildAndValidatePrefixMap();
        } catch ( InvalidConfigurationException e ) {
            LOG.logError( e.getMessage() );
            throw e;
        }
    }

    /**
     * Returns the deegreeParams.
     *
     * @return the deegreeParams
     */
    public WFSDeegreeParams getDeegreeParams() {
        return deegreeParams;
    }

    /**
     * The deegreeParams to set.
     *
     * @param deegreeParams
     */
    public void setDeegreeParams( WFSDeegreeParams deegreeParams ) {
        this.deegreeParams = deegreeParams;
    }

    /**
     * Returns a <code>Map</code> of the feature types that this configuration defines.
     *
     * @return keys: feature type names, values: mapped feature types
     */
    public Map<QualifiedName, MappedFeatureType> getMappedFeatureTypes() {
        return this.ftMap;
    }

    /**
     * Returns whether this WFS has unique prefixes for it's feature ids, so the type of a feature can always be
     * identified by its feature id.
     *
     * @return true, if it has unique prefixes, false otherwise
     */
    public boolean hasUniquePrefixMapping() {
        return hasUniquePrefixMapping;
    }

    /**
     * Returns the {@link MappedFeatureType} for the given feature id.
     *
     * @param featureId
     *            feature id to look up
     * @return the {@link MappedFeatureType} for the given feature id, null if no mapping was found
     */
    public MappedFeatureType getFeatureType( String featureId ) {

        // TODO improve this (but note that there is no ensured delimiter between prefix and the rest of the id)
        for ( String prefix : prefixMap.keySet() ) {
            if ( featureId.startsWith( prefix ) ) {
                return prefixMap.get( prefix );
            }
        }
        return null;
    }

    /**
     * The <code>WFSConfiguration</code> is processed as follows:
     * <ul>
     * <li>The data directories (as specified in the configuration) are scanned for {@link MappedGMLSchemaDocument}s</li>
     * <li>All {@link MappedFeatureType}s defined in any of the found {@link MappedGMLSchemaDocument}s are extracted, if
     * duplicate feature type definitions occur, an <code>InvalidConfigurationException</code> is thrown.</li>
     * <li>All feature types defined in the FeatureTypeList section of the WFS configuration are checked to have a
     * corresponding {@link MappedFeatureType} definition. If this is not the case (or if the DefaultSRS is not equal to
     * the CRS in the respective datastore definition), an <code>InvalidConfigurationException</code> is thrown.</li>
     * <li>NOTE: the above is not necessary for FeatureTypes that are processed by XSLT-scripts (because they are
     * usually mapped to a different FeatureTypes by XSLT)</li>
     * <li>All feature types that are not defined in the FeatureTypeList section, but have been defined in one of the
     * {@link MappedGMLSchemaDocument}s are added as feature types to the WFS configuration.</li>
     * </ul>
     * </p>
     *
     * @throws InvalidConfigurationException
     */
    @SuppressWarnings("unchecked")
    private void validateFeatureTypeDefinitions()
                            throws InvalidConfigurationException {

        // extract the mapped feature types from the given data directory
        this.ftMap = scanForMappedFeatureTypes();
        Map<QualifiedName, MappedFeatureType> tempFeatureTypeMap = (Map<QualifiedName, MappedFeatureType>) ( (HashMap<QualifiedName, MappedFeatureType>) this.ftMap ).clone();

        // check that for each configuration feature type a mapped feature type exists and that
        // their DefaultSRS are identical
        WFSFeatureType[] wfsFTs = getFeatureTypeList().getFeatureTypes();
        for ( int i = 0; i < wfsFTs.length; i++ ) {

            if ( !wfsFTs[i].isVirtual() ) {
                MappedFeatureType mappedFT = this.ftMap.get( wfsFTs[i].getName() );
                if ( mappedFT == null ) {
                    String msg = Messages.getMessage( "WFS_CONF_FT_APP_SCHEMA_MISSING", wfsFTs[i].getName() );
                    throw new InvalidConfigurationException( msg );
                }
                if ( !mappedFT.isVisible() ) {
                    String msg = Messages.getMessage( "WFS_CONF_FT_APP_SCHEMA_INVISIBLE", wfsFTs[i].getName() );
                    throw new InvalidConfigurationException( msg );
                }
                URI defaultSRS = wfsFTs[i].getDefaultSRS();
                if ( defaultSRS == null ) {
                    wfsFTs[i].setDefaultSrs( mappedFT.getDefaultSRS() );
                } else {
                    try {
                        CoordinateSystem crs1 = CRSFactory.create( defaultSRS.toASCIIString() );
                        CoordinateSystem crs2 = CRSFactory.create( mappedFT.getDefaultSRS().toASCIIString() );
                        if ( !crs1.equals( crs2 ) ) {
                            String msg = Messages.getMessage( "WFS_CONF_FT_APP_SCHEMA_WRONG_SRS", wfsFTs[i].getName(),
                                                              wfsFTs[i].getDefaultSRS(), mappedFT.getDefaultSRS() );
                            throw new InvalidConfigurationException( msg );
                        }
                    } catch ( UnknownCRSException e ) {
                        // probably not going to happen anyway
                        if ( !defaultSRS.equals( mappedFT.getDefaultSRS() ) ) {
                            String msg = Messages.getMessage( "WFS_CONF_FT_APP_SCHEMA_WRONG_SRS", wfsFTs[i].getName(),
                                                              wfsFTs[i].getDefaultSRS(), mappedFT.getDefaultSRS() );
                            throw new InvalidConfigurationException( msg );
                        }
                    }
                }

                // merge otherSRS from WFS configuration with otherSRS from schema
                Set<URI> mergedOtherSRS = new HashSet<URI>();
                for ( URI uri : wfsFTs[i].getOtherSrs() ) {
                    mergedOtherSRS.add( uri );
                }
                for ( URI uri : mappedFT.getOtherSRS() ) {
                    mergedOtherSRS.add( uri );
                }
                wfsFTs[i].setOtherSrs( mergedOtherSRS.toArray( new URI[mergedOtherSRS.size()] ) );
            }

            // remove datastore feature type
            tempFeatureTypeMap.remove( wfsFTs[i].getName() );
        }

        // add all remaining mapped feature types to the WFS configuration
        Iterator<QualifiedName> it = tempFeatureTypeMap.keySet().iterator();
        while ( it.hasNext() ) {
            QualifiedName featureTypeName = it.next();
            MappedFeatureType mappedFT = tempFeatureTypeMap.get( featureTypeName );
            if ( mappedFT.isVisible() ) {
                try {
                    getFeatureTypeList().addFeatureType( createWFSFeatureType( mappedFT ) );
                } catch ( UnknownCRSException e ) {
                    throw new InvalidConfigurationException( e );
                }
            }
        }
    }

    /**
     * Scans for and extracts the <code>MappedFeatureType</code> s that are located in the data directories of the
     * current WFS configuration.
     *
     * @return keys: featureTypeNames, values: mapped feature types
     * @throws InvalidConfigurationException
     */
    private Map<QualifiedName, MappedFeatureType> scanForMappedFeatureTypes()
                            throws InvalidConfigurationException {
        List<String> fileNameList = new ArrayList<String>();
        String[] dataDirectories = getDeegreeParams().getDataDirectories();

        for ( int i = 0; i < dataDirectories.length; i++ ) {
            File file = new File( dataDirectories[i] );
            String[] list = file.list( new XSDFileFilter() );
            if ( list != null ) {
                if ( list.length == 0 ) {
                    LOG.logInfo( "Specified datastore directory '" + dataDirectories[i]
                                 + "' does not contain any '.xsd' files." );
                }
                for ( int j = 0; j < list.length; j++ ) {
                    fileNameList.add( dataDirectories[i] + '/' + list[j] );
                }
            } else {
                LOG.logInfo( "Specified datastore directory '" + dataDirectories[i] + "' does not denote a directory." );
            }
        }
        String[] fileNames = fileNameList.toArray( new String[fileNameList.size()] );
        return extractMappedFeatureTypes( fileNames );
    }

    /**
     * Extracts the {@link MappedFeatureType} s which are defined in the given array of mapped application schema
     * filenames. Files that do not conform to mapped GML Application Schemas definition are omitted, so are multiple
     * definitions of the same feature types.
     *
     * @param fileNames
     *            fileNames to be scanned
     * @return keys: feature type names, values: mapped feature types
     * @throws InvalidConfigurationException
     */
    private Map<QualifiedName, MappedFeatureType> extractMappedFeatureTypes( String[] fileNames )
                            throws InvalidConfigurationException {

        Map<QualifiedName, MappedFeatureType> featureTypeMap = new HashMap<QualifiedName, MappedFeatureType>(
                                                                                                              fileNames.length );

        for ( int i = 0; i < fileNames.length; i++ ) {
            try {
                URL fileURL = new File( fileNames[i] ).toURL();
                LOG.logInfo( "Reading annotated GML application schema from URL '" + fileURL + "'." );
                MappedGMLSchemaDocument doc = new MappedGMLSchemaDocument();
                doc.load( fileURL );
                MappedGMLSchema gmlSchema = doc.parseMappedGMLSchema();

                FeatureType[] featureTypes = gmlSchema.getFeatureTypes();
                for ( int j = 0; j < featureTypes.length; j++ ) {
                    MappedFeatureType ft = featureTypeMap.get( featureTypes[j].getName() );
                    if ( ft != null ) {
                        String msg = Messages.getMessage( "WFS_CONF_MULTIPLE_FEATURE_TYPE_DEF",
                                                          featureTypes[j].getName(), fileNames[i] );
                        throw new InvalidConfigurationException( msg );
                    }
                    featureTypeMap.put( featureTypes[j].getName(), (MappedFeatureType) featureTypes[j] );
                }
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
                String msg = "Error loading '" + fileNames[i] + "': " + e.getMessage();
                throw new InvalidConfigurationException( msg, e );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                String msg = "Error parsing '" + fileNames[i] + "': " + e.getMessage();
                throw new InvalidConfigurationException( msg, e );
            }
        }
        return featureTypeMap;
    }

    private void buildAndValidatePrefixMap() {
        for ( MappedFeatureType ft : ftMap.values() ) {
            String prefix = ft.getGMLId().getPrefix();
            if ( prefixMap.containsKey( prefix ) ) {
                String msg = Messages.get( "WFS_CONF_FT_PREFICES_NOT_UNIQUE" );
                LOG.logWarning( msg );
                hasUniquePrefixMapping = false;
                break;
            }
            prefixMap.put( prefix, ft );
        }
    }

    /**
     * Creates a (minimal) <code>WFSFeatureType</code> from the given <code>MappedFeatureType</code>.
     *
     * @param mappedFeatureType
     * @throws UnknownCRSException
     */
    private WFSFeatureType createWFSFeatureType( MappedFeatureType mappedFeatureType )
                            throws UnknownCRSException {
        Operation[] operations = new Operation[1];
        operations[0] = new Operation( Operation.QUERY );
        FormatType[] outputFormats = new FormatType[1];
        // according to WFS 1.1.0 spec text/xml; subtype=gml/3.1.1
        // is the only mandatory format
        outputFormats[0] = new FormatType( null, null, null, "text/xml; subtype=gml/3.1.1" );
        CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );
        Envelope[] wgs84BoundingBoxes = new Envelope[1];
        wgs84BoundingBoxes[0] = GeometryFactory.createEnvelope( -180, -90, 180, 90, crs );
        URI defaultSRS = mappedFeatureType.getDefaultSRS();
        URI[] otherSRS = mappedFeatureType.getOtherSRS();
        WFSFeatureType featureType = new WFSFeatureType( mappedFeatureType.getName(), null, null, null, defaultSRS,
                                                         otherSRS, operations, outputFormats, wgs84BoundingBoxes, null );
        return featureType;
    }

    static class XSDFileFilter implements FilenameFilter {

        /**
         * Tests if a specified file should be included in a file list.
         *
         * @param dir
         *            the directory in which the file was found
         * @param name
         *            the name of the file
         * @return <code>true</code> if and only if the name should be included in the file list; <code>false</code>
         *         otherwise
         */
        public boolean accept( File dir, String name ) {
            int pos = name.lastIndexOf( "." );
            String ext = name.substring( pos + 1 );
            return ext.toUpperCase().equals( "XSD" );
        }
    }
}
