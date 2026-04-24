package zov.dayn.effectNoKill;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerStateManager { // менеждер состояния игроков: изменяет флаги заморозки и урона 

    private final Plugin plugin; 
    private final Map<UUID, PlayerState> states = new ConcurrentHashMap<>(); // потокобезопасная задача состояний: UUID --> PlayerState
    private final BukkitTask freezeTask; //Повторяющая задача удержавающая замороденых игроков наместе

    public PlayerStateManager(Plugin plugin) {
        this.plugin = plugin; 
        // Запускаем задачу каждый тик для принудительной заморозки 
        this.freezeTask = Bukkit.getScheduler().runTaskTimer(plugin, this::enforceFrozenPlayers, 1L, 1L);
    }

    public PlayerState getState(Player player) { // возвращает состояние игрока, создавая новое если его еще нет
        return states.computeIfAbsent(player.getUniqueId(), ignored -> new PlayerState());
    }

    public PlayerState getExistingState(UUID uuid) { // возвращает состояние игрока или NULL если оно не создавалось 
        return states.get(uuid);
    }

    public void setStun(Player player, boolean freeze, boolean cancelDamage) { // применяет стан, заморозку и опционально отмену урона 
        PlayerState state = getState(player);
        state.setFrozen(freeze);
        state.setCancelDamage(cancelDamage);
        state.setBlockDamage(cancelDamage);
        if (freeze) {
            state.setFrozenLocation(player.getLocation().clone());
            player.setVelocity(player.getVelocity().zero());
        }
    }

    public void setNoDamage(Player player) { // блокирует урон без заморозки 
        PlayerState state = getState(player);
        state.setBlockDamage(true);
        state.setCancelDamage(false);
    }

    public void clearCombat(Player player) { // удаляет состояние игрока 
        states.remove(player.getUniqueId());
    }

    public void clearAll() { // останавливает задачу и удаляет все состояния 
        freezeTask.cancel();
        states.clear();
    }

    public boolean isFrozen(Player player) { //Проверяет заморожен ли игрок 
        PlayerState state = states.get(player.getUniqueId());
        return state != null && state.isFrozen() && state.getFrozenLocation() != null;
    }

    public boolean isDamageBlocked(Player player) {
        PlayerState state = states.get(player.getUniqueId());
        return state != null && state.isBlockDamage();
    }
    //Возвращает множитель урона
    public double getDamageMultiplier(Player player) {  
        PlayerState state = states.get(player.getUniqueId());
        return state == null ? 1.0D : state.getDamageMultiplier();
    }

    public void setDamageMultiplier(Player player, double multiplier) { 
        PlayerState state = getState(player);
        state.setDamageMultiplier(multiplier);
    }

    public void resetDamageMultiplier(Player player) { // сбрасывает множитель урона одного игрока до обычного состояния 
        PlayerState state = states.get(player.getUniqueId());
        if (state != null) {
            state.setDamageMultiplier(1.0D);
        }
    }

    public void resetAllDamageMultipliers() { //Сбрасывает множитель урона всех игроков до стандартного 
        for (PlayerState state : states.values()) {
            state.setDamageMultiplier(1.0D);
        }
    }

    public void clearAllStates() { //Удаляет все состояния без остановки задачи 
        states.clear();
    }

    public void unfreeze(Player player) { //Удаляет все эффекты с игрока(заморозки и урона)
        PlayerState state = states.get(player.getUniqueId());
        if (state != null) {
            state.setFrozen(false);
            state.setBlockDamage(false);
            state.setCancelDamage(false);
            state.setFrozenLocation(null);
        }
    }

    private void enforceFrozenPlayers() { //Сервер возвращает обратно замороженых игроков 
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerState state = states.get(player.getUniqueId());
            if (state == null || !state.isFrozen()) {
                continue;
            }


            Location frozenLocation = state.getFrozenLocation();
            //Если игрок сменил мир обновляем точку заморозки
            if (frozenLocation == null || !frozenLocation.getWorld().equals(player.getWorld())) {
                frozenLocation = player.getLocation().clone();
                state.setFrozenLocation(frozenLocation);
            }

            player.setVelocity(player.getVelocity().zero());
            player.teleport(frozenLocation);
        }
    }

    public Plugin getPlugin() {
        return plugin;
    }
}