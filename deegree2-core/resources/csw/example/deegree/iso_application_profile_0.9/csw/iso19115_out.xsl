<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:app="http://www.deegree.org/app" xmlns:iso19115="http://schemas.opengis.net/iso19115full"
	xmlns:iso19115summary="http://schemas.opengis.net/iso19115summary"
	xmlns:iso19115brief="http://schemas.opengis.net/iso19115brief" xmlns:smXML="http://metadata.dgiwg.org/smXML">
	<xsl:template name="ISO19115BRIEF">
		<iso19115brief:MD_Metadata>
			<fileIdentifier>
				<smXML:CharacterString>
					<xsl:value-of select="app:fileidentifier" />
				</smXML:CharacterString>
			</fileIdentifier>
			<xsl:if test="app:hierarchyLevelCode">
				<hierarchyLevel>
					<smXML:MD_ScopeCode>
						<xsl:attribute name="codeList">MD_ScopeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_ScopeCode>
				</hierarchyLevel>
			</xsl:if>
			<xsl:for-each select="app:contact">
				<contact>
					<xsl:apply-templates select="./app:CI_RespParty" />
				</contact>
			</xsl:for-each>
			<xsl:for-each select="app:dataIdentification">
				<xsl:apply-templates select="app:MD_DataIdentification" />
			</xsl:for-each>
		</iso19115brief:MD_Metadata>
	</xsl:template>
	<xsl:template name="ISO19115SUMMARY">
		<iso19115summary:MD_Metadata>
			<fileIdentifier>
				<smXML:CharacterString>
					<xsl:value-of select="app:fileidentifier" />
				</smXML:CharacterString>
			</fileIdentifier>
			<xsl:if test="app:language">
				<language>
					<smXML:CharacterString>
						<xsl:value-of select="app:language" />
					</smXML:CharacterString>
				</language>
			</xsl:if>
			<xsl:if test="app:characterSet">
				<characterSet>
					<smXML:MD_CharacterSetCode>
						<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:characterSet/app:MD_CharacterSetCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_CharacterSetCode>
				</characterSet>
			</xsl:if>
			<xsl:if test="app:parentidentifier">
				<parentIdentifier>
					<smXML:CharacterString>
						<xsl:value-of select="app:parentidentifier" />
					</smXML:CharacterString>
				</parentIdentifier>
			</xsl:if>
			<xsl:if test="app:hierarchyLevelCode">
				<hierarchyLevel>
					<smXML:MD_ScopeCode>
						<xsl:attribute name="codeList">MD_ScopeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_ScopeCode>
				</hierarchyLevel>
			</xsl:if>
			<xsl:if test="app:hierarchyLevelName">
				<hierarchyLevelName>
					<smXML:CharacterString>
						<xsl:value-of select="app:hierarchyLevelName/app:HierarchyLevelName/app:name" />
					</smXML:CharacterString>
				</hierarchyLevelName>
			</xsl:if>
			<xsl:for-each select="app:contact">
				<contact>
					<xsl:apply-templates select="./app:CI_RespParty" />
				</contact>
			</xsl:for-each>
			<dateStamp>
				<smXML:DateTime>
					<xsl:value-of select="concat( app:dateStamp )" />
					<!--xsl:value-of select="concat( app:dateStamp, 'T00:00:00' )"/-->
				</smXML:DateTime>
			</dateStamp>
			<xsl:if test="app:metadataStandardName">
				<metadataStandardName>
					<smXML:CharacterString>
						<xsl:value-of select="app:metadataStandardName" />
					</smXML:CharacterString>
				</metadataStandardName>
			</xsl:if>
			<xsl:if test="app:metadataStandardVersion">
				<metadataStandardVersion>
					<smXML:CharacterString>
						<xsl:value-of select="app:metadataStandardVersion" />
					</smXML:CharacterString>
				</metadataStandardVersion>
			</xsl:if>
			<xsl:for-each select="app:dataIdentification">
				<xsl:apply-templates select="app:MD_DataIdentification" />
			</xsl:for-each>
			<xsl:for-each select="app:dataQualityInfo">
				<dataQualityInfo>
					<xsl:apply-templates select="app:DQ_DataQuality" />
				</dataQualityInfo>
			</xsl:for-each>
			<xsl:for-each select="app:referenceSystemInfo">
				<referenceSystemInfo>
					<smXML:MD_ReferenceSystem>
						<smXML:referenceSystemIdentifier>
							<xsl:apply-templates select="app:RS_Identifier" />
						</smXML:referenceSystemIdentifier>
					</smXML:MD_ReferenceSystem>
				</referenceSystemInfo>
			</xsl:for-each>
			<xsl:if test="app:distributionInfo/app:MD_Distribution">
				<xsl:call-template name="summary_distributioninfo" />
			</xsl:if>

		</iso19115summary:MD_Metadata>
	</xsl:template>
	<xsl:template name="ISO19115FULL">
		<iso19115:MD_Metadata>
			<iso19115:fileIdentifier>
				<smXML:CharacterString>
					<xsl:value-of select="app:fileidentifier" />
				</smXML:CharacterString>
			</iso19115:fileIdentifier>
			<xsl:if test="app:language">
				<iso19115:language>
					<smXML:CharacterString>
						<xsl:value-of select="app:language" />
					</smXML:CharacterString>
				</iso19115:language>
			</xsl:if>
			<xsl:if test="app:characterSet">
				<iso19115:characterSet>
					<smXML:MD_CharacterSetCode>
						<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:characterSet/app:MD_CharacterSetCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_CharacterSetCode>
				</iso19115:characterSet>
			</xsl:if>
			<xsl:if test="app:parentidentifier">
				<iso19115:parentIdentifier>
					<smXML:CharacterString>
						<xsl:value-of select="app:parentidentifier" />
					</smXML:CharacterString>
				</iso19115:parentIdentifier>
			</xsl:if>
			<xsl:if test="app:hierarchyLevelCode">
				<iso19115:hierarchyLevel>
					<smXML:MD_ScopeCode>
						<xsl:attribute name="codeList">MD_ScopeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_ScopeCode>
				</iso19115:hierarchyLevel>
			</xsl:if>
			<xsl:if test="app:hierarchyLevelName">
				<iso19115:hierarchyLevelName>
					<smXML:CharacterString>
						<xsl:value-of select="app:hierarchyLevelName/app:HierarchyLevelName/app:name" />
					</smXML:CharacterString>
				</iso19115:hierarchyLevelName>
			</xsl:if>
			<xsl:for-each select="app:contact">
				<iso19115:contact>
					<xsl:apply-templates select="./app:CI_RespParty" />
				</iso19115:contact>
			</xsl:for-each>
			<iso19115:dateStamp>
				<smXML:DateTime>
					<!--xsl:value-of select="concat( app:dateStamp, 'T00:00:00')"/-->
					<xsl:value-of select="app:dateStamp" />
				</smXML:DateTime>
			</iso19115:dateStamp>
			<xsl:if test="app:metadataStandardName">
				<iso19115:metadataStandardName>
					<smXML:CharacterString>
						<xsl:value-of select="app:metadataStandardName" />
					</smXML:CharacterString>
				</iso19115:metadataStandardName>
			</xsl:if>
			<xsl:if test="app:metadataStandardVersion">
				<iso19115:metadataStandardVersion>
					<smXML:CharacterString>
						<xsl:value-of select="app:metadataStandardVersion" />
					</smXML:CharacterString>
				</iso19115:metadataStandardVersion>
			</xsl:if>
			<xsl:for-each select="app:dataIdentification">
				<xsl:apply-templates select="app:MD_DataIdentification" />
			</xsl:for-each>
			<xsl:for-each select="app:dataQualityInfo">
				<iso19115:dataQualityInfo>
					<xsl:apply-templates select="app:DQ_DataQuality" />
				</iso19115:dataQualityInfo>
			</xsl:for-each>
			<xsl:for-each select="app:spatialReprenstationInfo">
				<iso19115:spatialRepresentationInfo>
					<xsl:apply-templates select="app:MD_VectorSpatialReprenstation" />
				</iso19115:spatialRepresentationInfo>
			</xsl:for-each>
			<xsl:for-each select="app:referenceSystemInfo">
				<iso19115:referenceSystemInfo>
					<smXML:MD_ReferenceSystem>
						<smXML:referenceSystemIdentifier>
							<xsl:apply-templates select="app:RS_Identifier" />
						</smXML:referenceSystemIdentifier>
					</smXML:MD_ReferenceSystem>
				</iso19115:referenceSystemInfo>
			</xsl:for-each>
			<xsl:for-each select="app:featureCatalogDescription">
				<xsl:apply-templates select="app:MD_FeatCatDesc" />
			</xsl:for-each>

			<xsl:if test="boolean( app:distributionInfo/app:MD_Distribution )">
				<xsl:call-template name="distributioninfo" />
			</xsl:if>

			<xsl:for-each select="app:legalConstraints">
				<smXML:metadataConstraints>
					<xsl:apply-templates select="app:MD_LegalConstraints" />
				</smXML:metadataConstraints>
			</xsl:for-each>
			<xsl:for-each select="app:securityConstraints">
				<smXML:metadataConstraints>
					<xsl:apply-templates select="app:MD_SecurityConstraints" />
				</smXML:metadataConstraints>
			</xsl:for-each>

			<xsl:for-each select="app:portrayalCatalogReference">
				<iso19115:portrayalCatalogueInfo>
					<xsl:apply-templates select="app:MD_PortrayalCatRef" />
				</iso19115:portrayalCatalogueInfo>
			</xsl:for-each>

			<xsl:for-each select="app:applicationSchemaInformation">
				<iso19115:applicationSchemaInfo>
					<xsl:apply-templates select="app:MD_ApplicationSchemaInformation" />
				</iso19115:applicationSchemaInfo>
			</xsl:for-each>

		</iso19115:MD_Metadata>
	</xsl:template>
</xsl:stylesheet>
