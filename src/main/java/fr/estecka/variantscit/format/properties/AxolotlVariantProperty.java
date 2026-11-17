package fr.estecka.variantscit.format.properties;

import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class AxolotlVariantProperty
extends AMonoComponentProperty<NbtElement>
{
	static public final AxolotlVariantProperty UNIT = new AxolotlVariantProperty();

	public AxolotlVariantProperty(){
		super(new net.minecraft.util.Identifier("minecraft", "bucket_entity_data"));
	}

	@Override
	public String GetPropertyString(NbtElement component) {
		if (!(component instanceof NbtCompound nbt) || !nbt.contains("Variant", NbtElement.NUMBER_TYPE))
			return null;

		int variantRaw = nbt.getInt("Variant");
		return AxolotlEntity.Variant.byId(variantRaw).getName();
	}
}
