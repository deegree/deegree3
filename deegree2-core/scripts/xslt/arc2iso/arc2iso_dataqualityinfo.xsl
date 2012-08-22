<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="DATAQUALITYINFO">
		<iso19115:dataQualityInfo>
			<smXML:DQ_DataQuality>
				<xsl:if test="dataqual/attracc/qattracc/attraccv">
					<smXML:report>
						<smXML:DQ_QuantitativeAttribute>
							<smXML:DQ_QuantitativeResult>
								<xsl:call-template name="UOMLENGTH"/>
								<smXML:value>
									<smXML:Record>
										<xsl:value-of select="dataqual/attracc/qattracc/attraccv"/>
									</smXML:Record>
								</smXML:value>
							</smXML:DQ_QuantitativeResult>
						</smXML:DQ_QuantitativeAttribute>
					</smXML:report>
				</xsl:if>
				<xsl:if test="dataqual/posacc/horizpa/qhorizpa/horizpav | dataqual/posacc/vertacc/qvertpa/vertaccv">
					<smXML:report>
						<smXML:DQ_AbsoluteExternalPositionalAccuracy>
							<!-- ESRI (FGDC):Horizontal-->
							<xsl:if test="dataqual/posacc/horizpa/qhorizpa/horizpav">
								<smXML:DQ_QuantitativeResult>
									<xsl:call-template name="UOMLENGTH"/>
									<smXML:value>
										<smXML:Record>
											<xsl:value-of select="dataqual/posacc/horizpa/qhorizpa/horizpav"/>
										</smXML:Record>
									</smXML:value>
								</smXML:DQ_QuantitativeResult>
							</xsl:if>
							<!-- ESRI (FGDC):Vertikal-->
							<xsl:if test="dataqual/posacc/vertacc/qvertpa/vertaccv">
								<smXML:DQ_QuantitativeResult>
									<xsl:call-template name="UOMLENGTH"/>
									<smXML:value>
										<smXML:Record>
											<xsl:value-of select="dataqual/posacc/vertacc/qvertpa/vertaccv"/>
										</smXML:Record>
									</smXML:value>
								</smXML:DQ_QuantitativeResult>
							</xsl:if>
						</smXML:DQ_AbsoluteExternalPositionalAccuracy>
					</smXML:report>
				</xsl:if>
				<smXML:scope>
					<smXML:DQ_Scope>
						<smXML:level>
							<smXML:MD_ScopeCode codeList="MD_ScopeCode" codeListValue="dataset"/>
						</smXML:level>
					</smXML:DQ_Scope>
				</smXML:scope>
				<xsl:apply-templates select="dataqual/lineage"/>
				<xsl:if test="dataqual/complete">
					<!-- no futher description available for element <complete> at ESRI documentation -->
					<smXML:report>
						<smXML:DQ_CompletenessCommission>
							<smXML:nameOfMeasure>
								<smXML:CharacterString>%</smXML:CharacterString>
							</smXML:nameOfMeasure>
							<smXML:DQ_QuantitativeResult>
								<smXML:UomLength>
									<smXML:uomName>
										<smXML:CharacterString>uom Name</smXML:CharacterString>
									</smXML:uomName>
									<smXML:conversionTolSOstandardUnit>
										<smXML:Real>1</smXML:Real>
									</smXML:conversionTolSOstandardUnit>
								</smXML:UomLength>
								<smXML:value>
									<smXML:Record>100</smXML:Record>
								</smXML:value>
							</smXML:DQ_QuantitativeResult>
						</smXML:DQ_CompletenessCommission>
					</smXML:report>
				</xsl:if>
			</smXML:DQ_DataQuality>
		</iso19115:dataQualityInfo>
	</xsl:template>
	
	<xsl:template match="lineage">
		<smXML:lineage>
			<smXML:LI_Lineage>				
				<xsl:if test="srcinfo">
					<smXML:source>
						<smXML:LI_Source>
							<!-- description contains all Information of the source Element wich cannot be mapped by ISO; seperated by | -->
							<smXML:description>
								<smXML:CharacterString>
									<xsl:value-of select="concat(typesrc, ' | ', srccitea, ' | ', srccontr, ' | ', srctime)"/>
								</smXML:CharacterString>
							</smXML:description>
							<xsl:if test="srcinfo/srcscale">
								<smXML:scaleDenominator>
									<smXML:MD_RepresentativeFraction>
										<smXML:denominator>
											<smXML:positiveInteger>
												<xsl:value-of select="srcinfo/srcscale"/>
											</smXML:positiveInteger>
										</smXML:denominator>
									</smXML:MD_RepresentativeFraction>
								</smXML:scaleDenominator>
							</xsl:if>
							<xsl:if test="boolean( srcinfo/srccite/citeinfo )">
								<smXML:sourceCitation>
									<xsl:apply-templates select="srcinfo/srccite/citeinfo"/>
								</smXML:sourceCitation>
							</xsl:if>
						</smXML:LI_Source>
						
					</smXML:source>
				</xsl:if>
				<xsl:apply-templates select="procstep"/>
			</smXML:LI_Lineage>
		</smXML:lineage>
	</xsl:template>
	
	<xsl:template match="procstep">
		<smXML:processStep>
			<smXML:LI_ProcessStep>
				<smXML:description>
					<smXML:CharacterString>
						<xsl:value-of select="procdesc"/>
					</smXML:CharacterString>
				</smXML:description>
				
				<!-- nach schema mandatory... -->
				<xsl:if test="procdate">
					<smXML:dateTime>
						<smXML:DateTime>
							<xsl:choose>
								<xsl:when test="proctime">
									<xsl:value-of select="concat(arc2iso:getISODate(procdate), 'T', arc2iso:getISOTime(proctime) )"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(arc2iso:getISODate(procdate), 'T00:00:00' )"/>
								</xsl:otherwise>
							</xsl:choose>
						</smXML:DateTime>
					</smXML:dateTime>
				</xsl:if>
				<xsl:if test="proccont">
					<smXML:processor>
						<xsl:apply-templates select="proccont/cntinfo"/>
					</smXML:processor>
				</xsl:if>
			</smXML:LI_ProcessStep>
		</smXML:processStep>
	</xsl:template>
	
	<xsl:template name="UOMLENGTH">
		<!-- not known in ESRI (FGDC) -->
		<smXML:UomLength>
			<smXML:uomName>
				<smXML:CharacterString>UNKNOWN</smXML:CharacterString>
			</smXML:uomName>
			<smXML:conversionTolSOstandardUnit>
				<smXML:Real>1.0</smXML:Real>
			</smXML:conversionTolSOstandardUnit>
		</smXML:UomLength>
	</xsl:template>
</xsl:stylesheet>
