package com.nadir.endersnbt.network;

import com.nadir.endersnbt.EnderSNBTWorkshop;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;

public record RequestNbtC2SPacket(TargetType targetType, BlockPos pos, int entityId, InteractionHand hand) implements CustomPacketPayload {
    public static final Type<RequestNbtC2SPacket> TYPE = new Type<>(EnderSNBTWorkshop.id("request_nbt"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestNbtC2SPacket> CODEC = StreamCodec.ofMember(
        RequestNbtC2SPacket::write, RequestNbtC2SPacket::new
    );

    private RequestNbtC2SPacket(RegistryFriendlyByteBuf buf) {
        this(
            buf.readEnum(TargetType.class),
            buf.readBlockPos(),
            buf.readInt(),
            buf.readEnum(InteractionHand.class)
        );
    }

    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(targetType);
        buf.writeBlockPos(pos != null ? pos : BlockPos.ZERO);
        buf.writeInt(entityId);
        buf.writeEnum(hand != null ? hand : InteractionHand.MAIN_HAND);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
