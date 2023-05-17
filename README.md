# Bing Wallpaper

Get and use the wallpaper of the day from Bing.
*****

## Build & install from sources
To compile and install this project, you must ensure that Java 9 and Maven 3 are being correctly installed.

```bash
#> mvn package
```


## Configuration
If needed, you use a custom configuration

**~/.bingwallpaper/settings.properties**
```
# Proxy (DIRECT, HTTP, SOCKS)
proxyType=none
proxyHost=none
proxyPort=none
# Wallpaper dimension (ie: 1920 x 1080)
dimensionWidth=auto
dimensionHeight=auto
# Where temporary file will be stored
targetFileName=auto
```


## Run

```bash
#> java -jar bing-wallpaper-<version>-jar-with-dependencies.jar [--single]]
```

If the flag `--single` is used, application will automatically exit after changing the wallpaper.
