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

package org.deegree.services.wpvs.config;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.configuration.BoundingBoxType;
import org.deegree.commons.configuration.ScaleDenominatorsType;
import org.deegree.commons.datasource.configuration.AbstractGeospatialDataSourceType;
import org.deegree.commons.datasource.configuration.BBoxConstraint;
import org.deegree.commons.datasource.configuration.ConstrainedDatabaseDataSourceType;
import org.deegree.commons.datasource.configuration.FileSetType;
import org.deegree.commons.datasource.configuration.FileType;
import org.deegree.commons.datasource.configuration.GeospatialFileSystemDataSourceType;
import org.deegree.commons.datasource.configuration.ScaleConstraint;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.deegree.services.wpvs.configuration.AbstractFeatureDatasetType;
import org.deegree.services.wpvs.exception.DatasourceException;
import org.deegree.services.wpvs.io.ModelBackend;

/**
 * The <code>ModelDatasetWrapper</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <R>
 *            type to use as a result
 * 
 */
public abstract class ModelDatasetWrapper<R> extends DatasetWrapper<R> {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( ModelDatasetWrapper.class );

    /** span of the default envelope */
    public final static double DEFAULT_SPAN = 0.001;

    // /**
    // * Construct a new model dataset wrapper.
    // *
    // * @param configAdapter
    // * to resolve any file urls against
    // */
    // public ModelDatasetWrapper( XMLAdapter configAdapter ) {
    // super( configAdapter );
    // }

    /**
     * Load the textures from the given directories.
     * 
     * @param afds
     */
    protected void loadTextureDirs( AbstractFeatureDatasetType afds ) {
        List<String> textureDirs = afds.getTextureDirectory();
        if ( !textureDirs.isEmpty() ) {
            for ( String td : textureDirs ) {
                if ( td != null ) {
                    File textureDir = new File( td );
                    if ( textureDir.exists() ) {
                        TexturePool.addTexturesFromDirectory( textureDir );
                    } else {
                        LOG.info( "Ignoring texture directory: " + td + " because it does not exist." );
                    }
                }
            }
        }
    }

    /**
     * Get the initialized datasources
     * 
     * @param mds
     * @param translationToLocalCRS
     *            to the scene 0
     * @param defaultCRS
     *            of the wpvs
     * @param adapter
     *            to use as resolver
     * @return the initialized model datasources
     */
    protected List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> initializeDatasources(
                                                                                                   AbstractFeatureDatasetType mds,
                                                                                                   double[] translationToLocalCRS,
                                                                                                   CRS defaultCRS,
                                                                                                   XMLAdapter adapter ) {
        List<AbstractGeospatialDataSourceType> datasources = mds.getFeatureDataSources();
        List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends = new ArrayList<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>>(
                                                                                                                                                         datasources.size() );
        if ( !datasources.isEmpty() ) {

            // BoundingBoxType parentBBox = mds.getBoundingBox();
            // ScaleDenominatorsType parentScales = mds.getScaleDenominators();
            // first initialize the backends.
            for ( AbstractGeospatialDataSourceType ds : datasources ) {
                if ( ds != null ) {
                    // clarifyConstraints( parentBBox, parentScales, ds );
                    ModelBackend<?> mb = getModelBackend( ds, adapter );
                    if ( mb != null ) {
                        // if ( mb instanceof PostgisBackend ) {
                        // double[] toLocal = getTranslationToLocalCRS();
                        mb.setWPVSTranslationVector( new double[] {
                                                                   translationToLocalCRS[0],
                                                                   translationToLocalCRS[1],
                                                                   ( translationToLocalCRS.length == 3 ? translationToLocalCRS[2]
                                                                                                      : 0 ) } );
                        mb.setWPVSBaseCRS( defaultCRS );
                        // }
                        backends.add( new Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>( ds, mb ) );
                    }
                }
            }
        }
        return backends;
    }

    /**
     * 
     * @param datatype
     * @param parentMaxPixelError
     */
    protected void clarifyInheritance( AbstractFeatureDatasetType datatype, Double parentMaxPixelError ) {
        datatype.setMaxPixelError( clarifyMaxPixelError( parentMaxPixelError, datatype.getMaxPixelError() ) );
    }

