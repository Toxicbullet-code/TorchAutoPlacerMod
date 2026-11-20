package com.toxicbullet.torchautoplacer;

import com.mojang.blaze3d.platform.InputConstants;
import com.toxicbullet.AutoPlace.AutoPlace;
import com.toxicbullet.GUI.CustomScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@Mod(value = TorchAutoPlacer.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = TorchAutoPlacer.MODID, value = Dist.CLIENT)
public class TorchAutoPlacerClient {

    public static final Lazy<KeyMapping> TOGGLE_KEYBIND = Lazy.of(() -> new KeyMapping(
            "key.torchautoplacer.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.torchautoplacer"
    ));

    private static boolean hasInitializedPosition = false;
    private static BlockPos lastPlayerPosition = null;

    public TorchAutoPlacerClient(ModContainer container, net.neoforged.bus.api.IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::registerKeyBindings);
    }

    private void registerKeyBindings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEYBIND.get());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_KEYBIND.get().consumeClick()) {
            Minecraft.getInstance().setScreen(new CustomScreen());
        }

        if (!AutoPlace.isEnabled) {
            hasInitializedPosition = false;
            lastPlayerPosition = null;
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.gameMode == null) {
            return;
        }
        if (client.screen != null) {
            return;
        }

        BlockPos currentPos = client.player.blockPosition();

        if (!hasInitializedPosition || lastPlayerPosition == null) {
            lastPlayerPosition = currentPos;
            hasInitializedPosition = true;
            return;
        }

        int requiredDistance = AutoPlace.blockInterval > 0 ? AutoPlace.blockInterval : 3;
        double distanceSq = lastPlayerPosition.distSqr(currentPos);
        boolean distanceMet = distanceSq >= (requiredDistance * requiredDistance);

        Direction backDir = client.player.getDirection().getOpposite();
        BlockPos targetPos = currentPos.below().relative(backDir);
        BlockPos checkAirPos = targetPos.above();

        boolean lightConditionMet = true;
        if (AutoPlace.lightModeEnabled) {
            int blockLight = client.level.getBrightness(LightLayer.BLOCK, checkAirPos);
            lightConditionMet = (blockLight <= 0);
        }

        if (distanceMet && lightConditionMet) {
            // Check if space is air AND the player's bounding box is not intersecting the target block space
            AABB targetBox = new AABB(checkAirPos);
            boolean playerColliding = client.player.getBoundingBox().intersects(targetBox);

            if (client.level.getBlockState(checkAirPos).isAir() && !playerColliding) {

                Item targetItem = AutoPlace.selectedItem != null ? AutoPlace.selectedItem : Items.TORCH;
                int hotbarSlot = -1;

                for (int i = 0; i < 9; i++) {
                    ItemStack item = client.player.getInventory().getItem(i);
                    if (!item.isEmpty() && item.getItem() == targetItem) {
                        hotbarSlot = i;
                        break;
                    }
                }

                if (hotbarSlot == -1 && targetItem != Items.TORCH) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack item = client.player.getInventory().getItem(i);
                        if (!item.isEmpty() && item.getItem() == Items.TORCH) {
                            hotbarSlot = i;
                            break;
                        }
                    }
                }

                if (hotbarSlot != -1) {
                    int previousSlot = client.player.getInventory().selected;
                    client.player.getInventory().selected = hotbarSlot;

                    Vec3 hitVec = new Vec3(
                            targetPos.getX() + 0.5,
                            targetPos.getY() + 1.0,
                            targetPos.getZ() + 0.5
                    );
                    BlockHitResult blockHitResult = new BlockHitResult(
                            hitVec, Direction.UP, targetPos, false
                    );

                    client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, blockHitResult);
                    client.player.getInventory().selected = previousSlot;

                    lastPlayerPosition = currentPos;
                }
            }
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
    }
}