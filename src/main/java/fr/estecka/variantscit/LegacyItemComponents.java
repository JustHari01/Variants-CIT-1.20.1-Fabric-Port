package fr.estecka.variantscit;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.util.Identifier;

/**
 * Reads the legacy item-NBT representation used by Minecraft 1.20.1 for the
 * item data that later versions expose as data components.
 */
public final class LegacyItemComponents
{
	private LegacyItemComponents() {}

	public static @Nullable NbtElement Get(ItemStack stack, Identifier componentId){
		NbtCompound tag = stack.getNbt();
		String namespace = componentId.getNamespace();
		String path = componentId.getPath();

		if (!"minecraft".equals(namespace))
			return tag == null ? null : tag.get(path);

		return switch (path){
			case "custom_data" -> tag;
			case "block_entity_data" -> GetTag(tag, "BlockEntityTag");
			case "bucket_entity_data" -> GetTag(tag, "BucketVariantTag");
			case "entity_data" -> GetTag(tag, "EntityTag");
			case "custom_name" -> GetPath(tag, "display", "Name");
			case "lore" -> GetPath(tag, "display", "Lore");
			case "custom_model_data" -> GetTag(tag, "CustomModelData");
			case "damage" -> stack.isDamageable() ? NbtInt.of(stack.getDamage()) : null;
			case "max_damage" -> stack.isDamageable() ? NbtInt.of(stack.getMaxDamage()) : null;
			case "enchantments" -> GetTag(tag, "Enchantments");
			case "stored_enchantments" -> GetTag(tag, "StoredEnchantments");
			case "repair_cost" -> GetTag(tag, "RepairCost");
			case "unbreakable" -> GetTag(tag, "Unbreakable");
			case "can_break" -> GetTag(tag, "CanDestroy");
			case "can_place_on" -> GetTag(tag, "CanPlaceOn");
			case "dyed_color" -> GetPath(tag, "display", "color");
			case "suspicious_stew_effects" -> GetTag(tag, "Effects");
			case "trim" -> GetTag(tag, "Trim");
			case "instrument" -> GetTag(tag, "instrument");
			case "potion_contents" -> PotionContents(tag);
			default -> tag == null ? null : tag.get(path);
		};
	}

	private static @Nullable NbtElement GetTag(@Nullable NbtCompound tag, String key){
		return tag == null ? null : tag.get(key);
	}

	private static @Nullable NbtElement GetPath(@Nullable NbtCompound tag, String parent, String key){
		if (tag == null || !tag.contains(parent, NbtElement.COMPOUND_TYPE))
			return null;
		return tag.getCompound(parent).get(key);
	}

	private static @Nullable NbtElement PotionContents(@Nullable NbtCompound tag){
		if (tag == null)
			return null;

		NbtCompound result = new NbtCompound();
		Copy(tag, result, "Potion", "potion");
		Copy(tag, result, "CustomPotionEffects", "custom_effects");
		Copy(tag, result, "CustomPotionColor", "custom_color");
		return result.isEmpty() ? null : result;
	}

	private static void Copy(NbtCompound from, NbtCompound to, String oldKey, String newKey){
		if (from.contains(oldKey))
			to.put(newKey, from.get(oldKey).copy());
	}
}
