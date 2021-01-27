<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:app="http://deegree.org/app">

  <xsl:output method="html" indent="yes"/>

  <xsl:template match="/">
    <html lang="de">
      <head/>
      <body>
        <div>
          <ul>
            <xsl:for-each select="//app:Instance">
              <xsl:apply-templates select="."/>
            </xsl:for-each>
          </ul>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="app:Instance">
    <li>
      <xsl:text>Identifier: </xsl:text>
      <xsl:value-of select="app:identifier"/>
      <xsl:text> Name: </xsl:text>
      <xsl:value-of select="app:name"/>
      <xsl:text> Props: </xsl:text>
      <xsl:for-each select="app:prop1">
        <xsl:value-of select="."/>
        <xsl:if test="position() != last()">, </xsl:if>
      </xsl:for-each>
    </li>
  </xsl:template>
</xsl:stylesheet>