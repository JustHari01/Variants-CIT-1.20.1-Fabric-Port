package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class GoatHornModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		NbtElement component = fr.estecka.variantscit.LegacyItemComponents.Get(stack, new Identifier("minecraft", "instrument"));
		if (component == null || component.getType() != NbtElement.STRING_TYPE)
			return null;

		return Identifier.tryParse(component.asString());
	}
}
