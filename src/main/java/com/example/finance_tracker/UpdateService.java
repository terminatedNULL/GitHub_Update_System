package com.example.finance_tracker;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.finance_tracker.FileUtility.*;

/**
 * Acts as an update manager for any active terminatedNULL services.
 * Facilitates downloading and updating of a local service installation
 *
 * TODO:
 *  + Custom GitHub account support
 *  + Custom filepath support
 *  + Callback (parameters specifically) documentation
 *  + Metadata.json parsing for version synchronization
 *  + validateUpdateList()
 *  + validateMetadata()
 *  + Callback function String Parameter(s) & documentation
 */
public class UpdateService {
    private final String serviceName;
    @SuppressWarnings("unchecked")
    private final Function<String[], Void>[] callbackFunctions = (Function<String[], Void>[]) new Function[Callbacks.values().length];
    private static final Logger logger = LoggerFactory.getLogger(UpdateService.class);
    private String version;

    public UpdateService(String serviceName) throws IOException {
        new FileWriter("logfile.log", false).close();
        FileUtility.registerLogger(logger);

        this.serviceName = serviceName;
        Arrays.fill(callbackFunctions, (Function<String, Void>) (Void) -> null);
    }

    public final void setCallbackFunction(Function<String[], Void> func, Callbacks callback) {
        callbackFunctions[callback.ordinal()] = func;
    }

    public void updateCheck() throws IOException, URISyntaxException {
        callbackFunctions[Callbacks.UPDATE_CHECK.ordinal()].apply(null);

        URI uri = new URI("https://terminatednull.github.io/updateService/" + serviceName + "/updateList.txt");
        HttpsURLConnection con = (HttpsURLConnection) uri.toURL().openConnection();

        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "text/plain");

        if(con.getResponseCode() != HttpsURLConnection.HTTP_OK) {
            callbackFunctions[Callbacks.UPDATE_UNAVAILABLE.ordinal()].apply(new String[] {String.valueOf(con.getResponseCode())});
            logger.error("Bad HTTP response code for file 'updateList.txt'! [{}]!", con.getResponseCode());
            return;
        }

        String inputLine;
        StringBuilder content = new StringBuilder();
        BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );

        while (((inputLine) = in.readLine()) != null) {
            content.append(inputLine).append("\n");
        }

        if (!validateUpdateList("")) {
            // TODO
        }

        callbackFunctions[Callbacks.UPDATE_IN_PROGRESS.ordinal()].apply(null);

        String[] fileContents = content.toString().split("\n");
        String[][] fileEntry = new String[content.length()][3];

        for(String[] str : fileEntry)
            Arrays.fill(str, "");

        for (int i = 0; i < fileContents.length; i++) {
            fileEntry[i] = fileContents[i].split(" ");
        }

        for(String[] entry : fileEntry) {
            try {
                switch(entry[0]) {
                    case "mod":
                        updateFile( downloadFile(entry[1]) );
                        break;
                    case "del":
                        FileUtility.deleteFile( entry[1], Paths.get(entry[2]) );
                        break;
                    case "new":
                        newFile(Objects.requireNonNull(downloadFile(entry[1])), Paths.get(entry[2]));
                        break;
                }
            } catch (Exception e) {
                callbackFunctions[Callbacks.UPDATE_UNSUCCESSFUL.ordinal()].apply(new String[] { entry[1] });
                logger.error("Update unsuccessful!\n");
                return;
            }
        }

        callbackFunctions[Callbacks.UPDATE_SUCCESSFUL.ordinal()].apply(null);

        in.close();
        clearTempDirectory();
    }

    private File downloadFile(String fileName) throws IOException, URISyntaxException {
        URI uri = new URI("https://terminatednull.github.io/updateService/" + serviceName + "/" + fileName);
        HttpsURLConnection con = (HttpsURLConnection) uri.toURL().openConnection();

        if (con.getResponseCode() != HttpsURLConnection.HTTP_OK) {
            callbackFunctions[Callbacks.DOWNLOAD_UNSUCCESSFUL.ordinal()].apply(null);
            logger.error("Bad HTTP response code for file '{}'! [{}]", fileName, con.getResponseCode());
            return null;
        }

        Path tempDir = Files.createTempDirectory(null);
        FileUtility.tempDirectory = tempDir;

        try (
            InputStream inputStream = con.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(tempDir + "\\" + fileName);
        ) {
            callbackFunctions[Callbacks.DOWNLOAD_IN_PROGRESS.ordinal()].apply(null);
            int bytesRead = -1;
            byte[] buffer = new byte[4096];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            callbackFunctions[Callbacks.DOWNLOAD_UNSUCCESSFUL.ordinal()].apply(new String[] {fileName});
            logger.error("Error downloading file '{}'!", fileName);
            return null;
        }

        callbackFunctions[Callbacks.DOWNLOAD_SUCCESSFUL.ordinal()].apply(null);

        con.disconnect();
        return new File(Paths.get(fileName).toString());
    }

    private void updateFile(File file) {
        // mod
    }

    private boolean validateUpdateList(String fileContent) {
        return true;
    }

    private boolean validateMetadata(String content) {
        return true;
    }

    public enum Callbacks {
        UPDATE_CHECK,
        UPDATE_UNAVAILABLE,
        UPDATE_IN_PROGRESS,
        UPDATE_SUCCESSFUL,
        UPDATE_UNSUCCESSFUL,
        DOWNLOAD_IN_PROGRESS,
        DOWNLOAD_SUCCESSFUL,
        DOWNLOAD_UNSUCCESSFUL,
        FILE_CREATION_UNSUCCESSFUL,
        FILE_DELETION_UNSUCCESSFUL,
        FILE_MODIFICATION_UNSUCCESSFUL
    }
}