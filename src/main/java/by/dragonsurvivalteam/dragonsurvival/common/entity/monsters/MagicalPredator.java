package by.dragonsurvivalteam.dragonsurvival.common.entity.monsters;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

public class MagicalPredator extends Monster{

	private final float scale;
	public int type;
	public float size;
	private boolean deathStar;
	private int teleportationCooldown;

	public MagicalPredator(EntityType<? extends Monster> entityIn, Level worldIn){
		super(entityIn, worldIn);
		type = worldIn.getRandom().nextInt(10);
		size = worldIn.getRandom().nextFloat() + 0.95F;
		scale = size / getBbHeight();
		deathStar = false;
	}

	private static int getActualDistance(Player player){

		AtomicInteger distance = new AtomicInteger();

		if(player != null){
			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(cap.isDragon() && !cap.isHiding()){
					distance.set(30);
				}else if(cap.isDragon()){
					distance.set(18);
				}else{
					distance.set(10);
				}
			});
		}
		return distance.get();
	}

//	public static Builder createMonsterAttributes(){
//		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.375D).add(Attributes.ARMOR, 2.0F).add(Attributes.ATTACK_DAMAGE, 2.0F * ServerConfig.predatorDamageFactor).add(Attributes.ATTACK_KNOCKBACK, 1.0F).add(Attributes.MAX_HEALTH, 29.5F * ServerConfig.predatorHealthFactor);
//	}

	@Override
	protected void tickDeath(){
		super.tickDeath();
		if(deathTime == 19 && !deathStar){
			deathStar = true;
			for(int i = 1; i < 10; ++i){
				for(int r = 0; r < 5; ++r){
					BlockPos blockpos = blockPosition().offset(level.random.nextInt(i) - level.random.nextInt(i), level.random.nextInt(i) - level.random.nextInt(i), level.random.nextInt(i) - level.random.nextInt(i));
					if(level.getBlockState(blockpos).canBeReplaced(Fluids.LAVA) && level.getEntityCollisions(null, new AABB(blockpos)).isEmpty()){
						if(level.isClientSide){
							spawnAnim();
						}else{
							BlockState starState = DSBlocks.PREDATOR_STAR_BLOCK.defaultBlockState();
							starState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(level.getFluidState(blockpos).getType() == Fluids.WATER));
							if(!level.getBlockState(blockpos).getMaterial().isLiquid()){
								level.destroyBlock(blockpos, true);
							}
							level.setBlockAndUpdate(blockpos, starState);
						}
						return;
					}
				}
			}
		}
	}

	@Override
	public boolean hurt(DamageSource source, float damage){
		if(isInvulnerableTo(source)){
			return false;
		}else if(source.getEntity() instanceof LivingEntity){
			teleportationCooldown = 30;
		}
		return super.hurt(source, damage);
	}

	@Override
	public void aiStep(){
		super.aiStep();
		level.addParticle(ParticleTypes.SMOKE, getX() + level.getRandom().nextFloat() * 1.25 - 0.75F, getY() + getBbHeight() / 1.5F * scale, getZ() + level.getRandom().nextFloat() * 1.25 - 0.75F, 0, level.getRandom().nextFloat() / 12.5f, 0);
		if(teleportationCooldown > 0){
			teleportationCooldown--;
		}
	}

	@Override
	protected boolean shouldDespawnInPeaceful(){
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource p_184601_1_){
		return SoundEvents.SKELETON_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return SoundEvents.SKELETON_DEATH;
	}

	@Override
	protected void registerGoals(){
		super.registerGoals();
		goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
		goalSelector.addGoal(4, new DevourXP(level, this));
		targetSelector.addGoal(1, new FindPlayerGoal(this));
		targetSelector.addGoal(2, new IsNearestDragonTargetGoal(this, true));
	}

	@Override
	protected int getExperienceReward(Player p_70693_1_){
		return 1 + level.random.nextInt(2);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound(){
		return SoundEvents.SKELETON_AMBIENT;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound){
		super.addAdditionalSaveData(compound);
		compound.putInt("teleportationCooldown", teleportationCooldown);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound){
		super.readAdditionalSaveData(compound);
		teleportationCooldown = compound.getInt("teleportationCooldown");
	}

	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason,
		@Nullable
			SpawnGroupData spawnDataIn,
		@Nullable
			CompoundTag dataTag){
		super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);


		if(worldIn.getRandom().nextInt(10) == 0){
			Skeleton skeletonentity = EntityType.SKELETON.create(level);
			skeletonentity.absMoveTo(getX(), getY(), getZ(), yRot, 0.0F);
			skeletonentity.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
			worldIn.addFreshEntity(skeletonentity);
			skeletonentity.startRiding(this);
		}
		getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("healthBoost", scale, AttributeModifier.Operation.MULTIPLY_BASE));
		setHealth((float)getAttribute(Attributes.MAX_HEALTH).getValue());
		getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("damageBoost", scale, AttributeModifier.Operation.MULTIPLY_BASE));
		getAttribute(Attributes.ATTACK_KNOCKBACK).addPermanentModifier(new AttributeModifier("attackBoost", scale, AttributeModifier.Operation.MULTIPLY_BASE));
		return spawnDataIn;
	}

	@Override
	public boolean doHurtTarget(Entity target){
		if(!super.doHurtTarget(target)){
			return false;
		}else{
			teleportationCooldown = 30;
			return true;
		}
	}

	@Override
	public double getPassengersRidingOffset(){
		return getBbHeight() * scale * 0.75D;
	}

	private void teleportTo(Entity p_70816_1_){
		Vec3 vec = p_70816_1_.position().subtract(p_70816_1_.getLookAngle().multiply(2, 1, 2));
		double d1 = vec.x();
		double d2 = 256;
		double d3 = vec.z();

		_teleportTo(d1, d2, d3);
	}

	private void _teleportTo(double x, double y, double z){
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos(x, y, z);

		while(blockpos$mutable.getY() > 0 && !level.getBlockState(blockpos$mutable).getMaterial().blocksMotion()){
			blockpos$mutable.move(Direction.DOWN);
		}

		BlockState blockstate = level.getBlockState(blockpos$mutable);
		boolean flag = blockstate.getMaterial().blocksMotion();
		boolean flag1 = blockstate.getFluidState().is(FluidTags.WATER);
		attemptTeleport(x, y, z, true);
		if(flag && !flag1){
			level.playSound(null, xo, yo, zo, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1.0F, 1.0F);
			playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
		}
	}

	public boolean attemptTeleport(double p_213373_1_, double p_213373_3_, double p_213373_5_, boolean p_213373_7_){
		double d0 = getX();
		double d1 = getY();
		double d2 = getZ();
		double d3 = p_213373_3_;
		boolean flag = false;
		BlockPos blockpos = new BlockPos(p_213373_1_, p_213373_3_, p_213373_5_);
		Level world = level;
		boolean flag1 = false;

		while(!flag1 && blockpos.getY() > 0){
			BlockPos blockpos1 = blockpos.below();
			BlockState blockstate = world.getBlockState(blockpos1);
			if(blockstate.getMaterial().blocksMotion()){
				flag1 = true;
			}else{
				--d3;
				blockpos = blockpos1;
			}

			if(flag1){
				teleportTo(p_213373_1_, d3, p_213373_5_);
				if(world.noCollision(this)){
					flag = true;
				}
			}
		}

		if(!flag){
			teleportTo(d0, d1, d2);
			return false;
		}else{
			if(p_213373_7_){
				world.broadcastEntityEvent(this, (byte)46);
			}

			getNavigation().stop();

			return true;
		}
	}

	static class DevourXP extends Goal{

		Level world;
		MagicalPredator entity;

		public DevourXP(Level worldIn, MagicalPredator entityIn){
			world = worldIn;
			entity = entityIn;
		}

		@Override
		public boolean canUse(){
			return true;
		}

		@Override
		public void tick(){
			//this.world.getEntities(EntityType.EXPERIENCE_ORB, this.entity.getBoundingBox().inflate(4), Objects::nonNull).forEach(xpOrb -> NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PacketSyncXPDevour(this.entity.getId(), xpOrb.getId())));
			super.tick();
		}
	}

	static class IsNearestDragonTargetGoal extends NearestAttackableTargetGoal<Player>{

		public IsNearestDragonTargetGoal(Mob p_i50313_1_, boolean p_i50313_3_){
			super(p_i50313_1_, Player.class, p_i50313_3_);
		}

		@Override
		protected AABB getTargetSearchArea(double p_188511_1_){
			Player player = (Player)target;
			return mob.getBoundingBox().inflate(getActualDistance(player));
		}
	}

	static class FindPlayerGoal extends NearestAttackableTargetGoal<Player>{
		private final MagicalPredator beast;

		public FindPlayerGoal(MagicalPredator beastIn){
			super(beastIn, Player.class, false);
			beast = beastIn;
		}

		@Override
		protected AABB getTargetSearchArea(double p_188511_1_){
			Player player = (Player)target;
			return mob.getBoundingBox().inflate(getActualDistance(player));
		}

		@Override
		public void tick(){
			if(target != null){
				if(target instanceof Player){
					if(beast.teleportationCooldown == 0){
						float diff = getActualDistance((Player)target) - beast.distanceTo(target);
						if(diff <= 16 & diff >= -2){
							beast.teleportTo(target);
							beast.teleportationCooldown = 10;
						}
					}
				}
			}
		}
	}
}