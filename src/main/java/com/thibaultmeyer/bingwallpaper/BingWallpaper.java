package com.thibaultmeyer.bingwallpaper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.platform.win32.WinDef;
import com.thibaultmeyer.bingwallpaper.utils.OperatingSystem;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.WindowsWallpaperChanger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BingWallpaper {

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * Main entry.
     *
     * @param args Entry arguments
     * @throws IOException If something goes wrong during the process
     */
    public static void main(final String[] args) throws IOException {
        // TODO: Parse args to get target, width and height
        final String targetFileName = "C:\\tmp\\file.jpg";

        final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        final int screenWidth = gd.getDisplayMode().getWidth();
        final int screenHeight = gd.getDisplayMode().getHeight();

        final URL url = retrieveDailyWallpaperUrl(screenWidth, screenHeight);
        if (url != null) {
            if (saveToLocal(url, targetFileName)) {
                new WindowsWallpaperChanger().changeWallpaper(targetFileName);
            }
        }

        scheduledExecutorService.schedule(new BingWallpaperService(), 1, TimeUnit.HOURS);
    }

    /**
     * Retrieve daily wallpaper URL from Bing API.
     *
     * @param width  The requested image "width" dimension
     * @param height The requested image "height" dimension
     * @return The wallpaper image URL
     * @throws IOException If something goes wrong during the process
     */
    private static URL retrieveDailyWallpaperUrl(final int width, final int height) throws IOException {
        final long currentTimeStamp = System.currentTimeMillis() / 1000;

        final URL bingApiUrl = new URL(String.format(Constant.BING_API_URL, currentTimeStamp, width, height));
        final HttpURLConnection httpConnection = (HttpURLConnection) bingApiUrl.openConnection();
        httpConnection.setRequestProperty("User-Agent", Constant.USER_AGENT_EDGE);

        if (httpConnection.getResponseCode() == 200) {
            final InputStreamReader inputStreamReader = new InputStreamReader(httpConnection.getInputStream());

            final ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode jsonNode = objectMapper.readTree(inputStreamReader);
            inputStreamReader.close();
            httpConnection.disconnect();

            final JsonNode jsonNodeImages = jsonNode.get("images");
            if (jsonNodeImages.has(0)) {
                return new URL(Constant.BING_URL + jsonNodeImages.get(0).get("url").asText());
            }
        } else {
            httpConnection.disconnect();
        }

        return null;
    }

    /**
     * Save content from a URL into a local file.
     *
     * @param urlToSave      URL of the content to retrieve
     * @param targetFileName Target full file name (ie: /tmp/file.jpg)
     * @return {@code true} in case of success, otherwise, {@code false}
     * @throws IOException If something goes wrong during the process
     */
    private static boolean saveToLocal(final URL urlToSave, final String targetFileName) throws IOException {
        final HttpURLConnection httpConnection = (HttpURLConnection) urlToSave.openConnection();
        httpConnection.setRequestProperty("User-Agent", Constant.USER_AGENT_EDGE);

        if (httpConnection.getResponseCode() == 200) {
            final InputStream inputStream = httpConnection.getInputStream();

            Files.copy(inputStream, new File(targetFileName).toPath(), StandardCopyOption.REPLACE_EXISTING);

            inputStream.close();
            httpConnection.disconnect();
            return true;
        } else {
            httpConnection.disconnect();
        }

        return false;
    }
}
