<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:wfs="http://www.opengis.net/wfs" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gml="http://www.opengis.net/gml" 
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:app="http://www.deegree.org/app"  
	xmlns:gmd="http://www.isotc211.org/2005/gmd"
	xmlns:gco="http://www.isotc211.org/2005/gco" 
	xmlns:gts="http://www.isotc211.org/2005/gts" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:java="java"
	xmlns:minmax="org.deegree.framework.xml.MinMaxExtractor" 
	xmlns:mapping="org.deegree.ogcwebservices.csw.iso_profile.Mapping2_0_2" 
	exclude-result-prefixes="false" >
	<!-- ================================================================== -->
	<xsl:include href="iso19119_out.xsl" />
	<xsl:include href="iso19115_out.xsl" />
	<xsl:include href="utility_templates.xsl" />
	<!-- ================================================================== -->
	<xsl:param name="REQUEST_ID" />
	<xsl:param name="SEARCH_STATUS" />
	<xsl:param name="TIMESTAMP" />
	<xsl:param name="ELEMENT_SET" />
	<xsl:param name="RECORD_SCHEMA" />
	<xsl:param name="RECORDS_MATCHED" />
	<xsl:param name="RECORDS_RETURNED" />
	<xsl:param name="NEXT_RECORD" />
	<xsl:param name="REQUEST_NAME" />
	
	<xsl:template match="wfs:FeatureCollection" xmlns:deegreewfs="http://www.deegree.org/wfs">
		<xsl:choose>
			<xsl:when test="$REQUEST_NAME = 'GetRecordById'">
				<csw:GetRecordByIdResponse xmlns:csw="http://www.opengis.net/cat/csw" version="2.0.0">
					<xsl:for-each select="gml:featureMember">
						<xsl:apply-templates select="app:MD_Metadata">
							<xsl:with-param name="HLEVEL">
								<xsl:value-of
									select="app:MD_Metadata/app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
							</xsl:with-param>
						</xsl:apply-templates>
					</xsl:for-each>
				</csw:GetRecordByIdResponse>
			</xsl:when>
			<xsl:otherwise>
				<csw:GetRecordsResponse xmlns:csw="http://www.opengis.net/cat/csw" version="2.0.0">
					<csw:RequestId>
						<xsl:value-of select="$REQUEST_ID" />
					</csw:RequestId>
					<csw:SearchStatus>
						<xsl:attribute name="status">
							<xsl:value-of select="$SEARCH_STATUS" />
						</xsl:attribute>
						<xsl:attribute name="timestamp">
							<xsl:value-of select="$TIMESTAMP" />
						</xsl:attribute>
					</csw:SearchStatus>
					<csw:SearchResults>
						<xsl:attribute name="requestId">
							<xsl:value-of select="$REQUEST_ID" />
						</xsl:attribute>
						<!--				<xsl:attribute name="elementSet"><xsl:value-of select="$ELEMENT_SET"/></xsl:attribute>-->
						<xsl:attribute name="recordSchema">
							<xsl:value-of select="$RECORD_SCHEMA" />
						</xsl:attribute>
						<xsl:attribute name="numberOfRecordsMatched">
							<xsl:value-of select="$RECORDS_MATCHED" />
						</xsl:attribute>
						<xsl:attribute name="numberOfRecordsReturned">
							<xsl:value-of select="$RECORDS_RETURNED" />
						</xsl:attribute>
						<xsl:attribute name="nextRecord">
							<xsl:value-of select="$NEXT_RECORD" />
						</xsl:attribute>
						<xsl:for-each select="gml:featureMember">
							<xsl:apply-templates select="app:MD_Metadata">
								<xsl:with-param name="HLEVEL">
									<xsl:value-of select="app:MD_Metadata/app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
								</xsl:with-param>
							</xsl:apply-templates>
						</xsl:for-each>
					</csw:SearchResults>
				</csw:GetRecordsResponse>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- =================================================
		template for MD_Metadata
		====================================================-->
	<xsl:template match="app:MD_Metadata">
		<xsl:param name="HLEVEL" />
		<xsl:if test="$ELEMENT_SET = 'brief' and ($HLEVEL = 'dataset' or $HLEVEL = 'series' or $HLEVEL = 'application')">
			<xsl:call-template name="ISO19115BRIEF" />
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'summary' and ($HLEVEL = 'dataset' or $HLEVEL = 'series' or $HLEVEL = 'application')">
			<xsl:call-template name="ISO19115SUMMARY" />
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'full' and ($HLEVEL = 'dataset' or $HLEVEL = 'series' or $HLEVEL = 'application')">
			<xsl:call-template name="ISO19115FULL" />
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'brief' and $HLEVEL = 'service'">
			<xsl:call-template name="ISO19119BRIEF" />
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'summary' and $HLEVEL = 'service'">
			<xsl:call-template name="ISO19119SUMMARY" />
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'full' and $HLEVEL = 'service'">
			<xsl:call-template name="ISO19119FULL" />
		</xsl:if>
	</xsl:template>
	
	<!-- =================================================
		template for MD_DataIdentification
		====================================================-->
	<xsl:template match="app:MD_DataIdentification">
		<xsl:if test="$ELEMENT_SET = 'brief'">
			<gmd:identificationInfo>
				<xsl:call-template name="dataident_brief" />
			</gmd:identificationInfo>
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'summary'">
			<gmd:identificationInfo>
				<xsl:call-template name="dataident_summary" />
			</gmd:identificationInfo>
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'full'">
			<gmd:identificationInfo>
				<xsl:call-template name="dataident" />
			</gmd:identificationInfo>
		</xsl:if>
	</xsl:template>
	
	<!-- =================================================
		template for MD_DataIdentification_brief
		==================================================== -->
	<xsl:template name="dataident_brief">
		<gmd:MD_DataIdentification>
			<xsl:attribute name="uuid">
				<xsl:value-of select="app:uuid" />
			</xsl:attribute>
			<xsl:attribute name="id">
				<xsl:value-of select="app:uuid" />
			</xsl:attribute>
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
			<xsl:for-each select="app:language">
				<gmd:language>
            <gmd:LanguageCode>
                <xsl:attribute name="codeList">#LanguageCode</xsl:attribute>
                <xsl:attribute name="codeListValue">
                        <xsl:value-of select="." />
                </xsl:attribute>
            </gmd:LanguageCode>
				</gmd:language>
			</xsl:for-each>
			<xsl:if test="boolean(app:boundingBox/app:EX_GeogrBBOX )">
				<gmd:extent>
					<xsl:apply-templates select="app:boundingBox/app:EX_GeogrBBOX" />				
				</gmd:extent>
			</xsl:if>
		</gmd:MD_DataIdentification>	
	</xsl:template>
	
		<!-- =================================================
		citation brief
		==================================================== -->
		<xsl:template name="citation_brief">
			<gmd:CI_Citation>
				<gmd:title>
					<gco:CharacterString>
						<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title" />
					</gco:CharacterString>
				</gmd:title>
				<xsl:if test="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:revisiondate">
					<gmd:date>
						<gmd:CI_Date>
							<gmd:date>
								<gco:DateTime>
									<xsl:call-template name="toISODateTime">
										<xsl:with-param name="datetime">
											<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:revisiondate" />
										</xsl:with-param>
									</xsl:call-template>
								</gco:DateTime>
							</gmd:date>
							<gmd:dateType>
								<gmd:CI_DateTypeCode>
									<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
									<xsl:attribute name="codeListValue">revision</xsl:attribute>
								</gmd:CI_DateTypeCode>
							</gmd:dateType>
						</gmd:CI_Date>
					</gmd:date>
				</xsl:if>
				<xsl:if test="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:creationdate">
					<gmd:date>
						<gmd:CI_Date>
							<gmd:date>
								<gco:DateTime>
									<xsl:call-template name="toISODateTime">
										<xsl:with-param name="datetime">
											<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:creationdate" />
										</xsl:with-param>
									</xsl:call-template>
								</gco:DateTime>
							</gmd:date>
							<gmd:dateType>
								<gmd:CI_DateTypeCode>
									<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
									<xsl:attribute name="codeListValue">creation</xsl:attribute>
								</gmd:CI_DateTypeCode>
							</gmd:dateType>
						</gmd:CI_Date>
					</gmd:date>
				</xsl:if>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:publicationdate">
					<xsl:if test=" . != '' ">
							<gmd:date>
								<gmd:CI_Date>
									<gmd:date>
										<gco:DateTime>
											<xsl:call-template name="toISODateTime">
												<xsl:with-param name="datetime">
													<xsl:value-of select="." />
												</xsl:with-param>
											</xsl:call-template>
										</gco:DateTime>
									</gmd:date>
									<gmd:dateType>
										<gmd:CI_DateTypeCode>
											<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
											<xsl:attribute name="codeListValue">publication</xsl:attribute>
										</gmd:CI_DateTypeCode>
									</gmd:dateType>
								</gmd:CI_Date>
							</gmd:date>
					</xsl:if>
				</xsl:for-each>
				
				<xsl:variable name="uuidIsRS">
					<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:rsidentifier/app:RS_Identifier">
						<xsl:if test="boolean(app:code = ../../../../../../app:uuid)">true</xsl:if>
					</xsl:for-each>
				</xsl:variable>
				<!-- one of the identifier of identification.ci_citation mus be the same like the fileIdentifier and another one the same like the uuid ! -->
				<xsl:variable name="context" select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:context" />
				<xsl:if test="boolean( $context = 'Identification' )">
					<gmd:identifier>
						<gmd:MD_Identifier>
							<gmd:code>
								<gco:CharacterString>
									<xsl:value-of select="../../app:fileidentifier" />
								</gco:CharacterString>
							</gmd:code>
						</gmd:MD_Identifier>
					</gmd:identifier>
					<xsl:if test="boolean($uuidIsRS != 'true')">
						<gmd:identifier>
							<gmd:RS_Identifier>
								<gmd:code>
									<gco:CharacterString>
										<xsl:value-of select="app:uuid" />
									</gco:CharacterString>
								</gmd:code>
							</gmd:RS_Identifier>
						</gmd:identifier>
					</xsl:if>						
				</xsl:if>
                <xsl:for-each select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:rsidentifier/app:RS_Identifier">
			<xsl:if test="boolean($uuidIsRS = 'true') or boolean(app:code != ../../../../../../app:uuid)">
				<gmd:identifier>
					<xsl:apply-templates select="."/>
				</gmd:identifier>
			</xsl:if>
                </xsl:for-each>
			</gmd:CI_Citation>
		</xsl:template>
	
	<!-- =================================================
		template for MD_DataIdentification_summary
		==================================================== -->
	<xsl:template name="dataident_summary">
		<gmd:MD_DataIdentification>
			<xsl:attribute name="uuid">
				<xsl:value-of select="app:uuid" />
			</xsl:attribute>
			<xsl:attribute name="id">
				<xsl:value-of select="app:uuid" />
			</xsl:attribute>
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
			<xsl:for-each	select="app:identificationInfo/app:MD_Identification/app:graphicOverview/app:MD_BrowseGraphic">
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
			<xsl:for-each select="app:spatialRepresentationType">
				<gmd:spatialRepresentationType>
					<gmd:MD_SpatialRepresentationTypeCode>
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:MD_SpatialRepTypeCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_SpatialRepresentationTypeCode>
				</gmd:spatialRepresentationType>
			</xsl:for-each>
			<xsl:for-each select="app:spatialResolution/app:MD_Resolution">
				<gmd:spatialResolution>
					<xsl:apply-templates select="."/>
				</gmd:spatialResolution>
			</xsl:for-each>
			<xsl:for-each select="app:language">
				<gmd:language>
				    <gmd:LanguageCode>
                <xsl:attribute name="codeList">#LanguageCode</xsl:attribute>
                <xsl:attribute name="codeListValue">
                        <xsl:value-of select="." />
                </xsl:attribute>
            </gmd:LanguageCode>
				</gmd:language>
			</xsl:for-each>
			<xsl:for-each select="app:characterSet">
				<gmd:characterSet>
					<gmd:MD_CharacterSetCode>
						<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:MD_CharacterSetCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_CharacterSetCode>
				</gmd:characterSet>
			</xsl:for-each>
			<xsl:for-each select="app:topicCategory">
				<gmd:topicCategory>
					<gmd:MD_TopicCategoryCode>
						<xsl:value-of select="app:MD_TopicCategoryCode/app:category" />
					</gmd:MD_TopicCategoryCode>
				</gmd:topicCategory>
			</xsl:for-each>
			<xsl:for-each select="app:boundingBox">
				<gmd:extent>
					<xsl:apply-templates select="app:EX_GeogrBBOX" />
				</gmd:extent>
			</xsl:for-each>
		</gmd:MD_DataIdentification>
	</xsl:template>
	
	<!-- ========================================================	
		citation summary
		===========================================================  -->
	<xsl:template name="summary_citation">
	<xsl:variable name="citationPath" select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation"/>
		<gmd:CI_Citation>
			<gmd:title>
				<gco:CharacterString>
					<xsl:value-of select="$citationPath/app:title" />
				</gco:CharacterString>
			</gmd:title>
			<xsl:if test="$citationPath/app:revisiondate">
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="$citationPath/app:revisiondate" />
									</xsl:with-param>
								</xsl:call-template>
							</gco:DateTime>
						</gmd:date>
						<gmd:dateType>
							<gmd:CI_DateTypeCode>
								<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">revision</xsl:attribute>
							</gmd:CI_DateTypeCode>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
			</xsl:if>
			<xsl:if test="$citationPath/app:creationdate">
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="$citationPath/app:creationdate" />
									</xsl:with-param>
								</xsl:call-template>
							</gco:DateTime>
						</gmd:date>
						<gmd:dateType>
							<gmd:CI_DateTypeCode>
								<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">creation</xsl:attribute>
							</gmd:CI_DateTypeCode>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
			</xsl:if>
			<xsl:for-each select="$citationPath/app:publicationdate">
				<xsl:if test=" . != '' ">
					<gmd:date>
						<gmd:CI_Date>
							<gmd:date>
								<gco:DateTime>
									<xsl:call-template name="toISODateTime">
										<xsl:with-param name="datetime">
											<xsl:value-of select="." />
										</xsl:with-param>
									</xsl:call-template>
								</gco:DateTime>
							</gmd:date>
							<gmd:dateType>
								<gmd:CI_DateTypeCode>
									<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
									<xsl:attribute name="codeListValue">publication</xsl:attribute>
								</gmd:CI_DateTypeCode>
							</gmd:dateType>
						</gmd:CI_Date>
					</gmd:date>			
				</xsl:if>
			</xsl:for-each>
			
			<xsl:variable name="uuidIsRS">
				<xsl:for-each select="$citationPath/app:rsidentifier/app:RS_Identifier">
					<xsl:if test="boolean(app:code = ../../../../../../app:uuid)">true</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<!-- one of the identifier of identification.ci_citation mus be the same like the fileIdentifier and another one the same like the uuid ! -->	
			<xsl:variable name="context" select="$citationPath/app:context" />
			<xsl:if test="boolean( $context = 'Identification' )">
				<gmd:identifier>
					<gmd:MD_Identifier>
						<gmd:code>
							<gco:CharacterString>
								<xsl:value-of select="../../app:fileidentifier" />
							</gco:CharacterString>
						</gmd:code>
					</gmd:MD_Identifier>
				</gmd:identifier>
					<xsl:if test="boolean($uuidIsRS != 'true')">
				<gmd:identifier>
					<gmd:RS_Identifier>
						<gmd:code>
							<gco:CharacterString>
								<xsl:value-of select="app:uuid" />
							</gco:CharacterString>
						</gmd:code>
					</gmd:RS_Identifier>
				</gmd:identifier>
			</xsl:if>
			</xsl:if>
			<xsl:for-each select="$citationPath/app:identifier">
				<gmd:identifier>
					<gmd:MD_Identifier>
						<gmd:code>
							<gco:CharacterString>
								<xsl:value-of select="." />
							</gco:CharacterString>
						</gmd:code>
					</gmd:MD_Identifier>
				</gmd:identifier>
			</xsl:for-each>
            <xsl:for-each select="$citationPath/app:rsidentifier/app:RS_Identifier">
	    <xsl:if test="boolean($uuidIsRS = 'true') or boolean(app:code != ../../../../../../app:uuid)">
                	<gmd:identifier>
				<xsl:apply-templates select="."/>
			</gmd:identifier>
		</xsl:if>
            </xsl:for-each>
		</gmd:CI_Citation>
	</xsl:template>

	<!-- ==============================================================
		distribution info summary
	================================================================= -->
	<xsl:template name="summary_distributioninfo">
		<gmd:distributionInfo>
			<gmd:MD_Distribution>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:distributionFormat/app:MD_Format">
					<gmd:distributionFormat>
						<gmd:MD_Format>
							<gmd:name>
								<gco:CharacterString>
									<xsl:value-of select="app:name" />
								</gco:CharacterString>
							</gmd:name>
							<gmd:version>
								<gco:CharacterString>
									<xsl:value-of select="app:version" />
								</gco:CharacterString>
							</gmd:version>
						</gmd:MD_Format>
					</gmd:distributionFormat>
				</xsl:for-each>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:transferOptions">
					<gmd:transferOptions>
						<gmd:MD_DigitalTransferOptions>
							<xsl:for-each select="app:MD_DigTransferOpt/app:onlineResource">
								<xsl:if test="boolean( app:CI_OnlineResource/app:linkage != '' )">
									<gmd:onLine>
										<gmd:CI_OnlineResource>
											<gmd:linkage>
												<gmd:URL>
													<xsl:value-of select="app:CI_OnlineResource/app:linkage" />
												</gmd:URL>
											</gmd:linkage>
										</gmd:CI_OnlineResource>																		
									</gmd:onLine>
								</xsl:if>
							</xsl:for-each>
						</gmd:MD_DigitalTransferOptions>
					</gmd:transferOptions>				
				</xsl:for-each>
			</gmd:MD_Distribution>
		</gmd:distributionInfo>
	</xsl:template>
	
	<!-- ==============================================================
		data quality summary
	================================================================= -->
	<xsl:template name="summary_dataquality">
		<gmd:DQ_DataQuality>
			<gmd:scope>
				<gmd:DQ_Scope>
					<gmd:level>
						<gmd:MD_ScopeCode>
							<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:DQ_DataQuality/app:scopelevelcodelistvalue" />
							</xsl:attribute>
						</gmd:MD_ScopeCode>
					</gmd:level>
				</gmd:DQ_Scope>
			</gmd:scope>
			<xsl:if test="app:DQ_DataQuality/app:lineagestatement">
				<gmd:lineage>
					<gmd:LI_Lineage>
						<gmd:statement>
							<gco:CharacterString>
								<xsl:value-of select="app:DQ_DataQuality/app:lineagestatement" />
							</gco:CharacterString>
						</gmd:statement>
					</gmd:LI_Lineage>
				</gmd:lineage>
			</xsl:if>
		</gmd:DQ_DataQuality>						
	</xsl:template>
	
	<!-- =================================================
		template for MD_DataIdentification full
		====================================================-->
	<xsl:template name="dataident">
		<gmd:MD_DataIdentification>
			<xsl:attribute name="uuid">
				<xsl:value-of select="app:uuid" />
			</xsl:attribute>
			<xsl:attribute name="id">
				<xsl:value-of select="app:uuid" />
			</xsl:attribute>
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
			<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:resourceMaintenance">
				<xsl:apply-templates select="." />
			</xsl:for-each>
			<xsl:for-each	select="app:identificationInfo/app:MD_Identification/app:graphicOverview/app:MD_BrowseGraphic">
				<gmd:graphicOverview>
					<xsl:apply-templates select="."/>
				</gmd:graphicOverview>
			</xsl:for-each>						
			<xsl:for-each
				select="app:identificationInfo/app:MD_Identification/app:descriptiveKeywords/app:MD_Keywords">
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
			<xsl:for-each
				select="app:identificationInfo/app:MD_Identification/app:resourceSpecificUsage/app:MD_Usage">
				<gmd:resourceSpecificUsage>
					<gmd:MD_Usage>
						<gmd:specificUsage>
							<gco:CharacterString>
								<xsl:value-of select="app:specificusage" />
							</gco:CharacterString>
						</gmd:specificUsage>
						<xsl:choose>
						  <xsl:when test="count(app:RespParty) &gt; 0">
						    <xsl:for-each select="app:RespParty">
						      <gmd:userContactInfo>
							<xsl:apply-templates select="app:CI_RespParty" />
						      </gmd:userContactInfo>
						    </xsl:for-each>
						  </xsl:when>
						  <xsl:otherwise>
						    <gmd:userContactInfo />
						  </xsl:otherwise>
						</xsl:choose>
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
			<xsl:for-each select="app:aggregationInfo/app:MD_AggregateInfo">
				<gmd:aggregationInfo>
					<gmd:MD_AggregateInformation>
						<xsl:if test="boolean( app:aggregateDataSetName/app:CI_Citation != '' )">
							<gmd:aggregateDataSetName>
								<xsl:apply-templates select="app:aggregateDataSetName/app:CI_Citation" />
							</gmd:aggregateDataSetName>
						</xsl:if>
						<gmd:associationType>
							<gmd:DS_AssociationTypeCode>
								<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#DS_AssociationTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">
									<xsl:value-of select="app:associationType/app:DS_AssociationTypeCode/app:codelistvalue" />
								</xsl:attribute>
							</gmd:DS_AssociationTypeCode>
						</gmd:associationType>
					</gmd:MD_AggregateInformation>
				</gmd:aggregationInfo>
			</xsl:for-each>
			<xsl:for-each select="app:spatialRepresentationType">
				<gmd:spatialRepresentationType>
					<gmd:MD_SpatialRepresentationTypeCode>
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:MD_SpatialRepTypeCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_SpatialRepresentationTypeCode>
				</gmd:spatialRepresentationType>
			</xsl:for-each>
			<xsl:for-each select="app:spatialResolution/app:MD_Resolution">
				<gmd:spatialResolution>
					<xsl:apply-templates select="."/>
				</gmd:spatialResolution>
			</xsl:for-each>
			<xsl:for-each select="app:language">
				<gmd:language>
				    <gmd:LanguageCode>
                <xsl:attribute name="codeList">#LanguageCode</xsl:attribute>
                <xsl:attribute name="codeListValue">
                        <xsl:value-of select="." />
                </xsl:attribute>
            </gmd:LanguageCode>
				</gmd:language>
			</xsl:for-each>
			<xsl:for-each select="app:characterSet">
				<gmd:characterSet>
					<gmd:MD_CharacterSetCode>
						<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:MD_CharacterSetCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_CharacterSetCode>
				</gmd:characterSet>
			</xsl:for-each>
			<xsl:for-each select="app:topicCategory">
				<gmd:topicCategory>
					<gmd:MD_TopicCategoryCode>
						<xsl:value-of select="app:MD_TopicCategoryCode/app:category" />
					</gmd:MD_TopicCategoryCode>
				</gmd:topicCategory>
			</xsl:for-each>
			<xsl:for-each select="app:verticalExtent">
				<gmd:extent>
					<xsl:apply-templates select="app:EX_VerticalExtent" />
				</gmd:extent>
			</xsl:for-each>
			<xsl:for-each select="app:temportalExtent">
				<gmd:extent>
					<xsl:apply-templates select="app:EX_TemporalExtent" />
				</gmd:extent>
			</xsl:for-each>
			<xsl:for-each select="app:boundingPolygon">
				<gmd:extent>
					<xsl:apply-templates select="app:EX_BoundingPolygon" />
				</gmd:extent>
			</xsl:for-each>
			<xsl:for-each select="app:geographicIdentifierCode">
				<gmd:extent>
					<gmd:EX_Extent>
						<gmd:geographicElement>
							<gmd:EX_GeographicDescription>
								<gmd:geographicIdentifier>
									<gmd:MD_Identifier>
										<gmd:code>
											<gco:CharacterString>
												<xsl:value-of select="." />
											</gco:CharacterString>
										</gmd:code>
									</gmd:MD_Identifier>
								</gmd:geographicIdentifier>
							</gmd:EX_GeographicDescription>
						</gmd:geographicElement>
					</gmd:EX_Extent>
				</gmd:extent>
			</xsl:for-each>
			<xsl:for-each select="app:boundingBox">
				<gmd:extent>
					<xsl:apply-templates select="app:EX_GeogrBBOX" />
				</gmd:extent>
			</xsl:for-each>
			<xsl:if test="app:supplementalInformation">
				<gmd:supplementalInformation>
					<gco:CharacterString>
						<xsl:value-of select="app:supplementalInformation" />
					</gco:CharacterString>
				</gmd:supplementalInformation>
			</xsl:if>
		</gmd:MD_DataIdentification>
	</xsl:template>

	<!-- ===============================================
		template for CI_CitedResponisbleParty
		=================================================-->
	<xsl:template match="app:CI_RespParty | app:CI_Citation/app:CI_RespParty">
		<gmd:CI_ResponsibleParty>
			<xsl:if test="app:individualname">
				<gmd:individualName>
					<gco:CharacterString>
						<xsl:value-of select="app:individualname" />
					</gco:CharacterString>
				</gmd:individualName>
			</xsl:if>
			<xsl:if test="app:organisationname">
				<gmd:organisationName>
					<gco:CharacterString>
						<xsl:value-of select="app:organisationname" />
					</gco:CharacterString>
				</gmd:organisationName>
			</xsl:if>
			<xsl:if test="app:positionname">
				<gmd:positionName>
					<gco:CharacterString>
						<xsl:value-of select="app:positionname" />
					</gco:CharacterString>
				</gmd:positionName>
			</xsl:if>
			<xsl:for-each select="app:contactInfo">
				<xsl:apply-templates select="app:CI_Contact" />
			</xsl:for-each>
			<gmd:role>
				<gmd:CI_RoleCode>
					<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode</xsl:attribute>
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="app:role/app:CI_RoleCode/app:codelistvalue" />
					</xsl:attribute>
				</gmd:CI_RoleCode>
			</gmd:role>
		</gmd:CI_ResponsibleParty>
	</xsl:template>
	
	<!-- ============================================
		template for CI_Contact
		===============================================-->
	<xsl:template match="app:CI_Contact">
		<gmd:contactInfo>
			<gmd:CI_Contact>
				<xsl:if test="app:voice or app:facsimile">
					<gmd:phone>
						<gmd:CI_Telephone>
							<xsl:for-each select="app:voice">
								<gmd:voice>
									<gco:CharacterString>
										<xsl:value-of select="." />
									</gco:CharacterString>
								</gmd:voice>
							</xsl:for-each>
							<xsl:for-each select="app:facsimile">
								<gmd:facsimile>
									<gco:CharacterString>
										<xsl:value-of select="." />
									</gco:CharacterString>
								</gmd:facsimile>
							</xsl:for-each>
						</gmd:CI_Telephone>
					</gmd:phone>
				</xsl:if>
				<xsl:apply-templates select="app:address/app:CI_Address" />
				<xsl:for-each select="app:onlineResource">
					<gmd:onlineResource>
						<xsl:apply-templates select="app:CI_OnlineResource" />
					</gmd:onlineResource>
				</xsl:for-each>
				<xsl:if test="app:hoursofservice">
					<gmd:hoursOfService>
						<gco:CharacterString>
							<xsl:value-of select="app:hoursofservice" />
						</gco:CharacterString>
					</gmd:hoursOfService>
				</xsl:if>
				<xsl:if test="app:contactinstructions">
					<gmd:contactInstructions>
						<gco:CharacterString>
							<xsl:value-of select="app:contactinstructions" />
						</gco:CharacterString>
					</gmd:contactInstructions>
				</xsl:if>
			</gmd:CI_Contact>
		</gmd:contactInfo>
	</xsl:template>
	
	<!-- ============================================
		template for CI_Address
		===============================================-->
	<xsl:template match="app:address/app:CI_Address">
		<gmd:address>
			<gmd:CI_Address>
				<xsl:for-each select="app:deliveryPoint">
					<gmd:deliveryPoint>
						<gco:CharacterString>
							<xsl:value-of select="app:DeliveryPoint/app:deliverypoint" />
						</gco:CharacterString>
					</gmd:deliveryPoint>
				</xsl:for-each>
				<xsl:if test="app:city">
					<gmd:city>
						<gco:CharacterString>
							<xsl:value-of select="app:city" />
						</gco:CharacterString>
					</gmd:city>
				</xsl:if>
				<xsl:if test="app:administrativeArea">
					<gmd:administrativeArea>
						<gco:CharacterString>
							<xsl:value-of select="app:administrativeArea" />
						</gco:CharacterString>
					</gmd:administrativeArea>
				</xsl:if>
				<xsl:if test="app:postalCode">
					<gmd:postalCode>
						<gco:CharacterString>
							<xsl:value-of select="app:postalCode" />
						</gco:CharacterString>
					</gmd:postalCode>
				</xsl:if>
				<xsl:if test="app:country">
					<gmd:country>
						<gco:CharacterString>
							<xsl:value-of select="app:country" />
						</gco:CharacterString>
					</gmd:country>
				</xsl:if>
				<xsl:for-each select="app:electronicMailAddress">
					<gmd:electronicMailAddress>
						<gco:CharacterString>
							<xsl:value-of select="app:ElectronicMailAddress/app:email" />
						</gco:CharacterString>
					</gmd:electronicMailAddress>
				</xsl:for-each>
			</gmd:CI_Address>
		</gmd:address>
	</xsl:template>
	
	<!-- ============================================
		template for CI_OnlineResource
		===============================================-->
	<xsl:template match="app:CI_OnlineResource">
		<gmd:CI_OnlineResource>
			<gmd:linkage>
				<gmd:URL>
					<xsl:value-of select="app:linkage" />
				</gmd:URL>
			</gmd:linkage>
			<xsl:if test="app:protocol">
				<gmd:protocol>	
						<gco:CharacterString>
							<xsl:value-of select="app:protocol"/>
						</gco:CharacterString>
				</gmd:protocol>
			</xsl:if>
			<xsl:if test="app:name">
				<gmd:name>
						<gco:CharacterString>
							<xsl:value-of select="app:name"/>
						</gco:CharacterString>
				</gmd:name>
			</xsl:if>
			<xsl:if test="app:description">
				<gmd:description>
					<gco:CharacterString>
						<xsl:value-of select="app:description"/>
					</gco:CharacterString>
				</gmd:description>
			</xsl:if>			
			<xsl:if test="app:function">
				<gmd:function>
					<gmd:CI_OnLineFunctionCode>
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_OnLineFunctionCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:function/app:CI_OnLineFunctionCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:CI_OnLineFunctionCode>
				</gmd:function>
			</xsl:if>
		</gmd:CI_OnlineResource>
	</xsl:template>
	
	<!-- =================================================
		template for CI_Citation
		====================================================-->
	<xsl:template match="app:CI_Citation">
		<gmd:CI_Citation>
			<gmd:title>
				<gco:CharacterString>
					<xsl:value-of select="app:title" />
				</gco:CharacterString>
			</gmd:title>
			<xsl:for-each select="app:alternateTitle">
				<gmd:alternateTitle>
					<gco:CharacterString>
						<xsl:value-of select="." />
					</gco:CharacterString>
				</gmd:alternateTitle>
			</xsl:for-each>
			<xsl:if test="app:revisiondate">
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="app:revisiondate" />
									</xsl:with-param>
								</xsl:call-template>
							</gco:DateTime>
						</gmd:date>
						<gmd:dateType>
							<gmd:CI_DateTypeCode>
								<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">revision</xsl:attribute>
							</gmd:CI_DateTypeCode>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
			</xsl:if>
			<xsl:if test="app:creationdate">
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="app:creationdate" />
									</xsl:with-param>
								</xsl:call-template>
							</gco:DateTime>
						</gmd:date>
						<gmd:dateType>
							<gmd:CI_DateTypeCode>
								<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">creation</xsl:attribute>
							</gmd:CI_DateTypeCode>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
			</xsl:if>
			<xsl:for-each select="app:publicationdate">
				<xsl:if test=" . != '' ">
					<gmd:date>
						<gmd:CI_Date>
							<gmd:date>
								<gco:DateTime>
									<xsl:call-template name="toISODateTime">
										<xsl:with-param name="datetime">
											<xsl:value-of select="." />
										</xsl:with-param>
									</xsl:call-template>
								</gco:DateTime>
							</gmd:date>
							<gmd:dateType>
								<gmd:CI_DateTypeCode>
									<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode</xsl:attribute>
									<xsl:attribute name="codeListValue">publication</xsl:attribute>
								</gmd:CI_DateTypeCode>
							</gmd:dateType>
						</gmd:CI_Date>
					</gmd:date>
				</xsl:if>
			</xsl:for-each>
			<xsl:if test="app:edition">
				<gmd:edition>
					<gco:CharacterString>
						<xsl:value-of select="app:edition" />
					</gco:CharacterString>
				</gmd:edition>
			</xsl:if>
			<xsl:if test="app:editiondate">
				<gmd:editionDate>
					<gco:DateTime>
						<xsl:call-template name="toISODateTime">
							<xsl:with-param name="datetime">
								<xsl:value-of select="app:editiondate" />
							</xsl:with-param>
						</xsl:call-template>
					</gco:DateTime>
				</gmd:editionDate>
			</xsl:if>
			
			<xsl:variable name="uuidIsRS">
				<xsl:for-each select="app:rsidentifier/app:RS_Identifier">
					<xsl:if test="boolean(app:code = ../../../../../../app:uuid)">true</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<!-- one of the identifier of identification.ci_citation mus be the same like the fileIdentifier and another one the same like the uuid ! -->
			<xsl:variable name="context" select="app:context" />
			<!-- 
			<xsl:if test="boolean( $context = 'Identification' )">
				<gmd:identifier>
					<gmd:MD_Identifier>
						<gmd:code>
							<gco:CharacterString>
								<xsl:value-of select="../../../../../../app:fileidentifier" />
							</gco:CharacterString>
						</gmd:code>
					</gmd:MD_Identifier>
				</gmd:identifier>
				<xsl:if test="boolean($uuidIsRS != 'true')">
					<gmd:identifier>
						<gmd:RS_Identifier>
							<gmd:code>
								<gco:CharacterString>
									<xsl:value-of select="../../../../app:uuid" />
								</gco:CharacterString>
							</gmd:code>
						</gmd:RS_Identifier>
					</gmd:identifier>
				</xsl:if>
			</xsl:if>
			 -->
			<xsl:for-each select="app:identifier">
				<gmd:identifier>
					<gmd:MD_Identifier>
						<gmd:code>
							<gco:CharacterString>
								<xsl:value-of select="." />
							</gco:CharacterString>
						</gmd:code>
					</gmd:MD_Identifier>
				</gmd:identifier>
			</xsl:for-each>
            <xsl:for-each select="app:rsidentifier/app:RS_Identifier">
            <!-- 
	    		<xsl:if test="boolean($uuidIsRS = 'true') or boolean(app:code != ../../../../../../app:uuid)">
	     	-->
	                <gmd:identifier>
	                    <xsl:apply-templates select="."/>
	                </gmd:identifier>
            <!-- 
				</xsl:if>
			 -->
            </xsl:for-each>
			<xsl:for-each select="app:citedResponsibleParty">
				<gmd:citedResponsibleParty>
					<xsl:apply-templates select="app:CI_RespParty" />
				</gmd:citedResponsibleParty>
			</xsl:for-each>
			<xsl:for-each select="app:presentationForm">
				<gmd:presentationForm>
					<gmd:CI_PresentationFormCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_PresentationFormCode">
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:CI_PresentationFormCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:CI_PresentationFormCode>
				</gmd:presentationForm>
			</xsl:for-each>
			<xsl:if test="app:series">
				<gmd:series>
					<xsl:for-each select="app:series/app:CI_Series">
						<gmd:CI_Series>
							<xsl:if test="app:name">
								<gmd:name>
									<gco:CharacterString>
										<xsl:value-of select="app:name" />
									</gco:CharacterString>
								</gmd:name>
							</xsl:if>
							<xsl:if test="app:issueidentification">
								<gmd:issueIdentification>
									<gco:CharacterString>
										<xsl:value-of select="app:issueidentification" />
									</gco:CharacterString>
								</gmd:issueIdentification>
							</xsl:if>
							<xsl:if test="app:page">
								<gmd:page>
									<gco:CharacterString>
										<xsl:value-of select="app:page" />
									</gco:CharacterString>
								</gmd:page>
							</xsl:if>
						</gmd:CI_Series>
					</xsl:for-each>
				</gmd:series>
			</xsl:if>
			<xsl:if test="app:otherCitationDetails">
				<gmd:otherCitationDetails>
					<gco:CharacterString>
						<xsl:value-of select="app:otherCitationDetails"/>
					</gco:CharacterString>
				</gmd:otherCitationDetails>
			</xsl:if>
			<xsl:if test="app:isbn">
				<gmd:ISBN>
					<gco:CharacterString>
						<xsl:value-of select="app:isbn" />
					</gco:CharacterString>
				</gmd:ISBN>
			</xsl:if>
			<xsl:if test="app:issn">
				<gmd:ISSN>
					<gco:CharacterString>
						<xsl:value-of select="app:issn" />
					</gco:CharacterString>
				</gmd:ISSN>
			</xsl:if>
		</gmd:CI_Citation>
	</xsl:template>
	
	<!-- ==================================================
		template for LegalConstraints
		=====================================================-->
	<xsl:template match="app:MD_LegalConstraints">
		<gmd:MD_LegalConstraints>
			<xsl:if test="boolean( app:useLimitations )">
				<gmd:useLimitation>
					<gco:CharacterString>
						<xsl:value-of select="app:useLimitations" />
					</gco:CharacterString>
				</gmd:useLimitation>
			</xsl:if>
			<xsl:for-each select="app:accessConstraints">
				<gmd:accessConstraints>
					<xsl:for-each select="app:MD_RestrictionCode">
						<gmd:MD_RestrictionCode>
							<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:codelistvalue" />
							</xsl:attribute>
						</gmd:MD_RestrictionCode>
					</xsl:for-each>
				</gmd:accessConstraints>
			</xsl:for-each>
			<xsl:for-each select="app:useConstraints">
				<gmd:useConstraints>
					<xsl:for-each select="app:MD_RestrictionCode">
						<gmd:MD_RestrictionCode>
							<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:codelistvalue" />
							</xsl:attribute>
						</gmd:MD_RestrictionCode>
					</xsl:for-each>
				</gmd:useConstraints>
			</xsl:for-each>
			<xsl:for-each select="app:otherConstraints">
				<gmd:otherConstraints>
					<gco:CharacterString>
						<xsl:value-of select="." />
					</gco:CharacterString>
				</gmd:otherConstraints>
			</xsl:for-each>
		</gmd:MD_LegalConstraints>
	</xsl:template>
	
	<!-- ==================================================
		template for SecurityConstraints
		=====================================================-->
	<xsl:template match="app:MD_SecurityConstraints">
		<gmd:MD_SecurityConstraints>
			<xsl:if test="boolean( app:useLimitations )">
				<gmd:useLimitation>
					<gco:CharacterString>
						<xsl:value-of select="app:useLimitations" />
					</gco:CharacterString>
				</gmd:useLimitation>
			</xsl:if>
			<gmd:classification>
				<gmd:MD_ClassificationCode>
					<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_ClassificationCode</xsl:attribute>
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="app:classification/app:MD_ClassificationCode/app:codelistvalue" />
					</xsl:attribute>
				</gmd:MD_ClassificationCode>
			</gmd:classification>
			<xsl:if test="boolean( app:userNote )">
				<gmd:userNote>
					<gco:CharacterString>
						<xsl:value-of select="app:userNote" />
					</gco:CharacterString>
				</gmd:userNote>
			</xsl:if>
			<xsl:if test="boolean( app:classificationSystem )">
				<gmd:classificationSystem>
					<gco:CharacterString>
						<xsl:value-of select="app:classificationSystem" />
					</gco:CharacterString>
				</gmd:classificationSystem>
			</xsl:if>
			<xsl:if test="boolean( app:handlingDescription )">
				<gmd:handlingDescription>
					<gco:CharacterString>
						<xsl:value-of select="app:handlingDescription" />
					</gco:CharacterString>
				</gmd:handlingDescription>
			</xsl:if>
		</gmd:MD_SecurityConstraints>
	</xsl:template>

    <!-- ==================================================
        template for Constraints
        =====================================================-->
    <xsl:template match="app:MD_Constraints">
        <gmd:MD_Constraints>
            <xsl:if test="boolean( app:useLimitations )">
                <gmd:useLimitation>
                    <gco:CharacterString>
                        <xsl:value-of select="app:useLimitations" />
                    </gco:CharacterString>
                </gmd:useLimitation>
            </xsl:if>
        </gmd:MD_Constraints>
    </xsl:template>
    
	<!-- =======================================================
		template for resource maintenance
		==========================================================-->
	<xsl:template match="app:MD_Identification/app:resourceMaintenance">
		<gmd:resourceMaintenance>
			<gmd:MD_MaintenanceInformation>
				<gmd:maintenanceAndUpdateFrequency>
					<gmd:MD_MaintenanceFrequencyCode>
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_MaintenanceFrequencyCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of
								select="app:MD_MaintenanceInformation/app:maintenanceAndUpdateFrequency/app:MD_MainFreqCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_MaintenanceFrequencyCode>
				</gmd:maintenanceAndUpdateFrequency>
				<xsl:if test="boolean( app:MD_MaintenanceInformation/app:dateOfNextUpdate )">
					<gmd:dateOfNextUpdate>
						<gco:DateTime>
							<xsl:call-template name="toISODateTime">
								<xsl:with-param name="datetime">
									<xsl:value-of select="app:MD_MaintenanceInformation/app:dateOfNextUpdate" />
								</xsl:with-param>
							</xsl:call-template>
						</gco:DateTime>
					</gmd:dateOfNextUpdate>
				</xsl:if>
				<xsl:if test="boolean( app:MD_MaintenanceInformation/app:userDefinedMaintenanceFrequency )">
					<gmd:userDefinedMaintenanceFrequency>
						<gts:TM_PeriodDuration>
							<xsl:value-of select="app:MD_MaintenanceInformation/app:userDefinedMaintenanceFrequency" />
						</gts:TM_PeriodDuration>
					</gmd:userDefinedMaintenanceFrequency>
				</xsl:if>
				<xsl:if test="boolean( app:MD_MaintenanceInformation/app:updateScope)">
					<gmd:updateScope>
						<gmd:MD_ScopeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode">
							<xsl:attribute name="codeListValue">
								<xsl:value-of
									select="app:MD_MaintenanceInformation/app:updateScope/app:MD_ScopeCode/app:codelistvalue" />
							</xsl:attribute>
						</gmd:MD_ScopeCode>
					</gmd:updateScope>
				</xsl:if>
				<xsl:if test="app:MD_MaintenanceInformation/app:note">
					<gmd:maintenanceNote>
						<gco:CharacterString>
							<xsl:value-of select="app:MD_MaintenanceInformation/app:note" />
						</gco:CharacterString>
					</gmd:maintenanceNote>
				</xsl:if>
			</gmd:MD_MaintenanceInformation>
		</gmd:resourceMaintenance>
	</xsl:template>
	
	<!-- =======================================================
		template for EX_VerticalExtent
		==========================================================-->
	<xsl:template match="app:EX_VerticalExtent">
		<gmd:EX_Extent>
			<xsl:if test="app:description">
				<gmd:description>
					<gco:CharacterString>
						<xsl:value-of select="app:description" />
					</gco:CharacterString>
				</gmd:description>
			</xsl:if>
			<gmd:verticalElement>
				<gmd:EX_VerticalExtent>
					<gmd:minimumValue>
						<gco:Real>
							<xsl:value-of select="app:minval" />
						</gco:Real>
					</gmd:minimumValue>
					<gmd:maximumValue>
						<gco:Real>
							<xsl:value-of select="app:maxval" />
						</gco:Real>
					</gmd:maximumValue>
					<xsl:if test="boolean( app:verticalDatum != '' ) or boolean( app:hrefAttribute != '' )">
						<gmd:verticalCRS>
                            <xsl:if test="boolean( app:hrefAttribute != '' )">
								<xsl:attribute name="xlink:href">
									<xsl:value-of select="app:hrefAttribute" />
								</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="boolean( app:verticalDatum != '' )">        
						        <xsl:copy-of select="app:verticalDatum/child::*"/>
                            </xsl:if>
						</gmd:verticalCRS>
					</xsl:if>
				</gmd:EX_VerticalExtent>
			</gmd:verticalElement>
		</gmd:EX_Extent>
	</xsl:template>
	
	<!-- =======================================================
		template for EX_TemporalExtent
		==========================================================-->
	<xsl:template match="app:EX_TemporalExtent">
		<gmd:EX_Extent>
			<xsl:if test="app:description">
				<gmd:description>
					<gco:CharacterString>
						<xsl:value-of select="app:description" />
					</gco:CharacterString>
				</gmd:description>
			</xsl:if>
			<gmd:temporalElement>
				<gmd:EX_TemporalExtent>
					<gmd:extent>
						<xsl:choose>
							<xsl:when test="boolean( app:begin_ and app:end_ ) ">
								<gml:TimePeriod>
									<xsl:attribute name="gml:id">
										<xsl:value-of select="mapping:getId()" />
									</xsl:attribute>
									<gml:begin>
										<gml:TimeInstant>
											<xsl:attribute name="gml:id">
												<xsl:value-of select="mapping:getId()" />
											</xsl:attribute>
											<gml:timePosition>
												<xsl:choose>
													<xsl:when test="contains( app:begin_, 'T' )">
														<xsl:value-of select="app:begin_" />
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="concat( app:begin_, 'T00:00:00')" />
													</xsl:otherwise>
												</xsl:choose>
											</gml:timePosition>
										</gml:TimeInstant>
									</gml:begin>
									<gml:end>
										<gml:TimeInstant>
											<xsl:attribute name="gml:id">
												<xsl:value-of select="mapping:getId()" />
											</xsl:attribute>
											<gml:timePosition>
												<xsl:choose>
													<xsl:when test="contains( app:end_, 'T' )">
														<xsl:value-of select="app:end_" />
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="concat( app:end_, 'T00:00:00')" />
													</xsl:otherwise>
												</xsl:choose>
											</gml:timePosition>
										</gml:TimeInstant>
									</gml:end>
								</gml:TimePeriod>
							</xsl:when>
							<xsl:when test="boolean( app:timePosition )">
								<gml:TimeInstant>
									<xsl:attribute name="gml:id">
										<xsl:value-of select="mapping:getId()" />
									</xsl:attribute>
									<gml:timePosition>
										<xsl:value-of select="concat( app:timePosition, 'T00:00:00')" />
									</gml:timePosition>
								</gml:TimeInstant>
							</xsl:when>
						</xsl:choose>
					</gmd:extent>
				</gmd:EX_TemporalExtent>
			</gmd:temporalElement>
		</gmd:EX_Extent>
	</xsl:template>
	
	<!-- ========================================================
		template for EX_BoundingPolygon
		===========================================================-->
	<xsl:template match="app:EX_BoundingPolygon">
		<gmd:EX_Extent>
			<xsl:if test="app:description">
				<gmd:description>
					<gco:CharacterString>
						<xsl:value-of select="app:description" />
					</gco:CharacterString>
				</gmd:description>
			</xsl:if>
			<gmd:geographicElement>
				<gmd:EX_BoundingPolygon>
					<gmd:polygon />
				</gmd:EX_BoundingPolygon>
			</gmd:geographicElement>
		</gmd:EX_Extent>
	</xsl:template>
	
	<!-- ========================================================
		template for EX_GeographicBoundingBox
		===========================================================-->
	<xsl:template match="app:EX_GeogrBBOX">
		<gmd:EX_Extent>
			<xsl:if test="app:description">
				<gmd:description>
					<gco:CharacterString>
						<xsl:value-of select="app:description" />
					</gco:CharacterString>
				</gmd:description>
			</xsl:if>
			<gmd:geographicElement>
				<gmd:EX_GeographicBoundingBox>
					<gmd:extentTypeCode>
						<gco:Boolean>1</gco:Boolean>
					</gmd:extentTypeCode>
					<gmd:westBoundLongitude>
						<gco:Decimal>
							<xsl:value-of select="minmax:getXMin( ./app:geom/child::*[1] )" />
						</gco:Decimal>
					</gmd:westBoundLongitude>
					<gmd:eastBoundLongitude>
						<gco:Decimal>
							<xsl:value-of select="minmax:getXMax( ./app:geom/child::*[1] )" />
						</gco:Decimal>
					</gmd:eastBoundLongitude>
					<gmd:southBoundLatitude>
						<gco:Decimal>
							<xsl:value-of select="minmax:getYMin( ./app:geom/child::*[1] )" />
						</gco:Decimal>
					</gmd:southBoundLatitude>
					<gmd:northBoundLatitude>
						<gco:Decimal>
							<xsl:value-of select="minmax:getYMax( ./app:geom/child::*[1] )" />
						</gco:Decimal>
					</gmd:northBoundLatitude>
				</gmd:EX_GeographicBoundingBox>
			</gmd:geographicElement>
		</gmd:EX_Extent>
	</xsl:template>
	
	<!-- ==========================================================
		template for DQ_Data Qality
		=============================================================-->
	<xsl:template match="app:DQ_DataQuality">
		<gmd:DQ_DataQuality>
			<gmd:scope>
				<gmd:DQ_Scope>
					<gmd:level>
						<gmd:MD_ScopeCode>
							<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_ScopeCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:scopelevelcodelistvalue" />
							</xsl:attribute>
						</gmd:MD_ScopeCode>
					</gmd:level>
				</gmd:DQ_Scope>
			</gmd:scope>
			<xsl:for-each select="app:DQ_Element">
				<xsl:apply-templates select="app:DQ_Element" />
			</xsl:for-each>
			<xsl:if test="app:lineagestatement or app:LI_Source or app:LI_ProcessStep">
				<gmd:lineage>
					<gmd:LI_Lineage>
						<xsl:if test="app:lineagestatement">
							<gmd:statement>
								<gco:CharacterString>
									<xsl:value-of select="app:lineagestatement" />
								</gco:CharacterString>
							</gmd:statement>
						</xsl:if>
						<xsl:for-each select="app:LI_ProcessStep">
							<gmd:processStep>
								<xsl:apply-templates select="app:LI_ProcessStep" />
							</gmd:processStep>
						</xsl:for-each>
						<xsl:for-each select="app:LI_Source">
							<gmd:source>
								<gmd:LI_Source>
									<xsl:if test="app:LI_Source/app:description">
										<gmd:description>
											<gco:CharacterString>
												<xsl:value-of select="app:LI_Source/app:description" />
											</gco:CharacterString>
										</gmd:description>
									</xsl:if>
									<xsl:if test="app:LI_Source/app:scaleDenominator">
										<gmd:scaleDenominator>
											<gmd:MD_RepresentativeFraction>
												<gmd:denominator>
													<gco:Integer>
														<xsl:value-of select="app:LI_Source/app:scaleDenominator" />
													</gco:Integer>
												</gmd:denominator>
											</gmd:MD_RepresentativeFraction>
										</gmd:scaleDenominator>
									</xsl:if>
									<xsl:if test="app:LI_Source/app:sourceReferenceSystem">
										<gmd:sourceReferenceSystem>
											<gmd:MD_ReferenceSystem>
												<gmd:referenceSystemIdentifier>
													<xsl:apply-templates select="app:LI_Source/app:sourceReferenceSystem/app:RS_Identifier" />
												</gmd:referenceSystemIdentifier>
											</gmd:MD_ReferenceSystem>
										</gmd:sourceReferenceSystem>
									</xsl:if>
									<xsl:if test="app:LI_Source/app:sourceCitation">
										<gmd:sourceCitation>
											<xsl:apply-templates
												select="app:LI_Source/app:sourceCitation/app:CI_Citation" />
										</gmd:sourceCitation>
									</xsl:if>
									<xsl:for-each select="app:LI_Source/app:sourceStep">
										<gmd:sourceStep>
											<xsl:apply-templates select="app:LI_ProcessStep" />
										</gmd:sourceStep>
									</xsl:for-each>
								</gmd:LI_Source>
							</gmd:source>
						</xsl:for-each>
					</gmd:LI_Lineage>
				</gmd:lineage>
			</xsl:if>
		</gmd:DQ_DataQuality>
	</xsl:template>

	<!-- ==========================================================
		template for ProcessStep
		=============================================================-->
	<xsl:template match="app:LI_ProcessStep">
		<gmd:LI_ProcessStep>
			<gmd:description>
				<gco:CharacterString>
					<xsl:value-of select="app:description" />
				</gco:CharacterString>
			</gmd:description>
			<xsl:if test="app:rationale">
				<gmd:rationale>
					<gco:CharacterString>
						<xsl:value-of select="app:rationale" />
					</gco:CharacterString>
				</gmd:rationale>
			</xsl:if>
			<xsl:if test="app:dateTime">
				<gmd:dateTime>
					<gco:DateTime>
						<xsl:call-template name="toISODateTime">
							<xsl:with-param name="datetime">
								<xsl:value-of select="app:dateTime" />
							</xsl:with-param>
						</xsl:call-template>
					</gco:DateTime>
				</gmd:dateTime>
			</xsl:if>
			<xsl:for-each select="app:processor">
				<gmd:processor>
					<xsl:apply-templates select="." />
				</gmd:processor>
			</xsl:for-each>
		</gmd:LI_ProcessStep>
	</xsl:template>
	
	<!-- ========================================================
		template for DQ_Element
		three different DQ_Elements are supported:
		- DQ_AbsoluteExternalPositionalAccuracy
		- DQ_CompletenessCommission
		- DQ_CompletenessOmission
		- DQ_DomainConsistency
		each DQ_Element must contain one QuantitativeResult or ConformanceResult element and may 
		contains two
		===========================================================-->
	<xsl:template match="app:DQ_Element">
		<gmd:report>
			<xsl:if test="app:type = 'DQ_AbsoluteExternalPositionalAccuracy' ">
				<gmd:DQ_AbsoluteExternalPositionalAccuracy>
					<xsl:call-template name="DQnameOfMeasure" />
					<xsl:call-template name="DQmeasureIdent" />
					<xsl:for-each select="app:quantitativeResult" >
						<gmd:result>
							<xsl:apply-templates select="."/>
						</gmd:result>
					</xsl:for-each>
					<xsl:for-each select="app:conformanceResult" >	
						<gmd:result>				
							<xsl:apply-templates select="."/>
						</gmd:result>
					</xsl:for-each>
				</gmd:DQ_AbsoluteExternalPositionalAccuracy>
			</xsl:if>
			<xsl:if test="app:type = 'DQ_CompletenessCommission' ">
				<gmd:DQ_CompletenessCommission>
					<xsl:call-template name="DQnameOfMeasure" />
					<xsl:call-template name="DQmeasureIdent" />
					<xsl:for-each select="app:quantitativeResult/app:DQ_QuantitativeResult" >
						<gmd:result>
							<xsl:apply-templates select="."/>
						</gmd:result>
					</xsl:for-each>
					<xsl:for-each select="app:conformanceResult/app:DQ_ConformanceResult" >					
						<gmd:result>
							<xsl:apply-templates select="."/>
						</gmd:result>
					</xsl:for-each>
				</gmd:DQ_CompletenessCommission>
			</xsl:if>
			<xsl:if test="app:type = 'DQ_CompletenessOmission' ">
				<gmd:DQ_CompletenessOmission>
					<xsl:call-template name="DQnameOfMeasure" />
					<xsl:call-template name="DQmeasureIdent" />
					<xsl:for-each select="app:quantitativeResult/app:DQ_QuantitativeResult" >
						<gmd:result>
							<xsl:apply-templates select="."/>
						</gmd:result>
					</xsl:for-each>
					<xsl:for-each select="app:conformanceResult/app:DQ_ConformanceResult" >					
						<gmd:result>
							<xsl:apply-templates select="."/>
						</gmd:result>
					</xsl:for-each>
				</gmd:DQ_CompletenessOmission>
			</xsl:if>
			<xsl:if test="app:type = 'DQ_DomainConsistency' ">
				<gmd:DQ_DomainConsistency>
					<xsl:call-template name="DQnameOfMeasure" />
					<xsl:call-template name="DQmeasureIdent" />
					<xsl:for-each select="app:quantitativeResult/app:DQ_QuantitativeResult" >
						<gmd:result>
							<xsl:apply-templates select="."/>
						</gmd:result>
					</xsl:for-each>
					<xsl:for-each select="app:conformanceResult/app:DQ_ConformanceResult" >					
						<gmd:result>
							<xsl:apply-templates select="."/>
						</gmd:result>
					</xsl:for-each>
				</gmd:DQ_DomainConsistency>
			</xsl:if>
		</gmd:report>
	</xsl:template>
	
	<xsl:template name="DQnameOfMeasure">
		<xsl:if test="app:nameofmeasure">
			<gmd:nameOfMeasure>
				<gco:CharacterString>
					<xsl:value-of select="app:nameofmeasure" />
				</gco:CharacterString>
			</gmd:nameOfMeasure>
		</xsl:if>
	</xsl:template>
	<xsl:template name="DQmeasureIdent">
		<xsl:if test="app:measureidentcode">
			<gmd:measureIdentification>
				<gmd:RS_Identifier>
					<gmd:code>
						<gco:CharacterString>
							<xsl:value-of select="app:measureidentcode" />
						</gco:CharacterString>
					</gmd:code>
					<xsl:if test="app:measureidentcodespace">
						<gmd:codeSpace>
							<gco:CharacterString>
								<xsl:value-of select="app:measureidentcodespace" />
							</gco:CharacterString>
						</gmd:codeSpace>
					</xsl:if>
				</gmd:RS_Identifier>
			</gmd:measureIdentification>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="app:DQ_QuantitativeResult">
		<gmd:DQ_QuantitativeResult>
			<gmd:valueUnit>
				<gml:UnitDefinition>
					<xsl:attribute name="gml:id">
						<xsl:value-of select="mapping:getId()" />
					</xsl:attribute>
					<gml:identifier>
						<xsl:attribute name="codeSpace">
							<xsl:value-of select="app:codeSpace"/>
						</xsl:attribute>
						<xsl:value-of select="app:identifier"/>
					</gml:identifier>
				</gml:UnitDefinition>
			</gmd:valueUnit>
			<xsl:for-each select="app:value">
				<gmd:value>
					<gco:Record>
						<xsl:value-of select="."/>
					</gco:Record>
				</gmd:value>
			</xsl:for-each>
		</gmd:DQ_QuantitativeResult>
	</xsl:template>
	
	<xsl:template match="app:DQ_ConformanceResult">
		<gmd:DQ_ConformanceResult>
			<gmd:specification>
				<xsl:apply-templates select="app:specification/app:CI_Citation" />
			</gmd:specification>
			<gmd:explanation>
				<gco:CharacterString>
					<xsl:value-of select="app:explanation"/>
				</gco:CharacterString>
			</gmd:explanation>
			<gmd:pass>
				<gco:Boolean>
					<xsl:value-of select="app:pass"/>
				</gco:Boolean>
			</gmd:pass>
		</gmd:DQ_ConformanceResult>
	</xsl:template>
	
	<!-- =====================================================
		template for MD_VectorSpatialRepresentation
		spatialRepresentationInfo
		========================================================-->
	<xsl:template match="app:MD_VectorSpatialReprenstation">
		<gmd:MD_VectorSpatialRepresentation>
			<xsl:if test="app:topoLevelCode">
				<gmd:topologyLevel>
					<gmd:MD_TopologyLevelCode>
						<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_TopologyLevelCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:topoLevelCode/app:MD_TopoLevelCode/app:codelistvalue" />
						</xsl:attribute>
					</gmd:MD_TopologyLevelCode>
				</gmd:topologyLevel>
			</xsl:if>
			<xsl:if test="app:geoTypeObjectTypeCode">
				<gmd:geometricObjects>
					<gmd:MD_GeometricObjects>
						<gmd:geometricObjectType>
							<gmd:MD_GeometricObjectTypeCode>
								<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_GeometricObjectTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">
									<xsl:value-of select="app:geoTypeObjectTypeCode/app:MD_GeoObjTypeCode/app:codelistvalue" />
								</xsl:attribute>
							</gmd:MD_GeometricObjectTypeCode>
						</gmd:geometricObjectType>
						<xsl:if test="app:geoobjcount">
							<gmd:geometricObjectCount>
								<gco:Integer>
									<xsl:value-of select="app:geoobjcount" />
								</gco:Integer>
							</gmd:geometricObjectCount>
						</xsl:if>
					</gmd:MD_GeometricObjects>
				</gmd:geometricObjects>
			</xsl:if>
		</gmd:MD_VectorSpatialRepresentation>
	</xsl:template>
	
	<!-- ========================================================
		template for RS_Identifier
		spatial reference system information
		===========================================================-->
	<xsl:template match="app:RS_Identifier">
		<gmd:RS_Identifier>
			<xsl:for-each select="app:authority">
				<gmd:authority>
					<xsl:apply-templates select="app:CI_Citation" />
				</gmd:authority>
			</xsl:for-each>
			<gmd:code>
				<gco:CharacterString>
					<xsl:value-of select="app:code" />
				</gco:CharacterString>
			</gmd:code>
			<xsl:if test="app:codespace">
				<gmd:codeSpace>
					<gco:CharacterString>
						<xsl:value-of select="app:codespace" />
					</gco:CharacterString>
				</gmd:codeSpace>
			</xsl:if>
			<xsl:if test="app:version">
				<gmd:version>
					<gco:CharacterString>
						<xsl:value-of select="app:version" />
					</gco:CharacterString>
				</gmd:version>
			</xsl:if>
		</gmd:RS_Identifier>
	</xsl:template>
	
	<!-- ==============================================================
		template for FeatureCatalogueDescription 
		content info / feature catalogue description
		=================================================================-->
	<xsl:template match="app:MD_FeatCatDesc">
		<gmd:contentInfo>
			<gmd:MD_FeatureCatalogueDescription>
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
				<gmd:includedWithDataset>
					<gco:Boolean>
						<xsl:value-of select="app:includedwithdataset" />
					</gco:Boolean>
				</gmd:includedWithDataset>
				<xsl:for-each select="app:featureType">
					<gmd:featureTypes>
						<gco:LocalName>
							<xsl:value-of select="app:FeatureTypes/app:localname" />
						</gco:LocalName>
					</gmd:featureTypes>
				</xsl:for-each>
				<xsl:for-each select="app:citation">
					<gmd:featureCatalogueCitation>
						<xsl:apply-templates select="app:CI_Citation" />
					</gmd:featureCatalogueCitation>
				</xsl:for-each>
			</gmd:MD_FeatureCatalogueDescription>
		</gmd:contentInfo>
	</xsl:template>
	
	<!-- ==============================================================
		template for PortayalCatalogueReference
		=================================================================-->
	<xsl:template match="app:MD_PortrayalCatRef">
		<gmd:MD_PortrayalCatalogueReference>
			<gmd:portrayalCatalogueCitation>
				<xsl:apply-templates select="app:citation/app:CI_Citation" />
			</gmd:portrayalCatalogueCitation>
		</gmd:MD_PortrayalCatalogueReference>
	</xsl:template>
	
	<!-- ==============================================================
		distribution info 
		=================================================================-->
	<xsl:template name="distributioninfo">
		<gmd:distributionInfo>
			<gmd:MD_Distribution>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:distributionFormat/app:MD_Format">
					<gmd:distributionFormat>
						<gmd:MD_Format>
							<gmd:name>
								<gco:CharacterString>
									<xsl:value-of select="app:name" />
								</gco:CharacterString>
							</gmd:name>
							<gmd:version>
								<gco:CharacterString>
									<xsl:value-of select="app:version" />
								</gco:CharacterString>
							</gmd:version>
							<xsl:if test="app:amendmentnumber">
								<gmd:amendmentNumber>
									<gco:CharacterString>
										<xsl:value-of select="app:amendmentnumber" />
									</gco:CharacterString>
								</gmd:amendmentNumber>
							</xsl:if>
							<xsl:if test="app:specification">
								<gmd:specification>
									<gco:CharacterString>
										<xsl:value-of select="app:specification" />
									</gco:CharacterString>
								</gmd:specification>
							</xsl:if>
							<xsl:if test="app:filedecomptech">
								<gmd:fileDecompressionTechnique>
									<gco:CharacterString>
										<xsl:value-of select="app:filedecomptech" />
									</gco:CharacterString>
								</gmd:fileDecompressionTechnique>
							</xsl:if>
						</gmd:MD_Format>
					</gmd:distributionFormat>
				</xsl:for-each>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:distributor">
					<xsl:apply-templates select="app:MD_Distributor" />
				</xsl:for-each>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:transferOptions">
					<xsl:apply-templates select="app:MD_DigTransferOpt" />
				</xsl:for-each>
			</gmd:MD_Distribution>
		</gmd:distributionInfo>
	</xsl:template>

	<!-- ==============================================================
		template for Distributor
		=================================================================-->
	<xsl:template match="app:MD_Distributor">
		<gmd:distributor>
			<gmd:MD_Distributor>
				<gmd:distributorContact>
					<xsl:for-each select="app:distributorContact">
						<xsl:apply-templates select="app:CI_RespParty" />
					</xsl:for-each>
				</gmd:distributorContact>
				<xsl:for-each select="app:distributionOrderProcess">
					<gmd:distributionOrderProcess>
						<gmd:MD_StandardOrderProcess>
							<xsl:if test="app:MD_StandOrderProc/app:fees">
								<gmd:fees>
									<gco:CharacterString>
										<xsl:value-of select="app:MD_StandOrderProc/app:fees" />
									</gco:CharacterString>
								</gmd:fees>
							</xsl:if>
							<xsl:if test="app:MD_StandOrderProc/app:orderinginstructions">
								<gmd:orderingInstructions>
									<gco:CharacterString>
										<xsl:value-of select="app:MD_StandOrderProc/app:orderinginstructions" />
									</gco:CharacterString>
								</gmd:orderingInstructions>
							</xsl:if>
							<xsl:if test="app:MD_StandOrderProc/app:turnaround">
								<gmd:turnaround>
									<gco:CharacterString>
										<xsl:value-of select="app:MD_StandOrderProc/app:turnaround" />
									</gco:CharacterString>
								</gmd:turnaround>
							</xsl:if>
						</gmd:MD_StandardOrderProcess>
					</gmd:distributionOrderProcess>
				</xsl:for-each>
			</gmd:MD_Distributor>
		</gmd:distributor>
	</xsl:template>
	
		<!-- ==============================================================
		template for DigitalTransferOptions
		=================================================================-->
	<xsl:template match="app:MD_DigTransferOpt">
		<gmd:transferOptions>
			<gmd:MD_DigitalTransferOptions>
				<xsl:if test="app:unitsofdistribution">
					<gmd:unitsOfDistribution>
						<gco:CharacterString>
							<xsl:value-of select="app:unitsofdistribution" />
						</gco:CharacterString>
					</gmd:unitsOfDistribution>
				</xsl:if>
				<xsl:if test="app:transfersize">
					<gmd:transferSize>
						<gco:Real>
							<xsl:value-of select="app:transfersize" />
						</gco:Real>
					</gmd:transferSize>
				</xsl:if>
				<xsl:for-each select="app:onlineResource">
					<gmd:onLine>
						<xsl:apply-templates select="app:CI_OnlineResource" />
					</gmd:onLine>
				</xsl:for-each>
				<xsl:if test="app:offlineMediumName or app:offlineMediumFormat">
					<gmd:offLine>
						<gmd:MD_Medium>
							<xsl:if test="app:offlineMediumName">
								<gmd:name>
									<gmd:MD_MediumNameCode>
										<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_MediumNameCode</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of
												select="app:offlineMediumName/app:MD_MediumNameCode/app:codelistvalue" />
										</xsl:attribute>
									</gmd:MD_MediumNameCode>
								</gmd:name>
							</xsl:if>
							<xsl:if test="app:offlineMediumFormat">
								<gmd:mediumFormat>
									<gmd:MD_MediumFormatCode>
										<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#MD_MediumFormatCode</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of
												select="app:offlineMediumFormat/app:MD_MediumFormatCode/app:codelistvalue" />
										</xsl:attribute>
									</gmd:MD_MediumFormatCode>
								</gmd:mediumFormat>
							</xsl:if>
							<xsl:if test="app:off_mediumnote">
								<gmd:mediumNote>
									<gco:CharacterString>
										<xsl:value-of select="app:off_mediumnote" />
									</gco:CharacterString>
								</gmd:mediumNote>
							</xsl:if>
						</gmd:MD_Medium>
					</gmd:offLine>
				</xsl:if>
			</gmd:MD_DigitalTransferOptions>
		</gmd:transferOptions>
	</xsl:template>

	<!-- ==============================================================
		template for ApplicationSchemaInformation 
		=================================================================-->
	<xsl:template match="app:MD_ApplicationSchemaInformation">
		<gmd:MD_ApplicationSchemaInformation>
			<gmd:name>
				<xsl:apply-templates select="app:citation/app:CI_Citation" />
			</gmd:name>
			<gmd:schemaLanguage>
				<gco:CharacterString>
					<xsl:value-of select="app:schemaLanguage" />
				</gco:CharacterString>
			</gmd:schemaLanguage>
			<gmd:constraintLanguage>
				<gco:CharacterString>
					<xsl:value-of select="app:constraintLanguage" />
				</gco:CharacterString>
			</gmd:constraintLanguage>
			<xsl:if test="app:schemaAscii">
				<gmd:schemaAscii>
					<gco:CharacterString>
						<xsl:value-of select="app:schemaAscii" />
					</gco:CharacterString>
				</gmd:schemaAscii>
			</xsl:if>
			<xsl:if test="app:graphicsFile64b">
				<gmd:graphicsFile>
					<gco:Binary>
						<xsl:value-of select="app:graphicsFile64b" />
					</gco:Binary>
				</gmd:graphicsFile>
			</xsl:if>
			<xsl:if test="app:softwareDevelFile64b">
				<gmd:softwareDevelopmentFile>
					<gco:Binary>
						<xsl:value-of select="app:softwareDevelFile64b" />
					</gco:Binary>
				</gmd:softwareDevelopmentFile>
			</xsl:if>
			<xsl:if test="app:softwareDevelFileFormat">
				<gmd:softwareDevelopmentFileFormat>
					<gco:CharacterString>
						<xsl:value-of select="app:softwareDevelFileFormat" />
					</gco:CharacterString>
				</gmd:softwareDevelopmentFileFormat>
			</xsl:if>
		</gmd:MD_ApplicationSchemaInformation>
	</xsl:template>
	
	<!-- ==============================================================
		template for BrowseGraphic
		=================================================================-->
	<xsl:template match="app:MD_BrowseGraphic">
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
	</xsl:template>
	
	<!-- ==============================================================
		template for MD_Resolution
		=================================================================-->
	<xsl:template match="app:MD_Resolution">
		<gmd:MD_Resolution>
			<xsl:choose>
				<xsl:when test="app:equivalentscale">
					<gmd:equivalentScale>
						<gmd:MD_RepresentativeFraction>
							<gmd:denominator>
								<gco:Integer>
									<xsl:value-of select="app:equivalentscale" />
								</gco:Integer>
							</gmd:denominator>
						</gmd:MD_RepresentativeFraction>
					</gmd:equivalentScale>
				</xsl:when>
				<xsl:otherwise>
					<gmd:distance>
						<gco:Distance>
							<xsl:attribute name="uom">
								<xsl:value-of select="app:uomName" />
							</xsl:attribute>
							<xsl:value-of select="app:distancevalue" />
						</gco:Distance>
					</gmd:distance>
				</xsl:otherwise>
			</xsl:choose>
		</gmd:MD_Resolution>
	</xsl:template>
	
	<!-- ===============================================
		CI_RespParty minimal
		=================================================-->
	<xsl:template name="ci_respparty_minimal">
		<gmd:CI_ResponsibleParty>
			<gmd:role>
				<gmd:CI_RoleCode>
					<xsl:attribute name="codeList">http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode</xsl:attribute>
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="app:role/app:CI_RoleCode/app:codelistvalue" />
					</xsl:attribute>
				</gmd:CI_RoleCode>
			</gmd:role>
		</gmd:CI_ResponsibleParty>
	</xsl:template>
    
    <!-- ===============================================
        PT_Locale
        =================================================-->
    <xsl:template match="app:PT_Locale">
        <gmd:locale>
            <gmd:PT_Locale>
                <gmd:languageCode>
                    <gmd:LanguageCode>
                        <xsl:attribute name="codeList">#LanguageCode</xsl:attribute>
                        <xsl:attribute name="codeListValue">
                            <xsl:value-of select="app:languageCode" />
                        </xsl:attribute>
                    </gmd:LanguageCode>
                </gmd:languageCode>
                <xsl:if test="boolean( app:country != '' )">
                    <gmd:country>
                        <gmd:Country>
                            <xsl:attribute name="codeList">#Country</xsl:attribute>
                            <xsl:attribute name="codeListValue">
                                <xsl:value-of select="app:country" />
                            </xsl:attribute>
                        </gmd:Country>
                    </gmd:country>
                </xsl:if>
                <gmd:characterEncoding>
                    <gmd:MD_CharacterSetCode>
                        <xsl:attribute name="codeList">#MD_CharacterSetCode</xsl:attribute>
                        <xsl:attribute name="codeListValue">
                            <xsl:value-of select="app:characterEncoding/app:MD_CharacterSetCode/app:codelistvalue" />
                        </xsl:attribute>
                    </gmd:MD_CharacterSetCode>
                </gmd:characterEncoding>
            </gmd:PT_Locale>
        </gmd:locale>
    </xsl:template>
</xsl:stylesheet>
