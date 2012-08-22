<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco"
xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="DISTRIBUTIONINFO">
		<gmd:distributionInfo>
			<gmd:MD_Distribution>
				<xsl:if test="distinfo/distrib/cntinfo">
					<gmd:distributor>
						<gmd:MD_Distributor>
							<gmd:distributorContact>
								<xsl:apply-templates select="distinfo/distrib/cntinfo"/>
							</gmd:distributorContact>

							<xsl:if test="distinfo/stdorder/fees | distinfo/stdorder/ordering | distinfo/stdorder/turnarnd">
								<gmd:distributionOrderProcess>
									<gmd:MD_StandardOrderProcess>
										<xsl:if test="distinfo/stdorder/fees">
											<gmd:fees>
												<gco:CharacterString>
													<xsl:value-of select="distinfo/stdorder/fees"/>
												</gco:CharacterString>
											</gmd:fees>
										</xsl:if>
										<xsl:if test="distinfo/availabl">
											<gmd:plannedAvailableDateTime>
												<gco:DateTime>
													<xsl:value-of select="concat( arc2iso:getISODate( distinfo/availabl/timeinfo/sngdate ), 'T', arc2iso:getISOTime( distinfo/availabl/timeinfo/caldate ) )"/>
												</gco:DateTime>
											</gmd:plannedAvailableDateTime>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/ordering">
											<gmd:orderingInstructions>
												<gco:CharacterString>
													<xsl:value-of select="distinfo/stdorder/ordering"/>
												</gco:CharacterString>
											</gmd:orderingInstructions>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/turnarnd">
											<gmd:turnaround>
												<gco:CharacterString>
													<xsl:value-of select="distinfo/stdorder/turnarnd"/>
												</gco:CharacterString>
											</gmd:turnaround>
										</xsl:if>
									</gmd:MD_StandardOrderProcess>
								</gmd:distributionOrderProcess>
							</xsl:if>
						</gmd:MD_Distributor>
					</gmd:distributor>
				</xsl:if>
				<gmd:distributorFormat>
					<gmd:MD_Format>
						<!-- Mandatory -->
						<gmd:name>
							<gco:CharacterString>
								<xsl:value-of select="distinfo/stdorder/digform/digtinfo/formname"/>
							</gco:CharacterString>
						</gmd:name>
						<gmd:version>
							<gco:CharacterString>
								<xsl:choose>
									<xsl:when test="distinfo/stdorder/digform/digtinfo/formverd | distinfo/stdorder/digform/digtinfo/formvern">
										<xsl:value-of select="concat( distinfo/stdorder/digform/digtinfo/formverd, ', Version ', distinfo/stdorder/digform/digtinfo/formvern )"/>
									</xsl:when>
									<xsl:otherwise>UNKNOWN</xsl:otherwise>
								</xsl:choose>
							</gco:CharacterString>
						</gmd:version>
						<!--- Optional -->
						<xsl:if test="distinfo/stdorder/digform/digtinfo/formspec">
							<gmd:specification>
								<gco:CharacterString>
									<xsl:value-of select="distinfo/stdorder/digform/digtinfo/formspec"/>
								</gco:CharacterString>
							</gmd:specification>
						</xsl:if>
						<xsl:if test="distinfo/stdorder/digform/digtinfo/formspec">
							<gmd:fileDecompressionTechnique>
								<gco:CharacterString>
									<xsl:value-of select="distinfo/stdorder/digform/digtinfo/filedec"/>
								</gco:CharacterString>
							</gmd:fileDecompressionTechnique>
						</xsl:if>
					</gmd:MD_Format>
				</gmd:distributorFormat>
				<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recden | distinfo/stdorder/digform/digtopt/offoptn/recdenu">
					<gmd:transferOptions>
						<gmd:MD_DigitalTransferOptions>
							<xsl:if test="distinfo/stdorder/digform/digtopt/onlinopt/networkr">
								<gmd:onLine>
									<gmd:CI_OnlineResource>
										<gmd:linkage>
											<gmd:URL>
												<xsl:value-of select="distinfo/stdorder/digform/digtopt/onlinopt/networkr"/>
											</gmd:URL>
										</gmd:linkage>
									</gmd:CI_OnlineResource>
								</gmd:onLine>
							</xsl:if>
							<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recden | distinfo/stdorder/digform/digtopt/offoptn/recdenu">
								<gmd:offLine>
									<gmd:MD_Medium>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/offmedia and arc2iso:getMediumFormatCode( distinfo/stdorder/digform/digtopt/offoptn/offmedia ) != 'UNKNOWN'">
											<gmd:name>
												<gmd:MD_MediumNameCode codeList="MD_MediumNameCode">
													<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getMediumFormatCode( distinfo/stdorder/digform/digtopt/offoptn/offmedia )"/></xsl:attribute>
												</gmd:MD_MediumNameCode>
											</gmd:name>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recfmt and arc2iso:getMediumNameCode( distinfo/stdorder/digform/digtopt/offoptn/recfmt  ) != 'UNKNOWN'">
											<gmd:mediumFormat>
												<gmd:MD_MediumFormatCode codeList="MD_MediumFormatCode">
													<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getMediumNameCode( distinfo/stdorder/digform/digtopt/offoptn/recfmt  )"/></xsl:attribute>
												</gmd:MD_MediumFormatCode>
											</gmd:mediumFormat>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recden">
											<gmd:density>
												<gmd:Real>
													<xsl:value-of select="distinfo/stdorder/digform/digtopt/offoptn/recden"/>
												</gmd:Real>
											</gmd:density>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recdenu">
											<gmd:densityUnits>
												<gmd:Real>
													<xsl:value-of select="distinfo/stdorder/digform/digtopt/offoptn/recdenu"/>
												</gmd:Real>
											</gmd:densityUnits>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/compat">
											<gmd:mediumNote>
												<gco:CharacterString>
													<xsl:value-of select="concat( 'Compatibility Information: ', distinfo/stdorder/digform/digtopt/offoptn/compat )"/>
												</gco:CharacterString>
											</gmd:mediumNote>
										</xsl:if>
									</gmd:MD_Medium>
								</gmd:offLine>
							</xsl:if>
						</gmd:MD_DigitalTransferOptions>
					</gmd:transferOptions>
				</xsl:if>
			</gmd:MD_Distribution>
		</gmd:distributionInfo>
	</xsl:template>
</xsl:stylesheet>
