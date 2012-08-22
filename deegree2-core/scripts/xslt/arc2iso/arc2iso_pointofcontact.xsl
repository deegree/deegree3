<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
xmlns:smXML="http://metadata.dgiwg.org/smXML">
	
	<xsl:template match="cntinfo">
		<smXML:CI_ResponsibleParty>
					
				<xsl:if test="cntperp/cntper">
					<smXML:individualName>
						<smXML:CharacterString>
							<xsl:value-of select="./cntperp/cntper"></xsl:value-of>
						</smXML:CharacterString>
					</smXML:individualName>
				</xsl:if>
				 
				<xsl:if test="cntperp/cntorg">
					<smXML:organisationName>
						<smXML:CharacterString>
							<xsl:value-of select="./cntperp/cntorg"></xsl:value-of>
						</smXML:CharacterString>
					</smXML:organisationName>
				</xsl:if>
				
				<xsl:if test="cntpos">
					<smXML:positionName>
						<smXML:CharacterString>
							<xsl:value-of select="./cntpos"></xsl:value-of>
						</smXML:CharacterString>
					</smXML:positionName>
				</xsl:if>
				
				<smXML:contactInfo>
					<smXML:CI_Contact>
				
						<smXML:phone>
							<smXML:CI_Telephone>
									<smXML:voice>
										<smXML:CharacterString>
											<xsl:value-of select="./cntvoice"></xsl:value-of>
										</smXML:CharacterString>
									</smXML:voice>
								<xsl:if test="cntfax">
									<smXML:facsimile>
										<smXML:CharacterString>
											<xsl:value-of select="./cntfax"></xsl:value-of>
										</smXML:CharacterString>
									</smXML:facsimile>
								</xsl:if>
							</smXML:CI_Telephone>
						</smXML:phone>
				
						<smXML:address>
							<smXML:CI_Address>
								
								<xsl:if test="./cntaddr/address">
									<smXML:deliveryPoint>
										<smXML:CharacterString>
											<xsl:value-of select="./cntaddr/address"></xsl:value-of>
										</smXML:CharacterString>
									</smXML:deliveryPoint> 
								</xsl:if>
								
								<smXML:city>
									<smXML:CharacterString>
										<xsl:value-of select="./cntaddr/city"></xsl:value-of>
									</smXML:CharacterString>
								</smXML:city>
								<smXML:administrativeArea>
									<smXML:CharacterString>
										<xsl:value-of select="./cntaddr/state"></xsl:value-of>
									</smXML:CharacterString>
								</smXML:administrativeArea>
								<smXML:postalCode>
									<smXML:CharacterString>
										<xsl:value-of select="./cntaddr/postal"></xsl:value-of>
									</smXML:CharacterString>
								</smXML:postalCode>
								<xsl:if test="./cntaddr/country">
									<smXML:country>
										<smXML:CharacterString>
											<xsl:value-of select="./cntaddr/country"></xsl:value-of>
										</smXML:CharacterString>
									</smXML:country>
								</xsl:if>
								<xsl:if test="./cntemail">
									<smXML:electronicMailAddress>
										<smXML:CharacterString>
											<xsl:value-of select="./cntemail"></xsl:value-of>
										</smXML:CharacterString>
									</smXML:electronicMailAddress>
								</xsl:if>
							</smXML:CI_Address>
						</smXML:address>
					
					<xsl:if test="hours">
						<smXML:hoursOfService>
							<smXML:CharacterString>
								<xsl:value-of select="./hours"></xsl:value-of>
							</smXML:CharacterString>
						</smXML:hoursOfService>
					</xsl:if>
			
					<xsl:if test="cntinst">
						<smXML:contactInstructions>
							<smXML:CharacterString>
								<xsl:value-of select="./cntinst"></xsl:value-of>
							</smXML:CharacterString>
						</smXML:contactInstructions>
					</xsl:if>

					</smXML:CI_Contact>
				</smXML:contactInfo>
				
				<smXML:role>
					<smXML:CI_RoleCode codeList="MD_ScopeCode" codeListValue="pointOfContact"></smXML:CI_RoleCode>
				</smXML:role>					
				
			</smXML:CI_ResponsibleParty>
		</xsl:template>	
	
	</xsl:stylesheet>	