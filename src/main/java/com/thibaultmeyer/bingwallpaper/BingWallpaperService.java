package com.thibaultmeyer.bingwallpaper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.MacOsWallpaperChanger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class BingWallpaperService implements Runnable {

    private static final String BING_URL = "https://www.bing.com";
    private static final String BING_API_URL = BING_URL + "/HPImageArchive.aspx?format=js&idx=0&n=1&nc=%d&uhd=1&uhdwidth=%d&uhdheight=%d";
    private static final String USER_AGENT_EDGE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36 Edg/97.0.1072.69";

    private final Dimension wallpaperDimension;
    private final String tmpFileName;

    /**
     * Build a new instance.
     *
     * @param wallpaperDimension The needed wallpaper dimension
     * @param tmpFileName        Temporary file for downloaded Wallpaper
     */
    public BingWallpaperService(final Dimension wallpaperDimension, final String tmpFileName) {
        this.wallpaperDimension = wallpaperDimension;
        this.tmpFileName = tmpFileName;
    }

    @Override
    public void run() {
        try {
            final URL url = retrieveDailyWallpaperUrl(wallpaperDimension.width, wallpaperDimension.height);
            if (url != null) {
                if (saveToLocal(url, tmpFileName)) {
                    new MacOsWallpaperChanger().changeWallpaper(tmpFileName);
                }
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Retrieve daily wallpaper URL from Bing API.
     *
     * @param width  The requested image "width" dimension
     * @param height The requested image "height" dimension
     * @return The wallpaper image URL
     * @throws IOException If something goes wrong during the process
     */
    private URL retrieveDailyWallpaperUrl(final int width, final int height) throws IOException {
        final long currentTimeStamp = System.currentTimeMillis() / 1000;

        final URL bingApiUrl = new URL(String.format(BING_API_URL, currentTimeStamp, width, height));
        final HttpURLConnection httpConnection = (HttpURLConnection) bingApiUrl.openConnection();
        httpConnection.setRequestProperty("User-Agent", USER_AGENT_EDGE);

        if (httpConnection.getResponseCode() == 200) {
            final InputStreamReader inputStreamReader = new InputStreamReader(httpConnection.getInputStream());

            final ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode jsonNode = objectMapper.readTree(inputStreamReader);
            inputStreamReader.close();
            httpConnection.disconnect();

            final JsonNode jsonNodeImages = jsonNode.get("images");
            if (jsonNodeImages.has(0)) {
                return new URL(BING_URL + jsonNodeImages.get(0).get("url").asText());
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
    private boolean saveToLocal(final URL urlToSave, final String targetFileName) throws IOException {
        final HttpURLConnection httpConnection = (HttpURLConnection) urlToSave.openConnection();
        httpConnection.setRequestProperty("User-Agent", USER_AGENT_EDGE);

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
