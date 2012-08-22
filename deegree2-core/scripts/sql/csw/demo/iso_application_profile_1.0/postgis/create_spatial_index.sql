CREATE INDEX spx_boundingpolygon ON ex_boundingpolygon USING GIST ( geom GIST_GEOMETRY_OPS );
CREATE INDEX spx_boundingbox ON ex_geogrbbox USING GIST ( geom GIST_GEOMETRY_OPS );
CREATE INDEX spx_CQP_BBOX ON CQP_BBOX USING GIST ( geom GIST_GEOMETRY_OPS );
