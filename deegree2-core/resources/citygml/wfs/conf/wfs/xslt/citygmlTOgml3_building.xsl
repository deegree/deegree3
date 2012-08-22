<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" xmlns:citygml="http://www.citygml.org/citygml/1/0/0" xmlns:xAL="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0">
	<xsl:template name="BUILDING">
		<citygml:building>
			<citygml:Building>
				<xsl:call-template name="ADDGMLID"/>
				<xsl:if test="boolean( citygml:yearOfConstruction )">
					<citygml:yearOfConstruction>
						<xsl:value-of select="citygml:yearOfConstruction"/>
					</citygml:yearOfConstruction>
				</xsl:if>
				<xsl:if test="boolean( citygml:measuredHeight )">
					<citygml:measuredheight>
						<xsl:value-of select="citygml:measuredHeight"/>
					</citygml:measuredheight>
				</xsl:if>
				<xsl:if test="boolean( citygml:storeysAboveGround )">
					<citygml:storeysaboveground>
						<xsl:value-of select="citygml:storeysAboveGround"/>
					</citygml:storeysaboveground>
				</xsl:if>
				<xsl:if test="boolean( citygml:storeysBelowGround )">
					<citygml:storeysbelowground>
						<xsl:value-of select="citygml:storeysBelowGround"/>
					</citygml:storeysbelowground>
				</xsl:if>
				<xsl:if test="boolean( citygml:storeyHeightsAboveGround )">
					<citygml:storeyheightsaboveground>
						<xsl:value-of select="citygml:storeyHeightsAboveGround"/>
					</citygml:storeyheightsaboveground>
				</xsl:if>
				<xsl:if test="boolean( citygml:storeyHeightsBelowGround )">
					<citygml:storeyheightsbelowground>
						<xsl:value-of select="citygml:storeyHeightsBelowGround"/>
					</citygml:storeyheightsbelowground>
				</xsl:if>
				<xsl:if test="boolean( citygml:roofType )">
					<citygml:roofType>
						<xsl:value-of select="citygml:roofType"/>
					</citygml:roofType>
				</xsl:if>
			</citygml:Building>
		</citygml:building>
		<xsl:call-template name="FEATURE"/>
	</xsl:template>
	<xsl:template match="citygml:BuildingInstallation">
		<xsl:call-template name="LINK_FEAT">
			<xsl:with-param name="ROLE" select="'outerBuildingInstallation'"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="citygml:boundedBy/*">
		<xsl:call-template name="LINK_FEAT">
			<xsl:with-param name="ROLE" select="local-name( . )"/>
		</xsl:call-template>
	</xsl:template>
	<xsl:template match="citygml:BuildingPart">
		<xsl:call-template name="LINK_FEAT">
			<xsl:with-param name="ROLE" select="'consistsOfBuildingPart'"/>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
