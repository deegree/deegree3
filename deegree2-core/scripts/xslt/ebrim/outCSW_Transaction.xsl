<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:app="http://www.deegree.org/app"
  xmlns:csw="http://www.opengis.net/cat/csw"
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:ogc="http://www.opengis.net/ogc" 
  xmlns:rim="urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0"
  xmlns:wfs="http://www.opengis.net/wfs" 
  xmlns:wrs="http://www.opengis.net/cat/wrs"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="wfs app xsi xsl ogc"
>
  <!-- the xmlns:java and xmlns:mapping defines are necessary for else they might appear in inner nodes 
       
       The following templates used here are found in inEbrim.xsl 
       
       All ebrim transformations are done in EBRIM_To_GML_feature.xsd
       -->

  <xsl:output method="xml" indent="yes"/>

  <!-- ###################################
       # Mapping the TransactionResponse #
       ###################################-->
  <xsl:template match="wfs:TransactionResponse">
    <csw:TransactionResponse>
      <xsl:apply-templates select="wfs:TransactionSummary"/>
      <xsl:apply-templates select="wfs:InsertResults"/>
    </csw:TransactionResponse>
  </xsl:template>
  
  <xsl:template match="wfs:TransactionSummary">
    <csw:TransactionSummary>
      <xsl:if test="$REQUEST_ID != ''">
        <xsl:attribute name="requestId">
          <xsl:value-of select="$REQUEST_ID"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="wfs:totalInserted != ''">
        <csw:totalInserted>
          <xsl:value-of select="normalize-space( wfs:totalInserted )"/>
        </csw:totalInserted>
      </xsl:if>
      
      <xsl:if test="wfs:totalUpdated != ''">
        <csw:totalUpdated>
          <xsl:value-of select="normalize-space( wfs:totalUpdated )"/>
        </csw:totalUpdated>
      </xsl:if>
      
      <xsl:if test="wfs:totalDeleted != ''">
        <csw:totalDeleted>
          <xsl:value-of select="normalize-space( wfs:totalDeleted )"/>
        </csw:totalDeleted>
      </xsl:if>
      
    </csw:TransactionSummary>
  </xsl:template>
   
</xsl:stylesheet>

