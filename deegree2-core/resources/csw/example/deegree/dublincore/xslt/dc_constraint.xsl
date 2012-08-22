<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:app="http://www.deegree.org/csw/dc" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:ogc="http://www.opengis.net/ogc">
	<!--
		Converts filter used in catalogue requests to the corresponding WFS filter.

		The complete contents of the 'csw:Constraint' is simply copied recursively, only the contents of 'ogc:PropertyName' elements is converted.
	-->
	<xsl:template match="csw:Constraint">
		<xsl:call-template name="copyFilterDeep"/>
	</xsl:template>
	<xsl:template name="copyFilterDeep">
		<xsl:choose>
			<xsl:when test="local-name() = 'PropertyName' and namespace-uri() = 'http://www.opengis.net/ogc'">
				<xsl:call-template name="convertToWFSPropertyName">
					<xsl:with-param name="CSW_PROPERTY">
						<xsl:value-of select="text()"/>
					</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="@* | text()"/>
				<xsl:for-each select="*">
					<xsl:copy>
						<xsl:call-template name="copyFilterDeep"/>
					</xsl:copy>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!--
		Converts property names used in catalogue requests to the corresponding feature properties.

		There are two different ways to specify properties in catalogue requests:
			- dc:*/dct* property names
            - "Core queryable properties" (Title, Subject, Abstract, Modified, Type, Format, Identifier, Source, Relation, Source,Target, Envelope, CRS)
	-->
	<xsl:template name="convertToWFSPropertyName">
		<xsl:param name="CSW_PROPERTY"/>
		<xsl:choose>
			<xsl:when test="contains($CSW_PROPERTY, ':')">
				<xsl:value-of select="concat('app:', substring-after($CSW_PROPERTY, ':'))"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat('app:', $CSW_PROPERTY)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
