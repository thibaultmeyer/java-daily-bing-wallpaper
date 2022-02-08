package com.thibaultmeyer.bingwallpaper;

import com.thibaultmeyer.bingwallpaper.utils.OperatingSystemUtils;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Get and use the wallpaper of the day from Bing.
 */
public final class BingWallpaper {

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * Main entry.
     *
     * @param args Entry arguments
     * @throws IOException If something goes wrong during the process
     */
    public static void main(final String[] args) throws IOException {
        System.out.println("Booting...");

        // Check if operating system is handled
        if (!BingWallpaperService.canRunOnThisSystem()) {
            System.err.println("Can't run on this operating system");
            System.exit(1);
        }

        // Load settings
        final Properties properties = loadOrCreateProperties();
        final Settings settings = loadSettings(properties);

        System.out.printf("  > Dimension      : %d x %d%n", settings.dimensionWidth, settings.dimensionHeight);
        System.out.printf("  > Target filename: %s%n", settings.targetFileName);
        if (settings.proxyType == null) {
            System.out.println("  > Proxy          : NO");
        } else {
            System.out.printf("  > Proxy          : %s %s:%d%n", settings.proxyType, settings.proxyHost, settings.proxyPort);
        }

        // Run service (looking for new wallpaper each hour)
        System.out.println("Ready!");
        scheduledExecutorService.scheduleWithFixedDelay(
            new BingWallpaperService(settings),
            0,
            1,
            TimeUnit.HOURS);
    }

    /**
     * Try to load Properties file or create a new one.
     *
     * @return The Properties
     * @throws IOException If something goes wrong during the process
     */
    private static Properties loadOrCreateProperties() throws IOException {
        final Path path = Paths.get(System.getProperty("user.home"), ".bingwallpaper", "settings.properties");
        final Properties properties = new Properties();

        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());

            properties.setProperty("dimensionWidth", "auto");
            properties.setProperty("dimensionHeight", "auto");
            properties.setProperty("targetFileName", "auto");
            properties.setProperty("proxyType", "none");
            properties.setProperty("proxyHost", "none");
            properties.setProperty("proxyPort", "none");

            final BufferedWriter bufferedWriter = Files.newBufferedWriter(path);
            properties.store(bufferedWriter, "BingWallpaper Settings");
            bufferedWriter.close();
        } else {
            final BufferedReader bufferedReader = Files.newBufferedReader(path);
            properties.load(bufferedReader);
            bufferedReader.close();
        }

        return properties;
    }

    /**
     * Load settings from Properties.
     *
     * @param properties The properties
     * @return Loaded settings
     * @throws IOException If something goes wrong during the process
     */
    private static Settings loadSettings(final Properties properties) throws IOException {
        // Use Properties to prepare Settings
        final int wallpaperDimensionWidth;
        final int wallpaperDimensionHeight;
        final String targetFileName;
        final Proxy.Type proxyType;
        final String proxyHost;
        final int proxyPort;

        // Wallpaper dimension
        if (properties.getProperty("dimensionWidth", "auto").toUpperCase(Locale.ENGLISH).equals("AUTO")
            || properties.getProperty("dimensionHeight", "auto").toUpperCase(Locale.ENGLISH).equals("AUTO")) {
            // Automatically detect wallpaper needed dimension
            final Dimension screenDimension = retrieveScreenDimension();
            wallpaperDimensionWidth = screenDimension.width;
            wallpaperDimensionHeight = screenDimension.height;
        } else {
            // Use value from Properties
            wallpaperDimensionWidth = Integer.parseInt(properties.getProperty("dimensionWidth"));
            wallpaperDimensionHeight = Integer.parseInt(properties.getProperty("dimensionHeight"));
        }

        // Target filename
        if (properties.getProperty("targetFileName", "auto").toUpperCase(Locale.ENGLISH).equals("AUTO")) {
            // Automatically detect "temp" folder
            final String temporaryFolder = System.getProperty("java.io.tmpdir");
            targetFileName = temporaryFolder.endsWith(File.separator)
                ? temporaryFolder + "file.jpg"
                : temporaryFolder + File.separatorChar + "file.jpg";
        } else {
            // Use value from Properties
            targetFileName = properties.getProperty("targetFileName");
        }

        if (properties.getProperty("proxyType", "none").toUpperCase(Locale.ENGLISH).equals("NONE")) {
            // No proxy
            proxyType = null;
            proxyHost = null;
            proxyPort = -1;
        } else {
            // Use value from Properties
            proxyType = Proxy.Type.valueOf(properties.getProperty("proxyType").toUpperCase(Locale.ENGLISH));
            proxyHost = properties.getProperty("proxyHost");
            proxyPort = Integer.parseInt(properties.getProperty("proxyPort"));
        }

        return new Settings(
            wallpaperDimensionWidth,
            wallpaperDimensionHeight,
            targetFileName,
            proxyType,
            proxyHost,
            proxyPort);
    }

    /**
     * Retrieve the screen dimension.
     *
     * @return The screen dimension
     * @throws IOException If something goes wrong during the process
     */
    public static Dimension retrieveScreenDimension() throws IOException {
        if (OperatingSystemUtils.IS_MAC) {
            final Runtime runtime = Runtime.getRuntime();
            final Process process = runtime.exec("system_profiler SPDisplaysDataType");

            final byte[] buffer = new byte[512];
            if (process.getInputStream().read(buffer) > 0) {
                final String output = new String(buffer, StandardCharsets.UTF_8);
                final String[] outputExploded = output.split("\n");

                for (final String line : outputExploded) {
                    if (line.contains("Resolution")) {
                        final String dimensionAsString = line.split(":")[1].trim();
                        final String[] dimensionExploded = dimensionAsString.split(" ");

                        return new Dimension(
                            Integer.parseInt(dimensionExploded[0]),
                            Integer.parseInt(dimensionExploded[2]));
                    }
                }
            }
        }

        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}
