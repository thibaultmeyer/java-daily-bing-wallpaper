package com.thibaultmeyer.bingwallpaper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.LinuxGnomeWallpaperChanger;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.MacOsWallpaperChanger;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.WallpaperChanger;
import com.thibaultmeyer.bingwallpaper.wallpaperchanger.WindowsWallpaperChanger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    private final Settings settings;

    private URL latestWallpaperUrl;

    /**
     * Build a new instance.
     *
     * @param settings Current settings
     */
    public BingWallpaperService(final Settings settings) {

        this.settings = settings;
        this.latestWallpaperUrl = null;
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
            if (url != null && !Objects.equals(url, latestWallpaperUrl)) {
                if (saveToLocal(url)) {
                    latestWallpaperUrl = url;

                    final boolean result = WALLPAPER_CHANGER_LIST
                        .stream()
                        .filter(WallpaperChanger::canRunOnThisSystem)
                        .findFirst()
                        .map(wp -> wp.changeWallpaper(settings.targetFileName))
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
            settings.dimensionWidth,
            settings.dimensionHeight));

        final HttpURLConnection httpConnection = openHttpConnection(bingApiUrl);

        if (httpConnection.getResponseCode() == 200) {
            final InputStreamReader inputStreamReader = new InputStreamReader(httpConnection.getInputStream());

            final JsonObject jsonObject = JsonParser.parseReader(inputStreamReader).getAsJsonObject();
            inputStreamReader.close();
            httpConnection.disconnect();

            final JsonArray jsonArrayImages = jsonObject.get("images").getAsJsonArray();
            if (jsonArrayImages.size() > 0) {
                return new URL(BING_URL + jsonArrayImages.get(0).getAsJsonObject().get("url").getAsString());
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

        final HttpURLConnection httpConnection = openHttpConnection(urlToSave);
        if (httpConnection.getResponseCode() == 200) {
            final InputStream inputStream = httpConnection.getInputStream();

            Files.copy(
                inputStream,
                new File(settings.targetFileName).toPath(),
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
     * Open an HTTP connection.
     *
     * @param url URL to use
     * @return Opened HTTP connection
     * @throws IOException If something goes wrong during the process
     */
    private HttpURLConnection openHttpConnection(final URL url) throws IOException {

        final Proxy proxy = configureProxy();
        final HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(proxy);

        httpConnection.setRequestProperty("User-Agent", USER_AGENT_EDGE);
        httpConnection.setConnectTimeout(15000);
        httpConnection.setReadTimeout(15000);
        httpConnection.connect();

        return httpConnection;
    }

    /**
     * Configure Proxy.
     *
     * @return Configured Proxy
     */
    private Proxy configureProxy() {

        if (settings.proxyType == null) {
            return Proxy.NO_PROXY;
        }

        return new Proxy(settings.proxyType, new InetSocketAddress(settings.proxyHost, settings.proxyPort));
    }
}
