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



    public static void init() {
        // Write common init code here.

        #if MC_VERSION == "1_21_1"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.21.1");
        #elif MC_VERSION == "1_21"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.21");
        #elif MC_VERSION == "1_20_6"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.20.6");
        #elif MC_VERSION == "1_20_4"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.20.4");
        #elif MC_VERSION == "1_20_3"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.20.3");
        #elif MC_VERSION == "1_20_2"
        WHOLE_MOD_LOGGER.info("Initializing Moss Fix for 1.20.2");
        #endif

        SPREAD_COBBLES = new MossFixDataPack("spread_cobbles",
                #if MC_NVERSION > 12004
                Component.translatable("resourcepack.mossfix.spread_cobbles"),
                #else
                "Spread Moss Over Cobblestone",
                "Allows moss to spread on all kinds of cobblestone.",
                #endif
                Set.of("c"),
                MossFixDataPack.PackPlatform.COMMON);

        SPREAD_SANDS = new MossFixDataPack("spread_sands",
                #if MC_NVERSION > 12004
                Component.translatable("resourcepack.mossfix.spread_sands"),
                #else
                "Spread Moss Over Sand and Gravel",
                "Allows moss to spread on all kinds of sand and gravel.",
                #endif
                Set.of("c"),
                MossFixDataPack.PackPlatform.COMMON);

    }
}
