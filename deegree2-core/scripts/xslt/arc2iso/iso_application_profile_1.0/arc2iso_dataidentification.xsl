<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco"
  xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="DATAIDENTIFICATION">
		<gmd:identificationInfo>
			<gmd:MD_DataIdentification>
				<gmd:citation>
					<xsl:apply-templates select="idinfo/citation/citeinfo"/>
				</gmd:citation>
				<gmd:abstract>
					<gco:CharacterString>
						<xsl:value-of select="idinfo/descript/abstract"/>
					</gco:CharacterString>
				</gmd:abstract>
				<gmd:purpose>
					<gco:CharacterString>
						<xsl:value-of select="idinfo/descript/purpose"/>
					</gco:CharacterString>
				</gmd:purpose>
				<gmd:status>
					<gmd:MD_ProgressCode codeList="MD_ProgressCode">
						<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getProgressCode( idinfo/status/progress )"/></xsl:attribute>
					</gmd:MD_ProgressCode>
				</gmd:status>
				
				<gmd:resourceFormat>
                    <gmd:MD_Format>
                        <gmd:name>
                            <gco:CharacterString>
                                <xsl:value-of select="idinfo/natvform"/>
                            </gco:CharacterString>
                        </gmd:name>
                        <!-- ESRI does not provide version informations for resource/native format -->
                        <gmd:version>
                            <gco:CharacterString>1.0.0</gco:CharacterString>
                        </gmd:version>
                    </gmd:MD_Format>
                </gmd:resourceFormat>
				
				<xsl:if test="idinfo/ptcontac/cntinfo">
					<gmd:pointOfContact>
						<xsl:apply-templates select="idinfo/ptcontac/cntinfo"/>
					</gmd:pointOfContact>
				</xsl:if>
				
				<xsl:apply-templates select="idinfo/keywords/theme"/>
				<xsl:apply-templates select="idinfo/keywords/place"/>
				<xsl:apply-templates select="idinfo/keywords/stratum"/>
				<xsl:apply-templates select="idinfo/keywords/temporal"/>
				
				<xsl:if test="idinfo/browse">
					<gmd:graphicOverview>
						<MD_BrowseGraphic>
							<gmd:fileName>
								<gco:CharacterString>
									<xsl:value-of select="idinfo/browse/browsen"/>
								</gco:CharacterString>
							</gmd:fileName>
							<gmd:fileDescription>
								<gco:CharacterString>
									<xsl:value-of select="idinfo/browse/browsed"/>
								</gco:CharacterString>
							</gmd:fileDescription>
							<gmd:fileType>
								<gco:CharacterString>
									<xsl:value-of select="idinfo/browse/browset"/>
								</gco:CharacterString>
							</gmd:fileType>
						</MD_BrowseGraphic>
					</gmd:graphicOverview>
				</xsl:if>
			
				<xsl:call-template name="RESOURCECONSTR"/>
				
				<gmd:language>
					<gco:CharacterString>
						<xsl:value-of select="idinfo/descript/langdata"/>
					</gco:CharacterString>
				</gmd:language>
				
				<gmd:topicCategory>
					<gmd:MD_TopicCategoryCode>
						<xsl:value-of select="arc2iso:getTopCatTypeCode( dataIdInfo/tpCat/TopicCatCd/@value )"></xsl:value-of>
					</gmd:MD_TopicCategoryCode>
				</gmd:topicCategory>
								
				<xsl:if test="idinfo/native">
					<gmd:environmentDescription>
						<gco:CharacterString>
							<xsl:value-of select="idinfo/native"/>
						</gco:CharacterString>
					</gmd:environmentDescription>
				</xsl:if>
				<xsl:apply-templates select="idinfo/spdom/bounding"/>
				
				<xsl:if test="idinfo/descript/supplinf">
					<gmd:supplementalInformation>
						<gco:CharacterString>
							<xsl:value-of select="idinfo/descript/supplinf"/>
						</gco:CharacterString>
					</gmd:supplementalInformation>
				</xsl:if>
				
			</gmd:MD_DataIdentification>
		</gmd:identificationInfo>
	</xsl:template>
	
	<xsl:template match="keywords/theme">
		<gmd:descriptiveKeywords>
			<gmd:MD_Keywords>
				<xsl:for-each select="themekey">
					<gmd:keyword>
						<gco:CharacterString>
							<xsl:value-of select="."/>
						</gco:CharacterString>
					</gmd:keyword>
				</xsl:for-each>
				<gmd:type>
					<gmd:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="theme"/>
				</gmd:type>
				<gmd:thesaurusName>
					<gmd:CI_Citation>
						<gmd:title>
							<gco:CharacterString>
								<xsl:value-of select="themekt"/>
							</gco:CharacterString>
						</gmd:title>
						<!-- esri unbekannt -->
						<gmd:date>
							<gmd:CI_Date>
								<gmd:date>
									<gco:Date>
										<xsl:value-of select="arc2iso:getISODate( ../../../metainfo/metd )"/>
									</gco:Date>
								</gmd:date>
								<gmd:dateType>
									<gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
								</gmd:dateType>
							</gmd:CI_Date>
						</gmd:date>
					</gmd:CI_Citation>
				</gmd:thesaurusName>
			</gmd:MD_Keywords>
		</gmd:descriptiveKeywords>
	</xsl:template>
	
	<xsl:template match="keywords/place">
		<gmd:descriptiveKeywords>
			<gmd:MD_Keywords>
				<xsl:for-each select="placekey">
					<gmd:keyword>
						<gco:CharacterString>
							<xsl:value-of select="."/>
						</gco:CharacterString>
					</gmd:keyword>
				</xsl:for-each>
				<gmd:type>
					<gmd:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="place"/>
				</gmd:type>
				<xsl:if test="themekt">
					<gmd:ThesaurusName>
						<gmd:CI_Citation>
							<gmd:title>
								<gco:CharacterString>
									<xsl:value-of select="themekt"/>
								</gco:CharacterString>
							</gmd:title>
							<!-- esri unbekannt -->
							<gmd:date>
								<gmd:CI_Date>
									<gmd:date>
										<gco:Date>
											<xsl:value-of select="arc2iso:getISODate( ../../../metainfo/metd )"/>
										</gco:Date>
									</gmd:date>
									<gmd:dateType>
										<gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
									</gmd:dateType>
								</gmd:CI_Date>
							</gmd:date>
						</gmd:CI_Citation>
					</gmd:ThesaurusName>
				</xsl:if>
			</gmd:MD_Keywords>
		</gmd:descriptiveKeywords>
	</xsl:template>
	<xsl:template match="keywords/stratum">
		<gmd:descriptiveKeywords>
			<gmd:MD_Keywords>
				<xsl:for-each select="stratumkey">
					<gmd:keyword>
						<gco:CharacterString>
							<xsl:value-of select="."/>
						</gco:CharacterString>
					</gmd:keyword>
				</xsl:for-each>
				<gmd:type>
					<gmd:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="stratum"/>
				</gmd:type>
				<xsl:if test="themekt">
					<gmd:ThesaurusName>
						<gmd:CI_Citation>
							<gmd:title>
								<gco:CharacterString>
									<xsl:value-of select="themekt"/>
								</gco:CharacterString>
							</gmd:title>
							<!-- esri unbekannt -->
							<gmd:date>
								<gmd:CI_Date>
									<gmd:date>
										<gco:Date>
											<xsl:value-of select="arc2iso:getISODate( ../../../metainfo/metd )"/>
										</gco:Date>
									</gmd:date>
									<gmd:dateType>
										<gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
									</gmd:dateType>
								</gmd:CI_Date>
							</gmd:date>
						</gmd:CI_Citation>
					</gmd:ThesaurusName>
				</xsl:if>
			</gmd:MD_Keywords>
		</gmd:descriptiveKeywords>
	</xsl:template>
	<xsl:template match="keywords/temporal">
		<gmd:descriptiveKeywords>
			<gmd:MD_Keywords>
				<xsl:for-each select="temporalkey">
					<gmd:keyword>
						<gco:CharacterString>
							<xsl:value-of select="."/>
						</gco:CharacterString>
					</gmd:keyword>
				</xsl:for-each>
				<gmd:type>
					<gmd:MD_KeywordTypeCode codeList="MD_KeywordTypeCode" codeListValue="temporal"/>
				</gmd:type>
				<xsl:if test="themekt">
					<gmd:ThesaurusName>
						<gmd:CI_Citation>
							<gmd:title>
								<gco:CharacterString>
									<xsl:value-of select="themekt"/>
								</gco:CharacterString>
							</gmd:title>
							<!-- esri unbekannt -->
							<gmd:date>
								<gmd:CI_Date>
									<gmd:date>
										<gco:Date>
											<xsl:value-of select="arc2iso:getISODate( ../../../metainfo/metd )"/>
										</gco:Date>
									</gmd:date>
									<gmd:dateType>
										<gmd:CI_DateTypeCode codeList="CI_DateTypeCode" codeListValue="creation"/>
									</gmd:dateType>
								</gmd:CI_Date>
							</gmd:date>
						</gmd:CI_Citation>
					</gmd:ThesaurusName>
				</xsl:if>
			</gmd:MD_Keywords>
		</gmd:descriptiveKeywords>
	</xsl:template>
	
