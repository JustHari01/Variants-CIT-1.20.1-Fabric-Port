package fr.estecka.variantscit.modules.impl;

import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class EnchantmentModule
extends AComponentCachingModule
{
	static public final MapCodec<EnchantmentModule> CreateCodec(Identifier targetComponent){
		return RecordCodecBuilder.mapCodec(builder->builder
			.group(
				Codec.unboundedMap(Identifier.CODEC, Codec.INT).optionalFieldOf("requiredEnchantments", Map.of()).forGetter(o->o.precondition),
				Codec.STRING.optionalFieldOf("levelSeparator").forGetter(o->o.separator)
			)
			.apply(builder, (pre,sep)->new EnchantmentModule(targetComponent, pre, sep))
		);
	}

	private final Map<Identifier, Integer> precondition;
	private final Optional<String> separator;

	public EnchantmentModule(Identifier component, Map<Identifier, Integer> precondition, Optional<String> separator){
		super(component);
		this.separator = separator;
		this.precondition = precondition;
	}

	@Override
	public ModelIdentifier GetModelForComponent(NbtElement component, IVariantManager library){
		if (!(component instanceof NbtList list))
			return null;

		Map<Enchantment, Integer> enchants = EnchantmentHelper.fromNbt(list);
		if (enchants.isEmpty() || !this.MatchesPrecondition(enchants))
			return null;

		if (enchants.size() > precondition.size()+1 && null != library.GetSpecialModel("multi"))
			return library.GetSpecialModel("multi");

		Map.Entry<Enchantment, Integer> bestFit = null;
		for (var enchant : enchants.entrySet()){
			Identifier id = Registries.ENCHANTMENT.getId(enchant.getKey());
			if (!this.precondition.containsKey(id) && CompareEnchants(enchant, bestFit, library) > 0)
				bestFit = enchant;
		}

		return bestFit == null ? null : this.GetEnchantModel(bestFit, library);
	}

	private boolean MatchesPrecondition(Map<Enchantment, Integer> enchants){
		for (var condition : this.precondition.entrySet()){
			Enchantment enchantment = Registries.ENCHANTMENT.get(condition.getKey());
			if (enchants.getOrDefault(enchantment, 0) < condition.getValue())
				return false;
		}
		return true;
	}

	private int CompareEnchants(Map.Entry<Enchantment, Integer> a, @Nullable Map.Entry<Enchantment, Integer> b, IVariantManager library){
		if (b == null) return 1;

		int result = Boolean.compare(
			this.HasVariantModel(a, library),
			this.HasVariantModel(b, library)
		);
		if (result != 0) return result;

		result = IncompatibleEnchantmentCount(a.getKey()) - IncompatibleEnchantmentCount(b.getKey());
		if (result != 0) return result;

		return a.getValue() - b.getValue();
	}

	private int IncompatibleEnchantmentCount(Enchantment source){
		int count = 0;
		for (Enchantment other : Registries.ENCHANTMENT)
			if (source != other && !source.canCombine(other))
				++count;
		return count;
	}

	private ModelIdentifier GetEnchantModel(Map.Entry<Enchantment, Integer> enchant, IVariantManager library){
		Identifier variantId = Registries.ENCHANTMENT.getId(enchant.getKey());
		if (variantId == null)
			return null;

		if (separator.isPresent()){
			Identifier baseId = variantId.withSuffixedPath(separator.get());
			for (int i=enchant.getValue(); 0<=i; --i){
				Identifier leveledId = baseId.withSuffixedPath(String.valueOf(i));
				if (library.HasVariantModel(leveledId)){
					variantId = leveledId;
					break;
				}
			}
		}
		return library.GetVariantModel(variantId);
	}

	private boolean HasVariantModel(Map.Entry<Enchantment, Integer> enchant, IVariantManager library){
		return null != GetEnchantModel(enchant, library);
	}
}
