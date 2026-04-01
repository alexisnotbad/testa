package com.kingssmp;

import com.kingssmp.managers.FabricWeaponRegistry;
import com.kingssmp.weapons.fabric.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KingsSMPMod implements ModInitializer {

    public static final String MOD_ID = "kingssmp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static KingsSMPMod instance;

    private FabricWeaponRegistry weaponRegistry;

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("KingsSMP Legendary Weapons initializing...");

        weaponRegistry = new FabricWeaponRegistry();

        // Register all weapons (items + recipes)
        weaponRegistry.register(new FabricVoidBow());
        weaponRegistry.register(new FabricLifestealer());
        weaponRegistry.register(new FabricGhostBlade());
        weaponRegistry.register(new FabricDragonBoneBlade());
        weaponRegistry.register(new FabricJudgementGavel());
        weaponRegistry.register(new FabricPrettyKittyBlade());
        weaponRegistry.register(new FabricSoulReaper());
        weaponRegistry.register(new FabricThunderfistGauntlet());
        weaponRegistry.register(new FabricWithersEmbrace());
        weaponRegistry.register(new FabricStarfallStaff());

        // Register event handlers
        com.kingssmp.listeners.FabricWeaponEventHandler.register();

        LOGGER.info("KingsSMP: {} legendary weapons registered!", weaponRegistry.getAll().size());
    }

    public static KingsSMPMod getInstance() { return instance; }
    public FabricWeaponRegistry getWeaponRegistry() { return weaponRegistry; }
}
