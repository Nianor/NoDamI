package profhugo.nodami.proxy;

import net.minecraftforge.common.MinecraftForge;
import profhugo.nodami.handlers.EntityHandler;

public class ServerProxy {

	public void preInit() {
		//CachedConfig.preInit();
		//I don't *think* I need this anymore, but we'll see
	}

	public void init() {
		MinecraftForge.EVENT_BUS.register(new EntityHandler());
	}

	public void postInit() {

	}

}
