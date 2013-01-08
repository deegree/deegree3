//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.services.wmts.controller;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.services.wmts.jaxb.DeegreeWMTS;
import org.deegree.services.wmts.jaxb.FeatureInfoFormatsType;
import org.deegree.theme.Theme;
import org.deegree.theme.persistence.ThemeManager;
import org.slf4j.Logger;

/**
 * Responsible for extracting/parsing WMTS jaxb config beans.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

class WmtsBuilder {

    private static final Logger LOG = getLogger( WmtsBuilder.class );

    static final String CONFIG_JAXB_PACKAGE = "org.deegree.services.wmts.jaxb";

    static final String CONFIG_SCHEMA = "/META-INF/schemas/wmts/3.2.0/wmts.xsd";

    private String metadataUrlTemplate;

    private ArrayList<Theme> themes;

    private FeatureInfoFormatsType featureInfoConf;

    WmtsBuilder( XMLAdapter controllerConf, DeegreeWorkspace workspace ) throws ResourceInitException {
        DeegreeWMTS conf;
        try {
            conf = (DeegreeWMTS) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, controllerConf, workspace );
        } catch ( JAXBException e ) {
            LOG.error( "Could not load service configuration: '{}'", e.getLinkedException().getMessage() );
            throw new ResourceInitException( "Error parsing service configuration: "
                                             + e.getLinkedException().getMessage(), e );
        }

        ThemeManager mgr = workspace.getSubsystemManager( ThemeManager.class );

        this.metadataUrlTemplate = conf.getMetadataURLTemplate();

        themes = new ArrayList<Theme>();

        List<String> ids = conf.getServiceConfiguration().getThemeId();
        for ( String id : ids ) {
            Theme t = mgr.get( id );
            if ( t == null ) {
                LOG.warn( "Theme with id {} was not available.", id );
                continue;
            }
            themes.add( t );
        }

        featureInfoConf = conf.getFeatureInfoFormats();
    }

    String getMetadataUrlTemplate() {
        return metadataUrlTemplate;
    }

    List<Theme> getThemes() {
        return themes;
    }

    FeatureInfoFormatsType getFeatureInfoFormatsConf() {
        return featureInfoConf;
    }

}
