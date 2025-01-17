package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowMobGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.HunterEntityCheckProcedure;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class HunterHoundEntity extends Wolf implements DragonHunter{
	public static final EntityDataAccessor<Integer> variety = SynchedEntityData.defineId(HunterHoundEntity.class, EntityDataSerializers.INT);

	public HunterHoundEntity(EntityType<? extends Wolf> type, Level world){
		super(type, world);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();

		this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 1));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Hunter.class).setAlertOthers());
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 2.0, false) {
			@Override
			protected double getAttackReachSqr(LivingEntity entity) {
				return (double) (4.0 + entity.getBbWidth() * entity.getBbWidth());
			}
		});
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 1, true, false, living -> living.hasEffect(MobEffects.BAD_OMEN) || living.hasEffect(DragonEffects.ROYAL_CHASE)));
		this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, Monster.class, false, false) {
			@Override
			public boolean canUse() {
				double x = HunterHoundEntity.this.getX();
				double y = HunterHoundEntity.this.getY();
				double z = HunterHoundEntity.this.getZ();
				Entity entity = HunterHoundEntity.this;
				Level world = HunterHoundEntity.this.level;
				return super.canUse() && HunterEntityCheckProcedure.execute(entity);
			}

			@Override
			public boolean canContinueToUse() {
				double x = HunterHoundEntity.this.getX();
				double y = HunterHoundEntity.this.getY();
				double z = HunterHoundEntity.this.getZ();
				Entity entity = HunterHoundEntity.this;
				Level world = HunterHoundEntity.this.level;
				return super.canContinueToUse() && HunterEntityCheckProcedure.execute(entity);
			}
		});
		this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 2.0, false) {
			@Override
			protected double getAttackReachSqr(LivingEntity entity) {
				return (double) (4.0 + entity.getBbWidth() * entity.getBbWidth());
			}
		});
		this.targetSelector.addGoal(7, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(8, new FollowMobGoal<>(KnightEntity.class, this, 30));
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		entityData.define(variety, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundNBT){
		super.addAdditionalSaveData(compoundNBT);
		compoundNBT.putInt("Variety", entityData.get(variety));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundNBT){
		super.readAdditionalSaveData(compoundNBT);
		entityData.set(variety, compoundNBT.getInt("Variety"));
	}

	@Override
	public boolean doHurtTarget(Entity entity){
		if(ServerConfig.houndDoesSlowdown && entity instanceof LivingEntity){
			if(((LivingEntity)entity).hasEffect(MobEffects.MOVEMENT_SLOWDOWN)){
				((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
			}else{
				((LivingEntity)entity).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
			}
		}
		return super.doHurtTarget(entity);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverWorld, DifficultyInstance difficultyInstance, MobSpawnType reason,
		@Nullable
			SpawnGroupData livingEntityData,
		@Nullable
			CompoundTag compoundNBT){
		entityData.set(variety, random.nextInt(8));
		return super.finalizeSpawn(serverWorld, difficultyInstance, reason, livingEntityData, compoundNBT);
	}

	@Override
	public boolean removeWhenFarAway(double distance){
		return !hasCustomName() && tickCount >= Functions.minutesToTicks(ServerConfig.hunterDespawnDelay);
	}

	@Override
	protected int getExperienceReward(Player p_70693_1_){
		return 1 + level.random.nextInt(2);
	}
}