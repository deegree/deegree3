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

package org.deegree.services.wpvs.io.file;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.index.PositionableModel;
import org.deegree.commons.utils.FileUtils;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.BillBoard;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.BuildingRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.RenderableManager;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.RenderablePrototype;
import org.deegree.services.wpvs.config.RenderableDataset;
import org.deegree.services.wpvs.io.BackendResult;
import org.deegree.services.wpvs.io.DataObjectInfo;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackendInfo;
import org.deegree.services.wpvs.io.serializer.PrototypeSerializer;
import org.deegree.services.wpvs.io.serializer.WROSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>FileBackend</code> is the access to the model in files on the local file system.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileBackend extends ModelBackend<Envelope> {

    private final static Logger LOG = LoggerFactory.getLogger( FileBackend.class );

    private ModelFile<BillBoard> treeFile;

    private ModelFile<WorldRenderableObject> buildingFile;

    private ModelFile<RenderablePrototype> prototypeFile;

    /**
     * @param billboardFile
     * @throws IOException
     */
    public FileBackend( File billboardFile ) throws IOException {
        treeFile = getTreeFile( billboardFile );
    }

    /**
     * 
     * @param entityFile
     * @param prototypeFile
     * @throws IOException
     */
    public FileBackend( File entityFile, File prototypeFile ) throws IOException {
        buildingFile = getBuildingFile( entityFile );
        this.prototypeFile = getPrototypeFile( prototypeFile );
    }

    /**
     * @param entityFile
     * @throws IOException
     */
    private ModelFile<WorldRenderableObject> getBuildingFile( File entityFile )
                            throws IOException {
        File[] files = mapFileType( entityFile );

        if ( !files[0].exists() ) {
            throw new IOException( "The given entity backend data file: " + files[0].getAbsolutePath()
                                   + " does not exist." );
        }
        if ( !files[1].exists() ) {
            throw new IOException( "The given entity backend index file: " + files[1].getAbsolutePath()
                                   + " does not exist." );
        }
        if ( !files[2].exists() ) {
            throw new IOException( "The given entity backend info file: " + files[2].getAbsolutePath()
                                   + " does not exist." );
        }
        return new ModelFile<WorldRenderableObject>( new IndexFile( files[0] ),
                                                     new DataFile<WorldRenderableObject>( files[1],
                                                                                          getBuildingSerializer() ),
                                                     files[2] );
    }

    /**
     * @param entityFile
     * @throws IOException
     */
    private ModelFile<RenderablePrototype> getPrototypeFile( File entityFile )
                            throws IOException {
        File[] files = mapFileType( entityFile );
        return new ModelFile<RenderablePrototype>( new IndexFile( files[0] ),
                                                   new DataFile<RenderablePrototype>( files[1],
                                                                                      getPrototypeSerializer() ),
                                                   files[2] );
    }

    /**
     * 
     * @throws IOException
     */
    private ModelFile<BillBoard> getTreeFile( File bilboardFile )
                            throws IOException {
        File[] files = mapFileType( bilboardFile );
        if ( !files[0].exists() ) {
            throw new IOException( "The given billboard backend data file: " + files[0].getAbsolutePath()
                                   + " does not exist." );
        }
        if ( !files[1].exists() ) {
            throw new IOException( "The given billboard backend index file: " + files[1].getAbsolutePath()
                                   + " does not exist." );
        }

        return new ModelFile<BillBoard>( new IndexFile( files[0] ), new DataFile<BillBoard>( files[1],
                                                                                             getTreeSerializer() ),
                                         files[2] );
    }

    private static File[] mapFileType( File entityFile ) {
        String filepath = FileUtils.getBasename( entityFile );
        File data = new File( filepath + ".bin" );
        File idx = new File( filepath + ".idx" );
        File info = new File( filepath + ".info" );
        return new File[] { idx, data, info };
    }

    @Override
    public Envelope createBackendEnvelope( Envelope geometry, int dimension ) {
        return geometry;
    }

    @Override
    public Envelope createEnvelope( Envelope someGeometry ) {
        return someGeometry;
    }

    @Override
    public Object getDeSerializedObjectForUUID( Type objectType, String uuid )
                            throws IOException {
        ModelFile<?> mf = mapTypeToFile( objectType );
        return mf.getObject( uuid );
    }

    /**
     * @param objectType
     * @return
     */
    private ModelFile<? extends PositionableModel> mapTypeToFile( Type objectType ) {
        switch ( objectType ) {
        case TREE:
            return treeFile;
        case PROTOTYPE:
            return prototypeFile;
        default:
            return buildingFile;
        }
    }

    @Override
    protected String getDriverPrefix() {
        return "";
    }

    @Override
    public void loadBuildings( BuildingRenderer bm, CRS baseCRS ) {
        if ( bm != null ) {
            try {
                WROSerializer serializer = getBuildingSerializer();
                serializer.setGeometryBuffer( bm.getGeometryBuffer() );
                List<DataObjectInfo<WorldRenderableObject>> readAllFromFile = buildingFile.readAllFromFile( baseCRS );
                Envelope datasetEnvelope = buildingFile.getBackendInfo().getDatasetEnvelope();
                if ( datasetEnvelope == null ) {
                    LOG.warn( "Could not determine the envelope of the buildings, this is strange!" );
                } else {
                    LOG.debug( "The envelope of the buildings: " + datasetEnvelope );
                    // bm.setValidDomain( datasetEnvelope );
                    if ( bm.getValidDomain() == null
                         || ( Math.abs( bm.getValidDomain().getSpan0() - RenderableDataset.DEFAULT_SPAN ) < 1E-8 ) ) {
                        bm.setValidDomain( datasetEnvelope );
                    }
                }
                for ( DataObjectInfo<WorldRenderableObject> doi : readAllFromFile ) {
                    WorldRenderableObject rp = doi.getData();
                    rp.setId( doi.getUuid() );
                    rp.setTime( new Timestamp( doi.getTime() ).toString() );
                    rp.setExternalReference( doi.getExternalRef() );
                    rp.setName( doi.getName() );
                    rp.setType( doi.getType() );
                    bm.add( rp );
                }
            } catch ( IOException e ) {
                LOG.error( "Could not read buildings from file backend because: " + e.getLocalizedMessage(), e );
            }
        }
    }

    @Override
    public List<RenderablePrototype> loadProtoTypes( DirectGeometryBuffer geometryBuffer, CRS baseCRS ) {
        List<RenderablePrototype> result = new LinkedList<RenderablePrototype>();
        try {
            PrototypeSerializer serializer = getPrototypeSerializer();
            serializer.setGeometryBuffer( geometryBuffer );
            List<DataObjectInfo<RenderablePrototype>> readAllFromFile = prototypeFile.readAllFromFile( baseCRS );
            Envelope datasetEnvelope = prototypeFile.getBackendInfo().getDatasetEnvelope();
            if ( datasetEnvelope == null && !readAllFromFile.isEmpty() ) {
                LOG.warn( "Could not determine the envelope of the prototypes, this is strange!" );
            } else {
                LOG.debug( "The envelope of the prototypes: " + datasetEnvelope );
            }
            for ( DataObjectInfo<RenderablePrototype> doi : readAllFromFile ) {
                RenderablePrototype rp = doi.getData();
                rp.setId( doi.getUuid() );
                rp.setTime( new Timestamp( doi.getTime() ).toString() );
                rp.setExternalReference( doi.getExternalRef() );
                rp.setName( doi.getName() );
                rp.setType( doi.getType() );
                result.add( rp );
            }
        } catch ( IOException e ) {
            LOG.error( "Could not read prototypes from file backend because: " + e.getLocalizedMessage(), e );
        }
        return result;
    }

    @Override
    public void loadTrees( TreeRenderer tm, CRS baseCRS ) {

        if ( tm != null ) {
            try {
                List<DataObjectInfo<BillBoard>> readAllFromFile = treeFile.readAllFromFile( baseCRS );
                Envelope datasetEnvelope = treeFile.getBackendInfo().getDatasetEnvelope();
                if ( datasetEnvelope == null ) {
                    LOG.warn( "Could not determine the envelope of the buildings, this is strange!" );
                } else {
                    LOG.debug( "The envelope of the trees: " + datasetEnvelope );
                    if ( tm.getValidDomain() == null
                         || ( Math.abs( tm.getValidDomain().getSpan0() - RenderableDataset.DEFAULT_SPAN ) < 1E-8 ) ) {
                        // no envelope was known (an old modelfile was read?)
                        tm.setValidDomain( datasetEnvelope );
                    }
                }

                for ( DataObjectInfo<BillBoard> doi : readAllFromFile ) {
                    tm.add( doi.getData() );
                }
            } catch ( IOException e ) {
                LOG.error( "Could not read trees from file backend because: " + e.getLocalizedMessage(), e );
            }
        }
    }

    @Override
    public BackendResult delete( String uuid, Type objectType, int qualityLevel, String sqlWhere )
                            throws IOException {
        throw new UnsupportedOperationException( "Deleting of objects is currently not supported by the filebackend." );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends PositionableModel> BackendResult insert( List<DataObjectInfo<P>> objects, Type objectType )
                            throws IOException {
        BackendResult result = new BackendResult();
        Iterator<DataObjectInfo<P>> iterator = objects.iterator();
        if ( objectType == Type.TREE ) {
            while ( iterator.hasNext() ) {
                if ( treeFile.add( (DataObjectInfo<BillBoard>) iterator.next() ) ) {
                    result.insertCount++;
                }
            }
        } else if ( objectType == Type.PROTOTYPE ) {
            while ( iterator.hasNext() ) {
                if ( prototypeFile.add( (DataObjectInfo<RenderablePrototype>) iterator.next() ) ) {
                    result.insertCount++;
                }
            }
        } else {
            while ( iterator.hasNext() ) {
                if ( buildingFile.add( (DataObjectInfo<WorldRenderableObject>) iterator.next() ) ) {
                    result.insertCount++;
                }
            }
        }
        return result;
    }

    @Override
    public void flush()
                            throws IOException {
        // treeFile.close();
        buildingFile.close();
        prototypeFile.close();
    }

    @Override
    public ModelBackendInfo getBackendInfo( org.deegree.services.wpvs.io.ModelBackend.Type type ) {
        switch ( type ) {
        case TREE:
            return treeFile.getBackendInfo();
        case BUILDING:
        case STAGE:
            return buildingFile.getBackendInfo();
        case PROTOTYPE:
            return prototypeFile.getBackendInfo();
        }
        return null;
    }

    @Override
    public List<Object> getDeSerializedObjectsForSQL( Type objectType, String sqlWhere ) {
        throw new UnsupportedOperationException( "Updating is currently not supported in the file backend." );
    }

    @Override
    public void loadEntities( RenderableManager<?> renderer, CRS baseCRS ) {
        if ( this.treeFile != null ) {
            loadTrees( (TreeRenderer) renderer, baseCRS );
        } else {
            loadBuildings( (BuildingRenderer) renderer, baseCRS );
        }
    }

    @Override
    public boolean isBillboard() {
        return this.treeFile != null;
    }

    public static void initFiles( File entityFile )
                            throws IOException {
        File[] files = mapFileType( entityFile );
        for ( File file : files ) {
            LOG.info( "Ensuring that file '" + file + "' exists..." );
            if ( !file.exists() ) {
                LOG.info( "Not yet. Creating it." );
                file.createNewFile();
            }
        }
    }
}
