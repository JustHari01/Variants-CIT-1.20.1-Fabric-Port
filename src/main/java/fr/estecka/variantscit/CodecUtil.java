package fr.estecka.variantscit;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.jetbrains.annotations.Nullable;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public final class CodecUtil
{
	static private final MinecraftClient client = MinecraftClient.getInstance();

	static public final Codec<Identifier> VCIT_IDENTIFIER = Codec.STRING.comapFlatMap(CodecUtil::VCitIdenfitier, Identifier::toString);
	static public final Codec<String> IDENTIFIER_PATH = Codec.STRING.flatXmap(path->ValidPath(path), path->ValidPath(path));
	static public final Codec<String> IDENTIFIER_NAMESPACE = Codec.STRING.flatXmap(path->ValidNamespace(path), path->ValidNamespace(path));
	static public final Codec<String> NONEMPTY_STRING = Codec.STRING.flatXmap(string->string.isEmpty() ? DataResult.error(()->"String cannot be empty") : DataResult.success(string), string->string.isEmpty() ? DataResult.error(()->"String cannot be empty") : DataResult.success(string));
	static public final Codec<Character> CHAR = Codec.STRING.flatXmap(
		string->string.length() == 1 ? DataResult.success(string.charAt(0)) : DataResult.error(()->"Expected a one-character string"),
		character->DataResult.success(String.valueOf(character))
	);
	static public final Codec<Pattern> REGEX = Codec.STRING.comapFlatMap(CodecUtil::ParseRegex, Pattern::toString);


/******************************************************************************/
/* # Base Types                                                               */
/******************************************************************************/

	static public DataResult<Identifier> VCitIdenfitier(String input){
		if (!input.contains(":"))
			input = VariantsCitMod.MODID + ":" + input;
		return Identifier.validate(input);
	}

	static private DataResult<String> ValidPath(String path){
		return path.matches("[a-z0-9/._-]+") ? DataResult.success(path) : DataResult.error(()->"Invalid character in path: "+path);
	}

	static private DataResult<String> ValidNamespace(String namespace){
		return namespace.matches("[a-z0-9._-]+") ? DataResult.success(namespace) : DataResult.error(()->"Invalid character in namespace: "+namespace);
	}

	static public DataResult<Pattern> ParseRegex(String regex){
		try {
			return DataResult.success(Pattern.compile(regex));
		}
		catch (PatternSyntaxException e){
			return DataResult.error(e::toString);
		}
	}

	static public <K,V> Codec<V> Enum(Codec<K> keyCodec, Map<K,V> units){
		return keyCodec.<V>flatXmap(
			key -> units.containsKey(key) ?
				DataResult.success(units.get(key)) :
				DataResult.error(()->"Unknown key: " + key.toString()),
			obj -> units.containsValue(obj) ?
				DataResult.success(units.entrySet().stream().filter(entry->obj.equals(entry.getValue())).map(Entry::getKey).findFirst().get()) :
				DataResult.error(()->"Unknown unit")
		);
	}


/******************************************************************************/
/* # Codec Modifiers                                                          */
/******************************************************************************/

	/**
	 * Functions to be used in `validate()` on deprecated codecs.
	 */
	static public <T> Function<T,DataResult<T>> WithWarning(String warning, Object... args){
		return o->{
			VariantsCitMod.LOGGER.warn(warning, args);
			return DataResult.success(o);
		};
	}

	static public <T> MapCodec<T> WithWarning(MapCodec<T> codec, String warning, Object... args){
		return codec.flatXmap(WithWarning(warning, args), WithWarning(warning, args));
	}

	static public <T> Codec<T> Validate(Codec<T> codec, Function<T,DataResult<T>> validator){
		return codec.flatXmap(validator, validator);
	}

	static public <T> MapCodec<T> Validate(MapCodec<T> codec, Function<T,DataResult<T>> validator){
		return codec.flatXmap(validator, validator);
	}

	static public <T> T GetOrThrow(DataResult<T> result){
		return result.getOrThrow(false, message->VariantsCitMod.LOGGER.error("Codec failure: {}", message));
	}

	static public boolean IsSuccess(DataResult<?> result){ return result.result().isPresent(); }
	static public boolean IsError(DataResult<?> result){ return result.error().isPresent(); }

	static public <T> Codec<List<T>> OneOrMany(Codec<T> original){
		var listCodec = original.listOf();
		return Codec.either(listCodec, original)
			.xmap(either->either.map(Function.identity(), List::of), list->Either.left(list));
	}

	static public <T> MapCodec<T> MapWithAlternative(MapCodec<T> primary, MapCodec<? extends T> alternative){
		@SuppressWarnings("unchecked")
		MapCodec<T> alternate = (MapCodec<T>) alternative;
		return Codec.mapEither(primary, alternate)
			.xmap(either->either.map(Function.identity(), Function.identity()), value->Either.left(value));
	}

	@SafeVarargs
	static public <T> Codec<T> WithAlternatives(Codec<? extends T> primary, Codec<? extends T>... altArray){
		@SuppressWarnings("unchecked")
		Codec<T> alternative = (Codec<T>) primary;
		for (Codec<? extends T> next : altArray){
			@SuppressWarnings("unchecked")
			Codec<T> nextCodec = (Codec<T>) next;
			Codec<T> prior = alternative;
			alternative = Codec.either(prior, nextCodec)
				.xmap(either->either.map(Function.identity(), Function.identity()), value->Either.left(value));
		}
		return alternative;
	}

	@SafeVarargs
	static public <T> MapCodec<T> MapWithAlternatives(MapCodec<T> primaryMap, MapCodec<T>... mapArray){
		MapCodec<T> alternative = primaryMap;
		for (MapCodec<T> next : mapArray){
			MapCodec<T> prior = alternative;
			alternative = Codec.mapEither(prior, next)
				.xmap(either->either.map(Function.identity(), Function.identity()), value->Either.left(value));
		}
		return alternative;
	}

	static public <T> MapCodec<T> WithAlias(Codec<T> codec, String primary, String alias){
		return MapWithAlternative(
			codec.fieldOf(primary),
			Validate(codec.fieldOf(alias), WithWarning("VCIT field `{}` is deprecated. Use `{}` instead.", alias, primary))
		);
	}

	static public <T> MapCodec<Optional<T>> OptionalWithAlias(Codec<T> codec, String primary, String alias){
		return WithAlias(codec, primary, alias)
			.xmap(Optional::of, Optional::get)
			.orElse(Optional.empty())
			;
	}

	/**
	 * Downcast the decoder's result to a superclass. This strips the codec of
	 * its encoding abilities.
	 */
	static public <SUPER, SUB extends SUPER> MapCodec<SUPER> Anonymize(MapCodec<SUB> original){
		return original.flatXmap(
			o->DataResult.success((SUPER)o),
			o->DataResult.error(()->"Encoding not supported by anonymized codec.")
		);
	}


/******************************************************************************/
/* # Component Serialization                                                  */
/******************************************************************************/

/******************************************************************************/
/* # Resource Reload                                                          */
/******************************************************************************/

	/**
	 * @param directory	The base directory, *without* the trailing `/`
	 * @param suffix	The file extension, including the separating dot.
	 * @return The matching  resources, keyed using  an ID that includes neither
	 * the directory nor the suffix.
	 */
	static public Map<Identifier,Resource> GetResources(ResourceManager manager, String directory, String suffix){
		Map<Identifier, Resource> result = new HashMap<>();
		var resources = manager.findResources(directory, id->id.getPath().endsWith(suffix));
		for (var e : resources.entrySet())
			result.put(AssetIdFromResourceId(e.getKey(), directory, suffix), e.getValue());

		return result;
	}

	static public Identifier AssetIdFromResourceId(Identifier id, String directory, String suffix){
		return id.withPath(path->path.substring(
			directory.length() + 1,
			path.length() - suffix.length()
		));
	}

	static public <T>  DataResult<T> ParseResource(Resource resource, MapCodec<T> codec){
		return ParseResource(resource, codec.codec());
	}

	static public <T>  DataResult<T> ParseResource(Resource resource, Codec<T> codec){
		JsonElement json;
		try {
			json = JsonParser.parseReader(resource.getReader());
		}
		catch (IOException|JsonParseException e){
			return DataResult.error(e::toString);
		}

		return codec.decode(JsonOps.INSTANCE, json).map(Pair::getFirst);
	}

	static public <T> Map<Identifier, T> ReloadResources(ResourceManager manager, Codec<T> codec, String directory, String suffix){
		Map<Identifier, T> results = new HashMap<>();

		Map<Identifier, Resource> resources = GetResources(manager, directory, suffix);
		for (var entry : resources.entrySet()){
			Identifier id = entry.getKey();
			DataResult<T> result = ParseResource(entry.getValue(), codec);
			if (IsSuccess(result))
				results.put(id, GetOrThrow(result));
			else {
				VariantsCitMod.LOGGER.error("Error loading resource {}:\n{}",
					entry.getKey(),
					result.error().get().message()
				);
			}
		}

		return results;
	}
}
