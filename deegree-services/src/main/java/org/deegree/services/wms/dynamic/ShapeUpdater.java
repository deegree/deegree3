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

package org.deegree.services.wms.dynamic;

import static org.deegree.services.wms.MapService.fillInheritedInformation;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.model.layers.EmptyLayer;
import org.deegree.services.wms.model.layers.FeatureLayer;
import org.deegree.services.wms.model.layers.Layer;
import org.slf4j.Logger;

/**
 * <code>ShapeUpdater</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(warn = "logs when multiple layers with the same name exist, also when shape layers cannot be loaded", debug = "logs successful changes in the layer structure", trace = "logs stack traces")
public class ShapeUpdater extends LayerUpdater {

    private static final Logger LOG = getLogger( ShapeUpdater.class );

    private File shapeDir;

    private final Layer parent;

    private final MapService service;

    /**
     * @param shapeDir
     * @param parent
     * @param service
     */
    public ShapeUpdater( File shapeDir, Layer parent, MapService service ) {
        this.shapeDir = shapeDir;
        this.parent = parent;
        this.service = service;
    }

    private boolean recurse( File dir, Layer parent ) {
        boolean changed = false;
        File[] fs = dir.listFiles();
        if ( fs != null ) {
            for ( File f : fs ) {
                String nm = f.getName();
                if ( nm.equals( ".svn" ) ) {
                    continue;
                }
                if ( f.isDirectory() ) {
                    Layer newParent = parent.getChild( nm );
                    if ( newParent == null && f.listFiles().length > 0 ) {
                        newParent = new EmptyLayer( nm, nm, parent );
                        if ( service.layers.containsKey( nm ) ) {
                            LOG.warn( "The layer with name '{}' is defined more than once."
                                      + " This may lead to problems.", nm );
                            LOG.warn( "Requesting this name will get you the last defined layer." );
                        }
                        service.layers.put( nm, newParent );
                        LOG.debug( "Loaded category layer {}", nm );
                        changed = true;
                        parent.addOrReplace( newParent );
                    }
                    changed |= recurse( f, newParent );
                } else {
                    if ( nm.length() > 4 ) {
                        String layName = nm.substring( 0, nm.length() - 4 );
                        String fstr = f.toString();
                        if ( nm.toLowerCase().endsWith( ".shp" ) && parent.getChild( layName ) == null ) {
                            try {
                                FeatureLayer lay = new FeatureLayer( layName, layName, parent, fstr );
                                changed = true;
                                if ( service.layers.containsKey( layName ) ) {
                                    LOG.warn( "The layer with name '{}' is defined more than once."
                                              + " This may lead to problems.", layName );
                                    LOG.warn( "Requesting this name will get you the last defined layer." );
                                }
                                service.layers.put( layName, lay );
                                parent.addOrReplace( lay );
                                LOG.debug( "Loaded shape file layer {}", layName );
                                try {
                                    String file = fstr.substring( 0, fstr.length() - 4 );
                                    File sld = new File( file + ".sld" );
                                    if ( !sld.exists() ) {
                                        sld = new File( file + ".SLD" );
                                    }
                                    if ( sld.exists() ) {
                                        changed |= service.registry.register( lay.getName(), sld, true );
                                    }
                                } catch ( FactoryConfigurationError e ) {
                                    LOG.warn( "Could not parse SLD/SE file for layer '{}'.", layName );
                                    LOG.trace( "Stack trace: ", e );
                                }
                            } catch ( FileNotFoundException e ) {
                                LOG.warn( "Shape file {} could not be deployed: {}", layName, e.getLocalizedMessage() );
                                LOG.trace( "Stack trace", e );
                            } catch ( IOException e ) {
                                LOG.warn( "Shape file {} could not be deployed: {}", layName, e.getLocalizedMessage() );
                                LOG.trace( "Stack trace", e );
                            }
                        }
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public boolean update() {
        boolean changed = recurse( shapeDir, parent );
        fillInheritedInformation( parent, parent.getSrs() );
        return changed | cleanup( parent, service );
    }

}
