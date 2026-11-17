package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.vanilla.UnclampedOverridePredicate;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtil;

public class PotionLevelPredicate
implements UnclampedOverridePredicate
{
	@Override
	public float unclampedCall(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
		var effects = PotionUtil.getPotionEffects(stack);
		if (effects.isEmpty())
			return 0;
		else
			return effects.get(0).getAmplifier();
	}
}
