.. _anchor-configuration-renderstyles:

==========
Map styles
==========

Style resources are used to obtain information on how to render geo objects (mostly features, but also coverages) into maps. The most common use case is to reference them from a layer configuration, in order to describe how the layer is to be rendered. This chapter assumes the reader is familiar with basic SLD/SE terms. The style configurations do not depend on any other resource.

In contrast to other deegree configurations the style configurations do not have a custom format. You can use standard SLD or SE documents (1.0.0 and 1.1.0 are supported), with a couple of deegree specific extensions, which are described below. Please refer to the SLD_ and SE_ specifications for reference. Additionally this page contains specific examples below.

.. _SLD: http://www.opengeospatial.org/standards/sld
.. _SE: http://www.opengeospatial.org/standards/se

In deegree terms, each SLD or SE file will create a *style store*. In case of an SE file (usually beginning at the FeatureTypeStyle or CoverageStyle level) the style store only contains one style, in case of an SLD file the style store may contain multiple styles, each identified by the layer (only NamedLayers make sense here) and the name of the style (only UserStyles make sense) when referenced later.

.. figure:: images/workspace-overview-style.png
   :figwidth: 80%
   :width: 80%
   :target: _images/workspace-overview-style.png

   Style resources define how geo objects are rendered

.. tip::
  When defining styles, take note of the log file. Upon startup the log will warn you about potential problems or errors during parsing, and upon rendering warnings will be emitted when rendering is unsuccessful eg. because you had a typo in a geometry property name. When you're seeing an empty map when expecting a fancy one, check the log before reporting a bug. deegree will tolerate a lot of syntactical errors in your style files, but you're more likely to get a good result when your files validate and you have no warnings in the log.

^^^^^^^^^^^^^^^^
Styling Examples
^^^^^^^^^^^^^^^^

________________
Point Symbolizer
________________

.. code-block:: xml

<FeatureTypeStyle
xmlns="http://www.opengis.net/se"
xmlns:app="http://www.deegree.org/app"
xmlns:ogc="http://www.opengis.net/ogc"
xmlns:sed="http://www.deegree.org/se"
xmlns:deegreeogc="http://www.deegree.org/ogc"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
 <Name>Weatherstations</Name>
 <Rule>
    <Name>Weatherstations</Name>
    <Description>
      <Title>Weatherstations in Utah</Title>
    </Description>
      <PointSymbolizer>
        <Graphic>
          <Mark>
            <WellKnownName>square</WellKnownName>
            <Fill>
              <SvgParameter name="fill">#FF0000</SvgParameter>
            </Fill>
            <Stroke>
              <SvgParameter name="stroke">#000000</SvgParameter>
              <SvgParameter name="stroke-width">1</SvgParameter>
            </Stroke>
          </Mark>
          <Size>13</Size>
        </Graphic>
      </PointSymbolizer>
  </Rule> 
</FeatureTypeStyle>

________________
Line Symbolizer
________________

.. code-block:: xml

<FeatureTypeStyle
xmlns="http://www.opengis.net/se"
xmlns:app="http://www.deegree.org/app"
xmlns:ogc="http://www.opengis.net/ogc"
xmlns:sed="http://www.deegree.org/se"
xmlns:deegreeogc="http://www.deegree.org/ogc"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
  <Name>Railroads</Name>
  <Rule>
    <Name>Railroads</Name>
    <LineSymbolizer>
      <Stroke>
        <SvgParameter name="stroke">#000000</SvgParameter>
        <SvgParameter name="stroke-opacity">1.0</SvgParameter>
        <SvgParameter name="stroke-width">0.3</SvgParameter>
      </Stroke>
      <PerpendicularOffset>1.5</PerpendicularOffset>
    </LineSymbolizer>
    <LineSymbolizer>
      <Stroke>
        <SvgParameter name="stroke">#ffffff</SvgParameter>
        <SvgParameter name="stroke-opacity">1.0</SvgParameter>
        <SvgParameter name="stroke-width">1.5</SvgParameter>
      </Stroke>
    </LineSymbolizer>
    <LineSymbolizer>
      <Stroke>
        <SvgParameter name="stroke">#000000</SvgParameter>
        <SvgParameter name="stroke-opacity">1.0</SvgParameter>
        <SvgParameter name="stroke-width">0.3</SvgParameter>
      </Stroke>
      <PerpendicularOffset>-1.5</PerpendicularOffset>
    </LineSymbolizer>
  </Rule>    
</FeatureTypeStyle>

__________________
Polygon Symbolizer
__________________

.. code-block:: xml

<FeatureTypeStyle
 xmlns="http://www.opengis.net/se"
 xmlns:app="http://www.deegree.org/app"
 xmlns:ogc="http://www.opengis.net/ogc"
 xmlns:sed="http://www.deegree.org/se"
 xmlns:deegreeogc="http://www.deegree.org/ogc"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
  <Name>LandslideAreas</Name>
  <Rule>
    <Name>LandslideAreas</Name>
    <Description>
      <Title>LandslideAreas</Title>
    </Description>
    <PolygonSymbolizer>
      <Fill>
        <SvgParameter name="fill">#cc3300</SvgParameter>
        <SvgParameter name="fill-opacity">0.3</SvgParameter>
      </Fill>
      <Stroke>
        <SvgParameter name="stroke">#000000</SvgParameter>
        <SvgParameter name="stroke-opacity">1.0</SvgParameter>
        <SvgParameter name="stroke-width">1</SvgParameter>
      </Stroke>
    </PolygonSymbolizer>
  </Rule>
