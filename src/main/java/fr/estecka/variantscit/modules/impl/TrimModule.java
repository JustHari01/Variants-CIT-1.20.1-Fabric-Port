package fr.estecka.variantscit.modules.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class TrimModule
extends ASimpleComponentCachingModule
{
	public TrimModule(){
		super(new Identifier("minecraft", "trim"));
	}

	@Override
	public Identifier GetVariantForComponent(NbtElement element){
		if (!(element instanceof NbtCompound trim)
		|| !trim.contains("pattern", NbtElement.STRING_TYPE)
		|| !trim.contains("material", NbtElement.STRING_TYPE))
			return null;

		Identifier pattern = Identifier.tryParse(trim.getString("pattern"));
		Identifier material = Identifier.tryParse(trim.getString("material"));
		if (pattern == null || material == null)
			return null;

		return pattern.withSuffixedPath("_" + material.getPath());
	}
}
