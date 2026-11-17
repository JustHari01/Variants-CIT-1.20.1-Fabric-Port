package fr.estecka.variantscit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mojang.serialization.MapCodec;
import fr.estecka.variantscit.modules.IItemModelProvider;
import fr.estecka.variantscit.modules.impl.*;
import fr.estecka.variantscit.reload.ModuleLoader;
import fr.estecka.variantscit.vanilla.*;


public class VariantsCitMod
implements ClientModInitializer, PreparableModelLoadingPlugin<ModuleLoader.Result>
{
	static public final String MODID = "variants-cit";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	static public int reloadcount = 0;
	static private Map<Item, IItemModelProvider> MODULES = new HashMap<>();
	static private Map<ModelIdentifier, Identifier> AUTOGEN = new HashMap<>();

	static public @Nullable IItemModelProvider GetModule(Item itemType){
		return MODULES.get(itemType);
	}

	static public Map<ModelIdentifier, Identifier> GetModelsToCreate(){
		return Map.copyOf(AUTOGEN);
	}

	@Override
	public void onInitializeClient(){
		PreparableModelLoadingPlugin.register(new ModuleLoader(), this);

		ModuleRegistry.Register(new Identifier("minecraft", "axolotl_variant"), AxolotlBucketModule.CODEC);
		ModuleRegistry.Register(Identifier.tryParse("minecraft:block_entity_data"), ComponentDataModule.CreateLegacyCodec(Identifier.tryParse("minecraft:block_entity_data")));
		ModuleRegistry.Register(Identifier.tryParse("minecraft:bucket_entity_data"), ComponentDataModule.CreateLegacyCodec(Identifier.tryParse("minecraft:bucket_entity_data")));
		ModuleRegistry.Register(Identifier.tryParse("minecraft:component_data"), ComponentDataModule.CODEC);
		ModuleRegistry.Register(Identifier.tryParse("minecraft:component_format"), MultiComponentFormatModule.CODEC);
		ModuleRegistry.Register(Identifier.tryParse("minecraft:custom_data"), ComponentDataModule.CreateLegacyCodec(Identifier.tryParse("minecraft:custom_data")));
		ModuleRegistry.Register(Identifier.tryParse("minecraft:custom_name"), CustomNameModule.CODEC);
		ModuleRegistry.Register(Identifier.tryParse("minecraft:durability"), DurabilityModule.CODEC);
		ModuleRegistry.Register(Identifier.tryParse("minecraft:enchantment"), EnchantmentModule.CreateCodec(Identifier.tryParse("minecraft:enchantments")));
		ModuleRegistry.Register(Identifier.tryParse("minecraft:entity_data"), ComponentDataModule.CreateLegacyCodec(Identifier.tryParse("minecraft:entity_data")));
		ModuleRegistry.Register(Identifier.tryParse("minecraft:instrument"), new GoatHornModule());
		ModuleRegistry.Register(Identifier.tryParse("minecraft:item_count"), ItemCountModule.CODEC);
		ModuleRegistry.Register(Identifier.tryParse("minecraft:jukebox_playable"), new MusicDiscModule());
		ModuleRegistry.Register(Identifier.tryParse("minecraft:painting_variant"), new PaintingVariantModule());
		ModuleRegistry.Register(Identifier.tryParse("minecraft:potion_effect"), new PotionEffectModule());
		ModuleRegistry.Register(Identifier.tryParse("minecraft:potion_type"), new PotionTypeModule());
		ModuleRegistry.Register(Identifier.tryParse("minecraft:stored_enchantment"), EnchantmentModule.CreateCodec(Identifier.tryParse("minecraft:stored_enchantments")));
		ModuleRegistry.Register(Identifier.tryParse("minecraft:stored_enchantments"), MapCodec.unit(() -> {
			LOGGER.warn("Module name `stored_enchantments` (plural) is being deprecated. use `stored_enchantment` (singular) instead.");
			return new EnchantmentModule(Identifier.tryParse("minecraft:stored_enchantments"), Map.of(), Optional.empty());
		}));
		ModuleRegistry.Register(Identifier.tryParse("minecraft:trim"), new TrimModule());
		ModuleRegistry.Register(Identifier.tryParse("minecraft:trim_pattern"), new TrimPatternModule());
		ModuleRegistry.Register(Identifier.tryParse("minecraft:trim_material"), new TrimMaterialModule());

		ModelPredicateProviderRegistry.register(Identifier.tryParse("minecraft:bucket_entity_age"), new BucketAgePredicate());
		ModelPredicateProviderRegistry.register(Items.ENCHANTED_BOOK, Identifier.tryParse("minecraft:level"), new EnchantedBookLevelPredicate());
		var potionPredicate = new PotionLevelPredicate();
		ModelPredicateProviderRegistry.register(Items.POTION, Identifier.tryParse("minecraft:amplifier"), potionPredicate);
		ModelPredicateProviderRegistry.register(Items.SPLASH_POTION, Identifier.tryParse("minecraft:amplifier"), potionPredicate);
		ModelPredicateProviderRegistry.register(Items.LINGERING_POTION, Identifier.tryParse("minecraft:amplifier"), potionPredicate);
	}

	@Override
	public void onInitializeModelLoader(ModuleLoader.Result result, ModelLoadingPlugin.Context pluginContext){
		++reloadcount;
		result.modelAggregator.modelsToLoad.stream().map(id -> (Identifier)id).forEach(pluginContext::addModels);

		for (var e : result.uniqueModules.entrySet())
			LOGGER.info("Found {} variants for VCIT module {}", e.getValue().library().GetVariantCount(), e.getKey());

		AUTOGEN = result.modelAggregator.modelsToCreate;
		MODULES = new HashMap<>();
		for (var entry : result.modulesPerItem.entrySet()){
			MODULES.put(
				entry.getKey().value(),
				IItemModelProvider.OfList( entry.getValue().stream().map(meta->meta.bakedModule()).toList() )
			);
		}
	}

}
