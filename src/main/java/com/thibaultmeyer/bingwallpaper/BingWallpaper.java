package com.thibaultmeyer.bingwallpaper;

import com.thibaultmeyer.bingwallpaper.utils.OperatingSystem;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

        if (!BingWallpaperService.canRunOnThisSystem()) {
            System.err.println("Can't run on this operating system");
            System.exit(1);
        }

        // TODO: Parse args to get target, width and height
        final String temporaryFolder = System.getProperty("java.io.tmpdir");
        final String targetFileName = temporaryFolder.endsWith("/")
            ? temporaryFolder + "file.jpg"
            : temporaryFolder + "/file.jpg";

        // Automatically detect wallpaper needed dimension
        final Dimension screenDimension = retrieveScreenDimension();

        // Run service (looking for new wallpaper each hour)
        System.out.printf("  > Dimension     : %d x %d%n", screenDimension.width, screenDimension.height);
        System.out.printf("  > Temporary file: %s%n", targetFileName);

        System.out.println("Ready!");
        scheduledExecutorService.scheduleWithFixedDelay(
            new BingWallpaperService(screenDimension, targetFileName),
            0,
            1,
            TimeUnit.HOURS);
    }

    /**
     * Retrieve the screen dimension.
     *
     * @return The screen dimension
     * @throws IOException If something goes wrong during the process
     */
    public static Dimension retrieveScreenDimension() throws IOException {
        if (OperatingSystem.IS_MAC) {
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
