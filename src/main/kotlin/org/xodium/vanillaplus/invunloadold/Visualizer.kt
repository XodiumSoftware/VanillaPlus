/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import de.jeff_media.InvUnload.UnloadSummary.PrintRecipient
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import org.xodium.vanillaplus.invunloadold.BlockUtils
import org.xodium.vanillaplus.invunloadold.Main
import java.util.*

class Visualizer protected constructor(private val main: Main) {
    val lastUnloads: HashMap<UUID?, ArrayList<Block?>?>
    val lastUnloadPositions: HashMap<UUID?, Location?>
    val activeVisualizations: HashMap<UUID?, Int?>

    //HashMap<UUID,ArrayList<Laser>> activeLasers;
    val unloadSummaries: HashMap<UUID?, UnloadSummary?>


    //ArrayList<Location> destinations;
    //Player p;
    // Barrier: okay
    init {
        lastUnloads = HashMap<UUID?, ArrayList<Block?>?>()
        lastUnloadPositions = HashMap<UUID?, Location?>()
        activeVisualizations = HashMap<UUID?, Int?>()
        //activeLasers = new HashMap<UUID,ArrayList<Laser>>();
        unloadSummaries = HashMap<UUID?, UnloadSummary?>()

        if (main.getConfig().getBoolean("laser-moves-with-player")) {
            /*Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {

			short timer=0;
			@Override
			public void run() {
				
				if(activeLasers==null) return;
				if(activeLasers.size()==0) return;
				
				
				timer++;
				
				for(Entry<UUID,ArrayList<Laser>> entry : activeLasers.entrySet()) {
					Player p = main.getServer().getPlayer(entry.getKey());
					if(p==null) {
						stopLaser(entry.getKey());
						continue;
					}
					
					//ArrayList<Block> lastUnload = lastUnloads.get(entry.getKey());
					for(Laser laser : entry.getValue()) {
						try {
							if(!laser.isStarted()) {
								stopLaser(p.getUniqueId());
								break;
							}
							laser.moveStart(p.getLocation().add(0, 0.75, 0));
							if(timer>50) {
								laser.callColorChange();

							}
						} catch (ReflectiveOperationException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
					}
					if(timer>50) timer=0;
				}
				
			}
			
		}, 0, 2);*/
        }
    }

    fun printSummaryToPlayer(p: Player) {
        val summary: UnloadSummary? = unloadSummaries.get(p.uniqueId)
        if (summary == null) return
        summary.print(PrintRecipient.PLAYER, p)
    }

    fun cancelVisualization(id: Int) {
        Bukkit.getScheduler().cancelTask(id)
    }

    fun cancelVisualization(p: Player) {
        if (activeVisualizations.containsKey(p.uniqueId)) {
            cancelVisualization(activeVisualizations.get(p.uniqueId)!!)
        }
        activeVisualizations.remove(p.uniqueId)
    }

    fun save(p: Player, affectedChests: ArrayList<Block?>?, summary: UnloadSummary?) {
        lastUnloads.put(p.uniqueId, affectedChests)
        lastUnloadPositions.put(p.uniqueId, p.location.add(0.0, 0.75, 0.0))
        unloadSummaries.put(p.uniqueId, summary)
        /*if(activeVisualizations.containsKey(p.getUniqueId())) {
			cancelVisualization(activeVisualizations.get(p.getUniqueId()));
			//play(p);
		}*/
    }

    fun play(p: Player) {
        if (lastUnloads.containsKey(p.uniqueId)) {
            play(lastUnloads.get(p.uniqueId), p)
        }
    }

