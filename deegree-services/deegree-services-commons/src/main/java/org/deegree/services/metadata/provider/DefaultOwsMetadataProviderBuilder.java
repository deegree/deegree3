/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.services.metadata.provider;

import static java.util.Collections.emptyList;
import static org.deegree.services.metadata.MetadataUtils.convertFromJAXB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.jaxb.metadata.DatasetMetadataType;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ExtendedCapabilitiesType;
import org.deegree.services.jaxb.metadata.ExternalMetadataAuthorityType;
import org.deegree.services.jaxb.metadata.ExternalMetadataSetIdType;
import org.deegree.services.jaxb.metadata.LanguageStringType;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;

/**
 * This class is responsible for building web service metadata providers.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class DefaultOwsMetadataProviderBuilder implements ResourceBuilder<OWSMetadataProvider> {

    private JAXBElement<DeegreeServicesMetadataType> md;

    private ResourceMetadata<OWSMetadataProvider> metadata;

    public DefaultOwsMetadataProviderBuilder( JAXBElement<DeegreeServicesMetadataType> md,
                                              ResourceMetadata<OWSMetadataProvider> metadata ) {
        this.md = md;
        this.metadata = metadata;
    }

    @Override
    public OWSMetadataProvider build() {
        try {
            Pair<ServiceIdentification, ServiceProvider> smd = convertFromJAXB( md.getValue() );
            Map<String, List<OMElement>> extendedCapabilities = new HashMap<String, List<OMElement>>();
            if ( md.getValue().getExtendedCapabilities() != null ) {
                for ( ExtendedCapabilitiesType ex : md.getValue().getExtendedCapabilities() ) {
                    String version = ex.getProtocolVersions();
                    if ( version == null ) {
                        version = "default";
                    }
                    List<OMElement> list = extendedCapabilities.get( version );
                    if ( list == null ) {
                        list = new ArrayList<OMElement>();
                        extendedCapabilities.put( version, list );
                    }
                    DOMSource domSource = new DOMSource( ex.getAny() );
                    XMLStreamReader xmlStream;
                    try {
                        xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( domSource );
                    } catch ( Exception t ) {
                        throw new ResourceInitException( "Error extracting extended capabilities: " + t.getMessage(), t );
                    }
                    list.add( new XMLAdapter( xmlStream ).getRootElement() );
                }
            }
            List<DatasetMetadata> datasets = convertDatasetMetadataFromJAXB( md.getValue().getDatasetMetadata() );
            Map<String, String> authorities = new HashMap<String, String>();
            if ( md.getValue().getDatasetMetadata() != null ) {
                for ( ExternalMetadataAuthorityType at : md.getValue().getDatasetMetadata().getExternalMetadataAuthority() ) {
                    authorities.put( at.getName(), at.getValue() );
                }
            }
            return new DefaultOWSMetadataProvider( smd.first, smd.second, extendedCapabilities, datasets, authorities,
                                                   metadata );
        } catch ( Exception e ) {
            throw new ResourceInitException( "Unable to read service metadata config: " + e.getLocalizedMessage(), e );
        }
    }

    private List<DatasetMetadata> convertDatasetMetadataFromJAXB( org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType.DatasetMetadata jaxbDatasetMetadata ) {
        List<DatasetMetadata> datasets = new ArrayList<DatasetMetadata>();
        if ( jaxbDatasetMetadata != null ) {
            for ( DatasetMetadataType jaxbEl : jaxbDatasetMetadata.getDataset() ) {
                datasets.add( convertDatasetMetadataFromJAXB( jaxbEl, jaxbDatasetMetadata.getMetadataUrlTemplate() ) );
            }
        }
        return datasets;
    }

    private DatasetMetadata convertDatasetMetadataFromJAXB( DatasetMetadataType jaxbEl, String metadataUrlPattern ) {
        QName name = jaxbEl.getName();
        List<LanguageString> titles = convertToLanguageStrings( jaxbEl.getTitle() );
        List<LanguageString> abstracts = convertToLanguageStrings( jaxbEl.getAbstract() );
        List<Pair<List<LanguageString>, CodeType>> keywords = emptyList();
        String url = buildMetadataUrl( metadataUrlPattern, jaxbEl.getMetadataSetId() );
        List<StringPair> externalUrls = new ArrayList<StringPair>();
        for ( ExternalMetadataSetIdType tp : jaxbEl.getExternalMetadataSetId() ) {
            externalUrls.add( new StringPair( tp.getAuthority(), tp.getValue() ) );
        }
        return new DatasetMetadata( name, titles, abstracts, keywords, url, externalUrls );
    }

    private String buildMetadataUrl( String pattern, String datasetId ) {
        if ( pattern == null || datasetId == null ) {
            return null;
        }
        return StringUtils.replaceAll( pattern, "${metadataSetId}", datasetId );
    }

    private List<LanguageString> convertToLanguageStrings( List<LanguageStringType> strings ) {
        List<LanguageString> languageStrings = new ArrayList<LanguageString>();
        if ( strings != null ) {
            for ( LanguageStringType string : strings ) {
                languageStrings.add( new LanguageString( string.getValue(), string.getLang() ) );
            }
        }
        return languageStrings;
    }

}
