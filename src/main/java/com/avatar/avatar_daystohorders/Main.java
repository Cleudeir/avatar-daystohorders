package com.avatar.avatar_daystohorders;

import com.avatar.avatar_daystohorders.server.Events;
import com.avatar.avatar_daystohorders.server.ServerConfig;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "avatar_daystohorders";

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
        // Some preinit code
        System.out.println("Setup method called");
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        System.out.println("Client setup method called");
        // Some client setup code
    }
}
