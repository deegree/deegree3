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

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.deegree.commons.utils.StringUtils.replaceAll;
import static org.deegree.theme.Themes.getAllLayers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.Description;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;
import org.deegree.layer.Layer;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.theme.Theme;
import org.deegree.theme.Themes;

/**
 * Obtains merged {@link LayerMetadata} and {@link DatasetMetadata} objects for {@link Theme} objects.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 *
 * @since 3.3
 */
class ThemeMetadataMerger {

    /**
     * Returns the combined layer metadata for the given theme/sublayers.
     *
     * @see LayerMetadata#merge(LayerMetadata)
     *
     * @param theme
     *            must not be <code>null</code>
     * @return combined layer metadata, never <code>null</code>
     */
    LayerMetadata mergeLayerMetadata( final Theme theme ) {
        final LayerMetadata themeMetadata = theme.getLayerMetadata();
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
     * Returns the combined (least restrictive) scale denominators for the given theme/sublayers.
     *
     * @param theme
     *            must not be <code>null</code>
     * @return combined scale denomiators, first value is min, second is max, never <code>null</code>
     */
    DoublePair mergeScaleDenominators( final Theme theme ) {
        Double min = POSITIVE_INFINITY;
        Double max = NEGATIVE_INFINITY;
        if ( theme.getLayerMetadata() != null && theme.getLayerMetadata().getScaleDenominators() != null ) {
            final DoublePair themeScales = theme.getLayerMetadata().getScaleDenominators();
            if ( !themeScales.first.isInfinite() ) {
                min = themeScales.first;
            }
            if ( !themeScales.second.isInfinite() ) {
                max = themeScales.second;
            }
        }
        final List<Layer> layers = Themes.getAllLayers( theme );
        if ( layers != null ) {
            for ( final Layer layer : layers ) {
                if ( layer.getMetadata() != null ) {
                    final DoublePair layerScales = layer.getMetadata().getScaleDenominators();
                    if ( layerScales != null ) {
                        min = Math.min( min, layerScales.first );
                        max = Math.max( max, layerScales.second );
                    }
                }
            }
        }
        return new DoublePair( min, max );
    }

    /**
     * Returns a {@link DatasetMetadata} object for the given {@link Theme} that combines the metadata provider
     * information with the layer metadata.
     *
     * @param providerMetadata
     *            can be <code>null</code>
     * @param theme
     *            must not be <code>null</code>
     * @param layerMetadata
     *            must not be <code>null</code>
     * @param mdUrlTemplate
     *            can be <code>null</code>
     * @return combined dataset metadata, never <code>null</code>
     */
    DatasetMetadata mergeDatasetMetadata( final DatasetMetadata providerMetadata, final Theme theme,
                                          final LayerMetadata layerMetadata, final String mdUrlTemplate ) {
        final DatasetMetadata layerDatasetMetadata = buildDatasetMetadata( layerMetadata, theme, mdUrlTemplate );
        return merge( providerMetadata, layerDatasetMetadata );
    }

    private DatasetMetadata buildDatasetMetadata( final LayerMetadata layerMetadata, final Theme theme,
                                                  final String mdUrlTemplate ) {
        String localName = layerMetadata.getName();
        if ( localName == null ) {
            localName = "unnamed";
        }
        final QName name = new QName( localName );
        final List<LanguageString> titles = new ArrayList<LanguageString>();
        final List<LanguageString> abstracts = new ArrayList<LanguageString>();
        final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
        final String metadataSetId = getFirstMetadataSetId( theme );
        final String metadataSetUrl = getUrlForMetadataSetId( metadataSetId, mdUrlTemplate );
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

    private DatasetMetadata merge( final DatasetMetadata providerMetadata, final DatasetMetadata layerMetadata ) {
        if ( providerMetadata == null ) {
            return layerMetadata;
        } else if ( layerMetadata == null ) {
            return providerMetadata;
        }
        final QName name = layerMetadata.getQName();
        final List<LanguageString> titles = merge( providerMetadata.getTitles(), layerMetadata.getTitles() );
        final List<LanguageString> abstracts = merge( providerMetadata.getAbstracts(), layerMetadata.getAbstracts() );
        final List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
        if ( providerMetadata.getKeywords() != null ) {
            keywords.addAll( providerMetadata.getKeywords() );
        }
        if ( layerMetadata.getKeywords() != null ) {
            keywords.addAll( layerMetadata.getKeywords() );
        }
        final String url = providerMetadata.getUrl() != null ? providerMetadata.getUrl() : layerMetadata.getUrl();
        final List<StringPair> externalUrls = new ArrayList<StringPair>();
        if ( providerMetadata.getExternalUrls() != null ) {
            externalUrls.addAll( providerMetadata.getExternalUrls() );
        }
        if ( layerMetadata.getExternalUrls() != null ) {
            externalUrls.addAll( layerMetadata.getExternalUrls() );
        }
        return new DatasetMetadata( name, titles, abstracts, keywords, url, externalUrls );
    }

    private List<LanguageString> merge( final List<LanguageString> first, final List<LanguageString> second ) {
        final List<LanguageString> merged = new ArrayList<LanguageString>();
        if ( first != null ) {
            merged.addAll( first );
        }
        if ( second != null ) {
            merged.addAll( second );
        }
        return merged;
    }

    private String getFirstMetadataSetId( final Theme theme ) {
        if ( theme.getLayerMetadata().getMetadataId() != null ) {
            return theme.getLayerMetadata().getMetadataId();
        }
        for ( final Layer layer : getAllLayers( theme ) ) {
            if ( layer.getMetadata().getMetadataId() != null ) {
                return layer.getMetadata().getMetadataId();
            }
        }
        return null;
    }

    private String getUrlForMetadataSetId( final String id, final String mdUrlTemplate ) {
        if ( id == null || mdUrlTemplate == null ) {
            return null;
        }
        return replaceAll( mdUrlTemplate, "${metadataSetId}", id );
    }
}
