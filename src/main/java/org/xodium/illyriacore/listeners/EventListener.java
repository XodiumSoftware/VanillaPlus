package org.xodium.illyriacore.listeners;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import java.util.Map;
import java.util.Optional;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EventListener implements Listener {

    private final LuckPerms lp;
    private final Map<EntityType, String> entityPermMap;

    public EventListener(Map<EntityType, String> entityPermMap, LuckPerms lp) {
        this.lp = lp;
        this.entityPermMap = entityPermMap;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Optional.ofNullable(entityPermMap.get(e.getEntity().getType()))
                .ifPresent(nodeStr -> handleEntityDeath(e, nodeStr));
    }

    private void handleEntityDeath(EntityDeathEvent e, String nodeStr) {
        if (e.getEntity().getKiller() instanceof Player) {
            Optional.ofNullable(this.lp.getUserManager().getUser(e.getEntity().getKiller().getUniqueId()))
                    .ifPresent(usr -> addPermissionIfAbsent(usr, nodeStr));
        }
    }

    private void addPermissionIfAbsent(User usr, String nodeStr) {
        PermissionNode node = PermissionNode.builder(nodeStr).build();
        usr.data().add(node);
        lp.getUserManager().saveUser(usr).join();
    }
}
