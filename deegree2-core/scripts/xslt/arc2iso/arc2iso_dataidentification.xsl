<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
 xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="DATAIDENTIFICATION">
		<iso19115:identificationInfo>
			<smXML:MD_DataIdentification>
				<smXML:citation>
					<xsl:apply-templates select="idinfo/citation/citeinfo"/>
				</smXML:citation>
				<smXML:abstract>
					<smXML:CharacterString>
						<xsl:value-of select="idinfo/descript/abstract"/>
					</smXML:CharacterString>
				</smXML:abstract>
				<smXML:purpose>
					<smXML:CharacterString>
						<xsl:value-of select="idinfo/descript/purpose"/>
					</smXML:CharacterString>
				</smXML:purpose>
				<smXML:status>
					<smXML:MD_ProgressCode codeList="MD_ProgressCode">
						<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getProgressCode( idinfo/status/progress )"/></xsl:attribute>
					</smXML:MD_ProgressCode>
				</smXML:status>
				
				<xsl:if test="idinfo/ptcontac/cntinfo">
					<smXML:pointOfContact>
						<xsl:apply-templates select="idinfo/ptcontac/cntinfo"/>
					</smXML:pointOfContact>
				</xsl:if>
				
				<xsl:if test="idinfo/keywords/theme">
					<xsl:apply-templates select="idinfo/keywords/theme"/>
				</xsl:if>
				
				<xsl:if test="idinfo/keywords/place">
					<xsl:apply-templates select="idinfo/keywords/place"/>
				</xsl:if>
				
				<xsl:if test="idinfo/keywords/stratum">
					<xsl:apply-templates select="idinfo/keywords/stratum"/>
				</xsl:if>
				
				<xsl:if test="idinfo/keywords/temporal">
					<xsl:apply-templates select="idinfo/keywords/temporal"/>
				</xsl:if>
				
				<xsl:if test="idinfo/browse">
					<smXML:graphicOverview>
						<MD_BrowseGraphic>
							<smXML:fileName>
								<smXML:CharacterString>
									<xsl:value-of select="idinfo/browse/browsen"/>
								</smXML:CharacterString>
							</smXML:fileName>
							<smXML:fileDescription>
								<smXML:CharacterString>
									<xsl:value-of select="idinfo/browse/browsed"/>
								</smXML:CharacterString>
							</smXML:fileDescription>
							<smXML:fileType>
								<smXML:CharacterString>
									<xsl:value-of select="idinfo/browse/browset"/>
								</smXML:CharacterString>
							</smXML:fileType>
						</MD_BrowseGraphic>
					</smXML:graphicOverview>
				</xsl:if>
			
				<xsl:call-template name="RESOURCECONSTR"/>
				
				<smXML:resourceFormat>
					<smXML:MD_Format>
						<smXML:name>
							<smXML:CharacterString>
								<xsl:value-of select="idinfo/natvform"/>
							</smXML:CharacterString>
						</smXML:name>
						<!-- ESRI does not provide version informations for resource/native format -->
						<smXML:version>
							<smXML:CharacterString>1.0.0</smXML:CharacterString>
						</smXML:version>
					</smXML:MD_Format>
				</smXML:resourceFormat>
				
				<smXML:language>
					<smXML:CharacterString>
						<xsl:value-of select="idinfo/descript/langdata"/>
					</smXML:CharacterString>
				</smXML:language>
				
				<smXML:topicCategory>
					<smXML:MD_TopicCategoryCode>
						<xsl:value-of select="arc2iso:getTopCatTypeCode( dataIdInfo/tpCat/TopicCatCd/@value )"></xsl:value-of>
					</smXML:MD_TopicCategoryCode>
				</smXML:topicCategory>
								
				<xsl:if test="idinfo/native">
					<smXML:environmentDescription>
						<smXML:CharacterString>
							<xsl:value-of select="idinfo/native"/>
						</smXML:CharacterString>
					</smXML:environmentDescription>
				</xsl:if>
				<xsl:apply-templates select="idinfo/spdom/bounding"/>
				
				<xsl:if test="idinfo/descript/supplinf">
					<smXML:supplementalInformation>
						<smXML:CharacterString>
							<xsl:value-of select="idinfo/descript/supplinf"/>
						</smXML:CharacterString>
					</smXML:supplementalInformation>
				</xsl:if>
				
			</smXML:MD_DataIdentification>
		</iso19115:identificationInfo>
	</xsl:template>
	
	<xsl:template match="keywords/theme">
		<smXML:descriptiveKeywords>
			<smXML:MD_Keywords>
				<xsl:for-each select="themekey">
					<smXML:keyword>
						<smXML:CharacterString>
							<xsl:value-of select="."/>
						</smXML:CharacterString>
					</smXML:keyword>
				</xsl:for-each>
				<smXML:type>
					<smXML:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="theme"/>
				</smXML:type>
				<smXML:thesaurusName>
					<smXML:CI_Citation>
						<smXML:title>
							<smXML:CharacterString>
								<xsl:value-of select="themekt"/>
							</smXML:CharacterString>
						</smXML:title>
						<!-- esri unbekannt -->
						<smXML:date>
							<smXML:CI_Date>
								<smXML:date>
									<smXML:Date>
										<xsl:value-of select="arc2iso:getISODate( ../../../metainfo/metd )"/>
									</smXML:Date>
								</smXML:date>
								<smXML:dateType>
									<smXML:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
								</smXML:dateType>
							</smXML:CI_Date>
						</smXML:date>
					</smXML:CI_Citation>
				</smXML:thesaurusName>
			</smXML:MD_Keywords>
		</smXML:descriptiveKeywords>
	</xsl:template>
	
	<xsl:template match="keywords/place">
		<smXML:descriptiveKeywords>
			<smXML:MD_Keywords>
				<xsl:for-each select="placekey">
					<smXML:keyword>
						<smXML:CharacterString>
							<xsl:value-of select="."/>
						</smXML:CharacterString>
					</smXML:keyword>
				</xsl:for-each>
				<smXML:type>
					<smXML:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="place"/>
				</smXML:type>
				<xsl:if test="themekt">
					<smXML:ThesaurusName>
						<smXML:CI_Citation>
							<smXML:title>
								<smXML:CharacterString>
									<xsl:value-of select="themekt"/>
								</smXML:CharacterString>
							</smXML:title>
							<!-- esri unbekannt -->
							<smXML:date>
								<smXML:CI_Date>
									<smXML:date>
										<smXML:Date>
											<xsl:value-of select="arc2iso:getISODate( ../../../metainfo/metd )"/>
										</smXML:Date>
									</smXML:date>
									<smXML:dateType>
										<smXML:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
									</smXML:dateType>
								</smXML:CI_Date>
							</smXML:date>
						</smXML:CI_Citation>
					</smXML:ThesaurusName>
				</xsl:if>
			</smXML:MD_Keywords>
		</smXML:descriptiveKeywords>
	</xsl:template>
	<xsl:template match="keywords/stratum">
		<smXML:descriptiveKeywords>
			<smXML:MD_Keywords>
				<xsl:for-each select="stratumkey">
					<smXML:keyword>
						<smXML:CharacterString>
							<xsl:value-of select="."/>
						</smXML:CharacterString>
					</smXML:keyword>
				</xsl:for-each>
				<smXML:type>
					<smXML:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="stratum"/>
				</smXML:type>
				<xsl:if test="themekt">
					<smXML:ThesaurusName>
						<smXML:CI_Citation>
							<smXML:title>
								<smXML:CharacterString>
									<xsl:value-of select="themekt"/>
								</smXML:CharacterString>
							</smXML:title>
							<!-- esri unbekannt -->
							<smXML:date>
								<smXML:CI_Date>
									<smXML:date>
										<smXML:Date>
											<xsl:value-of select="arc2iso:getISODate( ../../../metainfo/metd )"/>
										</smXML:Date>
									</smXML:date>
									<smXML:dateType>
										<smXML:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
									</smXML:dateType>
								</smXML:CI_Date>
							</smXML:date>
						</smXML:CI_Citation>
					</smXML:ThesaurusName>
				</xsl:if>
			</smXML:MD_Keywords>
		</smXML:descriptiveKeywords>
	</xsl:template>
	<xsl:template match="keywords/temporal">
		<smXML:descriptiveKeywords>
			<smXML:MD_Keywords>
				<xsl:for-each select="temporalkey">
					<smXML:keyword>
						<smXML:CharacterString>
							<xsl:value-of select="."/>
						</smXML:CharacterString>
					</smXML:keyword>
				</xsl:for-each>
				<smXML:type>
					<smXML:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="temporal"/>
				</smXML:type>
				<xsl:if test="themekt">
					<smXML:ThesaurusName>
						<smXML:CI_Citation>
							<smXML:title>
								<smXML:CharacterString>
									<xsl:value-of select="themekt"/>
								</smXML:CharacterString>
							</smXML:title>
							<!-- esri unbekannt -->
							<smXML:date>
								<smXML:CI_Date>
									<smXML:date>
										<smXML:Date>
											<xsl:value-of select="arc2iso:getISODate( ../../../metainfo/metd )"/>
										</smXML:Date>
									</smXML:date>
									<smXML:dateType>
										<smXML:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
									</smXML:dateType>
								</smXML:CI_Date>
							</smXML:date>
						</smXML:CI_Citation>
					</smXML:ThesaurusName>
				</xsl:if>
			</smXML:MD_Keywords>
		</smXML:descriptiveKeywords>
	</xsl:template>
	
