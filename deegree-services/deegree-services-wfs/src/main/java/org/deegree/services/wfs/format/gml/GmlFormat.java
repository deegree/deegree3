//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wfs.format.gml;

import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;

import java.io.IOException;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.wfs.GMLFormat.GetFeatureResponse;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.format.Format;
import org.deegree.services.wfs.format.gml.request.GmlDescribeFeatureTypeHandler;
import org.deegree.services.wfs.format.gml.request.GmlGetFeatureHandler;
import org.deegree.services.wfs.format.gml.request.GmlGetGmlObjectHandler;
import org.deegree.services.wfs.format.gml.request.GmlGetPropertyValueHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link Format} implementation that can handle GML 2/3.0/3.1/3.2 and the specific requirements for WFS 2.0
 * response <code>FeatureCollection</code>s (which are not GML feature collections in a strict sense).
 * <p>
 * NOTE: For WFS 1.1.0, some schema communities decided to use a different feature collection element than
 * <code>wfs:FeatureCollection</code>, mostly because <code>wfs:FeatureCollection</code> is bound to GML 3.1. This
 * practice is supported by this {@link Format} implementation for WFS 1.0.0 and WFS 1.1.0 output. However, for WFS 2.0,
 * there's hope that people will refrain from doing so (as WFS 2.0 <code>FeatureCollection</code> allows GML 3.2 output
 * and is not bound to any specific GML version). Therefore, it is currently not supported to use any different output
 * container for WFS 2.0.
 * </p>
 * 
 * @author <a href="mailto:wanhoff@lat-lon.de">Jeronimo Wanhoff</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GmlFormat implements Format {

    private static final Logger LOG = LoggerFactory.getLogger( GmlFormat.class );

    private final GmlFormatOptions options;

    private final GmlDescribeFeatureTypeHandler dftHandler;

    private final GmlGetFeatureHandler gfHandler;

    private final GmlGetPropertyValueHandler gpvHandler;

    private final GmlGetGmlObjectHandler ggoHandler;

    private final WebFeatureService master;

    public GmlFormat( WebFeatureService master, GMLVersion gmlVersion ) {
        this.master = master;
        this.options = new GmlFormatOptions( gmlVersion, null, null, null, false, false, master.getQueryMaxFeatures(),
                                             master.getCheckAreaOfUse(), null, null, gmlVersion.getMimeType(), false );
        this.dftHandler = new GmlDescribeFeatureTypeHandler( this );
        this.gfHandler = new GmlGetFeatureHandler( this );
        this.gpvHandler = new GmlGetPropertyValueHandler( this );
        this.ggoHandler = new GmlGetGmlObjectHandler( this );
    }

    public GmlFormat( WebFeatureService master, org.deegree.services.jaxb.wfs.GMLFormat formatDef )
                            throws ResourceInitException {
        this.master = master;

        boolean generateBoundedByForFeatures = false, disableStreaming = false;
        if ( formatDef.isGenerateBoundedByForFeatures() != null ) {
            generateBoundedByForFeatures = formatDef.isGenerateBoundedByForFeatures();
        }

        QName responseContainerEl = null, responseFeatureMemberEl = null;
        String schemaLocation = null, appSchemaBaseURL = null;

        GetFeatureResponse responseConfig = formatDef.getGetFeatureResponse();
        boolean exportOriginalSchema = false;
        if ( responseConfig != null ) {
            if ( responseConfig.isDisableStreaming() != null ) {
                disableStreaming = responseConfig.isDisableStreaming();
            }
            if ( responseConfig.getContainerElement() != null ) {
                responseContainerEl = responseConfig.getContainerElement();
            }
            if ( responseConfig.getFeatureMemberElement() != null ) {
                responseFeatureMemberEl = responseConfig.getFeatureMemberElement();
            }
            if ( responseConfig.getAdditionalSchemaLocation() != null ) {
                schemaLocation = responseConfig.getAdditionalSchemaLocation();
            }
            if ( responseConfig.getDisableDynamicSchema() != null ) {
                exportOriginalSchema = responseConfig.getDisableDynamicSchema().isValue();
                appSchemaBaseURL = responseConfig.getDisableDynamicSchema().getBaseURL();
                if ( appSchemaBaseURL != null && appSchemaBaseURL.endsWith( "/" ) ) {
                    appSchemaBaseURL = appSchemaBaseURL.substring( 0, appSchemaBaseURL.length() - 1 );
                }
                if ( appSchemaBaseURL != null && appSchemaBaseURL.isEmpty() ) {
                    appSchemaBaseURL = null;
                }
            }
        }

        int queryMaxFeatures = master.getQueryMaxFeatures();
        boolean checkAreaOfUse = master.getCheckAreaOfUse();

        CoordinateFormatter formatter = null;
        try {
            JAXBElement<?> formatterEl = formatDef.getAbstractCoordinateFormatter();
            if ( formatterEl != null ) {
                Object formatterConf = formatterEl.getValue();
                if ( formatterConf instanceof org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter ) {
                    LOG.info( "Setting up configured DecimalCoordinateFormatter." );
                    org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter decimalFormatterConf = (org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter) formatterConf;
                    formatter = new DecimalCoordinateFormatter( decimalFormatterConf.getPlaces().intValue() );
                } else if ( formatterConf instanceof org.deegree.services.jaxb.wfs.CustomCoordinateFormatter ) {
                    LOG.info( "Setting up CustomCoordinateFormatter." );
                    org.deegree.services.jaxb.wfs.CustomCoordinateFormatter customFormatterConf = (org.deegree.services.jaxb.wfs.CustomCoordinateFormatter) formatterConf;
                    formatter = (CoordinateFormatter) Class.forName( customFormatterConf.getJavaClass() ).newInstance();
                } else {
                    LOG.warn( "Unexpected JAXB type '" + formatterConf.getClass() + "'." );
                }
            }
        } catch ( Exception e ) {
            throw new ResourceInitException( "Error initializing coordinate formatter: " + e.getMessage(), e );
        }

        GMLVersion gmlVersion = GMLVersion.valueOf( formatDef.getGmlVersion().value() );
        String mimeType = formatDef.getMimeType().get( 0 );
        this.options = new GmlFormatOptions( gmlVersion, responseContainerEl, responseFeatureMemberEl, schemaLocation,
                                             disableStreaming, generateBoundedByForFeatures, queryMaxFeatures,
                                             checkAreaOfUse, formatter, appSchemaBaseURL, mimeType,
                                             exportOriginalSchema );

        // initialize handlers
        this.dftHandler = new GmlDescribeFeatureTypeHandler( this );
        this.gfHandler = new GmlGetFeatureHandler( this );
        this.gpvHandler = new GmlGetPropertyValueHandler( this );
        this.ggoHandler = new GmlGetGmlObjectHandler( this );
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public void doDescribeFeatureType( DescribeFeatureType request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {
        dftHandler.doDescribeFeatureType( request, response );
    }

    @Override
    public void doGetFeature( GetFeature request, HttpResponseBuffer response )
                            throws Exception {
        ResultType type = request.getPresentationParams().getResultType();
        if ( type == RESULTS || type == null ) {
            gfHandler.doGetFeatureResults( request, response );
        } else {
            gfHandler.doGetFeatureHits( request, response );
        }
    }

    @Override
    public void doGetGmlObject( GetGmlObject request, HttpResponseBuffer response )
                            throws Exception {
        ggoHandler.doSingleObjectResponse( request.getVersion(), request.getTraverseXlinkDepth(),
                                           request.getRequestedId(), response );
    }

    @Override
    public void doGetPropertyValue( GetPropertyValue request, HttpResponseBuffer response )
                            throws Exception {
        ResultType type = request.getPresentationParams().getResultType();
        if ( type == RESULTS || type == null ) {
            gpvHandler.doGetPropertyValueResult( request, response );
        } else {
            gpvHandler.doGetPropertyValueHits( request, response );
        }
    }

    /**
     * @return the master
     */
    public WebFeatureService getMaster() {
        return master;
    }

    /**
     * @return the options to control GML output
     */
    public GmlFormatOptions getGmlFormatOptions() {
        return options;
    }
}
