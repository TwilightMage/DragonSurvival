package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin( Block.class )
public class MixinBlock{
	@Inject( at = @At( "HEAD" ), method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", cancellable = true )
	private static void dropResources(BlockState p_220054_0_, Level p_220054_1_, BlockPos p_220054_2_,
		@Nullable
		BlockEntity p_220054_3_, Entity entity, ItemStack p_220054_5_, CallbackInfo ci){
		if(!DragonUtils.isDragon(entity)){
			return;
		}
		DragonStateHandler handler = DragonUtils.getHandler(entity);
		if(!DragonUtils.isDragonType(handler, DragonTypes.CAVE)){
			return;
		}

		if(p_220054_1_ instanceof ServerLevel){
			Block.getDrops(p_220054_0_, (ServerLevel)p_220054_1_, p_220054_2_, p_220054_3_, entity, p_220054_5_).forEach(p_220057_2_ -> {
				if(!p_220054_1_.isClientSide && !p_220057_2_.isEmpty() && p_220054_1_.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && !p_220054_1_.restoringBlockSnapshots){
					float f = 0.5F;
					double d0 = (double)(p_220054_1_.random.nextFloat() * 0.5F) + 0.25D;
					double d1 = (double)(p_220054_1_.random.nextFloat() * 0.5F) + 0.25D;
					double d2 = (double)(p_220054_1_.random.nextFloat() * 0.5F) + 0.25D;
					ItemEntity itementity = new ItemEntity(p_220054_1_, (double)p_220054_2_.getX() + d0, (double)p_220054_2_.getY() + d1, (double)p_220054_2_.getZ() + d2, p_220057_2_){
						@Override
						public boolean fireImmune(){
							return true;
						}
					};
					itementity.setDefaultPickUpDelay();
					p_220054_1_.addFreshEntity(itementity);
				}
			});
			p_220054_0_.spawnAfterBreak((ServerLevel)p_220054_1_, p_220054_2_, p_220054_5_);
		}

		ci.cancel();
	}
}