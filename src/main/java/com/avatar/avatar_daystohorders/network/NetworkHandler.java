package com.avatar.avatar_daystohorders.network;

import com.avatar.avatar_daystohorders.Main;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = Main.MODID)
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("avatar_daystohorders", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        INSTANCE.messageBuilder(PlayerStatusPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayerStatusPacket::toBytes)
                .decoder(PlayerStatusPacket::new)
                .consumerMainThread(PlayerStatusPacket::handle)
                .add();
    }
}
