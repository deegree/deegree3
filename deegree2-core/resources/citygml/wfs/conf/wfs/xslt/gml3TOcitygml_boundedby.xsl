<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" xmlns:wfs="http://www.opengis.net/wfs"
exclude-result-prefixes="wfs fo xsl ogc">
	<xsl:template name="BOUNDEDBY">
		<xsl:for-each select="citygml:Feature">
			<xsl:apply-templates select="."/>
			<xsl:for-each select="citygml:linkedGeometry/citygml:LINK_FEAT_GEOM">
				<xsl:if test="citygml:lod = 2">
					<citygml:lod2MultiSurface>
						<xsl:call-template name="buildingGEOM"/>
					</citygml:lod2MultiSurface>
				</xsl:if>
				<xsl:if test="citygml:lod = 3">
					<citygml:lod3MultiSurface>
						<xsl:call-template name="buildingGEOM"/>
					</citygml:lod3MultiSurface>
				</xsl:if>
				<xsl:if test="citygml:lod = 4">
					<citygml:lod4MultiSurface>
						<xsl:call-template name="buildingGEOM"/>
					</citygml:lod4MultiSurface>
				</xsl:if>
			</xsl:for-each>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>
