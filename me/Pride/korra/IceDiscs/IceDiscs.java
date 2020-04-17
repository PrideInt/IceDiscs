package me.Pride.korra.IceDiscs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import net.md_5.bungee.api.ChatColor;

public class IceDiscs extends IceAbility implements AddonAbility {
	
	private static String path = "ExtraAbilities.Prride.IceDiscs.";
	FileConfiguration config = ConfigManager.getConfig();
	
	private ArrayList<Discs> iceDiscs = new ArrayList<Discs>();
	
	private long cooldown;
	private double discs;
	private double selectRange;
	
	private int discCount;
	public boolean selected;
	
	private Block sourceBlock;
	private static Location origin;

	public IceDiscs(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		cooldown = config.getLong(path + "Cooldown");
		selectRange = config.getDouble(path + "SelectRange");
		discs = config.getDouble(path + "Discs");
		
		sourceBlock = player.getTargetBlock((Set<Material>) null, (int) selectRange);
		
		if (sourceBlock != null && !GeneralMethods.isRegionProtectedFromBuild(this, sourceBlock.getLocation()) &&
				sourceBlock.getType() == Material.ICE || sourceBlock.getType() == Material.PACKED_ICE || sourceBlock.getType() == Material.BLUE_ICE) {
			
			origin = sourceBlock.getLocation().add(0, 1, 0);
			
			selected = true;
			
			start();
		}
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "IceDiscs";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public void progress() {
		if (player.isSneaking()) {
			if (player.getLocation().distance(origin) < selectRange) {
				if (!bPlayer.canBendIgnoreBinds(this)) {
					remove();
					return;
				}
				
				if (!player.isOnline() || player.isDead()) {
					remove();
					return;
				}
				
				ParticleEffect.BLOCK_CRACK.display(sourceBlock.getLocation(), 5, 0.3f, 0.3f, 0.3f, 0, Material.ICE.createBlockData());
				
				for (Iterator<Discs> discs = iceDiscs.iterator(); discs.hasNext(); ) {
					
					Discs d = discs.next();
					d.progress();
					
					if (d.isOutOfRange() || d.isSolid() || d.aroundEntities()) {
						discs.remove();
					}
				}
			} else {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
		} else {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
	}
	
	public void iceDisc() {
		
		if (discCount <= discs) {
			iceDiscs.add(new Discs(this, player));
		
			discCount++;
			
		} else {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
	}

	@Override
	public String getDescription() {
		return Element.WATER.getColor() + "Waterbenders are able to generate small ice discs from ice blocks that causes piercing damage!";
	}
	
	@Override
	public String getInstructions() {
		return ChatColor.GOLD + "To use, hold sneak on an ice block and left click. Stepping away from a few blocks will cause the move to cancel.";
	}

	@Override
	public String getAuthor() {
		return Element.WATER.getColor() + "" + ChatColor.UNDERLINE + 
				"Prride, BlueHiddenWolf & Shookified";
	}

	@Override
	public String getVersion() {
		return Element.WATER.getColor() + "" + ChatColor.UNDERLINE + 
				"VERSION 1";
	}
	
	public static class Discs {

		private IceDiscs iceDiscs;
		private Player player;

		private double damage;
		private double range;
		private boolean moveable;
		private boolean slow;
		private long slowDuration;
		private int slowPower;
		private double speed;
		
		private int rotation;
		private boolean aroundEntities;
		private int potDuration;
		
		private Location location;
		private Vector direction;

		public Discs(IceDiscs iceDiscs, Player player) {
			this.iceDiscs = iceDiscs;
			this.player = iceDiscs.player;
			this.location = origin.clone();
			this.direction = player.getEyeLocation().getDirection().normalize();
			
			this.damage = ConfigManager.getConfig().getDouble(path + "Damage");
			this.range = ConfigManager.getConfig().getDouble(path + "Range");
			this.moveable = ConfigManager.getConfig().getBoolean(path + "Moveable");
			this.slow = ConfigManager.getConfig().getBoolean(path + "Slow");
			this.slowDuration = ConfigManager.getConfig().getInt(path + "SlowDuration");
			this.slowPower = ConfigManager.getConfig().getInt(path + "SlowPower");
			this.speed = ConfigManager.getConfig().getDouble(path + "Speed");
			
			this.potDuration = Math.toIntExact((slowDuration * 1000) / 50);
		}

		public void progress() {
				
			if (moveable) {
				direction = player.getEyeLocation().getDirection().normalize();
			}
			location.add(direction.clone().multiply(speed));
			
			ParticleEffect.BLOCK_CRACK.display(location, 5, 0f, 0f, 0f, 0, Material.ICE.createBlockData());
			
			rotation++;
			
			for (int i = -180; i < 180; i += 20) {
		        double angle = i * 3.141592653589793D / 180.0D;
		        double x = 1 * Math.cos(angle + rotation);
		        double z = 1 * Math.sin(angle + rotation);
		        Location loc = location.clone();
		        loc.add(x, 0, z);
		        ParticleEffect.BLOCK_CRACK.display(loc, 5, 0f, 0f, 0f, 0, Material.ICE.createBlockData());
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1)) {
				if ((entity instanceof LivingEntity) && !entity.getUniqueId().equals(player.getUniqueId())) {
					DamageHandler.damageEntity(entity, damage, iceDiscs);
					
					aroundEntities = true;
					
					if (slow) {
						
						((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, potDuration, slowPower));
					}
				}
			}
		}
		
		public boolean isSolid() {
			return GeneralMethods.isSolid(location.getBlock());
		}
		
		public boolean isOutOfRange() {
			return player.getEyeLocation().distance(location) > range;
		}
		
		public boolean aroundEntities() {
			return aroundEntities;
		}
	}

	@Override
	public void load() {
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new IceDiscsListener(), ProjectKorra.plugin);
		ProjectKorra.log.info(getName() + " " + getVersion() + " by " + getAuthor() + " loaded! ");
		
		ConfigManager.getConfig().addDefault(path + "Cooldown", 8000);
		ConfigManager.getConfig().addDefault(path + "SelectRange", 3);
		ConfigManager.getConfig().addDefault(path + "Range", 20);
		ConfigManager.getConfig().addDefault(path + "Discs", 5);
		ConfigManager.getConfig().addDefault(path + "Damage", 4);
		ConfigManager.getConfig().addDefault(path + "Moveable", true);
		ConfigManager.getConfig().addDefault(path + "Slow", true);
		ConfigManager.getConfig().addDefault(path + "SlowDuration", 2.5);
		ConfigManager.getConfig().addDefault(path + "SlowPower", 1);
		ConfigManager.getConfig().addDefault(path + "Speed", 1.5);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info(getName() + " " + getVersion() + " by " + getAuthor() + " stopped! ");
		super.remove();
	}

}
