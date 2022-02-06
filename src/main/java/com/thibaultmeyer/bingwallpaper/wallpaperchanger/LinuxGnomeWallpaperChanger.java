package com.thibaultmeyer.bingwallpaper.wallpaperchanger;

import com.thibaultmeyer.bingwallpaper.utils.OperatingSystemUtils;

import java.io.IOException;
import java.util.Locale;

/**
 * Linux (GNOME) implementation of {@code WallpaperChanger}.
 */
public class LinuxGnomeWallpaperChanger implements WallpaperChanger {

    @Override
    public boolean canRunOnThisSystem() {
        if (OperatingSystemUtils.IS_UNIX || OperatingSystemUtils.IS_SOLARIS) {
            String desktop = System.getenv("XDG_CURRENT_DESKTOP");
            if (desktop == null || desktop.isEmpty()) {
                desktop = System.getenv("DESKTOP_SESSION");
            }
            desktop = desktop == null
                ? ""
                : desktop.toUpperCase(Locale.ENGLISH);

            return desktop.contains("GNOME") || desktop.contains("UNITY");
        }

        return false;
    }

    @Override
    public boolean changeWallpaper(final String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            try {
                final String[] args = {
                    "gsettings",
                    "set",
                    "org.gnome.desktop.background",
                    "picture-uri",
                    fileName};

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
