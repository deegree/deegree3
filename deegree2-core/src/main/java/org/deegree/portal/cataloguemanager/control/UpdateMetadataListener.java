//$HeadURL: 
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.portal.cataloguemanager.control;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.portal.cataloguemanager.model.CICitation;
import org.deegree.portal.cataloguemanager.model.CIOnlineResource;
import org.deegree.portal.cataloguemanager.model.Date;
import org.deegree.portal.cataloguemanager.model.DistributionFormat;
import org.deegree.portal.cataloguemanager.model.DistributionInfo;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.cataloguemanager.model.Linkage;
import org.deegree.portal.cataloguemanager.model.MDDataIdentification;
import org.deegree.portal.cataloguemanager.model.MDDigitalTransferOptions;
import org.deegree.portal.cataloguemanager.model.MDDistribution;
import org.deegree.portal.cataloguemanager.model.MDFormat;
import org.deegree.portal.cataloguemanager.model.MDLegalConstraints;
import org.deegree.portal.cataloguemanager.model.MDMetadata;
import org.deegree.portal.cataloguemanager.model.MDReferenceSystem;
import org.deegree.portal.cataloguemanager.model.MDRepresentativeFraction;
import org.deegree.portal.cataloguemanager.model.MetadataBean;
import org.deegree.portal.cataloguemanager.model.Name;
import org.deegree.portal.cataloguemanager.model.OnLine;
import org.deegree.portal.cataloguemanager.model.ParentIdentifier;
import org.deegree.portal.cataloguemanager.model.ResourceConstraints;
import org.deegree.portal.cataloguemanager.model.SpatialResolution;
import org.deegree.portal.cataloguemanager.model.TransferOptions;
import org.deegree.portal.cataloguemanager.model.Version;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class UpdateMetadataListener extends InsertMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( UpdateMetadataListener.class );

    @Override
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        try {
            conf = getCatalogueManagerConfiguration( event );
            MetadataBean bean = (MetadataBean) event.getAsBean();

            JAXBContext jc = JAXBContext.newInstance( "org.deegree.portal.cataloguemanager.model" );
            Unmarshaller u = jc.createUnmarshaller();

            String tmp = conf.getTemplateURL( bean.getHlevel() );
            URL url = conf.resolve( tmp );
            MDMetadata mdMetadata = (MDMetadata) u.unmarshal( url );
            tmp = bean.getHlevel();
            if ( "series".equalsIgnoreCase( tmp ) ) {
                tmp = "datasetcollection";
            }
            mdMetadata.getHierarchyLevelName().setCharacterString( tmp );

            // metadata settings
            mdMetadata.getFileIdentifier().setCharacterString( bean.getIdentifier() );
            if ( "dataset".equalsIgnoreCase( bean.getHlevel() ) && bean.getParentId() != null ) {
                ParentIdentifier pi = new ParentIdentifier();
                pi.setCharacterString( bean.getParentId() );
                mdMetadata.setParentIdentifier( pi );
            }
            setContact( mdMetadata, bean );
            mdMetadata.getDateStamp().setDateTime( TimeTools.getISOFormattedTime() );
            if ( bean.getCrs() != null ) {
                MDReferenceSystem mdRef = mdMetadata.getReferenceSystemInfo().getMDReferenceSystem();
                mdRef.getReferenceSystemIdentifier().getRSIdentifier().getCode().setCharacterString( bean.getCrs() );
            }

            // data identification settings
            MDDataIdentification mdDataId = mdMetadata.getIdentificationInfo().getMDDataIdentification();
            mdDataId.getAbstract().setCharacterString( bean.getAbstract_() );
            // data metadata just will be include one set of keywords
            setKeywords( mdDataId.getDescriptiveKeywords().get( 0 ), bean );
            setInspireKeyword( mdDataId.getDescriptiveKeywords().get( 0 ), bean );
            setPointOfContact( mdDataId.getPointOfContact(), bean );
            mdDataId.getTopicCategory().setMDTopicCategoryCode( bean.getTopCat() );
            setExtent( mdDataId.getExtent().getEXExtent(), bean );
            SpatialResolution spr = mdDataId.getSpatialResolution();
            if ( bean.getScale() == null || bean.getScale().trim().length() == 0 ) {
                mdDataId.setSpatialResolution( null );
            } else {
                MDRepresentativeFraction frac = spr.getMDResolution().getEquivalentScale().getMDRepresentativeFraction();
                frac.getDenominator().setInteger( Integer.parseInt( bean.getScale().trim() ) );
            }

            CICitation citation = mdDataId.getCitation().getCICitation();
            citation.getTitle().setCharacterString( bean.getDatasetTitle() );
            citation.getIdentifier().getMDIdentifier().getCode().setCharacterString( bean.getIdentifier() );

            // citation dates
            List<Date> dates = citation.getDate();
            dates.clear();
            if ( bean.getCreation() != null ) {
                addDate( dates, "creation", bean.getCreation() );
            }
            if ( bean.getPublication() != null ) {
                addDate( dates, "publication", bean.getPublication() );
            }
            if ( bean.getRevision() != null ) {
                addDate( dates, "revision", bean.getRevision() );
            }

            // data access
            if ( bean.getTransferOnline() != null && bean.getTransferOnline().trim().length() > 0 ) {
                setDistributionInfo( mdMetadata, bean );
            } else {
                mdMetadata.setDistributionInfo( null );
            }

            // lineage
            if ( bean.getLineage() != null && bean.getLineage().trim().length() > 0 ) {
                mdMetadata.getDataQualityInfo().getDQDataQuality().getLineage().getLILineage().getStatement().setCharacterString(
                                                                                                                                  bean.getLineage() );
            } else {
                mdMetadata.setDataQualityInfo( null );
            }

            // resource constraints
            List<ResourceConstraints> list = mdMetadata.getIdentificationInfo().getMDDataIdentification().getResourceConstraints();
            if ( bean.getAccessConstraints() == null ) {
                list.clear();
            } else {
                MDLegalConstraints legal = null;
                for ( ResourceConstraints resourceConstraints : list ) {
                    if ( resourceConstraints.getMDLegalConstraints() != null ) {
                        legal = resourceConstraints.getMDLegalConstraints();
                        legal.getAccessConstraints().getMDRestrictionCode().setCodeListValue(
                                                                                              bean.getAccessConstraints() );
                        legal.getAccessConstraints().getMDRestrictionCode().setValue( bean.getAccessConstraints() );
                    }
                }
            }

            updateCSW( responseHandler, mdMetadata );

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }

    }

    private void setDistributionInfo( MDMetadata mdMetadata, MetadataBean bean ) {
        if ( mdMetadata.getDistributionInfo() == null ) {
            DistributionInfo distInfo = new DistributionInfo();
            mdMetadata.setDistributionInfo( distInfo );
        }
        MDDistribution mddist = mdMetadata.getDistributionInfo().getMDDistribution();
        if ( mddist == null ) {
            mddist = new MDDistribution();
            TransferOptions to = new TransferOptions();
            MDDigitalTransferOptions dto = new MDDigitalTransferOptions();
            OnLine ol = new OnLine();
            CIOnlineResource ciolr = new CIOnlineResource();
            ciolr.setLinkage( new Linkage() );
            ol.setCIOnlineResource( ciolr );
            dto.setOnLine( ol );
            to.setMDDigitalTransferOptions( dto );
            mddist.setTransferOptions( to );
            mdMetadata.getDistributionInfo().setMDDistribution( mddist );
        }
        MDDigitalTransferOptions to = mddist.getTransferOptions().getMDDigitalTransferOptions();
        to.getOnLine().getCIOnlineResource().getLinkage().setURL( bean.getTransferOnline() );
        if ( bean.getTransferFormatName() != null && bean.getTransferFormatName().length() > 0 ) {
            DistributionFormat format = mdMetadata.getDistributionInfo().getMDDistribution().getDistributionFormat();
            if ( format == null ) {
                format = new DistributionFormat();
                MDFormat mdf = new MDFormat();
                mdf.setName( new Name() );
                mdf.setVersion( new Version() );
                format.setMDFormat( mdf );
                mdMetadata.getDistributionInfo().getMDDistribution().setDistributionFormat( format );
            }
            format.getMDFormat().getName().setCharacterString( bean.getTransferFormatName() );
            if ( bean.getTransferFormatVersion() != null && bean.getTransferFormatVersion().length() > 0 ) {
                format.getMDFormat().getVersion().setCharacterString( bean.getTransferFormatVersion() );
            } else {
                format.getMDFormat().getVersion().setCharacterString( "unknown" );
            }
        } else {
            mdMetadata.getDistributionInfo().getMDDistribution().setDistributionFormat( null );
        }
    }

    @SuppressWarnings("unchecked")
    private String updateCSW( ResponseHandler responseHandler, MDMetadata mdMetadata )
                            throws JAXBException, IOException, HttpException, SAXException {

        JAXBContext jc = JAXBContext.newInstance( "org.deegree.portal.cataloguemanager.model" );
        Marshaller m = jc.createMarshaller();

        ByteArrayOutputStream bos = new ByteArrayOutputStream( 10000 );
        m.marshal( mdMetadata, bos );

        URL url = getClass().getResource( "/org/deegree/portal/cataloguemanager/control/resources/update.xml" );
        String s = FileUtils.readTextFile( url ).toString();
        String t = new String( bos.toByteArray() );
        int c = t.indexOf( "?>" );
        t = t.substring( c + 2 );
        s = StringTools.replace( s, "$id$", mdMetadata.getFileIdentifier().getCharacterString(), false );
        s = StringTools.replace( s, "$data$", t, false );
        String csw = conf.getCatalogueURL();

        XMLFragment xml = new XMLFragment( new StringReader( s ), XMLFragment.DEFAULT_URL );
        Enumeration<String> en = ( (HttpServletRequest) getRequest() ).getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        while ( en.hasMoreElements() ) {
            String name = (String) en.nextElement();
            if ( !name.equalsIgnoreCase( "accept-encoding" ) && !name.equalsIgnoreCase( "content-length" )
                 && !name.equalsIgnoreCase( "user-agent" ) ) {
                map.put( name, ( (HttpServletRequest) getRequest() ).getHeader( name ) );
            }
        }
        HttpMethod post = HttpUtils.performHttpPost( csw, xml, 60000, null, null, map );
        s = post.getResponseBodyAsString();
        if ( s.toLowerCase().indexOf( "exception" ) > -1 ) {
            ExceptionBean eb = new ExceptionBean( getClass().getName(), "update failed" );
            responseHandler.writeAndClose( true, eb );
        } else {
            responseHandler.writeAndClose( "insert performed" );
        }

        return s;
    }

}
