package fr.estecka.variantscit.vanilla;

import net.minecraft.client.world.ClientWorld;
import fr.estecka.variantscit.LegacyItemComponents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class BucketAgePredicate
implements UnclampedOverridePredicate
{
	@Override
	public float unclampedCall(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
		NbtElement bucket = LegacyItemComponents.Get(stack, new net.minecraft.util.Identifier("minecraft", "bucket_entity_data"));
		if (!(bucket instanceof NbtCompound nbt) || !nbt.contains("Age", NbtElement.NUMBER_TYPE))
			return 0;

		return nbt.getFloat("Age");
	}
}
