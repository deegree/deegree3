<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" exclude-result-prefixes="wfs fo xsl ogc">

	<xsl:template name="RAILWAY">
		<citygml:Railway>
			<xsl:copy-of select="citygml:Feature/@gml:id"/>
			<xsl:apply-templates select="citygml:Feature"/>
			<xsl:apply-templates select="citygml:Feature/citygml:railway/citygml:Railway"/>
		</citygml:Railway>
	</xsl:template>

	<xsl:template match="citygml:Railway" name="RailwayTemplate">
		
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
		
		<xsl:for-each select="../../citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature">
			<xsl:if test="../citygml:role = 'trafficArea' ">
				<citygml:trafficArea>
					<xsl:call-template name="TRAFFICAREA"></xsl:call-template>
				</citygml:trafficArea>
			</xsl:if>
		</xsl:for-each>
		
		<xsl:for-each select="../../citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature">
			<xsl:if test="../citygml:role = 'auxiliaryTrafficArea' ">
				<citygml:auxiliaryTrafficArea>
					<xsl:call-template name="AUXTRAFFICAREA"></xsl:call-template>
				</citygml:auxiliaryTrafficArea>
			</xsl:if>
		</xsl:for-each>
				
		<xsl:for-each select="../../citygml:linkedGeometry/citygml:LINK_FEAT_GEOM">
			<xsl:if test="citygml:lod = 0">
				<citygml:lod0Network>
					<xsl:call-template name="railwayGEOM">
						<xsl:with-param name="LOD">0</xsl:with-param>
					</xsl:call-template>
				</citygml:lod0Network>
			</xsl:if>
		
			<xsl:if test="citygml:lod = 1">
				<citygml:lod1MultiSurface>
					<xsl:call-template name="railwayGEOM">
						<xsl:with-param name="LOD">1</xsl:with-param>
					</xsl:call-template>
				</citygml:lod1MultiSurface>
			</xsl:if>
			
			<xsl:if test="citygml:lod = 2">
				<citygml:lod2MultiSurface>
					<xsl:call-template name="railwayGEOM">
						<xsl:with-param name="LOD">2</xsl:with-param>
					</xsl:call-template>
				</citygml:lod2MultiSurface>
			</xsl:if>
			
			<xsl:if test="citygml:lod = 3">
				<citygml:lod3MultiSurface>
					<xsl:call-template name="railwayGEOM">
						<xsl:with-param name="LOD">3</xsl:with-param>
					</xsl:call-template>
				</citygml:lod3MultiSurface>
			</xsl:if>
			
			<xsl:if test="citygml:lod = 4">
				<citygml:lod4MultiSurface>
					<xsl:call-template name="railwayGEOM">
						<xsl:with-param name="LOD">4</xsl:with-param>
					</xsl:call-template>
				</citygml:lod4MultiSurface>
			</xsl:if>
		</xsl:for-each>
		
		<xsl:for-each select="../../citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature/citygml:Feature">
		</xsl:for-each>
				
	</xsl:template>
			
	<xsl:template name="railwayGEOM">
		<xsl:param name="LOD">0</xsl:param>
		
		<xsl:choose>
			<xsl:when test="$LOD = 0">
				<xsl:for-each select="citygml:curve/citygml:Curve">
					<xsl:apply-templates select="."></xsl:apply-templates>
				</xsl:for-each>	
			</xsl:when>
			<xsl:otherwise>
				<gml:MultiSurface>
					<xsl:for-each select="citygml:polygon/citygml:Polygon">
						<gml:surfaceMember>
							<xsl:apply-templates select="."></xsl:apply-templates>
						</gml:surfaceMember>
					</xsl:for-each>
				</gml:MultiSurface>
			</xsl:otherwise>
		</xsl:choose>		
		
	</xsl:template>
	
</xsl:stylesheet>
