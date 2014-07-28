package org.deegree.commons.ows.metadata.layer;

public class LogoUrl extends UrlWithFormat {

    private final int width;

    private final int height;

    public LogoUrl( final String url, final String format, final int width, final int height ) {
        super( url, format );
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
