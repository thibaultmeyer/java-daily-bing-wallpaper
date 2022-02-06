package com.thibaultmeyer.bingwallpaper.utils;

public final class OperatingSystem {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean IS_WINDOWS = OS.contains("win");
    public static boolean IS_MAC = OS.contains("mac");
    public static boolean IS_UNIX = OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    public static boolean IS_SOLARIS = OS.contains("sunos");
}
