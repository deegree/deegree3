<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:deegreewfs="http://www.deegree.org/wfs"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:fo="http://www.w3.org/1999/XSL/Format" 
 xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" xmlns:wfs="http://www.opengis.net/wfs" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:java="java" xmlns:minmax="org.deegree.framework.xml.MinMaxExtractor">
  <xsl:param name="REQUEST_ID"/>
  <xsl:param name="SEARCH_STATUS"/>
  <xsl:param name="TIMESTAMP"/>
  <xsl:param name="ELEMENT_SET"/>
  <xsl:param name="RECORD_SCHEMA"/>
  <xsl:param name="RECORDS_MATCHED"/>
  <xsl:param name="RECORDS_RETURNED"/>
  <xsl:param name="NEXT_RECORD"/>
  <xsl:template match="deegreewfs:FeatureCollection">
    <csw:GetRecordsResponse xmlns:csw="http://www.opengis.net/cat/csw" version="2.0.0">
      <csw:RequestId>
        <xsl:value-of select="$REQUEST_ID"/>
      </csw:RequestId>
      <csw:SearchStatus>
        <xsl:attribute name="status"><xsl:value-of select="$SEARCH_STATUS"/></xsl:attribute>
        <xsl:attribute name="timestamp"><xsl:value-of select="$TIMESTAMP"/></xsl:attribute>
      </csw:SearchStatus>
      <csw:SearchResults>
        <xsl:attribute name="requestId"><xsl:value-of select="$REQUEST_ID"/></xsl:attribute>
        <!--				<xsl:attribute name="elementSet"><xsl:value-of select="$ELEMENT_SET"/></xsl:attribute>-->
        <xsl:attribute name="recordSchema"><xsl:value-of select="$RECORD_SCHEMA"/></xsl:attribute>
        <xsl:attribute name="numberOfRecordsMatched"><xsl:value-of select="$RECORDS_MATCHED"/></xsl:attribute>
        <xsl:attribute name="numberOfRecordsReturned"><xsl:value-of select="$RECORDS_RETURNED"/></xsl:attribute>
        <xsl:attribute name="nextRecord"><xsl:value-of select="$NEXT_RECORD"/></xsl:attribute>
        <xsl:for-each select="gml:featureMember/Record">
          <xsl:apply-templates select="."/>
        </xsl:for-each>
      </csw:SearchResults>
    </csw:GetRecordsResponse>
  </xsl:template>
  <xsl:template match="gml:featureMember/Record">
    <csw:Record>
      <xsl:apply-templates/>
    </csw:Record>
  </xsl:template>
  <xsl:template match="DUBLINCORE.ABSTRACT">
    <dc:abstract>
      <xsl:value-of select="."/>
    </dc:abstract>
  </xsl:template>
  <xsl:template match="DUBLINCORE.TITLE">
    <dc:title>
      <xsl:value-of select="."/>
    </dc:title>
  </xsl:template>
  <xsl:template match="DUBLINCORE.CREATOR">
    <dc:creator>
      <xsl:value-of select="."/>
    </dc:creator>
  </xsl:template>
  <xsl:template match="DUBLINCORE.SUBJECT">
    <dc:subject>
      <xsl:value-of select="."/>
    </dc:subject>
  </xsl:template>
  <xsl:template match="DUBLINCORE.PUBLISHER">
    <dc:publisher>
      <xsl:value-of select="."/>
    </dc:publisher>
  </xsl:template>
  <xsl:template match="DUBLINCORE.CONTRIBUTOR">
    <dc:contributor>
      <xsl:value-of select="."/>
    </dc:contributor>
  </xsl:template>
  <xsl:template match="DUBLINCORE.DATE">
    <dc:date>
      <xsl:value-of select="."/>
    </dc:date>
  </xsl:template>
  <xsl:template match="DUBLINCORE.DESCRIPTION">
    <dc:description>
      <xsl:value-of select="."/>
    </dc:description>
  </xsl:template>
  <xsl:template match="DUBLINCORE.TYPE">
    <dc:type>
      <xsl:value-of select="."/>
    </dc:type>
  </xsl:template>
  <xsl:template match="DUBLINCORE.FORMAT">
    <dc:format>
      <xsl:value-of select="."/>
    </dc:format>
  </xsl:template>
  <xsl:template match="DUBLINCORE.IDENTIFIER">
    <dc:identifier>
      <xsl:value-of select="."/>
    </dc:identifier>
  </xsl:template>
  <xsl:template match="DUBLINCORE.SOURCE">
    <dc:source>
      <xsl:value-of select="."/>
    </dc:source>
  </xsl:template>
  <xsl:template match="DUBLINCORE.LANGUAGE">
    <dc:language>
      <xsl:value-of select="."/>
    </dc:language>
  </xsl:template>
  <xsl:template match="DUBLINCORE.RELATION">
    <dc:relation>
      <xsl:value-of select="."/>
    </dc:relation>
  </xsl:template>
  <xsl:template match="DUBLINCORE.RIGHTS">
    <dc:rights>
      <xsl:value-of select="."/>
    </dc:rights>
  </xsl:template>
  <xsl:template match="DUBLINCORE.COVERAGE">
    <dc:spatial>
      <dcmiBox:Box name="Geographic">
        <xsl:attribute name="projection"><xsl:value-of select="gml:Polygon/@srsName"/></xsl:attribute>
        <xsl:variable name="xmax">
          <xsl:value-of select="minmax:getXMax(./child::*[1])"/>
        </xsl:variable>
        <xsl:variable name="xmin">
          <xsl:value-of select="minmax:getXMin(./child::*[1])"/>
        </xsl:variable>
        <xsl:variable name="ymax">
          <xsl:value-of select="minmax:getYMax(./child::*[1])"/>
        </xsl:variable>
        <xsl:variable name="ymin">
          <xsl:value-of select="minmax:getYMin(./child::*[1])"/>
        </xsl:variable>
        <dcmiBox:northlimit units="decimal degrees">
          <xsl:value-of select="$ymax"/>
        </dcmiBox:northlimit>
        <dcmiBox:eastlimit units="decimal degrees">
          <xsl:value-of select="$xmax"/>
        </dcmiBox:eastlimit>
        <dcmiBox:southlimit units="decimal degrees">
          <xsl:value-of select="$ymin"/>
        </dcmiBox:southlimit>
        <dcmiBox:westlimit units="decimal degrees">
          <xsl:value-of select="$xmin"/>
        </dcmiBox:westlimit>
      </dcmiBox:Box>
    </dc:spatial>
  </xsl:template>
</xsl:stylesheet>
