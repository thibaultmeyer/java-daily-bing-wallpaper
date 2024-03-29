package com.thibaultmeyer.bingwallpaper.wallpaperchanger;

import com.thibaultmeyer.bingwallpaper.utils.OperatingSystemUtils;

import java.io.IOException;

/**
 * MacOS (Apple) implementation of {@code WallpaperChanger}.
 */
public final class MacOsWallpaperChanger implements WallpaperChanger {

    @Override
    public boolean canRunOnThisSystem() {

        return OperatingSystemUtils.IS_MAC;
    }

    @Override
    public boolean changeWallpaper(final String fileName) {

        if (fileName != null && !fileName.isEmpty()) {
            try {
                final String[] args = {
                    "osascript",
                    "-e",
                    "tell application \"Finder\" to set desktop picture to POSIX file \"" + fileName + "\""};

                final Runtime runtime = Runtime.getRuntime();
                final Process process = runtime.exec(args);

                return process.isAlive() || process.exitValue() == 0;
            } catch (final IOException ex) {
                ex.printStackTrace();
            }
        }

        return false;
    }
}
