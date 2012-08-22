<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" xmlns:wfs="http://www.opengis.net/wfs"
 xmlns:xAL="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0"
 exclude-result-prefixes="wfs fo xsl ogc" >

	<xsl:template name="CITYFURNITURE">
		<citygml:CityFurniture>
			<xsl:apply-templates select="citygml:Feature"/>
			<xsl:apply-templates select="citygml:Feature/citygml:cityFurniture/citygml:CityFurniture"/>
		</citygml:CityFurniture>
	</xsl:template>

	<xsl:template match="citygml:CityFurniture" name="CityFurnitureTemplate">
		
		<xsl:for-each select="../../citygml:function/citygml:Function">
			<citygml:function>
				<xsl:value-of select="citygml:function"></xsl:value-of>
			</citygml:function>
		</xsl:for-each>
		
		<xsl:for-each select="../../citygml:usage/citygml:Usage">
			<citygml:usage>
				<xsl:value-of select="citygml:usage"></xsl:value-of>
			</citygml:usage>
		</xsl:for-each>
		
		<xsl:for-each select="../../citygml:linkedGeometry/citygml:LINK_FEAT_GEOM">
			<xsl:if test="citygml:lod = 1">
				<citygml:lod1Geometry>
					<xsl:call-template name="cfGEOM"></xsl:call-template>
				</citygml:lod1Geometry>
			</xsl:if>
			<xsl:if test="citygml:lod = 2">
				<citygml:lod2Geometry>
					<xsl:call-template name="cfGEOM"></xsl:call-template>
				</citygml:lod2Geometry>
			</xsl:if>
			<xsl:if test="citygml:lod = 3">
				<citygml:lod3Geometry>
					<xsl:call-template name="cfGEOM"></xsl:call-template>
				</citygml:lod3Geometry>
			</xsl:if>
			<xsl:if test="citygml:lod = 4">
				<citygml:lod4Geometry>
					<xsl:call-template name="cfGEOM"></xsl:call-template>
				</citygml:lod4Geometry>
			</xsl:if>	
		</xsl:for-each>

		<xsl:for-each select="../../citygml:address">
			<citygml:address>
				<xsl:apply-templates select="citygml:Address"></xsl:apply-templates>
			</citygml:address>
		</xsl:for-each>
				
	</xsl:template>
	
	<xsl:template name="cfGEOM">
		<!--
			type = 0 -> Solid
			type = 1 -> MultiSurface
			type = 2 -> MultiCurve
			type = 3 -> MultiPoint
			type = 4 -> CompositeSolid
			type = 5 -> CompositeSurface
		-->
		<xsl:if test="citygml:type = 0 ">
			<gml:Solid>
				<gml:exterior>
					<gml:CompositeSurface>
						<xsl:for-each select="citygml:solid/citygml:Solid/citygml:exteriorSurfaces/citygml:Polygon">
							<gml:surfaceMember>
								<xsl:apply-templates select="."></xsl:apply-templates>
							</gml:surfaceMember>
						</xsl:for-each>
					</gml:CompositeSurface>
				</gml:exterior>
				<xsl:for-each select="citygml:solid/citygml:Solid/citygml:interiorSurfaces">
					<gml:interior>
						<gml:CompositeSurface>
							<xsl:for-each select="citygml:InteriorSurface/citygml:interiorSurfaces/citygml:Polygon">
								<gml:surfaceMember>
									<xsl:apply-templates select="."></xsl:apply-templates>
								</gml:surfaceMember>
							</xsl:for-each>
						</gml:CompositeSurface>
					</gml:interior>
				</xsl:for-each>
			</gml:Solid>
		</xsl:if>
		
		<xsl:if test="citygml:type = 1">
			<gml:MultiSurface>
				<xsl:for-each select="citygml:polygon/citygml:Polygon">
					<gml:surfaceMember>
						<xsl:apply-templates select="."></xsl:apply-templates>
					</gml:surfaceMember>
				</xsl:for-each>
				<xsl:for-each select="citygml:composite/citygml:LINK_FEAT_GEOM">
					<gml:surfaceMember>
						<xsl:call-template name="cfGEOM"></xsl:call-template>
					</gml:surfaceMember>
				</xsl:for-each>
			</gml:MultiSurface>
		</xsl:if>
		
		<xsl:if test="citygml:type = 2">			
			<gml:MultiCurve>					
				<xsl:for-each select="citygml:curve/citygml:Curve">
					<gml:curveMember>
						<xsl:apply-templates select="."></xsl:apply-templates>
					</gml:curveMember>
				</xsl:for-each>	
			</gml:MultiCurve>
		</xsl:if>
		
		<xsl:if test="citygml:type = 4 ">
			<gml:CompositeSolid>
				<xsl:for-each select="citygml:composite/citygml:LINK_FEAT_GEOM/citygml:solid/citygml:Solid">
					<gml:solidMember>
						<gml:Solid>
							<gml:exterior>
								<gml:CompositeSurface>
									<xsl:for-each select="citygml:exteriorSurfaces/citygml:Polygon">
										<gml:surfaceMember>
											<xsl:apply-templates select="."></xsl:apply-templates>
										</gml:surfaceMember>
									</xsl:for-each>
								</gml:CompositeSurface>
							</gml:exterior>
							<xsl:for-each select="citygml:interiorSurfaces">
								<gml:interior>
									<gml:CompositeSurface>
										<xsl:for-each select="citygml:InteriorSurface/citygml:interiorSurfaces/citygml:Polygon">
											<gml:surfaceMember>
												<xsl:apply-templates select="."></xsl:apply-templates>
											</gml:surfaceMember>
										</xsl:for-each>
									</gml:CompositeSurface>
								</gml:interior>
							</xsl:for-each>
						</gml:Solid>
					</gml:solidMember>
				</xsl:for-each>
			</gml:CompositeSolid>
		</xsl:if>
		
		<xsl:if test="citygml:type = 5 ">
			<gml:CompositeSurface>
				<xsl:for-each select="citygml:polygon/citygml:Polygon">
					<gml:surfaceMember>
						<xsl:apply-templates select="."></xsl:apply-templates>
					</gml:surfaceMember>
				</xsl:for-each>
				<xsl:for-each select="citygml:composite/citygml:LINK_FEAT_GEOM">
					<gml:surfaceMember>
						<xsl:call-template name="cfGEOM"></xsl:call-template>
					</gml:surfaceMember>
				</xsl:for-each>
			</gml:CompositeSurface>
		</xsl:if>
		
	</xsl:template>
		
</xsl:stylesheet>
