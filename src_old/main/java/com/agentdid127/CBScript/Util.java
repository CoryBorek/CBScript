package com.agentdid127.CBScript;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class Util {

    /**
     * Copies complete directory and contents
     * @param src Source Folder
     * @param dest Destination Folder
     * @throws IOException
     */
    public static void copyDir(Path src, Path dest) throws IOException {
        Files.walk(src).forEach(path -> {
            try {
                Files.copy(path, dest.resolve(src.relativize(path)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Deletes full directory
     * @param dirPath Path of Directory to delete
     * @throws IOException
     */
    public static void deleteDirectoryAndContents(Path dirPath) throws IOException {
        if (!dirPath.toFile().exists()) return;

        //noinspection ResultOfMethodCallIgnored
        Files.walk(dirPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
