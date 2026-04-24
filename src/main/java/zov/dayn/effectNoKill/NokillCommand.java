package zov.dayn.effectNoKill;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// команда /nokill для управления эффектами заморозки и блокировки урона
public final class NokillCommand implements TabExecutor {

    private final PlayerStateManager stateManager;

    public NokillCommand(PlayerStateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
        // заморозить игрока, убрать урон и откидывание
            case "stun" -> {
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден.");
                    return true;
                }
                stateManager.setStun(target, true, true);
                sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " заморожен и не получает урон.");
            }
            // заморозить игрока но урон оставить
            case "stundmg" -> {
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден.");
                    return true;
                }
                stateManager.setStun(target, true, false);
                sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " заморожен, но урон остаётся.");
            }
            // снять урон без заморозки
            case "nodmg" -> {
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден.");
                    return true;
                }
                stateManager.setNoDamage(target);
                sender.sendMessage(ChatColor.GREEN + "Игрок " + target.getName() + " больше не получает урон.");
            }
            // снять все эффекты с одного игрока
            case "stop" -> {
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден.");
                    return true;
                }
                stateManager.unfreeze(target);
                sender.sendMessage(ChatColor.GREEN + "Эффекты nokill для " + target.getName() + " остановлены.");
            }
            // снять эффекты со всех игроков
            case "stopall" -> {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    stateManager.unfreeze(onlinePlayer);
                }
                stateManager.clearAllStates();
                sender.sendMessage(ChatColor.GREEN + "Все эффекты nokill отключены.");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("stun", "stundmg", "nodmg", "stop", "stopall"), args[0]);
        }

        if (args.length == 2 && !"stopall".equalsIgnoreCase(args[0])) {
            return filter(onlinePlayerNames(), args[1]);
        }

        return Collections.emptyList();
    }
// Вывод справки по командам
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "/nokill stun <ник> - заморозить, убрать урон и откидывание");
        sender.sendMessage(ChatColor.YELLOW + "/nokill stundmg <ник> - заморозить, оставить урон");
        sender.sendMessage(ChatColor.YELLOW + "/nokill nodmg <ник> - убрать урон, оставить откидывание");
        sender.sendMessage(ChatColor.YELLOW + "/nokill stop <ник> - снять эффект с игрока");
        sender.sendMessage(ChatColor.YELLOW + "/nokill stopall - снять эффекты со всех игроков");
    }
// список игроков онлайн
    private List<String> onlinePlayerNames() {
        List<String> result = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            result.add(player.getName());
        }
        return result;
    }
// фильтрация списка по введеному префиксу
    private List<String> filter(List<String> values, String input) {
        if (input == null || input.isEmpty()) {
            return values;
        }

        String lowerInput = input.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lowerInput)) {
                result.add(value);
            }
        }
        return result;
    }
}