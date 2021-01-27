<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:app="http://deegree.org/app">

  <xsl:output method="html" indent="yes"/>

  <xsl:template match="/">
    <html lang="de">
      <head/>
      <body>
        <h1>Ergebnis GFI</h1>
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
      <xsl:text> </xsl:text>
      <xsl:value-of select="('Identifier:', app:identifier, 'Name:', app:name)" separator=" "/>
      <xsl:text> Props: </xsl:text>
      <xsl:value-of select="app:prop1" separator=", "/>
    </li>
  </xsl:template>
</xsl:stylesheet>