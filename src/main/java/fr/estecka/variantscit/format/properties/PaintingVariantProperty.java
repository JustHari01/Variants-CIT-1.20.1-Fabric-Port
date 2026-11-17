package fr.estecka.variantscit.format.properties;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class PaintingVariantProperty
extends AMonoComponentProperty<NbtElement>
{
	static public final PaintingVariantProperty UNIT = new PaintingVariantProperty();

	private PaintingVariantProperty(){
		super(new Identifier("minecraft", "entity_data"));
	}

	@Override
	public String GetPropertyString(NbtElement component) {
		if (!(component instanceof NbtCompound nbt) || !nbt.contains("variant", NbtElement.STRING_TYPE))
			return null;
		return nbt.getString("variant");
	}

	public Identifier GetPropertyId(NbtElement component){
		String rawVariant = GetPropertyString(component);
		return (rawVariant!=null) ?  Identifier.tryParse(rawVariant) : null;
	}
	
}
