<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:app="http://www.deegree.org/app"
	xmlns:csw="http://www.opengis.net/cat/csw"
	xmlns:gml="http://www.opengis.net/gml"
	xmlns:iso19115="http://schemas.opengis.net/iso19115full"
	xmlns:ogc="http://www.opengis.net/ogc"
	xmlns:smXML="http://metadata.dgiwg.org/smXML"
	xmlns:wfs="http://www.opengis.net/wfs" xmlns:java="java"
	xmlns:mapping="org.deegree.ogcwebservices.csw.iso_profile.Mapping" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes" method="xml"
		version="1.0" />
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
			<xsl:apply-templates select="smXML:MD_Metadata" />
			<xsl:apply-templates select="iso19115:MD_Metadata" />
		</wfs:Insert>
	</xsl:template>
	<xsl:template match="csw:Update">
		<wfs:Update typeName="app:MD_Metadata">
			<xsl:apply-templates select="smXML:MD_Metadata" />
			<xsl:apply-templates select="iso19115:MD_Metadata" />
			<xsl:apply-templates select="csw:Constraint/ogc:Filter" />
		</wfs:Update>
	</xsl:template>
	<xsl:template match="csw:Delete">
		<wfs:Delete typeName="app:MD_Metadata">
			<xsl:apply-templates select="csw:Constraint/ogc:Filter" />
		</wfs:Delete>
	</xsl:template>

	<xsl:template match="smXML:CI_ResponsibleParty">
		<app:CI_RespParty>
			<xsl:variable name="individualName"
				select="smXML:individualName/smXML:CharacterString" />
			<xsl:if test="boolean( $individualName != '' )">
				<app:individualname>
					<xsl:value-of select="$individualName" />
				</app:individualname>
			</xsl:if>
			<xsl:variable name="organisationName"
				select="smXML:organisationName/smXML:CharacterString" />
			<xsl:if test="boolean( $organisationName != '' )">
				<app:organisationname>
					<xsl:value-of select="$organisationName" />
				</app:organisationname>
			</xsl:if>
			<xsl:variable name="positionName"
				select="smXML:positionName/smXML:CharacterString" />
			<xsl:if test="boolean( $positionName != '' )">
				<app:positionname>
					<xsl:value-of select="$positionName" />
				</app:positionname>
			</xsl:if>
			<xsl:if
				test="boolean(smXML:role/smXML:CI_RoleCode/@codeListValue)">
				<app:role>
					<app:CI_RoleCode>
						<app:codelistvalue>
							<xsl:value-of
								select="smXML:role/smXML:CI_RoleCode/@codeListValue" />
						</app:codelistvalue>
					</app:CI_RoleCode>
				</app:role>
			</xsl:if>
			<xsl:if test="smXML:contactInfo">
				<app:contactInfo>
					<xsl:apply-templates
						select="smXML:contactInfo/smXML:CI_Contact" />
				</app:contactInfo>
			</xsl:if>
		</app:CI_RespParty>
	</xsl:template>

	<xsl:template match="smXML:CI_Contact">
		<app:CI_Contact>
			<xsl:variable name="voice"
				select="smXML:phone/smXML:CI_Telephone/smXML:voice/smXML:CharacterString" />
			<xsl:if test="$voice != ''">
				<app:voice>
					<xsl:value-of select="$voice" />
				</app:voice>
			</xsl:if>
			<xsl:variable name="fax"
				select="smXML:phone/smXML:CI_Telephone/smXML:facsimile/smXML:CharacterString" />
			<xsl:if test="$fax != ''">
				<app:facsimile>
					<xsl:value-of select="$fax" />
				</app:facsimile>
			</xsl:if>
			<xsl:apply-templates
				select="smXML:address/smXML:CI_Address" />
			<xsl:for-each select="smXML:onlineResource">
				<app:onlineResource>
					<xsl:apply-templates
						select="smXML:CI_OnlineResource" />
				</app:onlineResource>
			</xsl:for-each>
			<xsl:variable name="hrs"
				select="smXML:hoursOfService/smXML:CharacterString" />
			<xsl:if test="$hrs != ''">
				<app:hoursofservice>
					<xsl:apply-templates select="$hrs" />
				</app:hoursofservice>
			</xsl:if>
			<xsl:variable name="contact"
				select="smXML:contactInstructions/smXML:CharacterString" />
			<xsl:if test="$contact != ''">
				<app:contactinstructions>
					<xsl:value-of select="$contact" />
				</app:contactinstructions>
			</xsl:if>
		</app:CI_Contact>
	</xsl:template>

	<xsl:template match="smXML:CI_Address">
		<app:address>
			<app:CI_Address>
				<xsl:for-each select="smXML:deliveryPoint">
					<xsl:if
						test="boolean( smXML:CharacterString != '' )">
						<app:deliveryPoint>
							<app:DeliveryPoint>
								<app:deliverypoint>
									<xsl:value-of
										select="smXML:CharacterString" />
								</app:deliverypoint>
							</app:DeliveryPoint>
						</app:deliveryPoint>
					</xsl:if>
				</xsl:for-each>
				<xsl:variable name="city"
					select="smXML:city/smXML:CharacterString" />
				<xsl:if test="$city != ''">
					<app:city>
						<xsl:value-of select="$city" />
					</app:city>
				</xsl:if>
				<xsl:variable name="admin"
					select="smXML:administrativeArea/smXML:CharacterString" />
				<xsl:if test="$admin != ''">
					<app:administrativeArea>
						<xsl:value-of select="$admin" />
					</app:administrativeArea>
				</xsl:if>
				<xsl:variable name="postal"
					select="smXML:postalCode/smXML:CharacterString" />
				<xsl:if test="$postal != ''">
					<app:postalCode>
						<xsl:value-of select="$postal" />
					</app:postalCode>
				</xsl:if>
				<xsl:variable name="country"
					select="smXML:country/smXML:CharacterString" />
				<xsl:if test="$country != ''">
					<app:country>
						<xsl:value-of select="$country" />
					</app:country>
				</xsl:if>
				<xsl:for-each select="smXML:electronicMailAddress">
					<xsl:if
						test="boolean( smXML:CharacterString != '' )">
						<app:electronicMailAddress>
							<app:ElectronicMailAddress>
								<app:email>
									<xsl:value-of
										select="smXML:CharacterString" />
								</app:email>
							</app:ElectronicMailAddress>
						</app:electronicMailAddress>
					</xsl:if>
				</xsl:for-each>
			</app:CI_Address>
		</app:address>
	</xsl:template>

	<xsl:template match="smXML:CI_Citation">
		<xsl:param name="context" />
		<app:CI_Citation>
			<xsl:for-each select="smXML:alternateTitle">
				<xsl:if test="boolean( smXML:CharacterString != '' )">
					<app:alternateTitle>
						<xsl:value-of select="smXML:CharacterString" />
					</app:alternateTitle>
				</xsl:if>
			</xsl:for-each>
			<xsl:apply-templates select="smXML:series/smXML:CI_Series" />
			<xsl:for-each select="smXML:presentationForm">
				<app:presentationForm>
					<app:CI_PresentationFormCode>
						<app:codelistvalue>
							<xsl:value-of
								select="smXML:CI_PresentationFormCode/@codeListValue" />
						</app:codelistvalue>
					</app:CI_PresentationFormCode>
				</app:presentationForm>
			</xsl:for-each>
			<xsl:for-each select="smXML:citedResponsibleParty">
				<app:citedResponsibleParty>
					<xsl:apply-templates
						select="smXML:CI_ResponsibleParty" />
				</app:citedResponsibleParty>
			</xsl:for-each>
			<xsl:variable name="title"
				select="smXML:title/smXML:CharacterString" />
			<xsl:if test="boolean( $title != '' )">
				<app:title>
					<xsl:value-of select="$title" />
				</app:title>
			</xsl:if>
			<xsl:variable name="edition"
				select="smXML:edition/smXML:CharacterString" />
			<xsl:if test="$edition != ''">
				<app:edition>
					<xsl:value-of select="$edition" />
				</app:edition>
			</xsl:if>
			<xsl:if test="boolean( smXML:editionDate )">
				<app:editiondate>
					<xsl:value-of
						select="smXML:editionDate/smXML:DateTime" />
					<xsl:value-of select="smXML:editionDate/smXML:Date" />
				</app:editiondate>
			</xsl:if>
			<!-- mehrfach laut ISO19115, nur einfach abgebildet -->
			<xsl:variable name="identifier"
				select="smXML:identifier/smXML:MD_Identifier/smXML:code/smXML:CharacterString" />
			<xsl:if test="boolean( $identifier != '' )">
				<app:identifier>
					<xsl:value-of select="$identifier" />
				</app:identifier>
			</xsl:if>
			<xsl:variable name="isbn"
				select="smXML:ISBN/smXML:CharacterString" />
			<xsl:if test="$isbn != ''">
				<app:isbn>
					<xsl:value-of select="$isbn" />
				</app:isbn>
			</xsl:if>
			<xsl:variable name="issn"
				select="smXML:ISSN/smXML:CharacterString" />
			<xsl:if test="$issn != ''">
				<app:issn>
					<xsl:value-of select="$issn" />
				</app:issn>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="boolean( smXML:date != '' )">
					<xsl:for-each select="smXML:date">
						<xsl:if
							test="smXML:CI_Date/smXML:dateType/smXML:CI_DateTypeCode/@codeListValue = 'revision'">
							<app:revisiondate>
								<xsl:value-of
									select="smXML:CI_Date/smXML:date/smXML:Date" />
								<xsl:value-of
									select="smXML:CI_Date/smXML:date/smXML:DateTime" />
							</app:revisiondate>
						</xsl:if>
						<xsl:if
							test="smXML:CI_Date/smXML:dateType/smXML:CI_DateTypeCode/@codeListValue = 'creation'">
							<app:creationdate>
								<xsl:value-of
									select="smXML:CI_Date/smXML:date/smXML:Date" />
								<xsl:value-of
									select="smXML:CI_Date/smXML:date/smXML:DateTime" />
							</app:creationdate>
						</xsl:if>
						<xsl:if
							test="smXML:CI_Date/smXML:dateType/smXML:CI_DateTypeCode/@codeListValue = 'publication'">
							<app:publicationdate>
								<xsl:value-of
									select="smXML:CI_Date/smXML:date/smXML:Date" />
								<xsl:value-of
									select="smXML:CI_Date/smXML:date/smXML:DateTime" />
							</app:publicationdate>
						</xsl:if>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message>
						------------- Mandatory Element 'smXML:date'
						missing! ----------------
					</xsl:message>
					<app:exception>
						Mandatory Element 'smXML:date' missing!
					</app:exception>
				</xsl:otherwise>
			</xsl:choose>
			<app:context>
				<xsl:value-of select="$context" />
			</app:context>
		</app:CI_Citation>
	</xsl:template>

	<xsl:template match="smXML:series/smXML:CI_Series">
		<app:series>
			<app:CI_Series>
				<xsl:variable name="name"
					select="smXML:name/smXML:CharacterString" />
				<xsl:if test="$name != ''">
					<app:name>
						<xsl:value-of select="$name" />
					</app:name>
				</xsl:if>
				<xsl:variable name="issueId"
					select="smXML:issueIdentification/smXML:CharacterString" />
				<xsl:if test="$issueId != ''">
					<app:issueidentification>
						<xsl:value-of select="$issueId" />
					</app:issueidentification>
				</xsl:if>
				<xsl:if test="smXML:page">
					<app:page>
						<xsl:value-of
							select="smXML:page/smXML:CharacterString" />
					</app:page>
				</xsl:if>
			</app:CI_Series>
		</app:series>
	</xsl:template>

	<xsl:template match="smXML:MD_Usage">
		<app:MD_Usage>
			<xsl:variable name="spec"
				select="smXML:specificUsage/smXML:CharacterString" />
			<xsl:if test="$spec != ''">
				<app:specificusage>
					<xsl:value-of select="$spec" />
				</app:specificusage>
			</xsl:if>
			<xsl:if
				test="smXML:userContactInfo/smXML:CI_ResponsibleParty">
				<app:RespParty>
					<xsl:apply-templates
						select="smXML:userContactInfo/smXML:CI_ResponsibleParty" />
				</app:RespParty>
			</xsl:if>
		</app:MD_Usage>
	</xsl:template>

	<xsl:template match="smXML:MD_Keywords">
		<app:MD_Keywords>
			<xsl:for-each select="smXML:keyword">
				<xsl:if test="boolean( smXML:CharacterString != '' )">
					<app:keyword>
						<app:Keyword>
							<app:keyword>
								<xsl:value-of
									select="smXML:CharacterString" />
							</app:keyword>
						</app:Keyword>
					</app:keyword>
				</xsl:if>
			</xsl:for-each>
			<xsl:if test="boolean( smXML:type ) ">
				<app:type>
					<app:MD_KeywordTypeCode>
						<app:codelistvalue>
							<xsl:value-of
								select="smXML:type/smXML:MD_KeywordTypeCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_KeywordTypeCode>
				</app:type>
			</xsl:if>
			<xsl:if test="smXML:thesaurusName">
				<app:thesaurusName>
					<xsl:apply-templates
						select="smXML:thesaurusName/smXML:CI_Citation">
						<xsl:with-param name="context">
							Keywords
						</xsl:with-param>
					</xsl:apply-templates>
				</app:thesaurusName>
			</xsl:if>
		</app:MD_Keywords>
	</xsl:template>

	<xsl:template match="smXML:MD_LegalConstraints">
		<app:MD_LegalConstraints>
			<xsl:if test="boolean( smXML:useLimitation )">
				<app:useLimitations>
					<xsl:value-of select="smXML:useLimitation" />
				</app:useLimitations>
			</xsl:if>
			<xsl:variable name="otherConst"
				select="smXML:otherConstraints/smXML:CharacterString" />
			<xsl:if test="$otherConst != ''">
				<app:otherConstraints>
					<xsl:value-of select="$otherConst" />
				</app:otherConstraints>
			</xsl:if>
			<xsl:for-each
				select="smXML:useConstraints/smXML:MD_RestrictionCode">
				<app:useConstraints>
					<app:MD_RestrictionCode>
						<app:codelistvalue>
							<xsl:value-of select="./@codeListValue" />
						</app:codelistvalue>
					</app:MD_RestrictionCode>
				</app:useConstraints>
			</xsl:for-each>
			<xsl:for-each
				select="smXML:accessConstraints/smXML:MD_RestrictionCode">
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

	<xsl:template match="smXML:MD_SecurityConstraints">
		<app:MD_SecurityConstraints>
			<xsl:if
				test="boolean( smXML:classification/smXML:MD_RestrictionCode )">
				<app:classification>
					<app:MD_ClassificationCode>
						<app:codelistvalue>
							<xsl:value-of
								select="smXML:classification/smXML:MD_RestrictionCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_ClassificationCode>
				</app:classification>
			</xsl:if>
			<xsl:variable name="userNote"
				select="smXML:userNote/smXML:CharacterString" />
			<xsl:if test="boolean( $userNote != '')">
				<app:userNote>
					<xsl:value-of select="$userNote" />
				</app:userNote>
			</xsl:if>
			<xsl:variable name="classificationSystem"
				select="smXML:classificationSystem/smXML:CharacterString" />
			<xsl:if test="boolean( $classificationSystem != '' )">
				<app:classificationSystem>
					<xsl:value-of select="$classificationSystem" />
				</app:classificationSystem>
			</xsl:if>
			<xsl:variable name="handlingDescription"
				select="smXML:handlingDescription/smXML:CharacterString" />
			<xsl:if test="boolean( $handlingDescription != '' )">
				<app:handlingDescription>
					<xsl:value-of select="$handlingDescription" />
				</app:handlingDescription>
			</xsl:if>
			<xsl:for-each select="smXML:useLimitation">
				<xsl:if test="boolean( smXML:CharacterString != '' )">
					<app:useLimitations>
						<xsl:value-of select="smXML:CharacterString" />
					</app:useLimitations>
				</xsl:if>
			</xsl:for-each>
		</app:MD_SecurityConstraints>
	</xsl:template>

	<xsl:template match="smXML:MD_MaintenanceInformation">
		<app:MD_MaintenanceInformation>
			<xsl:if test="boolean( smXML:updateScope )">
				<app:updateScope>
					<app:MD_ScopeCode>
						<app:codelistvalue>
							<xsl:value-of
								select="smXML:updateScope/smXML:MD_ScopeCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_ScopeCode>
				</app:updateScope>
			</xsl:if>
			<xsl:if
				test="boolean( smXML:maintenanceAndUpdateFrequency/smXML:MD_MaintenanceFrequencyCode )">
				<app:maintenanceAndUpdateFrequency>
					<app:MD_MainFreqCode>
						<app:codelistvalue>
							<xsl:value-of
								select="smXML:maintenanceAndUpdateFrequency/smXML:MD_MaintenanceFrequencyCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_MainFreqCode>
				</app:maintenanceAndUpdateFrequency>
			</xsl:if>
			<xsl:variable name="dateNext"
				select="smXML:dateOfNextUpdate/smXML:DateTime" />
			<xsl:if test="$dateNext != ''">
				<app:dateOfNextUpdate>
					<xsl:value-of select="$dateNext" />
				</app:dateOfNextUpdate>
			</xsl:if>
			<xsl:variable name="maint"
				select="smXML:userDefinedMaintenanceFrequency/smXML:TM_PeriodDuration" />
			<xsl:if test="$maint != ''">
				<app:userDefinedMaintenanceFrequency>
					<xsl:value-of select="$maint" />
				</app:userDefinedMaintenanceFrequency>
			</xsl:if>
			<xsl:variable name="note"
				select="smXML:maintenanceNote/smXML:CharacterString" />
			<xsl:if test="$note != ''">
				<app:note>
					<xsl:value-of select="$note" />
				</app:note>
			</xsl:if>
		</app:MD_MaintenanceInformation>
	</xsl:template>

	<xsl:template match="smXML:CI_OnlineResource">
		<app:CI_OnlineResource>
			<xsl:variable name="linkage"
				select="smXML:linkage/smXML:URL" />
			<xsl:if test="boolean( $linkage != '' )">
				<app:linkage>
					<xsl:value-of select="$linkage" />
				</app:linkage>
			</xsl:if>
			<xsl:if test="boolean( smXML:function )">
				<app:function>
					<app:CI_OnLineFunctionCode>
						<app:codelistvalue>
							<xsl:value-of
								select="smXML:function/smXML:CI_OnLineFunctionCode/@codeListValue" />
						</app:codelistvalue>
					</app:CI_OnLineFunctionCode>
				</app:function>
			</xsl:if>
		</app:CI_OnlineResource>
	</xsl:template>

	<xsl:template match="smXML:EX_Extent">
		<xsl:if test="boolean( smXML:verticalElement )">
			<app:verticalExtent>
				<xsl:call-template name="VERTEX" />
			</app:verticalExtent>
		</xsl:if>
		<xsl:if test="boolean( smXML:temporalElement )">
			<app:temportalExtent>
				<xsl:call-template name="TEMPEX" />
			</app:temportalExtent>
		</xsl:if>
		<xsl:if
			test="boolean( smXML:geographicElement/smXML:EX_GeographicBoundingBox )">
			<xsl:call-template name="GEOEX" />
		</xsl:if>
		<xsl:if
			test="boolean( smXML:geographicElement/smXML:EX_GeographicDescription )">
			<xsl:call-template name="GEODESCEX" />
		</xsl:if>
	</xsl:template>

	<xsl:template name="TEMPEX">
		<xsl:if
			test="boolean( smXML:EX_TemporalExtent/smXML:extent )">
			<app:EX_TemporalExtent>
				<xsl:if test="boolean( ../smXML:description )">
					<app:description>
						<xsl:value-of
							select=" ../smXML:description/smXML:CharacterString" />
					</app:description>
				</xsl:if>
				<app:begin_>
					<xsl:value-of
						select="smXML:EX_TemporalExtent/smXML:extent/smXML:TM_Primitive/gml:begin/gml:TimeInstant/gml:timePosition" />
					<xsl:value-of
						select="smXML:EX_TemporalExtent/smXML:extent/smXML:TM_Primitive/gml:beginPosition" />
				</app:begin_>
				<app:end_>
					<xsl:value-of
						select="smXML:EX_TemporalExtent/smXML:extent/smXML:TM_Primitive/gml:end/gml:TimeInstant/gml:timePosition" />
					<xsl:value-of
						select="smXML:EX_TemporalExtent/smXML:extent/smXML:TM_Primitive/gml:endPosition" />
				</app:end_>
			</app:EX_TemporalExtent>
		</xsl:if>
	</xsl:template>

	<xsl:template name="GEOEX">
		<xsl:variable name="minx">
			<xsl:value-of
				select="smXML:westBoundLongitude/smXML:approximateLongitude" />
		</xsl:variable>
		<xsl:variable name="maxx">
			<xsl:value-of
				select="smXML:eastBoundLongitude/smXML:approximateLongitude" />
		</xsl:variable>
		<xsl:variable name="miny">
			<xsl:value-of
				select="smXML:southBoundLatitude/smXML:approximateLatitude" />
		</xsl:variable>
		<xsl:variable name="maxy">
			<xsl:value-of
				select="smXML:northBoundLatitude/smXML:approximateLatitude" />
		</xsl:variable>
		<app:boundingBox>
			<app:EX_GeogrBBOX>
				<xsl:if test="boolean(../../smXML:description)">
					<app:description>
						<xsl:value-of
							select="../../smXML:description/smXML:CharacterString" />
					</app:description>
				</xsl:if>
				<app:geom>
					<gml:Polygon srsName="EPSG:4326">
						<gml:outerBoundaryIs>
							<gml:LinearRing>
								<gml:coordinates cs="," decimal="."
									ts=" ">
									<xsl:value-of
										select="concat( $minx, ',', $miny, ' ', $minx, ',', $maxy, ' ', $maxx, ',', $maxy, ' ', $maxx, ',', $miny, ' ',$minx, ',', $miny)" />
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
			<xsl:value-of
				select="smXML:geographicIdentifier/smXML:MD_Identifier/smXML:code/smXML:CharacterString" />
		</app:geographicIdentifierCode>
	</xsl:template>
	<xsl:template name="VERTEX">
		<app:EX_VerticalExtent>
			<xsl:if
				test="boolean( smXML:EX_VerticalExtent/smXML:minimumValue )">
				<app:minval>
					<xsl:value-of
						select="smXML:EX_VerticalExtent/smXML:minimumValue/smXML:Real" />
				</app:minval>
			</xsl:if>
			<xsl:if
				test="boolean( smXML:EX_VerticalExtent/smXML:maximumValue )">
				<app:maxval>
					<xsl:value-of
						select="smXML:EX_VerticalExtent/smXML:maximumValue/smXML:Real" />
				</app:maxval>
			</xsl:if>
			<xsl:if
				test="boolean( smXML:EX_VerticalExtent/smXML:unitOfMeasure/smXML:UomLength/smXML:uomName )">
				<app:uomname>
					<xsl:value-of
						select="smXML:EX_VerticalExtent/smXML:unitOfMeasure/smXML:UomLength/smXML:uomName/smXML:CharacterString" />
				</app:uomname>
			</xsl:if>
			<xsl:if
				test="boolean( smXML:EX_VerticalExtent/smXML:unitOfMeasure/smXML:UomLength/smXML:conversionTolSOstandardUnit )">
				<app:convtoisostdunit>
					<xsl:value-of
						select="smXML:EX_VerticalExtent/smXML:unitOfMeasure/smXML:UomLength/smXML:conversionTolSOstandardUnit/smXML:Real" />
				</app:convtoisostdunit>
			</xsl:if>
			<xsl:if
				test="boolean( smXML:EX_VerticalExtent/smXML:verticalDatum/smXML:RS_Identifier )">
				<app:verticalDatum>
					<app:RS_Identifier>
						<xsl:apply-templates
							select="smXML:EX_VerticalExtent/smXML:verticalDatum/smXML:RS_Identifier" />
					</app:RS_Identifier>
				</app:verticalDatum>
			</xsl:if>
		</app:EX_VerticalExtent>
	</xsl:template>

	<xsl:template match="smXML:RS_Identifier">
		<xsl:if test="boolean( smXML:code/smXML:CharacterString != '')">
		<app:code>
			<xsl:value-of select="smXML:code/smXML:CharacterString" />
		</app:code>
		</xsl:if>
		<xsl:if
			test="boolean( smXML:codeSpace/smXML:CharacterString  != '' )">
			<app:codespace>
				<xsl:value-of
					select="smXML:codeSpace/smXML:CharacterString" />
			</app:codespace>
		</xsl:if>
		<xsl:variable name="version"
			select="smXML:version/smXML:CharacterString" />
		<xsl:if test="$version != ''">
			<app:version>
				<xsl:value-of select="$version" />
			</app:version>
		</xsl:if>
		<xsl:if test="boolean(smXML:authority  != '' )">
			<app:authority>
				<xsl:apply-templates select="smXML:CI_Citation">
					<xsl:with-param name="context">
						Identifier
					</xsl:with-param>
				</xsl:apply-templates>
			</app:authority>
		</xsl:if>
	</xsl:template>

	<xsl:template match="smXML:MD_Distribution">
		<app:distributionInfo>
			<app:MD_Distribution>
				<xsl:apply-templates select="smXML:distributionFormat" />
				<xsl:apply-templates select="smXML:distributor" />
				<xsl:apply-templates
					select="smXML:transferOptions/smXML:MD_DigitalTransferOptions" />
			</app:MD_Distribution>
		</app:distributionInfo>
	</xsl:template>
	<xsl:template match="smXML:distributionFormat">
		<app:distributionFormat>
			<app:MD_Format>
				<xsl:variable name="Formatname"
					select="smXML:MD_Format/smXML:name/smXML:CharacterString" />
				<xsl:if test="boolean( $Formatname != '' )">
					<app:name>
						<xsl:value-of select="$Formatname" />
					</app:name>
				</xsl:if>
				<xsl:variable name="Formatversion"
					select="smXML:MD_Format/smXML:version/smXML:CharacterString" />
				<xsl:if test="boolean( $Formatversion != '' )">
					<app:version>
						<xsl:value-of select="$Formatversion" />
					</app:version>
				</xsl:if>
				<xsl:variable name="specification"
					select="smXML:MD_Format/smXML:specification/smXML:CharacterString" />
				<xsl:if test="$specification != ''">
					<app:specification>
						<xsl:value-of select="$specification" />
					</app:specification>
				</xsl:if>
				<xsl:variable name="fileTech"
					select="smXML:MD_Format/smXML:fileDecompressionTechnique/smXML:CharacterString" />
				<xsl:if test="$fileTech != ''">
					<app:filedecomptech>
						<xsl:value-of select="$fileTech" />
					</app:filedecomptech>
				</xsl:if>
				<xsl:variable name="amendNumber"
					select="smXML:MD_Format/smXML:amendmentNumber/smXML:CharacterString" />
				<xsl:if test="$amendNumber != ''">
					<app:amendmentnumber>
						<xsl:value-of select="$amendNumber" />
					</app:amendmentnumber>
				</xsl:if>
			</app:MD_Format>
		</app:distributionFormat>
	</xsl:template>
	<xsl:template match="smXML:distributor">
		<app:distributor>
			<app:MD_Distributor>
				<app:distributorContact>
					<xsl:apply-templates
						select="smXML:MD_Distributor/smXML:distributorContact/smXML:CI_ResponsibleParty" />
				</app:distributorContact>
				<xsl:apply-templates
					select="smXML:MD_Distributor/smXML:distributionOrderProcess" />
			</app:MD_Distributor>
		</app:distributor>
	</xsl:template>
	<xsl:template match="smXML:MD_DigitalTransferOptions">
		<app:transferOptions>
			<app:MD_DigTransferOpt>

				<xsl:if
					test="boolean( smXML:offLine/smXML:MD_Medium/smXML:name )">
					<app:offlineMediumName>
						<app:MD_MediumNameCode>
							<app:codelistvalue>
								<xsl:value-of
									select="smXML:offLine/smXML:MD_Medium/smXML:name/smXML:MD_MediumNameCode/@codeListValue" />
							</app:codelistvalue>
						</app:MD_MediumNameCode>
					</app:offlineMediumName>
				</xsl:if>
				<xsl:for-each
					select="smXML:offLine/smXML:MD_Medium/smXML:mediumFormat">
					<app:offlineMediumFormat>
						<app:MD_MediumFormatCode>
							<app:codelistvalue>
								<xsl:value-of
									select="smXML:MD_MediumFormatCode/@codeListValue" />
							</app:codelistvalue>
						</app:MD_MediumFormatCode>
					</app:offlineMediumFormat>
				</xsl:for-each>
				<xsl:if test="boolean( smXML:unitsOfDistribution )">
					<app:unitsofdistribution>
						<xsl:value-of
							select="smXML:unitsOfDistribution/smXML:CharacterString" />
					</app:unitsofdistribution>
				</xsl:if>
				<xsl:if test="boolean( smXML:onLine )">
					<app:onlineResource>
						<xsl:apply-templates
							select="smXML:onLine/smXML:CI_OnlineResource" />
					</app:onlineResource>
				</xsl:if>
				<xsl:if test="boolean( smXML:transferSize )">
					<app:transfersize>
						<xsl:value-of select="smXML:transferSize" />
					</app:transfersize>
				</xsl:if>
				<xsl:if
					test="boolean( smXML:offLine/smXML:MD_Medium/smXML:mediumNote )">
					<app:off_mediumnote>
						<xsl:value-of
							select="smXML:offLine/smXML:MD_Medium/smXML:mediumNote" />
					</app:off_mediumnote>
				</xsl:if>
			</app:MD_DigTransferOpt>
		</app:transferOptions>
	</xsl:template>

	<xsl:template match="smXML:distributionOrderProcess">
		<app:distributionOrderProcess>
			<app:MD_StandOrderProc>
				<xsl:variable name="fees"
					select="smXML:MD_StandardOrderProcess/smXML:fees/smXML:CharacterString" />
				<xsl:if test="$fees != ''">
					<app:fees>
						<xsl:value-of select="$fees" />
					</app:fees>
				</xsl:if>
				<xsl:variable name="ordInstructions"
					select="smXML:MD_StandardOrderProcess/smXML:orderingInstructions/smXML:CharacterString" />
				<xsl:if test="$ordInstructions != ''">
					<app:orderinginstructions>
						<xsl:value-of select="$ordInstructions" />
					</app:orderinginstructions>
				</xsl:if>
				<xsl:variable name="turnaround"
					select="smXML:MD_StandardOrderProcess/smXML:turnaround/smXML:CharacterString" />
				<xsl:if test="$turnaround != ''">
					<app:turnaround>
						<xsl:value-of select="$turnaround" />
					</app:turnaround>
				</xsl:if>
			</app:MD_StandOrderProc>
		</app:distributionOrderProcess>
	</xsl:template>

	<xsl:template match="smXML:MD_FeatureCatalogueDescription">
		<app:featureCatalogDescription>
			<app:MD_FeatCatDesc>
				<xsl:for-each select="smXML:featureCatalogueCitation">
					<app:citation>
						<xsl:apply-templates
							select="smXML:CI_Citation">
							<xsl:with-param name="context">
								FeatureCatalogue
							</xsl:with-param>
						</xsl:apply-templates>
					</app:citation>
				</xsl:for-each>
				<xsl:for-each select="smXML:LocalName">
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
				<xsl:variable name="language"
					select="smXML:language/smXML:CharacterString" />
				<xsl:if test="boolean( $language != '' )">
					<app:language>
						<xsl:value-of select="$language" />
					</app:language>
				</xsl:if>
				<xsl:variable name="includedWithDataset"
					select="smXML:includedWithDataset/smXML:Boolean" />
				<xsl:if test="boolean( $includedWithDataset != '' )">
					<app:includedwithdataset>
						<xsl:value-of select="$includedWithDataset" />
					</app:includedwithdataset>
				</xsl:if>
			</app:MD_FeatCatDesc>
		</app:featureCatalogDescription>
	</xsl:template>

	<xsl:template match="smXML:MD_PortrayalCatalogueReference">
		<app:portrayalCatalogReference>
			<app:MD_PortrayalCatRef>
				<xsl:if
					test="boolean( smXML:portrayalCatalogueCitation/smXML:CI_Citation )">
					<app:citation>
						<xsl:apply-templates
							select="smXML:portrayalCatalogueCitation/smXML:CI_Citation">
							<xsl:with-param name="context">
								PortrayalCatalogue
							</xsl:with-param>
						</xsl:apply-templates>
					</app:citation>
				</xsl:if>
			</app:MD_PortrayalCatRef>
		</app:portrayalCatalogReference>
	</xsl:template>

	<xsl:template match="smXML:MD_GeometricObjects">
		<app:MD_GeometricObjects>
			<app:geometricObjectType>
				<app:MD_GeometricObjectTypeCode>
					<app:codelistvalue>
						<xsl:value-of
							select="smXML:geometricObjectType/smXML:MD_GeometricObjectTypeCode/@codeListValue" />
					</app:codelistvalue>
				</app:MD_GeometricObjectTypeCode>
			</app:geometricObjectType>
		</app:MD_GeometricObjects>
	</xsl:template>

	<xsl:template match="smXML:MD_Resolution">
		<app:MD_Resolution>
			<xsl:if test="boolean( smXML:equivalentScale )">
				<app:equivalentscale>
					<xsl:value-of
						select="smXML:equivalentScale/smXML:MD_RepresentativeFraction/smXML:denominator/smXML:positiveInteger" />
				</app:equivalentscale>
			</xsl:if>
			<xsl:if test="boolean( smXML:distance )">
				<app:distancevalue>
					<xsl:value-of
						select="smXML:distance/smXML:Distance/smXML:value/smXML:Decimal" />
				</app:distancevalue>
				<app:uomName>
					<xsl:value-of
						select="smXML:distance/smXML:Distance/smXML:uom/smXML:UomLength/smXML:uomName/smXML:CharacterString" />
				</app:uomName>
				<app:conversionTolSOstandardUnit>
					<xsl:value-of
						select="smXML:distance/smXML:Distance/smXML:uom/smXML:UomLength/smXML:conversionTolSOstandardUnit/smXML:Real" />
				</app:conversionTolSOstandardUnit>
			</xsl:if>
		</app:MD_Resolution>
	</xsl:template>
	
	<!-- applicationSchemaInformation ???  -->
	<xsl:template match="smXML:MD_ApplicationSchemaInformation">
		<app:applicationSchemaInformation>
			<app:MD_ApplicationSchemaInformation>
				<xsl:if test="boolean( smXML:name/smXML:CI_Citation )">
					<app:citation>
						<xsl:apply-templates select="smXML:name/smXML:CI_Citation">
							<xsl:with-param name="context">
								ApplicationSchemaInformation
							</xsl:with-param>
						</xsl:apply-templates>
					</app:citation>
				</xsl:if>
				<xsl:variable name="schemaLanguage" select="smXML:schemaLanguage/smXML:CharacterString" />
				<xsl:if test="boolean( $schemaLanguage != '' )">
					<app:schemaLanguage>
						<xsl:value-of select="$schemaLanguage" />
					</app:schemaLanguage>
				</xsl:if>
				<xsl:variable name="constraintLanguage" select="smXML:constraintLanguage/smXML:CharacterString" />
				<xsl:if test="boolean( $constraintLanguage != '' )">
					<app:constraintLanguage>
						<xsl:value-of select="$constraintLanguage" />
					</app:constraintLanguage>
				</xsl:if>
				<xsl:variable name="schemaAscii" select="smXML:schemaAscii/smXML:CharacterString" />
				<xsl:if test="boolean( $schemaAscii != '' )">
					<app:schemaAscii>
						<xsl:value-of select="$schemaAscii" />
					</app:schemaAscii>
				</xsl:if>
				<xsl:variable name="graphicsFile64b" select="smXML:graphicsFile/smXML:b64Binary" />
				<xsl:if test="boolean( $graphicsFile64b != '')">
					<app:graphicsFile64b>
						<xsl:value-of select="$graphicsFile64b" />
					</app:graphicsFile64b>
				</xsl:if>
				<xsl:variable name="graphicsFileHex" select="smXML:graphicsFile/smXML:hexBinary" />
				<xsl:if test="boolean( $graphicsFileHex != '')">
					<app:graphicsFileHex>
						<xsl:value-of select="$graphicsFileHex" />
					</app:graphicsFileHex>
				</xsl:if>
				<xsl:variable name="softwareDevelFile64b" select="smXML:softwareDevelopmentFile/smXML:b64Binary" />
				<xsl:if test="boolean( $softwareDevelFile64b != '' )">
					<app:softwareDevelFile64b>
						<xsl:value-of select="$softwareDevelFile64b" />
					</app:softwareDevelFile64b>
				</xsl:if>
				<xsl:variable name="softwareDevelFileHex" select="smXML:softwareDevelopmentFile/smXML:hexBinary" />
				<xsl:if test="boolean( $softwareDevelFileHex != '' )">
					<app:softwareDevelFileHex>
						<xsl:value-of select="$softwareDevelFileHex" />
					</app:softwareDevelFileHex>
				</xsl:if>	
				<xsl:variable name="softwareDevelFileFormat" select="smXML:softwareDevelopmentFileFormat/smXML:CharacterString" />
				<xsl:if test="boolean( $softwareDevelFileFormat != '' )">
					<app:softwareDevelFileFormat>
						<xsl:value-of select="$softwareDevelFileFormat" />
					</app:softwareDevelFileFormat>
				</xsl:if>				
			</app:MD_ApplicationSchemaInformation>
		</app:applicationSchemaInformation>
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
				<xsl:attribute name="escape">
					<xsl:value-of select="./@escape" />
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
		<xsl:value-of select="mapping:mapPropertyValue( ., 'dataset' )" />
	</xsl:template>
	
	
</xsl:stylesheet>
