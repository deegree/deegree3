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
package org.deegree.metadata.persistence.iso.parsing;

import java.util.List;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.cs.CRSCodeType;
import org.deegree.metadata.persistence.types.BoundingBox;
import org.deegree.metadata.persistence.types.Format;
import org.deegree.metadata.persistence.types.Keyword;
import org.deegree.metadata.persistence.types.OperatesOnData;

/**
 * Properties that are queryable by applicationprofiles such as ISO application profile version 1.0 document 07-045.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class QueryableProperties {

    // ---<common queryable properties>
    // dc:subject
    private List<Keyword> keywords;

    private List<String> title;

    private List<String> _abstract;

    private String anyText;

    private List<Format> format;

    private String[] identifier;

    private Date[] modified;

    private String type;

    private List<BoundingBox> boundingBox;

    private List<CRSCodeType> crs;

    // ---</common queryable properties>

    // ---<additional common queryable properties>

    private Date revisionDate;

    private List<String> alternateTitle;

    private Date creationDate;

    private Date publicationDate;

    private String organisationName;

    private boolean hasSecurityConstraints;

    private String language;

    private List<String> resourceIdentifier;

    private String parentIdentifier;

    private List<String> resourceLanguage;

    private List<String> topicCategory;

    private List<OperatesOnData> operatesOnData;

    private int denominator;

    private float distanceValue;

    private String distanceUOM;

    private Date temporalExtentBegin;

    private Date temporalExtentEnd;

    // ---</additional common queryable properties>

    // ---<additional common queryable properties for SERVICE>

    private String serviceType;

    private List<String> serviceTypeVersion;

    private List<String> geographicDescriptionCode_service;

    private List<String> operation;

    private String couplingType;

    // ---</additional common queryable properties for SERVICE>

    // ---<additional common queryable properties for INSPIRE>

    private boolean degree;

    private List<String> specificationTitle;

    private String specificationDateType;

    private Date specificationDate;

    private List<String> limitation;

    private List<String> accessConstraints;

    private List<String> otherConstraints;

    private List<String> classification;

    /**
     * @return the degree
     */
    public boolean isDegree() {
        return degree;
    }

    /**
     * @param degree
     *            the degree to set
     */
    public void setDegree( boolean degree ) {
        this.degree = degree;
    }

    /**
     * @return the specificationTitle
     */
    public List<String> getSpecificationTitle() {
        return specificationTitle;
    }

    /**
     * @param specificationTitle
     *            the specificationTitle to set
     */
    public void setSpecificationTitle( List<String> specificationTitle ) {
        this.specificationTitle = specificationTitle;
    }

    /**
     * @return the specificationDateType
     */
    public String getSpecificationDateType() {
        return specificationDateType;
    }

    /**
     * @param specificationDateType
     *            the specificationDateType to set
     */
    public void setSpecificationDateType( String specificationDateType ) {
        this.specificationDateType = specificationDateType;
    }

    /**
     * @return the specificationDate
     */
    public Date getSpecificationDate() {
        return specificationDate;
    }

    /**
     * @param specificationDate
     *            the specificationDate to set
     */
    public void setSpecificationDate( Date specificationDate ) {
        this.specificationDate = specificationDate;
    }

    /**
     * @return the limitation
     */
    public List<String> getLimitation() {
        return limitation;
    }

    /**
     * @param limitation
     *            the limitation to set
     */
    public void setLimitation( List<String> limitation ) {
        this.limitation = limitation;
    }

    /**
     * @return the accessConstraints
     */
    public List<String> getAccessConstraints() {
        return accessConstraints;
    }

    /**
     * @param accessConstraints
     *            the accessConstraints to set
     */
    public void setAccessConstraints( List<String> accessConstraints ) {
        this.accessConstraints = accessConstraints;
    }

    /**
     * @return the otherConstraints
     */
    public List<String> getOtherConstraints() {
        return otherConstraints;
    }

    /**
     * @param otherConstraints
     *            the otherConstraints to set
     */
    public void setOtherConstraints( List<String> otherConstraints ) {
        this.otherConstraints = otherConstraints;
    }

    /**
     * @return the classification
     */
    public List<String> getClassification() {
        return classification;
    }

    /**
     * @param classification
     *            the classification to set
     */
    public void setClassification( List<String> classification ) {
        this.classification = classification;
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
    public void setLineage( String lineage ) {
        if ( lineage != null ) {
            lineage = lineage.replace( "'", "''" );
        }
        this.lineage = lineage;
    }

    private String lineage;

    // ---</additional common queryable properties for INSPIRE>

    /**
     * @return _abstract
     */
    public List<String> get_abstract() {
        return _abstract;
    }

    /**
     * 
     * @param _abstract
     */
    public void set_abstract( List<String> _abstract ) {
        this._abstract = _abstract;
    }

    /**
     * @return the anyText
     */
    public String getAnyText() {
        return anyText;
    }

    /**
     * @param anyText
     *            the anyText to set
     */
    public void setAnyText( String anyText ) {
        if ( anyText != null ) {
            anyText = anyText.replace( "'", "''" );

        }
        this.anyText = anyText;
    }

    /**
     * @return the format
     */
    public List<Format> getFormat() {
        return format;
    }

    /**
     * @param format
     *            the format to set
     */
    public void setFormat( List<Format> format ) {
        this.format = format;
    }

    /**
     * @return the identifier
     */
    public String[] getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier( String[] identifier ) {
        this.identifier = identifier;
    }

    /**
     * @return the modified
     */
    public Date[] getModified() {
        return modified;
    }

    /**
     * @param modified
     *            the modified to set
     */
    public void setModified( Date[] modified ) {
        this.modified = modified;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType( String type ) {
        this.type = type;
    }

    /**
     * @return the crs
     */
    public List<CRSCodeType> getCrs() {
        return crs;
    }

    /**
     * @param crs
     *            the crs to set
     */
    public void setCrs( List<CRSCodeType> crs ) {
        this.crs = crs;
    }

    /**
     * @return the title
     */
    public List<String> getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle( List<String> title ) {
        this.title = title;
    }

    /**
     * @return the boundingBox
     */
    public List<BoundingBox> getBoundingBox() {
        return boundingBox;
    }

    /**
     * @param boundingBox
     *            the boundingBox to set
     */
    public void setBoundingBox( List<BoundingBox> boundingBox ) {
        this.boundingBox = boundingBox;
    }

    /**
     * @return the keywords
     */
    public List<Keyword> getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            the keywords to set
     */
    public void setKeywords( List<Keyword> keywords ) {
        this.keywords = keywords;
    }

    /**
     * @return the alternateTitle
     */
    public List<String> getAlternateTitle() {
        return alternateTitle;
    }

    /**
     * @param alternateTitle
     *            the alternateTitle to set
     */
    public void setAlternateTitle( List<String> alternateTitle ) {
        this.alternateTitle = alternateTitle;
    }

    /**
     * @return the resourceLanguage
     */
    public List<String> getResourceLanguage() {
        return resourceLanguage;
    }

    /**
     * @param resourceLanguage
     *            the resourceLanguage to set
     */
    public void setResourceLanguage( List<String> resourceLanguage ) {
        this.resourceLanguage = resourceLanguage;
    }

    /**
     * @return the revisionDate
     */
    public Date getRevisionDate() {
        return revisionDate;
    }

    /**
     * @param revisionDate
     *            the revisionDate to set
     */
    public void setRevisionDate( Date revisionDate ) {
        this.revisionDate = revisionDate;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate( Date creationDate ) {
        this.creationDate = creationDate;
    }

    /**
     * @return the publicationDate
     */
    public Date getPublicationDate() {
        return publicationDate;
    }

    /**
     * @param publicationDate
     *            the publicationDate to set
     */
    public void setPublicationDate( Date publicationDate ) {
        this.publicationDate = publicationDate;
    }

    /**
     * @return the organisationName
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * @param organisationName
     *            the organisationName to set
     */
    public void setOrganisationName( String organisationName ) {
        if ( organisationName != null ) {
            organisationName = organisationName.replace( "'", "''" );
        }
        this.organisationName = organisationName;
    }

    /**
     * @return the hasSecurityConstraints
     */
    public boolean isHasSecurityConstraints() {
        return hasSecurityConstraints;
    }

    /**
     * @param hasSecurityConstraints
     *            the hasSecurityConstraints to set
     */
    public void setHasSecurityConstraints( boolean hasSecurityConstraints ) {
        this.hasSecurityConstraints = hasSecurityConstraints;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language
     *            the language to set
     */
    public void setLanguage( String language ) {
        this.language = language;
    }

    /**
     * @return the resourceIdentifier
     */
    public List<String> getResourceIdentifier() {
        return resourceIdentifier;
    }

    /**
     * @param resourceIdentifier
     *            the resourceIdentifier to set
     */
    public void setResourceIdentifier( List<String> resourceIdentifier ) {
        this.resourceIdentifier = resourceIdentifier;
    }

    /**
     * @return the parentIdentifier
     */
    public String getParentIdentifier() {
        return parentIdentifier;
    }

    /**
     * @param parentIdentifier
     *            the parentIdentifier to set
     */
    public void setParentIdentifier( String parentIdentifier ) {
        this.parentIdentifier = parentIdentifier;
    }

    /**
     * @return the topicCategory
     */
    public List<String> getTopicCategory() {
        return topicCategory;
    }

    /**
     * @param topicCategory
     *            the topicCategory to set
     */
    public void setTopicCategory( List<String> topicCategory ) {
        this.topicCategory = topicCategory;
    }

    /**
     * @return the serviceType
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * @param serviceType
     *            the serviceType to set
     */
    public void setServiceType( String serviceType ) {
        this.serviceType = serviceType;
    }

    /**
     * @return the serviceTypeVersion
     */
    public List<String> getServiceTypeVersion() {
        return serviceTypeVersion;
    }

    /**
     * @param serviceTypeVersion
     *            the serviceTypeVersion to set
     */
    public void setServiceTypeVersion( List<String> serviceTypeVersion ) {
        this.serviceTypeVersion = serviceTypeVersion;
    }

    /**
     * @return the geographicDescriptionCode_service
     */
    public List<String> getGeographicDescriptionCode_service() {
        return geographicDescriptionCode_service;
    }

    /**
     * @param geographicDescriptionCodeService
     *            the geographicDescriptionCode_service to set
     */
    public void setGeographicDescriptionCode_service( List<String> geographicDescriptionCodeService ) {
        geographicDescriptionCode_service = geographicDescriptionCodeService;
    }

    /**
     * @return the operation
     */
    public List<String> getOperation() {
        return operation;
    }

    /**
     * @param operation
     *            the operation to set
     */
    public void setOperation( List<String> operation ) {
        this.operation = operation;
    }

    /**
     * @return the couplingType
     */
    public String getCouplingType() {
        return couplingType;
    }

    /**
     * @param couplingType
     *            the couplingType to set
     */
    public void setCouplingType( String couplingType ) {

        this.couplingType = couplingType;
    }

    /**
     * @return the operatesOnData
     */
    public List<OperatesOnData> getOperatesOnData() {
        return operatesOnData;
    }

    /**
     * @param operatesOnData
     *            the operatesOnData to set
     */
    public void setOperatesOnData( List<OperatesOnData> operatesOnData ) {
        this.operatesOnData = operatesOnData;
    }

    /**
     * @return the denominator
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * @param denominator
     *            the denominator to set
     */
    public void setDenominator( int denominator ) {
        this.denominator = denominator;
    }

    /**
     * @return the distanceValue
     */
    public float getDistanceValue() {
        return distanceValue;
    }

    /**
     * @param distanceValue
     *            the distanceValue to set
     */
    public void setDistanceValue( float distanceValue ) {
        this.distanceValue = distanceValue;
    }

    /**
     * @return the distanceUOM
     */
    public String getDistanceUOM() {
        return distanceUOM;
    }

    /**
     * @param distanceUOM
     *            the distanceUOM to set
     */
    public void setDistanceUOM( String distanceUOM ) {
        if ( distanceUOM != null ) {
            distanceUOM = distanceUOM.replace( "'", "''" );
        }
        this.distanceUOM = distanceUOM;
    }

    /**
     * @return the temporalExtentBegin
     */
    public Date getTemporalExtentBegin() {
        return temporalExtentBegin;
    }

    /**
     * @param temporalExtentBegin
     *            the temporalExtentBegin to set
     */
    public void setTemporalExtentBegin( Date temporalExtentBegin ) {
        this.temporalExtentBegin = temporalExtentBegin;
    }

    /**
     * @return the temporalExtentEnd
     */
    public Date getTemporalExtentEnd() {
        return temporalExtentEnd;
    }

    /**
     * @param temporalExtentEnd
     *            the temporalExtentEnd to set
     */
    public void setTemporalExtentEnd( Date temporalExtentEnd ) {
        this.temporalExtentEnd = temporalExtentEnd;
    }

}
