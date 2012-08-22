<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" xmlns:wfs="http://www.opengis.net/wfs" 
xmlns:xAL="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0"
exclude-result-prefixes="fo xsl ogc" >

	<xsl:template name="BUILDING">
		<citygml:Building>
			<xsl:copy-of select="citygml:Feature/@gml:id"/>
			<xsl:apply-templates select="citygml:Feature"/>	
			<xsl:apply-templates select="citygml:Feature/citygml:building/citygml:Building"/>
		</citygml:Building>
	</xsl:template>

	<xsl:template match="citygml:Building" name="BuildingTemplate">		
				
		<xsl:if test="boolean( citygml:yearOfConstruction )">
			<citygml:yearOfConstruction>
				<xsl:value-of select="citygml:yearOfConstruction"></xsl:value-of>
			</citygml:yearOfConstruction>
		</xsl:if>
		
		<xsl:if test="boolean( citygml:roofType )">
			<citygml:roofType>
				<xsl:value-of select="citygml:roofType/citygml:RoofType/citygml:roofType"></xsl:value-of>
			</citygml:roofType>
		</xsl:if>
		
		<xsl:if test="boolean( citygml:measuredheight )">
			<citygml:measuredHeight uom="m">
				<xsl:value-of select="citygml:measuredheight"></xsl:value-of>
			</citygml:measuredHeight>
		</xsl:if>
		
		<xsl:if test="boolean( citygml:storeysaboveground )">
			<citygml:storeysAboveGround>
				<xsl:value-of select="citygml:storeysaboveground"></xsl:value-of>
			</citygml:storeysAboveGround>
		</xsl:if>
		
		<xsl:if test="boolean( citygml:storeysbelowground )">
			<citygml:storeysBelowGround>
				<xsl:value-of select="citygml:storeysbelowground"></xsl:value-of>
			</citygml:storeysBelowGround>
		</xsl:if>
		
		<xsl:if test="boolean( citygml:storeyheightsaboveground )">
			<citygml:storeyHeightsAboveGround uom="m">
				<xsl:value-of select="citygml:storeyheightsaboveground"></xsl:value-of>
			</citygml:storeyHeightsAboveGround>
		</xsl:if>
		
		<xsl:if test="boolean( citygml:storeyheightsbelowground )">
			<citygml:storeyHeightsBelowGround uom="m">
				<xsl:value-of select="citygml:storeyheightsbelowground"></xsl:value-of>
			</citygml:storeyHeightsBelowGround>
		</xsl:if>

		<xsl:for-each select="../../citygml:linkedGeometry/citygml:LINK_FEAT_GEOM">
			<xsl:if test="citygml:lod = 1">
				<xsl:if test="citygml:type = 0 or citygml:type = 4">
					<citygml:lod1Solid>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod1Solid>
				</xsl:if>
				<xsl:if test="citygml:type = 1 ">
					<citygml:lod1MultiSurface>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod1MultiSurface>
				</xsl:if>				
				
				<xsl:if test="boolean( ../../citygml:lod1tis )">
					<citygml:lod1TerrainIntersection>
						<xsl:copy-of select="../../citygml:lod1tis/child::*[1]"></xsl:copy-of>
					</citygml:lod1TerrainIntersection>
				</xsl:if>
			</xsl:if>
			
			<xsl:if test="citygml:lod = 2">
				<xsl:if test="citygml:type = 0 or citygml:type = 4">
					<citygml:lod2Solid>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod2Solid>
				</xsl:if>
				<xsl:if test="citygml:type = 1 ">
					<citygml:lod2MultiSurface>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod2MultiSurface>
				</xsl:if>
				<xsl:if test="citygml:type = 2 ">
					<citygml:lod2MultiCurve>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod2MultiCurve>
				</xsl:if>
				<xsl:if test="boolean( ../../citygml:lod2tis )">
					<citygml:lod2TerrainIntersection>
						<xsl:copy-of select="../../citygml:lod2tis/child::*[1]"></xsl:copy-of>
					</citygml:lod2TerrainIntersection>
				</xsl:if>
			</xsl:if>
			
		</xsl:for-each>
				
		<xsl:for-each select="../../citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature">
			<xsl:if test="../citygml:role = 'outerBuildingInstallation' ">
				<citygml:outerBuildingInstallation>
					<xsl:call-template name="OUTERBUILDINGINSTALLATION"></xsl:call-template>
				</citygml:outerBuildingInstallation>
			</xsl:if>
		</xsl:for-each>
		
		<xsl:for-each select="../../citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature">
			<xsl:if test="../citygml:role = 'CeilingSurface' ">
				<citygml:boundedBy>
					<citygml:CeilingSurface>
						<xsl:call-template name="BOUNDEDBY"></xsl:call-template>
					</citygml:CeilingSurface>
				</citygml:boundedBy>
			</xsl:if>
			<xsl:if test="../citygml:role = 'ClosureSurface' ">
				<citygml:boundedBy>
					<citygml:ClosureSurface>
						<xsl:call-template name="BOUNDEDBY"></xsl:call-template>
					</citygml:ClosureSurface>
				</citygml:boundedBy>
			</xsl:if>
			<xsl:if test="../citygml:role = 'FloorSurface' ">
				<citygml:boundedBy>
					<citygml:FloorSurface>
						<xsl:call-template name="BOUNDEDBY"></xsl:call-template>
					</citygml:FloorSurface>
				</citygml:boundedBy>
			</xsl:if>
			<xsl:if test="../citygml:role = 'GroundSurface' ">
				<citygml:boundedBy>
					<citygml:GroundSurface>
						<xsl:call-template name="BOUNDEDBY"></xsl:call-template>
					</citygml:GroundSurface>
				</citygml:boundedBy>
			</xsl:if>
			<xsl:if test="../citygml:role = 'InteriorWallSurface' ">
				<citygml:boundedBy>
					<citygml:InteriorWallSurface>
						<xsl:call-template name="BOUNDEDBY"></xsl:call-template>
					</citygml:InteriorWallSurface>
				</citygml:boundedBy>
			</xsl:if>
			<xsl:if test="../citygml:role = 'RoofSurface' ">
				<citygml:boundedBy>
					<citygml:RoofSurface>
						<xsl:call-template name="BOUNDEDBY"></xsl:call-template>
					</citygml:RoofSurface>
				</citygml:boundedBy>
			</xsl:if>
			<xsl:if test="../citygml:role = 'WallSurface' ">
				<citygml:boundedBy>
					<citygml:WallSurface>
						<xsl:call-template name="BOUNDEDBY"></xsl:call-template>
					</citygml:WallSurface>
				</citygml:boundedBy>
			</xsl:if>			
		</xsl:for-each>
		
		<xsl:for-each select="../../citygml:linkedGeometry/citygml:LINK_FEAT_GEOM">
			<xsl:if test="citygml:lod = 3">
				<xsl:if test="citygml:type = 0 or citygml:type = 4">
					<citygml:lod3Solid>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod3Solid>
				</xsl:if>
				<xsl:if test="citygml:type = 1 ">
					<citygml:lod3MultiSurface>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod3MultiSurface>
				</xsl:if>
				<xsl:if test="citygml:type = 2 ">
					<citygml:lod3MultiCurve>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod3MultiCurve>
				</xsl:if>
				<xsl:if test="boolean( ../../citygml:lod3tis )">
					<citygml:lod3TerrainIntersection>
						<xsl:copy-of select="../../citygml:lod3tis/child::*[1]"></xsl:copy-of>
					</citygml:lod3TerrainIntersection>
				</xsl:if>
			</xsl:if>
			<xsl:if test="citygml:lod = 4">
				<xsl:if test="citygml:type = 0 or citygml:type = 4">
					<citygml:lod4Solid>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod4Solid>
				</xsl:if>
				<xsl:if test="citygml:type = 1 ">
					<citygml:lod4MultiSurface>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod4MultiSurface>
				</xsl:if>
				<xsl:if test="citygml:type = 2 ">
					<citygml:lod4MultiCurve>
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</citygml:lod4MultiCurve>
				</xsl:if>
				<xsl:if test="boolean( ../../citygml:lod4tis )">
					<citygml:lod4TerrainIntersection>
						<xsl:copy-of select="../../citygml:lod4tis/child::*[1]"></xsl:copy-of>
					</citygml:lod4TerrainIntersection>
				</xsl:if>
			</xsl:if>						
		</xsl:for-each>
		
		<!--
			// TODO
			<xsl:for-each select="../../citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature">
				<xsl:if test="../citygml:role = 'interiorRoom' ">
					<citygml:interiorRoom>
						<xsl:call-template name="INTERIORROOM"></xsl:call-template>
					</citygml:interiorRoom>
				</xsl:if>
			</xsl:for-each>
		-->
		
		<xsl:for-each select="../../citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature">
			<xsl:if test="../citygml:role = 'consistsOfBuildingPart' ">			
				<citygml:consistsOfBuildingPart>
					<xsl:call-template name="BUILDINGPART"></xsl:call-template>
				</citygml:consistsOfBuildingPart>
			</xsl:if>
		</xsl:for-each>
		
		<xsl:for-each select="../../citygml:address">
			<citygml:address>
				<xsl:apply-templates select="citygml:Address"></xsl:apply-templates>
			</citygml:address>
		</xsl:for-each>
				
	</xsl:template>
	
		
	<xsl:template name="buildingGEOM">
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
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
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
						<xsl:call-template name="buildingGEOM"></xsl:call-template>
					</gml:surfaceMember>
				</xsl:for-each>
			</gml:CompositeSurface>
		</xsl:if>
		
	</xsl:template>
	
	<xsl:template name="BUILDINGPART">
		<citygml:BuildingPart>
			<xsl:apply-templates select="citygml:Feature"/>
			<xsl:for-each select="citygml:Feature/citygml:building/citygml:Building">
				<xsl:apply-templates select="."/>
			</xsl:for-each>
		</citygml:BuildingPart>
	</xsl:template>
	
</xsl:stylesheet>
