<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco" 
 xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="METACONSTRAINT">
		<gmd:metadataConstraints>
			<gmd:MD_LegalConstraints>
				<gmd:otherConstraints>
					<gco:CharacterString>
						<xsl:value-of select="concat( 'accessConstraint: ', metainfo/metac, '; useConstraint: ', metainfo/metuc)"/>
					</gco:CharacterString>
				</gmd:otherConstraints>
			</gmd:MD_LegalConstraints>
			<xsl:if test="metainfo/metsi/metsc">
				<gmd:MD_SecurityConstraints>
					<gmd:classification>
						<gmd:MD_ClassificationCode codeList="MD_ClassificationCode">
							<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getSecurityCode( metainfo/metsi/metsc )"/></xsl:attribute>
						</gmd:MD_ClassificationCode>
					</gmd:classification>
					<xsl:if test="metainfo/metsi/metscs">
						<gmd:classificationSystem>
							<gco:CharacterString>
								<xsl:value-of select="metainfo/metsi/metscs"/>
							</gco:CharacterString>
						</gmd:classificationSystem>
					</xsl:if>
					<xsl:if test="metainfo/metsi/metshd">
						<gmd:handlingDescription>
							<gco:CharacterString>
								<xsl:value-of select="metainfo/metsi/metshd"/>
							</gco:CharacterString>
						</gmd:handlingDescription>
					</xsl:if>
				</gmd:MD_SecurityConstraints>
			</xsl:if>
		</gmd:metadataConstraints>
	</xsl:template>
</xsl:stylesheet>
