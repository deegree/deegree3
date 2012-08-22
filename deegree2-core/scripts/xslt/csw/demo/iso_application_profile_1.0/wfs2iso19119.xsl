<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:ows="http://www.opengis.net/ows" 
	xmlns:ogc="http://www.opengis.net/ogc" 
	xmlns:wfs="http://www.opengis.net/wfs" 
	xmlns:gml="http://www.opengis.net/gml" 
	xmlns:java="java" 
	xmlns:ows2iso="de.latlon.bkg.OWS2ISO19119" 
	xmlns:srv="http://www.isotc211.org/2005/srv" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd" 
	xmlns:gco="http://www.isotc211.org/2005/gco" 
	xmlns:xlink="http://www.w3.org/1999/xlink" 
	exclude-result-prefixes="java ows2iso wfs ogc ows gml">
	<xsl:output encoding="UTF-8"/>
	<!-- 
		start variable definitions
	-->
	<xsl:variable name="TIMESTAMP">
		<!--
		date is required but there is no way to to read its value from a WMS capabilities document; 
		instead use current timestamp default
	-->
		<xsl:value-of select="ows2iso:getCurrentTimestamp()"/>
	</xsl:variable>
	<xsl:variable name="DEFAULT_ABSTRACT">BB WMS</xsl:variable>
	<!-- 
		start template definitions
	-->
	<xsl:template match="wfs:WFS_Capabilities">
		<gmd:MD_Metadata xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.isotc211.org/2005/gmd D:\java\projekte\iso19115\CSW202_APISO_100_FINAL\csw202_apiso100\iso\19139\20060504\gmd\metadataEntity.xsd http://www.isotc211.org/2005/srv D:\java\projekte\iso19115\CSW202_APISO_100_FINAL\csw202_apiso100\iso\19139\20060504\srv\serviceMetadata.xsd">
			<gmd:fileIdentifier>
				<gco:CharacterString>
					<xsl:value-of select="./ows:ServiceIdentification/ows:Title"/>
				</gco:CharacterString>
			</gmd:fileIdentifier>
			<gmd:language>
				<gco:CharacterString>ger/deu</gco:CharacterString>
			</gmd:language>
			<gmd:characterSet>
				<gmd:MD_CharacterSetCode codeList="MD_CharacterSetCode" codeListValue="utf8"/>
			</gmd:characterSet>
			<gmd:hierarchyLevel>
				<gmd:MD_ScopeCode codeList="MD_ScopeCode" codeListValue="service"/>
			</gmd:hierarchyLevel>
			<gmd:hierarchyLevelName>
				<gco:CharacterString>service</gco:CharacterString>
			</gmd:hierarchyLevelName>
			<gmd:contact>
				<xsl:choose>
					<xsl:when test="boolean( ows:ServiceProvider )">
						<xsl:call-template name="wfs_service_respParty"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="defaultRespParty"/>
					</xsl:otherwise>
				</xsl:choose>
			</gmd:contact>
			<gmd:dateStamp>
				<gco:DateTime>
					<xsl:value-of select="$TIMESTAMP"/>
				</gco:DateTime>
			</gmd:dateStamp>
			<gmd:metadataStandardName>
				<gco:CharacterString>ISO  19115:2003, 19119: 2006 (BE/BB)</gco:CharacterString>
			</gmd:metadataStandardName>
			<gmd:metadataStandardVersion>
				<gco:CharacterString>1.0</gco:CharacterString>
			</gmd:metadataStandardVersion>
			<xsl:apply-templates select="Capability/Layer/SRS"/>
			<xsl:call-template name="identification"/>
		</gmd:MD_Metadata>
	</xsl:template>
	<xsl:template name="defaultRespParty">
		<!--
		TODO
		add a default definition for a gmd:CI_ResponsibleParty
	-->
	</xsl:template>
	<xsl:template name="wfs_service_respParty">
		<!--
			template for adding a reesponsible party from service section of WMS capabilities
		-->
		<gmd:CI_ResponsibleParty>
			<gmd:individualName>
				<gco:CharacterString>
					<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:IndividualName"/>
				</gco:CharacterString>
			</gmd:individualName>
			<gmd:organisationName>
				<gco:CharacterString>
					<xsl:value-of select="./ows:ServiceProvider/ows:ProviderName"/>
				</gco:CharacterString>
			</gmd:organisationName>
			<gmd:contactInfo>
				<gmd:CI_Contact>
					<xsl:if test="./ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice or ./ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Facsimile">
						<gmd:phone>
							<gmd:CI_Telephone>
								<xsl:if test="./ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice">
									<gmd:voice>
										<gco:CharacterString>
											<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Voice"/>
										</gco:CharacterString>
									</gmd:voice>
								</xsl:if>
								<xsl:if test="./ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Facsimile">
									<gmd:facsimile>
										<gco:CharacterString>
											<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:ContactInfo/ows:Phone/ows:Facsimile"/>
										</gco:CharacterString>
									</gmd:facsimile>
								</xsl:if>
							</gmd:CI_Telephone>
						</gmd:phone>
					</xsl:if>
					<xsl:if test="boolean(./ows:ServiceProvider/ows:ServiceContact/ows:Address)">
						<gmd:address>
							<gmd:CI_Address>
								<gmd:deliveryPoint>
									<gco:CharacterString>
										<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:Address/ows:DeliveryPoint"/>
									</gco:CharacterString>
								</gmd:deliveryPoint>
								<gmd:city>
									<gco:CharacterString>
										<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:Address/ows:City"/>
									</gco:CharacterString>
								</gmd:city>
								<gmd:administrativeArea>
									<gco:CharacterString>
										<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:Address/ows:AdministrativeArea"/>
									</gco:CharacterString>
								</gmd:administrativeArea>
								<gmd:postalCode>
									<gco:CharacterString>
										<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:Address/ows:PostalCode"/>
									</gco:CharacterString>
								</gmd:postalCode>
								<gmd:country>
									<gco:CharacterString>
										<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:Address/ows:Country"/>
									</gco:CharacterString>
								</gmd:country>
								<xsl:if test="boolean(./ows:ServiceProvider/ows:ServiceContact/ows:Address/ows:ElectronicMailAddress )">
									<gmd:electronicMailAddress>
										<gco:CharacterString>
											<xsl:value-of select="./ows:ServiceProvider/ows:ServiceContact/ows:Address/ows:ElectronicMailAddress"/>
										</gco:CharacterString>
									</gmd:electronicMailAddress>
								</xsl:if>
							</gmd:CI_Address>
						</gmd:address>
					</xsl:if>
					<xsl:if test="boolean(./ows:ServiceProvider/ows:ProviderSite )">
						<gmd:onlineResource>
							<gmd:CI_OnlineResource>
								<gmd:linkage>
									<gmd:URL>
										<xsl:value-of select="./ows:ServiceProvider/ows:ProviderSite/@xlink:href"/>
									</gmd:URL>
								</gmd:linkage>
							</gmd:CI_OnlineResource>
						</gmd:onlineResource>
					</xsl:if>
				</gmd:CI_Contact>
			</gmd:contactInfo>
			<gmd:role>
				<gmd:CI_RoleCode codeList="MD_ScopeCode" codeListValue="pointOfContact"/>
			</gmd:role>
		</gmd:CI_ResponsibleParty>
	</xsl:template>
	<xsl:template name="identification">
		<gmd:identificationInfo>
			<srv:SV_ServiceIdentification>
				<gmd:citation>
					<gmd:CI_Citation>
						<gmd:title>
							<gco:CharacterString>
								<xsl:value-of select="./ows:ServiceIdentification/ows:Title"/>
							</gco:CharacterString>
						</gmd:title>
						<gmd:date>
							<gmd:CI_Date>
								<gmd:date>
									<gco:DateTime>
										<xsl:value-of select="$TIMESTAMP"/>
									</gco:DateTime>
								</gmd:date>
								<gmd:dateType>
									<gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
								</gmd:dateType>
							</gmd:CI_Date>
						</gmd:date>
					</gmd:CI_Citation>
				</gmd:citation>
				<gmd:abstract>
					<gco:CharacterString>
						<xsl:choose>
							<xsl:when test="boolean( ./ows:ServiceIdentification/ows:Abstract )">
								<xsl:value-of select="./ows:ServiceIdentification/ows:Abstract"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$DEFAULT_ABSTRACT"/>
							</xsl:otherwise>
						</xsl:choose>
					</gco:CharacterString>
				</gmd:abstract>

				<xsl:if test="boolean( ./ows:ServiceIdentification/ows:Keywords )">
					<gmd:descriptiveKeywords>
						<gmd:MD_Keywords>
							<xsl:for-each select="./ows:ServiceIdentification/ows:Keywords/ows:Keyword">
								<gmd:keyword>
									<gco:CharacterString>
										<xsl:value-of select="."></xsl:value-of>
									</gco:CharacterString>
								</gmd:keyword>
							</xsl:for-each>
							<xsl:if test="boolean( ./ows:ServiceIdentification/ows:Keywords/ows:Type )">
								<gmd:type>
									<gmd:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" >
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="./ows:ServiceIdentification/ows:Keywords/ows:Type"></xsl:value-of>
										</xsl:attribute>
									</gmd:MD_KeywordTypeCode>
								</gmd:type>
							</xsl:if>
						</gmd:MD_Keywords>						
					</gmd:descriptiveKeywords>
				</xsl:if>
				
				<srv:serviceType>
					<gco:LocalName>OGC:WFS</gco:LocalName>
				</srv:serviceType>
				<srv:serviceTypeVersion>
					<gco:CharacterString>1.1.0</gco:CharacterString>
				</srv:serviceTypeVersion>
				<xsl:if test="./ows:ServiceIdentification/ows:Fees">
					<srv:accessProperties>
						<gmd:MD_StandardOrderProcess>
							<gmd:fees>
								<gco:CharacterString>
									<xsl:value-of select="./ows:ServiceIdentification/ows:Fees"/>
								</gco:CharacterString>
							</gmd:fees>
						</gmd:MD_StandardOrderProcess>
					</srv:accessProperties>
				</xsl:if>
				<xsl:if test="./ows:ServiceIdentification/ows:AccessConstraints">
					<srv:restrictions>
						<gmd:MD_LegalConstraints>
							<gmd:accessConstraints>
								<gmd:MD_RestrictionCode codeList="MD_RestrictionCode" codeListValue="otherRestrictions"/>
							</gmd:accessConstraints>
							<gmd:otherConstraints>
								<gco:CharacterString>
									<xsl:value-of select="./ows:ServiceIdentification/ows:AccessConstraints"/>
								</gco:CharacterString>
							</gmd:otherConstraints>
						</gmd:MD_LegalConstraints>
					</srv:restrictions>
				</xsl:if>
				<!-- coupldResource -->
				<xsl:apply-templates select="//wfs:FeatureTypeList/wfs:FeatureType" mode="COUPLEDRESOURCE"/>
				<srv:couplingType>
					<srv:SV_CouplingType codeList="http://schemas.opengis.net/iso19119/couplingType" codeListValue="tight"/>
				</srv:couplingType>
				<xsl:apply-templates select="//ows:OperationsMetadata/ows:Operation"/>
				<xsl:apply-templates select="//wfs:FeatureTypeList/wfs:FeatureType" mode="OPERATESON"/>
			</srv:SV_ServiceIdentification>
		</gmd:identificationInfo>
	</xsl:template>
	<xsl:template match="ows:Operation">
		<srv:containsOperations>
			<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>
						<xsl:value-of select="./@name"/>
					</gco:CharacterString>
				</srv:operationName>
				<xsl:apply-templates select="ows:DCP/ows:HTTP"/>
				<xsl:apply-templates select="ows:Parameter"/>
				<xsl:if test="boolean(ows:DCP/ows:HTTP/ows:Get)">
					<xsl:apply-templates select="ows:DCP/ows:HTTP/ows:Get"/>
				</xsl:if>
				<xsl:if test="boolean(ows:DCP/ows:HTTP/ows:Post)">
					<xsl:apply-templates select="ows:DCP/ows:HTTP/ows:Post"/>
				</xsl:if>
			</srv:SV_OperationMetadata>
		</srv:containsOperations>
	</xsl:template>
	<xsl:template match="ows:Parameter ">
		<srv:parameters>
			<srv:SV_Parameter>
				<srv:name>
					<gco:aName>
						<gco:CharacterString>
							<xsl:value-of select="./@name"/>
						</gco:CharacterString>
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
	</xsl:template>
	<xsl:template match="ows:HTTP">
		<xsl:if test="boolean(ows:Get)">
			<srv:DCP>
				<srv:DCPList codeList="SV_DCPTypeCode" codeListValue="HTTPGet"/>
			</srv:DCP>
		</xsl:if>
		<xsl:if test="boolean(Post)">
			<srv:DCP>
				<srv:DCPList codeList="SV_DCPTypeCode" codeListValue="HTTPPost"/>
			</srv:DCP>
		</xsl:if>
	</xsl:template>
	<xsl:template match="ows:Get | ows:Post">
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
	<xsl:template match="wfs:FeatureType" mode="OPERATESON">
		<srv:operatesOn>
			<gmd:MD_DataIdentification>
				<gmd:citation>
					<gmd:CI_Citation>
						<gmd:title>
							<gco:CharacterString>
								<xsl:value-of select="wfs:Title"/>
							</gco:CharacterString>
						</gmd:title>
						<gmd:date>
							<!--
									date is required but there is no way to to read its value from a WMS capabilities
									document; instead use current timestamp and revision as default
								-->
							<gmd:CI_Date>
								<gmd:date>
									<gco:DateTime>
										<xsl:value-of select="$TIMESTAMP"/>
									</gco:DateTime>
								</gmd:date>
								<gmd:dateType>
									<gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="revision"/>
								</gmd:dateType>
							</gmd:CI_Date>
						</gmd:date>
						<gmd:identifier>
							<gmd:MD_Identifier>
								<gmd:code>
									<gco:CharacterString>
										<xsl:value-of select="wfs:Name"/>
									</gco:CharacterString>
								</gmd:code>
							</gmd:MD_Identifier>
						</gmd:identifier>
					</gmd:CI_Citation>
				</gmd:citation>
				<xsl:choose>
					<xsl:when test="Abstract">
						<gmd:abstract>
							<gco:CharacterString>
								<xsl:value-of select="wfs:Abstract"/>
							</gco:CharacterString>
						</gmd:abstract>
					</xsl:when>
					<xsl:otherwise>
						<gmd:abstract>
							<gco:CharacterString>
								<!-- use layer title as default abstract -->
								<xsl:value-of select="Title"/>
							</gco:CharacterString>
						</gmd:abstract>
					</xsl:otherwise>
				</xsl:choose>
				<gmd:language>
					<gco:CharacterString>de</gco:CharacterString>
				</gmd:language>
				<gmd:topicCategory>
					<!--
							topicCategory is required but there is no way to read its value from a WMS capabilities
							document nor to set a meaningful default
						-->
					<gmd:MD_TopicCategoryCode>environment</gmd:MD_TopicCategoryCode>
				</gmd:topicCategory>
			</gmd:MD_DataIdentification>
		</srv:operatesOn>
	</xsl:template>
	
	<xsl:template match="wfs:FeatureType" mode="COUPLEDRESOURCE">
		<srv:coupledResource>
			<srv:SV_CoupledResource>
				<srv:operationName>
					<gco:CharacterString>DescribeFeatureType</gco:CharacterString>
				</srv:operationName>
				<srv:identifier>
					<gco:CharacterString>
						<xsl:value-of select="wfs:Name"/>
					</gco:CharacterString>
				</srv:identifier>
				<gco:ScopedName>
					<xsl:value-of select="wfs:Name"/>
				</gco:ScopedName>
			</srv:SV_CoupledResource>
		</srv:coupledResource>
		<xsl:for-each select="wfs:Operations/wfs:Operation">
			<xsl:if test="boolean( . = 'Insert' ) or boolean( . = 'Delete' ) or boolean( . = 'Query' )  ">
				<srv:coupledResource>
					<srv:SV_CoupledResource>
						<srv:operationName>
							<gco:CharacterString>
								<xsl:choose> 
									<xsl:when test="boolean( . = 'Insert' ) or boolean( . = 'Delete' )">Transaction</xsl:when>
									<xsl:when test="boolean( . = 'Query' )">GetFeature</xsl:when>
								</xsl:choose>
							</gco:CharacterString>
						</srv:operationName>
						<srv:identifier>
							<gco:CharacterString>
								<xsl:value-of select="../../wfs:Name"/>
							</gco:CharacterString>
						</srv:identifier>
						<gco:ScopedName>
							<xsl:value-of select="../../wfs:Name"/>
						</gco:ScopedName>
					</srv:SV_CoupledResource>
				</srv:coupledResource>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>