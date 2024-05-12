package gg.jos.voicechatbroadcast;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.UUID;

@ForgeVoicechatPlugin
public class VoiceChatBroadcastPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return VoiceChatBroadcast.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoiceChatBroadcast.LOGGER.info("VoiceChatBroadcast Plugin Initialized");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
    }

    public void onMicrophone(MicrophonePacketEvent event) {
        if (event.getSenderConnection() == null) {
            return;
        }

        Group group = event.getSenderConnection().getGroup();

        if (group == null) {
            return;
        }

        // Make sure that the player is in the Broadcast group
        if (!group.getName().strip().equalsIgnoreCase("broadcast")) {
            return;
        }

        // Get the minecraft server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        // Get an instance of LuckPerms
        LuckPerms luckperms = LuckPermsProvider.get();

        // Get the current user sending the broadcast
        User user = luckperms.getUserManager().getUser(event.getSenderConnection().getPlayer().getUuid());

        // Check if the user collected exists
        if (user == null)
        {
            return;
        }

        // Cancel the actual microphone packet event that people in that group or close by don't hear the broadcaster twice
        // Event cancel come before permissions so that if someone doesn't have the permission, they won't be able to talk in the broadcast group
        event.cancel();

        // Get UUID of player talking
        UUID playerUUID = event.getSenderConnection().getPlayer().getUuid();

        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);

        if (player == null) {
            return;
        }

        // Check if the user has the broadcast permission. If not, tell them and quit broadcast.
        if (!(user.getCachedData().getPermissionData().checkPermission("voicechat.broadcast").asBoolean())) {
            player.displayClientMessage(new TextComponent(ChatFormatting.RED + "You do not have permission to broadcast."), true);
            return;
        }

        player.displayClientMessage(new TextComponent(ChatFormatting.GREEN + "⚑ You are broadcasting to the server!"), true);

        VoicechatServerApi api = event.getVoicechat();

        List<ServerPlayer> onlinePlayers = server.getPlayerList().getPlayers();

        for (ServerPlayer players : onlinePlayers)
        {
            // Cancel packet processing if it in relation to the broadcaster speaking
            if (players.getUUID().equals((event.getSenderConnection().getPlayer().getUuid())))
            {
                continue;
            }

            VoicechatConnection connection = api.getConnectionOf(players.getUUID());

            if (connection == null)
            {
                continue;
            }

            StaticSoundPacket convertedPacket;
            convertedPacket = createStaticSoundPacket(event.getPacket());

            api.sendStaticSoundPacketTo(connection, convertedPacket);

            players.displayClientMessage(new TextComponent(ChatFormatting.GRAY + "⚑ You are receiving a radio-based broadcast..."), true);
        }

    }

    public StaticSoundPacket createStaticSoundPacket(MicrophonePacket micPacket) {

        StaticSoundPacket soundPacket;

        soundPacket = micPacket.staticSoundPacketBuilder().build();

        return soundPacket;
    }
}
