package me.Pride.korra.IceDiscs;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class IceDiscsListener implements Listener {
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!event.isSneaking()) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
		if (bPlayer != null && bPlayer.canBend(CoreAbility.getAbility("IceDiscs")) && CoreAbility.getAbility(event.getPlayer(), IceDiscs.class) == null) {
			new IceDiscs(event.getPlayer());
		}
	}

	@EventHandler
	public void onSwing(PlayerAnimationEvent event) {
		if (event.isCancelled()) {
			return;
		}
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
		if (bPlayer == null) {
			return;
		} else if (!bPlayer.getBoundAbilityName().equals(CoreAbility.getAbility(IceDiscs.class).getName())) {
			return;
		}

		if (CoreAbility.hasAbility(event.getPlayer(), IceDiscs.class)) {
			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1F, 1F);
			IceDiscs iceDiscs = CoreAbility.getAbility(event.getPlayer(), IceDiscs.class);
			iceDiscs.iceDisc();
		}
	}

}
