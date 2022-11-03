package io._3650.itemupgrader.api.type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

/**
 * An internal class containing generally shared code between {@linkplain UpgradeAction}, {@linkplain UpgradeCondition}, and {@linkplain UpgradeResult}
 * @author LegoMaster3650
 *
 */
public abstract class IUpgradeType {
	
	private final IUpgradeInternals internals;
	
	/**
	 * Constructs an {@linkplain IUpgradeType} using the given internals
	 * @param internals {@linkplain IUpgradeInternals} containing information for this type
	 */
	public IUpgradeType(IUpgradeInternals internals) {
		this.internals = internals;
	}
	
	/**
	 * Gets the tooltip components for the object applied to the tooltip defined in the language file
	 * @param stack The {@linkplain ItemStack} to get tooltip context from
	 * @return A list of {@linkplain MutableComponent}s to apply to the tooltip specified in the language file
	 */
	public abstract MutableComponent[] getTooltip(ItemStack stack);
	
	/**
	 * Here is what you put in this method copy and paste it:<br>
	 * {@code this.getSerializer().toNetwork(this, buf);}<br>
	 * <br>
	 * If it doesn't work make getSerializer return your own {@code Serializer} type<br>
	 * <br>
	 * This one sucks so much but java is bad so here I am<br>
	 * Why is this needed? Because java hates the wildcard typing and stuff or something so I'm letting you deal with it in your own classes to get it to shut up.
	 * @param buf The {@linkplain FriendlyByteBuf} to serialize this to
	 */
	public abstract void hackyToNetworkReadJavadoc(FriendlyByteBuf buf);
	
	/**
	 * Gets this object's internals
	 * @return The {@linkplain IUpgradeInternals} associated with this object
	 */
	public final IUpgradeInternals getInternals() {
		return this.internals;
	}
	
	/**
	 * Gets this object's ID
	 * @return The {@linkplain ResourceLocation} associated with this object
	 */
	public final ResourceLocation getId() {
		return this.internals.id;
	}
	
	/**
	 * The descriptor identifier for this upgrade type variant
	 * @return The descriptor identifier for this upgrade type variant
	 */
	@Nonnull
	protected abstract String descriptorIdBase();
	
	/**
	 * The descriptor id suffix for this upgrade type variant
	 * @return The descriptor id suffix for this upgrade type variant
	 */
	@Nullable
	protected String descriptorIdSuffix() {
		return null;
	}
	
	@Nullable
	private String descriptionId;
	
	/**
	 * Gets the unlocalized descriptor id for translation
	 * @return The unlocalized descriptor id for translation
	 */
	@Nonnull
	public final String getDescriptionId() {
		if (this.descriptionId == null) {
			this.descriptionId = Util.makeDescriptionId(this.descriptorIdBase(), this.getId());
			if (this.descriptorIdSuffix() != null) this.descriptionId += "." + this.descriptorIdSuffix();
		}
		return this.descriptionId;
	}
	
	/**
	 * Gets this object's visibility (used for tooltips)
	 * @return If this object is visible
	 */
	public final boolean isVisible() {
		return this.internals.visible;
	}
	
	/**
	 * Checks if this object has a tooltip override
	 * @return If this object has a tooltip override
	 */
	public final boolean hasTooltipOverride() {
		return this.internals.tooltipOverride != null;
	}
	
	/**
	 * Gets this object's tooltip override if present
	 * @return The {@linkplain String} representing the override's resource key, or {@code null} if not present
	 */
	@Nullable
	public final String getTooltipOverride() {
		return this.internals.tooltipOverride;
	}
	
	/**
	 * Bundles together all of the data in an {@linkplain IUpgradeType} to keep you from having to juggle around 3 variables you probably won't need<br>
	 * And if you do, feel free to get any of them from this.<br>
	 * <b>NOTE:</b> The methods in this record are mainly just quality of life things for internal use only, don't pay much attention to them
	 * @param id The {@linkplain ResourceLocation} associated with the object
	 * @param tooltipOverride A {@linkplain String} containing a translation key for an override if present, or {@code null} if not
	 * @param visible A {@code boolean} specifying whether to display the object in tooltips or not
	 * @author LegoMaster3650
	 *
	 */
	public static final record IUpgradeInternals(ResourceLocation id, @Nullable String tooltipOverride, boolean visible) {
		public static final IUpgradeInternals of(ResourceLocation id, JsonObject json) {
			return new IUpgradeInternals(id, GsonHelper.getAsString(json, "tooltip_override", null), GsonHelper.getAsBoolean(json, "visible", true));
		}
		
		public static final IUpgradeInternals of(ResourceLocation id, FriendlyByteBuf buf) {
			String override = null;
			if (buf.readBoolean()) {override = buf.readUtf();}
			boolean visibility = buf.readBoolean();
			return new IUpgradeInternals(id, override, visibility);
		}
		
		public final void to(FriendlyByteBuf buf) {
			boolean override = this.tooltipOverride != null;
			buf.writeBoolean(override);
			if (override) buf.writeUtf(this.tooltipOverride);
			buf.writeBoolean(this.visible);
		}
	}
	
}