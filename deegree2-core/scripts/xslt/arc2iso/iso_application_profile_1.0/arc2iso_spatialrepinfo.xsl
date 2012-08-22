<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:gmd="http://www.isotc211.org/2005/gmd" 
 xmlns:gco="http://www.isotc211.org/2005/gco" 
 xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="SPATIALREPINFO">
		<gmd:spatialRepresentationInfo>
			<gmd:MD_VectorSpatialRepresentation>
				<gmd:topologyLevel>
					<gmd:MD_TopologyLevelCode codeList="MD_TopologyLevelCode" codeListValue="geometryOnly"/>
				</gmd:topologyLevel>
				<gmd:geometricObjects>
					<gmd:MD_GeometricObjects>
						<gmd:geometricObjectType>
							<gmd:MD_GeometricObjectTypeCode codeList="MD_GeometricObjectTypeCode">
								<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getGeometricObjectTypeCode(spdoinfo/ptvctinf/esriterm/efeageom)"/></xsl:attribute>
							</gmd:MD_GeometricObjectTypeCode>
						</gmd:geometricObjectType>
					</gmd:MD_GeometricObjects>
				</gmd:geometricObjects>
			</gmd:MD_VectorSpatialRepresentation>
			<!-- TODO:
		<gmd:MD_GridSpatialRepresentation>
		</gmd:MD_GridSpatialRepresentation>
		-->
		</gmd:spatialRepresentationInfo>
	</xsl:template>
</xsl:stylesheet>
