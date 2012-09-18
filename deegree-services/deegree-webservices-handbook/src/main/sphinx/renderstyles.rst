.. _anchor-configuration-renderstyles:

====================================
Style configuration
====================================

Style resources are used to obtain information on how to render geo objects (mostly features, but also coverages) into maps. The most common use case is to reference them from a layer configuration, in order to describe how the layer is to be rendered. This chapter assumes the reader is familiar with basic SLD/SE terms. The style configurations do not depend on any other resource.

In contrast to other deegree configurations the style configurations do not have a custom format. You can use standard SLD or SE documents (1.0.0 and 1.1.0 are supported), with a couple of deegree specific extensions, which are described below. Please refer to the StylesConfiguration_ wiki page for examples, and to the SLD_ and SE_ specifications for reference.

.. _StylesConfiguration: http://wiki.deegree.org/deegreeWiki/deegree3/WorkspaceConfiguration/StylesConfiguration
.. _SLD: http://www.opengeospatial.org/standards/sld
.. _SE: http://www.opengeospatial.org/standards/se

In deegree terms, each SLD or SE file will create a *style store*. In case of an SE file (usually beginning at the FeatureTypeStyle or CoverageStyle level) the style store only contains one style, in case of an SLD file the style store may contain multiple styles, each identified by the layer (only NamedLayers make sense here) and the name of the style (only UserStyles make sense) when referenced later.

.. tip::
  When defining styles, take note of the log file. Upon startup the log will warn you about potential problems or errors during parsing, and upon rendering warnings will be emitted when rendering is unsuccessful eg. because you had a typo in a geometry property name. When you're seeing an empty map when expecting a fancy one, check the log before reporting a bug. deegree will tolerate a lot of syntactical errors in your style files, but you're more likely to get a good result when your files validate and you have no warnings in the log.

^^^^^^^^^^^^^^^^^^^^^^^^^^^
deegree specific extensions
^^^^^^^^^^^^^^^^^^^^^^^^^^^

deegree supports some extensions of SLD/SE and filter encoding to enable more sophisticated styling. The following sections describe the respective extensions for SLD/SE and filter encoding.

_________________
SLD/SE extensions
_________________

TBD

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

