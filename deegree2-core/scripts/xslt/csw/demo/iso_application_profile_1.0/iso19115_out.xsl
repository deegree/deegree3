<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gml="http://www.opengis.net/gml" 
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:app="http://www.deegree.org/app" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco">
	
	<!-- ========================================================	
		resultset brief
	===========================================================  -->
	<xsl:template name="ISO19115BRIEF">
		<gmd:MD_Metadata>
			<gmd:fileIdentifier>
				<gco:CharacterString>
					<xsl:value-of select="app:fileidentifier" />
				</gco:CharacterString>
			</gmd:fileIdentifier>
			<xsl:if test="app:hierarchyLevelCode">
				<gmd:hierarchyLevel>
					<gmd:MD_ScopeCode>
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_ScopeCode>
				</gmd:hierarchyLevel>
			</xsl:if>
			<xsl:for-each select="app:contact/app:CI_RespParty">
				<gmd:contact>
					<xsl:call-template name="ci_respparty_minimal"/>
				</gmd:contact>
			</xsl:for-each>
			<gmd:dateStamp>
				<gco:DateTime>
					<xsl:value-of select="app:dateStamp" />
				</gco:DateTime>
			</gmd:dateStamp>
			<xsl:apply-templates select="app:dataIdentification/app:MD_DataIdentification"/>
		</gmd:MD_Metadata>
	</xsl:template>
	
	<!-- ========================================================	
		resultset summary
	===========================================================  -->
	<xsl:template name="ISO19115SUMMARY">
			<gmd:MD_Metadata>
				<gmd:fileIdentifier>
					<gco:CharacterString>
						<xsl:value-of select="app:fileidentifier" />
					</gco:CharacterString>
				</gmd:fileIdentifier>
				<xsl:if test="app:language">
					<gmd:language>
				    <gmd:LanguageCode>
                <xsl:attribute name="codeList">#LanguageCode</xsl:attribute>
                <xsl:attribute name="codeListValue">
                        <xsl:value-of select="app:language" />
                </xsl:attribute>
            </gmd:LanguageCode>
					</gmd:language>
				</xsl:if>
				<xsl:if test="app:characterSet">
					<gmd:characterSet>
						<gmd:MD_CharacterSetCode>
							<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:characterSet/app:MD_CharacterSetCode/app:codelistvalue" />
							</xsl:attribute>
						</gmd:MD_CharacterSetCode>
					</gmd:characterSet>
				</xsl:if>
				<xsl:if test="app:parentidentifier">
					<gmd:parentIdentifier>
						<gco:CharacterString>
							<xsl:value-of select="app:parentidentifier" />
						</gco:CharacterString>
					</gmd:parentIdentifier>
				</xsl:if>
				<xsl:if test="app:hierarchyLevelCode">
					<gmd:hierarchyLevel>
						<gmd:MD_ScopeCode>
							<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
							</xsl:attribute>
						</gmd:MD_ScopeCode>
					</gmd:hierarchyLevel>
				</xsl:if>
				<xsl:if test="app:hierarchyLevelName">
					<gmd:hierarchyLevelName>
						<gco:CharacterString>
							<xsl:value-of select="app:hierarchyLevelName/app:HierarchyLevelName/app:name" />
						</gco:CharacterString>
					</gmd:hierarchyLevelName>
				</xsl:if>
				<xsl:for-each select="app:contact/app:CI_RespParty">
					<gmd:contact>
						<xsl:call-template name="ci_respparty_minimal"/>
					</gmd:contact>
				</xsl:for-each>
				<gmd:dateStamp>
					<gco:DateTime>
						<xsl:value-of select="app:dateStamp" />
					</gco:DateTime>
				</gmd:dateStamp>				
				<xsl:if test="app:metadataStandardName">
					<gmd:metadataStandardName>
						<gco:CharacterString>
							<xsl:value-of select="app:metadataStandardName" />
						</gco:CharacterString>
					</gmd:metadataStandardName>
				</xsl:if>
				<xsl:if test="app:metadataStandardVersion">
					<gmd:metadataStandardVersion>
						<gco:CharacterString>
							<xsl:value-of select="app:metadataStandardVersion" />
						</gco:CharacterString>
					</gmd:metadataStandardVersion>
				</xsl:if>
				<xsl:for-each select="app:referenceSystemInfo">
					<gmd:referenceSystemInfo>
						<gmd:MD_ReferenceSystem>
							<gmd:referenceSystemIdentifier>
								<xsl:apply-templates select="app:RS_Identifier" />
							</gmd:referenceSystemIdentifier>
						</gmd:MD_ReferenceSystem>
					</gmd:referenceSystemInfo>
				</xsl:for-each>
				<xsl:apply-templates select="app:dataIdentification/app:MD_DataIdentification"/>
				<xsl:if test="boolean( app:distributionInfo/app:MD_Distribution )">
					<xsl:call-template name="summary_distributioninfo" />
				</xsl:if>
				<xsl:for-each select="app:dataQualityInfo">
					<gmd:dataQualityInfo>
						<xsl:call-template name="summary_dataquality"/>
					</gmd:dataQualityInfo>
				</xsl:for-each>
				<xsl:for-each select="app:legalConstraints">
					<gmd:metadataConstraints>
						<xsl:apply-templates select="app:MD_LegalConstraints" />
					</gmd:metadataConstraints>
				</xsl:for-each>
				<xsl:for-each select="app:securityConstraints">
					<gmd:metadataConstraints>
						<xsl:apply-templates select="app:MD_SecurityConstraints" />
					</gmd:metadataConstraints>
				</xsl:for-each>
                <xsl:for-each select="app:constraints">
                    <gmd:metadataConstraints>
                        <xsl:apply-templates select="app:MD_Constraints" />
                    </gmd:metadataConstraints>
                </xsl:for-each>
			</gmd:MD_Metadata>
		</xsl:template>	

	<!-- ========================================================	
		resultset full 
	===========================================================  -->
	<xsl:template name="ISO19115FULL">
		<gmd:MD_Metadata>
			<gmd:fileIdentifier>
				<gco:CharacterString>
					<xsl:value-of select="app:fileidentifier" />
				</gco:CharacterString>
			</gmd:fileIdentifier>
			<xsl:if test="app:language">
				<gmd:language>
				    <gmd:LanguageCode>
                <xsl:attribute name="codeList">#LanguageCode</xsl:attribute>
                <xsl:attribute name="codeListValue">
                        <xsl:value-of select="app:language" />
                </xsl:attribute>
            </gmd:LanguageCode>
				</gmd:language>
			</xsl:if>
			<xsl:if test="app:characterSet">
				<gmd:characterSet>
					<gmd:MD_CharacterSetCode>
						<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:characterSet/app:MD_CharacterSetCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_CharacterSetCode>
				</gmd:characterSet>
			</xsl:if>
			<xsl:if test="app:parentidentifier">
				<gmd:parentIdentifier>
					<gco:CharacterString>
						<xsl:value-of select="app:parentidentifier" />
					</gco:CharacterString>
				</gmd:parentIdentifier>
			</xsl:if>
			<xsl:if test="app:hierarchyLevelCode">
				<gmd:hierarchyLevel>
					<gmd:MD_ScopeCode>
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_ScopeCode>
				</gmd:hierarchyLevel>
			</xsl:if>
			<xsl:if test="app:hierarchyLevelName">
				<gmd:hierarchyLevelName>
					<gco:CharacterString>
						<xsl:value-of select="app:hierarchyLevelName/app:HierarchyLevelName/app:name" />
					</gco:CharacterString>
				</gmd:hierarchyLevelName>
			</xsl:if>
			<xsl:for-each select="app:contact">
				<gmd:contact>
					<xsl:apply-templates select="./app:CI_RespParty" />
				</gmd:contact>
			</xsl:for-each>
			<gmd:dateStamp>
				<gco:DateTime>
					<xsl:value-of select="app:dateStamp" />
				</gco:DateTime>
			</gmd:dateStamp>
			<xsl:if test="app:metadataStandardName">
				<gmd:metadataStandardName>
					<gco:CharacterString>
						<xsl:value-of select="app:metadataStandardName" />
					</gco:CharacterString>
				</gmd:metadataStandardName>
			</xsl:if>
			<xsl:if test="app:metadataStandardVersion">
				<gmd:metadataStandardVersion>
					<gco:CharacterString>
						<xsl:value-of select="app:metadataStandardVersion" />
					</gco:CharacterString>
				</gmd:metadataStandardVersion>
			</xsl:if>
			<xsl:for-each select="app:locale">
                <xsl:apply-templates select="app:PT_Locale"/>
			</xsl:for-each>
			<xsl:for-each select="app:spatialReprenstationInfo">
				<gmd:spatialRepresentationInfo>
					<xsl:apply-templates select="app:MD_VectorSpatialReprenstation" />
				</gmd:spatialRepresentationInfo>
			</xsl:for-each>
			<xsl:for-each select="app:referenceSystemInfo">
				<gmd:referenceSystemInfo>
					<gmd:MD_ReferenceSystem>
						<gmd:referenceSystemIdentifier>
							<xsl:apply-templates select="app:RS_Identifier" />
						</gmd:referenceSystemIdentifier>
					</gmd:MD_ReferenceSystem>
				</gmd:referenceSystemInfo>
			</xsl:for-each>
			<xsl:for-each select="app:dataIdentification">
				<xsl:apply-templates select="app:MD_DataIdentification" />
			</xsl:for-each>	
			<xsl:for-each select="app:featureCatalogDescription">
				<xsl:apply-templates select="app:MD_FeatCatDesc" />
			</xsl:for-each>
			<xsl:if test="boolean( app:distributionInfo/app:MD_Distribution )">
				<xsl:call-template name="distributioninfo" />
			</xsl:if>
			<xsl:for-each select="app:dataQualityInfo">
				<gmd:dataQualityInfo>
					<xsl:apply-templates select="app:DQ_DataQuality" />
				</gmd:dataQualityInfo>
			</xsl:for-each>
			<xsl:for-each select="app:portrayalCatalogReference">
				<gmd:portrayalCatalogueInfo>
					<xsl:apply-templates select="app:MD_PortrayalCatRef" />
				</gmd:portrayalCatalogueInfo>
			</xsl:for-each>
			<xsl:for-each select="app:legalConstraints">
				<gmd:metadataConstraints>
					<xsl:apply-templates select="app:MD_LegalConstraints" />
				</gmd:metadataConstraints>
			</xsl:for-each>
			<xsl:for-each select="app:securityConstraints">
				<gmd:metadataConstraints>
					<xsl:apply-templates select="app:MD_SecurityConstraints" />
				</gmd:metadataConstraints>
			</xsl:for-each>
            <xsl:for-each select="app:constraints">
                <gmd:metadataConstraints>
                    <xsl:apply-templates select="app:MD_Constraints" />
                </gmd:metadataConstraints>
            </xsl:for-each>
			<xsl:for-each select="app:applicationSchemaInformation">
				<gmd:applicationSchemaInfo>
					<xsl:apply-templates select="app:MD_ApplicationSchemaInformation" />
				</gmd:applicationSchemaInfo>
			</xsl:for-each>
		</gmd:MD_Metadata>
	</xsl:template>
	
</xsl:stylesheet>
