package com.nadir.endersnbt.client;

import com.nadir.endersnbt.network.RequestNbtC2SPacket;
import com.nadir.endersnbt.network.SendNbtS2CPacket;
import com.nadir.endersnbt.network.TargetType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class EnderSNBTWorkshopClient implements ClientModInitializer {
    public static KeyMapping nbtEditKeyBinding;

    @Override
    public void onInitializeClient() {
        nbtEditKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.enders_nbt.edit",
                GLFW.GLFW_KEY_N,
                "category.enders_nbt.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (nbtEditKeyBinding.consumeClick()) {
                if (client.player != null && client.player.hasPermissions(2)) {
                    HitResult hit = client.hitResult;
                    if (hit != null) {
                        if (hit.getType() == HitResult.Type.BLOCK) {
                            BlockHitResult blockHit = (BlockHitResult) hit;
                            ClientPlayNetworking.send(new RequestNbtC2SPacket(TargetType.BLOCK, blockHit.getBlockPos(), 0, null));
                        } else if (hit.getType() == HitResult.Type.ENTITY) {
                            EntityHitResult entityHit = (EntityHitResult) hit;
                            ClientPlayNetworking.send(new RequestNbtC2SPacket(TargetType.ENTITY, null, entityHit.getEntity().getId(), null));
                        } else if (hit.getType() == HitResult.Type.MISS) {
                            ClientPlayNetworking.send(new RequestNbtC2SPacket(TargetType.HAND, null, 0, net.minecraft.world.InteractionHand.MAIN_HAND));
                        }
                    } else {
                        ClientPlayNetworking.send(new RequestNbtC2SPacket(TargetType.HAND, null, 0, net.minecraft.world.InteractionHand.MAIN_HAND));
                    }
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SendNbtS2CPacket.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft.getInstance().setScreen(new com.nadir.endersnbt.client.gui.NbtEditorScreen(payload));
            });
        });
    }
}