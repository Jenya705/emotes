package io.github.kosmx.emotes.forge.mixin;


import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.forge.network.ServerNetwork;
import io.github.kosmx.emotes.server.network.EmotePlayTracker;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkInstance implements IServerNetworkInstance {

    private final EmotePlayTracker emoteTracker = new EmotePlayTracker();
    @Shadow public abstract void send(Packet<?> packet);

    HashMap<Byte, Byte> versions = new HashMap<>();
    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return versions;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        versions = map;
    }

    @Override
    public EmotePlayTracker getEmoteTracker() {
        return emoteTracker;
    }

    @Override
    public boolean sendPlayerID() {
        return true;
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        //sendMessage(builder.build().write(), null);
        this.send(ServerNetwork.newS2CEmotesPacket(builder.copyAndGetData()));
    }

    /*
    @Override
    public void sendMessage(byte[] bytes, @Nullable IEmotePlayerEntity target) {
        this.send(new CustomEmotePacket());
        //this.send(ServerPlayNetworking.createS2CPacket(ServerNetwork.channelID, new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))));
    }
     */
    @Override
    public void sendConfigCallback() {
        EmotePacket.Builder builder = new EmotePacket.Builder().configureToConfigExchange(true);
        try{
            this.send(ServerNetwork.newS2CEmotesPacket(builder.copyAndGetData()));
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
