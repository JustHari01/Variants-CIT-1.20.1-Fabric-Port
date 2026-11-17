package fr.estecka.variantscit.format.properties;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class IntegerComponentProperty
extends AMonoComponentProperty<NbtElement>
{
	static public final IntegerComponentProperty DAMAGE     = new IntegerComponentProperty(new Identifier("minecraft", "damage"));
	static public final IntegerComponentProperty MAX_DAMAGE = new IntegerComponentProperty(new Identifier("minecraft", "max_damage"));

	public IntegerComponentProperty(Identifier componentType){
		super(componentType);
	}

	@Override
	public String GetPropertyString(NbtElement component){
		return component.asString();
	}
}
