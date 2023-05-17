package com.thibaultmeyer.bingwallpaper.utils;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * Utility to force single instance.
 */
public final class SingleInstanceUtils {

    private static final String LOCK_FILE_WINDOWS = "\\\\.\\pipe\\bing-wallpaper-java";
    private static final String LOCK_FILE_UNIX_LIKE = "/tmp/bing-wallpaper-java.lock";

    /**
     * Check existing instance.
     *
     * @return {@code true} if other instance exists, otherwise, {@code false}
     */
    public static boolean hasExistingInstance() {

        if (OperatingSystemUtils.IS_WINDOWS) {
            final HANDLE hNamedPipe = Kernel32.INSTANCE.CreateNamedPipe(
                LOCK_FILE_WINDOWS,
                WinBase.PIPE_ACCESS_DUPLEX,                                                 // dwOpenMode
                WinBase.PIPE_TYPE_BYTE | WinBase.PIPE_READMODE_BYTE | WinBase.PIPE_WAIT,    // dwPipeMode
                1,                 // nMaxInstances,
                Byte.MAX_VALUE,    // nOutBufferSize,
                Byte.MAX_VALUE,    // nInBufferSize,
                1000,              // nDefaultTimeOut,
                null);             // lpSecurityAttributes

            // Handle will be invalid if named pipe already exists
            return WinBase.INVALID_HANDLE_VALUE.equals(hNamedPipe);
        } else {
            try {
                final File file = new File(LOCK_FILE_UNIX_LIKE);
                final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                final FileLock fileLock = randomAccessFile.getChannel().tryLock();

                if (fileLock != null) {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            if (!file.delete()) {
                                System.err.println("Unable to remove lock file: " + LOCK_FILE_UNIX_LIKE);
                            }
                        } catch (final Exception ex) {
                            System.err.println("Unable to remove lock file: " + LOCK_FILE_UNIX_LIKE);
                            ex.printStackTrace();
                        }
                    }));
                    return true;
                }
            } catch (final Exception ex) {
                System.err.println("Unable to create and/or lock file: " + LOCK_FILE_UNIX_LIKE);
                ex.printStackTrace();
            }

            return false;
        }
    }
}

