package com.nadir.endersnbt.network;

import com.nadir.endersnbt.EnderSNBTWorkshop;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public record SaveNbtC2SPacket(TargetType targetType, BlockPos pos, int entityId, InteractionHand hand, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<SaveNbtC2SPacket> TYPE = new Type<>(EnderSNBTWorkshop.id("save_nbt"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveNbtC2SPacket> CODEC = StreamCodec.ofMember(
        SaveNbtC2SPacket::write, SaveNbtC2SPacket::new
    );

    private SaveNbtC2SPacket(RegistryFriendlyByteBuf buf) {
        this(
            buf.readEnum(TargetType.class),
            buf.readBlockPos(),
            buf.readInt(),
            buf.readEnum(InteractionHand.class),
            (CompoundTag) buf.readNbt()
        );
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(targetType);
        buf.writeBlockPos(pos != null ? pos : BlockPos.ZERO);
        buf.writeInt(entityId);
        buf.writeEnum(hand != null ? hand : InteractionHand.MAIN_HAND);
        buf.writeNbt(tag);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
