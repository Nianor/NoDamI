package profhugo.nodami.config;

import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import profhugo.nodami.NoDamI;

import java.util.*;

//Also from IE, once again, thank you Blu
@EventBusSubscriber(modid = NoDamI.MODID, bus=Bus.MOD)
public class NoDamIConfig {

    public static final CachedConfig.IntValue iFrameInterval;
    public static final CachedConfig.BooleanValue excludePlayers;

    public static final CachedConfig.BooleanValue excludeAllMobs;

    public static final CachedConfig.DoubleValue attackCancelThreshold;

    public static final CachedConfig.DoubleValue knockbackCancelThreshold;

    public static final CachedConfig.ConfigValue<List<? extends String>> attackExcludedEntities;

    public static final CachedConfig.ConfigValue<List<? extends String>> dmgReceiveExcludedEntities;

    public static final CachedConfig.ConfigValue<List<? extends String>> damageSrcWhitelist;

    public static final CachedConfig.BooleanValue debugMode;

    public static final Map<String, CachedConfig.BooleanValue> compat = new HashMap<>();


    public static final CachedConfig CONFIG_SPEC;

    private static Config rawConfig;

    public static Config getRawConfig()
    {
        return Preconditions.checkNotNull(rawConfig);
    }

    static
    {
        CachedConfig.Builder builder = new CachedConfig.Builder();

        builder.push("CONFIG FOR NODAMI:");
        iFrameInterval = builder
                .comment("How many ticks of i-frames does an entity get when damaged, from 0 (default), to 2^31-1 (nothing can take damage)")
                .defineInRange("iFrameInterval",0,0,Integer.MAX_VALUE);

        excludePlayers = builder
                .comment("Are players excluded from this mod (if true, players will always get 10 ticks of i-frames on being damaged")
                .define("excludePlayers",false);

        excludeAllMobs = builder
                .comment("Are all mobs excluded from this mod (if true, mobs will always get 10 ticks of i-farmes on being damaged")
                .define("excludeAllMobs",false);

        attackCancelThreshold = builder
                .comment("How weak a player's attack can be before it gets nullified, from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, players cannot attack), or -0.1 (disables this feature)")
                .defineInRange("attackCancelThreshold",0.1, -0.1, 1);

        knockbackCancelThreshold = builder
                .comment("How weak a player's attack can be before the knockback gets nullified, from 0 (0%, cancels multiple attacks on the same tick) to 1 (100%, no knockback), or -0.1 (disables this feature)")
                .defineInRange("knockbackCancelThreshold",0.75f, -0.1f, 1);

        attackExcludedEntities = builder
                .comment("List of entities that need to give i-frames on attacking")
                .defineList("attackExcludedEntities", Arrays.asList("minecraft:slime", "tconstruct:blueslime", "thaumcraft:thaumslime"), obj -> true);

        dmgReceiveExcludedEntities = builder
                .comment("List of entities that need to receive i-frames on receiving attacks or relies on iFrames")
                .defineList("dmgRecieveExcludedEntities", Arrays.asList(new String[0]), obj -> true);

        damageSrcWhitelist = builder
                .comment("List of damage sources that need to give i-frames on doing damage (ex: lava).")
                .defineList("damageSrcWhitelist", Arrays.asList("inFire", "lava", "cactus", "lightningBolt", "inWall", "hotFloor"), obj -> true);
        debugMode = builder
                .comment("If true, turns on feature which sends a message when a player receives damage, containing information such as the name of the source and the quantity. Use this to find the name of the source you need to whitelist, or the id of the mob you want to exclude.")
                .define("debugMode", true);
        builder.pop();
        //builder.pop();
        /*builder.comment("A list of all mods that IE has integrated compatability for", "Setting any of these to false disables the respective compat")
                .push("compat");
        for(String mod : IECompatModules.getAvailableModules())
            compat.put(mod, builder.define(mod, true));
        builder.pop();*/ //Leaving this here as an example if I wanna do more proactive entity and damage source listing
        CONFIG_SPEC=builder.build();
    }


    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent ev)
    {
        if(CONFIG_SPEC.reloadIfMatched(ev, Type.SERVER))
        {
            rawConfig = ev.getConfig().getConfigData();
            refresh();
        }
    }
    public static void refresh() {
        //This is where we should sync client values to server values
    }
}
