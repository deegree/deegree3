CREATE INDEX idx_$TABLE$ ON $TABLE$(level);
CREATE INDEX idx_$TABLE$_pyr ON $TABLE$_pyr(minscale,maxscale);
CREATE INDEX spx_bbox ON $TABLE$ USING GIST ( bbox GIST_GEOMETRY_OPS );