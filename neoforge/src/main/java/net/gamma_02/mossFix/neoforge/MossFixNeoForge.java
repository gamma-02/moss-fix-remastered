package net.gamma_02.mossFix.neoforge;

import com.mojang.logging.LogUtils;
import net.gamma_02.mossFix.FileSystemUtils;
import net.gamma_02.mossFix.MossFix;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Mod(MossFix.MOD_ID)
public final class MossFixNeoForge {

    public static final Logger NEOFORGE_LOGGER = LogUtils.getLogger();

    public MossFixNeoForge() {
        // Run our common setup.
        MossFix.init();

        #if MC_VERSION >= 12109

        //this forces a zip file system for our mod jar to load. It's similar to what fabric-resource-loader does,
        // but it should work with NeoForge as well.
        try {
            URL url = MossFix.class.getClassLoader().getResource("dataPacks/");

            if(url == null){
                throw new IOException("Unable to access dataPacks resource!");
            }

            URI resourceUri = url.toURI();
            MossFix.modFileSystem = FileSystemUtils.getJarFileSystem(resourceUri, true);

        } catch (IOException | URISyntaxException e) {
            NEOFORGE_LOGGER.error("Unable to load mod jar file system!", e);
        }


        if(MossFix.modFileSystem != null && MossFix.modFileSystem.opened()){
            NEOFORGE_LOGGER.info("Loaded mod jar file system!");
        } else if (MossFix.modFileSystem != null) {
            NEOFORGE_LOGGER.info("Got mod jar file system!");
        }
        #endif

    }
}
