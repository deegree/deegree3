<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:app="http://www.deegree.org/app"
	xmlns:csw="http://www.opengis.net/cat/csw"
	xmlns:gml="http://www.opengis.net/gml"
	xmlns:ogc="http://www.opengis.net/ogc"
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:gts="http://www.isotc211.org/2005/gts"  	
	xmlns:wfs="http://www.opengis.net/wfs"  
    xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:deegreewfs="http://www.deegree.org/wfs"
	xmlns:java="java"
	xmlns:mapping="org.deegree.ogcwebservices.csw.iso_profile.Mapping2_0_2" version="1.0">

	<xsl:variable name="map" select="mapping:new( )"/>	
	
	<xsl:param name="NSP">a:a</xsl:param>
	
	<xsl:output encoding="UTF-8" indent="yes" method="xml" version="1.0" />
	<!-- ======================================================== -->
	<xsl:include href="iso19115_transaction.xsl" />
	<xsl:include href="iso19119_transaction.xsl" />
	<!-- ======================================================== -->
	<xsl:template match="csw:Transaction">
		<wfs:Transaction xmlns:xlink="http://www.w3.org/1999/xlink"
			service="WFS" version="1.1.0">
			<xsl:apply-templates select="csw:Insert" />
			<xsl:apply-templates select="csw:Update" />
			<xsl:apply-templates select="csw:Delete" />
		</wfs:Transaction>
	</xsl:template>
	<xsl:template match="csw:Insert">
		<wfs:Insert idgen="GenerateNew">
			<xsl:variable name="hierarchyLevel" select="gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
			<xsl:choose>
				<xsl:when test="boolean( $hierarchyLevel = 'service' )">
					<xsl:call-template name="SERVICEMETADATA"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="METADATA"/>			
				</xsl:otherwise>
			</xsl:choose>
		</wfs:Insert>
	</xsl:template>
	<xsl:template match="csw:Update">
		<wfs:Update typeName="app:MD_Metadata">
		<xsl:variable name="hierarchyLevel" select="gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
			<xsl:choose>
				<xsl:when test="boolean( $hierarchyLevel = 'service' )">
					<xsl:call-template name="SERVICEMETADATA"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="METADATA"/>			
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="csw:Constraint/ogc:Filter" />
		</wfs:Update>
	</xsl:template>
	<xsl:template match="csw:Delete">
		<wfs:Delete typeName="app:MD_Metadata">
			<xsl:apply-templates select="csw:Constraint/ogc:Filter" />
		</wfs:Delete>
	</xsl:template>

	<xsl:template match="gmd:CI_ResponsibleParty">
		<app:CI_RespParty>
			<xsl:variable name="individualName" select="gmd:individualName/gco:CharacterString" />
			<xsl:if test="boolean( $individualName != '' )">
				<app:individualname>
					<xsl:value-of select="$individualName" />
				</app:individualname>
			</xsl:if>
			<xsl:variable name="organisationName" select="gmd:organisationName/gco:CharacterString" />
			<xsl:if test="boolean( $organisationName != '' )">
				<app:organisationname>
					<xsl:value-of select="$organisationName" />
				</app:organisationname>
			</xsl:if>
			<xsl:variable name="positionName" select="gmd:positionName/gco:CharacterString" />
			<xsl:if test="boolean( $positionName != '' )">
				<app:positionname>
					<xsl:value-of select="$positionName" />
				</app:positionname>
			</xsl:if>
			<xsl:if test="boolean(gmd:role/gmd:CI_RoleCode/@codeListValue)">
				<app:role>
					<app:CI_RoleCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:role/gmd:CI_RoleCode/@codeListValue" />
						</app:codelistvalue>
					</app:CI_RoleCode>
				</app:role>
			</xsl:if>
			<xsl:if test="gmd:contactInfo">
				<app:contactInfo>
					<xsl:apply-templates select="gmd:contactInfo/gmd:CI_Contact" />
				</app:contactInfo>
			</xsl:if>
		</app:CI_RespParty>
	</xsl:template>

	<xsl:template match="gmd:CI_Contact">
		<app:CI_Contact>
            <xsl:for-each select="gmd:phone/gmd:CI_Telephone/gmd:voice/gco:CharacterString" >
    			<xsl:if test="boolean ( . != '' )">
    				<app:voice>
    					<xsl:value-of select="." />
    				</app:voice>
                </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="gmd:phone/gmd:CI_Telephone/gmd:facsimile/gco:CharacterString" >
    			<xsl:if test="boolean( . != '' )">
    				<app:facsimile>
    					<xsl:value-of select="." />
    				</app:facsimile>
    			</xsl:if>
            </xsl:for-each>
			<xsl:apply-templates select="gmd:address/gmd:CI_Address" />
			<xsl:for-each select="gmd:onlineResource">
				<app:onlineResource>
					<xsl:apply-templates select="gmd:CI_OnlineResource" />
				</app:onlineResource>
			</xsl:for-each>
			<xsl:variable name="hrs" select="gmd:hoursOfService/gco:CharacterString" />
			<xsl:if test="$hrs != ''">
				<app:hoursofservice>
					<xsl:apply-templates select="$hrs" />
				</app:hoursofservice>
			</xsl:if>
			<xsl:variable name="contact" select="gmd:contactInstructions/gco:CharacterString" />
			<xsl:if test="$contact != ''">
				<app:contactinstructions>
					<xsl:value-of select="$contact" />
				</app:contactinstructions>
			</xsl:if>
		</app:CI_Contact>
	</xsl:template>

	<xsl:template match="gmd:CI_Address">
		<app:address>
			<app:CI_Address>
				<xsl:for-each select="gmd:deliveryPoint">
					<xsl:if test="boolean( gco:CharacterString != '' )">
						<app:deliveryPoint>
							<app:DeliveryPoint>
								<app:deliverypoint>
									<xsl:value-of select="gco:CharacterString" />
								</app:deliverypoint>
							</app:DeliveryPoint>
						</app:deliveryPoint>
					</xsl:if>
				</xsl:for-each>
				<xsl:variable name="city" select="gmd:city/gco:CharacterString" />
				<xsl:if test="$city != ''">
					<app:city>
						<xsl:value-of select="$city" />
					</app:city>
				</xsl:if>
				<xsl:variable name="admin" select="gmd:administrativeArea/gco:CharacterString" />
				<xsl:if test="$admin != ''">
					<app:administrativeArea>
						<xsl:value-of select="$admin" />
					</app:administrativeArea>
				</xsl:if>
				<xsl:variable name="postal" select="gmd:postalCode/gco:CharacterString" />
				<xsl:if test="$postal != ''">
					<app:postalCode>
						<xsl:value-of select="$postal" />
					</app:postalCode>
				</xsl:if>
				<xsl:variable name="country" select="gmd:country/gco:CharacterString" />
				<xsl:if test="$country != ''">
					<app:country>
						<xsl:value-of select="$country" />
					</app:country>
				</xsl:if>
				<xsl:for-each select="gmd:electronicMailAddress">
					<xsl:if test="boolean( gco:CharacterString != '' )">
						<app:electronicMailAddress>
							<app:ElectronicMailAddress>
								<app:email>
									<xsl:value-of select="gco:CharacterString" />
								</app:email>
							</app:ElectronicMailAddress>
						</app:electronicMailAddress>
					</xsl:if>
				</xsl:for-each>
			</app:CI_Address>
		</app:address>
	</xsl:template>

	<xsl:template match="gmd:CI_Citation">
		<xsl:param name="context" />
		<app:CI_Citation>
			<xsl:for-each select="gmd:alternateTitle">
				<xsl:if test="boolean( gco:CharacterString != '' )">
					<app:alternateTitle>
						<xsl:value-of select="gco:CharacterString" />
					</app:alternateTitle>
				</xsl:if>
			</xsl:for-each>
			<xsl:apply-templates select="gmd:series/gmd:CI_Series" />
			<xsl:for-each select="gmd:presentationForm">
				<app:presentationForm>
					<app:CI_PresentationFormCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:CI_PresentationFormCode/@codeListValue" />
						</app:codelistvalue>
					</app:CI_PresentationFormCode>
				</app:presentationForm>
			</xsl:for-each>
			<xsl:for-each select="gmd:citedResponsibleParty">
				<app:citedResponsibleParty>
					<xsl:apply-templates select="gmd:CI_ResponsibleParty" />
				</app:citedResponsibleParty>
			</xsl:for-each>
			<xsl:variable name="title" select="gmd:title/gco:CharacterString" />
			<xsl:if test="boolean( $title != '' )">
				<app:title>
					<xsl:value-of select="$title" />
				</app:title>
			</xsl:if>
			<xsl:variable name="edition" select="gmd:edition/gco:CharacterString" />
			<xsl:if test="$edition != ''">
				<app:edition>
					<xsl:value-of select="$edition" />
				</app:edition>
			</xsl:if>
			<xsl:if test="boolean( gmd:editionDate )">
				<app:editiondate>
					<xsl:value-of select="gmd:editionDate/gco:DateTime" />
					<xsl:value-of select="gmd:editionDate/gco:Date" />
				</app:editiondate>
			</xsl:if>
			<xsl:variable name="fileIdent" select="../../../../gmd:fileIdentifier/gco:CharacterString" />
			
			<xsl:for-each select="gmd:identifier">
        <xsl:variable name="identifier" select="gmd:MD_Identifier/gmd:code/gco:CharacterString" />
        <!-- one of the identifier of identificaion.ci_citation must be the same like the fileIdentifier, this one should not be inserted! --> 
    		<xsl:if test="boolean( $identifier != '' ) and boolean( $context != 'Identification' ) and boolean( $identifier != $fileIdent)">
    				<app:identifier>
    					<xsl:value-of select="$identifier" />
    				</app:identifier>
    		</xsl:if>
    	</xsl:for-each>	
    	<xsl:for-each select="gmd:identifier">	
        <xsl:variable name="rsidentifier" select="gmd:RS_Identifier" />
        <xsl:if test="boolean( $rsidentifier != '' ) and boolean( $context != 'Identification' ) and boolean( $rsidentifier != $fileIdent)">
          <app:rsidentifier>
            <app:RS_Identifier>
              <xsl:apply-templates select="$rsidentifier" />
            </app:RS_Identifier>
          </app:rsidentifier>
        </xsl:if>
      </xsl:for-each>
			
			<xsl:variable name="isbn" select="gmd:ISBN/gco:CharacterString" />
			<xsl:if test="$isbn != ''">
				<app:isbn>
					<xsl:value-of select="$isbn" />
				</app:isbn>
			</xsl:if>
			<xsl:variable name="issn" select="gmd:ISSN/gco:CharacterString" />
			<xsl:if test="$issn != ''">
				<app:issn>
					<xsl:value-of select="$issn" />
				</app:issn>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="boolean( gmd:date != '' )">
					<xsl:for-each select="gmd:date">
						<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision'">
							<app:revisiondate>
								<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date" />
								<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime" />
							</app:revisiondate>
						</xsl:if>
					</xsl:for-each>
					<xsl:for-each select="gmd:date">
						<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'creation'">
							<app:creationdate>
								<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date" />
								<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime" />
							</app:creationdate>
						</xsl:if>
					</xsl:for-each>
					<xsl:for-each select="gmd:date">
						<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication'">
							<app:publicationdate>
								<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date" />
								<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime" />
							</app:publicationdate>
						</xsl:if>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message>
						------------- Mandatory Element 'gmd:date' missing! ----------------
					</xsl:message>
					<app:exception>
						Mandatory Element 'gmd:date' missing!
					</app:exception>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="boolean( gmd:otherCitationDetails/gco:CharacterString != '' )">
				<app:otherCitationDetails>
					<xsl:value-of select="gmd:otherCitationDetails/gco:CharacterString" />
				</app:otherCitationDetails>
			</xsl:if>
			<app:context>
				<xsl:value-of select="$context" />
			</app:context>
		</app:CI_Citation>
	</xsl:template>

	<xsl:template match="gmd:series/gmd:CI_Series">
		<app:series>
			<app:CI_Series>
				<xsl:variable name="name" select="gmd:name/gco:CharacterString" />
				<xsl:if test="$name != ''">
					<app:name>
						<xsl:value-of select="$name" />
					</app:name>
				</xsl:if>
				<xsl:variable name="issueId" select="gmd:issueIdentification/gco:CharacterString" />
				<xsl:if test="$issueId != ''">
					<app:issueidentification>
						<xsl:value-of select="$issueId" />
					</app:issueidentification>
				</xsl:if>
				<xsl:if test="gmd:page">
					<app:page>
						<xsl:value-of select="gmd:page/gco:CharacterString" />
					</app:page>
				</xsl:if>
			</app:CI_Series>
		</app:series>
	</xsl:template>

	<xsl:template match="gmd:MD_Usage">
		<app:MD_Usage>
			<xsl:variable name="spec" select="gmd:specificUsage/gco:CharacterString" />
			<xsl:if test="$spec != ''">
				<app:specificusage>
					<xsl:value-of select="$spec" />
				</app:specificusage>
			</xsl:if>
			<xsl:for-each select="gmd:userContactInfo">
                <xsl:if test="gmd:CI_ResponsibleParty">
			     	<app:RespParty>
				    	<xsl:apply-templates select="gmd:CI_ResponsibleParty" />
    		  		</app:RespParty>
                </xsl:if>
			</xsl:for-each>
		</app:MD_Usage>
	</xsl:template>

	<xsl:template match="gmd:MD_Keywords">
		<app:MD_Keywords>
			<xsl:for-each select="gmd:keyword">
				<xsl:if test="boolean( gco:CharacterString != '' )">
					<app:keyword>
						<app:Keyword>
							<app:keyword>
								<xsl:value-of select="gco:CharacterString" />
							</app:keyword>
						</app:Keyword>
					</app:keyword>
				</xsl:if>
			</xsl:for-each>
			<xsl:if test="boolean( gmd:type ) ">
				<app:type>
					<app:MD_KeywordTypeCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:type/gmd:MD_KeywordTypeCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_KeywordTypeCode>
				</app:type>
			</xsl:if>
			<xsl:if test="gmd:thesaurusName">
				<app:thesaurusName>
					<xsl:apply-templates select="gmd:thesaurusName/gmd:CI_Citation">
						<xsl:with-param name="context">
							Keywords
						</xsl:with-param>
					</xsl:apply-templates>
				</app:thesaurusName>
			</xsl:if>
		</app:MD_Keywords>
	</xsl:template>

	<xsl:template match="gmd:MD_LegalConstraints">
		<app:MD_LegalConstraints>
			<xsl:if test="boolean( gmd:useLimitation )">
				<app:useLimitations>
					<xsl:value-of select="gmd:useLimitation" />
				</app:useLimitations>
			</xsl:if>
			<xsl:for-each select="gmd:otherConstraints/gco:CharacterString">
				<xsl:if test=". != ''">
					<app:otherConstraints>
						<xsl:value-of select="." />
					</app:otherConstraints>
				</xsl:if>
			</xsl:for-each>
			<xsl:for-each select="gmd:useConstraints/gmd:MD_RestrictionCode">
				<app:useConstraints>
					<app:MD_RestrictionCode>
						<app:codelistvalue>
							<xsl:value-of select="./@codeListValue" />
						</app:codelistvalue>
					</app:MD_RestrictionCode>
				</app:useConstraints>
			</xsl:for-each>
			<xsl:for-each select="gmd:accessConstraints/gmd:MD_RestrictionCode">
				<app:accessConstraints>
					<app:MD_RestrictionCode>
						<app:codelistvalue>
							<xsl:value-of select="./@codeListValue" />
						</app:codelistvalue>
					</app:MD_RestrictionCode>
				</app:accessConstraints>
			</xsl:for-each>
			<app:defined>true</app:defined>
		</app:MD_LegalConstraints>
	</xsl:template>

	<xsl:template match="gmd:MD_SecurityConstraints">
		<app:MD_SecurityConstraints>
			<xsl:if test="boolean( gmd:classification/gmd:MD_ClassificationCode )">
				<app:classification>
					<app:MD_ClassificationCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:classification/gmd:MD_ClassificationCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_ClassificationCode>
				</app:classification>
			</xsl:if>
			<xsl:variable name="userNote" select="gmd:userNote/gco:CharacterString" />
			<xsl:if test="boolean( $userNote != '')">
				<app:userNote>
					<xsl:value-of select="$userNote" />
				</app:userNote>
			</xsl:if>
			<xsl:variable name="classificationSystem" select="gmd:classificationSystem/gco:CharacterString" />
			<xsl:if test="boolean( $classificationSystem != '' )">
				<app:classificationSystem>
					<xsl:value-of select="$classificationSystem" />
				</app:classificationSystem>
			</xsl:if>
			<xsl:variable name="handlingDescription" select="gmd:handlingDescription/gco:CharacterString" />
			<xsl:if test="boolean( $handlingDescription != '' )">
				<app:handlingDescription>
					<xsl:value-of select="$handlingDescription" />
				</app:handlingDescription>
			</xsl:if>
			<xsl:for-each select="gmd:useLimitation">
				<xsl:if test="boolean( gco:CharacterString != '' )">
					<app:useLimitations>
						<xsl:value-of select="gco:CharacterString" />
					</app:useLimitations>
				</xsl:if>
			</xsl:for-each>
		</app:MD_SecurityConstraints>
	</xsl:template>

    <xsl:template match="gmd:MD_Constraints">
        <app:MD_Constraints>
            <xsl:for-each select="gmd:useLimitation">
                <xsl:if test="boolean( gco:CharacterString != '' )">
                    <app:useLimitations>
                        <xsl:value-of select="gco:CharacterString" />
                    </app:useLimitations>
                </xsl:if>
            </xsl:for-each>
        </app:MD_Constraints>
    </xsl:template>
    
	<xsl:template match="gmd:MD_MaintenanceInformation">
		<app:MD_MaintenanceInformation>
			<xsl:if test="boolean( gmd:updateScope )">
				<app:updateScope>
					<app:MD_ScopeCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:updateScope/gmd:MD_ScopeCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_ScopeCode>
				</app:updateScope>
			</xsl:if>
			<xsl:if test="boolean( gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode )">
				<app:maintenanceAndUpdateFrequency>
					<app:MD_MainFreqCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:maintenanceAndUpdateFrequency/gmd:MD_MaintenanceFrequencyCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_MainFreqCode>
				</app:maintenanceAndUpdateFrequency>
			</xsl:if>
			<xsl:variable name="dateNext" select="gmd:dateOfNextUpdate/gco:DateTime" />
			<xsl:if test="$dateNext != ''">
				<app:dateOfNextUpdate>
					<xsl:value-of select="$dateNext" />
				</app:dateOfNextUpdate>
			</xsl:if>
			<xsl:variable name="maint" select="gmd:userDefinedMaintenanceFrequency/gts:TM_PeriodDuration" />
			<xsl:if test="$maint != ''">
				<app:userDefinedMaintenanceFrequency>
					<xsl:value-of select="$maint" />
				</app:userDefinedMaintenanceFrequency>
			</xsl:if>
			<xsl:variable name="note" select="gmd:maintenanceNote/gco:CharacterString" />
			<xsl:if test="$note != ''">
				<app:note>
					<xsl:value-of select="$note" />
				</app:note>
			</xsl:if>
		</app:MD_MaintenanceInformation>
	</xsl:template>

	<xsl:template match="gmd:CI_OnlineResource">
		<app:CI_OnlineResource>
			<xsl:variable name="linkage" select="gmd:linkage/gmd:URL" />
			<xsl:if test="boolean( $linkage != '' )">
				<app:linkage>
					<xsl:value-of select="$linkage" />
				</app:linkage>
			</xsl:if>
			<xsl:if test="boolean( gmd:function )">
				<app:function>
					<app:CI_OnLineFunctionCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue" />
						</app:codelistvalue>
					</app:CI_OnLineFunctionCode>
				</app:function>
			</xsl:if>
			<xsl:if test="boolean( gmd:protocol/gco:CharacterString != '' )">
				<app:protocol>
					<xsl:value-of select="gmd:protocol/gco:CharacterString"/>
				</app:protocol>
			</xsl:if>
			<xsl:if test="boolean( gmd:name/gco:CharacterString != '' )">
				<app:name>
					<xsl:value-of select="gmd:name/gco:CharacterString"/>
				</app:name>
			</xsl:if>
			<xsl:if test="boolean( gmd:description/gco:CharacterString != '' )">
				<app:description>
					<xsl:value-of select="gmd:description/gco:CharacterString"/>
				</app:description>
			</xsl:if>
		</app:CI_OnlineResource>
	</xsl:template>

	<xsl:template match="gmd:EX_Extent">
		<xsl:if test="boolean( gmd:verticalElement )">
			<app:verticalExtent>
				<xsl:call-template name="VERTEX" />
			</app:verticalExtent>
		</xsl:if>
		<xsl:if test="boolean( gmd:temporalElement )">
			<app:temportalExtent>
				<xsl:call-template name="TEMPEX" />
			</app:temportalExtent>
		</xsl:if>
		<xsl:if
			test="boolean( gmd:geographicElement/gmd:EX_GeographicBoundingBox )">
			<xsl:call-template name="GEOEX" />
		</xsl:if>
		<xsl:if
			test="boolean( gmd:geographicElement/gmd:EX_GeographicDescription )">
			<xsl:call-template name="GEODESCEX" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="TEMPEX">
		<xsl:if test="boolean( gmd:EX_TemporalExtent/gmd:extent )">
			<app:EX_TemporalExtent>
				<xsl:if test="boolean( ../gmd:description )">
					<app:description>
						<xsl:value-of select=" ../gmd:description/gco:CharacterString" />
					</app:description>
				</xsl:if>
				<xsl:variable name="begin" select="gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition"/>
				<xsl:variable name="beginPosition" select="gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:beginPosition" />
				<xsl:variable name="end" select="gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:end/gml:TimeInstant/gml:timePosition"/>
				<xsl:variable name="endPosition" select="gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:endPosition"/>
				<xsl:variable name="timePosition" select="gmd:EX_TemporalExtent/gmd:extent/gml:TimeInstant/gml:timePosition"/>
				<xsl:choose>
					<xsl:when test="boolean( $begin != '' or $beginPosition != '' ) and boolean( $end != '' or $endPosition != '' )">
							<app:begin_>
								<xsl:value-of select="$begin" />
								<xsl:value-of select="$beginPosition" />
							</app:begin_>
							<app:end_>
								<xsl:value-of select="$end" />
								<xsl:value-of select="$endPosition" />
							</app:end_>
						</xsl:when>
					<xsl:when test="boolean( $timePosition != '' )">
						<app:timePosition>
							<xsl:value-of select="$timePosition" />
						</app:timePosition>
					</xsl:when>
				</xsl:choose>
			</app:EX_TemporalExtent>
		</xsl:if>
	</xsl:template>

	<xsl:template name="GEOEX">
		<xsl:variable name="minx">
			<xsl:value-of select="gmd:westBoundLongitude/gco:Decimal" />
		</xsl:variable>
		<xsl:variable name="maxx">
			<xsl:value-of select="gmd:eastBoundLongitude/gco:Decimal" />
		</xsl:variable>
		<xsl:variable name="miny">
			<xsl:value-of select="gmd:southBoundLatitude/gco:Decimal" />
		</xsl:variable>
		<xsl:variable name="maxy">
			<xsl:value-of select="gmd:northBoundLatitude/gco:Decimal" />
		</xsl:variable>
		<app:boundingBox>
			<app:EX_GeogrBBOX>
				<xsl:if test="boolean(../../gmd:description)">
					<app:description>
						<xsl:value-of select="../../gmd:description/gco:CharacterString" />
					</app:description>
				</xsl:if>
				<app:geom>
					<gml:Polygon srsName="EPSG:4326">
						<gml:outerBoundaryIs>
							<gml:LinearRing>
								<gml:coordinates cs="," decimal="." ts=" ">
									<xsl:value-of select="concat( $minx, ',', $miny, ' ', $minx, ',', $maxy, ' ', $maxx, ',', $maxy, ' ', $maxx, ',', $miny, ' ',$minx, ',', $miny)" />
								</gml:coordinates>
							</gml:LinearRing>
						</gml:outerBoundaryIs>
					</gml:Polygon>
				</app:geom>
				<app:crs>EPSG:4326</app:crs>
			</app:EX_GeogrBBOX>
		</app:boundingBox>
	</xsl:template>

	<xsl:template name="GEODESCEX">
		<app:geographicIdentifierCode>
			<xsl:value-of select="gmd:geographicIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString" />
			<xsl:value-of select="gmd:geographicIdentifier/gmd:RS_Identifier/gmd:code/gco:CharacterString" />
		</app:geographicIdentifierCode>
	</xsl:template>
	
	<xsl:template name="VERTEX">
		<app:EX_VerticalExtent>
			<xsl:if test="boolean( gmd:EX_VerticalExtent/gmd:minimumValue )">
				<app:minval>
					<xsl:value-of select="gmd:EX_VerticalExtent/gmd:minimumValue/gco:Real" />
				</app:minval>
			</xsl:if>
			<xsl:if test="boolean( gmd:EX_VerticalExtent/gmd:maximumValue )">
				<app:maxval>
					<xsl:value-of select="gmd:EX_VerticalExtent/gmd:maximumValue/gco:Real" />
				</app:maxval>
			</xsl:if>
			<xsl:if test="boolean( gmd:EX_VerticalExtent/gmd:verticalCRS != '' )">
				<app:verticalDatum deegreewfs:skipParsing="true">	
					<xsl:copy-of select="gmd:EX_VerticalExtent/gmd:verticalCRS/child::*"/>
				</app:verticalDatum>
			</xsl:if>
            <xsl:if test="boolean( gmd:EX_VerticalExtent/gmd:verticalCRS/@xlink:href != '' )">
                <app:hrefAttribute>
                    <xsl:value-of select="gmd:EX_VerticalExtent/gmd:verticalCRS/@xlink:href"/>
                </app:hrefAttribute>
            </xsl:if>
		</app:EX_VerticalExtent>
	</xsl:template>

	<xsl:template match="gmd:RS_Identifier">
		<xsl:if test="boolean( gmd:code/gco:CharacterString != '')">
		<app:code>
			<xsl:value-of select="gmd:code/gco:CharacterString" />
		</app:code>
		</xsl:if>
		<xsl:if test="boolean( gmd:codeSpace/gco:CharacterString  != '' )">
			<app:codespace>
				<xsl:value-of select="gmd:codeSpace/gco:CharacterString" />
			</app:codespace>
		</xsl:if>
		<xsl:variable name="version" select="gmd:version/gco:CharacterString" />
		<xsl:if test="$version != ''">
			<app:version>
				<xsl:value-of select="$version" />
			</app:version>
		</xsl:if>
		<xsl:if test="boolean( gmd:authority  != '' )">
			<app:authority>
				<xsl:apply-templates select="gmd:authority/gmd:CI_Citation">
					<xsl:with-param name="context">
						Identifier
					</xsl:with-param>
				</xsl:apply-templates>
			</app:authority>
		</xsl:if>
	</xsl:template>

	<xsl:template match="gmd:MD_Distribution">
		<app:distributionInfo>
			<app:MD_Distribution>
				<xsl:apply-templates select="gmd:distributionFormat" />
				<xsl:apply-templates select="gmd:distributor" />
				<xsl:apply-templates select="gmd:transferOptions/gmd:MD_DigitalTransferOptions" />
			</app:MD_Distribution>
		</app:distributionInfo>
	</xsl:template>
	
	<xsl:template match="gmd:distributionFormat">
		<app:distributionFormat>
			<app:MD_Format>
			    <xsl:if test="boolean( gmd:MD_Format/gmd:name )">
                    <app:name>
                        <xsl:value-of select="gmd:MD_Format/gmd:name/gco:CharacterString" />
                    </app:name>
                </xsl:if>
                <xsl:if test="boolean( gmd:MD_Format/gmd:version )">
                    <app:version>
                        <xsl:value-of select="gmd:MD_Format/gmd:version/gco:CharacterString" />
                    </app:version>
                </xsl:if>
				<xsl:variable name="specification" select="gmd:MD_Format/gmd:specification/gco:CharacterString" />
				<xsl:if test="$specification != ''">
					<app:specification>
						<xsl:value-of select="$specification" />
					</app:specification>
				</xsl:if>
				<xsl:variable name="fileTech" select="gmd:MD_Format/gmd:fileDecompressionTechnique/gco:CharacterString" />
				<xsl:if test="$fileTech != ''">
					<app:filedecomptech>
						<xsl:value-of select="$fileTech" />
					</app:filedecomptech>
				</xsl:if>
				<xsl:variable name="amendNumber" select="gmd:MD_Format/gmd:amendmentNumber/gco:CharacterString" />
				<xsl:if test="$amendNumber != ''">
					<app:amendmentnumber>
						<xsl:value-of select="$amendNumber" />
					</app:amendmentnumber>
				</xsl:if>
			</app:MD_Format>
		</app:distributionFormat>
	</xsl:template>
	
	<xsl:template match="gmd:distributor">
		<app:distributor>
			<app:MD_Distributor>
				<app:distributorContact>
					<xsl:apply-templates select="gmd:MD_Distributor/gmd:distributorContact/gmd:CI_ResponsibleParty" />
				</app:distributorContact>
				<xsl:apply-templates select="gmd:MD_Distributor/gmd:distributionOrderProcess" />
			</app:MD_Distributor>
		</app:distributor>
	</xsl:template>
	
	<xsl:template match="gmd:MD_DigitalTransferOptions">
		<app:transferOptions>
			<app:MD_DigTransferOpt>
				<xsl:if test="boolean( gmd:offLine/gmd:MD_Medium/gmd:name )">
					<app:offlineMediumName>
						<app:MD_MediumNameCode>
							<app:codelistvalue>
								<xsl:value-of select="gmd:offLine/gmd:MD_Medium/gmd:name/gmd:MD_MediumNameCode/@codeListValue" />
							</app:codelistvalue>
						</app:MD_MediumNameCode>
					</app:offlineMediumName>
				</xsl:if>
				<xsl:for-each select="gmd:offLine/gmd:MD_Medium/gmd:mediumFormat">
					<app:offlineMediumFormat>
						<app:MD_MediumFormatCode>
							<app:codelistvalue>
								<xsl:value-of select="gmd:MD_MediumFormatCode/@codeListValue" />
							</app:codelistvalue>
						</app:MD_MediumFormatCode>
					</app:offlineMediumFormat>
				</xsl:for-each>
				<xsl:if test="boolean( gmd:unitsOfDistribution )">
					<app:unitsofdistribution>
						<xsl:value-of select="gmd:unitsOfDistribution/gco:CharacterString" />
					</app:unitsofdistribution>
				</xsl:if>
				<xsl:for-each select="gmd:onLine">
					<app:onlineResource>
						<xsl:apply-templates select="gmd:CI_OnlineResource" />
					</app:onlineResource>
				</xsl:for-each>
				<xsl:if test="boolean( gmd:transferSize )">
					<app:transfersize>
						<xsl:value-of select="gmd:transferSize/gco:Real" />
					</app:transfersize>
				</xsl:if>
				<xsl:if test="boolean( gmd:offLine/gmd:MD_Medium/gmd:mediumNote )">
					<app:off_mediumnote>
						<xsl:value-of select="gmd:offLine/gmd:MD_Medium/gmd:mediumNote" />
					</app:off_mediumnote>
				</xsl:if>
			</app:MD_DigTransferOpt>
		</app:transferOptions>
	</xsl:template>

	<xsl:template match="gmd:distributionOrderProcess">
		<app:distributionOrderProcess>
			<app:MD_StandOrderProc>
				<xsl:variable name="fees" select="gmd:MD_StandardOrderProcess/gmd:fees/gco:CharacterString" />
				<xsl:if test="$fees != ''">
					<app:fees>
						<xsl:value-of select="$fees" />
					</app:fees>
				</xsl:if>
				<xsl:variable name="ordInstructions" select="gmd:MD_StandardOrderProcess/gmd:orderingInstructions/gco:CharacterString" />
				<xsl:if test="$ordInstructions != ''">
					<app:orderinginstructions>
						<xsl:value-of select="$ordInstructions" />
					</app:orderinginstructions>
				</xsl:if>
				<xsl:variable name="turnaround" select="gmd:MD_StandardOrderProcess/gmd:turnaround/gco:CharacterString" />
				<xsl:if test="$turnaround != ''">
					<app:turnaround>
						<xsl:value-of select="$turnaround" />
					</app:turnaround>
				</xsl:if>
			</app:MD_StandOrderProc>
		</app:distributionOrderProcess>
	</xsl:template>

	<xsl:template match="gmd:MD_FeatureCatalogueDescription">
		<app:featureCatalogDescription>
			<app:MD_FeatCatDesc>
				<xsl:for-each select="gmd:featureCatalogueCitation">
					<app:citation>
						<xsl:apply-templates select="gmd:CI_Citation">
							<xsl:with-param name="context">
								FeatureCatalogue
							</xsl:with-param>
						</xsl:apply-templates>
					</app:citation>
				</xsl:for-each>
				<xsl:for-each select="gmd:featureTypes/gmd:LocalName">
					<xsl:if test="boolean( . != '' )">
						<app:featureType>
							<app:FeatureTypes>
								<app:localname>
									<xsl:value-of select="." />
								</app:localname>
							</app:FeatureTypes>
						</app:featureType>
					</xsl:if>
				</xsl:for-each>
				<xsl:variable name="language" select="gmd:language/gco:CharacterString" />
				<xsl:if test="boolean( $language != '' )">
					<app:language>
						<xsl:value-of select="$language" />
					</app:language>
				</xsl:if>
        <xsl:variable name="languageCode" select="gmd:language/gmd:LanguageCode/@codeListValue" />
        <xsl:if test="boolean( $languageCode != '' )">
            <app:language>
                <xsl:value-of select="$languageCode" />
            </app:language>
        </xsl:if>
				<xsl:variable name="includedWithDataset" select="gmd:includedWithDataset/gco:Boolean" />
				<xsl:if test="boolean( $includedWithDataset != '' )">
					<app:includedwithdataset>
						<xsl:value-of select="$includedWithDataset" />
					</app:includedwithdataset>
				</xsl:if>
			</app:MD_FeatCatDesc>
		</app:featureCatalogDescription>
	</xsl:template>

	<xsl:template match="gmd:MD_PortrayalCatalogueReference">
		<app:portrayalCatalogReference>
			<app:MD_PortrayalCatRef>
				<xsl:if test="boolean( gmd:portrayalCatalogueCitation/gmd:CI_Citation )">
					<app:citation>
						<xsl:apply-templates select="gmd:portrayalCatalogueCitation/gmd:CI_Citation">
							<xsl:with-param name="context">
								PortrayalCatalogue
							</xsl:with-param>
						</xsl:apply-templates>
					</app:citation>
				</xsl:if>
			</app:MD_PortrayalCatRef>
		</app:portrayalCatalogReference>
	</xsl:template>

