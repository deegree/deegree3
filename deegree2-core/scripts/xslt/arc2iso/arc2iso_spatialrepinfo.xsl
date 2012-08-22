<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:iso19115="http://schemas.opengis.net/iso19115full" 
 xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:java="java" xmlns:arc2iso="org.deegree.ogcwebservices.csw.iso_profile.Arc2ISO">
	<xsl:template name="SPATIALREPINFO">
		<iso19115:spatialRepresentationInfo>
			<smXML:MD_VectorSpatialRepresentation>
				<smXML:topologyLevel>
					<smXML:MD_TopologyLevelCode codeList="MD_TopologyLevelCode" codeListValue="geometryOnly"/>
				</smXML:topologyLevel>
				<smXML:geometricObjects>
					<smXML:MD_GeometricObjects>
						<smXML:geometricObjectType>
							<smXML:MD_GeometricObjectTypeCode codeList="MD_GeometricObjectTypeCode">
								<xsl:attribute name="codeListValue"><xsl:value-of select="arc2iso:getGeometricObjectTypeCode(spdoinfo/ptvctinf/esriterm/efeageom)"/></xsl:attribute>
							</smXML:MD_GeometricObjectTypeCode>
						</smXML:geometricObjectType>
					</smXML:MD_GeometricObjects>
				</smXML:geometricObjects>
			</smXML:MD_VectorSpatialRepresentation>
			<!-- TODO:
		<smXML:MD_GridSpatialRepresentation>
		</smXML:MD_GridSpatialRepresentation>
		-->
		</iso19115:spatialRepresentationInfo>
	</xsl:template>
</xsl:stylesheet>
