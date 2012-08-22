<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gml="http://www.opengis.net/gml" 
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:app="http://www.deegree.org/app" 
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:srv="http://www.isotc211.org/2005/srv"
	xmlns:couple="org.deegree.ogcwebservices.csw.iso_profile.Coupling" >

	<!-- ========================================================	
		template CSW_ServiceIdentification
	===========================================================  -->

	<xsl:template match="app:CSW_ServiceIdentification">
		<xsl:if test="$ELEMENT_SET = 'brief'">
			<gmd:identificationInfo>
				<xsl:call-template name="service_brief" />
			</gmd:identificationInfo>
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'summary'">
			<gmd:identificationInfo>
				<xsl:call-template name="service_summary" />
			</gmd:identificationInfo>
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'full'">
			<gmd:identificationInfo>
				<xsl:call-template name="service" />
			</gmd:identificationInfo>
		</xsl:if>
	</xsl:template>

	<!-- ========================================================	
		resultset brief
	===========================================================  -->
	<xsl:template name="ISO19119BRIEF">
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
			<xsl:apply-templates select="app:serviceIdentification/app:CSW_ServiceIdentification"/>
		</gmd:MD_Metadata>
	</xsl:template>
	
	<!-- ========================================================	
		resultset summary
	===========================================================  -->
	<xsl:template name="ISO19119SUMMARY">
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
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode</xsl:attribute>
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
					<xsl:value-of select="concat( app:dateStamp )" />
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
			<xsl:apply-templates select="app:serviceIdentification/app:CSW_ServiceIdentification" />
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
	<xsl:template name="ISO19119FULL">
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
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_CharacterSetCode</xsl:attribute>
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
					<xsl:value-of select="concat( app:dateStamp )" />
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
			<xsl:for-each select="app:serviceIdentification">
				<xsl:apply-templates select="app:CSW_ServiceIdentification" />
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
		</gmd:MD_Metadata>
	</xsl:template>

	<!-- ====================================================== 
		  service identification BRIEF	                                                
		====================================================== -->
	<xsl:template name="service_brief">
		<srv:SV_ServiceIdentification>
			<gmd:citation>
				<xsl:call-template name="citation_brief"/>
			</gmd:citation>
			<gmd:abstract>
				<gco:CharacterString>
					<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:abstract" />
				</gco:CharacterString>
			</gmd:abstract>
			<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:graphicOverview/app:MD_BrowseGraphic">
				<gmd:graphicOverview>
					<gmd:MD_BrowseGraphic>
						<gmd:fileName>
							<gco:CharacterString>
								<xsl:value-of select="app:filename" />
							</gco:CharacterString>
						</gmd:fileName>
					</gmd:MD_BrowseGraphic>
				</gmd:graphicOverview>
			</xsl:for-each>						
			<srv:serviceType>
				<gco:LocalName>
					<xsl:value-of select="app:servicetype" />
				</gco:LocalName>
			</srv:serviceType>
			<xsl:for-each select="app:serviceTypeVersion">
				<srv:serviceTypeVersion>
					<gco:CharacterString>
						<xsl:value-of select="." />
					</gco:CharacterString>
				</srv:serviceTypeVersion>
			</xsl:for-each>	
			<xsl:if test="boolean( app:boundingBox/app:EX_GeogrBBOX )">
				<srv:extent>
					<xsl:apply-templates select="app:boundingBox/app:EX_GeogrBBOX" />
				</srv:extent>
			</xsl:if>
			<srv:couplingType>
				<srv:SV_CouplingType codeList="http://someurl#SV_CouplingType">
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="app:couplingType/app:CSW_CouplingType/app:codelistvalue" />
					</xsl:attribute>
				</srv:SV_CouplingType>
			</srv:couplingType>
			<xsl:for-each select="app:operationMetadata/app:SV_OperationMetadata">
				<xsl:call-template name="operationMetadata_summary"/>
			</xsl:for-each>	
		</srv:SV_ServiceIdentification>
	</xsl:template>
	
	<!-- ======================================================
		service identification SUMMARY
		====================================================== -->
	<xsl:template name="service_summary">
			<srv:SV_ServiceIdentification>
				<gmd:citation>
					<xsl:call-template name="summary_citation"/>
				</gmd:citation>
				<gmd:abstract>
					<gco:CharacterString>
						<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:abstract" />
					</gco:CharacterString>
				</gmd:abstract>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:pointOfContact">
					<gmd:pointOfContact>
						<xsl:apply-templates select="app:CI_RespParty " />
					</gmd:pointOfContact>
				</xsl:for-each>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:graphicOverview/app:MD_BrowseGraphic">
					<gmd:graphicOverview>
						<gmd:MD_BrowseGraphic>
							<gmd:fileName>
								<gco:CharacterString>
									<xsl:value-of select="app:filename" />
								</gco:CharacterString>
							</gmd:fileName>
						</gmd:MD_BrowseGraphic>
					</gmd:graphicOverview>
				</xsl:for-each>
				<srv:serviceType>
					<gco:LocalName>
						<xsl:value-of select="app:servicetype" />
					</gco:LocalName>
				</srv:serviceType>
				<xsl:for-each select="app:serviceTypeVersion">
					<srv:serviceTypeVersion>
						<gco:CharacterString>
							<xsl:value-of select="." />
						</gco:CharacterString>
					</srv:serviceTypeVersion>
				</xsl:for-each>
				<xsl:for-each select="app:boundingBox">
					<srv:extent>
						<xsl:apply-templates select="app:EX_GeogrBBOX" />
					</srv:extent>
				</xsl:for-each>
				<srv:couplingType>
					<srv:SV_CouplingType codeList="http://someurl#SV_CouplingType">
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:couplingType/app:CSW_CouplingType/app:codelistvalue" />
						</xsl:attribute>
					</srv:SV_CouplingType>
				</srv:couplingType>
				<xsl:for-each select="app:operationMetadata/app:SV_OperationMetadata">
						<xsl:call-template name="operationMetadata_summary"/>
				</xsl:for-each>				
			</srv:SV_ServiceIdentification>
	</xsl:template>
	
	<!-- ========================================================	
		operationMetadata summary (only mandatory elements)
	===========================================================  -->
	<xsl:template name="operationMetadata_summary">
		<srv:containsOperations>
			<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>
						<xsl:value-of select="app:operationName/app:OperationNames/app:name" />
					</gco:CharacterString>
				</srv:operationName>
				<xsl:apply-templates select="app:DCP" />
				<xsl:for-each select="app:connectPoint">
					<srv:connectPoint>
						<xsl:apply-templates select="app:CI_OnlineResource" />
					</srv:connectPoint>
				</xsl:for-each>
			</srv:SV_OperationMetadata>
		</srv:containsOperations>
	</xsl:template>

	<!-- ======================================================
		service identification FULL
		====================================================== -->
	<xsl:template name="service">
			<srv:SV_ServiceIdentification>
				<gmd:citation>
					<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:citation">
						<xsl:apply-templates select="app:CI_Citation" />
					</xsl:for-each>
				</gmd:citation>
				<gmd:abstract>
					<gco:CharacterString>
						<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:abstract" />
					</gco:CharacterString>
				</gmd:abstract>
				<xsl:if test="app:identificationInfo/app:MD_Identification/app:purpose">
					<gmd:purpose>
						<gco:CharacterString>
							<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:purpose" />
						</gco:CharacterString>
					</gmd:purpose>
				</xsl:if>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:status">
					<gmd:status>
						<gmd:MD_ProgressCode>
							<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_ProgressCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:MD_ProgressCode/app:codelistvalue" />
							</xsl:attribute>
						</gmd:MD_ProgressCode>
					</gmd:status>
				</xsl:for-each>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:pointOfContact">
					<gmd:pointOfContact>
						<xsl:apply-templates select="app:CI_RespParty " />
					</gmd:pointOfContact>
				</xsl:for-each>
				<xsl:for-each select="app:resourceMaintenance">
					<xsl:apply-templates select="." />
				</xsl:for-each>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:graphicOverview/app:MD_BrowseGraphic">
					<gmd:graphicOverview>
						<gmd:MD_BrowseGraphic>
							<gmd:fileName>
								<gco:CharacterString>
									<xsl:value-of select="app:filename" />
								</gco:CharacterString>
							</gmd:fileName>
							<xsl:if test="app:filedescription">
								<gmd:fileDescription>
									<gco:CharacterString>
										<xsl:value-of select="app:filedescription" />
									</gco:CharacterString>
								</gmd:fileDescription>
							</xsl:if>
							<xsl:if test="app:filetype">
								<gmd:fileType>
									<gco:CharacterString>
										<xsl:value-of select="app:filetype" />
									</gco:CharacterString>
								</gmd:fileType>
							</xsl:if>
						</gmd:MD_BrowseGraphic>
					</gmd:graphicOverview>
				</xsl:for-each>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords">
					<gmd:descriptiveKeywords>
						<gmd:MD_Keywords>
							<xsl:for-each select="app:keyword/app:Keyword/app:keyword">
								<gmd:keyword>
									<gco:CharacterString>
										<xsl:value-of select="." />
									</gco:CharacterString>
								</gmd:keyword>
							</xsl:for-each>
                            <xsl:if test="boolean( app:type/app:MD_KeywordTypeCode/app:codelistvalue != '' )">
							<gmd:type>
								<gmd:MD_KeywordTypeCode>
									<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode</xsl:attribute>
									<xsl:attribute name="codeListValue">
										<xsl:value-of select="app:type/app:MD_KeywordTypeCode/app:codelistvalue" />
									</xsl:attribute>
								</gmd:MD_KeywordTypeCode>
							</gmd:type>
                            </xsl:if>
							<xsl:for-each select="app:thesaurusName">
								<gmd:thesaurusName>
									<xsl:apply-templates select="app:CI_Citation" />
								</gmd:thesaurusName>
							</xsl:for-each>
						</gmd:MD_Keywords>
					</gmd:descriptiveKeywords>
				</xsl:for-each>				
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:resourceSpecificUsage/app:MD_Usage">
					<gmd:resourceSpecificUsage>
						<gmd:MD_Usage>
							<gmd:specificUsage>
								<gco:CharacterString>
									<xsl:value-of select="app:specificusage" />
								</gco:CharacterString>
							</gmd:specificUsage>
							<gmd:userContactInfo>
								<xsl:apply-templates select="app:RespParty" />
							</gmd:userContactInfo>
						</gmd:MD_Usage>
					</gmd:resourceSpecificUsage>
				</xsl:for-each>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:legalConstraints">
					<gmd:resourceConstraints>
						<xsl:apply-templates select="app:MD_LegalConstraints" />
					</gmd:resourceConstraints>
				</xsl:for-each>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:securityConstraints">
					<gmd:resourceConstraints>
						<xsl:apply-templates select="app:MD_SecurityConstraints" />
					</gmd:resourceConstraints>
				</xsl:for-each>
                <xsl:for-each select="app:identificationInfo/app:MD_Identification/app:constraints">
                    <gmd:resourceConstraints>
                        <xsl:apply-templates select="app:MD_Constraints" />
                    </gmd:resourceConstraints>
                </xsl:for-each>
				<srv:serviceType>
					<gco:LocalName>
						<xsl:value-of select="app:servicetype" />
					</gco:LocalName>
				</srv:serviceType>
				<xsl:for-each select="app:serviceTypeVersion">
					<srv:serviceTypeVersion>
						<gco:CharacterString>
							<xsl:value-of select="." />
						</gco:CharacterString>
					</srv:serviceTypeVersion>
				</xsl:for-each>
				<xsl:if test="boolean(app:fees) or boolean(app:plannedAvailableDatetime) or boolean(app:orderingInstructions) or boolean(app:turnaround)">
					<srv:accessProperties>
						<gmd:MD_StandardOrderProcess>
							<xsl:if test="boolean(app:fees)">
								<gmd:fees>
									<gco:CharacterString>
										<xsl:value-of select="app:fees" />
									</gco:CharacterString>
								</gmd:fees>
							</xsl:if>
							<xsl:if test="boolean( app:plannedAvailableDatetime )">
								<gmd:plannedAvailableDateTime>
										<gco:DateTime>
											<xsl:choose>
												<xsl:when test="contains( app:plannedAvailableDatetime, 'T' )">
													<xsl:value-of select="app:plannedAvailableDatetime" />
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of
														select="concat( app:plannedAvailableDatetime, 'T00:00:00')" />
												</xsl:otherwise>
											</xsl:choose>
										</gco:DateTime>
								</gmd:plannedAvailableDateTime>
							</xsl:if>
							<xsl:if test="boolean(app:orderingInstructions)">
								<gmd:orderingInstructions>
									<gco:CharacterString>
										<xsl:value-of select="app:orderingInstructions" />
									</gco:CharacterString>
								</gmd:orderingInstructions>
							</xsl:if>
							<xsl:if test="boolean(app:turnaround)">
								<gmd:turnaround>
									<gco:CharacterString>
										<xsl:value-of select="app:turnaround" />
									</gco:CharacterString>
								</gmd:turnaround>
							</xsl:if>
						</gmd:MD_StandardOrderProcess>
					</srv:accessProperties>
				</xsl:if>
				<xsl:if test="app:restrictionsLegal">
					<srv:restrictions>
						<xsl:apply-templates select="app:restrictionsLegal/app:MD_LegalConstraints" />
					</srv:restrictions>
				</xsl:if>
				<xsl:if test="app:restrictionsSecurity">
					<srv:restrictions>
						<xsl:apply-templates select="app:restrictionsSecurity/app:MD_SecurityConstraints" />
					</srv:restrictions>
				</xsl:if>
                <xsl:if test="app:restrictionsConstraint">
                    <srv:restrictions>
                        <xsl:apply-templates select="app:restrictionsConstraint/app:MD_Constraints" />
                    </srv:restrictions>
                </xsl:if>
                <xsl:for-each select="app:temportalExtent">
                    <srv:extent>
                        <xsl:apply-templates select="app:EX_TemporalExtent" />
                    </srv:extent>
                </xsl:for-each>
				<xsl:for-each select="app:boundingBox">
					<srv:extent>
						<xsl:apply-templates select="app:EX_GeogrBBOX" />
					</srv:extent>
				</xsl:for-each>
				<xsl:for-each select="app:operatesOn/app:OperatesOn/app:operationName">
					<xsl:if test="boolean( ../app:uuidref != '' )">
						<srv:coupledResource>
							<srv:SV_CoupledResource>
								<srv:operationName>
									<gco:CharacterString>
										<xsl:value-of select="app:OperationNames/app:name"></xsl:value-of>
									</gco:CharacterString>
								</srv:operationName>
								<xsl:for-each select="../app:uuidref">
									<srv:identifier>
										<gco:CharacterString>
											<xsl:value-of select="." />
										</gco:CharacterString>
									</srv:identifier>
								</xsl:for-each>
								<xsl:if test="boolean( ../app:name != '' )">
									<gco:ScopedName>
										<xsl:value-of select="../app:name" />
									</gco:ScopedName>
								</xsl:if>
							</srv:SV_CoupledResource>
						</srv:coupledResource>
					</xsl:if>
				</xsl:for-each>
				<srv:couplingType>
					<srv:SV_CouplingType codeList="http://someurl#SV_CouplingType">
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:couplingType/app:CSW_CouplingType/app:codelistvalue" />
						</xsl:attribute>
					</srv:SV_CouplingType>
				</srv:couplingType>
				<xsl:for-each select="app:operationMetadata">
					<xsl:apply-templates select="app:SV_OperationMetadata" />
				</xsl:for-each>				
				<xsl:for-each select="app:operatesOn/app:OperatesOn/app:uuidref">
					<srv:operatesOn>
						<xsl:attribute name="xlink:href" >
							<xsl:value-of select="concat( couple:getCSWUrl(), '#', .)"/>
						</xsl:attribute>
						<xsl:attribute name="uuidref" >
							<xsl:value-of select="."/>
						</xsl:attribute>
					</srv:operatesOn>
				</xsl:for-each>
			</srv:SV_ServiceIdentification>
	</xsl:template>
	
	<!-- ========================================================	
		template for OperationMetadata
	===========================================================  -->
	<xsl:template match="app:SV_OperationMetadata">
		<srv:containsOperations>
			<srv:SV_OperationMetadata>
				<srv:operationName>
					<gco:CharacterString>
						<xsl:value-of select="app:operationName/app:OperationNames/app:name" />
					</gco:CharacterString>
				</srv:operationName>
				<xsl:apply-templates select="app:DCP" />
				<xsl:if test="app:operationDescription">
					<srv:operationDescription>
						<gco:CharacterString>
							<xsl:value-of select="app:operationDescription" />
						</gco:CharacterString>
					</srv:operationDescription>
				</xsl:if>
				<xsl:if test="app:invocationName">
					<srv:invocationName>
						<gco:CharacterString>
							<xsl:value-of select="app:invocationName" />
						</gco:CharacterString>
					</srv:invocationName>
				</xsl:if>
				<xsl:for-each select="app:parameters">
					<xsl:apply-templates select="app:SV_Parameter" />
				</xsl:for-each>
				<xsl:for-each select="app:connectPoint">
					<srv:connectPoint>
						<xsl:apply-templates select="app:CI_OnlineResource" />
					</srv:connectPoint>
				</xsl:for-each>
			</srv:SV_OperationMetadata>
		</srv:containsOperations>
	</xsl:template>
	
	<!-- ========================================================	
		template for DCP
	===========================================================  -->
	<xsl:template match="app:DCP">
		<srv:DCP>
			<xsl:for-each select="app:SV_DCPList">
				<srv:DCPList codeList="http://www.example.com" codeListValue="HTTPGet">
					<xsl:attribute name="codeList">http://someurl#DCPList</xsl:attribute>
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="app:codelistvalue" />
					</xsl:attribute>
				</srv:DCPList>
			</xsl:for-each>
		</srv:DCP>
	</xsl:template>
	
	<!-- ========================================================	
		template for Parameter
	===========================================================  -->
	<xsl:template match="app:SV_Parameter">
		<srv:parameters>
			<srv:SV_Parameter>
				<srv:name>
					<gco:aName>
						<gco:CharacterString>
							<xsl:value-of select="app:name" />
						</gco:CharacterString>
					</gco:aName>
					<gco:attributeType>
						<gco:TypeName>
							<gco:aName>
								<gco:CharacterString>
									<xsl:value-of select="app:type" />
								</gco:CharacterString>
							</gco:aName>
						</gco:TypeName>
					</gco:attributeType>
				</srv:name>
				<srv:direction>
					<srv:SV_ParameterDirection>
						<xsl:value-of select="app:direction" />
					</srv:SV_ParameterDirection>
				</srv:direction>				
				<srv:description>
					<gco:CharacterString>
						<xsl:value-of select="app:description" />
					</gco:CharacterString>
				</srv:description>
				<srv:optionality>
					<gco:CharacterString>
						<xsl:value-of select="app:optionality" />
					</gco:CharacterString>
				</srv:optionality>
				<srv:repeatability>
					<gco:Boolean>
						<xsl:value-of select="app:repeatability" />
					</gco:Boolean>
				</srv:repeatability>
				<srv:valueType>
					<gco:TypeName>
						<gco:aName>
							<gco:CharacterString>
									<xsl:value-of select="app:valuetype" />
							</gco:CharacterString>
						</gco:aName>						
					</gco:TypeName>
				</srv:valueType>
			</srv:SV_Parameter>
		</srv:parameters>
	</xsl:template>

</xsl:stylesheet>