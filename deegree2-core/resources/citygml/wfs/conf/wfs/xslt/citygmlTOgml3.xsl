<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" 
xmlns:java="java" xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils"
xmlns:wfs="http://www.opengis.net/wfs" 
xmlns:xAL="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0">

	<xsl:include href="citygmlTOgml3_building.xsl"></xsl:include>
	<xsl:include href="citygmlTOgml3_road.xsl"></xsl:include>
	<xsl:include href="citygmlTOgml3_railway.xsl"></xsl:include>
	<xsl:include href="citygmlTOgml3_cityfurniture.xsl"></xsl:include>
	
	<xsl:template match="wfs:FeatureCollection | citygml:CityModel">
		<wfs:FeatureCollection>			
			<xsl:for-each select="citygml:cityObjectMember | gml:featureMember">
				<xsl:apply-templates select="."></xsl:apply-templates>
			</xsl:for-each>
		</wfs:FeatureCollection>
	</xsl:template>
		
	<xsl:template name="ADDGMLID">

	</xsl:template>
	
	<xsl:template name="ADDGMLIDNUMBER">

	</xsl:template>
	
	<xsl:template match="citygml:Building">
		<citygml:Feature>			
			<xsl:copy-of select="./@gml:id"></xsl:copy-of>
			<xsl:call-template name="BUILDING"></xsl:call-template>
		</citygml:Feature>
	</xsl:template>
	
	<xsl:template match="citygml:Road">
		<citygml:Feature>			
			<xsl:copy-of select="./@gml:id"></xsl:copy-of>
			<xsl:call-template name="ROAD"></xsl:call-template>
		</citygml:Feature>
	</xsl:template>
	
	<xsl:template match="citygml:Railway">
		<citygml:Feature>			
			<xsl:copy-of select="./@gml:id"></xsl:copy-of>
			<xsl:call-template name="RAILWAY"></xsl:call-template>
		</citygml:Feature>
	</xsl:template>
	
	<xsl:template match="citygml:CityFurniture">
		<citygml:Feature>			
			<xsl:copy-of select="./@gml:id"></xsl:copy-of>
			<xsl:call-template name="CITYFURNITURE"></xsl:call-template>
		</citygml:Feature>
	</xsl:template>
				
	<xsl:template name="FEATURE">		
		<xsl:if test="boolean( citygml:address )">
			<xsl:call-template name="ADDRESS"></xsl:call-template>
		</xsl:if>
		<citygml:type>
			<xsl:value-of select="local-name(.)"></xsl:value-of>
		</citygml:type>
		<xsl:if test="boolean( citygml:creationDate )">
			<citygml:creation>
				<xsl:value-of select="citygml:creationDate"></xsl:value-of>
			</citygml:creation>
		</xsl:if>
		<xsl:if test="boolean( citygml:terminationDate )">
			<citygml:deletion>
				<xsl:value-of select="citygml:terminationDate"></xsl:value-of>
			</citygml:deletion>
		</xsl:if>
		<xsl:if test="gml:boundedBy">
			<citygml:envelope>				
				<gml:Polygon>
					<xsl:attribute name="srsName"><xsl:value-of select="./gml:boundedBy/gml:Envelope/@srsName"></xsl:value-of></xsl:attribute>
					<gml:outerBoundaryIs>
						<gml:LinearRing>
							<gml:coordinates cs="," decimal="." ts=" ">
								<xsl:value-of select="geometryutil:getPolygonCoordinatesFromEnvelope( ./gml:boundedBy/gml:Envelope )"></xsl:value-of>
							</gml:coordinates>
						</gml:LinearRing>
					</gml:outerBoundaryIs>
				</gml:Polygon>
			</citygml:envelope>
		</xsl:if>
		<xsl:if test="boolean( gml:name )">
			<citygml:featureName>
				<xsl:value-of select="gml:name"></xsl:value-of>
			</citygml:featureName>
		</xsl:if>
	
    	<xsl:apply-templates select="citygml:externalReference"></xsl:apply-templates>
	
    	<xsl:if test="boolean( citygml:lod1TerrainIntersection )">
            <citygml:lod1tis>
                <xsl:copy-of select="citygml:lod1TerrainIntersection/gml:MultiCurve"></xsl:copy-of>
            </citygml:lod1tis>
		</xsl:if>
		<xsl:if test="boolean( citygml:lod2TerrainIntersection )">
			<citygml:lod2tis>
                <xsl:copy-of select="citygml:lod2TerrainIntersection/gml:MultiCurve"></xsl:copy-of>
            </citygml:lod2tis>
		</xsl:if>
		<xsl:if test="boolean( citygml:lod3TerrainIntersection )">
			<citygml:lod3tis>
                <xsl:copy-of select="citygml:lod3TerrainIntersection/gml:MultiCurve"></xsl:copy-of>
            </citygml:lod3tis>
		</xsl:if>
		<xsl:if test="boolean( citygml:lod4TerrainIntersection )">
			<citygml:lod4tis>
                <xsl:copy-of select="citygml:lod4TerrainIntersection/gml:MultiCurve"></xsl:copy-of>
            </citygml:lod4tis>
		</xsl:if>

        <xsl:if test="boolean(citygml:function)">
    		<citygml:function>
    			<xsl:value-of select="citygml:function"></xsl:value-of>
    		</citygml:function>
        </xsl:if>        
		
        <xsl:if test="boolean(citygml:usage)">
    		<citygml:usage>
    			<xsl:value-of select="citygml:usage"></xsl:value-of>
    		</citygml:usage>
        </xsl:if>

		<xsl:apply-templates select="citygml:outerBuildingInstallation/citygml:BuildingInstallation"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:boundedBy/*"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:generalizesTo"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:consistsOfBuildingPart/citygml:BuildingPart"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:trafficArea/citygml:TrafficArea"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:auxiliaryTrafficArea/citygml:AuxiliaryTrafficArea"></xsl:apply-templates>
					
		<xsl:for-each select="citygml:lod1Solid | citygml:lod1MultiSurface | citygml:lod1Geometry">
			<xsl:call-template name="LINK_FEAT_GEOM">
				<xsl:with-param name="LOD" select="'1'"></xsl:with-param>
			</xsl:call-template>
		</xsl:for-each>
		
		<xsl:for-each select="citygml:lod2Solid | citygml:lod2MultiSurface | citygml:lod2MultiCurve | citygml:lod2Geometry">
			<xsl:call-template name="LINK_FEAT_GEOM">
				<xsl:with-param name="LOD" select="'2'"></xsl:with-param>
			</xsl:call-template>
		</xsl:for-each>
		
		<xsl:for-each select="citygml:lod3Solid | citygml:lod3MultiSurface | citygml:lod3MultiCurve | citygml:lod3Geometry">
			<xsl:call-template name="LINK_FEAT_GEOM">
				<xsl:with-param name="LOD" select="'3'"></xsl:with-param>
			</xsl:call-template>
		</xsl:for-each>
		
		<xsl:for-each select="citygml:lod4Solid | citygml:lod4MultiSurface | citygml:lod4MultiCurve | citygml:lod4Geometry">
			<xsl:call-template name="LINK_FEAT_GEOM">
				<xsl:with-param name="LOD" select="'4'"></xsl:with-param>
			</xsl:call-template>
		</xsl:for-each>
		
		<xsl:apply-templates select="citygml:dateAttribute"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:intAttribute"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:stringAttribute"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:uriAttribute"></xsl:apply-templates>
		<xsl:apply-templates select="citygml:doubleAttribute"></xsl:apply-templates>		
	</xsl:template>
	
	<xsl:template name="ADDRESS">
		<citygml:address>
			<citygml:Address>
				<xsl:call-template name="ADDGMLID"/>
				<xsl:if test="boolean( citygml:address//xAL:Locality[./@Type = 'Town']/xAL:Thoroughfare[./@Type = 'Street']/xAL:ThoroughfareNumber )">
					<citygml:code>
						<xsl:value-of select="citygml:address//xAL:Locality[./@Type = 'Town']/xAL:Thoroughfare[./@Type = 'Street']/xAL:ThoroughfareNumber"></xsl:value-of>
					</citygml:code>
				</xsl:if>
				<xsl:if test="boolean( citygml:address//xAL:Locality[./@Type = 'Town']/xAL:Thoroughfare[./@Type = 'Street']/xAL:ThoroughfareName )">
					<citygml:street>
						<xsl:value-of select="citygml:address//xAL:Locality[./@Type = 'Town']/xAL:Thoroughfare[./@Type = 'Street']/xAL:ThoroughfareName"></xsl:value-of>
					</citygml:street>
				</xsl:if>
				<xsl:if test="boolean( citygml:address//xAL:Locality[./@Type = 'Town']/xAL:PostalCode/xAL:PostalCodeNumber )">
					<citygml:postalcode>
						<xsl:value-of select="citygml:address//xAL:Locality[./@Type = 'Town']/xAL:PostalCode/xAL:PostalCodeNumber"></xsl:value-of>
					</citygml:postalcode>
				</xsl:if>
				<xsl:if test="boolean( citygml:address//xAL:Locality[./@Type = 'Town']/xAL:LocalityName )">
					<citygml:city>
						<xsl:value-of select="citygml:address//xAL:Locality[./@Type = 'Town']/xAL:LocalityName"></xsl:value-of>
					</citygml:city>
				</xsl:if>
				<xsl:if test="boolean( citygml:address//xAL:AdministrativeArea/xAL:AdministrativeAreaName )">
					<citygml:administrativearea>
						<xsl:value-of select="citygml:address//xAL:AdministrativeArea/xAL:AdministrativeAreaName"></xsl:value-of>	
					</citygml:administrativearea>
				</xsl:if>
				<xsl:if test="boolean( citygml:address//xAL:Country/xAL:CountryName )">
					<citygml:country>
						<xsl:value-of select="citygml:address/citygml:Address/citygml:xalAddress/xAL:AddressDetails/xAL:Country/xAL:CountryName"></xsl:value-of>
					</citygml:country>
				</xsl:if>
			</citygml:Address>
		</citygml:address>
	</xsl:template>
	
	<xsl:template match="citygml:externalReference">
		<citygml:externalReference>
			<citygml:Externalref>
				<xsl:call-template name="ADDGMLID"/>
				<xsl:if test="citygml:externalObject/citygml:name">
					<citygml:name>
						<xsl:value-of select="citygml:externalObject/citygml:name"></xsl:value-of>
					</citygml:name>
				</xsl:if>
				<xsl:if test="citygml:externalObject/citygml:uri">
					<citygml:uri>
						<xsl:value-of select="citygml:externalObject/citygml:uri"></xsl:value-of>
					</citygml:uri>
				</xsl:if>
				<xsl:if test="boolean( citygml:informationSystem )">
					<citygml:informationsystem>
						<xsl:value-of select="citygml:informationSystem"></xsl:value-of>
					</citygml:informationsystem>
				</xsl:if>
			</citygml:Externalref>
		</citygml:externalReference>
	</xsl:template>
	
	<xsl:template match="citygml:generalizesTo">
		<citygml:linkedFeature>
			<citygml:LINK_FEAT_FEAT>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:feature>
					<xsl:apply-templates select="citygml:Building"></xsl:apply-templates>
				</citygml:feature>
				<citygml:role>generalizesTo</citygml:role>
			</citygml:LINK_FEAT_FEAT>
		</citygml:linkedFeature>
	</xsl:template>
	
	<xsl:template name="LINK_FEAT">
		<xsl:param name="ROLE"></xsl:param>
		<citygml:linkedFeature>
			<citygml:LINK_FEAT_FEAT>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:feature>
					<citygml:Feature>
						<xsl:copy-of select="./@gml:id"></xsl:copy-of>
						<xsl:call-template name="FEATURE"></xsl:call-template>
					</citygml:Feature>
				</citygml:feature>	
				<xsl:if test="boolean( citygml:creationDate )">
					<citygml:creation>
						<xsl:value-of select="citygml:creationDate"></xsl:value-of>
					</citygml:creation>
				</xsl:if>
				<xsl:if test="boolean( citygml:terminationDate )">
					<citygml:deletion>
						<xsl:value-of select="citygml:terminationDate"></xsl:value-of>
					</citygml:deletion>
				</xsl:if>
				<citygml:role>
					<xsl:value-of select="$ROLE"></xsl:value-of>
				</citygml:role>
			</citygml:LINK_FEAT_FEAT>
		</citygml:linkedFeature>	
	</xsl:template>
	
	<xsl:template name="LINK_FEAT_GEOM">
		<xsl:param name="LOD"></xsl:param>
		<citygml:linkedGeometry>
			<citygml:LINK_FEAT_GEOM>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:lod>
					<xsl:value-of select="$LOD"></xsl:value-of>
				</citygml:lod>
				<xsl:if test="gml:Solid">
					<citygml:type>0</citygml:type>
				</xsl:if>
				<xsl:if test="gml:MultiSurface">
					<citygml:type>1</citygml:type>
				</xsl:if>
				<xsl:if test="gml:MultiCurve">
					<citygml:type>2</citygml:type>
				</xsl:if>
				<xsl:if test="gml:CompositeSolid">
					<citygml:type>4</citygml:type>
				</xsl:if>
				<xsl:apply-templates select="gml:Solid"></xsl:apply-templates>
				<xsl:apply-templates select="gml:CompositeSolid"></xsl:apply-templates>
				<xsl:apply-templates select="gml:MultiSurface"></xsl:apply-templates>
				<xsl:apply-templates select="gml:CompositeSurface"></xsl:apply-templates>
				<xsl:apply-templates select="gml:MultiCurve"></xsl:apply-templates>
				<xsl:apply-templates select="gml:CompositeCurve"></xsl:apply-templates>				
			</citygml:LINK_FEAT_GEOM>
		</citygml:linkedGeometry>
	</xsl:template>
	
	<xsl:template match="gml:Solid">
		<citygml:solid>
			<citygml:Solid>
				<xsl:call-template name="ADDGMLID"/>
				<xsl:for-each select="gml:exterior/gml:CompositeSurface/gml:surfaceMember/child::*">
					<citygml:exteriorSurfaces>					
						<citygml:Polygon>
							<xsl:call-template name="ADDGMLID"/>
							<citygml:geom>
								<xsl:apply-templates select="."></xsl:apply-templates>
							</citygml:geom>
							<xsl:apply-templates select="citygml:appearance/citygml:Material"></xsl:apply-templates>
							<xsl:apply-templates select="citygml:appearance/citygml:SimpleTexture"></xsl:apply-templates>
						</citygml:Polygon>					
					</citygml:exteriorSurfaces>
				</xsl:for-each>
				<xsl:if test="gml:interior">
					<xsl:for-each select="gml:interior">
						<citygml:interiorSurfaces>
							<citygml:InteriorSurface>
								<xsl:call-template name="ADDGMLIDNUMBER"/>							
								<xsl:for-each select="/gml:CompositeSurface/gml:surfaceMember/child::*">
									<citygml:interiorSurfaces>
										<citygml:Polygon>					
											<xsl:call-template name="ADDGMLID"/>					
											<citygml:geom>
												<xsl:apply-templates select="."></xsl:apply-templates>
											</citygml:geom>
										</citygml:Polygon>
									</citygml:interiorSurfaces>
								</xsl:for-each>
							</citygml:InteriorSurface>	
						</citygml:interiorSurfaces>
					</xsl:for-each>
				</xsl:if>
			</citygml:Solid>
		</citygml:solid>
	</xsl:template>
	
	<xsl:template match="gml:CompositeSolid">	
		<xsl:for-each select="gml:solidMember/child::*">
			<citygml:composite>
				<citygml:LINK_FEAT_GEOM>
					<xsl:call-template name="ADDGMLID"/>
					<citygml:type>0</citygml:type>
					<xsl:apply-templates select="."></xsl:apply-templates>
				</citygml:LINK_FEAT_GEOM>
			</citygml:composite>
		</xsl:for-each>		
	</xsl:template>
	
	<xsl:template match="gml:MultiSurface">
		<xsl:for-each select="gml:surfaceMember/child::*">
			<xsl:choose>            
				<xsl:when test="local-name( . ) = 'TexturedSurface' or local-name( . ) = 'Polygon' or local-name( . ) = 'Surface'">
					<citygml:polygon>
						<citygml:Polygon>		
							<xsl:call-template name="ADDGMLID"/>
							<citygml:geom>						
								<xsl:apply-templates select="."></xsl:apply-templates>	
							</citygml:geom>
							<xsl:apply-templates select="citygml:appearance/citygml:Material"></xsl:apply-templates>
							<xsl:apply-templates select="citygml:appearance/citygml:SimpleTexture"></xsl:apply-templates>
							<xsl:if test="boolean(./@orientation)">
								<citygml:orientation>
									<xsl:value-of select="./@orientation"></xsl:value-of>
								</citygml:orientation>
							</xsl:if>
						</citygml:Polygon>
					</citygml:polygon>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="."></xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="gml:CompositeSurface">
		<citygml:composite>
			<citygml:LINK_FEAT_GEOM>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:type>5</citygml:type>
				<xsl:for-each select="gml:surfaceMember/child::*">
					<citygml:polygon>
						<citygml:Polygon>		
							<xsl:call-template name="ADDGMLID"/>
							<citygml:geom>						
								<xsl:apply-templates select="."></xsl:apply-templates>	
							</citygml:geom>
							<xsl:apply-templates select="citygml:appearance/citygml:Material"></xsl:apply-templates>
							<xsl:apply-templates select="citygml:appearance/citygml:SimpleTexture"></xsl:apply-templates>
							<xsl:if test="boolean(./@orientation)">
								<citygml:orientation>
									<xsl:value-of select="./@orientation"></xsl:value-of>
								</citygml:orientation>
							</xsl:if>
						</citygml:Polygon>
					</citygml:polygon>			
				</xsl:for-each>
			</citygml:LINK_FEAT_GEOM>
		</citygml:composite>
	</xsl:template>
	
	<xsl:template match="gml:MultiCurve">
		<xsl:for-each select="gml:curveMember/child::*">
			<citygml:curve>
				<citygml:Curve>
					<xsl:call-template name="ADDGMLID"/>
					<citygml:geom>
						<xsl:copy-of select="."></xsl:copy-of>
					</citygml:geom>
				</citygml:Curve>
			</citygml:curve>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="gml:CompositeCurve">
		<xsl:for-each select="gml:curveMember/child::*">
			<citygml:composite>
				<citygml:LINK_FEAT_GEOM>
					<xsl:call-template name="ADDGMLID"/>
					<xsl:apply-templates select="."></xsl:apply-templates>
				</citygml:LINK_FEAT_GEOM>
			</citygml:composite>
		</xsl:for-each>
	</xsl:template>
		
	<xsl:template match="citygml:TexturedSurface">
		<xsl:apply-templates select="gml:baseSurface/child::*"></xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="gml:Surface | gml:Polygon | gml:Curve | gml:LineString | gml:Point">		
		<xsl:copy>
			<xsl:choose>
				<xsl:when test="boolean( ./@srsName ) = false">
					<xsl:attribute name="srsName"><xsl:value-of select="//@srsName"></xsl:value-of></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="srsName"><xsl:value-of select="./@srsName"></xsl:value-of></xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:for-each select="./*">
				<xsl:copy-of select="."></xsl:copy-of>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>
		
	<xsl:template match="citygml:Material">
		<citygml:material>
			<citygml:Material>
				<xsl:call-template name="ADDGMLID"/>
				<xsl:if test="citygml:shininess">
					<citygml:shininess>
						<xsl:value-of select="citygml:shininess"></xsl:value-of>
					</citygml:shininess>
				</xsl:if>
				<xsl:if test="citygml:transparency">
					<citygml:transparency>
						<xsl:value-of select="citygml:transparency"></xsl:value-of>
					</citygml:transparency>
				</xsl:if>
				<xsl:if test="citygml:ambientIntensity">
					<citygml:ambientintensity>
						<xsl:value-of select="citygml:ambientIntensity"></xsl:value-of>
					</citygml:ambientintensity>
				</xsl:if>
				<xsl:if test="citygml:specularColor">
					<citygml:specularcolor>
						<xsl:value-of select="citygml:specularColor"></xsl:value-of>
					</citygml:specularcolor>
				</xsl:if>
				<xsl:if test="citygml:diffuseColor">
					<citygml:diffusecolor>
						<xsl:value-of select="citygml:diffuseColor"></xsl:value-of>
					</citygml:diffusecolor>
				</xsl:if>
				<xsl:if test="citygml:emissiveColor">
					<citygml:emissivecolor>
						<xsl:value-of select="citygml:emissiveColor"></xsl:value-of>
					</citygml:emissivecolor>
				</xsl:if>
			</citygml:Material>
		</citygml:material>
	</xsl:template>
	
	<xsl:template match="citygml:SimpleTexture">
		<citygml:texture>
			<citygml:Texture>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:texturemap>
					<xsl:value-of select="citygml:textureMap"></xsl:value-of>
				</citygml:texturemap>
				<citygml:texturecoordinates>
					<xsl:value-of select="citygml:textureCoordinates"></xsl:value-of>
				</citygml:texturecoordinates>
				<xsl:if test="citygml:textureType">
					<citygml:texturetype>
						<xsl:value-of select="citygml:textureType"></xsl:value-of>
					</citygml:texturetype>
				</xsl:if>
				<xsl:if test="citygml:repeat">
					<citygml:repeat>
						<xsl:value-of select="citygml:repeat"></xsl:value-of>
					</citygml:repeat>
				</xsl:if>
			</citygml:Texture>
		</citygml:texture>
	</xsl:template>	
		
	<xsl:template match="citygml:dateAttribute">
		<citygml:dateAttribute>
			<citygml:Date_Attribute>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:name>
					<xsl:value-of select="./@name"></xsl:value-of>
				</citygml:name>
				<citygml:value>
					<xsl:value-of select="citygml:value"></xsl:value-of>
				</citygml:value>
			</citygml:Date_Attribute>
		</citygml:dateAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:doubleAttribute">
		<citygml:doubleAttribute>
			<citygml:Float_Attribute>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:name>
					<xsl:value-of select="./@name"></xsl:value-of>
				</citygml:name>
				<citygml:value>
					<xsl:value-of select="citygml:value"></xsl:value-of>
				</citygml:value>
			</citygml:Float_Attribute>
		</citygml:doubleAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:intAttribute">
		<citygml:intAttribute>
			<citygml:Int_Attribute>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:name>
					<xsl:value-of select="./@name"></xsl:value-of>
				</citygml:name>
				<citygml:value>
					<xsl:value-of select="citygml:value"></xsl:value-of>
				</citygml:value>
			</citygml:Int_Attribute>
		</citygml:intAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:stringAttribute">
		<citygml:stringAttribute>
			<citygml:Text_Attribute>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:name>
					<xsl:value-of select="./@name"></xsl:value-of>
				</citygml:name>
				<citygml:value>
					<xsl:value-of select="citygml:value"></xsl:value-of>
				</citygml:value>
			</citygml:Text_Attribute>
		</citygml:stringAttribute>
	</xsl:template>
	
	<xsl:template match="citygml:uriAttribute">
		<citygml:uriAttribute>
			<citygml:URI_Attribute>
				<xsl:call-template name="ADDGMLID"/>
				<citygml:name>
					<xsl:value-of select="./@name"></xsl:value-of>
				</citygml:name>
				<citygml:value>
					<xsl:value-of select="citygml:value"></xsl:value-of>
				</citygml:value>
			</citygml:URI_Attribute>
		</citygml:uriAttribute>
	</xsl:template>

</xsl:stylesheet>
