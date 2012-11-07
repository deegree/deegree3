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

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.protocol.wms.ops.SLDParser.getStyles;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.Filter;
import org.deegree.services.jaxb.wms.SLDStyleType;
import org.deegree.services.jaxb.wms.SLDStyleType.LegendGraphicFile;
import org.deegree.style.StyleRef;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreManager;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class SldStyleBuilder {

    private static final Logger LOG = getLogger( StyleBuilder.class );

    private StyleStoreManager styleManager;

    private StyleRegistry registry;

    private DeegreeWorkspace workspace;

    SldStyleBuilder( StyleStoreManager styleManager, StyleRegistry registry, DeegreeWorkspace workspace ) {
        this.styleManager = styleManager;
        this.registry = registry;
        this.workspace = workspace;
    }

    void parse( String layerName, XMLAdapter adapter, List<SLDStyleType> styles ) {
        File stylesDir = new File( workspace.getLocation(), "styles" );

        for ( SLDStyleType sty : styles ) {
            try {
                File file = new File( adapter.resolve( sty.getFile() ).toURI() );

                String namedLayer = sty.getNamedLayer();
                LOG.debug( "Will read styles from SLD '{}', for named layer '{}'.", file, namedLayer );
                Map<String, String> map = new HashMap<String, String>();
                Map<String, Pair<File, URL>> legends = new HashMap<String, Pair<File, URL>>();
                Map<String, Boolean> glgUrls = new HashMap<String, Boolean>();
                extractFromSld( sty, adapter, stylesDir, layerName, legends, glgUrls, map );

                if ( file.getParentFile().equals( stylesDir ) ) {
                    handleSldFromStyleStore( file, namedLayer, map, layerName, legends, glgUrls );
                }

                handleExternalSld( file, namedLayer, layerName, map, legends, glgUrls );
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace", e );
                LOG.info( "Style file '{}' for layer '{}' could not be parsed: '{}'.",
                          new Object[] { sty.getFile(), layerName, e.getLocalizedMessage() } );
            }
        }
    }

    private void extractFromSld( SLDStyleType sty, XMLAdapter adapter, File stylesDir, String layerName,
                                 Map<String, Pair<File, URL>> legends, Map<String, Boolean> glgUrls,
                                 Map<String, String> map )
                            throws Throwable {
        String name = null, lastName = null;
        for ( JAXBElement<?> elem : sty.getNameAndUserStyleAndLegendConfigurationFile() ) {
            if ( elem.getName().getLocalPart().equals( "Name" ) ) {
                name = elem.getValue().toString();
            } else if ( elem.getName().getLocalPart().equals( "LegendConfigurationFile" ) ) {
                File legendFile = new File( adapter.resolve( elem.getValue().toString() ).toURI() );
                Style style = findStyle( legendFile, stylesDir, layerName );

                if ( style != null ) {
                    if ( name != null ) {
                        style.setName( name );
                    }
                    registry.putLegend( layerName, style, false );
                }
            } else if ( elem.getName().getLocalPart().equals( "LegendGraphicFile" ) ) {
                LegendGraphicFile lgf = (LegendGraphicFile) elem.getValue();
                URL url = adapter.resolve( lgf.getValue() );
                if ( url.toURI().getScheme().equals( "file" ) ) {
                    File legend = new File( url.toURI() );
                    legends.put( lastName, new Pair<File, URL>( legend, null ) );
                } else {
                    legends.put( lastName, new Pair<File, URL>( null, url ) );
                }
                glgUrls.put( lastName, lgf.isOutputGetLegendGraphicUrl() );
            } else if ( elem.getName().getLocalPart().equals( "UserStyle" ) ) {
                if ( name == null ) {
                    name = elem.getValue().toString();
                }
                LOG.debug( "Will load user style with name '{}', it will be known as '{}'.", elem.getValue(), name );
                map.put( elem.getValue().toString(), name );
                lastName = name;
                name = null;
            }
        }
    }

    private Style findStyle( File legendFile, File stylesDir, String layerName ) {
        Style style;
        if ( legendFile.getParentFile().equals( stylesDir ) ) {
            // already loaded from style store
            String styleId = legendFile.getName().substring( 0, legendFile.getName().length() - 4 );
            StyleStore store = styleManager.get( styleId );
            if ( store != null ) {
                style = store.getStyle( null ).copy();
            } else {
                LOG.warn( "Style store {} was not available, trying to load directly.", styleId );
                style = registry.loadNoImport( layerName, legendFile, true );
            }
        } else {
            style = registry.loadNoImport( layerName, legendFile, true );
        }
        return style;
    }

    private void handleSldFromStyleStore( File file, String namedLayer, Map<String, String> map, String layerName,
                                          Map<String, Pair<File, URL>> legends, Map<String, Boolean> glgUrls ) {
        // already loaded from workspace
        String styleId = file.getName().substring( 0, file.getName().length() - 4 );
        StyleStore store = styleManager.get( styleId );

        if ( store != null ) {
            LOG.info( "Using SLD file loaded from style store." );
            for ( Style s : store.getAll( namedLayer ) ) {
                s.setName( map.get( s.getName() ) );
                registry.put( layerName, s, false );
                Pair<File, URL> p = legends.get( s.getName() );
                if ( p != null && p.first != null ) {
                    s.setLegendFile( p.first );
                } else if ( p != null ) {
                    s.setLegendURL( p.second );
                }
                s.setPrefersGetLegendGraphicUrl( glgUrls.get( s.getName() ) != null && glgUrls.get( s.getName() ) );
            }
            return;
        }
    }

    private void handleExternalSld( File file, String namedLayer, String layerName, Map<String, String> map,
                                    Map<String, Pair<File, URL>> legends, Map<String, Boolean> glgUrls )
                            throws Throwable {
        LOG.info( "Parsing SLD style file unavailable from style stores." );
        XMLInputFactory fac = XMLInputFactory.newInstance();
        FileInputStream is = new FileInputStream( file );
        try {
            XMLStreamReader in = fac.createXMLStreamReader( file.toURI().toURL().toString(), is );
            Pair<LinkedList<Filter>, LinkedList<StyleRef>> parsedStyles = getStyles( in, namedLayer, map );
            for ( StyleRef s : parsedStyles.second ) {
                if ( !s.isResolved() ) {
                    continue;
                }
                registry.put( layerName, s.getStyle(), false );
                Pair<File, URL> p = legends.get( s.getName() );
                if ( p != null && p.first != null ) {
                    s.getStyle().setLegendFile( p.first );
                } else if ( p != null ) {
                    s.getStyle().setLegendURL( p.second );
                }
                s.getStyle().setPrefersGetLegendGraphicUrl( glgUrls.get( s.getStyle().getName() ) != null
                                                                                    && glgUrls.get( s.getStyle().getName() ) );
            }
        } finally {
            closeQuietly( is );
        }
    }

}
