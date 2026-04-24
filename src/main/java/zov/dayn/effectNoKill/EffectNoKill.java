package zov.dayn.effectNoKill;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

// главный класс плагина
public final class EffectNoKill extends JavaPlugin {

    private PlayerStateManager stateManager;

    @Override
    public void onEnable() {
        // инициализируем менеджер состояний игроков и регистрируем слушателя событий боя и команды
        stateManager = new PlayerStateManager(this);
        getServer().getPluginManager().registerEvents(new CombatListener(stateManager), this);

        registerCommand("nokill", new NokillCommand(stateManager));
        registerCommand("upkill", new UpkillCommand(stateManager));
    }

    @Override
    public void onDisable() {
        // очищаем все состояния игроков при отключении плагина
        if (stateManager != null) {
            stateManager.clearAll();
        }
    }
    // вспомогательный метод
    private void registerCommand(String name, TabExecutor executor) {
        PluginCommand command = Objects.requireNonNull(getCommand(name), "Command /" + name + " is missing from plugin.yml");
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
