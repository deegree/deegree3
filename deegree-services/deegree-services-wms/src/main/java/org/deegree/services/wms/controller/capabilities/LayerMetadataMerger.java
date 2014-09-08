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

class LayerMetadataMerger {

    private final OWSMetadataProvider metadata;

    private final String mdUrlTemplate;

    LayerMetadataMerger( final OWSMetadataProvider metadata, final String mdUrlTemplate ) {
        this.metadata = metadata;
        this.mdUrlTemplate = mdUrlTemplate;
    }

    LayerMetadata getLayerTreeMetadata( final Theme theme ) {
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

    DatasetMetadata getDatasetMetadata( final Theme theme, final LayerMetadata layerTreeMetadata ) {
        if ( theme.getLayerMetadata().getName() != null ) {
            final String name = getNameFromThemeOrFirstNamedLayer( theme );
            final DatasetMetadata md = getDatasetMetadataFromProvider( name );
            if ( md != null ) {
                return md;
            }
        }
        return buildDatasetMetadataFromLayerTree( layerTreeMetadata, theme );
    }

    private String getNameFromThemeOrFirstNamedLayer( final Theme theme ) {
        if ( theme.getLayerMetadata().getName() != null ) {
            return theme.getLayerMetadata().getName();
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
        return metadata.getDatasetMetadata( new QName( themeOrLayerName ) );
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

    private String getUrlForMetadataSetId( final String id ) {
        if ( id == null ) {
            return null;
        }
        return replaceAll( mdUrlTemplate, "${metadataSetId}", id );
    }

}