<!--	if info/useconst mandatory-->
	<xsl:template name="RESOURCECONSTR">
		<gmd:resourceConstraints>
			<gmd:MD_LegalConstraints>
				<gmd:useLimitation>
					<gco:CharacterString>
						<xsl:value-of select="idinfo/useconst"/>
					</gco:CharacterString>
				</gmd:useLimitation>
				<gmd:otherConstraints>
					<gco:CharacterString>
						<xsl:value-of select="idinfo/accconst"/>
					</gco:CharacterString>
				</gmd:otherConstraints>
			</gmd:MD_LegalConstraints>
		</gmd:resourceConstraints>
		<xsl:if test="boolean( idinfo/secinfo )">
		<!-- 		
			not supported at the moment
			<gmd:resourceConstraints>
				<gmd:MD_SecurityConstraints>
					<gmd:classification>
						<gmd:MD_ClassificationCode codeList="MD_ClassificationCode">
							<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getSecurityCode( idinfo/secinfo/secclass )"/></xsl:attribute>
						</gmd:MD_ClassificationCode>
					</gmd:classification>
					<gmd:classificationSystem>
						<gco:CharacterString>
							<xsl:value-of select="idinfo/secinfo/secsys"/>
						</gco:CharacterString>
					</gmd:classificationSystem>
					<gmd:handlingDescription>
						<gco:CharacterString>
							<xsl:value-of select="idinfo/secinfo/sechandl"/>
						</gco:CharacterString>
					</gmd:handlingDescription>
				</gmd:MD_SecurityConstraints>
			</gmd:resourceConstraints>
		 -->			
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="idinfo/spdom/bounding">
		<gmd:extent>
			<gmd:EX_Extent>
				<gmd:geographicElement>
					<gmd:EX_GeographicBoundingBox>
						<gmd:westBoundLongitude>
							<gco:Decimal>
								<xsl:value-of select="arc2iso:formatCoord( westbc )"/>
							</gco:Decimal>
						</gmd:westBoundLongitude>
						<gmd:eastBoundLongitude>
							<gco:Decimal>
								<xsl:value-of select="arc2iso:formatCoord( eastbc )"/>
							</gco:Decimal>
						</gmd:eastBoundLongitude>
						<gmd:southBoundLatitude>
							<gco:Decimal>
								<xsl:value-of select="arc2iso:formatCoord( southbc )"/>
							</gco:Decimal>
						</gmd:southBoundLatitude>
						<gmd:northBoundLatitude>
							<gco:Decimal>
								<xsl:value-of select="arc2iso:formatCoord( northbc )"/>
							</gco:Decimal>
						</gmd:northBoundLatitude>
					</gmd:EX_GeographicBoundingBox>
				</gmd:geographicElement>
				<xsl:if test="../minalti and ../maxalti and ../altunits">
					<gmd:verticalElement>
						<gmd:EX_VerticalExtent>
							<gmd:minimumValue>
								<gco:Real>
									<xsl:value-of select="../minalti"/>
								</gco:Real>
							</gmd:minimumValue>
							<gmd:maximumValue>
								<gco:Real>
									<xsl:value-of select="../maxalti"/>
								</gco:Real>
							</gmd:maximumValue>
							<gmd:unitOfMeasure>
								<gmd:UomLength>
									<xsl:value-of select="../altunits"/>
								</gmd:UomLength>
							</gmd:unitOfMeasure>
							<gmd:verticalDatum>
								<gmd:RS_Identifier>
									<gmd:code>
										<gco:CharacterString xmlns:gco="http://www.isotc211.org/2005/gco">UNKNOWN</gco:CharacterString>
									</gmd:code>								
								</gmd:RS_Identifier>
							</gmd:verticalDatum>
						</gmd:EX_VerticalExtent>
					</gmd:verticalElement>
				</xsl:if>
			</gmd:EX_Extent>
		</gmd:extent>
	</xsl:template>
</xsl:stylesheet>
