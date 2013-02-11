package net.sf.perftence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    public static void writeToFile(final String path, final byte[] data) {
        final File outFile = new File(path);
        try {
            ensureDirectoryExists(outFile.getParentFile());
        } catch (FileNotFoundException e) {
            throw newWritingFileFailed(path, e);
        }
        try {
            writeToFile(outFile, data);
        } catch (Exception e) {
            throw newWritingFileFailed(path, e);
        }
    }

    private static void writeToFile(final File outFile, final byte[] data)
            throws FileNotFoundException, IOException {
        final FileOutputStream fos = new FileOutputStream(outFile);
        try {
            fos.write(data);
        } finally {
            fos.flush();
            fos.close();
        }
    }

    private static WritingFileFailed newWritingFileFailed(final String path,
            final Throwable cause) {
        return new WritingFileFailed("Couldn't write to file: '" + path + "' ",
                cause);
    }

    public static void ensureDirectoryExists(final File dir)
            throws FileNotFoundException {
        final File parent = dir.getParentFile();
        if (!dir.exists()) {
            if (parent == null) {
                throw new FileNotFoundException();
            }
            ensureDirectoryExists(parent);
            if (!dir.mkdir()) {
                throw new WritingFileFailed("Directory '" + dir.getName()
                        + "' wasn't created!");
            }
        }
    }
}