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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.portal.cataloguemanager.model.CIAddress;
import org.deegree.portal.cataloguemanager.model.CIContact;
import org.deegree.portal.cataloguemanager.model.CIResponsibleParty;
import org.deegree.portal.cataloguemanager.model.EXExtent;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.cataloguemanager.model.Keyword;
import org.deegree.portal.cataloguemanager.model.MDDataIdentification;
import org.deegree.portal.cataloguemanager.model.MDDistribution;
import org.deegree.portal.cataloguemanager.model.MDFormat;
import org.deegree.portal.cataloguemanager.model.MDMetadata;
import org.deegree.portal.cataloguemanager.model.MetadataBean;
import org.deegree.portal.cataloguemanager.model.TimePeriod;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ClearFormListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( ClearFormListener.class );

    @Override
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        CatalogueManagerConfiguration conf = getCatalogueManagerConfiguration( event );

        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.portal.cataloguemanager.model" );
            Unmarshaller u = jc.createUnmarshaller();

            String tmp = conf.getTemplateURL( "dataset" );
            URL url = conf.resolve( tmp );
            MDMetadata mdMetadata = (MDMetadata) u.unmarshal( url );

            MetadataBean metadata = new MetadataBean();

            metadata.setHlevel( "dataset" );
            CIResponsibleParty resParty = mdMetadata.getContact().getCIResponsibleParty();
            CIContact contact = resParty.getContactInfo().getCIContact();
            CIAddress address = contact.getAddress().getCIAddress();
            metadata.setContactCity( address.getCity().getCharacterString() );
            metadata.setContactCountry( address.getCountry().getCharacterString() );
            metadata.setContactDeliveryPoint( address.getDeliveryPoint().getCharacterString() );
            metadata.setContactOrganisationName( resParty.getOrganisationName().getCharacterString() );
            metadata.setContactPostalCode( address.getPostalCode().getCharacterString() );
            metadata.setContactRole( resParty.getRole().getCIRoleCode().getCodeListValue() );

            MDDataIdentification dataIdent = mdMetadata.getIdentificationInfo().getMDDataIdentification();
            EXExtent extent = dataIdent.getExtent().getEXExtent();
            TimePeriod timePeriod = extent.getTemporalElement().getEXTemporalExtent().getExtent().getTimePeriod();
            metadata.setBegin( timePeriod.getBeginPosition() );
            metadata.setEnd( timePeriod.getEndPosition() );
            if ( extent.getDescription() != null ) {
                metadata.setGeogrDescription( extent.getDescription().getCharacterString() );
            }

            List<Keyword> keywords = dataIdent.getDescriptiveKeywords().get( 0 ).getMDKeywords().getKeyword();
            List<String> kws = new ArrayList<String>( keywords.size() );
            for ( Keyword keyword : keywords ) {
                kws.add( keyword.getCharacterString() );
            }
            metadata.setKeywords( kws );

            resParty = dataIdent.getPointOfContact().getCIResponsibleParty();
            contact = resParty.getContactInfo().getCIContact();
            address = contact.getAddress().getCIAddress();
            metadata.setPocCity( address.getCity().getCharacterString() );
            metadata.setPocCountry( address.getCountry().getCharacterString() );
            metadata.setPocDeliveryPoint( address.getDeliveryPoint().getCharacterString() );
            metadata.setPocOrganisationName( resParty.getOrganisationName().getCharacterString() );
            metadata.setPocPostalCode( address.getPostalCode().getCharacterString() );
            metadata.setPocRole( resParty.getRole().getCIRoleCode().getCodeListValue() );
            metadata.setTopCat( dataIdent.getTopicCategory().getMDTopicCategoryCode() );

            if ( mdMetadata.getDistributionInfo() != null
                 && mdMetadata.getDistributionInfo().getMDDistribution() != null ) {
                MDDistribution mdDistribution = mdMetadata.getDistributionInfo().getMDDistribution();
                if ( mdDistribution.getTransferOptions() != null
                     && mdDistribution.getTransferOptions().getMDDigitalTransferOptions() != null
                     && mdDistribution.getTransferOptions().getMDDigitalTransferOptions().getOnLine() != null
                     && mdDistribution.getTransferOptions().getMDDigitalTransferOptions().getOnLine().getCIOnlineResource() != null
                     && mdDistribution.getTransferOptions().getMDDigitalTransferOptions().getOnLine().getCIOnlineResource().getLinkage() != null ) {
                    metadata.setTransferOnline( mdDistribution.getTransferOptions().getMDDigitalTransferOptions().getOnLine().getCIOnlineResource().getLinkage().getURL() );
                }
                if ( mdDistribution.getDistributionFormat() != null
                     && mdDistribution.getDistributionFormat().getMDFormat() != null ) {
                    MDFormat mdFormat = mdDistribution.getDistributionFormat().getMDFormat();
                    if ( mdFormat.getName() != null ) {
                        metadata.setTransferFormatName( mdFormat.getName().getCharacterString() );
                    }
                    if ( mdFormat.getVersion() != null ) {
                        metadata.setTransferFormatVersion( mdFormat.getVersion().getCharacterString() );
                    }
                }
            }

            responseHandler.setContentType( "text/plain; charset=" + Charset.defaultCharset().displayName() );
            responseHandler.writeAndClose( true, metadata );

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }
    }
}