    /**
     * Checks and sets the constraints of the given datasource.
     * 
     * @param parentBBox
     *            of the parent
     * @param parentScales
     *            of the parent
     * @param agds
     *            to be clarified.
     */
    protected void clarifyConstraints( BoundingBoxType parentBBox, ScaleDenominatorsType parentScales,
                                       AbstractGeospatialDataSourceType agds ) {
        BBoxConstraint bbc = agds.getBBoxConstraint();
        if ( bbc == null ) {
            bbc = new BBoxConstraint();
            bbc.setBoundingBox( parentBBox );
            agds.setBBoxConstraint( bbc );
        }

        ScaleConstraint sc = agds.getScaleConstraint();
        if ( sc == null ) {
            sc = new ScaleConstraint();
            sc.setScaleDenominators( parentScales );
            agds.setScaleConstraint( sc );
        }

    }

    /**
     * Creates a {@link ModelBackend} from the given datasource.
     * 
     * @param ds
     *            to create the backend from.
     * @return the initialized {@link ModelBackend}
     */
    private ModelBackend<?> getModelBackend( AbstractGeospatialDataSourceType ds, XMLAdapter adapter ) {
        ModelBackend<?> mb = null;
        if ( ds != null ) {
            String id = null;
            String hostURL = null;
            if ( ds instanceof ConstrainedDatabaseDataSourceType ) {
                ConstrainedDatabaseDataSourceType cds = (ConstrainedDatabaseDataSourceType) ds;
                id = cds.getConnectionPoolId();
            } else if ( ds instanceof GeospatialFileSystemDataSourceType ) {
                GeospatialFileSystemDataSourceType gfds = (GeospatialFileSystemDataSourceType) ds;
                List<JAXBElement<? extends FileType>> files = gfds.getAbstractFile();
                if ( files.isEmpty() ) {
                    JAXBElement<? extends FileSetType> fileSet = gfds.getAbstractFileSet();
                    if ( fileSet != null ) {
                        LOG.warn( "Filesets are currently not supported for modeltypes." );
                    }
                } else {
                    id = "FileBackend";
                    if ( files.size() > 1 ) {
                        LOG.warn( "Multiple model filedatasource files declared, currently only the first directory is evaluated." );
                    }
                    // Only interested in the first one.
                    JAXBElement<? extends FileType> dataSource = files.get( 0 );
                    String file = dataSource.getValue().getValue();
                    URL resolvedUrl = resolve( adapter, file );
                    File f = new File( resolvedUrl.getFile() );
                    if ( !f.exists() ) {
                        LOG.warn( "Datasourcefile: " + file + " does not exist." );
                    } else {
                        if ( !f.isDirectory() ) {
                            LOG.warn( "Datasourcefile: " + file + " is not a directory, this may not be." );
                        } else {
                            hostURL = f.getAbsolutePath();
                        }
                    }
                }

            }
            try {
                mb = ModelBackend.getInstance( id, hostURL );
            } catch ( UnsupportedOperationException e ) {
                LOG.error( "Ignoring datasource: " + ds.getDataSourceName() + " because: " + e.getLocalizedMessage(), e );
            } catch ( DatasourceException e ) {
                LOG.error( "Ignoring datasource because: " + e.getLocalizedMessage(), e );
            }

        }

        return mb;
    }

    /**
     * @param translationVector
     * @param coordinateSystem
     * @return a default 3d envelope at 0,0 from the scene.
     */
    public static Envelope createDefaultEnvelope( double[] translationVector, CRS coordinateSystem ) {
        return geomFac.createEnvelope( new double[] { 0, 0, 0 }, new double[] { DEFAULT_SPAN, DEFAULT_SPAN,
                                                                               DEFAULT_SPAN }, coordinateSystem );
    }

    /**
     * @param toLocalCRS
     * @param dsEnvelope
     * @param sceneEnvelope
     * @return the merged scene envelope (in realworld coordinates).
     */
    public Envelope mergeGlobalWithScene( double[] toLocalCRS, Envelope dsEnvelope, Envelope sceneEnvelope ) {
        if ( dsEnvelope != null && ( Math.abs( dsEnvelope.getSpan0() - ModelDatasetWrapper.DEFAULT_SPAN ) > 1E-8 ) ) {

            // convert the global dataset (in wpvs world coordinates) to real world coordinates.
            double[] min = dsEnvelope.getMin().getAsArray();
            double[] max = dsEnvelope.getMax().getAsArray();
            double[] tMin = Arrays.copyOf( min, min.length );
            double[] tMax = Arrays.copyOf( max, max.length );
            tMin[0] += ( -toLocalCRS[0] );
            tMin[1] += ( -toLocalCRS[1] );
            tMax[0] += ( -toLocalCRS[0] );
            tMax[1] += ( -toLocalCRS[1] );
            Envelope tEnv = geomFac.createEnvelope( tMin, tMax, dsEnvelope.getCoordinateSystem() );
            sceneEnvelope = sceneEnvelope.merge( tEnv );
        }
        return sceneEnvelope;
    }
}
