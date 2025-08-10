package org.sosly.vwp.tasks;

import net.minecraft.nbt.CompoundTag;

public abstract class AbstractTask {
    protected long stateStartTime;
    protected long lastUpdateTime;
    
    public AbstractTask() {
        this.stateStartTime = 0;
        this.lastUpdateTime = 0;
    }
    
    public void markStateStart(long gameTime) {
        this.stateStartTime = gameTime;
    }
    
    public long getTimeInState(long currentTime) {
        if (stateStartTime == 0) {
            return 0;
        }
        return currentTime - stateStartTime;
    }
    
    public void update(long gameTime) {
        this.lastUpdateTime = gameTime;
    }
    
    public abstract void reset();
    
    public abstract void save(CompoundTag tag);
    
    public abstract void load(CompoundTag tag);
    
    protected void saveBaseData(CompoundTag tag) {
        tag.putLong("StateStartTime", stateStartTime);
        tag.putLong("LastUpdateTime", lastUpdateTime);
    }
    
    protected void loadBaseData(CompoundTag tag) {
        stateStartTime = tag.getLong("StateStartTime");
        lastUpdateTime = tag.getLong("LastUpdateTime");
    }
}