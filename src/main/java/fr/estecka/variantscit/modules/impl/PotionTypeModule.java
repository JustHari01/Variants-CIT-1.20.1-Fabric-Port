package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class PotionTypeModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		if (stack.getNbt() == null || !stack.getNbt().contains("Potion"))
			return null;

		return Registries.POTION.getId(PotionUtil.getPotion(stack));
	}
}
