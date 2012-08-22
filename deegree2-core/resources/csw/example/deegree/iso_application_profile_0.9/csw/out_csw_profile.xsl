<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gml="http://www.opengis.net/gml" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:app="http://www.deegree.org/app" xmlns:iso19115="http://schemas.opengis.net/iso19115full"
	xmlns:iso19115summary="http://schemas.opengis.net/iso19115summary"
	xmlns:iso19115brief="http://schemas.opengis.net/iso19115brief" xmlns:iso19119="http://schemas.opengis.net/iso19119"
	xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:java="java"
	xmlns:minmax="org.deegree.framework.xml.MinMaxExtractor" exclude-result-prefixes="false">
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
									<xsl:value-of
										select="app:MD_Metadata/app:hierarchyLevelCode/app:HierarchyLevelCode/app:codelistvalue" />
								</xsl:with-param>
							</xsl:apply-templates>
						</xsl:for-each>
					</csw:SearchResults>
				</csw:GetRecordsResponse>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="app:MD_Metadata">
		<xsl:param name="HLEVEL" />
		<xsl:if
			test="$ELEMENT_SET = 'brief' and ($HLEVEL = 'dataset' or $HLEVEL = 'series' or $HLEVEL = 'application')">
			<xsl:call-template name="ISO19115BRIEF" />
		</xsl:if>
		<xsl:if
			test="$ELEMENT_SET = 'summary' and ($HLEVEL = 'dataset' or $HLEVEL = 'series' or $HLEVEL = 'application')">
			<xsl:call-template name="ISO19115SUMMARY" />
		</xsl:if>
		<xsl:if
			test="$ELEMENT_SET = 'full' and ($HLEVEL = 'dataset' or $HLEVEL = 'series' or $HLEVEL = 'application')">
			<xsl:call-template name="ISO19115FULL" />
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'brief' and $HLEVEL = 'service'">
			<xsl:call-template name="ISO19119BRIEF" />
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
			<identificationInfo>
				<xsl:call-template name="dataident_brief" />
			</identificationInfo>
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'summary'">
			<identificationInfo>
				<xsl:call-template name="dataident_summary" />
			</identificationInfo>
		</xsl:if>
		<xsl:if test="$ELEMENT_SET = 'full'">
			<iso19115:identificationInfo>
				<xsl:call-template name="dataident" />
			</iso19115:identificationInfo>
		</xsl:if>
	</xsl:template>
	<!-- =================================================
		template for MD_DataIdentification_brief
		====================================================-->
	<xsl:template name="dataident_brief">
		<iso19115brief:MD_DataIdentification>
			<title>
				<smXML:CharacterString>
					<xsl:value-of
						select="app:identificationInfo/app:MD_Identification/app:citation/app:CI_Citation/app:title" />
				</smXML:CharacterString>
			</title>
			<xsl:for-each select="app:topicCategory">
				<topicCategory>
					<smXML:MD_TopicCategoryCode>
						<xsl:value-of select="app:MD_TopicCategoryCode/app:category" />
					</smXML:MD_TopicCategoryCode>
				</topicCategory>
			</xsl:for-each>
			<xsl:for-each select="app:verticalExtent">
				<extent>
					<xsl:apply-templates select="app:EX_VerticalExtent" />
				</extent>
			</xsl:for-each>
			<xsl:for-each select="app:temportalExtent">
				<extent>
					<xsl:apply-templates select="app:EX_TemporalExtent" />
				</extent>
			</xsl:for-each>
			<xsl:for-each select="app:boundingPolygon">
				<extent>
					<xsl:apply-templates select="app:EX_BoundingPolygon" />
				</extent>
			</xsl:for-each>
			<xsl:for-each select="app:geographicIdentifierCode">
				<extent>
					<smXML:EX_Extent>
						<smXML:geographicElement>
							<smXML:EX_GeographicDescription>
								<smXML:geographicIdentifier>
									<smXML:MD_Identifier>
										<smXML:code>
											<smXML:CharacterString>
												<xsl:value-of select="." />
											</smXML:CharacterString>
										</smXML:code>
									</smXML:MD_Identifier>
								</smXML:geographicIdentifier>
							</smXML:EX_GeographicDescription>
						</smXML:geographicElement>
					</smXML:EX_Extent>
				</extent>
			</xsl:for-each>
			<xsl:for-each select="app:boundingBox">
				<extent>
					<xsl:apply-templates select="app:EX_GeogrBBOX" />
				</extent>
			</xsl:for-each>
		</iso19115brief:MD_DataIdentification>
	</xsl:template>
	<!-- =================================================
		template for MD_DataIdentification_summary
		====================================================-->
	<xsl:template name="dataident_summary">
		<iso19115summary:MD_DataIdentification>
			<citation>
				<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:citation">
					<xsl:call-template name="summary_citation" />
				</xsl:for-each>
			</citation>
			<abstract>
				<smXML:CharacterString>
					<xsl:value-of select="app:identificationInfo/app:MD_Identification/app:abstract" />
				</smXML:CharacterString>
			</abstract>
			<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:pointOfContact">
				<smXML:pointOfContact>
					<xsl:apply-templates select="app:CI_RespParty " />
				</smXML:pointOfContact>
			</xsl:for-each>
			<xsl:choose>
				<xsl:when test="boolean( app:identificationInfo/app:MD_Identification/app:legalConstraints )">
					<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:legalConstraints">
						<smXML:resourceConstraints>
							<xsl:apply-templates select="app:MD_LegalConstraints" />
						</smXML:resourceConstraints>
					</xsl:for-each>
				</xsl:when>
				<xsl:when test="boolean( app:identificationInfo/app:MD_Identification/app:securityConstraints )">
					<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:securityConstraints">
						<smXML:resourceConstraints>
							<xsl:apply-templates select="app:MD_SecurityConstraints" />
						</smXML:resourceConstraints>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<resourceConstraints>
						<smXML:MD_Constraints>
							<smXML:useLimitation>
								<smXML:CharacterString>
									Dies ist ein dummy-Wert, der aufgrund eines Fehlers in der ISO19115 summary
									Schemadefinition erforderlich ist.
								</smXML:CharacterString>
							</smXML:useLimitation>
						</smXML:MD_Constraints>
					</resourceConstraints>
				</xsl:otherwise>
			</xsl:choose>

			<xsl:for-each select="app:spatialRepresentationType">
				<spatialRepresentationType>
					<smXML:MD_SpatialRepresentationTypeCode>
						<xsl:attribute name="codeList">MD_SpatialRepresentationTypeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:MD_SpatialRepTypeCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_SpatialRepresentationTypeCode>
				</spatialRepresentationType>
			</xsl:for-each>
			<xsl:for-each select="app:spatialResolution/app:MD_Resolution">
				<spatialResolution>
					<smXML:MD_Resolution>
						<xsl:choose>
							<xsl:when test="app:equivalentscale">
								<smXML:equivalentScale>
									<smXML:MD_RepresentativeFraction>
										<smXML:denominator>
											<smXML:positiveInteger>
												<xsl:value-of select="app:equivalentscale" />
											</smXML:positiveInteger>
										</smXML:denominator>
									</smXML:MD_RepresentativeFraction>
								</smXML:equivalentScale>
							</xsl:when>
							<xsl:otherwise>
								<smXML:distance>
									<smXML:Distance>
										<smXML:value>
											<smXML:Decimal>
												<xsl:value-of select="app:distancevalue" />
											</smXML:Decimal>
											<smXML:nonNegativeInteger>99999</smXML:nonNegativeInteger>
											<smXML:realLongitude>99.99</smXML:realLongitude>
											<smXML:approximateLongitude>-99.99</smXML:approximateLongitude>
											<smXML:approximateLatitude>9.99</smXML:approximateLatitude>
										</smXML:value>
										<smXML:uom>
											<smXML:UomLength>
												<smXML:uomName>
													<smXML:CharacterString>
														<xsl:value-of select="app:uomName" />
													</smXML:CharacterString>
												</smXML:uomName>
												<smXML:conversionTolSOstandardUnit>
													<smXML:Real>
														<xsl:value-of select="app:conversionTolSOstandardUnit" />
													</smXML:Real>
												</smXML:conversionTolSOstandardUnit>
											</smXML:UomLength>
										</smXML:uom>
									</smXML:Distance>
								</smXML:distance>
							</xsl:otherwise>
						</xsl:choose>
					</smXML:MD_Resolution>
				</spatialResolution>
			</xsl:for-each>
			<xsl:for-each select="app:language">
				<language>
					<smXML:CharacterString>
						<xsl:value-of select="." />
					</smXML:CharacterString>
				</language>
			</xsl:for-each>
			<xsl:for-each select="app:characterSet">
				<characterSet>
					<smXML:MD_CharacterSetCode>
						<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:MD_CharacterSetCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_CharacterSetCode>
				</characterSet>
			</xsl:for-each>
			<xsl:for-each select="app:topicCategory">
				<topicCategory>
					<smXML:MD_TopicCategoryCode>
						<xsl:value-of select="app:MD_TopicCategoryCode/app:category" />
					</smXML:MD_TopicCategoryCode>
				</topicCategory>
			</xsl:for-each>
			<xsl:for-each select="app:verticalExtent">
				<extent>
					<xsl:call-template name="summary_VerticalExtent" />
				</extent>
			</xsl:for-each>
			<xsl:for-each select="app:temportalExtent">
				<extent>
					<xsl:call-template name="summary_TemporalExtent" />
				</extent>
			</xsl:for-each>
			<xsl:for-each select="app:boundingPolygon">
				<extent>
					<xsl:call-template name="summary_BoundingPolygon" />
				</extent>
			</xsl:for-each>
			<xsl:for-each select="app:geographicIdentifierCode">
				<extent>
					<iso19115summary:EX_Extent>
						<smXML:geographicElement>
							<smXML:EX_GeographicDescription>
								<smXML:geographicIdentifier>
									<smXML:MD_Identifier>
										<smXML:code>
											<smXML:CharacterString>
												<xsl:value-of select="." />
											</smXML:CharacterString>
										</smXML:code>
									</smXML:MD_Identifier>
								</smXML:geographicIdentifier>
							</smXML:EX_GeographicDescription>
						</smXML:geographicElement>
					</iso19115summary:EX_Extent>
				</extent>
			</xsl:for-each>
			<xsl:for-each select="app:boundingBox">
				<extent>
					<xsl:call-template name="summary_GeogrBBOX" />
				</extent>
			</xsl:for-each>
		</iso19115summary:MD_DataIdentification>
	</xsl:template>
	<!-- ========================================================	
		citation summary
		=========================================================== -->
	<xsl:template name="summary_citation">
		<iso19115summary:CI_Citation>
			<title>
				<smXML:CharacterString>
					<xsl:value-of select="app:CI_Citation/app:title" />
				</smXML:CharacterString>
			</title>
			<xsl:if test="app:CI_Citation/app:revisiondate">
				<date>
					<smXML:CI_Date>
						<smXML:date>
							<smXML:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="app:CI_Citation/app:revisiondate" />
									</xsl:with-param>
								</xsl:call-template>
							</smXML:DateTime>
						</smXML:date>
						<smXML:dateType>
							<smXML:CI_DateTypeCode>
								<xsl:attribute name="codeList">CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">revision</xsl:attribute>
							</smXML:CI_DateTypeCode>
						</smXML:dateType>
					</smXML:CI_Date>
				</date>
			</xsl:if>
			<xsl:if test="app:CI_Citation/app:creationdate">
				<date>
					<smXML:CI_Date>
						<smXML:date>
							<smXML:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="app:CI_Citation/app:creationdate" />
									</xsl:with-param>
								</xsl:call-template>
							</smXML:DateTime>
						</smXML:date>
						<smXML:dateType>
							<smXML:CI_DateTypeCode>
								<xsl:attribute name="codeList">CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">creation</xsl:attribute>
							</smXML:CI_DateTypeCode>
						</smXML:dateType>
					</smXML:CI_Date>
				</date>
			</xsl:if>
			<xsl:if test="app:CI_Citation/app:publicationdate">
				<date>
					<smXML:CI_Date>
						<smXML:date>
							<smXML:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="app:CI_Citation/app:publicationdate" />
									</xsl:with-param>
								</xsl:call-template>
							</smXML:DateTime>
						</smXML:date>
						<smXML:dateType>
							<smXML:CI_DateTypeCode>
								<xsl:attribute name="codeList">CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">publication</xsl:attribute>
							</smXML:CI_DateTypeCode>
						</smXML:dateType>
					</smXML:CI_Date>
				</date>
			</xsl:if>
			<xsl:for-each select="app:CI_Citation/app:identifier">
				<identifier>
					<smXML:MD_Identifier>
						<smXML:code>
							<smXML:CharacterString>
								<xsl:value-of select="." />
							</smXML:CharacterString>
						</smXML:code>
					</smXML:MD_Identifier>
				</identifier>
			</xsl:for-each>
			<xsl:for-each select="app:citedResponsibleParty">
				<citedResponsibleParty>
					<xsl:apply-templates select="app:CI_Citation/app:CI_RespParty" />
				</citedResponsibleParty>
			</xsl:for-each>
		</iso19115summary:CI_Citation>
	</xsl:template>
	<!-- ========================================================	
		vertical extent summary
		=========================================================== -->
	<xsl:template name="summary_VerticalExtent">
		<iso19115summary:EX_Extent>
			<xsl:if test="app:description">
				<description>
					<smXML:CharacterString>
						<xsl:value-of select="app:EX_VerticalExtent/app:description" />
					</smXML:CharacterString>
				</description>
			</xsl:if>
			<verticalElement>
				<smXML:EX_VerticalExtent>
					<smXML:minimumValue>
						<smXML:Real>
							<xsl:value-of select="app:EX_VerticalExtent/app:minval" />
						</smXML:Real>
					</smXML:minimumValue>
					<smXML:maximumValue>
						<smXML:Real>
							<xsl:value-of select="app:EX_VerticalExtent/app:maxval" />
						</smXML:Real>
					</smXML:maximumValue>
					<smXML:unitOfMeasure>
						<smXML:UomLength>
							<smXML:uomName>
								<smXML:CharacterString>
									<xsl:value-of select="app:EX_VerticalExtent/app:uomname" />
								</smXML:CharacterString>
							</smXML:uomName>
							<smXML:conversionTolSOstandardUnit>
								<smXML:Real>
									<xsl:value-of select="app:EX_VerticalExtent/app:convtoisostdunit" />
								</smXML:Real>
							</smXML:conversionTolSOstandardUnit>
						</smXML:UomLength>
					</smXML:unitOfMeasure>
					<smXML:verticalDatum>
						<xsl:apply-templates select="app:EX_VerticalExtent//app:verticalDatum/app:RS_Identifier" />
					</smXML:verticalDatum>
				</smXML:EX_VerticalExtent>
			</verticalElement>
		</iso19115summary:EX_Extent>
	</xsl:template>
	<!-- =======================================================
		temporal extent summary
		==========================================================-->
	<xsl:template name="summary_TemporalExtent">
		<iso19115summary:EX_Extent>
			<xsl:if test="app:description">
				<description>
					<smXML:CharacterString>
						<xsl:value-of select="app:EX_TemporalExtent/app:description" />
					</smXML:CharacterString>
				</description>
			</xsl:if>
			<temporalElement>
				<smXML:EX_TemporalExtent>
					<smXML:extent>
						<smXML:TM_Primitive xsi:type="gml:TimePeriodType">
							<gml:begin>
								<gml:TimeInstant>
									<gml:timePosition>
										<xsl:choose>
											<xsl:when test="contains( app:EX_TemporalExtent/app:begin_, 'T' )">
												<xsl:value-of select="app:EX_TemporalExtent/app:begin_" />
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of
													select="concat( app:EX_TemporalExtent/app:begin_, 'T00:00:00')" />
											</xsl:otherwise>
										</xsl:choose>
									</gml:timePosition>
								</gml:TimeInstant>
							</gml:begin>
							<gml:end>
								<gml:TimeInstant>
									<gml:timePosition>
										<xsl:choose>
											<xsl:when test="contains( app:EX_TemporalExtent/app:end_, 'T' )">
												<xsl:value-of select="app:EX_TemporalExtent/app:end_" />
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of
													select="concat( app:EX_TemporalExtent/app:end_, 'T00:00:00')" />
											</xsl:otherwise>
										</xsl:choose>
									</gml:timePosition>
								</gml:TimeInstant>
							</gml:end>
						</smXML:TM_Primitive>
					</smXML:extent>
				</smXML:EX_TemporalExtent>
			</temporalElement>
		</iso19115summary:EX_Extent>
	</xsl:template>
	<!-- ========================================================
		bounding polygon summary
		===========================================================-->
	<xsl:template name="summary_BoundingPolygon">
		<iso19115summary:EX_Extent>
			<xsl:if test="app:description">
				<description>
					<smXML:CharacterString>
						<xsl:value-of select="app:EX_BoundingPolygon/app:description" />
					</smXML:CharacterString>
				</description>
			</xsl:if>
			<geographicElement>
				<smXML:EX_BoundingPolygon>
					<smXML:polygon />
				</smXML:EX_BoundingPolygon>
			</geographicElement>
		</iso19115summary:EX_Extent>
	</xsl:template>
	<!-- ========================================================
		geographic bbox summary
		===========================================================-->
	<xsl:template name="summary_GeogrBBOX">
		<iso19115summary:EX_Extent>
			<xsl:if test="app:description">
				<description>
					<smXML:CharacterString>
						<xsl:value-of select="app:EX_GeogrBBOX/app:description" />
					</smXML:CharacterString>
				</description>
			</xsl:if>
			<geographicElement>
				<smXML:EX_GeographicBoundingBox>
					<smXML:extentTypeCode>
						<smXML:Boolean>1</smXML:Boolean>
					</smXML:extentTypeCode>
					<smXML:westBoundLongitude>
						<smXML:approximateLongitude>
							<xsl:value-of select="minmax:getXMin( ./app:EX_GeogrBBOX/app:geom/child::*[1] )" />
						</smXML:approximateLongitude>
					</smXML:westBoundLongitude>
					<smXML:eastBoundLongitude>
						<smXML:approximateLongitude>
							<xsl:value-of select="minmax:getXMax( ./app:EX_GeogrBBOX/app:geom/child::*[1] )" />
						</smXML:approximateLongitude>
					</smXML:eastBoundLongitude>
					<smXML:southBoundLatitude>
						<smXML:approximateLatitude>
							<xsl:value-of select="minmax:getYMin( ./app:EX_GeogrBBOX/app:geom/child::*[1] )" />
						</smXML:approximateLatitude>
					</smXML:southBoundLatitude>
					<smXML:northBoundLatitude>
						<smXML:approximateLatitude>
							<xsl:value-of select="minmax:getYMax( ./app:EX_GeogrBBOX/app:geom/child::*[1] )" />
						</smXML:approximateLatitude>
					</smXML:northBoundLatitude>
				</smXML:EX_GeographicBoundingBox>
			</geographicElement>
		</iso19115summary:EX_Extent>
	</xsl:template>
	<!-- ==============================================================
		distribution info summary
		=================================================================-->
	<xsl:template name="summary_distributioninfo">
		<distributionInfo>
			<iso19115summary:MD_Distribution>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:distributionFormat/app:MD_Format">
					<distributionFormat>
						<iso19115summary:MD_Format>
							<name>
								<smXML:CharacterString>
									<xsl:value-of select="app:name" />
								</smXML:CharacterString>
							</name>
							<version>
								<smXML:CharacterString>
									<xsl:value-of select="app:version" />
								</smXML:CharacterString>
							</version>
							<xsl:if test="app:specification">
								<specification>
									<smXML:CharacterString>
										<xsl:value-of select="app:specification" />
									</smXML:CharacterString>
								</specification>
							</xsl:if>
							<xsl:if test="app:filedecomptech">
								<fileDecompressionTechnique>
									<smXML:CharacterString>
										<xsl:value-of select="app:filedecomptech" />
									</smXML:CharacterString>
								</fileDecompressionTechnique>
							</xsl:if>
						</iso19115summary:MD_Format>
					</distributionFormat>
				</xsl:for-each>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:transferOptions">
					<xsl:call-template name="summary_DigTransferOpt" />
				</xsl:for-each>
			</iso19115summary:MD_Distribution>
		</distributionInfo>
	</xsl:template>
	<!-- =================================================
		template for MD_DigitalTransferOptions summary
		====================================================-->
	<xsl:template name="summary_DigTransferOpt">
		<transferOptions>
			<iso19115summary:MD_DigitalTransferOptions>
				<xsl:if test="app:MD_DigTransferOpt/app:unitsofdistribution">
					<unitsOfDistribution>
						<smXML:CharacterString>
							<xsl:value-of select="app:MD_DigTransferOpt/app:unitsofdistribution" />
						</smXML:CharacterString>
					</unitsOfDistribution>
				</xsl:if>
				<xsl:if test="app:MD_DigTransferOpt/app:onlineResource">
					<onLine>
						<xsl:apply-templates select="app:MD_DigTransferOpt/app:onlineResource/app:CI_OnlineResource" />
					</onLine>
				</xsl:if>
				<xsl:if
					test="app:MD_DigTransferOpt/app:offlineMediumName or app:MD_DigTransferOpt/app:offlineMediumFormat">
					<offLine>
						<iso19115summary:MD_Medium>
							<xsl:if test="app:MD_DigTransferOpt/app:offlineMediumName">
								<name>
									<smXML:MD_MediumNameCode>
										<xsl:attribute name="codeList">MD_MediumNameCode</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of
												select="app:MD_DigTransferOpt/app:offlineMediumName/app:MD_MediumNameCode/app:codelistvalue" />
										</xsl:attribute>
									</smXML:MD_MediumNameCode>
								</name>
							</xsl:if>
							<xsl:if test="app:MD_DigTransferOpt/app:offlineMediumFormat">
								<mediumFormat>
									<smXML:MD_MediumFormatCode>
										<xsl:attribute name="codeList">MD_MediumFormatCode</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of
												select="app:MD_DigTransferOpt/app:offlineMediumFormat/app:MD_MediumFormatCode/app:codelistvalue" />
										</xsl:attribute>
									</smXML:MD_MediumFormatCode>
								</mediumFormat>
							</xsl:if>
						</iso19115summary:MD_Medium>
					</offLine>
				</xsl:if>
				<xsl:if test="app:transfersize">
					<transferSize>
						<smXML:CharacterString>
							<xsl:value-of select="app:MD_DigTransferOpt/app:transfersize" />
						</smXML:CharacterString>
					</transferSize>
				</xsl:if>
			</iso19115summary:MD_DigitalTransferOptions>
		</transferOptions>
	</xsl:template>
	<!-- =================================================
		template for MD_DataIdentification full
		====================================================-->
	<xsl:template name="dataident">
		<smXML:MD_DataIdentification>
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

			<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:legalConstraints">
				<smXML:resourceConstraints>
					<xsl:apply-templates select="app:MD_LegalConstraints" />
				</smXML:resourceConstraints>
			</xsl:for-each>
			<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:securityConstraints">
				<smXML:resourceConstraints>
					<xsl:apply-templates select="app:MD_SecurityConstraints" />
				</smXML:resourceConstraints>
			</xsl:for-each>

			<xsl:for-each select="app:identificationInfo/app:MD_Identification/app:resourceMaintenance">
				<xsl:apply-templates select="." />
			</xsl:for-each>
			<xsl:for-each select="app:spatialRepresentationType">
				<smXML:spatialRepresentationType>
					<smXML:MD_SpatialRepresentationTypeCode>
						<xsl:attribute name="codeList">MD_SpatialRepresentationTypeCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:MD_SpatialRepTypeCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_SpatialRepresentationTypeCode>
				</smXML:spatialRepresentationType>
			</xsl:for-each>
			<xsl:for-each select="app:spatialResolution/app:MD_Resolution">
				<smXML:spatialResolution>
					<smXML:MD_Resolution>
						<xsl:choose>
							<xsl:when test="app:equivalentscale">
								<smXML:equivalentScale>
									<smXML:MD_RepresentativeFraction>
										<smXML:denominator>
											<smXML:positiveInteger>
												<xsl:value-of select="app:equivalentscale" />
											</smXML:positiveInteger>
										</smXML:denominator>
									</smXML:MD_RepresentativeFraction>
								</smXML:equivalentScale>
							</xsl:when>
							<xsl:otherwise>
								<smXML:distance>
									<smXML:Distance>
										<smXML:value>
											<smXML:Decimal>
												<xsl:value-of select="app:distancevalue" />
											</smXML:Decimal>
											<smXML:nonNegativeInteger>99999</smXML:nonNegativeInteger>
											<smXML:realLongitude>99.99</smXML:realLongitude>
											<smXML:approximateLongitude>-99.99</smXML:approximateLongitude>
											<smXML:approximateLatitude>9.99</smXML:approximateLatitude>
										</smXML:value>
										<smXML:uom>
											<smXML:UomLength>
												<smXML:uomName>
													<smXML:CharacterString>
														<xsl:value-of select="app:uomName" />
													</smXML:CharacterString>
												</smXML:uomName>
												<smXML:conversionTolSOstandardUnit>
													<smXML:Real>
														<xsl:value-of select="app:conversionTolSOstandardUnit" />
													</smXML:Real>
												</smXML:conversionTolSOstandardUnit>
											</smXML:UomLength>
										</smXML:uom>
									</smXML:Distance>
								</smXML:distance>
							</xsl:otherwise>
						</xsl:choose>
					</smXML:MD_Resolution>
				</smXML:spatialResolution>
			</xsl:for-each>
			<xsl:for-each select="app:language">
				<smXML:language>
					<smXML:CharacterString>
						<xsl:value-of select="." />
					</smXML:CharacterString>
				</smXML:language>
			</xsl:for-each>
			<xsl:if test="app:supplementalInformation">
				<smXML:supplementalInformation>
					<smXML:CharacterString>
						<xsl:value-of select="app:supplementalInformation" />
					</smXML:CharacterString>
				</smXML:supplementalInformation>
			</xsl:if>
			<xsl:for-each select="app:characterSet">
				<smXML:characterSet>
					<smXML:MD_CharacterSetCode>
						<xsl:attribute name="codeList">MD_CharacterSetCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:MD_CharacterSetCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_CharacterSetCode>
				</smXML:characterSet>
			</xsl:for-each>
			<xsl:for-each select="app:topicCategory">
				<smXML:topicCategory>
					<smXML:MD_TopicCategoryCode>
						<xsl:value-of select="app:MD_TopicCategoryCode/app:category" />
					</smXML:MD_TopicCategoryCode>
				</smXML:topicCategory>
			</xsl:for-each>
			<xsl:for-each select="app:verticalExtent">
				<smXML:extent>
					<xsl:apply-templates select="app:EX_VerticalExtent" />
				</smXML:extent>
			</xsl:for-each>
			<xsl:for-each select="app:temportalExtent">
				<smXML:extent>
					<xsl:apply-templates select="app:EX_TemporalExtent" />
				</smXML:extent>
			</xsl:for-each>
			<xsl:for-each select="app:boundingPolygon">
				<smXML:extent>
					<xsl:apply-templates select="app:EX_BoundingPolygon" />
				</smXML:extent>
			</xsl:for-each>
			<xsl:for-each select="app:geographicIdentifierCode">
				<smXML:extent>
					<smXML:EX_Extent>
						<smXML:geographicElement>
							<smXML:EX_GeographicDescription>
								<smXML:geographicIdentifier>
									<smXML:MD_Identifier>
										<smXML:code>
											<smXML:CharacterString>
												<xsl:value-of select="." />
											</smXML:CharacterString>
										</smXML:code>
									</smXML:MD_Identifier>
								</smXML:geographicIdentifier>
							</smXML:EX_GeographicDescription>
						</smXML:geographicElement>
					</smXML:EX_Extent>
				</smXML:extent>
			</xsl:for-each>
			<xsl:for-each select="app:boundingBox">
				<smXML:extent>
					<xsl:apply-templates select="app:EX_GeogrBBOX" />
				</smXML:extent>
			</xsl:for-each>
		</smXML:MD_DataIdentification>
	</xsl:template>
	<!-- ===============================================
		template for CI_CitedResponisbleParty
		=================================================-->
	<xsl:template match="app:CI_RespParty | app:CI_Citation/app:CI_RespParty">
		<smXML:CI_ResponsibleParty>
			<xsl:if test="app:individualname">
				<smXML:individualName>
					<smXML:CharacterString>
						<xsl:value-of select="app:individualname" />
					</smXML:CharacterString>
				</smXML:individualName>
			</xsl:if>
			<xsl:if test="app:organisationname">
				<smXML:organisationName>
					<smXML:CharacterString>
						<xsl:value-of select="app:organisationname" />
					</smXML:CharacterString>
				</smXML:organisationName>
			</xsl:if>
			<xsl:if test="app:positionname">
				<smXML:positionName>
					<smXML:CharacterString>
						<xsl:value-of select="app:positionname" />
					</smXML:CharacterString>
				</smXML:positionName>
			</xsl:if>
			<xsl:for-each select="app:contactInfo">
				<xsl:apply-templates select="app:CI_Contact" />
			</xsl:for-each>
			<smXML:role>
				<smXML:CI_RoleCode>
					<xsl:attribute name="codeList">CI_RoleCode</xsl:attribute>
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="app:role/app:CI_RoleCode/app:codelistvalue" />
					</xsl:attribute>
				</smXML:CI_RoleCode>
			</smXML:role>
		</smXML:CI_ResponsibleParty>
	</xsl:template>
	<xsl:template match="app:CI_Contact">
		<smXML:contactInfo>
			<smXML:CI_Contact>
				<xsl:if test="app:voice or app:facsimile">
					<smXML:phone>
						<smXML:CI_Telephone>
							<xsl:for-each select="app:voice">
								<smXML:voice>
									<smXML:CharacterString>
										<xsl:value-of select="." />
									</smXML:CharacterString>
								</smXML:voice>
							</xsl:for-each>
							<xsl:for-each select="app:facsimile">
								<smXML:facsimile>
									<smXML:CharacterString>
										<xsl:value-of select="." />
									</smXML:CharacterString>
								</smXML:facsimile>
							</xsl:for-each>
						</smXML:CI_Telephone>
					</smXML:phone>
				</xsl:if>
				<xsl:apply-templates select="app:address/app:CI_Address" />
				<xsl:for-each select="app:onlineResource">
					<smXML:onlineResource>
						<xsl:apply-templates select="app:CI_OnlineResource" />
					</smXML:onlineResource>
				</xsl:for-each>
				<xsl:if test="app:hoursofservice">
					<smXML:hoursOfService>
						<smXML:CharacterString>
							<xsl:value-of select="app:hoursofservice" />
						</smXML:CharacterString>
					</smXML:hoursOfService>
				</xsl:if>
				<xsl:if test="app:contactinstructions">
					<smXML:contactInstructions>
						<smXML:CharacterString>
							<xsl:value-of select="app:contactinstructions" />
						</smXML:CharacterString>
					</smXML:contactInstructions>
				</xsl:if>
			</smXML:CI_Contact>
		</smXML:contactInfo>
	</xsl:template>
	<xsl:template match="app:address/app:CI_Address">
		<smXML:address>
			<smXML:CI_Address>
				<xsl:for-each select="app:deliveryPoint">
					<smXML:deliveryPoint>
						<smXML:CharacterString>
							<xsl:value-of select="app:DeliveryPoint/app:deliverypoint" />
						</smXML:CharacterString>
					</smXML:deliveryPoint>
				</xsl:for-each>
				<xsl:if test="app:city">
					<smXML:city>
						<smXML:CharacterString>
							<xsl:value-of select="app:city" />
						</smXML:CharacterString>
					</smXML:city>
				</xsl:if>
				<xsl:if test="app:administrativeArea">
					<smXML:administrativeArea>
						<smXML:CharacterString>
							<xsl:value-of select="app:administrativeArea" />
						</smXML:CharacterString>
					</smXML:administrativeArea>
				</xsl:if>
				<xsl:if test="app:postalCode">
					<smXML:postalCode>
						<smXML:CharacterString>
							<xsl:value-of select="app:postalCode" />
						</smXML:CharacterString>
					</smXML:postalCode>
				</xsl:if>
				<xsl:if test="app:country">
					<smXML:country>
						<smXML:CharacterString>
							<xsl:value-of select="app:country" />
						</smXML:CharacterString>
					</smXML:country>
				</xsl:if>
				<xsl:for-each select="app:electronicMailAddress">
					<smXML:electronicMailAddress>
						<smXML:CharacterString>
							<xsl:value-of select="app:ElectronicMailAddress/app:email" />
						</smXML:CharacterString>
					</smXML:electronicMailAddress>
				</xsl:for-each>
			</smXML:CI_Address>
		</smXML:address>
	</xsl:template>
	<!-- ============================================
		template for CI_OnlineResource
		===============================================-->
	<xsl:template match="app:CI_OnlineResource">
		<smXML:CI_OnlineResource>
			<smXML:linkage>
				<smXML:URL>
					<xsl:value-of select="app:linkage" />
				</smXML:URL>
			</smXML:linkage>
			<xsl:if test="app:function">
				<smXML:function>
					<smXML:CI_OnLineFunctionCode>
						<xsl:attribute name="codeList">CI_OnLineFunctionCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:function/app:CI_OnLineFunctionCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:CI_OnLineFunctionCode>
				</smXML:function>
			</xsl:if>
		</smXML:CI_OnlineResource>
	</xsl:template>
	<!-- =================================================
		template for CI_Citation
		====================================================-->
	<xsl:template match="app:CI_Citation">
		<smXML:CI_Citation>
			<smXML:title>
				<smXML:CharacterString>
					<xsl:value-of select="app:title" />
				</smXML:CharacterString>
			</smXML:title>
			<xsl:for-each select="app:alternateTitle">
				<smXML:alternateTitle>
					<smXML:CharacterString>
						<xsl:value-of select="." />
					</smXML:CharacterString>
				</smXML:alternateTitle>
			</xsl:for-each>
			<xsl:if test="app:revisiondate">
				<smXML:date>
					<smXML:CI_Date>
						<smXML:date>
							<smXML:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="app:revisiondate" />
									</xsl:with-param>
								</xsl:call-template>
							</smXML:DateTime>
						</smXML:date>
						<smXML:dateType>
							<smXML:CI_DateTypeCode>
								<xsl:attribute name="codeList">CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">revision</xsl:attribute>
							</smXML:CI_DateTypeCode>
						</smXML:dateType>
					</smXML:CI_Date>
				</smXML:date>
			</xsl:if>
			<xsl:if test="app:creationdate">
				<smXML:date>
					<smXML:CI_Date>
						<smXML:date>
							<smXML:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="app:creationdate" />
									</xsl:with-param>
								</xsl:call-template>
							</smXML:DateTime>
						</smXML:date>
						<smXML:dateType>
							<smXML:CI_DateTypeCode>
								<xsl:attribute name="codeList">CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">creation</xsl:attribute>
							</smXML:CI_DateTypeCode>
						</smXML:dateType>
					</smXML:CI_Date>
				</smXML:date>
			</xsl:if>
			<xsl:if test="app:publicationdate">
				<smXML:date>
					<smXML:CI_Date>
						<smXML:date>
							<smXML:DateTime>
								<xsl:call-template name="toISODateTime">
									<xsl:with-param name="datetime">
										<xsl:value-of select="app:publicationdate" />
									</xsl:with-param>
								</xsl:call-template>
							</smXML:DateTime>
						</smXML:date>
						<smXML:dateType>
							<smXML:CI_DateTypeCode>
								<xsl:attribute name="codeList">CI_DateTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">publication</xsl:attribute>
							</smXML:CI_DateTypeCode>
						</smXML:dateType>
					</smXML:CI_Date>
				</smXML:date>
			</xsl:if>
			<xsl:if test="app:edition">
				<smXML:edition>
					<smXML:CharacterString>
						<xsl:value-of select="app:edition" />
					</smXML:CharacterString>
				</smXML:edition>
			</xsl:if>
			<xsl:if test="app:editiondate">
				<smXML:editionDate>
					<smXML:DateTime>
						<xsl:call-template name="toISODateTime">
							<xsl:with-param name="datetime">
								<xsl:value-of select="app:editiondate" />
							</xsl:with-param>
						</xsl:call-template>
					</smXML:DateTime>
				</smXML:editionDate>
			</xsl:if>
			<xsl:for-each select="app:identifier">
				<smXML:identifier>
					<smXML:MD_Identifier>
						<smXML:code>
							<smXML:CharacterString>
								<xsl:value-of select="." />
							</smXML:CharacterString>
						</smXML:code>
					</smXML:MD_Identifier>
				</smXML:identifier>
			</xsl:for-each>
			<xsl:for-each select="app:citedResponsibleParty">
				<smXML:citedResponsibleParty>
					<xsl:apply-templates select="app:CI_RespParty" />
				</smXML:citedResponsibleParty>
			</xsl:for-each>
			<xsl:for-each select="app:presentationForm">
				<smXML:presentationForm>
					<smXML:CI_PresentationFormCode codeList="CI_PresentationFormCode">
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:CI_PresentationFormCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:CI_PresentationFormCode>
				</smXML:presentationForm>
			</xsl:for-each>
			<xsl:if test="app:series">
				<smXML:series>
					<xsl:for-each select="app:series/app:CI_Series">
						<smXML:CI_Series>
							<xsl:if test="app:name">
								<smXML:name>
									<smXML:CharacterString>
										<xsl:value-of select="app:name" />
									</smXML:CharacterString>
								</smXML:name>
							</xsl:if>
							<xsl:if test="app:issueidentification">
								<smXML:issueIdentification>
									<smXML:CharacterString>
										<xsl:value-of select="app:issueidentification" />
									</smXML:CharacterString>
								</smXML:issueIdentification>
							</xsl:if>
							<xsl:if test="app:page">
								<smXML:page>
									<smXML:CharacterString>
										<xsl:value-of select="app:page" />
									</smXML:CharacterString>
								</smXML:page>
							</xsl:if>
						</smXML:CI_Series>
					</xsl:for-each>
				</smXML:series>
			</xsl:if>
			<xsl:if test="app:isbn">
				<smXML:ISBN>
					<smXML:CharacterString>
						<xsl:value-of select="app:isbn" />
					</smXML:CharacterString>
				</smXML:ISBN>
			</xsl:if>
			<xsl:if test="app:issn">
				<smXML:ISSN>
					<smXML:CharacterString>
						<xsl:value-of select="app:issn" />
					</smXML:CharacterString>
				</smXML:ISSN>
			</xsl:if>
		</smXML:CI_Citation>
	</xsl:template>
	<!-- ==================================================
		template for LegalConstraints
		=====================================================-->
	<xsl:template match="app:MD_LegalConstraints">
		<smXML:MD_LegalConstraints>
			<xsl:if test="boolean( app:useLimitations )">
				<smXML:useLimitation>
					<smXML:CharacterString>
						<xsl:value-of select="app:useLimitations" />
					</smXML:CharacterString>
				</smXML:useLimitation>
			</xsl:if>
			<xsl:for-each select="app:accessConstraints">
				<smXML:accessConstraints>
					<xsl:for-each select="app:MD_RestrictionCode">
						<smXML:MD_RestrictionCode>
							<xsl:attribute name="codeList">MD_RestrictionCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:codelistvalue" />
							</xsl:attribute>
						</smXML:MD_RestrictionCode>
					</xsl:for-each>
				</smXML:accessConstraints>
			</xsl:for-each>
			<xsl:for-each select="app:useConstraints">
				<smXML:useConstraints>
					<xsl:for-each select="app:MD_RestrictionCode">
						<smXML:MD_RestrictionCode>
							<xsl:attribute name="codeList">MD_RestrictionCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:codelistvalue" />
							</xsl:attribute>
						</smXML:MD_RestrictionCode>
					</xsl:for-each>
				</smXML:useConstraints>
			</xsl:for-each>
			<xsl:for-each select="app:otherConstraints">
				<smXML:otherConstraints>
					<smXML:CharacterString>
						<xsl:value-of select="." />
					</smXML:CharacterString>
				</smXML:otherConstraints>
			</xsl:for-each>
		</smXML:MD_LegalConstraints>
	</xsl:template>
	<!-- ==================================================
		template for SecurityConstraints
		=====================================================-->
	<xsl:template match="app:MD_SecurityConstraints">
		<smXML:MD_SecurityConstraints>
			<smXML:classification>
				<smXML:MD_RestrictionCode>
					<xsl:attribute name="codeList">MD_RestrictionCode</xsl:attribute>
					<xsl:attribute name="codeListValue">
						<xsl:value-of select="app:classification/app:MD_ClassificationCode/app:codelistvalue" />
					</xsl:attribute>
				</smXML:MD_RestrictionCode>
			</smXML:classification>
			<xsl:if test="boolean( app:userNote )">
				<smXML:userNote>
					<smXML:CharacterString>
						<xsl:value-of select="app:userNote" />
					</smXML:CharacterString>
				</smXML:userNote>
			</xsl:if>
			<xsl:if test="boolean( app:classificationSystem )">
				<smXML:classificationSystem>
					<smXML:CharacterString>
						<xsl:value-of select="app:classificationSystem" />
					</smXML:CharacterString>
				</smXML:classificationSystem>
			</xsl:if>
			<xsl:if test="boolean( app:handlingDescription )">
				<smXML:handlingDescription>
					<smXML:CharacterString>
						<xsl:value-of select="app:handlingDescription" />
					</smXML:CharacterString>
				</smXML:handlingDescription>
			</xsl:if>
			<xsl:if test="boolean( app:useLimitations )">
				<smXML:useLimitation>
					<smXML:CharacterString>
						<xsl:value-of select="app:useLimitations" />
					</smXML:CharacterString>
				</smXML:useLimitation>
			</xsl:if>
		</smXML:MD_SecurityConstraints>
	</xsl:template>

	<xsl:template match="app:MD_Identification/app:resourceMaintenance">
		<smXML:resourceMaintenance>
			<smXML:MD_MaintenanceInformation>
				<smXML:maintenanceAndUpdateFrequency>
					<smXML:MD_MaintenanceFrequencyCode>
						<xsl:attribute name="codeList">MD_MaintenanceFrequencyCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of
								select="app:MD_MaintenanceInformation/app:maintenanceAndUpdateFrequency/app:MD_MainFreqCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_MaintenanceFrequencyCode>
				</smXML:maintenanceAndUpdateFrequency>
				<xsl:if test="boolean( app:MD_MaintenanceInformation/app:dateOfNextUpdate )">
					<smXML:dateOfNextUpdate>
						<smXML:DateTime>
							<xsl:call-template name="toISODateTime">
								<xsl:with-param name="datetime">
									<xsl:value-of select="app:MD_MaintenanceInformation/app:dateOfNextUpdate" />
								</xsl:with-param>
							</xsl:call-template>
						</smXML:DateTime>
					</smXML:dateOfNextUpdate>
				</xsl:if>
				<xsl:if test="boolean( app:MD_MaintenanceInformation/app:userDefinedMaintenanceFrequency )">
					<smXML:userDefinedMaintenanceFrequency>
						<smXML:TM_PeriodDuration>
							<xsl:value-of select="app:MD_MaintenanceInformation/app:userDefinedMaintenanceFrequency" />
						</smXML:TM_PeriodDuration>
					</smXML:userDefinedMaintenanceFrequency>
				</xsl:if>
				<xsl:if test="boolean( app:MD_MaintenanceInformation/app:updateScope)">
					<smXML:updateScope>
						<smXML:MD_ScopeCode codeList="MD_ScopeCode">
							<xsl:attribute name="codeListValue">
								<xsl:value-of
									select="app:MD_MaintenanceInformation/app:updateScope/app:MD_ScopeCode/app:codelistvalue" />
							</xsl:attribute>
						</smXML:MD_ScopeCode>
					</smXML:updateScope>
				</xsl:if>
				<xsl:if test="app:MD_MaintenanceInformation/app:note">
					<smXML:maintenanceNote>
						<smXML:CharacterString>
							<xsl:value-of select="app:MD_MaintenanceInformation/app:note" />
						</smXML:CharacterString>
					</smXML:maintenanceNote>
				</xsl:if>
			</smXML:MD_MaintenanceInformation>
		</smXML:resourceMaintenance>
	</xsl:template>
	<!-- =======================================================
		vertical extent
		==========================================================-->
	<xsl:template match="app:EX_VerticalExtent">
		<smXML:EX_Extent>
			<xsl:if test="app:description">
				<smXML:description>
					<smXML:CharacterString>
						<xsl:value-of select="app:description" />
					</smXML:CharacterString>
				</smXML:description>
			</xsl:if>
			<smXML:verticalElement>
				<smXML:EX_VerticalExtent>
					<smXML:minimumValue>
						<smXML:Real>
							<xsl:value-of select="app:minval" />
						</smXML:Real>
					</smXML:minimumValue>
					<smXML:maximumValue>
						<smXML:Real>
							<xsl:value-of select="app:maxval" />
						</smXML:Real>
					</smXML:maximumValue>
					<smXML:unitOfMeasure>
						<smXML:UomLength>
							<smXML:uomName>
								<smXML:CharacterString>
									<xsl:value-of select="app:uomname" />
								</smXML:CharacterString>
							</smXML:uomName>
							<smXML:conversionTolSOstandardUnit>
								<smXML:Real>
									<xsl:value-of select="app:convtoisostdunit" />
								</smXML:Real>
							</smXML:conversionTolSOstandardUnit>
						</smXML:UomLength>
					</smXML:unitOfMeasure>
					<smXML:verticalDatum>
						<xsl:apply-templates select="app:verticalDatum/app:RS_Identifier" />
					</smXML:verticalDatum>
				</smXML:EX_VerticalExtent>
			</smXML:verticalElement>
		</smXML:EX_Extent>
	</xsl:template>
	<!-- =======================================================
		temporal extent
		==========================================================-->
	<xsl:template match="app:EX_TemporalExtent">
		<smXML:EX_Extent>
			<xsl:if test="app:description">
				<smXML:description>
					<smXML:CharacterString>
						<xsl:value-of select="app:description" />
					</smXML:CharacterString>
				</smXML:description>
			</xsl:if>
			<smXML:temporalElement>
				<smXML:EX_TemporalExtent>
					<smXML:extent>
						<smXML:TM_Primitive xsi:type="gml:TimePeriodType">
							<gml:begin>
								<gml:TimeInstant>
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
						</smXML:TM_Primitive>
					</smXML:extent>
				</smXML:EX_TemporalExtent>
			</smXML:temporalElement>
		</smXML:EX_Extent>
	</xsl:template>
	<!-- ========================================================
		bounding polygon
		===========================================================-->
	<xsl:template match="app:EX_BoundingPolygon">
		<smXML:EX_Extent>
			<xsl:if test="app:description">
				<smXML:description>
					<smXML:CharacterString>
						<xsl:value-of select="app:description" />
					</smXML:CharacterString>
				</smXML:description>
			</xsl:if>
			<smXML:geographicElement>
				<smXML:EX_BoundingPolygon>
					<smXML:Polygon />
				</smXML:EX_BoundingPolygon>
			</smXML:geographicElement>
		</smXML:EX_Extent>
	</xsl:template>
	<!-- ========================================================
		geographic bbox
		===========================================================-->
	<xsl:template match="app:EX_GeogrBBOX">
		<smXML:EX_Extent>
			<xsl:if test="app:description">
				<smXML:description>
					<smXML:CharacterString>
						<xsl:value-of select="app:description" />
					</smXML:CharacterString>
				</smXML:description>
			</xsl:if>
			<smXML:geographicElement>
				<smXML:EX_GeographicBoundingBox>
					<smXML:extentTypeCode>
						<smXML:Boolean>1</smXML:Boolean>
					</smXML:extentTypeCode>
					<smXML:westBoundLongitude>
						<smXML:approximateLongitude>
							<xsl:value-of select="minmax:getXMin( ./app:geom/child::*[1] )" />
						</smXML:approximateLongitude>
					</smXML:westBoundLongitude>
					<smXML:eastBoundLongitude>
						<smXML:approximateLongitude>
							<xsl:value-of select="minmax:getXMax( ./app:geom/child::*[1] )" />
						</smXML:approximateLongitude>
					</smXML:eastBoundLongitude>
					<smXML:southBoundLatitude>
						<smXML:approximateLatitude>
							<xsl:value-of select="minmax:getYMin( ./app:geom/child::*[1] )" />
						</smXML:approximateLatitude>
					</smXML:southBoundLatitude>
					<smXML:northBoundLatitude>
						<smXML:approximateLatitude>
							<xsl:value-of select="minmax:getYMax( ./app:geom/child::*[1] )" />
						</smXML:approximateLatitude>
					</smXML:northBoundLatitude>
				</smXML:EX_GeographicBoundingBox>
			</smXML:geographicElement>
		</smXML:EX_Extent>
	</xsl:template>
	<!-- ==========================================================
		data quality
		=============================================================-->
	<!-- das alte
		<xsl:template match="app:DQ_DataQuality">
		<smXML:DQ_DataQuality>
		<smXML:scope>
		<smXML:DQ_Scope>
		<smXML:level>
		<smXML:MD_ScopeCode>
		<xsl:attribute name="codeList">MD_ScopeCode</xsl:attribute>
		<xsl:attribute name="codeListValue"><xsl:value-of select="app:scopelevelcodelistvalue"/></xsl:attribute>
		</smXML:MD_ScopeCode>
		</smXML:level>
		
		</smXML:DQ_Scope>
		</smXML:scope>
		<xsl:if test="app:lineagestatement or app:lineagesourcedesc or app:lineageprocessdesc or app:lineageprocessdesc">
		<smXML:lineage>
		<smXML:LI_Lineage>
		<xsl:if test="app:lineagestatement">
		<smXML:statement>
		<smXML:CharacterString>
		<xsl:value-of select="app:lineagestatement"/>
		</smXML:CharacterString>
		</smXML:statement>
		</xsl:if>
		<xsl:if test="app:lineagesourcedesc">
		<smXML:source>
		<smXML:LI_Source>
		<smXML:description>
		<smXML:CharacterString>
		<xsl:value-of select="app:lineagesourcedesc"/>
		</smXML:CharacterString>
		</smXML:description>
		</smXML:LI_Source>
		</smXML:source>
		</xsl:if>
		<xsl:apply-templates select="app:lineageprocessdesc"/>
		</smXML:LI_Lineage>
		</smXML:lineage>
		</xsl:if>
		<xsl:for-each select="app:DQ_Element">
		<xsl:apply-templates select="app:DQ_Element"/>
		</xsl:for-each>
		</smXML:DQ_DataQuality>
		</xsl:template>
		<xsl:template match="app:lineageprocessdesc">
		<smXML:processStep>
		<smXML:LI_ProcessStep>
		<smXML:description>
		<smXML:CharacterString>
		<xsl:value-of select="."/>
		</smXML:CharacterString>
		</smXML:description>
		<xsl:if test="boolean( ../app:lineageprocessdatetime )">
		<smXML:dateTime>
		<smXML:DateTime>
		<xsl:call-template name="toISODateTime">
		<xsl:with-param name="datetime">
		<xsl:value-of select="../app:lineageprocessdatetime"/>
		</xsl:with-param>
		</xsl:call-template>
		</smXML:DateTime>
		</smXML:dateTime>
		</xsl:if>
		</smXML:LI_ProcessStep>
		</smXML:processStep>
		</xsl:template>-->

	<xsl:template match="app:DQ_DataQuality">
		<smXML:DQ_DataQuality>
			<smXML:scope>
				<smXML:DQ_Scope>
					<smXML:level>
						<smXML:MD_ScopeCode>
							<xsl:attribute name="codeList">MD_ScopeCode</xsl:attribute>
							<xsl:attribute name="codeListValue">
								<xsl:value-of select="app:scopelevelcodelistvalue" />
							</xsl:attribute>
						</smXML:MD_ScopeCode>
					</smXML:level>
					<!--xsl:if test="app:scopelevedescription">
						<smXML:levelDescription>
						<xsl:value-of select="app:scopelevedescription"/>
						</smXML:levelDescription>
						</xsl:if-->
				</smXML:DQ_Scope>
			</smXML:scope>

			<xsl:if test="app:lineagestatement or app:LI_Source or app:LI_ProcessStep">
				<smXML:lineage>
					<smXML:LI_Lineage>
						<xsl:if test="app:lineagestatement">
							<smXML:statement>
								<smXML:CharacterString>
									<xsl:value-of select="app:lineagestatement" />
								</smXML:CharacterString>
							</smXML:statement>
						</xsl:if>

						<xsl:for-each select="app:LI_Source">
							<smXML:source>
								<smXML:LI_Source>
									<xsl:if test="app:LI_Source/app:description">
										<smXML:description>
											<smXML:CharacterString>
												<xsl:value-of select="app:LI_Source/app:description" />
											</smXML:CharacterString>
										</smXML:description>
									</xsl:if>
									<xsl:if test="app:LI_Source/app:scaleDenominator">
										<smXML:scaleDenominator>
											<smXML:Integer>
												<xsl:value-of select="app:LI_Source/app:scaleDenominator" />
											</smXML:Integer>
										</smXML:scaleDenominator>
									</xsl:if>
									<xsl:if test="app:LI_Source/app:sourceCitation">
										<smXML:sourceCitation>
											<xsl:apply-templates
												select="app:LI_Source/app:sourceCitation/app:CI_Citation" />
										</smXML:sourceCitation>
									</xsl:if>
									<xsl:if test="app:LI_Source/app:sourceReferenceSystem">
										<smXML:sourceReferenceSystem>
											<xsl:apply-templates
												select="app:LI_Source/app:sourceReferenceSystem/app:RS_Identifier" />
										</smXML:sourceReferenceSystem>
									</xsl:if>
									<xsl:for-each select="app:LI_Source/app:sourceStep">
										<smXML:sourceStep>
											<xsl:apply-templates select="app:LI_ProcessStep" />
										</smXML:sourceStep>
									</xsl:for-each>
								</smXML:LI_Source>
							</smXML:source>
						</xsl:for-each>

						<xsl:for-each select="app:LI_ProcessStep">
							<smXML:processStep>
								<xsl:apply-templates select="app:LI_ProcessStep" />
							</smXML:processStep>
						</xsl:for-each>

					</smXML:LI_Lineage>
				</smXML:lineage>
			</xsl:if>

			<xsl:for-each select="app:DQ_Element">
				<xsl:apply-templates select="app:DQ_Element" />
			</xsl:for-each>
		</smXML:DQ_DataQuality>
	</xsl:template>

	<xsl:template match="app:LI_ProcessStep">
		<smXML:LI_ProcessStep>
			<smXML:description>
				<smXML:CharacterString>
					<xsl:value-of select="app:description" />
				</smXML:CharacterString>
			</smXML:description>
			<xsl:if test="app:rationale">
				<smXML:rationale>
					<smXML:CharacterString>
						<xsl:value-of select="app:rationale" />
					</smXML:CharacterString>
				</smXML:rationale>
			</xsl:if>
			<xsl:if test="app:dateTime">
				<smXML:DateTime>
					<xsl:call-template name="toISODateTime">
						<xsl:with-param name="datetime">
							<xsl:value-of select="app:dateTime" />
						</xsl:with-param>
					</xsl:call-template>
				</smXML:DateTime>
			</xsl:if>
			<xsl:if test="app:processor">
				<smXML:processor>
					<xsl:apply-templates select="app:processor/app:CI_RespParty" />
				</smXML:processor>
			</xsl:if>
		</smXML:LI_ProcessStep>
	</xsl:template>
	<!-- ========================================================
		four different DQ_Elements are supported:
		- DQ_AbsoluteExternalPositionalAccuracy
		- DQ_CompletenessCommission
		each DQ_Element must contain one QuantitativeResult element and may 
		contains two
		===========================================================-->
	<xsl:template match="app:DQ_Element">
		<smXML:report>
			<xsl:if test="app:type = 'DQ_AbsoluteExternalPositionalAccuracy' ">
				<smXML:DQ_AbsoluteExternalPositionalAccuracy>
					<xsl:call-template name="DQnameOfMeasure" />
					<xsl:call-template name="dqelement1" />
					<xsl:if test="app:uomname2">
						<xsl:call-template name="dqelement2" />
					</xsl:if>
				</smXML:DQ_AbsoluteExternalPositionalAccuracy>
			</xsl:if>
			<xsl:if test="app:type = 'DQ_CompletenessCommission' ">
				<smXML:DQ_CompletenessCommission>
					<xsl:call-template name="DQnameOfMeasure" />
					<xsl:call-template name="dqelement1" />
					<xsl:if test="app:uomname2">
						<xsl:call-template name="dqelement2" />
					</xsl:if>
				</smXML:DQ_CompletenessCommission>
			</xsl:if>
		</smXML:report>
	</xsl:template>
	<xsl:template name="DQnameOfMeasure">
		<xsl:if test="app:nameofmeasure">
			<smXML:nameOfMeasure>
				<smXML:CharacterString>
					<xsl:value-of select="app:nameofmeasure" />
				</smXML:CharacterString>
			</smXML:nameOfMeasure>
		</xsl:if>
	</xsl:template>
	<xsl:template name="dqelement1">
		<smXML:DQ_QuantitativeResult>
			<smXML:UomLength>
				<smXML:uomName>
					<smXML:CharacterString>
						<xsl:value-of select="app:uomname1" />
					</smXML:CharacterString>
				</smXML:uomName>
				<smXML:conversionTolSOstandardUnit>
					<smXML:Real>
						<xsl:value-of select="app:convtoisostdunit1" />
					</smXML:Real>
				</smXML:conversionTolSOstandardUnit>
			</smXML:UomLength>
			<smXML:value>
				<smXML:Record>
					<xsl:value-of select="app:value1" />
				</smXML:Record>
			</smXML:value>
		</smXML:DQ_QuantitativeResult>
	</xsl:template>
	<xsl:template name="dqelement2">
		<smXML:DQ_QuantitativeResult>
			<smXML:UomLength>
				<smXML:uomName>
					<smXML:CharacterString>
						<xsl:value-of select="app:uomname2" />
					</smXML:CharacterString>
				</smXML:uomName>
				<smXML:conversionTolSOstandardUnit>
					<smXML:Real>
						<xsl:value-of select="app:convtoisostdunit2" />
					</smXML:Real>
				</smXML:conversionTolSOstandardUnit>
			</smXML:UomLength>
			<smXML:value>
				<smXML:Record>
					<xsl:value-of select="app:value2" />
				</smXML:Record>
			</smXML:value>
		</smXML:DQ_QuantitativeResult>
	</xsl:template>
	<!-- =====================================================
		spatialRepresentationInfo
		========================================================-->
	<xsl:template match="app:MD_VectorSpatialReprenstation">
		<smXML:MD_VectorSpatialRepresentation>
			<xsl:if test="app:topoLevelCode">
				<smXML:topologyLevel>
					<smXML:MD_TopologyLevelCode>
						<xsl:attribute name="codeList">MD_TopologyLevelCode</xsl:attribute>
						<xsl:attribute name="codeListValue">
							<xsl:value-of select="app:topoLevelCode/app:MD_TopoLevelCode/app:codelistvalue" />
						</xsl:attribute>
					</smXML:MD_TopologyLevelCode>
				</smXML:topologyLevel>
			</xsl:if>
			<xsl:if test="app:geoTypeObjectTypeCode">
				<smXML:geometricObjects>
					<smXML:MD_GeometricObjects>
						<smXML:geometricObjectType>
							<smXML:MD_GeometricObjectTypeCode>
								<xsl:attribute name="codeList">MD_GeometricObjectTypeCode</xsl:attribute>
								<xsl:attribute name="codeListValue">
									<xsl:value-of
										select="app:geoTypeObjectTypeCode/app:MD_GeoObjTypeCode/app:codelistvalue" />
								</xsl:attribute>
							</smXML:MD_GeometricObjectTypeCode>
						</smXML:geometricObjectType>
						<xsl:if test="app:geoobjcount">
							<smXML:geometricObjectCount>
								<smXML:CharacterString>
									<xsl:value-of select="app:geoobjcount" />
								</smXML:CharacterString>
							</smXML:geometricObjectCount>
						</xsl:if>
					</smXML:MD_GeometricObjects>
				</smXML:geometricObjects>
			</xsl:if>
		</smXML:MD_VectorSpatialRepresentation>
	</xsl:template>
	<!-- ========================================================
		spatial reference system information
		===========================================================-->
	<xsl:template match="app:RS_Identifier">
		<smXML:RS_Identifier>
			<xsl:for-each select="app:authority">
				<smXML:authority>
					<xsl:apply-templates select="app:CI_Citation" />
				</smXML:authority>
			</xsl:for-each>
			<smXML:code>
				<smXML:CharacterString>
					<xsl:value-of select="app:code" />
				</smXML:CharacterString>
			</smXML:code>
			<xsl:if test="app:codespace">
				<smXML:codeSpace>
					<smXML:CharacterString>
						<xsl:value-of select="app:codespace" />
					</smXML:CharacterString>
				</smXML:codeSpace>
			</xsl:if>
			<xsl:if test="app:version">
				<smXML:version>
					<smXML:CharacterString>
						<xsl:value-of select="app:version" />
					</smXML:CharacterString>
				</smXML:version>
			</xsl:if>
		</smXML:RS_Identifier>
	</xsl:template>
	<!-- ==============================================================
		content info / feature catalogue description
		=================================================================-->
	<xsl:template match="app:MD_FeatCatDesc">
		<iso19115:contentInfo>
			<smXML:MD_FeatureCatalogueDescription>
				<xsl:if test="app:language">
					<smXML:language>
						<smXML:CharacterString>
							<xsl:value-of select="app:language" />
						</smXML:CharacterString>
					</smXML:language>
				</xsl:if>
				<smXML:includedWithDataset>
					<smXML:Boolean>
						<xsl:value-of select="app:includedwithdataset" />
					</smXML:Boolean>
				</smXML:includedWithDataset>
				<xsl:for-each select="app:featureType">
					<smXML:LocalName>
						<xsl:value-of select="app:FeatureTypes/app:localname" />
					</smXML:LocalName>
				</xsl:for-each>
				<xsl:for-each select="app:citation">
					<smXML:featureCatalogueCitation>
						<xsl:apply-templates select="app:CI_Citation" />
					</smXML:featureCatalogueCitation>
				</xsl:for-each>
			</smXML:MD_FeatureCatalogueDescription>
		</iso19115:contentInfo>
	</xsl:template>
	<!-- ==============================================================
		portayal catalogue info
		=================================================================-->
	<xsl:template match="app:MD_PortrayalCatRef">
		<smXML:MD_PortrayalCatalogueReference>
			<smXML:portrayalCatalogueCitation>
				<xsl:apply-templates select="app:citation/app:CI_Citation" />
			</smXML:portrayalCatalogueCitation>
		</smXML:MD_PortrayalCatalogueReference>
	</xsl:template>
	<!-- ==============================================================
		distribution info 
		=================================================================-->

	<xsl:template name="distributioninfo">
		<iso19115:distributionInfo>
			<smXML:MD_Distribution>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:distributionFormat/app:MD_Format">
					<smXML:distributionFormat>
						<smXML:MD_Format>
							<smXML:name>
								<smXML:CharacterString>
									<xsl:value-of select="app:name" />
								</smXML:CharacterString>
							</smXML:name>
							<smXML:version>
								<smXML:CharacterString>
									<xsl:value-of select="app:version" />
								</smXML:CharacterString>
							</smXML:version>
							<xsl:if test="app:specification">
								<smXML:specification>
									<smXML:CharacterString>
										<xsl:value-of select="app:specification" />
									</smXML:CharacterString>
								</smXML:specification>
							</xsl:if>
							<xsl:if test="app:filedecomptech">
								<smXML:fileDecompressionTechnique>
									<smXML:CharacterString>
										<xsl:value-of select="app:filedecomptech" />
									</smXML:CharacterString>
								</smXML:fileDecompressionTechnique>
							</xsl:if>
							<xsl:if test="app:amendmentnumber">
								<smXML:amendmentNumber>
									<smXML:CharacterString>
										<xsl:value-of select="app:amendmentnumber" />
									</smXML:CharacterString>
								</smXML:amendmentNumber>
							</xsl:if>
						</smXML:MD_Format>
					</smXML:distributionFormat>
				</xsl:for-each>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:distributor">
					<xsl:apply-templates select="app:MD_Distributor" />
				</xsl:for-each>
				<xsl:for-each select="app:distributionInfo/app:MD_Distribution/app:transferOptions">
					<xsl:apply-templates select="app:MD_DigTransferOpt" />
				</xsl:for-each>
			</smXML:MD_Distribution>
		</iso19115:distributionInfo>
	</xsl:template>

	<xsl:template match="app:MD_Distributor">
		<smXML:distributor>
			<smXML:MD_Distributor>
				<smXML:distributorContact>
					<xsl:for-each select="app:distributorContact">
						<xsl:apply-templates select="app:CI_RespParty" />
					</xsl:for-each>
				</smXML:distributorContact>
				<xsl:for-each select="app:distributionOrderProcess">
					<smXML:distributionOrderProcess>
						<smXML:MD_StandardOrderProcess>
							<xsl:if test="app:MD_StandOrderProc/app:fees">
								<smXML:fees>
									<smXML:CharacterString>
										<xsl:value-of select="app:MD_StandOrderProc/app:fees" />
									</smXML:CharacterString>
								</smXML:fees>
							</xsl:if>
							<xsl:if test="app:MD_StandOrderProc/app:orderinginstructions">
								<smXML:orderingInstructions>
									<smXML:CharacterString>
										<xsl:value-of select="app:MD_StandOrderProc/app:orderinginstructions" />
									</smXML:CharacterString>
								</smXML:orderingInstructions>
							</xsl:if>
							<xsl:if test="app:MD_StandOrderProc/app:turnaround">
								<smXML:turnaround>
									<smXML:CharacterString>
										<xsl:value-of select="app:MD_StandOrderProc/app:turnaround" />
									</smXML:CharacterString>
								</smXML:turnaround>
							</xsl:if>
						</smXML:MD_StandardOrderProcess>
					</smXML:distributionOrderProcess>
				</xsl:for-each>
			</smXML:MD_Distributor>
		</smXML:distributor>
	</xsl:template>
	<xsl:template match="app:MD_DigTransferOpt">
		<smXML:transferOptions>
			<smXML:MD_DigitalTransferOptions>
				<xsl:if test="app:unitsofdistribution">
					<smXML:unitsOfDistribution>
						<smXML:CharacterString>
							<xsl:value-of select="app:unitsofdistribution" />
						</smXML:CharacterString>
					</smXML:unitsOfDistribution>
				</xsl:if>
				<xsl:if test="app:onlineResource">
					<smXML:onLine>
						<xsl:apply-templates select="app:onlineResource/app:CI_OnlineResource" />
					</smXML:onLine>
				</xsl:if>
				<xsl:if test="app:offlineMediumName or app:offlineMediumFormat">
					<smXML:offLine>
						<smXML:MD_Medium>
							<xsl:if test="app:offlineMediumName">
								<smXML:name>
									<smXML:MD_MediumNameCode>
										<xsl:attribute name="codeList">MD_MediumNameCode</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of
												select="app:offlineMediumName/app:MD_MediumNameCode/app:codelistvalue" />
										</xsl:attribute>
									</smXML:MD_MediumNameCode>
								</smXML:name>
							</xsl:if>
							<xsl:if test="app:offlineMediumFormat">
								<smXML:mediumFormat>
									<smXML:MD_MediumFormatCode>
										<xsl:attribute name="codeList">MD_MediumFormatCode</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of
												select="app:offlineMediumFormat/app:MD_MediumFormatCode/app:codelistvalue" />
										</xsl:attribute>
									</smXML:MD_MediumFormatCode>
								</smXML:mediumFormat>
							</xsl:if>
							<xsl:if test="app:off_mediumnote">
								<smXML:mediumNote>
									<smXML:CharacterString>
										<xsl:value-of select="app:off_mediumnote" />
									</smXML:CharacterString>
								</smXML:mediumNote>
							</xsl:if>
						</smXML:MD_Medium>
					</smXML:offLine>
				</xsl:if>
				<xsl:if test="app:transfersize">
					<smXML:transferSize>
						<smXML:CharacterString>
							<xsl:value-of select="app:transfersize" />
						</smXML:CharacterString>
					</smXML:transferSize>
				</xsl:if>
			</smXML:MD_DigitalTransferOptions>
		</smXML:transferOptions>
	</xsl:template>

	<!-- ==============================================================
		applicationSchemaInformation 
		=================================================================-->
	<xsl:template match="app:MD_ApplicationSchemaInformation">
		<smXML:MD_ApplicationSchemaInformation>
			<smXML:name>
				<xsl:apply-templates select="app:citation/app:CI_Citation" />
			</smXML:name>
			<smXML:schemaLanguage>
				<smXML:CharacterString>
					<xsl:value-of select="app:schemaLanguage" />
				</smXML:CharacterString>
			</smXML:schemaLanguage>
			<smXML:constraintLanguage>
				<smXML:CharacterString>
					<xsl:value-of select="app:constraintLanguage" />
				</smXML:CharacterString>
			</smXML:constraintLanguage>
			<xsl:if test="app:schemaAscii">
				<smXML:schemaAscii>
					<smXML:CharacterString>
						<xsl:value-of select="app:schemaAscii" />
					</smXML:CharacterString>
				</smXML:schemaAscii>
			</xsl:if>
			<xsl:if test="app:graphicsFile64b">
				<smXML:graphicsFile>
					<smXML:b64Binary>
						<xsl:value-of select="app:graphicsFile64b" />
					</smXML:b64Binary>
				</smXML:graphicsFile>
			</xsl:if>
			<xsl:if test="app:graphicsFileHex">
				<smXML:graphicsFile>
					<smXML:hexBinary>
						<xsl:value-of select="app:graphicsFileHex" />
					</smXML:hexBinary>
				</smXML:graphicsFile>
			</xsl:if>
			<xsl:if test="app:softwareDevelFile64b">
				<smXML:softwareDevelopmentFile>
					<smXML:b64Binary>
						<xsl:value-of select="app:softwareDevelFile64b" />
					</smXML:b64Binary>
				</smXML:softwareDevelopmentFile>
			</xsl:if>
			<xsl:if test="app:softwareDevelFileHex">
				<smXML:softwareDevelopmentFile>
					<smXML:hexBinary>
						<xsl:value-of select="app:softwareDevelFileHex" />
					</smXML:hexBinary>
				</smXML:softwareDevelopmentFile>
			</xsl:if>
			<xsl:if test="app:softwareDevelFileFormat">
				<smXML:softwareDevelopmentFileFormat>
					<smXML:CharacterString>
						<xsl:value-of select="app:softwareDevelFileFormat" />
					</smXML:CharacterString>
				</smXML:softwareDevelopmentFileFormat>
			</xsl:if>
		</smXML:MD_ApplicationSchemaInformation>
	</xsl:template>

</xsl:stylesheet>
