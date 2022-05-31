package com.github.steveice10.mc.protocol.packet.ingame.clientbound.level;

import com.github.steveice10.mc.protocol.data.MagicValues;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.level.event.*;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.packet.Packet;
import com.nukkitx.math.vector.Vector3i;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.With;

import java.io.IOException;

@Data
@With
@AllArgsConstructor
public class ClientboundLevelEventPacket implements Packet {
    private final @NonNull LevelEvent event;
    private final @NonNull Vector3i position;
    private final LevelEventData data;
    private final boolean broadcast;

    public ClientboundLevelEventPacket(@NonNull LevelEvent event, @NonNull Vector3i position, @NonNull LevelEventData data) {
        this(event, position, data, false);
    }

    public ClientboundLevelEventPacket(NetInput in) throws IOException {
        this.event = LevelEvent.read(in);
        this.position = Position.read(in);
        int value = in.readInt();
        switch (this.event) {
            case RECORD:
                this.data = new RecordEventData(value);
                break;
            case SMOKE:
                this.data = MagicValues.key(SmokeEventData.class, value % 6);
                break;
            case BREAK_BLOCK:
                this.data = new BreakBlockEventData(value);
                break;
            case BREAK_SPLASH_POTION:
            case BREAK_SPLASH_POTION2:
                this.data = new BreakPotionEventData(value);
                break;
            case BONEMEAL_GROW:
            case BONEMEAL_GROW_WITH_SOUND:
                this.data = new BonemealGrowEventData(value);
                break;
            case COMPOSTER:
                this.data = value > 0 ? ComposterEventData.FILL_SUCCESS : ComposterEventData.FILL;
                break;
            case ENDERDRAGON_FIREBALL_EXPLODE:
                this.data = value == 1 ? DragonFireballEventData.HAS_SOUND : DragonFireballEventData.NO_SOUND;
                break;
            case SCULK_BLOCK_CHARGE:
                this.data = new SculkBlockChargeEventData(value);
                break;
            default:
                this.data = null;
                break;
        }

        this.broadcast = in.readBoolean();
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeInt(this.event.getId());
        Position.write(out, this.position);
        int value = 0;
        if (this.data instanceof RecordEventData) {
            value = ((RecordEventData) this.data).getRecordId();
        } else if (this.data instanceof SmokeEventData) {
            value = MagicValues.value(Integer.class, this.data);
        } else if (this.data instanceof BreakBlockEventData) {
            value = ((BreakBlockEventData) this.data).getBlockState();
        } else if (this.data instanceof BreakPotionEventData) {
            value = ((BreakPotionEventData) this.data).getPotionId();
        } else if (this.data instanceof BonemealGrowEventData) {
            value = ((BonemealGrowEventData) this.data).getParticleCount();
        } else if (this.data instanceof ComposterEventData) {
            value = MagicValues.value(Integer.class, this.data);
        } else if (this.data instanceof DragonFireballEventData) {
            value = MagicValues.value(Integer.class, this.data);
        } else if (this.data instanceof SculkBlockChargeEventData) {
            value = ((SculkBlockChargeEventData) data).getLevelValue();
        }

        out.writeInt(value);
        out.writeBoolean(this.broadcast);
    }
}