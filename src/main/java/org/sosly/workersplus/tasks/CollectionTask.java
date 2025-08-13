package org.sosly.workersplus.tasks;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.data.Want;
import org.sosly.workersplus.entities.ai.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CollectionTask extends AbstractTask<CollectionTask.State> {
    private final AbstractWorkerEntity collector;
    private List<Want> itemsToCollect;
    private AbstractWorkerEntity target;
    private static final String ITEMS = "items";
    private static final String TARGET = "target";

    public CollectionTask(AbstractWorkerEntity collector, TaskCoordinator coordinator) {
        super(State.class, coordinator);
        this.collector = collector;
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        this.target = null;
        this.itemsToCollect = new ArrayList<>();
    }

    @Override
    public void setData(String key, Object value) {
        switch (key) {
            case ITEMS -> {
                if (!(value instanceof List<?> valueList)) {
                    throw new IllegalArgumentException("items must be a List<Want>");
                }

                if (!valueList.stream().allMatch(Want.class::isInstance)) {
                    throw new IllegalArgumentException("items must be a List<Want>");
                }

                this.itemsToCollect = new ArrayList<>((List<Want>) valueList);
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
            case ITEMS -> {
                if (itemsToCollect == null) {
                    return Optional.empty();
                }
                return Optional.of(itemsToCollect);
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
        VillageWorkersPlus.LOGGER.debug("Loading collection task");
        
        if (tag.contains(TARGET)) {
            this.target = (AbstractWorkerEntity) Objects.requireNonNull(collector.getServer())
                    .overworld()
                    .getEntity(tag.getUUID(TARGET));

            if (this.target == null) {
                this.reset();
                return;
            }
        }
    }

    public enum State implements TaskState {
        FINDING_WORKERS(new Step(TargetKnownWorkerGoal.class)),
        GOING_TO_WORKER(new Step(MoveToDestinationGoal.class,
                t -> getTargetChest((CollectionTask) t))),
        ASSESSING_EXCESS(new Step(AssessWorkerExcessGoal.class)),
        COLLECTING_ITEMS(new Step(GetItemsFromContainerGoal.class,
                t -> getTargetChest((CollectionTask) t))),
        VERIFY_COLLECTION(new Step(VerifyCollectionGoal.class)),
        GOING_TO_CHEST(new Step(MoveToDestinationGoal.class,
                t -> getCollectorChest((CollectionTask) t))),
        DEPOSITING_ITEMS(new Step(PutItemsInContainerGoal.class,
                t -> getCollectorChest((CollectionTask) t))),

        GOING_HOME(new Step(MoveToDestinationGoal.class,
                t -> getCollectorHome((CollectionTask) t)))

        ;

        private final Step step;

        State(Step step) {
            this.step = step;
        }

        public Step getStep() {
            return step;
        }

        private static BlockPos getCollectorHome(CollectionTask task) {
            AbstractWorkerEntity collector = task.collector;
            if (collector == null) {
                return null;
            }
            return collector.getStartPos();
        }

        private static BlockPos getCollectorChest(CollectionTask task) {
            AbstractWorkerEntity collector = task.collector;
            if (collector == null) {
                return null;
            }
            return collector.getChestPos();
        }

        private static BlockPos getTargetChest(CollectionTask task) {
            AbstractWorkerEntity target = task.target;
            if (target == null) {
                return null;
            }
            return target.getChestPos();
        }
    }
}