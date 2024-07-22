package com.juliohf.windcharge;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WindChargePlugin extends JavaPlugin implements Listener {


    private double explosionForce;
    private Particle particleType;
    private boolean addParticles;
    private double projectileSpeed;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        loadConfigValues();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void loadConfigValues() {
        explosionForce = getConfig().getDouble("explosionForce", 4.0);
        addParticles = getConfig().getBoolean("addParticles", true);
        projectileSpeed = getConfig().getDouble("projectileSpeed", 1.0);

        try {
            particleType = Particle.valueOf(getConfig().getString("particleType", "GUST").toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Partícula invalida, retornando para GUST");
            particleType = Particle.GUST;
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof WindCharge) {
            WindCharge windcharge = (WindCharge) event.getEntity();

            Vector velocity = windcharge.getVelocity().normalize().multiply(projectileSpeed);
            windcharge.setVelocity(velocity);

            if (addParticles) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!windcharge.isDead() && windcharge.isValid()) {
                            windcharge.getWorld().spawnParticle(particleType, windcharge.getLocation(), 10, 0.2, 0.2, 0.2, 0.05);
                        } else {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(this, 0L, 1L);
            }
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.HAND) {
            Player player = event.getPlayer();
            ItemStack item = player.getInventory().getItemInMainHand();

            if (item.getType().toString().equals("WIND_CHARGE")) {

                int newAmount = item.getAmount() - 1;
                if (newAmount <= 0) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    item.setAmount(newAmount);
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof WindCharge) {
            event.setCancelled(true);
            entity.getWorld().createExplosion(entity.getLocation(), (float) explosionForce);
        }

    }
}