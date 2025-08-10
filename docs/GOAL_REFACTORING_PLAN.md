# Goal System Refactoring Plan

## Overview
Refactor the monolithic `CheckKnownWorkersGoal` (742 lines) into smaller, reusable goal components that can be composed for various worker behaviors. This will enable complex social interactions, trading systems, and other emergent behaviors.

## Current State
- `CheckKnownWorkersGoal` handles the entire porter delivery cycle in one massive goal
- State is tracked via `Porter.DeliveryState` enum
- Difficult to test, maintain, and extend
- Cannot reuse logic for other worker types

## Proposed Architecture

### 1. State Machine Extraction
Create a data object pattern for managing complex multi-step tasks:

```java
public class DeliveryTask {
    public enum State {
        IDLE,
        SELECTING_WORKER,
        NAVIGATING_TO_WORKER,
        ASSESSING_NEEDS,
        GOING_TO_OWN_CHEST,
        COLLECTING_ITEMS,
        GOING_TO_WORKER_CHEST,
        DELIVERING_ITEMS,
        RETURNING_HOME
    }
    
    private State currentState = State.IDLE;
    private WorkerInfo targetWorker;
    private BlockPos targetWorkerChestPos;
    private List<ItemStack> itemsToDeliver;
    private long stateStartTime;
    private Container openContainer;
    private boolean chestOpened;
    
    // State transition methods
    public void transitionTo(State newState) { ... }
    public boolean isInState(State state) { ... }
    public void reset() { ... }
}
```

This pattern can be extended for other task types:
- `TradingTask` - For merchant workers
- `PatrolTask` - For guard workers  
- `SocialTask` - For workers meeting and chatting
- `InspectionTask` - For supervisor workers

### 2. Goal Decomposition

#### Generic Reusable Goals

**NavigateToPositionGoal**
- Handles pathfinding to any BlockPos
- Can be used by any entity needing to move somewhere
- Extracted from lines 177-207, 395-411 of CheckKnownWorkersGoal

**SelectKnownWorkerGoal**
- Selects a worker from the known workers list
- Could use different selection strategies (random, nearest, most-in-need)
- Extracted from lines 66-73

**AssessWorkerNeedsGoal**  
- Evaluates what a worker needs (food, tools, materials)
- Returns a needs assessment that other goals can act on
- Extracted from lines 429-493

**CollectItemsFromChestGoal**
- Collects specified items from a container
- Generic item collection with filters/predicates
- Extracted from lines 568-666

**DeliverItemsToChestGoal**
- Delivers items to a specified container
- Handles inventory transfer and stacking logic
- Extracted from lines 668-741

**ReturnHomeGoal**
- Returns entity to their start/home position
- Useful for any worker that needs to return home
- Extracted from lines 395-411

**OpenChestGoal**
- Handles chest opening animation, sound, and game events
- Extracted from lines 550-566 (open portion)

**CloseChestGoal**  
- Handles chest closing animation, sound, and game events
- Extracted from lines 550-566 (close portion)

#### Goal Composition Examples

**Porter Delivery Sequence:**
1. SelectKnownWorkerGoal (priority 7)
2. NavigateToPositionGoal (priority 6) 
3. AssessWorkerNeedsGoal (priority 6)
4. NavigateToPositionGoal (priority 5) - to own chest
5. OpenChestGoal (priority 5)
6. CollectItemsFromChestGoal (priority 5)
7. CloseChestGoal (priority 5)
8. NavigateToPositionGoal (priority 4) - to worker chest
9. OpenChestGoal (priority 4)
10. DeliverItemsToChestGoal (priority 4)
11. CloseChestGoal (priority 4)
12. ReturnHomeGoal (priority 3)

**Social Interaction Sequence:**
1. SelectKnownWorkerGoal (priority 5)
2. NavigateToPositionGoal (priority 4)
3. ChatWithWorkerGoal (priority 3) - new goal
4. ReturnHomeGoal (priority 2)

**Supervisor Inspection Sequence:**
1. SelectKnownWorkerGoal (priority 6)
2. NavigateToPositionGoal (priority 5)
3. AssessWorkerNeedsGoal (priority 4)
4. RecordInspectionGoal (priority 3) - new goal
5. ReturnHomeGoal (priority 2)

### 3. Implementation Steps

#### Phase 1: Infrastructure
1. Create `org.sosly.vwp.tasks` package
2. Implement `DeliveryTask` data object
3. Create `AbstractTask` base class for common task operations
4. Add task management to Porter entity

#### Phase 2: Extract Generic Goals  
1. Extract and implement each generic goal:
   - NavigateToPositionGoal
   - SelectKnownWorkerGoal
   - AssessWorkerNeedsGoal
   - CollectItemsFromChestGoal
   - DeliverItemsToChestGoal
   - ReturnHomeGoal
   - OpenChestGoal
   - CloseChestGoal

#### Phase 3: Update Porter
1. Remove `DeliveryState` enum from Porter
2. Add `DeliveryTask currentDeliveryTask` field
3. Update goal registration to use new decomposed goals
4. Ensure goals check task state for activation

#### Phase 4: Testing
1. Create integration tests for delivery sequence
2. Test state transitions
3. Test goal interruption and resumption
4. Verify chest interactions work correctly

#### Phase 5: Cleanup
1. Delete old `CheckKnownWorkersGoal`
2. Update any references
3. Document new goal system

### 4. Benefits

**Immediate Benefits:**
- Smaller, focused goals (100-150 lines vs 742)
- Easier to test individual behaviors
- Clear separation of concerns
- Reusable components for other workers

**Future Possibilities:**
- Social systems (workers meeting, chatting, forming relationships)
- Complex trading networks between workers
- Patrol and guard behaviors
- Worker scheduling (day/night shifts)
- Emergency response behaviors
- Multi-worker cooperation on tasks

### 5. Migration Strategy

To minimize risk:
1. Keep `CheckKnownWorkersGoal` functional during development
2. Implement new goals alongside old system
3. Add feature flag to switch between old/new system
4. Test thoroughly with new system
5. Remove old system once stable

### 6. Key Technical Considerations

**State Persistence:**
- Task state must be saved/loaded with entity NBT
- Consider state versioning for future changes

**Goal Priority:**
- Higher priority goals interrupt lower ones
- State machine must handle interruption gracefully

**Performance:**
- More goals = more checks per tick
- Consider goal activation caching
- Batch similar checks where possible

**Debugging:**
- Add comprehensive logging for state transitions
- Consider debug overlay showing current goals/states
- Test commands to force state transitions

### 7. Example Code Structure

```
src/main/java/org/sosly/vwp/
├── tasks/
│   ├── AbstractTask.java
│   ├── DeliveryTask.java
│   ├── TradingTask.java (future)
│   └── SocialTask.java (future)
├── entities/
│   ├── ai/
│   │   ├── NavigateToPositionGoal.java
│   │   ├── SelectKnownWorkerGoal.java
│   │   ├── AssessWorkerNeedsGoal.java
│   │   ├── CollectItemsFromChestGoal.java
│   │   ├── DeliverItemsToChestGoal.java
│   │   ├── ReturnHomeGoal.java
│   │   ├── OpenChestGoal.java
│   │   ├── CloseChestGoal.java
│   │   └── MeetWorkerGoal.java (existing)
│   └── workers/
│       └── Porter.java (updated)
```

## Success Criteria
- [ ] All delivery functionality works as before
- [ ] Individual goals are under 200 lines
- [ ] Goals are reusable across different worker types
- [ ] State transitions are logged and debuggable
- [ ] Performance is equal or better than before
- [ ] Tests pass for all scenarios