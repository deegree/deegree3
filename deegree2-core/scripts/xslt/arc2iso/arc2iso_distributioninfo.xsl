<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:iso19115="http://schemas.opengis.net/iso19115full" xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="DISTRIBUTIONINFO">
		<iso19115:distributionInfo>
			<smXML:MD_Distribution>
				<xsl:if test="distinfo/distrib/cntinfo">
					<smXML:distributor>
						<smXML:MD_Distributor>
						
							<smXML:distributorContact>
								<xsl:apply-templates select="distinfo/distrib/cntinfo"/>
							</smXML:distributorContact>

							<xsl:if test="distinfo/stdorder/fees | distinfo/stdorder/ordering | distinfo/stdorder/turnarnd">
								<smXML:distributionOrderProcess>
									<smXML:MD_StandardOrderProcess>
										<xsl:if test="distinfo/stdorder/fees">
											<smXML:fees>
												<smXML:CharacterString>
													<xsl:value-of select="distinfo/stdorder/fees"/>
												</smXML:CharacterString>
											</smXML:fees>
										</xsl:if>
										<xsl:if test="distinfo/availabl">
											<smXML:plannedAvailableDateTime>
												<smXML:DateTime>
													<xsl:value-of select="concat( arc2iso:getISODate( distinfo/availabl/timeinfo/sngdate ), 'T', arc2iso:getISOTime( distinfo/availabl/timeinfo/caldate ) )"/>
												</smXML:DateTime>
											</smXML:plannedAvailableDateTime>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/ordering">
											<smXML:orderingInstructions>
												<smXML:CharacterString>
													<xsl:value-of select="distinfo/stdorder/ordering"/>
												</smXML:CharacterString>
											</smXML:orderingInstructions>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/turnarnd">
											<smXML:turnaround>
												<smXML:CharacterString>
													<xsl:value-of select="distinfo/stdorder/turnarnd"/>
												</smXML:CharacterString>
											</smXML:turnaround>
										</xsl:if>
									</smXML:MD_StandardOrderProcess>
								</smXML:distributionOrderProcess>
							</xsl:if>
						</smXML:MD_Distributor>
					</smXML:distributor>
				</xsl:if>
				<smXML:distributorFormat>
					<smXML:MD_Format>
						<!-- Mandatory -->
						<smXML:name>
							<smXML:CharacterString>
								<xsl:value-of select="distinfo/stdorder/digform/digtinfo/formname"/>
							</smXML:CharacterString>
						</smXML:name>
						<smXML:version>
							<smXML:CharacterString>
								<xsl:choose>
									<xsl:when test="distinfo/stdorder/digform/digtinfo/formverd | distinfo/stdorder/digform/digtinfo/formvern">
										<xsl:value-of select="concat( distinfo/stdorder/digform/digtinfo/formverd, ', Version ', distinfo/stdorder/digform/digtinfo/formvern )"/>
									</xsl:when>
									<xsl:otherwise>UNKNOWN</xsl:otherwise>
								</xsl:choose>
							</smXML:CharacterString>
						</smXML:version>
						<!--- Optional -->
						<xsl:if test="distinfo/stdorder/digform/digtinfo/formspec">
							<smXML:specification>
								<smXML:CharacterString>
									<xsl:value-of select="distinfo/stdorder/digform/digtinfo/formspec"/>
								</smXML:CharacterString>
							</smXML:specification>
						</xsl:if>
						<xsl:if test="distinfo/stdorder/digform/digtinfo/formspec">
							<smXML:fileDecompressionTechnique>
								<smXML:CharacterString>
									<xsl:value-of select="distinfo/stdorder/digform/digtinfo/filedec"/>
								</smXML:CharacterString>
							</smXML:fileDecompressionTechnique>
						</xsl:if>
					</smXML:MD_Format>
				</smXML:distributorFormat>
				<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recden | distinfo/stdorder/digform/digtopt/offoptn/recdenu">
					<smXML:transferOptions>
						<smXML:MD_DigitalTransferOptions>
							<xsl:if test="distinfo/stdorder/digform/digtopt/onlinopt/networkr">
								<smXML:onLine>
									<smXML:CI_OnlineResource>
										<smXML:linkage>
											<smXML:URL>
												<xsl:value-of select="distinfo/stdorder/digform/digtopt/onlinopt/networkr"/>
											</smXML:URL>
										</smXML:linkage>
									</smXML:CI_OnlineResource>
								</smXML:onLine>
							</xsl:if>
							<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recden | distinfo/stdorder/digform/digtopt/offoptn/recdenu">
								<smXML:offLine>
									<smXML:MD_Medium>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/offmedia and arc2iso:getMediumFormatCode( distinfo/stdorder/digform/digtopt/offoptn/offmedia ) != 'UNKNOWN'">
											<smXML:name>
												<smXML:MD_MediumNameCode codeList="MD_MediumNameCode">
													<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getMediumFormatCode( distinfo/stdorder/digform/digtopt/offoptn/offmedia )"/></xsl:attribute>
												</smXML:MD_MediumNameCode>
											</smXML:name>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recfmt and arc2iso:getMediumNameCode( distinfo/stdorder/digform/digtopt/offoptn/recfmt  ) != 'UNKNOWN'">
											<smXML:mediumFormat>
												<smXML:MD_MediumFormatCode codeList="MD_MediumFormatCode">
													<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getMediumNameCode( distinfo/stdorder/digform/digtopt/offoptn/recfmt  )"/></xsl:attribute>
												</smXML:MD_MediumFormatCode>
											</smXML:mediumFormat>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recden">
											<smXML:density>
												<smXML:Real>
													<xsl:value-of select="distinfo/stdorder/digform/digtopt/offoptn/recden"/>
												</smXML:Real>
											</smXML:density>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/recdenu">
											<smXML:densityUnits>
												<smXML:Real>
													<xsl:value-of select="distinfo/stdorder/digform/digtopt/offoptn/recdenu"/>
												</smXML:Real>
											</smXML:densityUnits>
										</xsl:if>
										<xsl:if test="distinfo/stdorder/digform/digtopt/offoptn/compat">
											<smXML:mediumNote>
												<smXML:CharacterString>
													<xsl:value-of select="concat( 'Compatibility Information: ', distinfo/stdorder/digform/digtopt/offoptn/compat )"/>
												</smXML:CharacterString>
											</smXML:mediumNote>
										</xsl:if>
									</smXML:MD_Medium>
								</smXML:offLine>
							</xsl:if>
						</smXML:MD_DigitalTransferOptions>
					</smXML:transferOptions>
				</xsl:if>
			</smXML:MD_Distribution>
		</iso19115:distributionInfo>
	</xsl:template>
</xsl:stylesheet>
