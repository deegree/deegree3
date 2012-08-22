<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:app="http://www.deegree.org/app" xmlns:iso19119="http://schemas.opengis.net/iso19119"
	xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:iso19115brief="http://schemas.opengis.net/iso19115brief">

	<xsl:template name="ISO19119BRIEF">
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
			<xsl:for-each select="app:serviceIdentification">
				<identificationInfo>
					<xsl:call-template name="CSW_SERVICEIDENTIFICATION_BRIEF"></xsl:call-template>
				</identificationInfo>
			</xsl:for-each>
		</iso19115brief:MD_Metadata>
	</xsl:template>

	<xsl:template name="ISO19119FULL">
		<smXML:MD_Metadata>
			<smXML:fileIdentifier>
				<smXML:CharacterString>
					<xsl:value-of select="app:fileidentifier" />
				</smXML:CharacterString>
			</smXML:fileIdentifier>
			<xsl:if test="app:language">
				<smXML:language>
					<smXML:CharacterString>
						<xsl:value-of select="app:language" />
					</smXML:CharacterString>
				</smXML:language>
			</xsl:if>
			<xsl:if test="app:characterSet">
				<smXML:characterSet>
					<smXML:MD_CharacterSetCode>
						<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:characterSet/app:MD_CharacterSetCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_CharacterSetCode>
				</smXML:characterSet>
			</xsl:if>
			<xsl:if test="app:parentidentifier">
				<smXML:parentIdentifier>
					<smXML:CharacterString>
						<xsl:value-of select="app:parentidentifier" />
					</smXML:CharacterString>
				</smXML:parentIdentifier>
			</xsl:if>
			<xsl:if test="app:hierarchyLevelCode">
				<smXML:hierarchyLevel>
					<smXML:MD_ScopeCode>
						<xsl:attribute name="codeList">MD_ScopeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_ScopeCode>
				</smXML:hierarchyLevel>
			</xsl:if>
			<xsl:if test="app:hierarchyLevelName">
				<smXML:hierarchyLevelName>
					<smXML:CharacterString>
						<xsl:value-of select="app:hierarchyLevelName/app:HierarchyLevelName/app:name" />
					</smXML:CharacterString>
				</smXML:hierarchyLevelName>
			</xsl:if>
			<xsl:for-each select="app:contact">
				<smXML:contact>
					<xsl:apply-templates select="./app:CI_RespParty" />
				</smXML:contact>
			</xsl:for-each>
			<smXML:dateStamp>
				<smXML:DateTime>
					<xsl:value-of select="concat( app:dateStamp )" />
				</smXML:DateTime>
			</smXML:dateStamp>
			<xsl:if test="app:metadataStandardName">
				<smXML:metadataStandardName>
					<smXML:CharacterString>
						<xsl:value-of select="app:metadataStandardName" />
					</smXML:CharacterString>
				</smXML:metadataStandardName>
			</xsl:if>
			<xsl:if test="app:metadataStandardVersion">
				<smXML:metadataStandardVersion>
					<smXML:CharacterString>
						<xsl:value-of select="app:metadataStandardVersion" />
					</smXML:CharacterString>
				</smXML:metadataStandardVersion>
			</xsl:if>
			<xsl:for-each select="app:serviceIdentification">
				<xsl:apply-templates select="app:CSW_ServiceIdentification" />
			</xsl:for-each>
			<xsl:for-each select="app:dataQualityInfo">
				<smXML:dataQualityInfo>
					<xsl:apply-templates select="app:DQ_DataQuality" />
				</smXML:dataQualityInfo>
			</xsl:for-each>
			<xsl:for-each select="app:spatialReprenstationInfo">
				<smXML:spatialRepresentationInfo>
					<xsl:apply-templates select="app:MD_VectorSpatialReprenstation" />
				</smXML:spatialRepresentationInfo>
			</xsl:for-each>
			<xsl:for-each select="app:referenceSystemInfo">
				<smXML:referenceSystemInfo>
					<smXML:MD_ReferenceSystem>
						<smXML:referenceSystemIdentifier>
							<xsl:apply-templates select="app:RS_Identifier" />
						</smXML:referenceSystemIdentifier>
					</smXML:MD_ReferenceSystem>
				</smXML:referenceSystemInfo>
			</xsl:for-each>
		</smXML:MD_Metadata>
	</xsl:template>

	<!-- ====================================================== -->
	<!--                                service identification 		                                                 -->
	<!-- ====================================================== -->
	<xsl:template name="CSW_SERVICEIDENTIFICATION_BRIEF">
		<iso19115brief:CSW_ServiceIdentification>
			<title>
				<smXML:CharacterString>
					<xsl:value-of
						select="app:CSW_ServiceIdentification/app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title">
					</xsl:value-of>
				</smXML:CharacterString>
			</title>
			<serviceType>
				<smXML:CharacterString>
					<xsl:value-of select="app:CSW_ServiceIdentification/app:servicetype" />
				</smXML:CharacterString>
			</serviceType>
			<xsl:for-each select="app:CSW_ServiceIdentification/app:serviceTypeVersion">
				<serviceTypeVersion>
					<smXML:CharacterString>
						<xsl:value-of select="." />
					</smXML:CharacterString>
				</serviceTypeVersion>
			</xsl:for-each>
			<xsl:for-each select="app:boundingBox">
				<extent>
					<xsl:apply-templates select="app:EX_GeogrBBOX" />
				</extent>
			</xsl:for-each>
			<couplingType>
				<iso19119:CSW_CouplingType codeList="http://schemas.opengis.net/iso19119/couplingType">
					<xsl:attribute name="codeListValue">
						<xsl:value-of
							select="app:CSW_ServiceIdentification/app:couplingType/app:CSW_CouplingType/app:codelistvalue" />
					</xsl:attribute>
				</iso19119:CSW_CouplingType>
			</couplingType>
		</iso19115brief:CSW_ServiceIdentification>
	</xsl:template>


	<xsl:template match="app:CSW_ServiceIdentification">
		<smXML:identificationInfo>
			<iso19119:CSW_ServiceIdentification>
				<smXML:citation>
					<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:citation">
						<xsl:apply-templates select="app:CI_Citation" />
					</xsl:for-each>
				</smXML:citation>
				<smXML:abstract>
					<smXML:CharacterString>
						<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:abstract" />
					</smXML:CharacterString>
				</smXML:abstract>
				<xsl:if test="app:identificationInfo/app:MD_Identification/app:purpose">
					<smXML:purpose>
						<smXML:CharacterString>
							<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:purpose" />
						</smXML:CharacterString>
					</smXML:purpose>
				</xsl:if>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:status">
					<smXML:status>
						<smXML:MD_ProgressCode>
							<xsl:attribute name="codeList">MD_ProgressCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:MD_ProgressCode/app:codelistvalue" />
							</xsl:attribute>
						</smXML:MD_ProgressCode>
					</smXML:status>
				</xsl:for-each>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:pointOfContact">
					<smXML:pointOfContact>
						<xsl:apply-templates select="app:CI_RespParty " />
					</smXML:pointOfContact>
				</xsl:for-each>
				<xsl:for-each
					select="app:identificationInfo/app:MD_Identification/app:resourceSpecificUsage/app:MD_Usage">
					<smXML:resourceSpecificUsage>
						<smXML:MD_Usage>
							<smXML:specificUsage>
								<smXML:CharacterString>
									<xsl:value-of select="app:specificusage" />
								</smXML:CharacterString>
							</smXML:specificUsage>
							<smXML:userContactInfo>
								<xsl:apply-templates select="app:RespParty" />
							</smXML:userContactInfo>
						</smXML:MD_Usage>
					</smXML:resourceSpecificUsage>
				</xsl:for-each>
				<xsl:for-each
					select="app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords">
					<smXML:descriptiveKeywords>
						<smXML:MD_Keywords>
							<xsl:for-each select="app:keyword/app:Keyword/app:keyword">
								<smXML:keyword>
									<smXML:CharacterString>
										<xsl:value-of select="." />
									</smXML:CharacterString>
								</smXML:keyword>
							</xsl:for-each>
							<smXML:type>
								<smXML:MD_KeywordTypeCode>
									<xsl:attribute name="codeList">MD_KeywordTypeCode</xsl:attribute>
									<xsl:attribute name="codeListValue">
										<xsl:value-of select="app:type/app:MD_KeywordTypeCode/app:codelistvalue" />
									</xsl:attribute>
								</smXML:MD_KeywordTypeCode>
							</smXML:type>
							<xsl:for-each select="app:thesaurusName">
								<smXML:thesaurusName>
									<xsl:apply-templates select="app:CI_Citation" />
								</smXML:thesaurusName>
							</xsl:for-each>
						</smXML:MD_Keywords>
					</smXML:descriptiveKeywords>
				</xsl:for-each>
				<xsl:for-each
					select="app:identificationInfo/app:MD_Identification/app:graphicOverview/app:MD_BrowseGraphic">
					<smXML:graphicOverview>
						<smXML:MD_BrowseGraphic>
							<smXML:fileName>
								<smXML:CharacterString>
									<xsl:value-of select="app:filename" />
								</smXML:CharacterString>
							</smXML:fileName>
							<xsl:if test="app:filedescription">
								<smXML:fileDescription>
									<smXML:CharacterString>
										<xsl:value-of select="app:filedescription" />
									</smXML:CharacterString>
								</smXML:fileDescription>
							</xsl:if>
							<xsl:if test="app:filetype">
								<smXML:fileType>
									<smXML:CharacterString>
										<xsl:value-of select="app:filetype" />
									</smXML:CharacterString>
								</smXML:fileType>
							</xsl:if>
						</smXML:MD_BrowseGraphic>
					</smXML:graphicOverview>
				</xsl:for-each>

				<!--xsl:for-each select="app:identificationInfo/app:MD_Identification/app:resourceConstraints">
					<smXML:resourceConstraints>
					<xsl:apply-templates select="app:MD_LegalConstraints"/>
					</smXML:resourceConstraints>
					</xsl:for-each-->
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:legalConstraints">
					<smXML:resourceConstraints>
						<xsl:apply-templates select="app:MD_LegalConstraints" />
					</smXML:resourceConstraints>
				</xsl:for-each>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:securityConstraints">
					<smXML:resourceConstraints>
						<xsl:apply-templates select="app:MD_LegalConstraints" />
					</smXML:resourceConstraints>
				</xsl:for-each>

				<xsl:for-each select="app:resourceMaintenance">
					<xsl:apply-templates select="." />
				</xsl:for-each>
				<iso19119:serviceType>
					<smXML:CharacterString>
						<xsl:value-of select="app:servicetype" />
					</smXML:CharacterString>
				</iso19119:serviceType>
				<xsl:for-each select="app:serviceTypeVersion">
					<iso19119:serviceTypeVersion>
						<smXML:CharacterString>
							<xsl:value-of select="." />
						</smXML:CharacterString>
					</iso19119:serviceTypeVersion>
				</xsl:for-each>
				<xsl:if
					test="boolean(app:fees) or boolean(app:plannedAvailableDatetime) or boolean(app:orderingInstructions) or boolean(app:turnaround)">
					<iso19119:accessProperties>
						<xsl:if test="boolean(app:fees)">
							<smXML:fees>
								<smXML:CharacterString>
									<xsl:value-of select="app:fees" />
								</smXML:CharacterString>
							</smXML:fees>
						</xsl:if>
						<xsl:if test="boolean( app:plannedAvailableDatetime )">
							<smXML:plannedAvailableDatetime>
								<smXML:date>
									<smXML:DateTime>
										<xsl:choose>
											<xsl:when test="contains( app:plannedAvailableDatetime, 'T' )">
												<xsl:value-of select="app:plannedAvailableDatetime" />
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of
													select="concat( app:plannedAvailableDatetime, 'T00:00:00')" />
											</xsl:otherwise>
										</xsl:choose>
									</smXML:DateTime>
								</smXML:date>
							</smXML:plannedAvailableDatetime>
						</xsl:if>
						<xsl:if test="boolean(app:orderingInstructions)">
							<smXML:orderingInstructions>
								<smXML:CharacterString>
									<xsl:value-of select="app:orderingInstructions" />
								</smXML:CharacterString>
							</smXML:orderingInstructions>
						</xsl:if>
						<xsl:if test="boolean(app:turnaround)">
							<smXML:turnaround>
								<smXML:CharacterString>
									<xsl:value-of select="app:turnaround" />
								</smXML:CharacterString>
							</smXML:turnaround>
						</xsl:if>
					</iso19119:accessProperties>
				</xsl:if>

				<!--xsl:for-each select="app:restrictions">
					<iso19119:restrictions>
					<xsl:apply-templates select="app:MD_LegalConstraints"/>
					</iso19119:restrictions>
					</xsl:for-each-->
				<xsl:if test="app:restrictionsLegal">
					<iso19119:restrictions>
						<xsl:apply-templates select="app:restrictionsLegal/app:MD_LegalConstraints" />
					</iso19119:restrictions>
				</xsl:if>
				<xsl:if test="app:restrictionsSecurity">
					<iso19119:restrictions>
						<xsl:apply-templates select="app:restrictionsSecurity/app:MD_SecurityConstraints" />
					</iso19119:restrictions>
				</xsl:if>

				<xsl:for-each select="app:operationMetadata">
					<xsl:apply-templates select="app:SV_OperationMetadata" />
				</xsl:for-each>
				<!-- 
					<xsl:for-each select="app:operatesOn/app:OperatesOn">
					<iso19119:operatesOn>
					<smXML:Reference>
					<xsl:attribute name="uuidref"><xsl:value-of select="app:name"/></xsl:attribute>
					</smXML:Reference>
					</iso19119:operatesOn>
					</xsl:for-each>
				-->
				<xsl:for-each select="app:boundingBox">
					<iso19119:extent>
						<xsl:apply-templates select="app:EX_GeogrBBOX" />
					</iso19119:extent>
				</xsl:for-each>
				<xsl:for-each select="app:coupledResource/app:CoupledResource/app:operationName">
					<iso19119:coupledResource>
						<iso19119:CSW_CoupledResource>
							<iso19119:operationName>
								<smXML:CharacterString>
									<xsl:value-of select="app:OperationNames/app:name"></xsl:value-of>
								</smXML:CharacterString>
							</iso19119:operationName>
							<xsl:for-each select="../app:identifier">
								<iso19119:identifier>
									<smXML:CharacterString>
										<xsl:value-of select="." />
									</smXML:CharacterString>
								</iso19119:identifier>
							</xsl:for-each>
						</iso19119:CSW_CoupledResource>
					</iso19119:coupledResource>
				</xsl:for-each>
				<iso19119:couplingType>
					<iso19119:CSW_CouplingType codeList="http://schemas.opengis.net/iso19119/couplingType">
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:couplingType/app:CSW_CouplingType/app:codelistvalue" />
						</xsl:attribute>
					</iso19119:CSW_CouplingType>
				</iso19119:couplingType>
			</iso19119:CSW_ServiceIdentification>
		</smXML:identificationInfo>
	</xsl:template>
	<xsl:template match="app:SV_OperationMetadata">
		<iso19119:operationMetadata>
			<iso19119:SV_OperationMetadata>
				<iso19119:operationName>
					<smXML:CharacterString>
						<xsl:value-of select="app:operationName/app:OperationNames/app:name" />
					</smXML:CharacterString>
				</iso19119:operationName>
				<xsl:apply-templates select="app:DCP" />
				<xsl:if test="app:operationDescription">
					<iso19119:operationDescription>
						<smXML:CharacterString>
							<xsl:value-of select="app:operationDescription" />
						</smXML:CharacterString>
					</iso19119:operationDescription>
				</xsl:if>
				<xsl:if test="app:invocationName">
					<iso19119:invocationName>
						<smXML:CharacterString>
							<xsl:value-of select="app:invocationName" />
						</smXML:CharacterString>
					</iso19119:invocationName>
				</xsl:if>
				<xsl:for-each select="app:connectPoint">
					<iso19119:connectPoint>
						<xsl:apply-templates select="app:CI_OnlineResource" />
					</iso19119:connectPoint>
				</xsl:for-each>
				<xsl:for-each select="app:parameters">
					<xsl:apply-templates select="app:SV_Parameter" />
				</xsl:for-each>
			</iso19119:SV_OperationMetadata>
		</iso19119:operationMetadata>
	</xsl:template>
	<xsl:template match="app:DCP">
		<iso19119:DCP>
			<xsl:for-each select="app:SV_DCPList">
				<iso19119:SV_DCPList codeList="http://www.example.com" codeListValue="HTTPGet">
					<xsl:attribute name="codeList">SV_DCPTypeCode</xsl:attribute>
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="app:codelistvalue" />
					</xsl:attribute>
				</iso19119:SV_DCPList>
			</xsl:for-each>
		</iso19119:DCP>
	</xsl:template>
	<xsl:template match="app:SV_Parameter">
		<iso19119:parameters>
			<iso19119:SV_Parameter>
				<iso19119:name>
					<smXML:MemberName>
						<smXML:aName>
							<smXML:CharacterString>
								<xsl:value-of select="app:name" />
							</smXML:CharacterString>
						</smXML:aName>
						<smXML:attributeType>
							<smXML:TypeName>
								<smXML:aName>
									<smXML:CharacterString>
										<xsl:value-of select="app:type" />
									</smXML:CharacterString>
								</smXML:aName>
							</smXML:TypeName>
						</smXML:attributeType>
					</smXML:MemberName>
				</iso19119:name>
				<iso19119:direction>
					<iso19119:SV_ParameterDirection>
						<xsl:value-of select="app:direction" />
					</iso19119:SV_ParameterDirection>
				</iso19119:direction>
				<iso19119:description>
					<smXML:CharacterString>
						<xsl:value-of select="app:description" />
					</smXML:CharacterString>
				</iso19119:description>
				<iso19119:optionality>
					<smXML:CharacterString>
						<xsl:value-of select="app:optionality" />
					</smXML:CharacterString>
				</iso19119:optionality>
				<iso19119:repeatability>
					<smXML:Boolean>
						<xsl:value-of select="app:repeatability" />
					</smXML:Boolean>
				</iso19119:repeatability>
			</iso19119:SV_Parameter>
		</iso19119:parameters>
	</xsl:template>

</xsl:stylesheet>
