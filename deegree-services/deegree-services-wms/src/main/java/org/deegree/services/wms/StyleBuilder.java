//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wms;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.jaxb.wms.DirectStyleType;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreManager;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * Updates a style registry from jaxb beans.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class StyleBuilder {

    private static final Logger LOG = getLogger( StyleBuilder.class );

    private StyleStoreManager styleManager;

    private HashSet<String> soleStyleFiles;

    private HashSet<String> soleLegendFiles;

    private StyleRegistry registry;

    private DeegreeWorkspace workspace;

    StyleBuilder( StyleStoreManager styleManager, HashSet<String> soleStyleFiles, HashSet<String> soleLegendFiles,
                  StyleRegistry registry, DeegreeWorkspace workspace ) {
        this.styleManager = styleManager;
        this.soleStyleFiles = soleStyleFiles;
        this.soleLegendFiles = soleLegendFiles;
        this.registry = registry;
        this.workspace = workspace;
    }

    void parse( String layerName, List<DirectStyleType> styles, XMLAdapter adapter ) {
        File stylesDir = new File( workspace.getLocation(), "styles" );
        for ( DirectStyleType sty : styles ) {
            handleStyleConfig( sty, adapter, stylesDir, layerName, styles );
            handleLegendConfig( sty, adapter, stylesDir, layerName, styles );
        }
    }

    private void handleStyleConfig( DirectStyleType sty, XMLAdapter adapter, File stylesDir, String layerName,
                                    List<DirectStyleType> styles ) {
        try {
            File file = new File( adapter.resolve( sty.getFile() ).toURI() );

            Style style;
            if ( file.getParentFile().equals( stylesDir ) ) {
                // already loaded from style store
                String styleId = file.getName().substring( 0, file.getName().length() - 4 );
                StyleStore store = styleManager.get( styleId );
                if ( store != null ) {
                    style = store.getStyle( null ).copy();
                } else {
                    LOG.warn( "Style store {} was not available, trying to load directly.", styleId );
                    style = registry.loadNoImport( layerName, file, false );
                }
            } else {
                // outside of workspace - load it manually
                style = registry.loadNoImport( layerName, file, false );
            }
            if ( style != null ) {
                String name = sty.getName();
                if ( name != null ) {
                    style.setName( name );
                }
                if ( styles.size() == 1 ) {
                    soleStyleFiles.add( file.getName() );
                }
                registry.put( layerName, style, false );
                if ( sty.getLegendGraphicFile() != null ) {
                    URL url = adapter.resolve( sty.getLegendGraphicFile().getValue() );
                    if ( url.toURI().getScheme().equals( "file" ) ) {
                        File legend = new File( url.toURI() );
                        style.setLegendFile( legend );
                    } else {
                        style.setLegendURL( url );
                    }
                    style.setPrefersGetLegendGraphicUrl( sty.getLegendGraphicFile().isOutputGetLegendGraphicUrl() );
                }
            }
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace", e );
            LOG.info( "Style file '{}' for layer '{}' could not be resolved.", sty.getFile(), layerName );
        }
    }

    private void handleLegendConfig( DirectStyleType sty, XMLAdapter adapter, File stylesDir, String layerName,
                                     List<DirectStyleType> styles ) {
        try {
            if ( sty.getLegendConfigurationFile() != null ) {
                File file = new File( adapter.resolve( sty.getLegendConfigurationFile() ).toURI() );
                Style style;
                if ( file.getParentFile().equals( stylesDir ) ) {
                    // already loaded from style store
                    String styleId = file.getName().substring( 0, file.getName().length() - 4 );
                    StyleStore store = styleManager.get( styleId );
                    if ( store != null ) {
                        style = store.getStyle( null ).copy();
                    } else {
                        LOG.warn( "Style store {} was not available, trying to load directly.", styleId );
                        style = registry.loadNoImport( layerName, file, true );
                    }
                } else {
                    // outside of workspace - load it manually
                    style = registry.loadNoImport( layerName, file, true );
                }

                String name = sty.getName();
                if ( style != null ) {
                    if ( name != null ) {
                        style.setName( name );
                    }
                    if ( styles.size() == 1 ) {
                        soleLegendFiles.add( file.getName() );
                    }
                    registry.putLegend( layerName, style, false );
                }
            }
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace", e );
            LOG.info( "Style file '{}' for layer '{}' could not be resolved.", sty.getFile(), layerName );
        }
    }

}
