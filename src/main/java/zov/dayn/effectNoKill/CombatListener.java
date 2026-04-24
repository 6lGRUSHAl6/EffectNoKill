package zov.dayn.effectNoKill;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;


// слушаетель событий боя, который обрабатывает урон, заморозку и выход игрока
public final class CombatListener implements Listener {

    private final PlayerStateManager stateManager;

    public CombatListener(PlayerStateManager stateManager) {
        this.stateManager = stateManager;
    }
//обработка входящего урона.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        PlayerState state = stateManager.getExistingState(player.getUniqueId());
        if (state != null && state.isCancelDamage()) {
            event.setCancelled(true);
            return;
        }

        if (state != null && state.isBlockDamage()) {
            event.setDamage(0.0D);
            return;
        }

        double multiplier = stateManager.getDamageMultiplier(player);
        if (multiplier != 1.0D) {
            event.setDamage(event.getDamage() * multiplier);
        }
    }
//блокировка перемещения замороженного игрока
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!stateManager.isFrozen(player) || event.getTo() == null) {
            return;
        }

        PlayerState state = stateManager.getExistingState(player.getUniqueId());
        if (state == null || state.getFrozenLocation() == null) {
            return;
        }

        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            event.setTo(state.getFrozenLocation().clone());
            return;
        }

        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {
            Location locked = state.getFrozenLocation().clone();
            locked.setYaw(event.getTo().getYaw());
            locked.setPitch(event.getTo().getPitch());
            event.setTo(locked);
        }
    }
// при выходе очищаем состояние
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stateManager.clearCombat(event.getPlayer());
    }
}