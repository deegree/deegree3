<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/TR/REC-html40"
  xmlns:wms="http://www.opengis.net/wms" version="1.0">

  <xsl:output method="html" />

  <xsl:template match="wms:WMS_Capabilities">
    <html>
      <body>
        <h5>Service Metadata</h5>
        <table>
          <tr>
            <td>Name</td>
            <td>
              <xsl:value-of select="wms:Service/wms:Name" />
            </td>
          </tr>
          <tr>
            <td>Title</td>
            <td>
              <xsl:value-of select="wms:Service/wms:Title" />
            </td>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>