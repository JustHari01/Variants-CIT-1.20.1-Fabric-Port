package fr.estecka.variantscit.modules.impl;

import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.api.ISimpleCitModule;
import fr.estecka.variantscit.LegacyItemComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

/**
 * Optimization for deterministic modules that may require expensive computation
 * upon a single item component.
 * 
 * The cache in this version is allowed to survive resource reloads.
 */
abstract class ASimpleComponentCachingModule
implements ISimpleCitModule
{
	protected final Identifier componentType;

	/*
	 * The lifetime of each entry  is roughly equivalent  to the lifetime of the
	 * associated  item stack's  component. Item components  are supposed  to be
	 * immutable, so a variant  should never need  to be recomputed  for a given
	 * component.
	 */
	final WeakHashMap<NbtElement, @Nullable Identifier> cachedVariants = new WeakHashMap<>();

	public ASimpleComponentCachingModule(Identifier component){
		this.componentType = component;
	}


	@Override
	public final Identifier GetItemVariant(ItemStack stack){
		NbtElement component = LegacyItemComponents.Get(stack, this.componentType);
		if (component == null)
			return null;

		/**
		 * Do  not  use  computeIfAbsent! It would  attempt  to  recompute  null
		 * values, which are valid to cache.
		 */
		if (!cachedVariants.containsKey(component))
			cachedVariants.put(component, GetVariantForComponent(component));

		return cachedVariants.get(component);
	}

	public abstract Identifier GetVariantForComponent(NbtElement component);
}
