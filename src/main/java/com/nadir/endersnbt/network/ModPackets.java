package com.nadir.endersnbt.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ModPackets {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(RequestNbtC2SPacket.TYPE, RequestNbtC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SendNbtS2CPacket.TYPE, SendNbtS2CPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SaveNbtC2SPacket.TYPE, SaveNbtC2SPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestNbtC2SPacket.TYPE, ModPackets::onRequestNbt);
        ServerPlayNetworking.registerGlobalReceiver(SaveNbtC2SPacket.TYPE, ModPackets::onSaveNbt);
    }

    private static void onRequestNbt(RequestNbtC2SPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        if (!player.hasPermissions(2)) return;

        context.server().execute(() -> {
            CompoundTag tag = new CompoundTag();
            switch (payload.targetType()) {
                case BLOCK -> {
                    BlockEntity be = player.level().getBlockEntity(payload.pos());
                    if (be != null) {
                        tag = be.saveWithFullMetadata(player.level().registryAccess());
                    }
                }
                case ENTITY -> {
                    Entity entity = player.level().getEntity(payload.entityId());
                    if (entity != null) {
                        entity.save(tag);
                    }
                }
                case HAND -> {
                    ItemStack stack = player.getItemInHand(payload.hand());
                    if (!stack.isEmpty()) {
                        tag = (CompoundTag) stack.save(player.level().registryAccess(), new CompoundTag());
                    }
                }
            }

            ServerPlayNetworking.send(player, new SendNbtS2CPacket(payload.targetType(), payload.pos(), payload.entityId(), payload.hand(), tag));
        });
    }

    private static void onSaveNbt(SaveNbtC2SPacket payload, ServerPlayNetworking.Context context) {
        ServerPlayer player = context.player();
        if (!player.hasPermissions(2)) return;

        context.server().execute(() -> {
            switch (payload.targetType()) {
                case BLOCK -> {
                    BlockEntity be = player.level().getBlockEntity(payload.pos());
                    if (be != null) {
                        be.loadWithComponents(payload.tag(), player.level().registryAccess());
                        be.setChanged();
                        player.level().sendBlockUpdated(payload.pos(), be.getBlockState(), be.getBlockState(), 3);
                    }
                }
                case ENTITY -> {
                    Entity entity = player.level().getEntity(payload.entityId());
                    if (entity != null) {
                        String id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                        if (id != null) {
                            payload.tag().putString("id", id);
                        }
                        entity.load(payload.tag());
                    }
                }
                case HAND -> {
                    ItemStack stack = player.getItemInHand(payload.hand());
                    if (!stack.isEmpty()) {
                        ItemStack newStack = ItemStack.parseOptional(player.level().registryAccess(), payload.tag());
                        player.setItemInHand(payload.hand(), newStack);
                    }
                }
            }
        });
    }
}
