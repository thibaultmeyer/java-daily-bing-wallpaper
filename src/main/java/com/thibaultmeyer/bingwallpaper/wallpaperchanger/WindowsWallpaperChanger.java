package com.thibaultmeyer.bingwallpaper.wallpaperchanger;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;
import com.thibaultmeyer.bingwallpaper.utils.OperatingSystem;

import java.util.HashMap;

/**
 * Windows implementation of {@code WallpaperChanger}.
 */
public class WindowsWallpaperChanger implements WallpaperChanger {

    @Override
    public boolean checkRequirement() {
        return OperatingSystem.IS_WINDOWS;
    }

    @Override
    public boolean changeWallpaper(final String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            return User32.INSTANCE.SystemParametersInfo(
                new WinDef.UINT(User32.SPI_SETDESKWALLPAPER),
                new WinDef.UINT(0),
                fileName,
                new WinDef.UINT(User32.SPIF_UPDATEINIFILE | User32.SPIF_SENDWININICHANGE));
        }

        return false;
    }

    /**
     * JNA User32 implementation.
     */
    public interface User32 extends StdCallLibrary {

        int SPI_SETDESKWALLPAPER = 20;
        int SPIF_UPDATEINIFILE = 0x01;
        int SPIF_SENDWININICHANGE = 0x02;

        User32 INSTANCE = Native.load("user32", User32.class, new HashMap<String, Object>() {
            {
                put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
                put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
            }
        });

        /**
         * Retrieves or sets the value of one of the system-wide parameters.
         * This function can also update the user profile while setting a parameter
         * <br/>
         * <b>Read more:</b> https://docs.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-systemparametersinfoa
         *
         * @param uiAction [IN] The system-wide parameter to be retrieved or set
         * @param uiParam  [IN] A parameter whose usage and format depends on the system parameter being queried or set
         * @param pvParam  [IN, OUT] A parameter whose usage and format depends on the system parameter being queried or set
         * @param fWinIni  [IN] If a system parameter is being set, specifies whether the user profile is to be updated
         * @return If the function succeeds, the return value is {@code true}, otherwise, {@code false}
         */
        boolean SystemParametersInfo(final WinDef.UINT uiAction,
                                     final WinDef.UINT uiParam,
                                     final String pvParam,
                                     final WinDef.UINT fWinIni);
    }
}
