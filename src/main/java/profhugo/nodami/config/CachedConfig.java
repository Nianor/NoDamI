package profhugo.nodami.config;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import profhugo.nodami.NoDamI;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
//Code from Immersive Engineering, thank you IE.
public class CachedConfig {
	private final List<ConfigValue<?>> value;
	private final ForgeConfigSpec spec;

	private CachedConfig(List<ConfigValue<?>> value, ForgeConfigSpec spec)
	{
		this.value = value;
		this.spec = spec;
	}

	public void refreshCached()
	{
		value.forEach(ConfigValue::refresh);
	}

	public boolean reloadIfMatched(ModConfigEvent ev, Type configType)
	{
		if(ev.getConfig().getModId().equals(NoDamI.MODID)&&ev.getConfig().getType()==configType)
		{
			refreshCached();
			return true;
		}
		return false;
	}

	public ForgeConfigSpec getBaseSpec()
	{
		return spec;
	}

	public static class ConfigValue<T> implements Supplier<T>
	{
		private final ForgeConfigSpec.ConfigValue<T> baseValue;
		private T cached;

		public ConfigValue(CachedConfig.Builder builder, ForgeConfigSpec.ConfigValue<T> baseValue)
		{
			this.baseValue = baseValue;
			builder.values.add(this);
		}

		@Nonnull
		public T get()
		{
			return Objects.requireNonNull(cached);
		}

		public void refresh()
		{
			cached = baseValue.get();
		}

		public ForgeConfigSpec.ConfigValue<T> getBase()
		{
			return baseValue;
		}

		public T getOr(T valueDuringStartup)
		{
			if(cached!=null)
				return cached;
			else
				return valueDuringStartup;
		}

		public T getOrDefault()
		{
			return getOr(baseValue.get());
		}
	}

	public static class IntValue extends ConfigValue<Integer>
	{
		IntValue(Builder builder, ForgeConfigSpec.IntValue base)
		{
			super(builder, base);
		}
	}

	public static class DoubleValue extends ConfigValue<Double> implements DoubleSupplier
	{
		DoubleValue(Builder builder, ForgeConfigSpec.DoubleValue base)
		{
			super(builder, base);
		}

		@Override
		public double getAsDouble()
		{
			return get();
		}
	}

	public static class BooleanValue extends ConfigValue<Boolean>
	{
		BooleanValue(Builder builder, ForgeConfigSpec.BooleanValue base)
		{
			super(builder, base);
		}
	}

	public static class EnumValue<T extends Enum<T>> extends ConfigValue<T>
	{
		EnumValue(Builder builder, ForgeConfigSpec.EnumValue<T> base)
		{
			super(builder, base);
		}
	}

	public static class Builder
	{
		private final ForgeConfigSpec.Builder inner;
		private final List<ConfigValue<?>> values = new ArrayList<>();

		public Builder()
		{
			this.inner = new ForgeConfigSpec.Builder();
		}

		public Builder comment(String... comment)
		{
			inner.comment(comment);
			return this;
		}

		public Builder worldRestart()
		{
			inner.worldRestart();
			return this;
		}

		public Builder push(String... names)
		{
			inner.push(ImmutableList.copyOf(names));
			return this;
		}

		public Builder pop()
		{
			inner.pop();
			return this;
		}

		public Builder pop(int count)
		{
			inner.pop(count);
			return this;
		}

		public <T> ConfigValue<T> define(String name, T defValue)
		{
			return new ConfigValue<>(this, inner.define(name, defValue));
		}

		public BooleanValue define(String name, boolean def)
		{
			return new BooleanValue(this, inner.define(name, def));
		}

		public DoubleValue defineInRange(String name, double def, double min, double max)
		{
			return new DoubleValue(this, inner.defineInRange(name, def, min, max));
		}

		public IntValue defineInRange(String name, int def, int min, int max)
		{
			return new IntValue(this, inner.defineInRange(name, def, min, max));
		}

		public <T extends Enum<T>> EnumValue<T> defineEnum(String name, T defaultValue)
		{
			return new EnumValue<>(this, inner.defineEnum(name, defaultValue));
		}

		public <T> ConfigValue<List<? extends T>> defineList(String path, List<? extends T> defaultValue, Predicate<Object> elementValidator)
		{
			return new ConfigValue<>(this, inner.defineList(path, defaultValue, elementValidator));
		}

		public CachedConfig build()
		{
			return new CachedConfig(values, inner.build());
		}

		public Builder push(ImmutableList<String> of)
		{
			inner.push(of);
			return this;
		}
	}
/* //Disabling all as this system has completely changed
	private static Configuration config = null;

	public static int iFrameInterval;
	public static boolean excludePlayers, excludeAllMobs, debugMode;
	public static float attackCancelThreshold, knockbackCancelThreshold;
	
	public static HashSet<String> attackExcludedEntities, dmgReceiveExcludedEntities, damageSrcWhitelist;
	
	public static void preInit() {
		File configFile = new File(Loader.instance().getConfigDir(), "nodami.cfg");
		config = new Configuration(configFile);
		syncConfig();

	}

	public static Configuration getConfig() {
		return config;
	}
	
	private static void syncConfig() {
		iFrameInterval = config.getInt("iFrameInterval", "core", 0, 0, Integer.MAX_VALUE, "How many ticks of i-frames does an entity get when damaged, from 0 (default), to 2^31-1 (nothing can take damage)");
		excludePlayers = config.getBoolean("excludePlayers", "core", false, "Are players excluded from this mod (if true, players will always get 10 ticks of i-frames on being damaged");
		excludeAllMobs = config.getBoolean("excludeAllMobs", "core", false, "Are all mobs excluded from this mod (if true, mobs will always get 10 ticks of i-farmes on being damaged");
		
		attackCancelThreshold = config.getFloat("attackCancelThreshold", "thresholds", 0.1f, -0.1f, 1, "How weak a player's attack can be before it gets nullified, from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, players cannot attack), or -0.1 (disables this feature)");
		knockbackCancelThreshold = config.getFloat("knockbackCancelThreshold", "thresholds", 0.75f, -0.1f, 1, "How weak a player's attack can be before the knockback gets nullified, from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, no knockback), or -0.1 (disables this feature)");
		
		attackExcludedEntities = new HashSet<>(Arrays.asList(config.getStringList("attackExcludedEntities", "exclusions", new String[] {"minecraft:slime", "tconstruct:blueslime", "thaumcraft:thaumslime"}, "List of entities that need to give i-frames on attacking")));
		dmgReceiveExcludedEntities = new HashSet<>(Arrays.asList(config.getStringList("damageReceiveExcludedEntities", "exclusions", new String[0], "List of entities that need to receive i-frames on receiving attacks or relies on iFrames")));
		damageSrcWhitelist = new HashSet<>(Arrays.asList(config.getStringList("dmgSrcWhitelist", "exclusions", new String[] {"inFire", "lava", "cactus", "lightningBolt", "inWall", "hotFloor"}, "List of damage sources that need to give i-frames on doing damage (ex: lava).")));
		
		debugMode = config.getBoolean("debugMode", "misc", false, "If true, turns on feature which sends a message when a player receives damage, containing information such as the name of the source and the quantity. Use this to find the name of the source you need to whitelist, or the id of the mob you want to exclude.");
		
		if (config.hasChanged()) {
            config.save();
        }
	}
*/
}
