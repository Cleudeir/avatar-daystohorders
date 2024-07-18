package com.avatar.avatar_daystohorders;

import com.avatar.avatar_daystohorders.network.StatusUpdatePacket;
import com.avatar.avatar_daystohorders.server.Events;
import com.avatar.avatar_daystohorders.server.ServerConfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "avatar_daystohorders";

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Events());

        System.out.println("Mod constructor called");
        GlobalConfig.init();
        ServerConfig.init();
    }

    private void setup(final FMLCommonSetupEvent event) {
        NETWORK_CHANNEL.registerMessage(0, StatusUpdatePacket.class, StatusUpdatePacket::toBytes,
                StatusUpdatePacket::new, StatusUpdatePacket::handle);
        System.out.println("Setup method called");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        System.out.println("Client setup method called");
    }
}
