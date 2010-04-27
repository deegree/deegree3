<?xml version="1.0" encoding="UTF-8"?>
<FeatureTypeStyle xmlns="http://www.opengis.net/se" xmlns:ogc="http://www.opengis.net/ogc" xmlns:sed="http://www.deegree.org/se"
  xmlns:deegreeogc="http://www.deegree.org/ogc" xmlns:xplan="http://www.deegree.org/xplanung/1/0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
  <Rule>
    <ogc:Filter>
      <ogc:Not>
        <ogc:PropertyIsEqualTo>
          <!-- property name of the feature store (column in postgis or shape) -->
          <ogc:PropertyName>COLOR</ogc:PropertyName>
          <ogc:Literal></ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </ogc:Not>
    </ogc:Filter>
    <PolygonSymbolizer>
      <Stroke>
        <SvgParameter name="stroke">#000000</SvgParameter>
      </Stroke>
      <Fill>
        <SvgParameter name="fill">
          <!-- fill with a color value found in given column (color should be SE-encoded e.g. #00ff00 -->
          <ogc:PropertyName>COLOR</ogc:PropertyName>
        </SvgParameter>
      </Fill>
    </PolygonSymbolizer>
  </Rule>

  <Rule>
    <ogc:Filter>
      <ogc:Not>
        <ogc:PropertyIsEqualTo>
          <ogc:PropertyName>TEXTURE</ogc:PropertyName>
          <ogc:Literal></ogc:Literal>
        </ogc:PropertyIsEqualTo>
      </ogc:Not>
    </ogc:Filter>
    <PolygonSymbolizer>
      <Stroke>
        <SvgParameter name="stroke">#000000</SvgParameter>
      </Stroke>
      <Fill>
        <!--
          Fill the geometry with the texture value found in given Column (Propertyname).
          The value in this this Onlineresource is dependent on the value in the column:
          1) the column only contains a word like 'water', then below will be expanded to file:/directory/to/the/texture/water.jpg
          2) The column contains a filename, 'water.jpg', then the <OnlineResource> below should be  file:/directory/to/the/texture/<ogc:PropertyName>TEXTURE</ogc:PropertyName>
          c) The column contains an absolute path: the <OnlineResource> below should be  <ogc:PropertyName>TEXTURE</ogc:PropertyName>
        -->
        <GraphicFill>
          <Graphic>
            <ExternalGraphic>
              <Format>jpg</Format>
              <OnlineResource>file:/directory/to/the/textures/<ogc:PropertyName>TEXTURE</ogc:PropertyName>.jpg</OnlineResource>
            </ExternalGraphic>
          </Graphic>
        </GraphicFill>
      </Fill>
    </PolygonSymbolizer>
  </Rule>
</FeatureTypeStyle>