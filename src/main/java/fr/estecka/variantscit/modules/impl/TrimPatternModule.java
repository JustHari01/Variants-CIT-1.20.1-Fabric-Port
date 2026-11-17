package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import fr.estecka.variantscit.LegacyItemComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class TrimPatternModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		NbtElement data = LegacyItemComponents.Get(stack, new Identifier("minecraft", "trim"));
		if (!(data instanceof NbtCompound trim) || !trim.contains("pattern", NbtElement.STRING_TYPE))
			return null;

		return Identifier.tryParse(trim.getString("pattern"));
	}
}
