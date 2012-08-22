/*----------------    FILE HEADER  ------------------------------------------
 Copyright (C) 2001-2008 by:
 lat/lon GmbH
 http://www.lat-lon.de

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 ---------------------------------------------------------------------------*/
package org.deegree.portal.cataloguemanager.model;

import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version. $Revision$, $Date$
 */
public class MetadataBean {

    private String identifier;

    private String hlevel;

    private String parentId;

    private String contactIndividualName;

    private String contactOrganisationName;

    private String contactRole;

    private String contactDeliveryPoint;

    private String contactCity;

    private String contactPostalCode;

    private String contactCountry;

    private String contactVoice;

    private String contactFacsimile;

    private String contactEmailAddress;

    private String datasetTitle;

    private String abstract_;

    private String topCat;

    private List keywords;

    private String geogrDescription;

    private String crs;

    private String scale;

    private String creation;

    private String publication;

    private String revision;

    private String begin;

    private String end;

    private String pocIndividualName;

    private String pocOrganisationName;

    private String pocRole;

    private String pocDeliveryPoint;

    private String pocCity;

    private String pocPostalCode;

    private String pocCountry;

    private String pocVoice;

    private String pocFacsimile;

    private String pocEmailAddress;

    private String lineage;

    private String inspireDataTheme;

    private String transferOnline;
    
    private String transferFormatName;
    
    private String transferFormatVersion;
    
    private String accessConstraints;
    
    private Map serviceMetadataBean;

    /**
     * @return the abstract_
     */
    public String getAbstract_() {
        return abstract_;
    }

    /**
     * @param abstract_
     *            the abstract_ to set
     */
    public void setAbstract_( String abstract_ ) {
        this.abstract_ = abstract_;
    }

    /**
     * @return the begin
     */
    public String getBegin() {
        return begin;
    }

    /**
     * @param begin
     *            the begin to set
     */
    public void setBegin( String begin ) {
        this.begin = begin;
    }

    /**
     * @return the contactCity
     */
    public String getContactCity() {
        return contactCity;
    }

    /**
     * @param contactCity
     *            the contactCity to set
     */
    public void setContactCity( String contactCity ) {
        this.contactCity = contactCity;
    }

    /**
     * @return the contactCountry
     */
    public String getContactCountry() {
        return contactCountry;
    }

    /**
     * @param contactCountry
     *            the contactCountry to set
     */
    public void setContactCountry( String contactCountry ) {
        this.contactCountry = contactCountry;
    }

    /**
     * @return the contactDeliveryPoint
     */
    public String getContactDeliveryPoint() {
        return contactDeliveryPoint;
    }

    /**
     * @param contactDeliveryPoint
     *            the contactDeliveryPoint to set
     */
    public void setContactDeliveryPoint( String contactDeliveryPoint ) {
        this.contactDeliveryPoint = contactDeliveryPoint;
    }

    /**
     * @return the contactEmailAddress
     */
    public String getContactEmailAddress() {
        return contactEmailAddress;
    }

    /**
     * @param contactEmailAddress
     *            the contactEmailAddress to set
     */
    public void setContactEmailAddress( String contactEmailAddress ) {
        this.contactEmailAddress = contactEmailAddress;
    }

    /**
     * @return the contactFacsimile
     */
    public String getContactFacsimile() {
        return contactFacsimile;
    }

    /**
     * @param contactFacsimile
     *            the contactFacsimile to set
     */
    public void setContactFacsimile( String contactFacsimile ) {
        this.contactFacsimile = contactFacsimile;
    }

    /**
     * @return the contactIndividualName
     */
    public String getContactIndividualName() {
        return contactIndividualName;
    }

    /**
     * @param contactIndividualName
     *            the contactIndividualName to set
     */
    public void setContactIndividualName( String contactIndividualName ) {
        this.contactIndividualName = contactIndividualName;
    }

    /**
     * @return the contactOrganisationName
     */
    public String getContactOrganisationName() {
        return contactOrganisationName;
    }

    /**
     * @param contactOrganisationName
     *            the contactOrganisationName to set
     */
    public void setContactOrganisationName( String contactOrganisationName ) {
        this.contactOrganisationName = contactOrganisationName;
    }

    /**
     * @return the contactPostalCode
     */
    public String getContactPostalCode() {
        return contactPostalCode;
    }

    /**
     * @param contactPostalCode
     *            the contactPostalCode to set
     */
    public void setContactPostalCode( String contactPostalCode ) {
        this.contactPostalCode = contactPostalCode;
    }

    /**
     * @return the contactRole
     */
    public String getContactRole() {
        return contactRole;
    }

    /**
     * @param contactRole
     *            the contactRole to set
     */
    public void setContactRole( String contactRole ) {
        this.contactRole = contactRole;
    }

    /**
     * @return the contactVoice
     */
    public String getContactVoice() {
        return contactVoice;
    }

    /**
     * @param contactVoice
     *            the contactVoice to set
     */
    public void setContactVoice( String contactVoice ) {
        this.contactVoice = contactVoice;
    }

    /**
     * @return the creation
     */
    public String getCreation() {
        return creation;
    }

