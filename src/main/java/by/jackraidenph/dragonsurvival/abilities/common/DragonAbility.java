package by.jackraidenph.dragonsurvival.abilities.common;

import by.jackraidenph.dragonsurvival.DragonSurvivalMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.text.NumberFormat;
import java.util.HashMap;

public abstract class DragonAbility
{
    protected static NumberFormat nf = NumberFormat.getInstance();
    private static HashMap<String, ResourceLocation> iconCache = new HashMap<>();
    
    protected int level = 0;
    
    protected final String id, icon;
    protected final int maxLevel;
    protected final int minLevel;
    
    public DragonAbility(String abilityId, String icon, int minLevel, int maxLevel){
        this.nf.setMaximumFractionDigits(1);
        
        this.id = abilityId;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.minLevel = minLevel;
        this.level = minLevel;
    }
    
    public abstract DragonAbility createInstance();
    
    public ResourceLocation getIcon()
    {
        if(!iconCache.containsKey(getLevel() + "_" + getId())){
            ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/" + icon + "_" + getLevel() + ".png");
            iconCache.put(getLevel() + "_" + getId(), texture);
        }
        
        return iconCache.get(getLevel() + "_" + getId());
    }
    
    public String getId() {return id;}
    
    public IFormattableTextComponent getTitle(){
        return new TranslationTextComponent("ds.skill." + getId());
    }
    
    public IFormattableTextComponent getDescription(){
        return new TranslationTextComponent("ds.skill.description." + getId());
    }
    
    public int getMaxLevel()
    {
        return maxLevel;
    }
    public int getMinLevel()
    {
        return minLevel;
    }
    
    public int getLevel() {
        return this.level;
    }
    public void setLevel(int level) {
        this.level = Math.min(getMaxLevel(), Math.max(getMinLevel(), level));
    }
    
    public void tick(){}
    public void frame(float partialTicks){}
    
    public void onKeyPressed(PlayerEntity player) {}
    
    public CompoundNBT saveNBT(){
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("level", level);
        return nbt;
    }
    
    public void loadNBT(CompoundNBT nbt){
        level = nbt.getInt("level");
    }
}
