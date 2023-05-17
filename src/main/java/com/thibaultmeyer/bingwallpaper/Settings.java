package com.thibaultmeyer.bingwallpaper;

import java.net.Proxy;

/**
 * Settings.
 */
public final class Settings {

    /**
     * Wallpaper dimension - Width (Pixel)
     */
    public final int dimensionWidth;

    /**
     * Wallpaper dimension - Height (Pixel)
     */
    public final int dimensionHeight;

    /**
     * Location where the wallpaper will be saved on local disk.
     */
    public final String targetFileName;

    /**
     * Type of the Proxy to use (ie: HTTP)
     */
    public final Proxy.Type proxyType;

    /**
     * Host of the Proxy to use (ie: 127.0.0.1)
     */
    public final String proxyHost;

    /**
     * Listen port of the Proxy to use (ie: 8080)
     */
    public final int proxyPort;

    /**
     * Build a new instance.
     *
     * @param dimensionWidth  Wallpaper dimension - Width (Pixel)
     * @param dimensionHeight Wallpaper dimension - Height (Pixel)
     * @param targetFileName  Location where the wallpaper will be saved on local disk
     * @param proxyType       Proxy Type (ie: HTTP)
     * @param proxyHost       Proxy Host (ie: 127.0.0.1)
     * @param proxyPort       Proxy Port (ie: 8080)
     */
    public Settings(final int dimensionWidth,
                    final int dimensionHeight,
                    final String targetFileName,
                    final Proxy.Type proxyType,
                    final String proxyHost,
                    final int proxyPort) {

        this.dimensionWidth = dimensionWidth;
        this.dimensionHeight = dimensionHeight;
        this.targetFileName = targetFileName;
        this.proxyType = proxyType;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
}