    /**
     * @param creation
     *            the creation to set
     */
    public void setCreation( String creation ) {
        this.creation = creation;
    }

    /**
     * @return the crs
     */
    public String getCrs() {
        return crs;
    }

    /**
     * @param crs
     *            the crs to set
     */
    public void setCrs( String crs ) {
        this.crs = crs;
    }

    /**
     * @return the datasetTitle
     */
    public String getDatasetTitle() {
        return datasetTitle;
    }

    /**
     * @param datasetTitle
     *            the datasetTitle to set
     */
    public void setDatasetTitle( String datasetTitle ) {
        this.datasetTitle = datasetTitle;
    }

    /**
     * @return the end
     */
    public String getEnd() {
        return end;
    }

    /**
     * @param end
     *            the end to set
     */
    public void setEnd( String end ) {
        this.end = end;
    }

    /**
     * @return the geogrDescription
     */
    public String getGeogrDescription() {
        return geogrDescription;
    }

    /**
     * @param geogrDescription
     *            the geogrDescription to set
     */
    public void setGeogrDescription( String geogrDescription ) {
        this.geogrDescription = geogrDescription;
    }

    /**
     * @return the hlevel
     */
    public String getHlevel() {
        return hlevel;
    }

    /**
     * @param hlevel
     *            the hlevel to set
     */
    public void setHlevel( String hlevel ) {
        this.hlevel = hlevel;
    }

    /**
     * @return the idendifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the idendifier to set
     */
    public void setIdentifier( String identifier ) {
        this.identifier = identifier;
    }

    /**
     * @return the keywords
     */
    @SuppressWarnings("unchecked")
    public List getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            the keywords to set
     */
    @SuppressWarnings("unchecked")
    public void setKeywords( List keywords ) {
        this.keywords = keywords;
    }

    /**
     * @return the parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @param parentId
     *            the parentId to set
     */
    public void setParentId( String parentId ) {
        this.parentId = parentId;
    }

    /**
     * @return the pocCity
     */
    public String getPocCity() {
        return pocCity;
    }

    /**
     * @param pocCity
     *            the pocCity to set
     */
    public void setPocCity( String pocCity ) {
        this.pocCity = pocCity;
    }

    /**
     * @return the pocCountry
     */
    public String getPocCountry() {
        return pocCountry;
    }

    /**
     * @param pocCountry
     *            the pocCountry to set
     */
    public void setPocCountry( String pocCountry ) {
        this.pocCountry = pocCountry;
    }

    /**
     * @return the pocDeliveryPoint
     */
    public String getPocDeliveryPoint() {
        return pocDeliveryPoint;
    }

    /**
     * @param pocDeliveryPoint
     *            the pocDeliveryPoint to set
     */
    public void setPocDeliveryPoint( String pocDeliveryPoint ) {
        this.pocDeliveryPoint = pocDeliveryPoint;
    }

    /**
     * @return the pocEmailAddress
     */
    public String getPocEmailAddress() {
        return pocEmailAddress;
    }

    /**
     * @param pocEmailAddress
     *            the pocEmailAddress to set
     */
    public void setPocEmailAddress( String pocEmailAddress ) {
        this.pocEmailAddress = pocEmailAddress;
    }

    /**
     * @return the pocFacsimile
     */
    public String getPocFacsimile() {
        return pocFacsimile;
    }

    /**
     * @param pocFacsimile
     *            the pocFacsimile to set
     */
    public void setPocFacsimile( String pocFacsimile ) {
        this.pocFacsimile = pocFacsimile;
    }

    /**
     * @return the pocIndividualName
     */
    public String getPocIndividualName() {
        return pocIndividualName;
    }

    /**
     * @param pocIndividualName
     *            the pocIndividualName to set
     */
    public void setPocIndividualName( String pocIndividualName ) {
        this.pocIndividualName = pocIndividualName;
    }

    /**
     * @return the pocOrganisationName
     */
    public String getPocOrganisationName() {
        return pocOrganisationName;
    }

    /**
     * @param pocOrganisationName
     *            the pocOrganisationName to set
     */
    public void setPocOrganisationName( String pocOrganisationName ) {
        this.pocOrganisationName = pocOrganisationName;
    }

    /**
     * @return the pocPostalCode
     */
    public String getPocPostalCode() {
        return pocPostalCode;
    }

    /**
     * @param pocPostalCode
     *            the pocPostalCode to set
     */
    public void setPocPostalCode( String pocPostalCode ) {
        this.pocPostalCode = pocPostalCode;
    }

    /**
     * @return the pocRole
     */
    public String getPocRole() {
        return pocRole;
    }

    /**
     * @param pocRole
     *            the pocRole to set
     */
    public void setPocRole( String pocRole ) {
        this.pocRole = pocRole;
    }

    /**
     * @return the pocVoice
     */
    public String getPocVoice() {
        return pocVoice;
    }

    /**
     * @param pocVoice
     *            the pocVoice to set
     */
    public void setPocVoice( String pocVoice ) {
        this.pocVoice = pocVoice;
    }

    /**
     * @return the publication
     */
    public String getPublication() {
        return publication;
    }

