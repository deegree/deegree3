<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  
xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco">
	
	<xsl:template match="cntinfo">
		<gmd:CI_ResponsibleParty>
					
				<xsl:if test="cntperp/cntper">
					<gmd:individualName>
						<gco:CharacterString>
							<xsl:value-of select="./cntperp/cntper"></xsl:value-of>
						</gco:CharacterString>
					</gmd:individualName>
				</xsl:if>
				 
				<xsl:if test="cntperp/cntorg">
					<gmd:organisationName>
						<gco:CharacterString>
							<xsl:value-of select="./cntperp/cntorg"></xsl:value-of>
						</gco:CharacterString>
					</gmd:organisationName>
				</xsl:if>
				
				<xsl:if test="cntpos">
					<gmd:positionName>
						<gco:CharacterString>
							<xsl:value-of select="./cntpos"></xsl:value-of>
						</gco:CharacterString>
					</gmd:positionName>
				</xsl:if>
				
				<gmd:contactInfo>
					<gmd:CI_Contact>
						<gmd:phone>
							<gmd:CI_Telephone>
									<gmd:voice>
										<gco:CharacterString>
											<xsl:value-of select="./cntvoice"></xsl:value-of>
										</gco:CharacterString>
									</gmd:voice>
								<xsl:if test="cntfax">
									<gmd:facsimile>
										<gco:CharacterString>
											<xsl:value-of select="./cntfax"></xsl:value-of>
										</gco:CharacterString>
									</gmd:facsimile>
								</xsl:if>
							</gmd:CI_Telephone>
						</gmd:phone>
				
						<gmd:address>
							<gmd:CI_Address>
								
								<xsl:if test="./cntaddr/address">
									<gmd:deliveryPoint>
										<gco:CharacterString>
											<xsl:value-of select="./cntaddr/address"></xsl:value-of>
										</gco:CharacterString>
									</gmd:deliveryPoint> 
								</xsl:if>
								
								<gmd:city>
									<gco:CharacterString>
										<xsl:value-of select="./cntaddr/city"></xsl:value-of>
									</gco:CharacterString>
								</gmd:city>
								<gmd:administrativeArea>
									<gco:CharacterString>
										<xsl:value-of select="./cntaddr/state"></xsl:value-of>
									</gco:CharacterString>
								</gmd:administrativeArea>
								<gmd:postalCode>
									<gco:CharacterString>
										<xsl:value-of select="./cntaddr/postal"></xsl:value-of>
									</gco:CharacterString>
								</gmd:postalCode>
								<xsl:if test="./cntaddr/country">
									<gmd:country>
										<gco:CharacterString>
											<xsl:value-of select="./cntaddr/country"></xsl:value-of>
										</gco:CharacterString>
									</gmd:country>
								</xsl:if>
								<xsl:if test="./cntemail">
									<gmd:electronicMailAddress>
										<gco:CharacterString>
											<xsl:value-of select="./cntemail"></xsl:value-of>
										</gco:CharacterString>
									</gmd:electronicMailAddress>
								</xsl:if>
							</gmd:CI_Address>
						</gmd:address>
					
					<xsl:if test="hours">
						<gmd:hoursOfService>
							<gco:CharacterString>
								<xsl:value-of select="./hours"></xsl:value-of>
							</gco:CharacterString>
						</gmd:hoursOfService>
					</xsl:if>
			
					<xsl:if test="cntinst">
						<gmd:contactInstructions>
							<gco:CharacterString>
								<xsl:value-of select="./cntinst"></xsl:value-of>
							</gco:CharacterString>
						</gmd:contactInstructions>
					</xsl:if>

					</gmd:CI_Contact>
				</gmd:contactInfo>
				
				<gmd:role>
					<gmd:CI_RoleCode codeList="MD_ScopeCode" codeListValue="pointOfContact"></gmd:CI_RoleCode>
				</gmd:role>					
				
			</gmd:CI_ResponsibleParty>
		</xsl:template>	
	
	</xsl:stylesheet>	