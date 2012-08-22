<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:app="http://www.deegree.org/app"
	xmlns:iso19119="http://schemas.opengis.net/iso19119">
	<xsl:template match="smXML:MD_Metadata">
		<app:MD_Metadata>
			<xsl:for-each select="smXML:referenceSystemInfo">
				<xsl:apply-templates select="." />
			</xsl:for-each>
			<xsl:for-each select="smXML:contact">
				<app:contact>
					<xsl:apply-templates select="smXML:CI_ResponsibleParty" />
				</app:contact>
			</xsl:for-each>
			<xsl:choose>
				<xsl:when test="boolean( smXML:identificationInfo/iso19119:CSW_ServiceIdentification != '' )">
					<xsl:apply-templates select="smXML:identificationInfo/iso19119:CSW_ServiceIdentification" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:message>
						------------- Mandatory Element 'smXML:identificationInfo' missing! ----------------
					</xsl:message>
					<app:exception>Mandatory Element 'smXML:identificationInfo' missing!</app:exception>
				</xsl:otherwise>
			</xsl:choose>
			<app:fileidentifier>
				<xsl:value-of select="smXML:fileIdentifier/smXML:CharacterString" />
			</app:fileidentifier>
			<xsl:if test="boolean( smXML:language )">
				<app:language>
					<xsl:value-of select="smXML:language/smXML:CharacterString" />
				</app:language>
			</xsl:if>
			<xsl:if test="smXML:characterSet">
				<app:characterSet>
					<app:MD_CharacterSetCode>
						<app:codelistvalue>
							<xsl:value-of select="smXML:characterSet/smXML:MD_CharacterSetCode/@codeListValue" />
						</app:codelistvalue>
					</app:MD_CharacterSetCode>
				</app:characterSet>
			</xsl:if>
			<app:dateStamp>
				<xsl:value-of select="smXML:dateStamp/smXML:DateTime" />
				<xsl:value-of select="smXML:dateStamp/smXML:Date" />
			</app:dateStamp>
			<xsl:variable name="metaName" select="smXML:metadataStandardName/smXML:CharacterString" />
			<xsl:if test="$metaName != ''">
				<app:metadataStandardName>
					<xsl:value-of select="$metaName" />
				</app:metadataStandardName>
			</xsl:if>
			<xsl:variable name="metaVersion" select="smXML:metadataStandardVersion/smXML:CharacterString" />
			<xsl:if test="$metaVersion != ''">
				<app:metadataStandardVersion>
					<xsl:value-of select="$metaVersion" />
				</app:metadataStandardVersion>
			</xsl:if>
			<xsl:apply-templates select="smXML:hierarchyLevel" />
			<xsl:apply-templates select="smXML:hierarchyLevelName" />
		</app:MD_Metadata>
	</xsl:template>
	<xsl:template match="smXML:hierarchyLevel">
		<xsl:variable name="HL">
			<xsl:value-of select="smXML:MD_ScopeCode/@codeListValue" />
		</xsl:variable>
		<app:hierarchyLevelCode>
			<app:HierarchyLevelCode>
				<app:codelistvalue>
					<xsl:choose>
						<xsl:when test="$HL = 'service'">service</xsl:when>
						<xsl:otherwise>invalid hierarchylevel! must be csw:service</xsl:otherwise>
					</xsl:choose>
				</app:codelistvalue>
			</app:HierarchyLevelCode>
		</app:hierarchyLevelCode>
	</xsl:template>
	<xsl:template match="smXML:hierarchyLevelName">
		<app:hierarchyLevelName>
			<app:HierarchyLevelName>
				<app:name>
					<xsl:value-of select="smXML:CharacterString" />
				</app:name>
			</app:HierarchyLevelName>
		</app:hierarchyLevelName>
	</xsl:template>
	<xsl:template match="smXML:referenceSystemInfo">
		<app:referenceSystemInfo>
			<app:RS_Identifier>
				<xsl:apply-templates
					select="smXML:MD_ReferenceSystem/smXML:referenceSystemIdentifier/smXML:RS_Identifier" />
			</app:RS_Identifier>
		</app:referenceSystemInfo>
	</xsl:template>
	<xsl:template match="iso19119:CSW_ServiceIdentification">
		<app:serviceIdentification>
			<app:CSW_ServiceIdentification>
				<app:identificationInfo>
					<app:MD_Identification>
						<xsl:for-each select="smXML:status">
							<app:status>
								<app:MD_ProgressCode>
									<app:codelistvalue>
										<xsl:value-of select="smXML:MD_ProgressCode/@codeListValue" />
									</app:codelistvalue>
								</app:MD_ProgressCode>
							</app:status>
						</xsl:for-each>
						<xsl:for-each select="smXML:citation">
							<app:citation>
								<xsl:apply-templates select="smXML:CI_Citation">
									<xsl:with-param name="context">Identification</xsl:with-param>
								</xsl:apply-templates>
							</app:citation>
						</xsl:for-each>
						<xsl:for-each select="smXML:pointOfContact">
							<app:pointOfContact>
								<xsl:apply-templates select="smXML:CI_ResponsibleParty" />
							</app:pointOfContact>
						</xsl:for-each>
						<xsl:for-each select="smXML:resourceSpecificUsage">
							<app:resourceSpecificUsage>
								<xsl:apply-templates select="smXML:MD_Usage" />
							</app:resourceSpecificUsage>
						</xsl:for-each>
						<xsl:for-each select="smXML:resourceMaintenance">
							<app:resourceMaintenance>
								<xsl:apply-templates select="smXML:MD_MaintenanceInformation" />
							</app:resourceMaintenance>
						</xsl:for-each>
						<xsl:for-each select="smXML:MD_DataIdentification/smXML:resourceConstraints">
							<xsl:if test="smXML:MD_LegalConstraints">
								<app:legalConstraints>
									<xsl:apply-templates select="smXML:MD_LegalConstraints" />
								</app:legalConstraints>
							</xsl:if>
							<xsl:if test="smXML:MD_SecurityConstraints">
								<app:securityConstraints>
									<xsl:apply-templates select="smXML:MD_SecurityConstraints" />
								</app:securityConstraints>
							</xsl:if>
						</xsl:for-each>
						<xsl:for-each select="smXML:descriptiveKeywords">
							<app:descriptiveKeywords>
								<xsl:apply-templates select="smXML:MD_Keywords" />
							</app:descriptiveKeywords>
						</xsl:for-each>
						<xsl:if test="boolean(smXML:abstract)">
							<app:abstract>
								<xsl:value-of select="smXML:abstract/smXML:CharacterString" />
							</app:abstract>
						</xsl:if>
						<xsl:if test="boolean(smXML:purpose)">
							<app:purpose>
								<xsl:value-of select="smXML:purpose/smXML:CharacterString" />
							</app:purpose>
						</xsl:if>
					</app:MD_Identification>
				</app:identificationInfo>
				<app:servicetype>
					<xsl:value-of select="iso19119:serviceType/smXML:CharacterString" />
				</app:servicetype>
				<xsl:for-each select="iso19119:serviceTypeVersion">
					<app:serviceTypeVersion>
						<xsl:value-of select="smXML:CharacterString" />
					</app:serviceTypeVersion>
				</xsl:for-each>
				<xsl:apply-templates select="iso19119:accessProperties" />
				<xsl:if test="boolean( iso19119:restrictions/smXML:MD_LegalConstraints )">
					<app:restrictionsLegal>
						<xsl:apply-templates select="iso19119:restrictions/smXML:MD_LegalConstraints" />
					</app:restrictionsLegal>
				</xsl:if>
				<xsl:if test="boolean( iso19119:restrictions/smXML:MD_SecurityConstraints )">
					<app:restrictionsSecurity>
						<xsl:apply-templates select="iso19119:restrictions/smXML:MD_SecurityConstraints" />
					</app:restrictionsSecurity>
				</xsl:if>
				<app:couplingType>
					<app:CSW_CouplingType>
						<app:codelistvalue>
							<xsl:value-of select="iso19119:couplingType/iso19119:CSW_CouplingType/@codeListValue" />
						</app:codelistvalue>
					</app:CSW_CouplingType>
				</app:couplingType>
				<xsl:for-each select="iso19119:operationMetadata">
					<app:operationMetadata>
						<xsl:apply-templates select="iso19119:SV_OperationMetadata" />
					</app:operationMetadata>
				</xsl:for-each>
				<xsl:for-each
					select="iso19119:extent/smXML:EX_Extent/smXML:geographicElement/smXML:EX_GeographicBoundingBox">
					<xsl:call-template name="GEOEX" />
				</xsl:for-each>
				<!-- 
					<xsl:for-each select="iso19119:operatesOn">
					<app:operatesOn>
					<xsl:apply-templates select="." />
					</app:operatesOn>
					</xsl:for-each>
				-->
				<xsl:for-each select="iso19119:coupledResource/iso19119:CSW_CoupledResource">
					<app:coupledResource>
						<xsl:apply-templates select="." />
					</app:coupledResource>
				</xsl:for-each>
			</app:CSW_ServiceIdentification>
		</app:serviceIdentification>
	</xsl:template>
	<xsl:template match="iso19119:SV_OperationMetadata">
		<app:SV_OperationMetadata>
			<app:operationName>
				<app:OperationNames>
					<app:name>
						<xsl:value-of select="iso19119:operationName/smXML:CharacterString" />
					</app:name>
				</app:OperationNames>
			</app:operationName>
			<xsl:if test="boolean( iso19119:operationDescription )">
				<app:operationDescription>
					<xsl:value-of select="iso19119:operationDescription/smXML:CharacterString" />
				</app:operationDescription>
			</xsl:if>
			<xsl:if test="boolean( iso19119:invocationName ) ">
				<app:invocationName>
					<xsl:value-of select="iso19119:invocationName/smXML:CharacterString" />
				</app:invocationName>
			</xsl:if>
			<xsl:for-each select="iso19119:parameters">
				<app:parameters>
					<xsl:apply-templates select="iso19119:SV_Parameter" />
				</app:parameters>
			</xsl:for-each>
			<xsl:for-each select="iso19119:connectPoint">
				<app:connectPoint>
					<xsl:apply-templates select="smXML:CI_OnlineResource" />
				</app:connectPoint>
			</xsl:for-each>
			<xsl:for-each select="iso19119:DCP">
				<app:DCP>
					<app:SV_DCPList>
						<app:codelistvalue>
							<xsl:value-of select="iso19119:SV_DCPList/@codeListValue" />
						</app:codelistvalue>
					</app:SV_DCPList>
				</app:DCP>
			</xsl:for-each>
		</app:SV_OperationMetadata>
	</xsl:template>
	<xsl:template match="iso19119:SV_Parameter">
		<app:SV_Parameter>
			<app:name>
				<xsl:value-of select="iso19119:name/smXML:MemberName/smXML:aName/smXML:CharacterString" />
			</app:name>
			<app:type>
				<xsl:value-of
					select="iso19119:name/smXML:MemberName/smXML:attributeType/smXML:TypeName/smXML:aName/smXML:CharacterString" />
			</app:type>
			<app:direction>
				<xsl:value-of select="iso19119:direction/iso19119:SV_ParameterDirection" />
			</app:direction>
			<app:description>
				<xsl:value-of select="iso19119:description/smXML:CharacterString" />
			</app:description>
			<app:optionality>
				<xsl:value-of select="iso19119:optionality/smXML:CharacterString" />
			</app:optionality>
			<app:repeatability>
				<xsl:value-of select="iso19119:repeatability/smXML:Boolean" />
			</app:repeatability>
		</app:SV_Parameter>
	</xsl:template>
	<xsl:template match="iso19119:operatesOn">
		<xsl:if test="boolean( smXML:Reference)">
			<app:dataidentification>
				<xsl:value-of select="smXML:Reference/@uuidref" />
			</app:dataidentification>
		</xsl:if>
		<xsl:if test="boolean( smXML:MD_DataIdentification  )">
			<app:OperatesOn>
				<!-- <app:citation>
					<xsl:apply-templates select="smXML:MD_DataIdentification/smXML:citation/smXML:CI_Citation"/>	
					</app:citation> -->
				<app:name>
					<xsl:value-of
						select="smXML:MD_DataIdentification/smXML:citation/smXML:CI_Citation/smXML:identifier/smXML:MD_Identifier/smXML:code/smXML:CharacterString" />
				</app:name>
				<app:title>
					<xsl:value-of
						select="smXML:MD_DataIdentification/smXML:citation/smXML:CI_Citation/smXML:title/smXML:CharacterString" />
				</app:title>
				<app:abstract>
					<xsl:value-of select="smXML:MD_DataIdentification/smXML:abstract/smXML:CharacterString" />
				</app:abstract>
			</app:OperatesOn>
		</xsl:if>
	</xsl:template>
	<xsl:template match="iso19119:CSW_CoupledResource">
		<app:CoupledResource>
			<app:operationName>
				<app:OperationNames>
					<app:name>
						<xsl:value-of select="iso19119:operationName/smXML:CharacterString" />
					</app:name>
				</app:OperationNames>
			</app:operationName>
			<xsl:for-each select="iso19119:identifier">
				<app:identifier>
					<xsl:value-of select="smXML:CharacterString" />
				</app:identifier>
			</xsl:for-each>
		</app:CoupledResource>
	</xsl:template>
	<xsl:template match="iso19119:accessProperties">
		<xsl:if test="boolean(smXML:fees)">
			<app:fees>
				<xsl:value-of select="smXML:fees/smXML:CharacterString" />
			</app:fees>
		</xsl:if>
		<xsl:if test="boolean(smXML:plannedAvailableDatetime)">
			<app:plannedAvailableDatetime>
				<xsl:value-of select="smXML:plannedAvailableDatetime/smXML:date/smXML:DateTime" />
			</app:plannedAvailableDatetime>
		</xsl:if>
		<xsl:if test="boolean(smXML:orderingInstructions)">
			<app:orderingInstructions>
				<xsl:value-of select="smXML:orderingInstructions/smXML:CharacterString" />
			</app:orderingInstructions>
		</xsl:if>
		<xsl:if test="boolean(smXML:turnaround)">
			<app:turnaround>
				<xsl:value-of select="smXML:turnaround/smXML:CharacterString" />
			</app:turnaround>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
