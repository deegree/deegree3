CREATE INDEX spx_wpvsdata ON wpvsdata USING GIST ( geometry GIST_GEOMETRY_OPS );
CREATE INDEX spx_polygon ON polygon USING GIST ( geometry GIST_GEOMETRY_OPS );
CREATE INDEX spx_feature_env ON feature USING GIST ( envelope GIST_GEOMETRY_OPS );
CREATE INDEX spx_feature_elod1tis ON feature USING GIST ( lod1tis GIST_GEOMETRY_OPS );
CREATE INDEX spx_feature_elod2tis ON feature USING GIST ( lod2tis GIST_GEOMETRY_OPS );
CREATE INDEX spx_feature_elod3tis ON feature USING GIST ( lod3tis GIST_GEOMETRY_OPS );
CREATE INDEX spx_feature_elod4tis ON feature USING GIST ( lod4tis GIST_GEOMETRY_OPS );

