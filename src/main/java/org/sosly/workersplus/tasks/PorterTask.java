package org.sosly.workersplus.tasks;

import com.talhanation.workers.entities.AbstractWorkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.sosly.workersplus.VillageWorkersPlus;
import org.sosly.workersplus.data.Need;
import org.sosly.workersplus.data.Want;
import org.sosly.workersplus.entities.ai.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PorterTask extends AbstractTask<PorterTask.State> {
    private final AbstractWorkerEntity porter;
    private List<?> needs;
    private List<?> excess;
    private AbstractWorkerEntity target;
    private static final String ITEMS = "items";
    private static final String TARGET = "target";

    public PorterTask(AbstractWorkerEntity porter, TaskCoordinator coordinator) {
        super(State.class, coordinator);
        this.porter = porter;
        reset();
    }

    @Override
    public void reset() {
        State currentState = getCurrentState();

        if (currentState == State.ASSESSING_NEEDS) {
            VillageWorkersPlus.LOGGER.info("No needs found, skipping to GOING_TO_WORKER_CHEST");
            skipToState(State.GOING_TO_WORKER_CHEST);
            return;
        }

        if (currentState == State.ASSESSING_EXCESS) {
            VillageWorkersPlus.LOGGER.info("No excess found, skipping to RETURNING_HOME");
            skipToState(State.RETURNING_HOME);
            return;
        }

        super.reset();
        this.target = null;
        this.needs = null;
        this.excess = null;
    }

    @Override
    public void setData(String key, Object value) {
        switch (key) {
            case ITEMS -> {
                if (!(value instanceof List<?>)) {
                    throw new IllegalArgumentException("items must be a List");
                }

                State currentState = getCurrentState();
                if (currentState == State.ASSESSING_NEEDS) {
                    this.needs = (List<?>) value;
                } else if (currentState == State.ASSESSING_EXCESS) {
                    this.excess = (List<?>) value;
                } else {
                    throw new IllegalStateException("Cannot set items in state: " + currentState);
                }
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
                State currentState = getCurrentState();
                if (currentState == null) {
                    return Optional.empty();
                }

                return switch (currentState) {
                    case COLLECTING_DELIVERY_ITEMS, DELIVERING_ITEMS -> {
                        if (needs == null) yield Optional.empty();
                        yield Optional.of(needs);
                    }
                    case COLLECTING_EXCESS_ITEMS, DEPOSITING_EXCESS -> {
                        if (excess == null) yield Optional.empty();
                        yield Optional.of(excess);
                    }
                    default -> Optional.empty();
                };
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
        VillageWorkersPlus.LOGGER.debug("Loading porter task");

        if (tag.contains(TARGET)) {
            this.target = (AbstractWorkerEntity) Objects.requireNonNull(porter.getServer())
                    .overworld()
                    .getEntity(tag.getUUID(TARGET));

            if (this.target == null) {
                this.reset();
                return;
            }
        }
    }

    private State getCurrentState() {
        if (step < 0 || step >= State.values().length) {
            return null;
        }
        return State.values()[step];
    }

    public enum State implements TaskState {
        SELECTING_WORKER(new Step(TargetKnownWorkerGoal.class)),
        NAVIGATING_TO_WORKER(new Step(MoveToDestinationGoal.class,
                t -> getTargetAt((PorterTask) t))),
        ASSESSING_NEEDS(new Step(AssessWorkerNeedsGoal.class)),
        GOING_TO_OWN_CHEST_FOR_DELIVERY(new Step(MoveToDestinationGoal.class,
                t -> getPorterChest((PorterTask) t))),
        COLLECTING_DELIVERY_ITEMS(new Step(GetItemsFromContainerGoal.class,
                t -> getPorterChest((PorterTask) t))),
        GOING_TO_WORKER_CHEST_FOR_DELIVERY(new Step(MoveToDestinationGoal.class,
                t -> getTargetChest((PorterTask) t))),
        DELIVERING_ITEMS(new Step(PutItemsInContainerGoal.class,
                t -> getTargetChest((PorterTask) t))),
        GOING_TO_WORKER_CHEST(new Step(MoveToDestinationGoal.class,
                t -> getTargetChest((PorterTask) t))),
        ASSESSING_EXCESS(new Step(AssessWorkerExcessGoal.class)),
        COLLECTING_EXCESS_ITEMS(new Step(GetItemsFromContainerGoal.class,
                t -> getTargetChest((PorterTask) t))),
        GOING_TO_OWN_CHEST_FOR_DEPOSIT(new Step(MoveToDestinationGoal.class,
                t -> getPorterChest((PorterTask) t))),
        DEPOSITING_EXCESS(new Step(PutItemsInContainerGoal.class,
                t -> getPorterChest((PorterTask) t))),
        RETURNING_HOME(new Step(MoveToDestinationGoal.class,
                t -> getPorterHome((PorterTask) t)));

        private final Step step;

        State(Step step) {
            this.step = step;
        }

        public Step getStep() {
            return step;
        }

        private static BlockPos getPorterHome(PorterTask task) {
            AbstractWorkerEntity porter = task.porter;
            if (porter == null) {
                return null;
            }
            return porter.getStartPos();
        }

        private static BlockPos getTargetAt(PorterTask task) {
            AbstractWorkerEntity target = task.target;
            if (target == null) {
                return null;
            }
            return target.blockPosition();
        }

        private static BlockPos getPorterChest(PorterTask task) {
            AbstractWorkerEntity porter = task.porter;
            if (porter == null) {
                return null;
            }
            return porter.getChestPos();
        }

        private static BlockPos getTargetChest(PorterTask task) {
            AbstractWorkerEntity target = task.target;
            if (target == null) {
                return null;
            }
            return target.getChestPos();
        }
    }
}
