<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:srv="http://www.isotc211.org/2005/srv" exclude-result-prefixes="xsl">

	<xsl:template match="gmd:MD_Metadata">
		<gmd:MD_Metadata>
			<xsl:copy-of select="./@*"></xsl:copy-of>
			<xsl:copy-of select="gmd:fileIdentifier"></xsl:copy-of>
            <!-- 
			<xsl:copy-of select="gmd:hierarchyLevel"></xsl:copy-of>
             -->
			<gmd:identificationInfo>
				<xsl:apply-templates select="gmd:identificationInfo/gmd:MD_DataIdentification"></xsl:apply-templates>
				<xsl:apply-templates select="gmd:identificationInfo/srv:SV_ServiceIdentification"></xsl:apply-templates>
			</gmd:identificationInfo>
		</gmd:MD_Metadata>
	</xsl:template>

	<xsl:template match="gmd:MD_DataIdentification">
		<gmd:MD_DataIdentification>
			<xsl:call-template name="CONTENT"></xsl:call-template>
		</gmd:MD_DataIdentification>
	</xsl:template>
	
	<xsl:template match="srv:SV_ServiceIdentification">
		<srv:SV_ServiceIdentification>
			<xsl:call-template name="CONTENT"></xsl:call-template>
		</srv:SV_ServiceIdentification>
	</xsl:template>
	
	<xsl:template name="CONTENT">
		<gmd:citation>
			<gmd:CI_Citation>
				<gmd:title>
					<xsl:copy-of select="gmd:citation/gmd:CI_Citation/gmd:title"></xsl:copy-of>
				</gmd:title>					
                <!-- 
				<gmd:identifier>
					<gmd:MD_Identifier>
						<gmd:code>
							<gco:CharacterString>
								<xsl:value-of select="/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString"></xsl:value-of>
							</gco:CharacterString>
						</gmd:code>
					</gmd:MD_Identifier>
				</gmd:identifier>
                 -->
			</gmd:CI_Citation>				
		</gmd:citation>
        <!-- 
		<xsl:if test="boolean( gmd:graphicOverview )">
			<gmd:graphicOverview>
				<xsl:copy-of select="gmd:graphicOverview"></xsl:copy-of>
			</gmd:graphicOverview>
		</xsl:if>
		<xsl:if test="boolean( gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox )">
			<gmd:extent>
				<gmd:EX_Extent>
					<gmd:geographicElement>
						<xsl:copy-of select="gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox"></xsl:copy-of>
					</gmd:geographicElement>
				</gmd:EX_Extent>
			</gmd:extent>
		</xsl:if>
         -->
	</xsl:template>
</xsl:stylesheet>