<!-- NOT IN USE
 	<xsl:template match="smXML:MD_GeometricObjects">
		<app:MD_GeometricObjects>
			<app:geometricObjectType>
				<app:MD_GeometricObjectTypeCode>
					<app:codelistvalue>
						<xsl:value-of select="smXML:geometricObjectType/smXML:MD_GeometricObjectTypeCode/@codeListValue" />
					</app:codelistvalue>
				</app:MD_GeometricObjectTypeCode>
			</app:geometricObjectType>
		</app:MD_GeometricObjects>
	</xsl:template>
-->

	<xsl:template match="gmd:MD_Resolution">
		<app:MD_Resolution>
			<xsl:if test="boolean( gmd:equivalentScale )">
				<app:equivalentscale>
					<xsl:value-of select="gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer" />
				</app:equivalentscale>
			</xsl:if>
			<xsl:if test="boolean( gmd:distance )">
				<app:distancevalue>
					<xsl:value-of select="gmd:distance/gco:Distance" />
				</app:distancevalue>
				<app:uomName>
					<xsl:value-of select="gmd:distance/gco:Distance/@uom" />
				</app:uomName>
			</xsl:if>
		</app:MD_Resolution>
	</xsl:template>
	
	<xsl:template match="gmd:MD_ApplicationSchemaInformation">
		<app:applicationSchemaInformation>
			<app:MD_ApplicationSchemaInformation>
				<xsl:if test="boolean( gmd:name/gmd:CI_Citation )">
					<app:citation>
						<xsl:apply-templates select="gmd:name/gmd:CI_Citation">
							<xsl:with-param name="context">
								ApplicationSchemaInformation
							</xsl:with-param>
						</xsl:apply-templates>
					</app:citation>
				</xsl:if>
				<xsl:variable name="schemaLanguage" select="gmd:schemaLanguage/gco:CharacterString" />
				<xsl:if test="boolean( $schemaLanguage != '' )">
					<app:schemaLanguage>
						<xsl:value-of select="$schemaLanguage" />
					</app:schemaLanguage>
				</xsl:if>
				<xsl:variable name="constraintLanguage" select="gmd:constraintLanguage/gco:CharacterString" />
				<xsl:if test="boolean( $constraintLanguage != '' )">
					<app:constraintLanguage>
						<xsl:value-of select="$constraintLanguage" />
					</app:constraintLanguage>
				</xsl:if>
				<xsl:variable name="schemaAscii" select="gmd:schemaAscii/gco:CharacterString" />
				<xsl:if test="boolean( $schemaAscii != '' )">
					<app:schemaAscii>
						<xsl:value-of select="$schemaAscii" />
					</app:schemaAscii>
				</xsl:if>
				<xsl:variable name="graphicsFile64b" select="gmd:graphicsFile/gco:Binary" />
				<xsl:if test="boolean( $graphicsFile64b != '')">
					<app:graphicsFile64b>
						<xsl:value-of select="$graphicsFile64b" />
					</app:graphicsFile64b>
				</xsl:if>
				<xsl:variable name="softwareDevelFile64b" select="gmd:softwareDevelopmentFile/gco:Binary" />
				<xsl:if test="boolean( $softwareDevelFile64b != '' )">
					<app:softwareDevelFile64b>
						<xsl:value-of select="$softwareDevelFile64b" />
					</app:softwareDevelFile64b>
				</xsl:if>
				<xsl:variable name="softwareDevelFileFormat" select="gmd:softwareDevelopmentFileFormat/gco:CharacterString" />
				<xsl:if test="boolean( $softwareDevelFileFormat != '' )">
					<app:softwareDevelFileFormat>
						<xsl:value-of select="$softwareDevelFileFormat" />
					</app:softwareDevelFileFormat>
				</xsl:if>				
			</app:MD_ApplicationSchemaInformation>
		</app:applicationSchemaInformation>
	</xsl:template>
    
    <xsl:template match="gmd:PT_Locale">
        <xsl:variable name="localeLang" select="gmd:languageCode/gmd:LanguageCode" />
        <xsl:variable name="localeChar" select="gmd:characterEncoding/gmd:MD_CharacterSetCode" />
        <xsl:if test="boolean($localeLang/@codeList != '' ) and boolean( $localeLang/@codeListValue != '' ) and
                        boolean($localeChar/@codeList != '' ) and boolean( $localeChar/@codeListValue != '' )">
            <app:locale>
                <app:PT_Locale>
                    <app:languageCode>
                        <xsl:value-of select="$localeLang/@codeListValue" />
                    </app:languageCode>
                    <xsl:variable name="localeCountry" select="gmd:country/gmd:Country" />
                    <xsl:if test="boolean($localeCountry/@codeList != '' ) and boolean( $localeCountry/@codeListValue != '' )">
                        <app:country>
                            <xsl:value-of select="$localeCountry/@codeListValue" />
                        </app:country>
                    </xsl:if>
                    <app:characterEncoding>
                        <app:MD_CharacterSetCode>
                            <app:codelistvalue>
                                <xsl:value-of select="$localeChar/@codeListValue"/>
                            </app:codelistvalue>
                        </app:MD_CharacterSetCode>
                    </app:characterEncoding>
                </app:PT_Locale>
            </app:locale>   
        </xsl:if>
    </xsl:template>
	
	<!-- =========================================================== 
		FILTER
		===============================================================-->
	<xsl:template match="csw:Constraint/ogc:Filter">
		<ogc:Filter>
			<xsl:apply-templates select="ogc:And" />
			<xsl:apply-templates select="ogc:Or" />
			<xsl:apply-templates select="ogc:Not" />
			<xsl:if
				test="local-name(./child::*[1]) != 'And' and local-name(./child::*[1])!='Or' and local-name(./child::*[1])!='Not'">
				<xsl:for-each select="./child::*">
					<xsl:call-template name="copyProperty" />
				</xsl:for-each>
			</xsl:if>
		</ogc:Filter>
	</xsl:template>
	<xsl:template match="ogc:And | ogc:Or | ogc:Not">
		<xsl:copy>
			<xsl:apply-templates select="ogc:And" />
			<xsl:apply-templates select="ogc:Or" />
			<xsl:apply-templates select="ogc:Not" />
			<xsl:for-each select="./child::*">
				<xsl:if
					test="local-name(.) != 'And' and local-name(.)!='Or' and local-name(.)!='Not'">
					<xsl:call-template name="copyProperty" />
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template name="copyProperty">
		<xsl:copy>
			<xsl:if test="local-name(.) = 'PropertyIsLike'">
				<xsl:attribute name="wildCard">
					<xsl:value-of select="./@wildCard" />
				</xsl:attribute>
				<xsl:attribute name="singleChar">
					<xsl:value-of select="./@singleChar" />
				</xsl:attribute>
				<xsl:attribute name="escapeChar">
					<xsl:value-of select="./@escape" />
					<xsl:value-of select="./@escapeChar" />
				</xsl:attribute>
			</xsl:if>
			<ogc:PropertyName>
				<xsl:apply-templates select="ogc:PropertyName" />
			</ogc:PropertyName>
			<xsl:for-each select="./child::*">
				<xsl:if test="local-name(.) != 'PropertyName' ">
					<xsl:copy-of select="." />
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="ogc:PropertyName | csw:ElementName">
		<!-- mapping property name value -->
		<xsl:value-of select="mapping:mapPropertyValue( $map, ., $NSP )" />
	</xsl:template>
	
	
</xsl:stylesheet>
