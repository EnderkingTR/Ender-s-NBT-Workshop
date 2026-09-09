package com.nadir.endersnbt.network;

import com.nadir.endersnbt.EnderSNBTWorkshop;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public record SendNbtS2CPacket(TargetType targetType, BlockPos pos, int entityId, InteractionHand hand, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<SendNbtS2CPacket> TYPE = new Type<>(EnderSNBTWorkshop.id("send_nbt"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SendNbtS2CPacket> CODEC = StreamCodec.ofMember(
        SendNbtS2CPacket::write, SendNbtS2CPacket::new
    );

    private SendNbtS2CPacket(RegistryFriendlyByteBuf buf) {
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
