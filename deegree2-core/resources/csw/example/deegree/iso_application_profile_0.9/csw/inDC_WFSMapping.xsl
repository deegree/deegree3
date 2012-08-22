<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<!-- Mappings used for DublinCore fields in the generated WFS-GetFeature-requests.-->
	<xsl:variable name="WFS_title">DUBLINCORE.TITLE</xsl:variable>
	<xsl:variable name="WFS_creator">DUBLINCORE.CREATOR</xsl:variable>
	<xsl:variable name="WFS_subject">DUBLINCORE.SUBJECT</xsl:variable>
	<xsl:variable name="WFS_description">DUBLINCORE.DESCRIPTION</xsl:variable>
	<xsl:variable name="WFS_publisher">DUBLINCORE.PUBLISHER</xsl:variable>
	<xsl:variable name="WFS_contributor">DUBLINCORE.CONTRIBUTOR</xsl:variable>
	<xsl:variable name="WFS_date">DUBLINCORE.DATE</xsl:variable>
	<xsl:variable name="WFS_type">DUBLINCORE.TYPE</xsl:variable>
	<xsl:variable name="WFS_format">DUBLINCORE.FORMAT</xsl:variable>
	<xsl:variable name="WFS_identifier">DUBLINCORE.IDENTIFIER</xsl:variable>
	<xsl:variable name="WFS_source">DUBLINCORE.SOURCE</xsl:variable>
	<xsl:variable name="WFS_language">DUBLINCORE.LANGUAGE</xsl:variable>
	<xsl:variable name="WFS_relation">DUBLINCORE.RELATION</xsl:variable>
	<xsl:variable name="WFS_coverage">DUBLINCORE.COVERAGE</xsl:variable>
	<xsl:variable name="WFS_rights">DUBLINCORE.RIGHTS</xsl:variable>
</xsl:stylesheet>
