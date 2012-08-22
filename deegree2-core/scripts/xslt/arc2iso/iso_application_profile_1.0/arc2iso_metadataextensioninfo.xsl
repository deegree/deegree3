<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
xmlns:smXML="http://metadata.dgiwg.org/smXML">
	<xsl:template name="METAEXTENSIONINFO">
		<iso19115:metadataExtensionInfo>
			<smXML:MD_MetadataExtension>
				<smXML:extensionOnLineResource>
					<smXML:CI_OnlineResource>
						<smXML:linkage>
							<smXML:URL>
								<xsl:value-of select="metainfo/metextns/onlink"/>
							</smXML:URL>
						</smXML:linkage>
					</smXML:CI_OnlineResource>
				</smXML:extensionOnLineResource>
			</smXML:MD_MetadataExtension>
		</iso19115:metadataExtensionInfo>
	</xsl:template>
</xsl:stylesheet>
