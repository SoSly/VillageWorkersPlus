package org.sosly.vwp.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class WorkerInfo {
    private final UUID id;
    private String name;
    private BlockPos workPosition;
    
    public WorkerInfo(UUID id, String name, BlockPos workPosition) {
        this.id = id;
        this.name = name;
        this.workPosition = workPosition;
    }
    
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BlockPos getWorkPosition() {
        return workPosition;
    }
    
    public void setWorkPosition(BlockPos pos) {
        this.workPosition = pos;
    }
    
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.putString("name", name);
        if (workPosition != null) {
            tag.putInt("workX", workPosition.getX());
            tag.putInt("workY", workPosition.getY());
            tag.putInt("workZ", workPosition.getZ());
        }
        return tag;
    }
    
    public static WorkerInfo load(CompoundTag tag) {
        UUID id = tag.getUUID("id");
        String name = tag.getString("name");
        BlockPos workPos = null;
        if (tag.contains("workX")) {
            workPos = new BlockPos(tag.getInt("workX"), tag.getInt("workY"), tag.getInt("workZ"));
        }
        return new WorkerInfo(id, name, workPos);
    }
}
