package fr.estecka.variantscit.modules.impl;

import java.text.Normalizer;
import java.util.Map;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class CustomNameModule
extends ASimpleComponentCachingModule
{
	static public final MapCodec<CustomNameModule> CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Codec.BOOL.fieldOf("debug").orElse(false).forGetter(p->p.debug),
			Codec.unboundedMap(Codec.STRING, Identifier.CODEC).optionalFieldOf("specialNames", Map.of()).forGetter(p->p.specialNames)
		)
		.apply(builder, CustomNameModule::new)
	);

	private final boolean debug;
	private final Map<String,Identifier> specialNames;

	public CustomNameModule(boolean debug, Map<String, Identifier> specialNames){
		super(new Identifier("minecraft", "custom_name"));
		this.debug = debug;
		this.specialNames = specialNames;
	}

	@Override
	public Identifier GetVariantForComponent(NbtElement component){
		Text text = Text.Serializer.fromJson(component.asString());
		if (text == null)
			return null;
		String name = text.getString();
		if (specialNames.containsKey(name))
			return specialNames.get(name);
		
		name = this.Transform(name);
		if (debug)
			VariantsCitMod.LOGGER.info("[custom_name VCIT] #{} \"{}\" -> `{}`", super.cachedVariants.size(), text.getString(), name);
		return Identifier.tryParse(name);
	}

	public String Transform(String name){
		return Normalizer.normalize(name, Normalizer.Form.NFD)
			.replace(' ', '_')
			.toLowerCase()
			.replaceAll("[^a-zA-Z0-9_.-]", "")
			;
	}
}
