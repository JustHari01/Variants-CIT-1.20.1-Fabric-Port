package fr.estecka.variantscit.vanilla;

import org.jetbrains.annotations.Nullable;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class EnchantedBookLevelPredicate
implements UnclampedOverridePredicate
{
	@Override
	public float unclampedCall(ItemStack stack, @Nullable ClientWorld world, LivingEntity entity, int seed){
		return EnchantmentHelper.get(stack).values().stream().mapToInt(Integer::intValue).max().orElse(0);
	}
}
