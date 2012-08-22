<?xml version="1.0" encoding="UTF-8"?>
<!--***************************************************************************************************
*** WMS GetCapabilities 1.1.1 response to CSW 2.0.2 ISO 19115/19119/19139 metadata
*** For <WMT_MS_Capabilities> 1.1.1 but not yet for <WMS_Capabilities> 1.3.0
***
*** Based on an XSLT script from:
***                   Wolfgang Grunberg 
***                   Arizona Geological Survey
***                   08/18/2009
***
*** Modified by: Andreas Poth
***                   lat/lon GmbH
***                   http://www.lat-lon.de
***                   2010-11-10
***
*** Metadata is schema valid with:
***   ISO 19139/19115 Metadata - http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20060504/gmd/metadataEntity.xsd 
***   ISO 19139/19119 Service - http://www.isotc211.org/2005/srv (http://schemas.opengis.net/iso/19139/20060504/srv/serviceMetadata.xsd
***   INSPIRE metadata validator: http://www.inspire-geoportal.eu/index.cfm/pageid/48
*** 
*** NOTE: Version 1.3.0 WMS GetCapabilities requests will not be transformed correctly.
**************************************************************************************************-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wms="http://www.opengis.net/wms" xmlns:sld="http://www.opengis.net/sld" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xlink="http://www.w3.org/1999/xlink" exclude-result-prefixes="xsl wms sld">
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	<!--*** PARAMETERS  ***-->
	<!-- Set required time stamp. XSLT 1 does not support current-dateTime(). -->
	<xsl:param name="DATETIME" select="'1950-01-01T01:01:01'"/>
	<!-- parameters for responsible party -->
	<xsl:param name="INDIVIDUALNAME">Andreas Poth</xsl:param>
	<xsl:param name="ORGANISATIONNAME">lat/lon</xsl:param>
	<xsl:param name="DELIVERYPOINT">Aennchenstraße 19</xsl:param>
	<xsl:param name="CITY">Bonn</xsl:param>
	<xsl:param name="POSTALCODE">53177</xsl:param>
	<xsl:param name="EMAIL">info@lat-lon.de</xsl:param>
	<xsl:param name="ONLINERESOURCE">http://www.lat-lon.de</xsl:param>
	<!--*** VARIABLES ***-->
	<!-- Default abstract for required CSW element -->
	<xsl:variable name="DEFAULT_ABSTRACT">WMS Service</xsl:variable>
	<!-- Empty String for testing purpose -->
	<xsl:variable name="EMPTY_STRING">
		<xsl:value-of select="''"/>
	</xsl:variable>
	<!--*** TEMPLATES ***-->
	<!-- NOTE: for some reason XMLSpy's and MSXML's  XSLT parsers choke on the xmlns="http://www.opengis.net/wms"  attribute in <WMS_Capabilities> and both can't match the following template node(s). XMLSpy and MSXML will use their own default template (dumps all values) if they can't match a node to a template. -->
	<!-- <xsl:template match="WMT_MS_Capabilities | WMS_Capabilities"> Dosen't work because of the above reason. -->
	<!--<xsl:template match="/"> Returns empty elements. -->
	<!--*** TEMPLATE: WMT_MS_Capabilities (WMS GetCapabilities 1.1.1) ***-->
	<xsl:template match="WMT_MS_Capabilities | wms:WMS_Capabilities">
		<!-- CSW Insert transaction  -->
		<!-- Metadata -->
		<gmd:MD_Metadata xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.isotc211.org/2005/gmd D:\java\projekte\iso19115\CSW202_APISO_100_FINAL\csw202_apiso100\iso\19139\20060504\gmd\applicationSchema.xsd http://www.isotc211.org/2005/srv D:\java\projekte\iso19115\CSW202_APISO_100_FINAL\csw202_apiso100\iso\19139\20060504\srv\srv.xsd">
			<!-- Sadly, the CSW and GMD schema have duplicate GML refernces which lead to schema validation errors.
					xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
					xsi:schemaLocation="http://www.isotc211.org/2005/gmd http://schemas.opengis.net/iso/19139/20060504/gmd/metadataEntity.xsd 
					http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/serviceMetadata.xsd"> -->
			<gmd:fileIdentifier>
				<gco:CharacterString>
					<xsl:value-of select="./Service/Title"/>
					<xsl:value-of select="./wms:Service/wms:Title"/>
				</gco:CharacterString>
			</gmd:fileIdentifier>
			<!-- ISO 639-2 Bibliographic Code -->
			<gmd:language>
				<gmd:LanguageCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#LanguageCode" codeListValue="eng">eng</gmd:LanguageCode>
			</gmd:language>
			<!-- MD_CharacterSetCode: utf8, 8859part1, ucs2, ... -->
			<gmd:characterSet>
				<gmd:MD_CharacterSetCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_CharacterSetCode" codeListValue="utf8"/>
			</gmd:characterSet>
			<!-- Define if this record is a dataset (default), service, feature, software, etc. -->
			<gmd:hierarchyLevel>
				<gmd:MD_ScopeCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_ScopeCode" codeListValue="service"/>
			</gmd:hierarchyLevel>
			<!-- name of the hierarchy levels for which the metadata is provided - required in deegree?-->
			<gmd:hierarchyLevelName>
				<gco:CharacterString>service</gco:CharacterString>
			</gmd:hierarchyLevelName>
			<!-- Metadata Point of Contact - REQUIRED -->
			<gmd:contact>
				<xsl:choose>
					<xsl:when test="Service/ContactInformation or wms:Service/wms:ContactInformation">
						<xsl:call-template name="wms_service_respParty"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="defaultRespParty"/>
					</xsl:otherwise>
				</xsl:choose>
			</gmd:contact>
			<!-- Metadata Date Stamp -->
			<gmd:dateStamp>
				<gco:DateTime>
					<xsl:value-of select="$DATETIME"/>
				</gco:DateTime>
			</gmd:dateStamp>
			<!-- Metadata Standard -->
			<gmd:metadataStandardName>
				<gco:CharacterString>ISO19119</gco:CharacterString>
			</gmd:metadataStandardName>
			<gmd:metadataStandardVersion>
				<gco:CharacterString>2005/PDAM 1</gco:CharacterString>
			</gmd:metadataStandardVersion>
			<!-- Basic information required to uniquely identify a resource or resources -->
			<xsl:apply-templates select="Capability/Layer/SRS | wms:Capability/wms:Layer/wms:CRS"/>
			<xsl:call-template name="identification"/>
		</gmd:MD_Metadata>
	</xsl:template>
	<!--*** Shared Templates ***-->
	<!-- TEMPLATE: Default Responsible Party -->
	<xsl:template name="defaultRespParty">
		<gmd:CI_ResponsibleParty>
			<gmd:individualName>
				<gco:CharacterString>
					<xsl:value-of select="$INDIVIDUALNAME"/>
				</gco:CharacterString>
			</gmd:individualName>
			<gmd:organisationName>
				<gco:CharacterString>
					<xsl:value-of select="$ORGANISATIONNAME"/>
				</gco:CharacterString>
			</gmd:organisationName>
			<gmd:contactInfo>
				<gmd:CI_Contact>
					<gmd:address>
						<gmd:CI_Address>
							<gmd:deliveryPoint>
								<gco:CharacterString>
									<xsl:value-of select="$DELIVERYPOINT"/>
								</gco:CharacterString>
							</gmd:deliveryPoint>
							<gmd:city>
								<gco:CharacterString>
									<xsl:value-of select="$CITY"/>
								</gco:CharacterString>
							</gmd:city>
							<gmd:postalCode>
								<gco:CharacterString>
									<xsl:value-of select="$POSTALCODE"/>
								</gco:CharacterString>
							</gmd:postalCode>
							<gmd:electronicMailAddress>
								<gco:CharacterString>
									<xsl:value-of select="$EMAIL"/>
								</gco:CharacterString>
							</gmd:electronicMailAddress>
						</gmd:CI_Address>
					</gmd:address>
					<gmd:onlineResource>
						<gmd:CI_OnlineResource>
							<gmd:linkage>
								<gmd:URL>
									<xsl:value-of select="$ONLINERESOURCE"/>
								</gmd:URL>
							</gmd:linkage>
						</gmd:CI_OnlineResource>
					</gmd:onlineResource>
				</gmd:CI_Contact>
			</gmd:contactInfo>
			<gmd:role>
				<!-- because of inspire it must be pointOfContact -->
				<gmd:CI_RoleCode codeList="CI_RoleCode" codeListValue="pointOfContact"/>
			</gmd:role>
		</gmd:CI_ResponsibleParty>
	</xsl:template>
	<!-- TEMPLATE: Responsible Party derived from service section of WMS capabilities -->
	<xsl:template name="wms_service_respParty">
		<gmd:CI_ResponsibleParty>
			<!-- Individual Name -->
			<xsl:variable name="IN">
				<xsl:value-of select="concat( ./Service/ContactInformation/ContactPersonPrimary/ContactPerson, ./wms:Service/wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactPerson )"/>
			</xsl:variable>
			<gmd:individualName>
				<gco:CharacterString>
					<xsl:choose>
						<xsl:when test="string-length( $IN ) &gt; 0">
							<xsl:value-of select="$IN"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$INDIVIDUALNAME"/>
						</xsl:otherwise>
					</xsl:choose>
				</gco:CharacterString>
			</gmd:individualName>
			<!-- Organisation Name - REQUIRED by deegree and INSPIRE -->
			<xsl:variable name="CO">
				<xsl:value-of select="concat(./Service/ContactInformation/ContactPersonPrimary/ContactOrganization, ./wms:Service/wms:ContactInformation/wms:ContactPersonPrimary/wms:ContactOrganization )"/>
			</xsl:variable>
			<gmd:organisationName>
				<gco:CharacterString>
					<!--<xsl:value-of select="./Service/ContactInformation/ContactPersonPrimary/ContactOrganization"/>-->
					<xsl:choose>
						<xsl:when test="string-length( $CO ) &gt; 0">
							<xsl:value-of select="$CO"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$ORGANISATIONNAME"/>
						</xsl:otherwise>
					</xsl:choose>
				</gco:CharacterString>
			</gmd:organisationName>
			<gmd:contactInfo>
				<gmd:CI_Contact>
					<xsl:variable name="VO">
						<xsl:value-of select="concat( ./Service/ContactInformation/ContactVoiceTelephone, ./wms:Service/wms:ContactInformation/wms:ContactVoiceTelephone )"/>
					</xsl:variable>
					<xsl:variable name="FAX">
						<xsl:value-of select="concat(./Service/ContactInformation/ContactFacsimileTelephone, ./wms:Service/wms:ContactInformation/wms:ContactFacsimileTelephone )"/>
					</xsl:variable>
					<!-- Phone/Fax -->
					<xsl:if test="string-length( $VO ) &gt; 0 or string-length( $FAX ) &gt; 0">
						<gmd:phone>
							<gmd:CI_Telephone>
								<xsl:if test="string-length( $VO ) &gt; 0">
									<gmd:voice>
										<gco:CharacterString>
											<xsl:value-of select="$VO"/>
										</gco:CharacterString>
									</gmd:voice>
								</xsl:if>
								<xsl:if test="string-length( $FAX ) &gt; 0">
									<gmd:facsimile>
										<gco:CharacterString>
											<xsl:value-of select="$FAX"/>
										</gco:CharacterString>
									</gmd:facsimile>
								</xsl:if>
							</gmd:CI_Telephone>
						</gmd:phone>
					</xsl:if>
					<!-- Address -->
					<gmd:address>
						<gmd:CI_Address>
							<gmd:deliveryPoint>
								<xsl:variable name="DP">
									<xsl:value-of select="concat( ./Service/ContactInformation/ContactAddress/Address, ./wms:Service/wms:ContactInformation/wms:ContactAddress/wms:Address)"/>
								</xsl:variable>
								<gco:CharacterString>
									<xsl:choose>
										<xsl:when test="string-length( $DP ) &gt; 0">
											<xsl:value-of select="$DP"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$DELIVERYPOINT"/>
										</xsl:otherwise>
									</xsl:choose>
								</gco:CharacterString>
							</gmd:deliveryPoint>
							<gmd:city>
								<xsl:variable name="CY">
									<xsl:value-of select="concat( ./Service/ContactInformation/ContactAddress/City, ./wms:Service/wms:ContactInformation/wms:ContactAddress/wms:City)"/>
								</xsl:variable>
								<gco:CharacterString>
									<xsl:choose>
										<xsl:when test="string-length( $CY ) &gt; 0">
											<xsl:value-of select="$CY"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$CITY"/>
										</xsl:otherwise>
									</xsl:choose>
								</gco:CharacterString>
							</gmd:city>
							<xsl:variable name="AA">
								<xsl:value-of select="concat( ./Service/ContactInformation/ContactAddress/StateOrProvince, ./wms:Service/wms:ContactInformation/wms:ContactAddress/wms:StateOrProvince )"/>
							</xsl:variable>
							<xsl:if test="string-length( $AA ) &gt; 0">
								<gmd:administrativeArea>
									<gco:CharacterString>
										<xsl:value-of select="$AA"/>
									</gco:CharacterString>
								</gmd:administrativeArea>
							</xsl:if>
							<gmd:postalCode>
								<xsl:variable name="PC">
									<xsl:value-of select="concat( ./Service/ContactInformation/ContactAddress/PostCode, ./wms:Service/wms:ContactInformation/wms:ContactAddress/wms:PostCode )"/>
								</xsl:variable>
								<gco:CharacterString>
									<xsl:choose>
										<xsl:when test="string-length( $PC ) &gt; 0">
											<xsl:value-of select="$PC"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$POSTALCODE"/>
										</xsl:otherwise>
									</xsl:choose>
								</gco:CharacterString>
							</gmd:postalCode>
							<xsl:variable name="CTR">
								<xsl:value-of select="concat( ./Service/ContactInformation/ContactAddress/Country, ./wms:Service/wms:ContactInformation/wms:ContactAddress/wms:Country )"/>
							</xsl:variable>
							<xsl:if test="string-length( $CTR ) &gt; 0">
								<gmd:country>
									<gco:CharacterString>
										<xsl:value-of select="$CTR"/>
									</gco:CharacterString>
								</gmd:country>
							</xsl:if>
							<!-- e-mail - REQUIRED by INSPIRE  -->
							<gmd:electronicMailAddress>
								<xsl:variable name="EMA">
									<xsl:value-of select="concat(./Service/ContactInformation/ContactElectronicMailAddress, ./wms:Service/wms:ContactInformation/wms:ContactElectronicMailAddress )"/>
								</xsl:variable>
								<gco:CharacterString>
									<xsl:choose>
										<xsl:when test="string-length( $EMA ) &gt; 0">
											<xsl:value-of select="$EMA"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$EMAIL"/>
										</xsl:otherwise>
									</xsl:choose>
								</gco:CharacterString>
							</gmd:electronicMailAddress>
						</gmd:CI_Address>
					</gmd:address>
					<!-- Online Resource -->
					<gmd:onlineResource>
						<xsl:variable name="OL">
							<xsl:value-of select="concat( ./Service/OnlineResource/@xlink:href, ./wms:Service/wms:OnlineResource/@xlink:href )"/>
						</xsl:variable>
						<gmd:CI_OnlineResource>
							<gmd:linkage>
								<gmd:URL>
									<xsl:choose>
										<xsl:when test="string-length( $OL ) &gt; 0">
											<xsl:value-of select="$OL"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$ONLINERESOURCE"/>
										</xsl:otherwise>
									</xsl:choose>
								</gmd:URL>
							</gmd:linkage>
							<gmd:protocol>
								<gco:CharacterString>HTTP</gco:CharacterString>
							</gmd:protocol>
						</gmd:CI_OnlineResource>
					</gmd:onlineResource>
				</gmd:CI_Contact>
			</gmd:contactInfo>
			<gmd:role>
				<gmd:CI_RoleCode codeList="CI_RoleCode" codeListValue="pointOfContact"/>
			</gmd:role>
		</gmd:CI_ResponsibleParty>
	</xsl:template>
	<!-- TEMPLATE: Basic information required to uniquely identify a resource or resources -->
	<xsl:template name="identification">
		<gmd:identificationInfo>
			<srv:SV_ServiceIdentification>
				<gmd:citation>
					<gmd:CI_Citation>
						<!-- Dataset Title - REQUIRED -->
						<gmd:title>
							<gco:CharacterString>
								<xsl:value-of select="Service/Title | wms:Service/wms:Title"/>
							</gco:CharacterString>
						</gmd:title>
						<!-- Dataset Publication Date - REQUIRED by ISO -->
						<gmd:date>
							<gmd:CI_Date>
								<gmd:date>
									<gco:DateTime>
										<xsl:value-of select="$DATETIME"/>
									</gco:DateTime>
								</gmd:date>
								<gmd:dateType>
									<gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
								</gmd:dateType>
							</gmd:CI_Date>
						</gmd:date>
					</gmd:CI_Citation>
				</gmd:citation>
				<!-- Abstract - REQUIRED -->
				<gmd:abstract>
					<xsl:variable name="ABS">
						<xsl:value-of select="concat( Service/Abstract, wms:Service/wms:Abstract )"/>
					</xsl:variable>
					<gco:CharacterString>
						<xsl:choose>
							<xsl:when test="string-length( $ABS ) &gt; 0">
								<xsl:value-of select="$ABS"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$DEFAULT_ABSTRACT"/>
							</xsl:otherwise>
						</xsl:choose>
					</gco:CharacterString>
				</gmd:abstract>
				<!-- required by inspire -->
				<pointOfContact xmlns="http://www.isotc211.org/2005/gmd">
					<xsl:choose>
						<xsl:when test="boolean(/WMT_MS_Capabilities/Service/ContactInformation) or boolean(/wms:WMS_Capabilities/wms:Service/wms:ContactInformation)">
							<xsl:call-template name="wms_service_respParty"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="defaultRespParty"/>
						</xsl:otherwise>
					</xsl:choose>
				</pointOfContact>
				<!-- Keywords - test if Keywordlist exists AND if Keyword string is not empty -->
				<gmd:descriptiveKeywords>
					<gmd:MD_Keywords>
						<gmd:keyword>
							<gco:CharacterString>humanGeographicViewer</gco:CharacterString>
						</gmd:keyword>
					</gmd:MD_Keywords>
				</gmd:descriptiveKeywords>
				<!-- If there are keywords and if they are not empty -->
				<xsl:if test="boolean( Service/KeywordList/Keyword ) or boolean( wms:Service/wms:KeywordList/wms:Keyword )">
					<gmd:descriptiveKeywords>
						<gmd:MD_Keywords>
							<xsl:for-each select="Service/KeywordList/Keyword">
								<gmd:keyword>
									<gco:CharacterString>
										<xsl:value-of select="."/>
									</gco:CharacterString>
								</gmd:keyword>
							</xsl:for-each>
							<xsl:for-each select="wms:Service/wms:KeywordList/wms:Keyword">
								<gmd:keyword>
									<gco:CharacterString>
										<xsl:value-of select="."/>
									</gco:CharacterString>
								</gmd:keyword>
							</xsl:for-each>
							<gmd:type>
								<gmd:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="theme"/>
							</gmd:type>
						</gmd:MD_Keywords>
					</gmd:descriptiveKeywords>
				</xsl:if>
				<!-- required by inspire -->
				<gmd:resourceConstraints>
					<gmd:MD_Constraints>
						<gmd:useLimitation>
							<xsl:choose>
								<xsl:when test="boolean( Service/Fees ) or boolean( wms:Service/wms:Fees )">
									<gco:CharacterString>
										<xsl:value-of select="Service/Fees"/>
										<xsl:value-of select="wms:Service/wms:Fees"/>
									</gco:CharacterString>
								</xsl:when>
								<xsl:otherwise>
									<gco:CharacterString>none</gco:CharacterString>
								</xsl:otherwise>
							</xsl:choose>
						</gmd:useLimitation>
					</gmd:MD_Constraints>
				</gmd:resourceConstraints>
				<!-- WMS Service Information; according to inspire value must be 'view' -->
				<srv:serviceType>
					<gco:LocalName>view</gco:LocalName>
				</srv:serviceType>
				<!-- Can't get WMT_MS_Capabilities@version property directly  -->
				<srv:serviceTypeVersion>
					<xsl:choose>
						<xsl:when test="boolean(/*/@version)">
							<gco:CharacterString>
								<xsl:value-of select="/*/@version"/>
							</gco:CharacterString>
						</xsl:when>
						<xsl:otherwise>
							<gco:CharacterString>1.1.1</gco:CharacterString>
						</xsl:otherwise>
					</xsl:choose>
				</srv:serviceTypeVersion>
				<!-- Access Properties -->
				<!-- information about the availability of the service, including: fees, planned available date and time, ordering instructions, turnaround -->
				<xsl:if test="boolean( Service/Fees ) or boolean( wms:Service/wms:Fees )">
					<srv:accessProperties>
						<gmd:MD_StandardOrderProcess>
							<gmd:fees>
								<gco:CharacterString>
									<xsl:value-of select="Service/Fees"/>
									<xsl:value-of select="wms:Service/wms:Fees"/>
								</gco:CharacterString>
							</gmd:fees>
						</gmd:MD_StandardOrderProcess>
					</srv:accessProperties>
				</xsl:if>
				<!-- legal and security constraints on accessing the service and distributing data generated by the service -->
				<xsl:if test="boolean( Service/AccessConstraints ) or boolean( wms:Service/wms:AccessConstraints )">
					<srv:restrictions>
						<gmd:MD_LegalConstraints>
							<gmd:accessConstraints>
								<gmd:MD_RestrictionCode codeList="MD_RestrictionCode" codeListValue="otherRestrictions"/>
							</gmd:accessConstraints>
							<gmd:otherConstraints>
								<gco:CharacterString>
									<xsl:value-of select="Service/AccessConstraints"/>
									<xsl:value-of select="wms:Service/wms:AccessConstraints"/>
								</gco:CharacterString>
							</gmd:otherConstraints>
						</gmd:MD_LegalConstraints>
					</srv:restrictions>
				</xsl:if>
				<!-- Service Extent  -->
				<xsl:apply-templates select="Capability/Layer/LatLonBoundingBox"/>
				<xsl:apply-templates select="wms:Capability/wms:Layer/wms:EX_GeographicBoundingBox"/>
				<!-- Coupled Resources - now mandatory for deegree WMS/WFS service metadata records -->
				<!-- "further description of the data coupling in the case of tightly coupled services" -->
				<xsl:call-template name="addCoupledResource"/>
				<!-- Type of coupling between service and associated data (if exists) -->
				<srv:couplingType>
					<xsl:choose>
						<xsl:when test="Capability/UserDefinedSymbolization/@UserLayer = 1">
							<srv:SV_CouplingType codeList="SV_CouplingType" codeListValue="mixed"/>
						</xsl:when>
						<xsl:otherwise>
							<srv:SV_CouplingType codeList="SV_CouplingType" codeListValue="tight"/>
						</xsl:otherwise>
					</xsl:choose>
				</srv:couplingType>
				<xsl:call-template name="addGetCapabilitiesOpMetadata"/>
				<xsl:call-template name="addGetMapOpMetadata"/>
				<xsl:if test="boolean(Capability/Request/GetFeatureInfo) or boolean( wms:Capability/wms:Request/wms:GetFeatureInfo )">
					<xsl:call-template name="addGetFeatureInfoOpMetadata"/>
				</xsl:if>
				<xsl:if test="boolean(Capability/Request/DescribeLayer) or boolean( wms:Capability/wms:Request/sld:DescribeLayer)">
					<xsl:call-template name="addDescribeLayerOpMetadata"/>
				</xsl:if>
				<xsl:if test="boolean(Capability/Request/GetLegendGraphic) or boolean( wms:Capability/wms:Request/sld:GetLegendGraphic)">
					<xsl:call-template name="addGetLegendGraphicOpMetadata"/>
				</xsl:if>
				<xsl:apply-templates select="Capability/Layer | wms:Capability/wms:Layer"/>
			</srv:SV_ServiceIdentification>
		</gmd:identificationInfo>
	</xsl:template>
	<!-- TEMPLATE: GetCapability -->
	<xsl:template name="addGetCapabilitiesOpMetadata">
		<!--
			append GetCapabilities description
		-->
		<srv:containsOperations>
			<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>GetCapabilities</gco:CharacterString>
				</srv:operationName>
				<xsl:apply-templates select="Capability/Request/GetCapabilities/DCPType/HTTP | wms:Capability/wms:Request/wms:GetCapabilities/wms:DCPType/wms:HTTP"/>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Service</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Version</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Request</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: GetCapabilities</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<xsl:choose>
					<xsl:when test="boolean(Capability/Request/GetCapabilities/DCPType/HTTP/*) or boolean(wms:Capability/wms:Request/wms:GetCapabilities/wms:DCPType/wms:HTTP/*)">
						<xsl:apply-templates select="Capability/Request/GetCapabilities/DCPType/HTTP/Get/OnlineResource | wms:Capability/wms:Request/wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="Capability/Request/GetCapabilities/DCPType/HTTP/Post/OnlineResource | wms:Capability/wms:Request/wms:GetCapabilities/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource"/>
					</xsl:otherwise>
				</xsl:choose>
			</srv:SV_OperationMetadata>
		</srv:containsOperations>
	</xsl:template>
	<!-- TEMPLATE: GetMap -->
	<xsl:template name="addGetMapOpMetadata">
		<!--
			append GetMap description
		-->
		<srv:containsOperations>
			<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>GetMap</gco:CharacterString>
				</srv:operationName>
				<xsl:apply-templates select="Capability/Request/GetCapabilities/DCPType/HTTP | wms:Capability/wms:Request/wms:GetMap/wms:DCPType/wms:HTTP"/>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Version</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Request</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: GetMap</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Layers</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Styles</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>srs</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>BBOX</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>width</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>height</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Format</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>TRANSPARENT</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>boolean</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>boolean</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>BGCOLOR</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>EXCEPTIONS</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>TIME</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>ELEVATION</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<xsl:choose>
					<xsl:when test="boolean(Capability/Request/GetMap/DCPType/HTTP/*) or boolean(wms:Capability/wms:Request/wms:GetMap/wms:DCPType/wms:HTTP/*)">
						<xsl:apply-templates select="Capability/Request/GetMap/DCPType/HTTP/Get/OnlineResource | wms:Capability/wms:Request/wms:GetMap/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="Capability/Request/GetMap/DCPType/HTTP/Post/OnlineResource | wms:Capability/wms:Request/wms:GetMap/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource"/>
					</xsl:otherwise>
				</xsl:choose>
			</srv:SV_OperationMetadata>
		</srv:containsOperations>
	</xsl:template>
	<!-- TEMPLATE: GetFeatureInfo -->
	<xsl:template name="addGetFeatureInfoOpMetadata">
		<!--
			append GetFeatureInfo description
		-->
		<srv:containsOperations>
			<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>GetFeatureInfo</gco:CharacterString>
				</srv:operationName>
				<xsl:apply-templates select="Capability/Request/GetCapabilities/DCPType/HTTP | wms:Capability/wms:Request/wms:GetFeatureInfo/wms:DCPType/wms:HTTP"/>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Version</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Request</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: GetFeatureInfo</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Layers</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Styles</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>srs</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>BBOX</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>width</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>height</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Format</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>TRANSPARENT</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>boolean</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>boolean</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>BGCOLOR</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>EXCEPTIONS</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>TIME</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>ELEVATION</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>QUERY_LAYERS</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>INFO_FORMAT</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>FEATURE_COUNT</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>x</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>y</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<xsl:choose>
					<xsl:when test="boolean(Capability/Request/GetFeatureInfo/DCPType/HTTP/*) or boolean(wms:Capability/wms:Request/wms:GetFeatureInfo/wms:DCPType/wms:HTTP/*)">
						<xsl:apply-templates select="Capability/Request/GetFeatureInfo/DCPType/HTTP/Get/OnlineResource | wms:Capability/wms:Request/wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="Capability/Request/GetFeatureInfo/DCPType/HTTP/Post/OnlineResource | wms:Capability/wms:Request/wms:GetFeatureInfo/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource"/>
					</xsl:otherwise>
				</xsl:choose>
			</srv:SV_OperationMetadata>
		</srv:containsOperations>
	</xsl:template>
	<!-- TEMPLATE: DescribeLayer -->
	<xsl:template name="addDescribeLayerOpMetadata">
		<!--
			append DescribeLayer description
		-->
		<srv:containsOperations>
			<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>DescribeLayer</gco:CharacterString>
				</srv:operationName>
				<xsl:apply-templates select="Capability/Request/GetCapabilities/DCPType/HTTP | wms:Capability/wms:Request/sld:DescribeLayer/wms:DCPType/wms:HTTP"/>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Version</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Request</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Layers</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<xsl:choose>
					<xsl:when test="boolean(Capability/Request/DescribeLayer/DCPType/HTTP/*) or boolean(wms:Capability/wms:Request/sld:DescribeLayer/wms:DCPType/wms:HTTP/*)">
						<xsl:apply-templates select="Capability/Request/DescribeLayer/DCPType/HTTP/Get/OnlineResource | wms:Capability/wms:Request/sld:DescribeLayer/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="Capability/Request/DescribeLayer/DCPType/HTTP/Post/OnlineResource | wms:Capability/wms:Request/sld:DescribeLayer/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource"/>
					</xsl:otherwise>
				</xsl:choose>
				
			</srv:SV_OperationMetadata>
		</srv:containsOperations>
	</xsl:template>
	<!-- TEMPLATE: GetLegendGraphic -->
	<xsl:template name="addGetLegendGraphicOpMetadata">
		<!--
			append GetLegendGraphic description
		-->
		<srv:containsOperations>
			<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>GetLegendGraphic</gco:CharacterString>
				</srv:operationName>
				<xsl:apply-templates select="Capability/Request/GetCapabilities/DCPType/HTTP | wms:Capability/wms:Request/sld:GetLegendGraphic/wms:DCPType/wms:HTTP"/>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Version</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>-</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Request</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: GetLegendGraphic</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Layer</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>Style</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>FEATURETYPE</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>RULE</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>SCALE</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>scale</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>SLD</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>SLD_BODY</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>FORMAT</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>mandatory</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>WIDTH</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>HEIGHT</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>integer</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>integer</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<srv:parameters>
					<srv:SV_Parameter>
						<srv:name>
							<gco:aName>
								<gco:CharacterString>EXCEPTIONS</gco:CharacterString>
							</gco:aName>
							<gco:attributeType>
								<gco:TypeName>
									<gco:aName>
										<gco:CharacterString>String</gco:CharacterString>
									</gco:aName>
								</gco:TypeName>
							</gco:attributeType>
						</srv:name>
						<srv:direction>
							<srv:SV_ParameterDirection>in</srv:SV_ParameterDirection>
						</srv:direction>
						<srv:description>
							<gco:CharacterString>fixed value: DescribeLayer</gco:CharacterString>
						</srv:description>
						<srv:optionality>
							<gco:CharacterString>optional</gco:CharacterString>
						</srv:optionality>
						<srv:repeatability>
							<gco:Boolean>false</gco:Boolean>
						</srv:repeatability>
						<srv:valueType>
							<gco:TypeName>
								<gco:aName>
									<gco:CharacterString>String</gco:CharacterString>
								</gco:aName>
							</gco:TypeName>
						</srv:valueType>
					</srv:SV_Parameter>
				</srv:parameters>
				<xsl:choose>
					<xsl:when test="boolean(Capability/Request/GetLegendGraphic/DCPType/HTTP/*) or boolean(wms:Capability/wms:Request/sld:GetLegendGraphic/wms:DCPType/wms:HTTP/*)">
						<xsl:apply-templates select="Capability/Request/GetLegendGraphic/DCPType/HTTP/Get/OnlineResource | wms:Capability/wms:Request/sld:GetLegendGraphic/wms:DCPType/wms:HTTP/wms:Get/wms:OnlineResource"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="Capability/Request/GetLegendGraphic/DCPType/HTTP/Post/OnlineResource | wms:Capability/wms:Request/sld:GetLegendGraphic/wms:DCPType/wms:HTTP/wms:Post/wms:OnlineResource"/>
					</xsl:otherwise>
				</xsl:choose>
			
			</srv:SV_OperationMetadata>
		</srv:containsOperations>
	</xsl:template>
	<!-- TEMPLATE: HTTP Get/Post -->
	<xsl:template match="HTTP | wms:HTTP">
		<xsl:if test="boolean( Get ) or boolean( wms:Get )">
			<srv:DCP>
				<srv:DCPList codeList="SV_DCPTypeCode" codeListValue="HTTPGet"/>
			</srv:DCP>
		</xsl:if>
		<xsl:if test="boolean( Post ) or boolean( wms:Post )">
			<srv:DCP>
				<srv:DCPList codeList="SV_DCPTypeCode" codeListValue="HTTPPost"/>
			</srv:DCP>
		</xsl:if>
	</xsl:template>
	<!-- TEMPLATE: Online Resources -->
	<xsl:template match="OnlineResource | wms:OnlineResource">
		<srv:connectPoint>
			<gmd:CI_OnlineResource>
				<gmd:linkage>
					<gmd:URL>
						<xsl:value-of select="./@xlink:href"/>
					</gmd:URL>
				</gmd:linkage>
			</gmd:CI_OnlineResource>
		</srv:connectPoint>
	</xsl:template>
	<!-- TEMPLATE: operatesOn -->
	<xsl:template match="Layer | wms:Layer">
		<xsl:if test="boolean( Name ) or boolean( wms:Name )">
			<srv:operatesOn>
				<xsl:attribute name="xlink:href" namespace="http://www.w3.org/1999/xlink"><xsl:value-of select="concat( '#', ./Name, ./wms:Name )"/></xsl:attribute>
			</srv:operatesOn>
		</xsl:if>
		<xsl:apply-templates select="Layer | wms:Layer"/>
	</xsl:template>
	<!-- TEMPLATE: Bounding Box  -->
	<xsl:template match="LatLonBoundingBox">
		<srv:extent>
			<gmd:EX_Extent>
				<gmd:geographicElement>
					<gmd:EX_GeographicBoundingBox>
						<gmd:westBoundLongitude>
							<gco:Decimal>
								<xsl:value-of select="./@minx"/>
							</gco:Decimal>
						</gmd:westBoundLongitude>
						<gmd:eastBoundLongitude>
							<gco:Decimal>
								<xsl:value-of select="./@maxx"/>
							</gco:Decimal>
						</gmd:eastBoundLongitude>
						<gmd:southBoundLatitude>
							<gco:Decimal>
								<xsl:value-of select="./@miny"/>
							</gco:Decimal>
						</gmd:southBoundLatitude>
						<gmd:northBoundLatitude>
							<gco:Decimal>
								<xsl:value-of select="./@maxy"/>
							</gco:Decimal>
						</gmd:northBoundLatitude>
					</gmd:EX_GeographicBoundingBox>
				</gmd:geographicElement>
			</gmd:EX_Extent>
		</srv:extent>
	</xsl:template>
	<xsl:template match="wms:EX_GeographicBoundingBox">
		<srv:extent>
			<gmd:EX_Extent>
				<gmd:geographicElement>
					<gmd:EX_GeographicBoundingBox>
						<gmd:westBoundLongitude>
							<gco:Decimal>
								<xsl:value-of select="wms:westBoundLongitude"/>
							</gco:Decimal>
						</gmd:westBoundLongitude>
						<gmd:eastBoundLongitude>
							<gco:Decimal>
								<xsl:value-of select="wms:eastBoundLongitude"/>
							</gco:Decimal>
						</gmd:eastBoundLongitude>
						<gmd:southBoundLatitude>
							<gco:Decimal>
								<xsl:value-of select="wms:southBoundLatitude"/>
							</gco:Decimal>
						</gmd:southBoundLatitude>
						<gmd:northBoundLatitude>
							<gco:Decimal>
								<xsl:value-of select="wms:northBoundLatitude"/>
							</gco:Decimal>
						</gmd:northBoundLatitude>
					</gmd:EX_GeographicBoundingBox>
				</gmd:geographicElement>
			</gmd:EX_Extent>
		</srv:extent>
	</xsl:template>
	<!-- TEMPLATE: SRS -->
	<xsl:template match="SRS | wms:CRS">
		<gmd:referenceSystemInfo>
			<gmd:MD_ReferenceSystem>
				<gmd:referenceSystemIdentifier>
					<gmd:RS_Identifier>
						<gmd:code>
							<gco:CharacterString>
								<xsl:value-of select="."/>
							</gco:CharacterString>
						</gmd:code>
					</gmd:RS_Identifier>
				</gmd:referenceSystemIdentifier>
			</gmd:MD_ReferenceSystem>
		</gmd:referenceSystemInfo>
	</xsl:template>
	<!-- TEMPLATE: Coupled Resources - now mandatory for deegree WMS/WFS service metadata records:"further description of the data coupling in the case of tightly coupled services" -->
	<!-- NOTE: This is a hack! Based on a WMS getCapabilities response, there is no good way to know which service operations apply to which layer. Also, some <Layer> tags act as containers but I assume that GetMap will operate on those anyways. -->
	<xsl:template name="addCoupledResource">
		<xsl:for-each select="//Layer[boolean(./Name) ] | //wms:Layer[./wms:Name]">
				<!-- *** GetMap *** -->
				<!-- NOTE: Only retreave <Layer> tags which have a <Name> child element. -->
				<srv:coupledResource>
					<srv:SV_CoupledResource>
						<!-- Name of the service operation: GetMap, GetFeatureInfo, etc. -->
						<srv:operationName>
							<gco:CharacterString>GetMap</gco:CharacterString>
						</srv:operationName>
						<!-- Name of the identifier of a given tightly coupled dataset. -->
						<srv:identifier>
							<gco:CharacterString>
								<xsl:value-of select="concat( Name, wms:Name )"/>
							</gco:CharacterString>
						</srv:identifier>
						<gco:ScopedName>
							<xsl:value-of select="concat( Name, wms:Name )"/>
						</gco:ScopedName>
					</srv:SV_CoupledResource>
				</srv:coupledResource>
				<!-- *** GetFeatureInfo *** -->
				<!-- NOTE: Only retreave <Layer> tags who have a queryable="1" property and a <Name> child element. -->
				<xsl:if test="@queryable = 1">
					<srv:coupledResource>
						<srv:SV_CoupledResource>
							<!-- Name of the service operation: GetMap, GetFeatureInfo, etc. -->
							<srv:operationName>
								<gco:CharacterString>GetFeatureInfo</gco:CharacterString>
							</srv:operationName>
							<!-- Name of the identifier of a given tightly coupled dataset. -->
							<srv:identifier>
								<gco:CharacterString>
									<xsl:value-of select="concat( Name, wms:Name )"/>
								</gco:CharacterString>
							</srv:identifier>
							<gco:ScopedName>
								<xsl:value-of select="concat( Name, wms:Name )"/>
							</gco:ScopedName>
						</srv:SV_CoupledResource>
					</srv:coupledResource>
				</xsl:if>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
