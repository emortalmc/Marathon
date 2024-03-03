package dev.emortal.minestom.marathon.animator;

import dev.emortal.minestom.marathon.MarathonGame;
import dev.emortal.minestom.marathon.util.BetterEntity;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PathAnimator implements BlockAnimator {
    private static final double TIME_TO_ANIMATE = 0.6;

    private @Nullable BetterEntity lastEntity = null;

    @Override
    public void setBlockAnimated(@NotNull Instance instance, @NotNull Point point, @NotNull Block block, @NotNull Point lastPoint) {
        Pos realLastPoint = Pos.fromPoint(lastPoint.add(0.5, 0, 0.5));
        if (this.lastEntity != null && !this.lastEntity.isRemoved()) {
            realLastPoint = this.lastEntity.getPosition();
        }

        this.lastEntity = new BetterEntity(EntityType.FALLING_BLOCK);
        this.lastEntity.setTag(MarathonGame.MARATHON_ENTITY_TAG, true);
        this.lastEntity.setPhysics(false);
        this.lastEntity.setDrag(false);
        this.lastEntity.setNoGravity(true);

        FallingBlockMeta meta = (FallingBlockMeta) this.lastEntity.getEntityMeta();
        meta.setBlock(block);

        Vec newVelocity = Vec.fromPoint(point.sub(realLastPoint))
                .normalize()
                .mul(point.distance(realLastPoint) / TIME_TO_ANIMATE);
        this.lastEntity.setVelocity(newVelocity);

        this.lastEntity.setInstance(instance, realLastPoint);

        Entity finalEntity = this.lastEntity;
        this.lastEntity.scheduler()
                .buildTask(() -> {
                    finalEntity.remove();
                    instance.setBlock(point, block);
                })
                .delay(TaskSchedule.tick((int) (TIME_TO_ANIMATE * ServerFlag.SERVER_TICKS_PER_SECOND)))
                .schedule();
//        lastEntity.scheduleRemove(Duration.ofMillis((long) (timeToAnimate * 1000)));
    }

    @Override
    public void reset() {
        this.lastEntity = null;
    }
}
