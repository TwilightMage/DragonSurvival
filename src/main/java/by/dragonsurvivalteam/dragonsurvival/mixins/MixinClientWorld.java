package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicPlaceholder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;


@Mixin( ClientLevel.class )
public abstract class MixinClientWorld extends Level{
	@Shadow
	@Final
	private LevelRenderer levelRenderer;

	protected MixinClientWorld(WritableLevelData pLevelData, ResourceKey<Level> pDimension, Holder<DimensionType> pDimensionType, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed){
		super(pLevelData, pDimension, pDimensionType, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed);
	}


	@Inject( at = @At( "HEAD" ), method = "destroyBlockProgress", cancellable = true )
	public void destroyBlockProgress(int playerId, BlockPos pos, int progress, CallbackInfo ci){
		BlockState state = getBlockState(pos);

		if(state.getBlock() instanceof SourceOfMagicBlock){
			if(!state.getValue(SourceOfMagicBlock.PRIMARY_BLOCK)){
				BlockEntity blockEntity = getBlockEntity(pos);
				BlockPos pos1 = pos;

				if(blockEntity instanceof SourceOfMagicPlaceholder){
					pos1 = ((SourceOfMagicPlaceholder)blockEntity).rootPos;
				}

				levelRenderer.destroyBlockProgress(playerId, pos1, progress);
				ci.cancel();
			}
		}
	}
}