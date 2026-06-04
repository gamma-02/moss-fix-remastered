package net.gamma_02.mossFix.fabric;

import com.mojang.logging.LogUtils;
import net.gamma_02.mossFix.MossFix;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public final class MossFixFabric implements ModInitializer {

    public static final Logger FABRIC_MOD_LOGGER = LogUtils.getLogger();

    private static FileSystemUtils.FileSystemHolder modFileSystem = null;

    public static FileSystemUtils.FileSystemHolder getModFileSystem(){
        return modFileSystem;
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        MossFix.init();

        //this forces a zip file system for our mod jar to load. It's similar to what fabric-resource-loader does,
        // but it should work with NeoForge as well.
        try {
            URL url = MossFix.class.getResource("/dataPacks/");

            if(url == null){
                throw new IOException("Unable to access dataPacks resource!");
            }

            URI resourceUri = url.toURI();
            modFileSystem = FileSystemUtils.getJarFileSystem(resourceUri, true);

        } catch (IOException | URISyntaxException e) {
            FABRIC_MOD_LOGGER.error("Unable to load mod jar file system!", e);
        }


        if(modFileSystem != null && modFileSystem.opened()){
            FABRIC_MOD_LOGGER.info("Loaded mod jar file system!");
        } else if (modFileSystem != null) {
            FABRIC_MOD_LOGGER.info("Got mod jar file system!");
        }

    }


}