    /**
     * @param publication
     *            the publication to set
     */
    public void setPublication( String publication ) {
        this.publication = publication;
    }

    /**
     * @return the revision
     */
    public String getRevision() {
        return revision;
    }

    /**
     * @param revision
     *            the revision to set
     */
    public void setRevision( String revision ) {
        this.revision = revision;
    }

    /**
     * @return the scale
     */
    public String getScale() {
        return scale;
    }

    /**
     * @param scale
     *            the scale to set
     */
    public void setScale( String scale ) {
        this.scale = scale;
    }

    /**
     * @return the topCat
     */
    public String getTopCat() {
        return topCat;
    }

    /**
     * @param topCat
     *            the topCat to set
     */
    public void setTopCat( String topCat ) {
        this.topCat = topCat;
    }

    /**
     * @return the lineage
     */
    public String getLineage() {
        return lineage;
    }

    /**
     * @param lineage
     *            the lineage to set
     */
    public void setLineage( String linkage ) {
        this.lineage = linkage;
    }

    /**
     * @return the inspireDataTheme
     */
    public String getInspireDataTheme() {
        return inspireDataTheme;
    }

    /**
     * @param inspireDataTheme
     *            the inspireDataTheme to set
     */
    public void setInspireDataTheme( String inspireDataTheme ) {
        this.inspireDataTheme = inspireDataTheme;
    }

    /**
     * @return the transferOnline
     */
    public String getTransferOnline() {
        return transferOnline;
    }

    /**
     * @param transferOnline
     *            the transferOnline to set
     */
    public void setTransferOnline( String transferOnline ) {
        this.transferOnline = transferOnline;
    }
    
    

    /**
     * @return the transferFormatName
     */
    public String getTransferFormatName() {
        return transferFormatName;
    }

    /**
     * @param transferFormatName the transferFormatName to set
     */
    public void setTransferFormatName( String transferFormatName ) {
        this.transferFormatName = transferFormatName;
    }

    /**
     * @return the transferFormatVersion
     */
    public String getTransferFormatVersion() {
        return transferFormatVersion;
    }

    /**
     * @param transferFormatVersion the transferFormatVersion to set
     */
    public void setTransferFormatVersion( String transferFormatVersion ) {
        this.transferFormatVersion = transferFormatVersion;
    }
    
    /**
     * @return the accessConstraints
     */
    public String getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * @param accessConstraints the accessConstraints to set
     */
    public void setAccessConstraints( String accessConstraints ) {
        this.accessConstraints = accessConstraints;
    }
    
    


    /**
     * @return the serviceMetadataBean
     */
    public Map getServiceMetadataBean() {
        return serviceMetadataBean;
    }

    /**
     * @param serviceMetadataBean the serviceMetadataBean to set
     */
    public void setServiceMetadataBean( Map serviceMetadataBean ) {
        this.serviceMetadataBean = serviceMetadataBean;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( identifier + "\n\r" );
        sb.append( hlevel + "\n\r" );
        sb.append( parentId + "\n\r" );
        sb.append( contactIndividualName + "\n\r" );
        sb.append( contactOrganisationName + "\n\r" );
        sb.append( contactRole + "\n\r" );
        sb.append( contactDeliveryPoint + "\n\r" );
        sb.append( contactCity + "\n\r" );
        sb.append( contactPostalCode + "\n\r" );
        sb.append( contactCountry + "\n\r" );
        sb.append( contactVoice + "\n\r" );
        sb.append( contactFacsimile + "\n\r" );
        sb.append( contactEmailAddress + "\n\r" );
        sb.append( datasetTitle + "\n\r" );
        sb.append( abstract_ + "\n\r" );
        sb.append( topCat + "\n\r" );
        sb.append( inspireDataTheme + "\n\r" );
        sb.append( "keywords: " + "\n\r" );
        if ( keywords != null ) {
            for ( Object keyword : keywords ) {
                sb.append( keyword + "\n\r" );
            }
        }
        sb.append( "\n\r" );
        sb.append( lineage + "\n\r" );
        sb.append( geogrDescription + "\n\r" );
        sb.append( crs + "\n\r" );
        sb.append( scale + "\n\r" );
        sb.append( creation + "\n\r" );
        sb.append( publication + "\n\r" );
        sb.append( revision + "\n\r" );
        sb.append( begin + "\n\r" );
        sb.append( end + "\n\r" );
        sb.append( pocIndividualName + "\n\r" );
        sb.append( pocOrganisationName + "\n\r" );
        sb.append( pocRole + "\n\r" );
        sb.append( pocDeliveryPoint + "\n\r" );
        sb.append( pocCity + "\n\r" );
        sb.append( pocPostalCode + "\n\r" );
        sb.append( pocCountry + "\n\r" );
        sb.append( pocVoice + "\n\r" );
        sb.append( pocFacsimile + "\n\r" );
        sb.append( pocEmailAddress + "\n\r" );
        sb.append( transferOnline + "\n\r" );
        sb.append( transferFormatName + "\n\r" );
        sb.append( transferFormatVersion + "\n\r" );
        sb.append( accessConstraints + "\n\r" );
        return sb.toString();
    }

}
