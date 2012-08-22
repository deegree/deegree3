<?xml version="1.0" encoding="UTF-8"?>
  <!--
    author: Andreas Poth
    (c) deegree - LGPL
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ll="http://www.lat-lon.de">
  <xsl:template match="ll:FeatureCollection">
    <xsl:copy-of select="."></xsl:copy-of>
  </xsl:template>
</xsl:stylesheet>
