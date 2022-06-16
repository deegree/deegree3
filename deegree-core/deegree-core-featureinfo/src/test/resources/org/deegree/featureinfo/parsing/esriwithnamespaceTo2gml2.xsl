<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:app="http://www.deegree.org"
  xmlns:gml="http://www.opengis.net/gml" xmlns:esri_wms="http://www.esri.com/wms">
  <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
  <xsl:template match="esri_wms:FeatureInfoResponse">
    <gml:FeatureCollection xmlns:app="http://www.deegree.org" xmlns:gml="http://www.opengis.net/gml"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <xsl:apply-templates select="esri_wms:FIELDS" />
    </gml:FeatureCollection>
  </xsl:template>
  <xsl:template match="esri_wms:FIELDS">
    <gml:featureMember>
      <app:Test>
        <app:NaturaTunnus>
          <xsl:value-of select="@NaturaTunnus" />
        </app:NaturaTunnus>
        <app:Nimi>
          <xsl:value-of select="@Nimi" />
        </app:Nimi>
      </app:Test>
    </gml:featureMember>
  </xsl:template>
</xsl:stylesheet>