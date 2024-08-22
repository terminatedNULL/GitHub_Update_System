package com.example.finance_tracker;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtility {

    private static Logger logger;

    public static Path tempDirectory;

    public static void registerLogger(Logger logger) {
        FileUtility.logger = logger;
    }

    /**
     * Deletes a target file, searching recursively if it cannot be found in the base directory
     *
     * @param name The name of the file to delete
     * @param path The starting directory to search for the file from
     * @throws IOException Errors if the file cannot be deleted successfully
     */
    public static void deleteFile(String name, Path path) throws IOException {
        Path deletePath = Paths.get(path + "\\" + name);

        try {
            Files.delete(deletePath);
        } catch (NoSuchFileException e) {
            logger.warn("Error deleting file '{}', file not found! Attempting recursive search!", name);
        } catch (DirectoryNotEmptyException e) {
            logger.error("Error Deleting file '{}', directory '{}' is not empty!", name, deletePath);
        } catch (IOException e) {
            logger.warn("Error deleting file '{}', attempting recursive search!", name);
        } catch (SecurityException e) {
            logger.error("Error deleting file '{}', access denied!", name);
        } finally {
            // Search for file recursively
            List<Path> found;

            try (
                Stream<Path> pStream = Files.find(
                    path,
                    Integer.MAX_VALUE,
                    (p, basicFileAttributes) ->
                        p.getFileName().toString().equalsIgnoreCase(name)
                )
            ) {
                found = pStream.collect(Collectors.toList());
            }

            if(found.isEmpty()) {
                logger.warn("Error deleting file '{}', file not found!", name);
            }

            try {
                Files.delete(found.get(0));
            } catch (NoSuchFileException e) {
                logger.warn("Error deleting file '{}', file not found!", name);
            } catch (DirectoryNotEmptyException e) {
                logger.error("Error Deleting file '{}', directory '{}' is not empty!", name, deletePath);
            } catch (IOException e) {
                logger.error("Error deleting file '{}'", name);
            } catch (SecurityException e) {
                logger.error("Error deleting file '{}', access denied!", name);
            }
        }
    }

    public static void newFile(File file, Path dir) {
        newFile(file, dir, (Void) -> null);
    }

    public static void newFile(File file, Path dir, Function<String, Void> callback) {
        try {
            Files.move(Paths.get(tempDirectory + "\\" + file.getName()), Paths.get(dir + "\\" + file.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Error creating file '{}'! [{}]", file.getName(), System.getProperty("user.dir"));
        } catch (SecurityException e) {
            logger.error("Access denied creating file '{}'! [{}]", file.getName(), System.getProperty("user.dir"));
        } finally {
            callback.apply(null);
        }
    }

    public static void clearTempDirectory() {
        for (File f : Objects.requireNonNull(tempDirectory.toFile().listFiles())) {
            try {
                if(!f.delete()) {
                    logger.error("Error deleting file '{}' [{}]", f.getName(), tempDirectory);
                }
            } catch (SecurityException e) {
                logger.error("Access denied when deleting file '{}' [{}]", f.getName(), tempDirectory);
            }
        }

        try {
            Files.delete(tempDirectory);
        } catch (Exception e) {
            logger.error("Error deleting temporary directory '{}'",  tempDirectory);
        }
    }

    /**
     * Find a file in a directory recursively
     * @param name The name of the file to find
     * @param startDir The directory to start the search from
     * @return The path of the file if found, null if not
     */
    private Path findFile(String name, File startDir) {
        // idfk
        return null;
    }
}
