package goblinbob.mba.editor;

import goblinbob.mobends.forge.addon.AddonHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ModEntry.MODID)
public class ModEntry
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MODID = "mobends-editor";
    public static final String NAME = "Mo' Bends Editor";
    public static final String VERSION = "0.1.0";

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("PRE-INIT PHASE");

        AddonHelper.registerAddon(MODID, new AddonEntry());
    }
}
