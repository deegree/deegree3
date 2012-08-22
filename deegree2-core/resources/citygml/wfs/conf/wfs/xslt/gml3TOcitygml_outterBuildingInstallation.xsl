<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" xmlns:wfs="http://www.opengis.net/wfs"
exclude-result-prefixes="wfs fo xsl ogc">

	<xsl:template name="OUTERBUILDINGINSTALLATION">
		<citygml:BuildingInstallation>
			<xsl:for-each select="citygml:Feature">
				<xsl:apply-templates select="."/>
				<xsl:for-each select="citygml:linkedGeometry/citygml:LINK_FEAT_GEOM">
					<xsl:if test="citygml:lod = 2">						
						<citygml:lod2Geometry>												
							<xsl:call-template name="buildingGEOM"></xsl:call-template>
						</citygml:lod2Geometry>
					</xsl:if>
					<xsl:if test="citygml:lod = 3">
						<citygml:lod3Geometry>						
							<xsl:call-template name="buildingGEOM"></xsl:call-template>
						</citygml:lod3Geometry>
					</xsl:if>
					<xsl:if test="citygml:lod = 4">
						<citygml:lod4Geometry>
							<xsl:call-template name="buildingGEOM"></xsl:call-template>
						</citygml:lod4Geometry>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
		</citygml:BuildingInstallation>
	</xsl:template>

</xsl:stylesheet>
