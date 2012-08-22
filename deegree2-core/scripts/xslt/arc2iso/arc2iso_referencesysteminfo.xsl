<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
xmlns:smXML="http://metadata.dgiwg.org/smXML">
	<xsl:template name="REFERENCESYSTEMINFO">
		<xsl:if test="spref/horizsys/cordsysn/projcsn">
			<iso19115:referenceSystemInfo>
				<smXML:MD_ReferenceSystem>
					<smXML:referenceSystemIdentifier>
						<smXML:RS_Identifier>
							<smXML:code>
								<smXML:CharacterString>
									<xsl:value-of select="spref/horizsys/cordsysn/projcsn"/>
								</smXML:CharacterString>
							</smXML:code>
						</smXML:RS_Identifier>
					</smXML:referenceSystemIdentifier>
				</smXML:MD_ReferenceSystem>
			</iso19115:referenceSystemInfo>
		</xsl:if>
		<xsl:if test="spref/horizsys/cordsysn/geogcsn">
			<iso19115:referenceSystemInfo>
				<smXML:MD_ReferenceSystem>
					<smXML:referenceSystemIdentifier>
						<smXML:RS_Identifier>
							<smXML:code>
								<smXML:CharacterString>
									<xsl:value-of select="spref/horizsys/cordsysn/geogcsn"/>
								</smXML:CharacterString>
							</smXML:code>
						</smXML:RS_Identifier>
					</smXML:referenceSystemIdentifier>
				</smXML:MD_ReferenceSystem>
			</iso19115:referenceSystemInfo>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
