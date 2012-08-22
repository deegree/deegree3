<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco">
	<xsl:template name="REFERENCESYSTEMINFO">
		<xsl:if test="spref/horizsys/cordsysn/projcsn">
			<gmd:referenceSystemInfo>
				<gmd:MD_ReferenceSystem>
					<gmd:referenceSystemIdentifier>
						<gmd:RS_Identifier>
							<gmd:code>
								<gco:CharacterString>
									<xsl:value-of select="spref/horizsys/cordsysn/projcsn"/>
								</gco:CharacterString>
							</gmd:code>
						</gmd:RS_Identifier>
					</gmd:referenceSystemIdentifier>
				</gmd:MD_ReferenceSystem>
			</gmd:referenceSystemInfo>
		</xsl:if>
		<xsl:if test="spref/horizsys/cordsysn/geogcsn">
			<gmd:referenceSystemInfo>
				<gmd:MD_ReferenceSystem>
					<gmd:referenceSystemIdentifier>
						<gmd:RS_Identifier>
							<gmd:code>
								<gco:CharacterString>
									<xsl:value-of select="spref/horizsys/cordsysn/geogcsn"/>
								</gco:CharacterString>
							</gmd:code>
						</gmd:RS_Identifier>
					</gmd:referenceSystemIdentifier>
				</gmd:MD_ReferenceSystem>
			</gmd:referenceSystemInfo>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
