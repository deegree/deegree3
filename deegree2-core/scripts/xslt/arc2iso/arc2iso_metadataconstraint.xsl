<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="METACONSTRAINT">
		<iso19115:metadataConstraints>
			<smXML:MD_LegalConstraints>
				<smXML:otherConstraints>
					<smXML:CharacterString>
						<xsl:value-of select="concat( 'accessConstraint: ', metainfo/metac, '; useConstraint: ', metainfo/metuc)"/>
					</smXML:CharacterString>
				</smXML:otherConstraints>
			</smXML:MD_LegalConstraints>
			<xsl:if test="metainfo/metsi/metsc">
				<smXML:MD_SecurityConstraints>
					<smXML:classification>
						<smXML:MD_ClassificationCode codeList="MD_ClassificationCode">
							<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getSecurityCode( metainfo/metsi/metsc )"/></xsl:attribute>
						</smXML:MD_ClassificationCode>
					</smXML:classification>
					<xsl:if test="metainfo/metsi/metscs">
						<smXML:classificationSystem>
							<smXML:CharacterString>
								<xsl:value-of select="metainfo/metsi/metscs"/>
							</smXML:CharacterString>
						</smXML:classificationSystem>
					</xsl:if>
					<xsl:if test="metainfo/metsi/metshd">
						<smXML:handlingDescription>
							<smXML:CharacterString>
								<xsl:value-of select="metainfo/metsi/metshd"/>
							</smXML:CharacterString>
						</smXML:handlingDescription>
					</xsl:if>
				</smXML:MD_SecurityConstraints>
			</xsl:if>
		</iso19115:metadataConstraints>
	</xsl:template>
</xsl:stylesheet>
