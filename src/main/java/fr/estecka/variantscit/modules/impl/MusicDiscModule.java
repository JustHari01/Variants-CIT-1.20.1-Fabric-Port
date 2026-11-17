package fr.estecka.variantscit.modules.impl;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MusicDiscModule
implements ISimpleCitModule
{
	@Override
	public Identifier GetItemVariant(ItemStack stack){
		if (!(stack.getItem() instanceof MusicDiscItem disc))
			return null;

		Identifier sound = Registries.SOUND_EVENT.getId(disc.getSound());
		String path = sound.getPath();
		String prefix = "music_disc.";
		return path.startsWith(prefix) ? new Identifier(sound.getNamespace(), path.substring(prefix.length())) : sound;
	}
}