<!--	if info/useconst mandatory-->
	<xsl:template name="RESOURCECONSTR">
		<smXML:resourceConstraints>
			<smXML:MD_LegalConstraints>
				<smXML:useLimitation>
					<smXML:CharacterString>
						<xsl:value-of select="idinfo/useconst"/>
					</smXML:CharacterString>
				</smXML:useLimitation>
				<smXML:otherConstraints>
					<smXML:CharacterString>
						<xsl:value-of select="idinfo/accconst"/>
					</smXML:CharacterString>
				</smXML:otherConstraints>
			</smXML:MD_LegalConstraints>
		</smXML:resourceConstraints>
		<xsl:if test="boolean( idinfo/secinfo )">
		<!-- 		
			not supported at the moment
			<smXML:resourceConstraints>
				<smXML:MD_SecurityConstraints>
					<smXML:classification>
						<smXML:MD_ClassificationCode codeList="MD_ClassificationCode">
							<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getSecurityCode( idinfo/secinfo/secclass )"/></xsl:attribute>
						</smXML:MD_ClassificationCode>
					</smXML:classification>
					<smXML:classificationSystem>
						<smXML:CharacterString>
							<xsl:value-of select="idinfo/secinfo/secsys"/>
						</smXML:CharacterString>
					</smXML:classificationSystem>
					<smXML:handlingDescription>
						<smXML:CharacterString>
							<xsl:value-of select="idinfo/secinfo/sechandl"/>
						</smXML:CharacterString>
					</smXML:handlingDescription>
				</smXML:MD_SecurityConstraints>
			</smXML:resourceConstraints>
		 -->			
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="idinfo/spdom/bounding">
		<smXML:extent>
			<smXML:EX_Extent>
				<smXML:geographicElement>
					<smXML:EX_GeographicBoundingBox>
						<smXML:westBoundLongitude>
							<smXML:approximateLongitude>
								<xsl:value-of select="arc2iso:formatCoord( westbc )"/>
							</smXML:approximateLongitude>
						</smXML:westBoundLongitude>
						<smXML:eastBoundLongitude>
							<smXML:approximateLongitude>
								<xsl:value-of select="arc2iso:formatCoord( eastbc )"/>
							</smXML:approximateLongitude>
						</smXML:eastBoundLongitude>
						<smXML:southBoundLatitude>
							<smXML:approximateLatitude>
								<xsl:value-of select="arc2iso:formatCoord( southbc )"/>
							</smXML:approximateLatitude>
						</smXML:southBoundLatitude>
						<smXML:northBoundLatitude>
							<smXML:approximateLatitude>
								<xsl:value-of select="arc2iso:formatCoord( northbc )"/>
							</smXML:approximateLatitude>
						</smXML:northBoundLatitude>
					</smXML:EX_GeographicBoundingBox>
				</smXML:geographicElement>
				<xsl:if test="../minalti and ../maxalti and ../altunits">
					<smXML:verticalElement>
						<smXML:EX_VerticalExtent>
							<smXML:minimumValue>
								<smXML:Real>
									<xsl:value-of select="../minalti"/>
								</smXML:Real>
							</smXML:minimumValue>
							<smXML:maximumValue>
								<smXML:Real>
									<xsl:value-of select="../maxalti"/>
								</smXML:Real>
							</smXML:maximumValue>
							<smXML:unitOfMeasure>
								<smXML:UomLength>
									<xsl:value-of select="../altunits"/>
								</smXML:UomLength>
							</smXML:unitOfMeasure>
							<smXML:verticalDatum>
								<smXML:RS_Identifier>
									<smXML:codeSpace>
										<smXML:CharacterString>UNKNOWN</smXML:CharacterString>
									</smXML:codeSpace>
									<smXML:version>
										<smXML:CharacterString>UNKNOWN</smXML:CharacterString>
									</smXML:version>
								</smXML:RS_Identifier>
							</smXML:verticalDatum>
						</smXML:EX_VerticalExtent>
					</smXML:verticalElement>
				</xsl:if>
			</smXML:EX_Extent>
		</smXML:extent>
	</xsl:template>
</xsl:stylesheet>
