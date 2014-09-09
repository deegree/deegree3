/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.services.wms.controller.capabilities;

import static org.deegree.commons.utils.StringUtils.replaceAll;
import static org.deegree.theme.Themes.getAllLayers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;

/**
 * Obtains merged {@link LayerMetadata} and {@link DatasetMetadata} objects for {@link Theme} objects.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * 
 * @since 3.3
 */
class LayerMetadataMerger {

    private final OWSMetadataProvider metadataProvider;

    private final String mdUrlTemplate;

    /**
     * Creates a new {@link LayerMetadata} instance.
     * 
     * @param metadataProvider
     *            provider for metadata on OWS datasets, can be <code>null</code>
     * @param mdUrlTemplate
     *            URL template for requesting metadata records (<code>${metadataSetId}</code> will be replaced with
     *            metadata id), can be <code>null</code>
     */
    LayerMetadataMerger( final OWSMetadataProvider metadataProvider, final String mdUrlTemplate ) {
        this.metadataProvider = metadataProvider;
        this.mdUrlTemplate = mdUrlTemplate;
    }

    /**
     * Returns a {@link LayerMetadata} object that merges information from layers if not defined directly in the
     * {@link LayerMetadata} of the {@link Theme}.
     * 
     * @see LayerMetadata#merge(LayerMetadata)
     * 
     * @param theme
     *            must not be <code>null</code>
     * @return merged layer metadata, never <code>null</code>
     */
    LayerMetadata getLayerTreeMetadata( final Theme theme ) {
        final LayerMetadata themeMetadata = theme.getMetadata();
        LayerMetadata layerMetadata = null;
        for ( final Layer l : Themes.getAllLayers( theme ) ) {
            if ( layerMetadata == null ) {
                layerMetadata = l.getMetadata();
            } else {
                layerMetadata.merge( l.getMetadata() );
            }
        }
        themeMetadata.merge( layerMetadata );
        return themeMetadata;
    }

    /**
     * Returns a {@link DatasetMetadata} for the given {@link Theme}, either obtained from the metadata provider or
     * generated from the given layer metadata (if not available from metadata provider).
     * 
     * @param theme
     *            must not be <code>null</code>
     * @param layerTreeMetadata
     *            must not be <code>null</code>
     * @return dataset metadata, never <code>null</code>
     */
    DatasetMetadata getDatasetMetadata( final Theme theme, final LayerMetadata layerTreeMetadata ) {
        if ( metadataProvider != null && theme.getMetadata().getName() != null ) {
            final String name = getNameFromThemeOrFirstNamedLayer( theme );
            final DatasetMetadata md = getDatasetMetadataFromProvider( name );
            if ( md != null ) {
                return md;
            }
        }
        return buildDatasetMetadataFromLayerTree( layerTreeMetadata, theme );
    }

    private String getNameFromThemeOrFirstNamedLayer( final Theme theme ) {
        if ( theme.getMetadata().getName() != null ) {
            return theme.getMetadata().getName();
        }
        for ( final Layer layer : getAllLayers( theme ) ) {
            if ( layer.getMetadata().getName() != null ) {
                return layer.getMetadata().getName();
            }
        }
        return null;
    }

    private DatasetMetadata getDatasetMetadataFromProvider( final String themeOrLayerName ) {
        if ( themeOrLayerName == null ) {
            return null;
        }
        return metadataProvider.getDatasetMetadata( new QName( themeOrLayerName ) );
    }

    private DatasetMetadata buildDatasetMetadataFromLayerTree( final LayerMetadata layerMetadata, final Theme theme ) {
        String localName = layerMetadata.getName();
        if ( localName == null ) {
            localName = "unnamed";
        }
        final QName name = new QName( localName );
        final List<LanguageString> titles = new ArrayList<LanguageString>();
        final List<LanguageString> abstracts = new ArrayList<LanguageString>();
        final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
        final String metadataSetId = getFirstMetadataSetId( theme );
        final String metadataSetUrl = getUrlForMetadataSetId( metadataSetId );
        final List<StringPair> externalUrls = new ArrayList<StringPair>();
        final Description description = layerMetadata.getDescription();
        if ( description != null ) {
            if ( description.getTitles() != null ) {
                titles.addAll( description.getTitles() );
            }
            if ( description.getAbstracts() != null ) {
                abstracts.addAll( description.getAbstracts() );
            }
            if ( description.getKeywords() != null ) {
                keywords.addAll( description.getKeywords() );
            }
        }
        return new DatasetMetadata( name, titles, abstracts, keywords, metadataSetUrl, externalUrls );
    }

    private String getFirstMetadataSetId( final Theme theme ) {
        if ( theme.getMetadata().getMetadataId() != null ) {
            return theme.getMetadata().getMetadataId();
        }
        for ( final Layer layer : getAllLayers( theme ) ) {
            if ( layer.getMetadata().getMetadataId() != null ) {
                return layer.getMetadata().getMetadataId();
            }
        }
        return null;
    }

    private String getUrlForMetadataSetId( final String id ) {
        if ( id == null ) {
            return null;
        }
        return replaceAll( mdUrlTemplate, "${metadataSetId}", id );
    }

}
