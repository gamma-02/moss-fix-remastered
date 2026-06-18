package net.gamma_02.mossFix.neoforge;

import com.mojang.logging.LogUtils;
import net.gamma_02.mossFix.FileSystemUtils;
import net.gamma_02.mossFix.MossFix;
import net.neoforged.fml.classloading.ModuleClassLoader;
import net.neoforged.fml.classloading.transformation.TransformingClassLoader;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleReference;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

@Mod(MossFix.MOD_ID)
public final class MossFixNeoForge {

    public static final Logger NEOFORGE_LOGGER = LogUtils.getLogger();

    public MossFixNeoForge() {
        // Run our common setup.
        MossFix.init(true);

        #if MC_VERSION >= 12109


//        System.out.println(jdk.internal.loader.ClassLoaders.appClassLoader().findResource("dataPacks/"));
        //this forces a zip file system for our mod jar to load. It's similar to what fabric-resource-loader does,
        // but it should work with NeoForge as well.
        try {

            NEOFORGE_LOGGER.debug(MossFix.class.getClassLoader().toString());
            NEOFORGE_LOGGER.debug(Thread.currentThread().getContextClassLoader().toString());

            URL url = MossFix.class.getClassLoader().getResource("dataPacks/");

            final ModuleReference[] firstReference = getModuleReferences();

            if(url == null){
                NEOFORGE_LOGGER.info("Unable to access dataPacks resource from class loader resource! Trying to fall back on module loader");

                Optional<URI> location = firstReference[0].location();

                if(location.isEmpty()) {
                    throw new IOException("Unable to access dataPacks resource from module loader!");
                }

                URI uri = location.get();

                URI jarUri = new URI("jar:" + uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());

                NEOFORGE_LOGGER.debug("mod jar location: {}", jarUri);

                MossFix.modFileSystem = FileSystemUtils.getJarFileSystem(uri, true);

                MossFix.setModJarLocation(jarUri.toString());

            } else {
                NEOFORGE_LOGGER.debug(url.toString());

                URI resourceUri = url.toURI();
                MossFix.modFileSystem = FileSystemUtils.getJarFileSystem(resourceUri, true);

                String dataPacksURI = resourceUri.toString();

                MossFix.setModJarLocation(dataPacksURI.replace("!/dataPacks/", ""));
            }



        } catch (IOException | URISyntaxException e) {
            NEOFORGE_LOGGER.error("Unable to load mod jar file system!", e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("lmao fucking how", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("sounds about right", e);
        }


        if(MossFix.modFileSystem != null && MossFix.modFileSystem.opened()){
            NEOFORGE_LOGGER.info("Loaded mod jar file system!");
        } else if (MossFix.modFileSystem != null) {
            NEOFORGE_LOGGER.info("Got mod jar file system!");
        }
        #endif

    }

    #if MC_VERSION >= 12109
    private static ModuleReference @NotNull [] getModuleReferences() throws NoSuchFieldException, IllegalAccessException {
        @SuppressWarnings("UnstableApiUsage") var loader = ((ModuleClassLoader) MossFixNeoForge.class.getClassLoader());
        @SuppressWarnings("UnstableApiUsage") Field loaderPackageLookup = ModuleClassLoader.class.getDeclaredField("packageLookup");

        loaderPackageLookup.setAccessible(true);

        Map<String, ?> packageLookup = (Map<String, ?>) loaderPackageLookup.get(loader);
        Set<String> keySet = packageLookup.keySet();

        final ModuleReference[] firstReference = {null};

        keySet.forEach((s) -> {
            if(s.contains("gamma_02")){
                NEOFORGE_LOGGER.debug("module: {}", s);

                Object infoThing = packageLookup.get(s);
                Class<?> clazz = infoThing.getClass();
                try {
                    Field referenceField = clazz.getDeclaredField("moduleReference");
                    referenceField.setAccessible(true);

                    ModuleReference reference = (ModuleReference) referenceField.get(infoThing);

                    if(firstReference[0] == null && reference.location().isPresent() && reference.location().get().toString().contains(".jar")){
                        firstReference[0] = reference;
                    }

                    NEOFORGE_LOGGER.debug("Reference: {}", reference);
                    NEOFORGE_LOGGER.debug("Location: {}", reference.location());
                    NEOFORGE_LOGGER.debug("Descriptor: {}", reference.descriptor());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        return firstReference;
    }
    #endif
}