</FeatureTypeStyle>

_______________
Text Symbolizer
_______________

.. code-block:: xml

<FeatureTypeStyle
 xmlns="http://www.opengis.net/se"
 xmlns:app="http://www.deegree.org/app"
 xmlns:ogc="http://www.opengis.net/ogc"
 xmlns:sed="http://www.deegree.org/se"
 xmlns:deegreeogc="http://www.deegree.org/ogc"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://www.opengis.net/se http://schemas.opengis.net/se/1.1.0/FeatureStyle.xsd http://www.deegree.org/se http://schemas.deegree.org/se/1.1.0/Symbolizer-deegree.xsd">
  <Name>Municipalities</Name>
  <Rule>
    <Name>Municipalities</Name>
    <Description>
      <Title>Municipalities</Title>
    </Description>
    <MaxScaleDenominator>200000</MaxScaleDenominator>
    <TextSymbolizer>
      <Label>
        <ogc:PropertyName>app:NAME</ogc:PropertyName>
      </Label>
      <Font>
        <SvgParameter name="font-family">Arial</SvgParameter>
        <SvgParameter name="font-family">Sans-Serif</SvgParameter>
        <SvgParameter name="font-weight">bold</SvgParameter>
        <SvgParameter name="font-size">12</SvgParameter>
      </Font>
      <Halo>
        <Radius>1</Radius>
        <Fill>
          <SvgParameter name="fill-opacity">1.0</SvgParameter>
          <SvgParameter name="fill">#fefdC3</SvgParameter>
        </Fill>
      </Halo>
      <Fill>
        <SvgParameter name="fill">#000000</SvgParameter>
      </Fill>
    </TextSymbolizer>
  </Rule>
</FeatureTypeStyle>

^^^^^^^^^^^^^^^^^^^^^
SLD/SE clarifications
^^^^^^^^^^^^^^^^^^^^^

This chapter is meant to clarify deegree's behaviour when using standard SLD/SE constructs.

________________________________________
Perpendicular offset/polygon orientation
________________________________________

For polygon rendering, the orientation is always fixed, and will be corrected if a feature store yields inconsistent geometries. The outer ring is always oriented counter clockwise, inner rings are oriented clockwise.

A positive perpendicular offset setting results in an offset movement in the outer direction, a negative setting moves the offset into the interior. For inner rings the effect is flipped (a positive setting moves into the interior of the inner ring, a negative setting moves into the exterior of the inner ring).

^^^^^^^^^^^^^^^^^^^^^^^^^^^
deegree specific extensions
^^^^^^^^^^^^^^^^^^^^^^^^^^^

deegree supports some extensions of SLD/SE and filter encoding to enable more sophisticated styling. The following sections describe the respective extensions for SLD/SE and filter encoding.
For several specific extensions, there is a deegree SE XML Schema_.

.. _Schema: http://schemas.deegree.org/se

_________________
SLD/SE extensions
_________________


---------------------------------
Use of TTF files as Mark symbols
---------------------------------

You can use TrueType font files to use custom vector symbols in a ``Mark`` element:

.. code-block:: xml

  <Mark>
    <OnlineResource xlink:href="filepath/yousans.ttf" />
    <Format>ttf</Format>
    <MarkIndex>99</MarkIndex>
    <Fill>
      <SvgParameter name="fill">#000000</SvgParameter>
      ...
    </Fill>
    <Stroke>
      <SvgParameter name="stroke-opacity">0</SvgParameter>
      ...
    </Stroke>
  </Mark>

To find out what index you need to access, have a look at this post_ on the mailinglist which explains it very well.

.. _post: http://osgeo-org.1560.n6.nabble.com/SE-Styling-MarkIndex-glyph-index-tt5022210.html#a5026571

-------------------
Label AutoPlacement
-------------------

deegree has an option for SE LabelPlacement to automatically place labels on the map.
To enable AutoPlacement, you can simply set the "auto" attribute to "true".

.. code-block:: xml

    <LabelPlacement>
      <PointPlacement auto="true">
        <Displacement>
          <DisplacementX>0</DisplacementX>
          <DisplacementY>0</DisplacementY>
        </Displacement>
        <Rotation>0</Rotation>
      </PointPlacement>
    </LabelPlacement> 

.. tip::
  AutoPlacement for labels only works for PointPlacement. AutoPlacement for LinePlacement is not implemented yet.

__________________________
Filter encoding extensions
__________________________

There are a couple of deegree specific functions which can be expressed as standard OGC function expressions in SLD/SE.

Most of the functions are currently described in the FilterFunctions_, but new ones will be described here (the descriptions from the wiki will be ported soon TODO TODO).

.. _FilterFunctions: http://wiki.deegree.org/deegreeWiki/deegree3/FilterFunctions

---------------
GetCurrentScale
---------------

The GetCurrentScale function takes no arguments, and dynamically provides you with the value of the current map scale denominator (only to be used in GetMap requests!). The scale denominator will be adapted to any custom pixel size you may be using in your request, and is the same scale denominator the WMS uses internally for filtering out layers/style rules.

Let's have a look at an example:

.. code-block:: xml

  ...
  <sld:SvgParameter name="stroke-width">
    <ogc:Function name="idiv">
      <ogc:Literal>500000</ogc:Literal>
      <ogc:Function name="GetCurrentScale" />
    </ogc:Function>
  </sld:SvgParameter>
  ...

In this case, the stroke width will be one pixel for scales around 500000, and will get bigger as you zoom in (and the scale denominator gets smaller). Scale denominators above 500000 will yield invisible strokes with a width of zero.
