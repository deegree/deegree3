<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:app="http://www.deegree.org/app" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:wfs="http://www.opengis.net/wfs" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:couple="org.deegree.ogcwebservices.csw.iso_profile.Coupling" version="1.0">
	
	<xsl:template name="METADATA">
		<app:MD_Metadata>
			<xsl:for-each select="gmd:MD_Metadata/gmd:referenceSystemInfo">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<xsl:for-each select="gmd:MD_Metadata/gmd:contact">
				<app:contact>
					<xsl:apply-templates select="gmd:CI_ResponsibleParty"/>
				</app:contact>
			</xsl:for-each>
			<xsl:apply-templates select="gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution"/>
			<xsl:for-each select="gmd:MD_Metadata/gmd:contentInfo/gmd:MD_FeatureCatalogueDescription">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<xsl:choose>
				<xsl:when test="boolean( gmd:MD_Metadata/gmd:identificationInfo != '' )">
					<app:dataIdentification>
						<xsl:apply-templates select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification"/>
					</app:dataIdentification>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message>
						------------- Mandatory Element 'gmd:identificationInfo' missing! ----------------
					</xsl:message>
					<app:exception>Mandatory Element 'gmd:identificationInfo' missing!</app:exception>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="gmd:MD_Metadata/gmd:portrayalCatalogueInfo/gmd:MD_PortrayalCatalogueReference"/>
			<xsl:for-each select="gmd:MD_Metadata/gmd:spatialRepresentationInfo">
				<app:spatialReprenstationInfo>
					<xsl:apply-templates select="gmd:MD_VectorSpatialRepresentation"/>
				</app:spatialReprenstationInfo>
			</xsl:for-each>
			<xsl:for-each select="gmd:MD_Metadata/gmd:dataQualityInfo">
				<app:dataQualityInfo>
					<xsl:apply-templates select="gmd:DQ_DataQuality"/>
				</app:dataQualityInfo>
			</xsl:for-each>
			<xsl:variable name="fileIdentifier" select="gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"/>
			<xsl:if test="boolean( $fileIdentifier != '' )">
				<app:fileidentifier>
					<xsl:value-of select="$fileIdentifier"/>
				</app:fileidentifier>
			</xsl:if>
			<xsl:if test="boolean( gmd:MD_Metadata/gmd:language/gco:CharacterString != '' )">
				<app:language>
					<xsl:value-of select="gmd:MD_Metadata/gmd:language/gco:CharacterString"/>
				</app:language>
			</xsl:if>
      <xsl:if test="boolean( gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue != '' )">
    	  <app:language>
        	<xsl:value-of select="gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>
        </app:language>
      </xsl:if>
			<xsl:if test="boolean( gmd:MD_Metadata/gmd:characterSet )">
				<app:characterSet>
					<app:MD_CharacterSetCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:MD_Metadata/gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue"/>
						</app:codelistvalue>
					</app:MD_CharacterSetCode>
				</app:characterSet>
			</xsl:if>
			<xsl:if test="boolean( gmd:MD_Metadata/gmd:parentIdentifier )">
				<app:parentidentifier>
					<xsl:value-of select="gmd:MD_Metadata/gmd:parentIdentifier/gco:CharacterString"/>
				</app:parentidentifier>
			</xsl:if>
			<xsl:variable name="date" select="gmd:MD_Metadata/gmd:dateStamp/gco:Date"/>
			<xsl:variable name="dateTime" select="gmd:MD_Metadata/gmd:dateStamp/gco:DateTime"/>
			<xsl:if test="boolean( $dateTime != '' or $date != '' ) and 
						  not( boolean( $dateTime and $date ) )">
				<app:dateStamp>
					<xsl:value-of select="$dateTime"/>
					<xsl:value-of select="$date"/>
				</app:dateStamp>
			</xsl:if>
			<xsl:variable name="metaName" select="gmd:MD_Metadata/gmd:metadataStandardName/gco:CharacterString"/>
			<xsl:if test="$metaName != ''">
				<app:metadataStandardName>
					<xsl:value-of select="$metaName"/>
				</app:metadataStandardName>
			</xsl:if>
			<xsl:variable name="metaVersion" select="gmd:MD_Metadata/gmd:metadataStandardVersion/gco:CharacterString"/>
			<xsl:if test="$metaVersion != ''">
				<app:metadataStandardVersion>
					<xsl:value-of select="$metaVersion"/>
				</app:metadataStandardVersion>
			</xsl:if>
      <xsl:choose>
        <xsl:when test="boolean( count(gmd:MD_Metadata/gmd:hierarchyLevel) > 0 )">
            <xsl:for-each select="gmd:MD_Metadata/gmd:hierarchyLevel">
                <xsl:call-template name="MDHIERARCHYLEVEL"/>
            </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
            <!-- execute at least one time, to set default value! -->
            <xsl:call-template name="MDHIERARCHYLEVEL"/>
        </xsl:otherwise>
      </xsl:choose>
			<xsl:for-each select="gmd:MD_Metadata/gmd:hierarchyLevelName">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<xsl:for-each select="gmd:MD_Metadata/gmd:metadataConstraints">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
			<xsl:for-each select="gmd:MD_Metadata/gmd:applicationSchemaInfo">
				<xsl:apply-templates select="gmd:MD_ApplicationSchemaInformation"/>
			</xsl:for-each>
			<xsl:for-each select="gmd:MD_Metadata/gmd:locale">
				<xsl:apply-templates select="gmd:PT_Locale"/>
			</xsl:for-each>
			<xsl:call-template name="CQP"/>

		</app:MD_Metadata>
	</xsl:template>
	
	<xsl:template match="gmd:metadataConstraints">
		<xsl:if test="boolean( gmd:MD_LegalConstraints )">
			<app:legalConstraints>
				<xsl:apply-templates select="gmd:MD_LegalConstraints"/>
			</app:legalConstraints>
		</xsl:if>
		<xsl:if test="boolean( gmd:MD_SecurityConstraints )">
			<app:securityConstraints>
				<xsl:apply-templates select="gmd:MD_SecurityConstraints"/>
			</app:securityConstraints>
		</xsl:if>
        <xsl:if test="boolean( gmd:MD_Constraints )">
            <app:constraints>
                <xsl:apply-templates select="gmd:MD_Constraints"/>
            </app:constraints>
        </xsl:if>
	</xsl:template>
	<xsl:template name="MDHIERARCHYLEVEL">
		<xsl:variable name="HL">
			<xsl:value-of select="gmd:MD_ScopeCode/@codeListValue"/>
		</xsl:variable>
		<app:hierarchyLevelCode>
			<app:HierarchyLevelCode>
				<app:codelistvalue>
					<xsl:choose>
						<xsl:when test="$HL = 'series'">
							series
						</xsl:when>
						<xsl:when test="$HL = 'application'">
							application
						</xsl:when>
                        <!-- default is dataset -->
						<xsl:otherwise>
							dataset
						</xsl:otherwise>
					</xsl:choose>
				</app:codelistvalue>
			</app:HierarchyLevelCode>
		</app:hierarchyLevelCode>
	</xsl:template>
	<xsl:template match="gmd:hierarchyLevelName">
		<xsl:if test=" gco:CharacterString != ''">
			<app:hierarchyLevelName>
				<app:HierarchyLevelName>
					<app:name>
						<xsl:value-of select="gco:CharacterString"/>
					</app:name>
				</app:HierarchyLevelName>
			</app:hierarchyLevelName>
		</xsl:if>
	</xsl:template>
	<xsl:template match="gmd:referenceSystemInfo">
		<app:referenceSystemInfo>
			<app:RS_Identifier>
				<xsl:apply-templates select="gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier"/>
			</app:RS_Identifier>
		</app:referenceSystemInfo>
	</xsl:template>
	<xsl:template match="gmd:MD_DataIdentification">
			<app:MD_DataIdentification>
				<app:identificationInfo>
					<app:MD_Identification>
						<xsl:for-each select="gmd:status">
							<app:status>
								<app:MD_ProgressCode>
									<app:codelistvalue>
										<xsl:value-of select="gmd:MD_ProgressCode/@codeListValue"/>
									</app:codelistvalue>
								</app:MD_ProgressCode>
							</app:status>
                        </xsl:for-each>
						<xsl:if test="boolean( gmd:citation )">
							<app:citation>
								<xsl:apply-templates select="gmd:citation/gmd:CI_Citation">
									<xsl:with-param name="context">
										Identification
									</xsl:with-param>
								</xsl:apply-templates>
							</app:citation>
						</xsl:if>
						<xsl:for-each select="gmd:pointOfContact">
							<app:pointOfContact>
								<xsl:apply-templates select="gmd:CI_ResponsibleParty"/>
							</app:pointOfContact>
						</xsl:for-each>
						<xsl:for-each select="gmd:resourceSpecificUsage">
							<app:resourceSpecificUsage>
								<xsl:apply-templates select="gmd:MD_Usage"/>
							</app:resourceSpecificUsage>
						</xsl:for-each>
						<xsl:for-each select="gmd:resourceMaintenance">
							<app:resourceMaintenance>
								<xsl:apply-templates select="gmd:MD_MaintenanceInformation"/>
							</app:resourceMaintenance>
						</xsl:for-each>
						<xsl:for-each select="gmd:graphicOverview/gmd:MD_BrowseGraphic">
							<app:graphicOverview>
								<xsl:apply-templates select="."/>
							</app:graphicOverview>
						</xsl:for-each>
						<xsl:for-each select="gmd:resourceConstraints/gmd:MD_LegalConstraints">
							<xsl:if test="boolean( . )">
								<app:legalConstraints>
									<xsl:apply-templates select="."/>
								</app:legalConstraints>
							</xsl:if>
                        </xsl:for-each>
                        <xsl:for-each select="gmd:resourceConstraints/gmd:MD_SecurityConstraints">
							<xsl:if test="boolean( . )">
								<app:securityConstraints>
									<xsl:apply-templates select="."/>
								</app:securityConstraints>
							</xsl:if>
                        </xsl:for-each>
                        <xsl:for-each select="gmd:resourceConstraints/gmd:MD_Constraints">
                            <xsl:if test="boolean( . )">
                                <app:constraints>
                                    <xsl:apply-templates select="."/>
                                </app:constraints>
                            </xsl:if>
                        </xsl:for-each>
						<xsl:for-each select="gmd:descriptiveKeywords">
							<app:descriptiveKeywords>
								<xsl:apply-templates select="gmd:MD_Keywords"/>
							</app:descriptiveKeywords>
						</xsl:for-each>
						<xsl:variable name="abstract" select="gmd:abstract/gco:CharacterString"/>
						<xsl:if test="boolean( $abstract != '' )">
							<app:abstract>
								<xsl:value-of select="$abstract"/>
							</app:abstract>
						</xsl:if>
						<xsl:variable name="purpose" select="gmd:purpose/gco:CharacterString"/>
						<xsl:if test="boolean( $purpose != '' )">
							<app:purpose>
								<xsl:value-of select="$purpose"/>
							</app:purpose>
						</xsl:if>
					</app:MD_Identification>
				</app:identificationInfo>
				<xsl:for-each select="gmd:topicCategory">
					<app:topicCategory>
						<app:MD_TopicCategoryCode>
							<app:category>
								<xsl:value-of select="gmd:MD_TopicCategoryCode"/>
							</app:category>
						</app:MD_TopicCategoryCode>
					</app:topicCategory>
				</xsl:for-each>
				<xsl:for-each select="gmd:spatialRepresentationType">
					<app:spatialRepresentationType>
						<app:MD_SpatialRepTypeCode>
							<app:codelistvalue>
								<xsl:value-of select="gmd:MD_SpatialRepresentationTypeCode/@codeListValue"/>
							</app:codelistvalue>
						</app:MD_SpatialRepTypeCode>
					</app:spatialRepresentationType>
				</xsl:for-each>
				<xsl:for-each select="gmd:spatialResolution">
					<app:spatialResolution>
						<xsl:apply-templates select="gmd:MD_Resolution"/>
					</app:spatialResolution>
				</xsl:for-each>
				<xsl:for-each select="gmd:extent/gmd:EX_Extent/gmd:verticalElement">
					<app:verticalExtent>
						<xsl:call-template name="VERTEX"/>
					</app:verticalExtent>
				</xsl:for-each>
				<xsl:for-each select="gmd:extent/gmd:EX_Extent/gmd:temporalElement">
					<app:temportalExtent>
						<xsl:call-template name="TEMPEX"/>
					</app:temportalExtent>
				</xsl:for-each>
				<xsl:for-each select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
					<xsl:call-template name="GEOEX"/>
				</xsl:for-each>
				<xsl:for-each select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicDescription">
					<xsl:call-template name="GEODESCEX"/>
				</xsl:for-each>
				<xsl:for-each select="gmd:language">
					<xsl:if test="boolean( gco:CharacterString != '' )">
						<app:language>
							<xsl:value-of select="gco:CharacterString"/>
						</app:language>
					</xsl:if>
          <xsl:if test="boolean( gmd:LanguageCode/@codeListValue != '' )">
            <app:language>
                <xsl:value-of select="gmd:LanguageCode/@codeListValue"/>
            </app:language>
          </xsl:if>
				</xsl:for-each>
				<xsl:variable name="supplementalInformation" select="gmd:supplementalInformation/gco:CharacterString"/>
				<xsl:if test="boolean( $supplementalInformation != '' )">
					<app:supplementalInformation>
						<xsl:value-of select="$supplementalInformation"/>
					</app:supplementalInformation>
				</xsl:if>
				<xsl:for-each select="gmd:characterSet/gmd:MD_CharacterSetCode/@codeListValue">
					<xsl:if test="boolean( . != '' )">
						<app:characterSet>
							<app:MD_CharacterSetCode>
								<app:codelistvalue>
									<xsl:value-of select="."/>
								</app:codelistvalue>
							</app:MD_CharacterSetCode>
						</app:characterSet>
					</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="gmd:aggregationInfo/gmd:MD_AggregateInformation">
					<xsl:variable name="assTypeValue" select="gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue" />
					<xsl:variable name="assType" select="gmd:associationType/gmd:DS_AssociationTypeCode/@codeList" />
					<xsl:if test="boolean( $assTypeValue != '' ) and boolean($assType != '' )">
						<app:aggregationInfo>
							<app:MD_AggregateInfo>
								<xsl:if test="boolean( gmd:aggregateDataSetName/gmd:CI_Citation != '' )">
									<app:aggregateDataSetName>
										<xsl:apply-templates select="gmd:aggregateDataSetName/gmd:CI_Citation" >
											<xsl:with-param name="context">AggregateInfo</xsl:with-param>
										</xsl:apply-templates>
									</app:aggregateDataSetName>
								</xsl:if>
								<app:associationType>
									<app:DS_AssociationTypeCode>
										<app:codelistvalue>
											<xsl:value-of select="$assTypeValue" />
										</app:codelistvalue>
									</app:DS_AssociationTypeCode>
								</app:associationType>
							</app:MD_AggregateInfo>
						</app:aggregationInfo>
					</xsl:if>
				</xsl:for-each>
				<!-- select uuid or first citation.identifier which is unequal to the fileIdentifier; otherwise create new id/uuid-->
				<xsl:variable name="uuid">
					<xsl:choose>
						<xsl:when test="boolean( @uuid != '' )">
							<xsl:value-of select="couple:getValidId( @uuid )" />
						</xsl:when>
						<xsl:when test="boolean(gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString[. != ../../../../../../../../gmd:fileIdentifier/gco:CharacterString ][1] != '' )">
							<xsl:value-of select="gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString[. != ../../../../../../../../gmd:fileIdentifier/gco:CharacterString ][1]" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="couple:createValidId()" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<app:uuid>
					<xsl:value-of select="$uuid" />
				</app:uuid>
			</app:MD_DataIdentification>
	</xsl:template>
	<xsl:template match="gmd:MD_BrowseGraphic">
		<app:MD_BrowseGraphic>
			<xsl:variable name="fileName" select="gmd:fileName/gco:CharacterString"/>
			<xsl:if test="boolean( $fileName != '' ) ">
				<app:filename>
					<xsl:value-of select="$fileName"/>
				</app:filename>
			</xsl:if>
			<xsl:variable name="fileDescription" select="gmd:fileDescription/gco:CharacterString"/>
			<xsl:if test="boolean( $fileDescription != '' )">
				<app:filedescription>
					<xsl:value-of select="$fileDescription"/>
				</app:filedescription>
			</xsl:if>
			<xsl:variable name="fileType" select="gmd:fileType/gco:CharacterString"/>
			<xsl:if test="boolean( $fileType != '' )">
				<app:filetype>
					<xsl:value-of select="$fileType"/>
				</app:filetype>
			</xsl:if>
		</app:MD_BrowseGraphic>
	</xsl:template>
	<xsl:template match="gmd:MD_VectorSpatialRepresentation">
		<app:MD_VectorSpatialReprenstation>
			<xsl:if test="boolean( gmd:topologyLevel )">
				<app:topoLevelCode>
					<app:MD_TopoLevelCode>
						<app:codelistvalue>
							<xsl:value-of select="gmd:topologyLevel/gmd:MD_TopologyLevelCode/@codeListValue"/>
						</app:codelistvalue>
					</app:MD_TopoLevelCode>
				</app:topoLevelCode>
			</xsl:if>
			<xsl:if test="boolean( gmd:geometricObjects )">
				<app:geoTypeObjectTypeCode>
					<app:MD_GeoObjTypeCode>
						<app:codelistvalue>
							<xsl:apply-templates select="gmd:geometricObjects/gmd:MD_GeometricObjects/gmd:geometricObjectType/gmd:MD_GeometricObjectTypeCode/@codeListValue"/>
						</app:codelistvalue>
					</app:MD_GeoObjTypeCode>
				</app:geoTypeObjectTypeCode>
				<xsl:if test="boolean( gmd:geometricObjects/gmd:MD_GeometricObjects/gmd:geometricObjectCount ) ">
					<app:geoobjcount>
						<xsl:value-of select="gmd:geometricObjects/gmd:MD_GeometricObjects/gmd:geometricObjectCount/gco:Integer"/>
					</app:geoobjcount>
				</xsl:if>
			</xsl:if>
		</app:MD_VectorSpatialReprenstation>
	</xsl:template>
	<xsl:template match="gmd:DQ_DataQuality">
		<app:DQ_DataQuality>
			<xsl:apply-templates select="gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy | gmd:report/gmd:DQ_CompletenessCommission | gmd:report/gmd:DQ_CompletenessOmission | gmd:report/gmd:DQ_DomainConsistency"/>
			<xsl:if test="boolean( gmd:scope/gmd:DQ_Scope )">
				<app:scopelevelcodelistvalue>
					<xsl:value-of select="gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue"/>
				</app:scopelevelcodelistvalue>
			</xsl:if>
			<xsl:if test="boolean( gmd:lineage/gmd:LI_Lineage/gmd:statement )">
				<app:lineagestatement>
					<xsl:value-of select="gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString"/>
				</app:lineagestatement>
			</xsl:if>
			<xsl:for-each select="gmd:lineage/gmd:LI_Lineage/gmd:source">
				<xsl:apply-templates select="gmd:LI_Source"/>
			</xsl:for-each>
			<xsl:for-each select="gmd:lineage/gmd:LI_Lineage/gmd:processStep">
				<xsl:call-template name="PROCSTEP"/>
			</xsl:for-each>
		</app:DQ_DataQuality>
	</xsl:template>
	<xsl:template match="gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source">
		<app:LI_Source>
			<app:LI_Source>
				<xsl:if test="boolean( gmd:description )">
					<app:description>
						<xsl:value-of select="gmd:description/gco:CharacterString"/>
					</app:description>
				</xsl:if>
				<xsl:if test="boolean( gmd:scaleDenominator )">
					<app:scaleDenominator>
						<xsl:value-of select="gmd:scaleDenominator/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer"/>
					</app:scaleDenominator>
				</xsl:if>
				<xsl:if test="boolean( gmd:sourceCitation )">
					<app:sourceCitation>
						<xsl:apply-templates select="gmd:sourceCitation/gmd:CI_Citation">
							<xsl:with-param name="context">
								Source
							</xsl:with-param>
						</xsl:apply-templates>
					</app:sourceCitation>
				</xsl:if>
				<xsl:if test="boolean( gmd:sourceReferenceSystem )">
					<app:sourceReferenceSystem>
						<app:RS_Identifier>
							<xsl:apply-templates select="gmd:sourceReferenceSystem/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier"/>
						</app:RS_Identifier>
					</app:sourceReferenceSystem>
				</xsl:if>
				<xsl:for-each select="gmd:sourceStep">
					<app:sourceStep>
						<xsl:apply-templates select="gmd:LI_ProcessStep"/>
					</app:sourceStep>
				</xsl:for-each>
			</app:LI_Source>
		</app:LI_Source>
	</xsl:template>
	<xsl:template name="PROCSTEP">
		<app:LI_ProcessStep>
			<xsl:apply-templates select="gmd:LI_ProcessStep"/>
		</app:LI_ProcessStep>
	</xsl:template>
	<xsl:template match="gmd:LI_ProcessStep">
		<app:LI_ProcessStep>
			<xsl:variable name="description" select="gmd:description/gco:CharacterString"/>
			<xsl:if test="boolean( $description != '' )">
				<app:description>
					<xsl:value-of select="$description"/>
				</app:description>
			</xsl:if>
			<xsl:variable name="rationale" select="gmd:rationale/gco:CharacterString"/>
			<xsl:if test="boolean( $rationale != '' )">
				<app:rationale>
					<xsl:value-of select="$rationale"/>
				</app:rationale>
			</xsl:if>
			<xsl:variable name="DateTime" select="gmd:dateTime/gco:DateTime"/>
			<xsl:if test="boolean( $DateTime != '' )">
				<app:dateTime>
					<xsl:value-of select="$DateTime"/>
				</app:dateTime>
			</xsl:if>
			<xsl:for-each select="gmd:processor">
				<app:processor>
					<xsl:apply-templates select="gmd:CI_ResponsibleParty"/>
				</app:processor>
			</xsl:for-each>
		</app:LI_ProcessStep>
	</xsl:template>
	
	<xsl:template match="gmd:DQ_AbsoluteExternalPositionalAccuracy | gmd:DQ_CompletenessCommission | gmd:DQ_CompletenessOmission  | gmd:DQ_DomainConsistency">
    
        <!-- quantitative or qualitative result are mandatory !!! -->
        <xsl:variable name="quantidentifier" select="gmd:result/gmd:DQ_QuantitativeResult/gmd:valueUnit/gml:UnitDefinition/gml:identifier"/>
        <xsl:variable name="quantvalue" select="gmd:result/gmd:DQ_QuantitativeResult/gmd:value/gco:Record"/>
          
        <xsl:variable name="confspecification" select="gmd:result/gmd:DQ_ConformanceResult/gmd:specification"/>
        <xsl:variable name="confexplanation" select="gmd:result/gmd:DQ_ConformanceResult/gmd:explanation/gco:CharacterString"/>
        <xsl:variable name="confpass" select="gmd:result/gmd:DQ_ConformanceResult/gmd:pass/gco:Boolean"/>
        
        <xsl:if test="(boolean( $quantvalue != '' ) and boolean( $quantidentifier/@codeSpace != '' ) ) or ( boolean( $confspecification != '' ) and boolean( $confexplanation != ''  ) and boolean ( $confpass != '' ) )">
    		<app:DQ_Element>
    			<app:DQ_Element>
    				<xsl:if test="boolean( gmd:nameOfMeasure )">
    					<app:nameofmeasure>
    						<xsl:value-of select="gmd:nameOfMeasure/gco:CharacterString"/>
    					</app:nameofmeasure>
    				</xsl:if>
    				<xsl:if test="boolean( gmd:measureIdentification/gmd:RS_Identifier/gmd:code/gco:CharacterString )">
    					<app:measureidentcode>
    						<xsl:value-of select="gmd:measureIdentification/gmd:RS_Identifier/gmd:code/gco:CharacterString"/>
    					</app:measureidentcode>
    				</xsl:if>
    				<xsl:if test="boolean( gmd:measureIdentification/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString )">
    					<app:measureidentcodespace>
    						<xsl:value-of select="gmd:measureIdentification/gmd:RS_Identifier/gmd:codeSpace/gco:CharacterString"/>
    					</app:measureidentcodespace>
    				</xsl:if>
    				<xsl:for-each select="gmd:result">
    					<xsl:if test="position() = 1 or position() = 2">
    						<xsl:apply-templates select="gmd:DQ_QuantitativeResult" />
    					</xsl:if>
    				</xsl:for-each>
    				<xsl:for-each select="gmd:result">
    					<xsl:if test="position() = 1 or position() = 2">
    						<xsl:apply-templates select="gmd:DQ_ConformanceResult" />
    					</xsl:if>				
    				</xsl:for-each>
    				<app:type>
    					<xsl:value-of select="local-name(.)"/>
    				</app:type>
    			</app:DQ_Element>
    		</app:DQ_Element>
        </xsl:if>
	</xsl:template>

	<xsl:template match="gmd:DQ_QuantitativeResult">
		<xsl:variable name="identifier" select="gmd:valueUnit/gml:UnitDefinition/gml:identifier"/>
		<xsl:variable name="value" select="gmd:value/gco:Record"/>			
		<xsl:if test="boolean( $value != '' ) and boolean( $identifier/@codeSpace != '' )">
			<app:quantitativeResult>
				<app:DQ_QuantitativeResult>
					<app:identifier>
						<xsl:value-of select="$identifier"/>
					</app:identifier>
					<app:codeSpace>
						<xsl:value-of select="$identifier/@codeSpace"/>
					</app:codeSpace>
					<xsl:for-each select="gmd:value">
						<xsl:if test="boolean( gco:Record != '' )">
							<app:value>
								<xsl:value-of select="gco:Record"/>
							</app:value>
						</xsl:if>
					</xsl:for-each>
				</app:DQ_QuantitativeResult>
			</app:quantitativeResult>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="gmd:DQ_ConformanceResult">
		<xsl:variable name="specification" select="gmd:specification"/>
		<xsl:variable name="explanation" select="gmd:explanation/gco:CharacterString"/>
		<xsl:variable name="pass" select="gmd:pass/gco:Boolean"/>
		<xsl:if test=" boolean( $specification ) and boolean( $explanation != ''  ) and boolean ( $pass != '' )">
			<app:conformanceResult>
				<app:DQ_ConformanceResult>
					<app:explanation>
						<xsl:value-of select="$explanation"></xsl:value-of>
					</app:explanation>
					<app:pass>
						<xsl:value-of select="$pass"></xsl:value-of>
					</app:pass>
					<app:specification>
						<xsl:apply-templates select="$specification/gmd:CI_Citation">
								<xsl:with-param name="context">ConformanceResult</xsl:with-param>
							</xsl:apply-templates>
						</app:specification>
				</app:DQ_ConformanceResult>
			</app:conformanceResult>
		</xsl:if>
	</xsl:template>	
		
	
	<xsl:template name="CQP">	
		<app:commonQueryableProperties>
			<app:CQP_Main>
			
				<xsl:variable name="cqpKeywords" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword"/>
				<xsl:variable name="cqpTopc" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode"/>
				<xsl:if test="boolean( $cqpKeywords != '' ) or boolean( $cqpTopc != '' ) ">
					<app:subject>
						<xsl:for-each select="$cqpKeywords">
							<xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
						</xsl:for-each>
						<xsl:for-each select="$cqpTopc">
							<xsl:value-of select="concat( '|', ., '|' )"/>
						</xsl:for-each>
					</app:subject>
				</xsl:if>
	
				<app:title>
					<xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
				</app:title>
	
				<app:abstract>
					<xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"/>
				</app:abstract>
	
				<app:anyText>
					<!--
						this must be used if CSW in configured on Oracle as backend
					-->
					<xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
					<xsl:value-of select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString"/>
					<xsl:for-each select="$cqpTopc">
							<xsl:value-of select="."/>
						</xsl:for-each>
						<xsl:for-each select="$cqpKeywords">
							<xsl:value-of select="."/>
						</xsl:for-each>
					<!--xsl:value-of select="."/-->
				</app:anyText>
	
				<app:identifier>
					<xsl:value-of select="gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"/>
				</app:identifier>
	
				<app:modified>
					<xsl:value-of select="gmd:MD_Metadata/gmd:dateStamp/gco:DateTime"/>
					<xsl:value-of select="gmd:MD_Metadata/gmd:dateStamp/gco:Date"/>
				</app:modified>
	
				<app:type>
					<xsl:value-of select="gmd:MD_Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue"/>
				</app:type>
				
				<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date">
					<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision'">
						<app:revisionDate>
							<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date"/>
							<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime"/>
						</app:revisionDate>
					</xsl:if>
				</xsl:for-each>
				<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date">
					<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'creation'">
						<app:creationDate>
							<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date"/>
							<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime"/>
						</app:creationDate>
					</xsl:if>
				</xsl:for-each>					
				<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date">
					<xsl:if test="gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'publication'">
						<app:publicationDate>
							<xsl:value-of select="gmd:CI_Date/gmd:date/gco:Date"/>
							<xsl:value-of select="gmd:CI_Date/gmd:date/gco:DateTime"/>
						</app:publicationDate>
					</xsl:if>
				</xsl:for-each>
				
				<xsl:variable name="qcpAlternateTitle" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:alternateTitle"/>
				<xsl:if test="boolean( $qcpAlternateTitle != '' )">
					<app:alternateTitle>
						<xsl:for-each select="$qcpAlternateTitle">
							<xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
						</xsl:for-each>
					</app:alternateTitle>
				</xsl:if>
				
				<xsl:variable name="qcpResourceId" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code"/>
                <xsl:variable name="qcpResourceId2" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:RS_Identifier/gmd:code"/>
				<xsl:if test="boolean( $qcpResourceId != '' ) or boolean( $qcpResourceId2 != '' )">
					<app:resourceIdentifier>
						<xsl:for-each select="$qcpResourceId">
							<xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
						</xsl:for-each>
                        <xsl:for-each select="$qcpResourceId2">
                            <xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
                        </xsl:for-each>
					</app:resourceIdentifier>
				</xsl:if>
                
				<app:resourceLanguage>
					<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:language">
            <xsl:if test="boolean( gco:CharacterString != '' )">
                <xsl:value-of select="concat( '|', gco:CharacterString, '|' )"/>
            </xsl:if>
            <xsl:if test="boolean( gmd:LanguageCode/@codeListValue != '' )">
                <xsl:value-of select="concat( '|', gmd:LanguageCode/@codeListValue, '|' )"/>
            </xsl:if>
					</xsl:for-each>
				</app:resourceLanguage>
				
				<xsl:for-each select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox">
					<xsl:variable name="minx" select="gmd:westBoundLongitude/gco:Decimal"/>
					<xsl:variable name="maxx" select="gmd:eastBoundLongitude/gco:Decimal"/>
					<xsl:variable name="miny" select="gmd:southBoundLatitude/gco:Decimal"/>
					<xsl:variable name="maxy" select="gmd:northBoundLatitude/gco:Decimal"/>
					<app:bbox>
						<app:CQP_BBOX>
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
						</app:CQP_BBOX>
					</app:bbox>
				</xsl:for-each>
				
				<xsl:variable name="cqpTopcCat" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode"/>
				<xsl:if test="boolean( $cqpTopcCat != '' )">
					<app:topicCategory>
						<xsl:for-each select="$cqpTopcCat">
							<xsl:value-of select="concat( '|', ., '|' )"/>
						</xsl:for-each>
					</app:topicCategory>
				</xsl:if>
				
				<xsl:variable name="cqpParentID" select="gmd:MD_Metadata/gmd:parentIdentifier"/>
				<xsl:if test="boolean( $cqpParentID != '' )">
					<app:parentIdentifier>
							<xsl:value-of select="$cqpParentID"/>
					</app:parentIdentifier>
				</xsl:if>
                
                <xsl:if test="boolean( gmd:MD_Metadata/gmd:language/gco:CharacterString != '' )">
                    <app:language>
                        <xsl:value-of select="gmd:MD_Metadata/gmd:language/gco:CharacterString"/>
                    </app:language>
                </xsl:if>
                <xsl:if test="boolean( gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue != '' )">
                    <app:language>
                        <xsl:value-of select="gmd:MD_Metadata/gmd:language/gmd:LanguageCode/@codeListValue"/>
                    </app:language>
                </xsl:if>
                
                <xsl:variable name="cqpLineage" select="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement/gco:CharacterString"/>
                <xsl:if test="boolean( $cqpLineage != '' )">
                    <app:lineage>
                            <xsl:value-of select="$cqpLineage"/>
                    </app:lineage>
                </xsl:if>
                
                <!-- conditionApplyingToAccessAndUse  -->
                <xsl:variable name="cqpLimit" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_Constraints/gmd:useLimitation/gco:CharacterString"/>
                <xsl:variable name="cqpLimitLegal" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString"/>
                <xsl:variable name="cqpLimitSecurity" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:useLimitation/gco:CharacterString"/>
                <xsl:if test="boolean( $cqpLimit != '' ) or boolean( $cqpLimitLegal != '' ) or boolean( $cqpLimitSecurity != '' )">
                    <app:condAppToAccAndUse>
                        <xsl:for-each select="$cqpLimit">
                            <xsl:value-of select="concat( '|', ., '|' )"/>
                        </xsl:for-each>
                        <xsl:for-each select="$cqpLimitLegal">
                            <xsl:value-of select="concat( '|', ., '|' )"/>
                        </xsl:for-each>
                        <xsl:for-each select="$cqpLimitSecurity">
                            <xsl:value-of select="concat( '|', ., '|' )"/>
                        </xsl:for-each>
                    </app:condAppToAccAndUse>
                </xsl:if>
                
                <xsl:variable name="cqpAcessConstraints" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:accessConstraints/gmd:MD_RestrictionCode/@codeListValue"/>
                <xsl:if test="boolean( $cqpAcessConstraints != '' )">
                    <app:accessConstraints>
                        <xsl:for-each select="$cqpAcessConstraints">
                            <xsl:value-of select="concat( '|', ., '|' )"/>
                        </xsl:for-each>
                    </app:accessConstraints>
                </xsl:if>
                
                <xsl:variable name="cqpOtherConstraints" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:otherConstraints/gco:CharacterString"/>
                <xsl:if test="boolean( $cqpOtherConstraints != '' )">
                    <app:otherConstraints>
                        <xsl:for-each select="$cqpOtherConstraints">
                            <xsl:value-of select="concat( '|', ., '|' )"/>
                        </xsl:for-each>
                    </app:otherConstraints>
                </xsl:if>
                
                <xsl:variable name="cqpClassification" select="gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_SecurityConstraints/gmd:classification/gmd:MD_ClassificationCode/@codeListValue"/>
                <xsl:if test="boolean( $cqpClassification != '' )">
                    <app:classification>
                        <xsl:for-each select="$cqpClassification">
                            <xsl:value-of select="concat( '|', ., '|' )"/>
                        </xsl:for-each>
                    </app:classification>
                </xsl:if>
                
                <!-- nur service
                <xsl:variable name="cqpOperatesOn" select=""/>
                <xsl:if test="boolean( $cqpOperatesOn != '' )">
                    <app:operatesOn>
                        <xsl:for-each select="$cqpOperatesOn">
                            <xsl:value-of select="concat( '|', , '|' )"/>
                        </xsl:for-each>
                    </app:operatesOn>
                </xsl:if>
                
                <xsl:variable name="cqpCouplingType" select=""/>
                <xsl:if test="boolean( $cqpCouplingType != '' )">
                    <app:couplingType>
                        <xsl:for-each select="$cqpCouplingType">
                            <xsl:value-of select="concat( '|', , '|' )"/>
                        </xsl:for-each>
                    </app:couplingType>
                </xsl:if>
                
                <xsl:variable name="cqpOperation" select=""/>
                <xsl:if test="boolean( $cqpOperation != '' )">
                    <app:operation>
                        <xsl:for-each select="$cqpOperation">
                            <xsl:value-of select="concat( '|', , '|' )"/>
                        </xsl:for-each>
                    </app:operation>
                </xsl:if>
                
                <xsl:variable name="cqpOperatesOnName" select=""/>
                <xsl:if test="boolean( $cqpOperatesOnName != '' )">
                    <app:operatesOnName>
                        <xsl:for-each select="$cqpOperatesOnName">
                            <xsl:value-of select="concat( '|', , '|' )"/>
                        </xsl:for-each>
                    </app:operatesOnName>
                </xsl:if>
                
                <xsl:variable name="cqpOperatesOnIdentifier" select=""/>
                <xsl:if test="boolean( $cqpOperatesOnIdentifier != '' )">
                    <app:operatesOnIdentifier>
                        <xsl:for-each select="$cqpOperatesOnIdentifier">
                            <xsl:value-of select="concat( '|', , '|' )"/>
                        </xsl:for-each>
                    </app:operatesOnIdentifier>
                </xsl:if>
                 -->
                <xsl:variable name="cqpDomainConsistency" select="gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult"/>
                <xsl:for-each select="$cqpDomainConsistency">
                    <app:domainConsistency>
                        <app:CQP_DomainConsistency>
                            <xsl:variable name="specificationTitle" select="gmd:specification/gmd:CI_Citation/gmd:title/gco:CharacterString"/>
                            <xsl:if test="boolean( $specificationTitle != '' )">
                                <app:specificationTitle>
                                        <xsl:value-of select="$specificationTitle"/>
                                </app:specificationTitle>
                            </xsl:if>
                            
                            <xsl:variable name="degree" select="gmd:pass/gco:Boolean"/>
                            <xsl:if test="boolean( $degree != '' )">
                                <app:degree>
                                        <xsl:value-of select="$degree"/>
                                </app:degree>
                            </xsl:if>
                            <xsl:for-each select="gmd:specification/gmd:CI_Citation/gmd:date/gmd:CI_Date">
                                <app:specificationDate>
                                    <app:CQP_SpecificationDate>
                                        <app:dateStamp>
                                            <xsl:value-of select="gmd:date/gco:Date"/>
                                            <xsl:value-of select="gmd:date/gco:DateTime" />
                                        </app:dateStamp>
                                        <app:datetype>
                                            <xsl:value-of select="gmd:dateType/gmd:CI_DateTypeCode/@codeListValue"/>
                                        </app:datetype>
                                    </app:CQP_SpecificationDate>
                                </app:specificationDate>
                            </xsl:for-each>
                        </app:CQP_DomainConsistency>
                    </app:domainConsistency>
                </xsl:for-each>
                
			</app:CQP_Main>
		</app:commonQueryableProperties>
	</xsl:template>	
	
</xsl:stylesheet>
