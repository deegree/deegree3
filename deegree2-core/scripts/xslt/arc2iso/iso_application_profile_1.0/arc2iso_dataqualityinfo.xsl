<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco" 
 xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="DATAQUALITYINFO">
		<gmd:dataQualityInfo>
			<gmd:DQ_DataQuality>
				<xsl:if test="dataqual/attracc/qattracc/attraccv">
					<gmd:report>
						<gmd:DQ_QuantitativeAttribute>
							<gmd:DQ_QuantitativeResult>
								<xsl:call-template name="UOMLENGTH"/>
								<gmd:value>
									<gmd:Record>
										<xsl:value-of select="dataqual/attracc/qattracc/attraccv"/>
									</gmd:Record>
								</gmd:value>
							</gmd:DQ_QuantitativeResult>
						</gmd:DQ_QuantitativeAttribute>
					</gmd:report>
				</xsl:if>
				<xsl:if test="dataqual/posacc/horizpa/qhorizpa/horizpav | dataqual/posacc/vertacc/qvertpa/vertaccv">
					<gmd:report>
						<gmd:DQ_AbsoluteExternalPositionalAccuracy>
							<!-- ESRI (FGDC):Horizontal-->
							<xsl:if test="dataqual/posacc/horizpa/qhorizpa/horizpav">
								<gmd:DQ_QuantitativeResult>
									<xsl:call-template name="UOMLENGTH"/>
									<gmd:value>
										<gmd:Record>
											<xsl:value-of select="dataqual/posacc/horizpa/qhorizpa/horizpav"/>
										</gmd:Record>
									</gmd:value>
								</gmd:DQ_QuantitativeResult>
							</xsl:if>
							<!-- ESRI (FGDC):Vertikal-->
							<xsl:if test="dataqual/posacc/vertacc/qvertpa/vertaccv">
								<gmd:DQ_QuantitativeResult>
									<xsl:call-template name="UOMLENGTH"/>
									<gmd:value>
										<gmd:Record>
											<xsl:value-of select="dataqual/posacc/vertacc/qvertpa/vertaccv"/>
										</gmd:Record>
									</gmd:value>
								</gmd:DQ_QuantitativeResult>
							</xsl:if>
						</gmd:DQ_AbsoluteExternalPositionalAccuracy>
					</gmd:report>
				</xsl:if>
				<gmd:scope>
					<gmd:DQ_Scope>
						<gmd:level>
							<gmd:MD_ScopeCode codeList="MD_ScopeCode" codeListValue="dataset"/>
						</gmd:level>
					</gmd:DQ_Scope>
				</gmd:scope>
				<xsl:apply-templates select="dataqual/lineage"/>
				<xsl:if test="dataqual/complete">
					<!-- no futher description available for element <complete> at ESRI documentation -->
					<gmd:report>
						<gmd:DQ_CompletenessCommission>
							<gmd:nameOfMeasure>
								<gco:CharacterString>%</gco:CharacterString>
							</gmd:nameOfMeasure>
							<gmd:DQ_QuantitativeResult>
								<gmd:UomLength>
									<gmd:uomName>
										<gco:CharacterString>uom Name</gco:CharacterString>
									</gmd:uomName>
									<gmd:conversionTolSOstandardUnit>
										<gmd:Real>1</gmd:Real>
									</gmd:conversionTolSOstandardUnit>
								</gmd:UomLength>
								<gmd:value>
									<gmd:Record>100</gmd:Record>
								</gmd:value>
							</gmd:DQ_QuantitativeResult>
						</gmd:DQ_CompletenessCommission>
					</gmd:report>
				</xsl:if>
			</gmd:DQ_DataQuality>
		</gmd:dataQualityInfo>
	</xsl:template>
	
	<xsl:template match="lineage">
		<gmd:lineage>
			<gmd:LI_Lineage>				
				<xsl:if test="srcinfo">
					<gmd:source>
						<gmd:LI_Source>
							<!-- description contains all Information of the source Element wich cannot be mapped by ISO; seperated by | -->
							<gmd:description>
								<gco:CharacterString>
									<xsl:value-of select="concat(typesrc, ' | ', srccitea, ' | ', srccontr, ' | ', srctime)"/>
								</gco:CharacterString>
							</gmd:description>
							<xsl:if test="srcinfo/srcscale">
								<gmd:scaleDenominator>
									<gmd:MD_RepresentativeFraction>
										<gmd:denominator>
											<gmd:positiveInteger>
												<xsl:value-of select="srcinfo/srcscale"/>
											</gmd:positiveInteger>
										</gmd:denominator>
									</gmd:MD_RepresentativeFraction>
								</gmd:scaleDenominator>
							</xsl:if>
							<xsl:if test="boolean( srcinfo/srccite/citeinfo )">
								<gmd:sourceCitation>
									<xsl:apply-templates select="srcinfo/srccite/citeinfo"/>
								</gmd:sourceCitation>
							</xsl:if>
						</gmd:LI_Source>
					</gmd:source>
				</xsl:if>
				<xsl:apply-templates select="procstep"/>
			</gmd:LI_Lineage>
		</gmd:lineage>
	</xsl:template>
	
	<xsl:template match="procstep">
		<gmd:processStep>
			<gmd:LI_ProcessStep>
				<gmd:description>
					<gco:CharacterString>
						<xsl:value-of select="procdesc"/>
					</gco:CharacterString>
				</gmd:description>
				
				<!-- nach schema mandatory... -->
				<xsl:if test="procdate">
					<gmd:dateTime>
						<gmd:DateTime>
							<xsl:choose>
								<xsl:when test="proctime">
									<xsl:value-of select="concat(arc2iso:getISODate(procdate), 'T', arc2iso:getISOTime(proctime) )"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="concat(arc2iso:getISODate(procdate), 'T00:00:00' )"/>
								</xsl:otherwise>
							</xsl:choose>
						</gmd:DateTime>
					</gmd:dateTime>
				</xsl:if>
				<xsl:if test="proccont">
					<gmd:processor>
						<xsl:apply-templates select="proccont/cntinfo"/>
					</gmd:processor>
				</xsl:if>
			</gmd:LI_ProcessStep>
		</gmd:processStep>
	</xsl:template>
	
	<xsl:template name="UOMLENGTH">
		<!-- not known in ESRI (FGDC) -->
		<gmd:UomLength>
			<gmd:uomName>
				<gco:CharacterString>UNKNOWN</gco:CharacterString>
			</gmd:uomName>
			<gmd:conversionTolSOstandardUnit>
				<gmd:Real>1.0</gmd:Real>
			</gmd:conversionTolSOstandardUnit>
		</gmd:UomLength>
	</xsl:template>
</xsl:stylesheet>
