<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:app="http://www.deegree.org/app"
  xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" version="1.0.0" xmlns:deegreeogc="http://www.deegree.org/ogc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <Name>ElevationContours</Name>
  <UserLayer>
    <Name>ElevationContours</Name>
    <LayerFeatureConstraints>
      <FeatureTypeConstraint>
        <FeatureTypeName>app:ElevationContours</FeatureTypeName>
      </FeatureTypeConstraint>
    </LayerFeatureConstraints>
    <UserStyle>
      <Name>ElevationContours</Name>
      <FeatureTypeStyle>
        <Rule>
          <Name>ELEV_4999</Name>
          <ogc:Filter>
            <ogc:PropertyIsLessThanOrEqualTo>
              <ogc:PropertyName>app:elev</ogc:PropertyName>
              <ogc:Literal>4999</ogc:Literal>
            </ogc:PropertyIsLessThanOrEqualTo>
          </ogc:Filter>
          <MinScaleDenominator>0</MinScaleDenominator>
          <MaxScaleDenominator>3.779527559017323E14</MaxScaleDenominator>
          <LineSymbolizer>
            <Geometry>
              <ogc:PropertyName>app:contourLine</ogc:PropertyName>
            </Geometry>
            <Stroke>
              <CssParameter name="stroke">#588c58</CssParameter>
              <CssParameter name="stroke-opacity">1.0</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
              <CssParameter name="stroke-dasharray">1</CssParameter>
            </Stroke>
          </LineSymbolizer>
        </Rule>
        <Rule>
          <Name>ELEV_5000</Name>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>app:elev</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>5000</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>7499</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <MinScaleDenominator>0</MinScaleDenominator>
          <MaxScaleDenominator>3.779527559017323E14</MaxScaleDenominator>
          <LineSymbolizer>
            <Geometry>
              <ogc:PropertyName>app:contourLine</ogc:PropertyName>
            </Geometry>
            <Stroke>
              <CssParameter name="stroke">#b26b00</CssParameter>
              <CssParameter name="stroke-opacity">1.0</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
              <CssParameter name="stroke-dasharray">1</CssParameter>
            </Stroke>
          </LineSymbolizer>
        </Rule>
        <Rule>
          <Name>ELEV_7500</Name>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>app:elev</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>7500</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>9999</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <MinScaleDenominator>0</MinScaleDenominator>
          <MaxScaleDenominator>3.779527559017323E14</MaxScaleDenominator>
          <LineSymbolizer>
            <Geometry>
              <ogc:PropertyName>app:contourLine</ogc:PropertyName>
            </Geometry>
            <Stroke>
              <CssParameter name="stroke">#8e8e00</CssParameter>
              <CssParameter name="stroke-opacity">1.0</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
              <CssParameter name="stroke-dasharray">1</CssParameter>
            </Stroke>
          </LineSymbolizer>
        </Rule>
        <Rule>
          <Name>ELEV_10000</Name>
          <ogc:Filter>
            <ogc:PropertyIsGreaterThanOrEqualTo>
              <ogc:PropertyName>app:elev</ogc:PropertyName>
              <ogc:Literal>10000</ogc:Literal>
            </ogc:PropertyIsGreaterThanOrEqualTo>
          </ogc:Filter>
          <MinScaleDenominator>0</MinScaleDenominator>
          <MaxScaleDenominator>3.779527559017323E14</MaxScaleDenominator>
          <LineSymbolizer>
            <Geometry>
              <ogc:PropertyName>app:contourLine</ogc:PropertyName>
            </Geometry>
            <Stroke>
              <CssParameter name="stroke">#853f10</CssParameter>
              <CssParameter name="stroke-opacity">1.0</CssParameter>
              <CssParameter name="stroke-width">1</CssParameter>
              <CssParameter name="stroke-dasharray">1</CssParameter>
            </Stroke>
          </LineSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </UserLayer>
</StyledLayerDescriptor>
