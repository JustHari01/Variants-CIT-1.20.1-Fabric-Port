package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class PotionEffectModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		var effects = PotionUtil.getPotionEffects(stack);
		if (effects.isEmpty())
			return null;

		return Registries.STATUS_EFFECT.getId(effects.get(0).getEffectType());
	}
}
