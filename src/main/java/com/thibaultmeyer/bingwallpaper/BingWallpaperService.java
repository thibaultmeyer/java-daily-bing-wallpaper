package com.thibaultmeyer.bingwallpaper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.LinuxGnomeWallpaperChanger;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.MacOsWallpaperChanger;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.WallpaperChanger;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.WindowsWallpaperChanger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

/**
 * This service takes care of getting the wallpaper of the day and using it.
 */
public final class BingWallpaperService implements Runnable {

    private static final String BING_URL = "https://www.bing.com";
    private static final String BING_API_URL = BING_URL + "/HPImageArchive.aspx?format=js&idx=0&n=1&nc=%d&uhd=1&uhdwidth=%d&uhdheight=%d";
    private static final String USER_AGENT_EDGE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.80 Safari/537.36 Edg/97.0.1072.69";

    private static final List<WallpaperChanger> WALLPAPER_CHANGER_LIST = Arrays.asList(
        new LinuxGnomeWallpaperChanger(),
        new MacOsWallpaperChanger(),
        new WindowsWallpaperChanger());

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

    /**
     * Determine if  Wallpaper Changer can work on this current operating system.
     *
     * @return {@code true} if it can work, otherwise, {@code false}
     */
    public static boolean canRunOnThisSystem() {
        return WALLPAPER_CHANGER_LIST
            .stream()
            .anyMatch(WallpaperChanger::canRunOnThisSystem);
    }

    @Override
    public void run() {
        try {
            final URL url = retrieveDailyWallpaperUrl();
            if (url != null) {
                if (saveToLocal(url)) {
                    final boolean result = WALLPAPER_CHANGER_LIST
                        .stream()
                        .filter(WallpaperChanger::canRunOnThisSystem)
                        .findFirst()
                        .map(wp -> wp.changeWallpaper(tmpFileName))
                        .orElse(false);
                    if (result) {
                        System.out.println("New wallpaper applied with success");
                    } else {
                        System.err.println("Can't apply new wallpaper");
                    }
                }
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }

        System.gc();
    }

    /**
     * Retrieve daily wallpaper URL from Bing API.
     *
     * @return The wallpaper image URL
     * @throws IOException If something goes wrong during the process
     */
    private URL retrieveDailyWallpaperUrl() throws IOException {
        final URL bingApiUrl = new URL(String.format(
            BING_API_URL,
            System.currentTimeMillis() / 1000,
            wallpaperDimension.width,
            wallpaperDimension.height));

        final HttpURLConnection httpConnection = createHttpConnection(bingApiUrl);

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
     * @param urlToSave URL of the content to retrieve
     * @return {@code true} in case of success, otherwise, {@code false}
     * @throws IOException If something goes wrong during the process
     */
    private boolean saveToLocal(final URL urlToSave) throws IOException {
        final HttpURLConnection httpConnection = createHttpConnection(urlToSave);

        if (httpConnection.getResponseCode() == 200) {
            final InputStream inputStream = httpConnection.getInputStream();

            Files.copy(
                inputStream,
                new File(tmpFileName).toPath(),
                StandardCopyOption.REPLACE_EXISTING);

            inputStream.close();
            httpConnection.disconnect();
            return true;
        } else {
            httpConnection.disconnect();
        }

        return false;
    }

    /**
     * Create an HTTP connection.
     *
     * @param url URL to use
     * @return Created HTTP connection
     * @throws IOException If something goes wrong during the process
     */
    private HttpURLConnection createHttpConnection(final URL url) throws IOException {
        final Proxy proxy = configureProxy();
        final HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(proxy);
        httpConnection.setRequestProperty("User-Agent", USER_AGENT_EDGE);
        httpConnection.setConnectTimeout(15000);
        httpConnection.setReadTimeout(15000);

        return httpConnection;
    }

    /**
     * Configure Proxy.
     *
     * @return Configured Proxy
     */
    private Proxy configureProxy() {
        //TODO : new Proxy(Proxy.Type.HTTP, new InetSocketAddress("ip", port));
        final Proxy proxy = Proxy.NO_PROXY;

        return proxy;
    }
}
