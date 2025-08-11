package org.sosly.workersplus.tasks;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.data.Need;
import org.sosly.workersplus.entities.ai.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class DeliveryTask extends AbstractTask<DeliveryTask.State>  {
    private final Supplier<AbstractWorkerEntity> deliverer;
    private List<Need> needs;
    private AbstractWorkerEntity target;
    private static final String NEEDS = "items";
    private static final String TARGET = "target";

    public DeliveryTask(Supplier<AbstractWorkerEntity> deliverer) {
        super(State.class);
        this.deliverer = deliverer;
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        this.target = null;
    }

    @Override
    public void setData(String key, Object value) {
        switch (key) {
            case NEEDS -> {
                if (!(value instanceof List<?> valueList)) {
                    throw new IllegalArgumentException("needs must be a List<Need>");
                }

                if (!valueList.stream().allMatch(Need.class::isInstance)) {
                    throw new IllegalArgumentException("needs must be a List<Need>");
                }

                this.needs = ((List<Need>) valueList);
            }
            case TARGET -> {
                if (!(value instanceof AbstractWorkerEntity)) {
                    throw new IllegalArgumentException("target must be an AbstractWorkerEntity");
                }

                this.target = (AbstractWorkerEntity) value;
            }
            default -> throw new IllegalArgumentException("Unknown task data key: " + key);
        }
    }

    @Override
    public Optional<Object> getData(String key) {
        switch (key) {
            case NEEDS -> {
                if (needs == null) {
                    return Optional.empty();
                }
                return Optional.of(needs);
            }
            case TARGET -> {
                if (target == null) {
                    return Optional.empty();
                }
                return Optional.of(target);
            }
            default -> throw new IllegalArgumentException("Unknown task data key: " + key);
        }
    }

    @Override
    public CompoundTag save() {
        CompoundTag nbt = super.save();

        if (target != null) {
            nbt.putUUID(TARGET, target.getUUID());
        }

        return nbt;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        VillageWorkersPlus.LOGGER.debug("Loading delivery task");
        if (tag.contains(TARGET)) {
            this.target = (AbstractWorkerEntity) Objects.requireNonNull(deliverer.get().getServer())
                    .overworld()
                    .getEntity(tag.getUUID(TARGET));

            if (this.target == null) {
                this.reset();
                return;
            }
        }
    }

    public enum State implements TaskState {
        SELECTING_WORKER(new Step(TargetKnownWorkerGoal.class)),
        NAVIGATING_TO_WORKER(new Step(MoveToDestinationGoal.class,
                t -> getTargetAt((DeliveryTask) t))),
        ASSESSING_NEEDS(new Step(AssessWorkerNeedsGoal.class)),
        GOING_TO_OWN_CHEST(new Step(MoveToDestinationGoal.class,
                t -> getDeliverChest((DeliveryTask) t))),
        COLLECTING_ITEMS(new Step(GetItemsFromContainerGoal.class,
                t -> getDeliverChest((DeliveryTask) t))),
        GOING_TO_WORKER_CHEST(new Step(MoveToDestinationGoal.class,
                t -> getTargetChest((DeliveryTask) t))),
        DELIVERING_ITEMS(new Step(PutItemsInContainerGoal.class,
                t -> getTargetChest((DeliveryTask) t))),
        RETURNING_HOME(new Step(MoveToDestinationGoal.class,
                t -> getDelivererHome((DeliveryTask) t)))

        ;

        private final Step step;

        State(Step step) {
            this.step = step;
        }

        public Step getStep() {
            return step;
        }

        private static BlockPos getDelivererHome(DeliveryTask task) {
            AbstractWorkerEntity deliverer = task.deliverer.get();
            if (deliverer == null) {
                return null;
            }
            return deliverer.getStartPos();
        }

        private static BlockPos getTargetAt(DeliveryTask task) {
            AbstractWorkerEntity target = task.target;
            if (target == null) {
                return null;
            }
            return target.blockPosition();
        }

        private static BlockPos getDeliverChest(DeliveryTask task) {
            AbstractWorkerEntity deliverer = task.deliverer.get();
            if (deliverer == null) {
                return null;
            }
            return deliverer.getChestPos();
        }

        private static BlockPos getTargetChest(DeliveryTask task) {
            AbstractWorkerEntity target = task.target;
            if (target == null) {
                return null;
            }
            return target.getChestPos();
        }
    }
}
