package by.jackraidenph.dragonsurvival.network.Abilities;

import by.jackraidenph.dragonsurvival.abilities.DragonAbilities;
import by.jackraidenph.dragonsurvival.abilities.common.ActiveDragonAbility;
import by.jackraidenph.dragonsurvival.abilities.common.DragonAbility;
import by.jackraidenph.dragonsurvival.capability.DragonStateProvider;
import by.jackraidenph.dragonsurvival.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncCurrentAbilityCasting implements IMessage<SyncCurrentAbilityCasting>
{
	
	public int playerId;
	private DragonAbility currentAbility;
	
	public SyncCurrentAbilityCasting() {
	
	}
	
	public SyncCurrentAbilityCasting(int playerId, DragonAbility currentAbility) {
		this.playerId = playerId;
		this.currentAbility = currentAbility;
	}
	
	@Override
	public void encode(SyncCurrentAbilityCasting message, PacketBuffer buffer) {
		buffer.writeInt(message.playerId);
		buffer.writeBoolean(currentAbility != null);
		if(currentAbility != null){
			buffer.writeUtf(currentAbility.getId());
			buffer.writeNbt(currentAbility.saveNBT());
		}
	}
	
	@Override
	public SyncCurrentAbilityCasting decode(PacketBuffer buffer) {
		int playerId = buffer.readInt();
		DragonAbility ability = null;
		
		if(buffer.readBoolean()){
			String id = buffer.readUtf();
			ability = DragonAbilities.ABILITY_LOOKUP.get(id).createInstance();
			ability.loadNBT(buffer.readNbt());
		}
		
		return new SyncCurrentAbilityCasting(playerId, ability);
	}
	
	@Override
	public void handle(SyncCurrentAbilityCasting message, Supplier<NetworkEvent.Context> supplier) {
		ServerPlayerEntity playerEntity = supplier.get().getSender();
		
		if(playerEntity == null)
			return;
		
		DragonStateProvider.getCap(playerEntity).ifPresent(dragonStateHandler -> {
			dragonStateHandler.setCurrentlyCasting((ActiveDragonAbility)message.currentAbility);
		});
	}
}