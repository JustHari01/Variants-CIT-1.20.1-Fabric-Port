package fr.estecka.variantscit.format.properties;

import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import fr.estecka.variantscit.LegacyItemComponents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public abstract class AMonoComponentProperty<T extends NbtElement>
implements IStringProperty
{
	protected final Identifier componentType;

	protected AMonoComponentProperty(Identifier componentType){
		this.componentType = componentType;
	}

	public abstract String GetPropertyString(T component);

	@Override
	public final @Nullable T GetReference(ItemStack stack) {
		@SuppressWarnings("unchecked")
		T value = (T) LegacyItemComponents.Get(stack, componentType);
		return value;
	}

	@Override
	public final int GetPropertyHash(ItemStack stack) {
		return Objects.hashCode(GetReference(stack));
	}

	@Override
	public final String GetPropertyString(ItemStack stack) {
		T component = GetReference(stack);
		return (component!=null) ? GetPropertyString(component) : null;
	}
}
