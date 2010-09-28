<?xml version="1.0" encoding="iso-8859-1"?>
<!-- (c) 2007 interactive instruments GmbH -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:def="http://www.interactive-instruments.de/ShapeChange/Definitions/0.5" xmlns:xlink="http://www.w3.org/1999/xlink">
	<xsl:output method="html"/>
	<xsl:include href="ShapeChangeDefinitionsUtility.xsl"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Definitions for type '<xsl:value-of select="/def:TypeDefinition/gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>'</title>
			</head>
			<body>
				<xsl:apply-templates select="/def:TypeDefinition" mode="header"/>
				<xsl:apply-templates select="/def:TypeDefinition" mode="body"/>
				<xsl:for-each select="/def:TypeDefinition/gml:dictionaryEntry/def:PropertyDefinition">
					<xsl:apply-templates select="." mode="header"/>
					<xsl:apply-templates select="." mode="body"/>
				</xsl:for-each>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
