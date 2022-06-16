<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/TR/REC-html40"
  xmlns:ogc="http://www.opengis.net/ogc" version="1.0">

  <xsl:output method="html" />

  <xsl:template match="ogc:ServiceExceptionReport">
    <html>
      <body>
        <h5>ServiceExceptionReport</h5>
        <table>
          <tr>
            <td>Code</td>
            <td>
              <xsl:value-of select="ogc:ServiceException/@code" />
            </td>
          </tr>
          <tr>
            <td>Fehler</td>
            <td>
              <xsl:value-of select="ogc:ServiceException" />
            </td>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>