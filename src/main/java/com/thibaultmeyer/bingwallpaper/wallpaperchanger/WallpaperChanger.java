package com.thibaultmeyer.bingwallpaper.wallpaperchanger;

/**
 * Wallpaper changer.
 */
public interface WallpaperChanger {

    /**
     * Determine if this wallpaper changer can work.
     *
     * @return {@code true} if it can work, otherwise, {@code false}
     */
    boolean checkRequirement();

    /**
     * Change the Wallpaper.
     *
     * @param fileName New file to use as wallpaper
     * @return {@code true} in case of success, otherwise, {@code false}
     */
    boolean changeWallpaper(final String fileName);
}
