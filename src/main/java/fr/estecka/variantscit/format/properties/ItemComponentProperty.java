package fr.estecka.variantscit.format.properties;

import java.util.Optional;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.CodecUtil;
import fr.estecka.variantscit.LegacyItemComponents;
import fr.estecka.variantscit.format.INbtInput;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.NbtAdapter;
import fr.estecka.variantscit.format.NbtPath;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

public record ItemComponentProperty(
	Identifier componentType,
	NbtAdapter nbtAdapter
)
implements IStringProperty
{
	static public final MapCodec<ItemComponentProperty> MAP_CODEC = RecordCodecBuilder.mapCodec(builder->builder
		.group(
			Identifier.CODEC.fieldOf("componentType").forGetter(ItemComponentProperty::componentType),
			NbtAdapter.MAPCODEC.forGetter(ItemComponentProperty::nbtAdapter)
		)
		.apply(builder, ItemComponentProperty::new)
	);

	static public final Codec<TransformableProperty<ItemComponentProperty>> MONOSTRING_DECODER = Codec.STRING.flatXmap(
		ItemComponentProperty::MonostringParse,
		__->DataResult.error(()->"Encoding not supported")
	);

	@Override
	public int GetPropertyHash(ItemStack stack){
		Object cmp = GetReference(stack);
		return (cmp!=null) ? cmp.hashCode() : 0;
	}

	@Override
	public Object GetReference(ItemStack stack) {
		return LegacyItemComponents.Get(stack, this.componentType);
	}

	@Override
	public String GetPropertyString(ItemStack stack){
		NbtElement nbt = LegacyItemComponents.Get(stack, this.componentType);
		if (nbt == null)
			return null;

		String result = this.nbtAdapter.ResolveData(nbt);
		return result;
	}

	static private DataResult<TransformableProperty<ItemComponentProperty>> MonostringParse(String input){
		int pathLocation;
		for (pathLocation = 0; pathLocation<input.length(); pathLocation++){
			char c = input.charAt(pathLocation);
			if (c == '.' || !Identifier.isCharValid(c))
				break;
		}

		String s_component = input.substring(0, pathLocation);
		String s_path      = input.substring(pathLocation);

		var r_path = (!s_path.isEmpty()) ? NbtPath.Parse(s_path) : DataResult.success(NbtPath.IDENTITY);
		var r_component = Identifier.CODEC.decode(NbtOps.INSTANCE, NbtString.of(s_component)).map(Pair::getFirst);

		if (CodecUtil.IsError(r_path))
			return r_path.map(__->null);

		if (CodecUtil.IsError(r_component))
			return r_component.map(__->null);

		NbtPath path = CodecUtil.GetOrThrow(r_path);
		Identifier component = CodecUtil.GetOrThrow(r_component);

		return DataResult.success(new TransformableProperty<>(
			new ItemComponentProperty(component, new NbtAdapter(path, INbtInput.AUTO)),
			IStringTransform.SANITIZE_AUTO,
			Optional.empty()
		));
	}
}
