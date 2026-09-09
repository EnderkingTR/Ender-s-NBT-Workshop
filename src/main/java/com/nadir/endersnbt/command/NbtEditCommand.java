package com.nadir.endersnbt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.nadir.endersnbt.network.RequestNbtC2SPacket;
import com.nadir.endersnbt.network.SendNbtS2CPacket;
import com.nadir.endersnbt.network.TargetType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NbtEditCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nbtedit")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("hand")
                .executes(NbtEditCommand::editHand))
            .then(Commands.literal("block")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(context -> editBlock(context, BlockPosArgument.getLoadedBlockPos(context, "pos")))))
            .then(Commands.literal("entity")
                .then(Commands.argument("target", EntityArgument.entity())
                    .executes(context -> editEntity(context, EntityArgument.getEntity(context, "target")))))
        );
    }

    private static int editHand(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (!stack.isEmpty()) {
                CompoundTag tag = (CompoundTag) stack.save(player.level().registryAccess(), new CompoundTag());
                ServerPlayNetworking.send(player, new SendNbtS2CPacket(TargetType.HAND, BlockPos.ZERO, 0, InteractionHand.MAIN_HAND, tag));
            }
        } catch (Exception e) {
            // ignore
        }
        return 1;
    }

    private static int editBlock(CommandContext<CommandSourceStack> context, BlockPos pos) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            BlockEntity be = player.level().getBlockEntity(pos);
            if (be != null) {
                CompoundTag tag = be.saveWithFullMetadata(player.level().registryAccess());
                ServerPlayNetworking.send(player, new SendNbtS2CPacket(TargetType.BLOCK, pos, 0, InteractionHand.MAIN_HAND, tag));
            }
        } catch (Exception e) {
            // ignore
        }
        return 1;
    }

    private static int editEntity(CommandContext<CommandSourceStack> context, Entity target) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            CompoundTag tag = new CompoundTag();
            target.save(tag);
            ServerPlayNetworking.send(player, new SendNbtS2CPacket(TargetType.ENTITY, BlockPos.ZERO, target.getId(), InteractionHand.MAIN_HAND, tag));
        } catch (Exception e) {
            // ignore
        }
        return 1;
    }
}
