package org.sosly.vwp.data;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkerRelationships {
    private final Map<UUID, WorkerInfo> knownWorkers = new HashMap<>();

    public Map<UUID, WorkerInfo> getRelationships() {
        return knownWorkers;
    }

    public void addKnown(UUID workerId, WorkerInfo info) {
        knownWorkers.put(workerId, info);
    }

    public void removeKnown(UUID workerId) {
        knownWorkers.remove(workerId);
    }

    public boolean knows(UUID workerId) {
        return knownWorkers.containsKey(workerId);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<UUID, WorkerInfo> entry : knownWorkers.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue().save());
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        knownWorkers.clear();
        for (String key : tag.getAllKeys()) {
            UUID id = UUID.fromString(key);
            CompoundTag workerTag = tag.getCompound(key);
            WorkerInfo info = WorkerInfo.load(workerTag);
            knownWorkers.put(id, info);
        }
    }
}
