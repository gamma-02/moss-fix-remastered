package net.gamma_02.mossFix.fabric;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.zip.ZipError;

public class FileSystemUtils {



    public static FileSystemHolder getJarFileSystem(URI uri, boolean create) throws IOException {
        URI jarUri;
        String scheme = uri.getScheme();

        try {
            if(!scheme.startsWith("jar")) {
                jarUri = new URI("jar:" + uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());
            } else {
                jarUri = uri;
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        boolean opened = false;
        FileSystem ret;

        try {
            ret = FileSystems.getFileSystem(jarUri);
        } catch (FileSystemNotFoundException ignore) {
            try {
                ret = FileSystems.newFileSystem(jarUri, create ? Collections.singletonMap("create", "true") : Collections.emptyMap());
                opened = true;

            } catch (FileSystemAlreadyExistsException ignore2) {
                ret = FileSystems.getFileSystem(jarUri);
            } catch (IOException | ZipError e) {
                throw new IOException("Error accessing " + uri + ": " + e, e);
            }
        }

        return new FileSystemHolder(ret, opened);
    }

    public record FileSystemHolder(FileSystem system, boolean opened) {}
}
