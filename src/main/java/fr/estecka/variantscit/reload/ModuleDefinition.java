package fr.estecka.variantscit.reload;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;

public record ModuleDefinition(
	Identifier type,
	Optional<List<Identifier>> targets,
	int priority,
	String modelPrefix,
	Optional<Identifier> modelParent,
	Optional<Identifier> fallbackModel,
	Map<String,Identifier> specialModels
)
{
	static public final MapCodec<ModuleDefinition> CODEC = RecordCodecBuilder.<ModuleDefinition>mapCodec(builder->builder
		.group(
			Identifier.CODEC.fieldOf("type").forGetter(ModuleDefinition::type),
			CodecUtil.OneOrMany(Identifier.CODEC).optionalFieldOf("items").forGetter(ModuleDefinition::targets),
			Codec.INT.fieldOf("priority").orElse(0).forGetter(ModuleDefinition::priority),
			CodecUtil.Validate(Codec.STRING, ModuleDefinition::ValidatePath).fieldOf("modelPrefix").forGetter(ModuleDefinition::modelPrefix),
			Identifier.CODEC.optionalFieldOf("modelParent").forGetter(ModuleDefinition::fallbackModel),
			Identifier.CODEC.optionalFieldOf("fallback").forGetter(ModuleDefinition::fallbackModel),
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC).optionalFieldOf("special", ImmutableMap.<String,Identifier>of()).forGetter(ModuleDefinition::specialModels)
		)
		.apply(builder, ModuleDefinition::new)
	);

	static public DataResult<String> ValidatePath(String path){
		if (path.matches("[a-z0-9/._-]+"))
			return DataResult.success(path);
		else
			return DataResult.error(()->"Invalid character in path: "+path);
	}

	public @Nullable ModelIdentifier GetFallbackModelId(){
		return fallbackModel.map(id->new ModelIdentifier(id, "inventory")).orElse(null);
	}

	public Map<String, @Nullable ModelIdentifier> GetSpecialModelIds(){
		Map<String, @Nullable ModelIdentifier> result = new HashMap<>();
		for (var entry : this.specialModels.entrySet())
			result.put(entry.getKey(), new ModelIdentifier(entry.getValue(), "inventory"));
		return result;
	}
}
