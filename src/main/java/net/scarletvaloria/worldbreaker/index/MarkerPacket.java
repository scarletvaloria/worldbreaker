package net.scarletvaloria.worldbreaker.index;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.scarletvaloria.worldbreaker.WorldbreakerProtocol;

public record MarkerPacket(BlockPos pos) implements CustomPayload {    public static final Id<MarkerPacket> ID = new Id<>(Identifier.of(WorldbreakerProtocol.MOD_ID, "marker_packet"));
    public static final PacketCodec<RegistryByteBuf, MarkerPacket> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, MarkerPacket::pos, MarkerPacket::new);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}