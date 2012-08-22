<?xml version="1.0" encoding="UTF-8"?>
<!--
	Default conversion script for transforming the GML response to a GetFeatureInfo
	request
	author: Andreas Poth
	version: 4.6.2003
	(c) deegree - LGPL
-->
<xsl:stylesheet version="1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ll="http://www.lat-lon.de" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:gml="http://www.opengis.net/gml" xsi:schemaLocation="http://www.lat-lon.de/ http://services.deegree.org/deegree/schema/featureschema.xsd">
	<xsl:template match="ll:FeatureCollection">
			<xsl:copy-of select="."></xsl:copy-of>
	</xsl:template>
</xsl:stylesheet>
