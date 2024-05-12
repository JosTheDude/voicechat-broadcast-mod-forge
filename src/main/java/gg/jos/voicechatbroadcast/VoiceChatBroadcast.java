package gg.jos.voicechatbroadcast;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("voicechatbroadcast")
public class VoiceChatBroadcast {
    public static final String MOD_ID = "voicechatbroadcast";

    public static final Logger LOGGER = LogUtils.getLogger();
    public static MinecraftServer minecraftServer;

    private VoiceChatBroadcastPlugin voiceChatBroadcastPlugin;

    public void VoiceChatBroadcastMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("VoiceChatBroadcast Mod Initialized");
    }

}