package profhugo.nodami;

import com.mojang.logging.LogUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import profhugo.nodami.config.NoDamIConfig;
import profhugo.nodami.proxy.ClientProxy;
import profhugo.nodami.proxy.ServerProxy;

import java.util.stream.Collectors;


@Mod("nodami")
public class NoDamI {

	public static final String MODID = "nodami";
	public static final String NAME = "No Damage Immunity";
	public static final String VERSION = "1.3.2";

	private static final Logger LOGGER = LogUtils.getLogger();

	public NoDamI() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);

		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, NoDamIConfig.CONFIG_SPEC.getBaseSpec(), "nodami-server.toml");

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	public static ServerProxy proxy = DistExecutor.safeRunForDist(()-> ClientProxy::new, ()-> ServerProxy::new);
	private void setup(final FMLCommonSetupEvent event) {
		// Some preinit code
		System.out.println(NAME + " is loading!");
		proxy.preInit();
		proxy.init();
		proxy.postInit();
	}
	private void enqueueIMC(final InterModEnqueueEvent event) {
		// Some example code to dispatch IMC to another mod
		/*InterModComms.sendTo("nodami", "helloworld", () -> {
			LOGGER.info("Hello world from the MDK");
			return "Hello world";
		});*/
	}

	private void processIMC(final InterModProcessEvent event) {
		// Some example code to receive and process InterModComms from other mods
		LOGGER.info("Got IMC {}", event.getIMCStream().
				map(m -> m.messageSupplier().get()).
				collect(Collectors.toList()));
	}
}
