package net.gamma_02.mossFix;


import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.util.Set;

public final class MossFix {
    public static final String MOD_ID = "mossfix";

    public static Logger WHOLE_MOD_LOGGER = LogUtils.getLogger();

    public static MossFixDataPack SPREAD_COBBLES;
    public static MossFixDataPack SPREAD_SANDS;

    public static FileSystemUtils.FileSystemHolder modFileSystem = null;

    public static FileSystemUtils.FileSystemHolder getModFileSystem(){
        return modFileSystem;
    }

    public static void init() {
        // Write common init code here.

        #if MC_VERSION_STRING == "1_21_7"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.21.7");
        #elif MC_VERSION_STRING == "1_21_6"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.21.6");
        #elif MC_VERSION_STRING == "1_21_4"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.21.4");
        #elif MC_VERSION_STRING == "1_21_3"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.21.3");
        #elif MC_VERSION_STRING == "1_21_1"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.21.1");
        #elif MC_VERSION_STRING == "1_21"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.21");
        #elif MC_VERSION_STRING == "1_20_6"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.20.6");
        #elif MC_VERSION_STRING == "1_20_4"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.20.4");
        #elif MC_VERSION_STRING == "1_20_3"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.20.3");
        #elif MC_VERSION_STRING == "1_20_2"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.20.2");
        #endif

        SPREAD_COBBLES = new MossFixDataPack("spread_cobbles",
                #if MC_VERSION > 12004
                Component.literal("Spread Moss Over Cobblestone"),
                #else
                "Spread Moss Over Cobblestone",
                "Allows moss to spread on all kinds of cobblestone.",
                #endif
                Set.of("c"),
                MossFixDataPack.PackPlatform.COMMON);

        SPREAD_SANDS = new MossFixDataPack("spread_sands",
                #if MC_VERSION > 12004
                Component.literal("Spread Moss Over Sand & Gravel"),
                #else
                "Spread Moss Over Sand and Gravel",
                "Allows moss to spread on all kinds of sand and gravel.",
                #endif
                Set.of("c"),
                MossFixDataPack.PackPlatform.COMMON);

    }
}
