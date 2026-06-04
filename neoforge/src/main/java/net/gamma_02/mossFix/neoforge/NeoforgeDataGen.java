package net.gamma_02.mossFix.neoforge;
#if MC_NVERSION >= 12100

import net.gamma_02.mossFix.MossFix;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MossFix.MOD_ID)
public class NeoforgeDataGen {


    @SubscribeEvent
    public static void gatherDataGen(final GatherDataEvent datagen){

    }

}
#endif
