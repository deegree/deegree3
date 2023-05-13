<?xml version='1.0'?>
<stylesheet xmlns='http://www.w3.org/1999/XSL/Transform' version='1.0' xmlns:wms='http://www.deegree.org/services/wms'
  xmlns:t='http://www.deegree.org/themes/standard' xmlns:d='http://www.deegree.org/metadata/description' xmlns:g='http://www.deegree.org/metadata/spatial'>

  <strip-space elements='*' />

  <template match="text()|@*|*" />
  <template match="text()|@*|*" mode="fstore" />
  <template match="text()|@*|*" mode="layer" />

  <template match='/*'>
    <apply-templates />
  </template>

  <template match='wms:ServiceConfiguration'>
    <t:Themes>
      <apply-templates select='.//wms:FeatureStoreId' mode='fstore' />
      <apply-templates mode='layer' />
    </t:Themes>
  </template>

  <template match='wms:UnrequestableLayer' mode='layer'>
    <t:Theme>
      <d:Title><value-of select='wms:Title' /></d:Title>
      <apply-templates mode='layer' />
    </t:Theme>
  </template>

  <template match='wms:RequestableLayer' mode='layer'>
    <t:Theme>
      <t:Identifier>
        <value-of select='wms:Name' />
      </t:Identifier>
      <d:Title><value-of select='wms:Title' /></d:Title>
      <t:Layer>
        <value-of select='wms:Name' />
      </t:Layer>
      <apply-templates mode='layer' />
    </t:Theme>
  </template>

  <template match='wms:LogicalLayer' mode='layer'>
    <t:Layer>LogicalLayer</t:Layer>
  </template>

  <template match='wms:CRS'>
    <g:CRS><value-of select='.' /></g:CRS>
  </template>

  <template match='wms:FeatureStoreId' mode='fstore'>
    <t:LayerStoreId>
      <value-of select='.' />
    </t:LayerStoreId>
  </template>

</stylesheet>