    fun play(
        destinations: ArrayList<Block>,
        p: Player,
        interval: Double,
        count: Int,
        particle: Particle,
        speed: Double,
        maxDistance: Int
    ) {
        for (destination in destinations) {
            val start = p.location
            val vec: Vector =
                getDirectionBetweenLocations(start, BlockUtils.getCenterOfBlock(destination).add(0, -0.5, 0))
            if (start.distance(destination.location) < maxDistance) {
                var i = 1.0
                while (i <= start.distance(destination.location)) {
                    vec.multiply(i)
                    start.add(vec)
                    p.spawnParticle(particle, start, count, 0.0, 0.0, 0.0, speed)
                    start.subtract(vec)
                    vec.normalize()
                    i += interval
                }
            }
        }
    }

    /*void toggleLaser(Player p,int duration) {
		if(lastUnloads.containsKey(p.getUniqueId())
				&& lastUnloads.get(p.getUniqueId()).size()>0 
				& !activeLasers.containsKey(p.getUniqueId())) {
			playLaser(lastUnloads.get(p.getUniqueId()),p,duration);
		} else {
			stopLaser(p.getUniqueId());
		}
	}*/
    /*void stopLaser(UUID p) {
		ArrayList<Laser> lasers = activeLasers.get(p);
		if(lasers==null) return;
		for(Laser laser : lasers) {
			if(laser.isStarted()) laser.stop();
			laser = null;
		}
		lasers = null;
		activeLasers.remove(p);
	}*/
    /*void playLaser(ArrayList<Block> affectedChests,Player p,int duration) {
		stopLaser(p.getUniqueId());
		ArrayList<Laser> lasers = new ArrayList<Laser>();
		Location loc = lastUnloadPositions.get(p.getUniqueId());
		for(Block block : affectedChests) {
			try {
				Laser laser = new Laser(loc, BlockUtils.getCenterOfBlock(block).add(0, -1, 0), duration, main.getConfig().getInt("laser-max-distance"));
				laser.start(main);
				lasers.add(laser);
			} catch (ReflectiveOperationException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				main.getLogger().warning("Could not start laser for player "+p.getName());
			}
		}
		if(lasers.size()>0) {
			activeLasers.put(p.getUniqueId(), lasers);
		}
	}*/
    fun play(affectedChests: ArrayList<Block>, p: Player) {
        // Visualize
        // TODO: Move the declarations out of the Runnable
        val particle =
            Particle.valueOf(main.getConfig().getString("laser-particle", "CRIT")!!.uppercase(Locale.getDefault()))
        //Particle particle = Particle.CRIT;
        val count = main.getConfig().getInt("laser-count", 1)
        val maxDistance = main.getConfig().getInt("laser-max-distance", 128)
        val interval = main.getConfig().getDouble("laser-interval", 0.3)
        val speed = main.getConfig().getDouble("laser-speed", 0.001)

        val task = Bukkit.getServer().scheduler.scheduleSyncRepeatingTask(
            main,
            Runnable { play(affectedChests, p, interval, count, particle, speed, maxDistance) },
            0,
            2
        )

        activeVisualizations.put(p.uniqueId, task)

        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getServer().scheduler.cancelTask(task)
                activeVisualizations.remove(p.uniqueId)
            }
        }.runTaskLater(main, 100)
    }

    fun chestAnimation(block: Block?, player: Player) {
        val loc = BlockUtils.getCenterOfBlock(block!!)

        if (main.getConfig().getBoolean("spawn-particles")) {
            if (main.getConfig().getBoolean("error-particles")) {
                main.logger.warning(
                    "Cannot spawn particles, because particle type \"" + main.getConfig()
                        .getString("particle-type") + "\" does not exist! Please check your config.yml"
                )
            } else {
                val particleCount = main.getConfig().getInt("particle-count")
                val particle =
                    Particle.valueOf(main.getConfig().getString("particle-type")!!.uppercase(Locale.getDefault()))
                player.spawnParticle(particle, loc, particleCount, 0.0, 0.0, 0.0)
            }
        }
    }

    companion object {
        private fun getDirectionBetweenLocations(start: Location, end: Location): Vector {
            return end.toVector().subtract(start.toVector())
        }
    }
}
