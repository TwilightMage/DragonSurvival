package by.jackraidenph.dragonsurvival.magic.entity.particle;

import by.jackraidenph.dragonsurvival.registration.ParticleRegistry;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Locale;

public class SnowflakeData implements IParticleData
{
	public static final IDeserializer<SnowflakeData> DESERIALIZER = new IDeserializer<SnowflakeData>()
	{
		public SnowflakeData fromCommand(ParticleType<SnowflakeData> particleTypeIn, StringReader reader) throws CommandSyntaxException
		{
			reader.expect(' ');
			float duration = (float)reader.readDouble();
			reader.expect(' ');
			boolean swirls = reader.readBoolean();
			return new SnowflakeData(duration, swirls);
		}
		
		public SnowflakeData fromNetwork(ParticleType<SnowflakeData> particleTypeIn, PacketBuffer buffer)
		{
			return new SnowflakeData(buffer.readFloat(), buffer.readBoolean());
		}
	};
	
	private final float duration;
	private final boolean swirls;
	
	public SnowflakeData(float duration, boolean spins)
	{
		this.duration = duration;
		this.swirls = spins;
	}
	
	@Override
	public void writeToNetwork(PacketBuffer buffer)
	{
		buffer.writeFloat(this.duration);
		buffer.writeBoolean(this.swirls);
	}
	
	@SuppressWarnings( "deprecation" )
	@Override
	public String writeToString()
	{
		return String.format(Locale.ROOT, "%s %.2f %b", Registry.PARTICLE_TYPE.getKey(this.getType()), this.duration, this.swirls);
	}
	
	@Override
	public ParticleType<SnowflakeData> getType()
	{
		return ParticleRegistry.SNOWFLAKE.get();
	}
	
	@OnlyIn( Dist.CLIENT )
	public float getDuration()
	{
		return this.duration;
	}
	
	@OnlyIn( Dist.CLIENT )
	public boolean getSwirls()
	{
		return this.swirls;
	}
	
	public static Codec<SnowflakeData> CODEC(ParticleType<SnowflakeData> particleType)
	{
		return RecordCodecBuilder.create((codecBuilder) -> codecBuilder.group(Codec.FLOAT.fieldOf("duration").forGetter(SnowflakeData::getDuration), Codec.BOOL.fieldOf("swirls").forGetter(SnowflakeData::getSwirls)).apply(codecBuilder, SnowflakeData::new));
	}
}
