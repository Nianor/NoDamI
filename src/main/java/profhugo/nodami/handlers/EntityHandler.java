package profhugo.nodami.handlers;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import profhugo.nodami.config.NoDamIConfig;

public class EntityHandler {

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onEntityHurt(LivingHurtEvent event) {
		if (!event.isCanceled()) {
			LivingEntity entity = event.getEntityLiving();

			if (entity.level.isClientSide()) {
				return;
			}
			DamageSource source = event.getSource();
			Entity trueSource = source.getDirectEntity();
			if (NoDamIConfig.debugMode.get() && entity instanceof Player) {
				String trueSourceName;
				if (trueSource != null && EntityType.getKey(trueSource.getType()) != null) {
					trueSourceName = EntityType.getKey(trueSource.getType()).toString();
				} else {
					trueSourceName = "null";
				}
				String message = String.format("Type of damage received: %s\nAmount: %.3f\nTrue Source (mob id): %s\n",
						source.getMsgId(), event.getAmount(), trueSourceName);
				TextComponent messageComponent = new TextComponent(message);
				(entity).sendMessage(messageComponent, entity.getUUID());
			}
			
			if (NoDamIConfig.excludePlayers.get() && entity instanceof Player) {
				return;
			}
			
			if (NoDamIConfig.excludeAllMobs.get() && !(entity instanceof Player)) {
				return;
			}
			
			ResourceLocation loc = EntityType.getKey(entity.getType());
			if (loc != null && NoDamIConfig.dmgReceiveExcludedEntities.get().contains(loc.toString())) {
				return;
			}
			
			// May have more DoTs missing in this list
			// Not anymore (/s)
			if (NoDamIConfig.damageSrcWhitelist.get().contains(source.getMsgId())) {
				return;
			}

			// Mobs that do damage on collusion but have no attack timer
			if (trueSource != null) {
				loc = EntityType.getKey(trueSource.getType());
				if (loc != null && NoDamIConfig.attackExcludedEntities.get().contains(loc.toString())) {
					return;
				}
			}
			
			
			entity.invulnerableTime = NoDamIConfig.iFrameInterval.get();
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerAttack(AttackEntityEvent event) {
		if (!event.isCanceled()) {
			Player player = event.getPlayer();
			if (player.level.isClientSide()) {
				return;
			}
			float str = player.getAttackStrengthScale(0);
			if (str <= NoDamIConfig.attackCancelThreshold.get()) {
				event.setCanceled(true);
				return;
			}
			if (str <= NoDamIConfig.knockbackCancelThreshold.get()) {
				// Don't worry, it's only magic
				player.hurtTime = -1;
			}
		}
	}


	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onKnockback(LivingKnockBackEvent event) {
		if (!event.isCanceled()) {
			Entity attacker = null;
			if(event.getEntity() instanceof LivingEntity) {
				attacker = ((LivingEntity) event.getEntity()).getCombatTracker().getLastEntry().getAttacker();
			}
			if (attacker != null && !attacker.level.isClientSide()) {
				// IT'S ONLY MAGIC
				if (attacker instanceof Player && ((Player) attacker).hurtTime == -1) {
					event.setCanceled(true);
					((Player) attacker).hurtTime = 0;
				}
			}
		}
	}
}
