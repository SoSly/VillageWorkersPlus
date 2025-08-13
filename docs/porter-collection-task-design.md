# Porter Collection Task Design

## Overview
The Collection Task enables Porters to visit known workers' chests and collect items that workers don't need, preventing chest overflow and automating resource management.

## Core Mechanics

### What Gets Collected
1. **Unwanted Items**: Any item where `worker.wantsToKeep(itemStack)` returns `false`
2. **Excess Wanted Items**: For items where `worker.wantsToKeep(itemStack)` returns `true`:
   - If the item is stackable, keep only 1 stack (64 items max)
   - Take any additional stacks beyond the first

### Task Flow
1. Porter identifies a known worker who has a chest with excess/unwanted items
2. Porter navigates to the worker's chest location  
3. Porter collects items based on the rules above (using existing `GetItemsFromContainerGoal`)
4. Porter returns home to deposit items in own chest (using existing `PutItemsInContainerGoal`)
5. Task ends, allowing next task to begin

## Implementation Components

### New Classes
- `CollectionTask` - Manages the collection task state and logic (similar to `DeliveryTask`)
- `AssessWorkerExcessGoal` - Identifies items to be collected

### Reused Existing Classes
- `GetItemsFromContainerGoal` - Already handles taking items from containers
- `PutItemsInContainerGoal` - Already handles depositing items
- `MoveToDestinationGoal` - Already handles navigation
- `TargetKnownWorkerGoal` - Already finds workers from relationships
- `StartTaskGoal` / `EndTaskGoal` - Already handle task lifecycle

### Goal Priority Structure
```java
// Existing delivery goals (higher priority)
3: PutItemsInContainerGoal (delivery)
4: GetItemsFromContainerGoal (delivery)
5: MoveToDestinationGoal (delivery)
6: AssessWorkerNeedsGoal (delivery)
7: TargetKnownWorkerGoal (delivery)
8: EndTaskGoal (delivery)
9: StartTaskGoal (delivery)

// New collection goals (lower priority)
10: PutItemsInContainerGoal (collection) // Return home to deposit
11: GetItemsFromContainerGoal (collection) // Reuse for collecting excess
12: MoveToDestinationGoal (collection)  // Reuse existing
13: AssessWorkerExcessGoal (collection) // NEW - identifies items to be collected
14: TargetKnownWorkerGoal (collection)  // Reuse to find workers
15: EndTaskGoal (collection)
16: StartTaskGoal (collection)

// General goals (lowest priority)
20: ReturnToHomeGoal
25: MeetNewWorkerGoal
30: RandomStrollGoal
```

### CollectionTask State
The `CollectionTask` will track:
- Current target worker
- Target container position
- Items to collect (determined by `AssessWorkerExcessGoal`)
- Task phase (ASSESSING, COLLECTING, DEPOSITING)

### AssessWorkerExcessGoal Logic
This goal will:
1. Find a worker with a known chest position
2. Check the chest contents
3. For each item stack:
   - If `!worker.wantsToKeep(item)` → Mark for collection
   - If `worker.wantsToKeep(item)` and it's stackable:
     - Count total of this item type
     - If total > 64 → Mark excess for collection
4. Populate the `CollectionTask` with items to collect
5. Set the task destination to the worker's chest

## Worker-Specific Collection Examples

### Lumberjack
- **Keeps**: Saplings (up to 64), axes
- **Porter Collects**: Logs, planks, sticks, leaves, apples, stripped logs

### Farmer  
- **Keeps**: Seeds (up to 64 each type), hoes, bone meal
- **Porter Collects**: Excess wheat, carrots, potatoes, beetroot

### Miner
- **Keeps**: Torches (up to 64), pickaxes, shovels
- **Porter Collects**: Cobblestone, dirt, gravel, andesite, diorite, granite, excess ores

### Fisher
- **Keeps**: Fishing rods, boats
- **Porter Collects**: Fish, junk items (boots, bottles, bowls), treasure items

### Animal Farmers
- **Keeps**: Breeding food (up to 64), shears/buckets as appropriate
- **Porter Collects**: Wool, beef, pork, chicken, eggs, leather, milk buckets

## Requirements
- Worker must have a chest position set
- Porter must have inventory space available
- Porter must have a chest position set (for depositing collected items)