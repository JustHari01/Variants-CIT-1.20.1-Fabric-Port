package fr.estecka.variantscit;

import net.minecraft.util.Identifier;
import fr.estecka.variantscit.format.INbtInput;
import fr.estecka.variantscit.format.IStringTransform;
import fr.estecka.variantscit.format.properties.*;
import fr.estecka.variantscit.format.transforms.*;

public final class VCitRegistries
{
	static public final DecodableRegistry<IStringProperty> ITEM_PROPERTIES = new DecodableRegistry<>("property", new Identifier("minecraft", "item_component"), TransformableProperty::CodecOf);
	static public final DecodableRegistry<IStringTransform> TRANSFORMS = new DecodableRegistry<>("function", new Identifier("minecraft", "regex"), OptionalTransform::CodecOf);
	static public final DecodableRegistry<INbtInput> NBT_INPUTS = new DecodableRegistry<>("type");

	static {
		ITEM_PROPERTIES.RegisterUnit(new Identifier("minecraft", "axolotl_variant"), AxolotlVariantProperty.UNIT);
		ITEM_PROPERTIES.Register(new Identifier("minecraft", "bucket_entity_age"), EntityAgeMapProperty.MAP_CODEC, EntityAgeMapProperty.UNIT);
		ITEM_PROPERTIES.RegisterMap(new Identifier("minecraft", "item_component"), ItemComponentProperty.MAP_CODEC);
		ITEM_PROPERTIES.RegisterUnit(new Identifier("minecraft", "item_count"), new ItemCountProperty());
		ITEM_PROPERTIES.RegisterUnit(new Identifier("minecraft", "item_type"), new ItemTypeProperty());
		ITEM_PROPERTIES.RegisterUnit(new Identifier("minecraft", "painting_variant"), PaintingVariantProperty.UNIT);

		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "noop"),               IStringTransform.NOOP);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "null"),               IStringTransform.NULL);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "lowercase"),          String::toLowerCase);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "discard_path"),       IStringTransform::DiscardPath);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "discard_namespace"),  IStringTransform::DiscardNamespace);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "sanitize"),           IStringTransform.SANITIZE_LEGACY);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "sanitize_legacy"),    IStringTransform.SANITIZE_LEGACY);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "sanitize_path"),      IStringTransform.SANITIZE_PATH);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "sanitize_namespace"), IStringTransform.SANITIZE_NAMESPACE);
		TRANSFORMS.RegisterUnit(new Identifier("minecraft", "sanitize_auto"),      IStringTransform::AutoSanitize);
		TRANSFORMS.RegisterMap(new Identifier("minecraft", "test"),                TestTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(new Identifier("minecraft", "successive"),          SuccessiveTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(new Identifier("minecraft", "alternative"),         AlternativeTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(new Identifier("minecraft", "whitelist"),           FilterlistTransform.MAPCODEC_WHITELIST);
		TRANSFORMS.RegisterMap(new Identifier("minecraft", "blacklist"),           FilterlistTransform.MAPCODEC_BLACKLIST);
		TRANSFORMS.RegisterMap(new Identifier("minecraft", "charset_remap"),       CharRemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(new Identifier("minecraft", "remap"),               RemapTransform.MAPCODEC);
		TRANSFORMS.RegisterMap(new Identifier("minecraft", "regex"),               RegexTransform.MAPCODEC);

		NBT_INPUTS.RegisterUnit(new Identifier("minecraft", "auto"),            INbtInput.AUTO);
		NBT_INPUTS.RegisterUnit(new Identifier("minecraft", "primitive"),       INbtInput.PRIMITIVE);
		NBT_INPUTS.RegisterUnit(new Identifier("minecraft", "string"),          INbtInput::String);
		NBT_INPUTS.RegisterUnit(new Identifier("minecraft", "number"),          INbtInput::Number);
		NBT_INPUTS.RegisterUnit(new Identifier("minecraft", "identifier"),      INbtInput::Identifier);
		NBT_INPUTS.RegisterUnit(new Identifier("minecraft", "rich_text"),       INbtInput::RichText);
		NBT_INPUTS.RegisterUnit(new Identifier("minecraft", "rich_text_array"), INbtInput::RichTextArray);
		NBT_INPUTS.RegisterUnit(new Identifier("minecraft", "snbt"),            INbtInput::Snbt);
	}
}
