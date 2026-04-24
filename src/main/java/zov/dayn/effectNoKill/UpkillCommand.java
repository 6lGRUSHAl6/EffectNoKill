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

//Команда /upkill для управлением множителем урона
public final class UpkillCommand implements TabExecutor {

    private final PlayerStateManager stateManager;

    public UpkillCommand(PlayerStateManager stateManager) {
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
            case "updmg" -> { // увеличить входящий урон игрока в X раз 
                if (args.length < 3) {
                    sendHelp(sender);
                    return true;
                }

                Integer multiplier = parseMultiplier(sender, args[1]);
                if (multiplier == null) {
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден.");
                    return true;
                }

                stateManager.setDamageMultiplier(target, multiplier);
                sender.sendMessage(ChatColor.GREEN + "Урон игрока " + target.getName() + " увеличен в " + multiplier + " раз.");
            }
            case "downdmg" -> { // уменьшить входящий урон в X раз 
                if (args.length < 3) {
                    sendHelp(sender);
                    return true;
                }

                Integer multiplier = parseMultiplier(sender, args[1]);
                if (multiplier == null) {
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден.");
                    return true;
                }

                stateManager.setDamageMultiplier(target, 1.0D / multiplier);
                sender.sendMessage(ChatColor.GREEN + "Урон игрока " + target.getName() + " уменьшен в " + multiplier + " раз.");
            }
            case "stop" -> { // сбросить множитель для одного игрока
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Игрок не найден.");
                    return true;
                }

                stateManager.resetDamageMultiplier(target);
                sender.sendMessage(ChatColor.GREEN + "Прежний урон игрока " + target.getName() + " восстановлен.");
            }
            case "stopall" -> { // сбросить множитель урона для всех игроков
                stateManager.resetAllDamageMultipliers();
                sender.sendMessage(ChatColor.GREEN + "Прежний урон восстановлен всем игрокам.");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("updmg", "downdmg", "stop", "stopall"), args[0]);
        }

        if (args.length == 2 && ("updmg".equalsIgnoreCase(args[0]) || "downdmg".equalsIgnoreCase(args[0]))) {
            return filter(List.of("2", "3", "4", "5"), args[1]);
        }

        if (args.length == 3 && !"stopall".equalsIgnoreCase(args[0])) {
            return filter(onlinePlayerNames(), args[2]);
        }

        return Collections.emptyList();
    }
// выводит справку по команде
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "/upkill updmg <2-5> <ник> - увеличить урон");
        sender.sendMessage(ChatColor.YELLOW + "/upkill downdmg <2-5> <ник> - уменьшить урон");
        sender.sendMessage(ChatColor.YELLOW + "/upkill stop <ник> - вернуть старый урон игроку");
        sender.sendMessage(ChatColor.YELLOW + "/upkill stopall - вернуть урон всем игрокам");
    }

    private Integer parseMultiplier(CommandSender sender, String rawValue) {
        try {
            int value = Integer.parseInt(rawValue);
            if (value < 2 || value > 5) {
                sender.sendMessage(ChatColor.RED + "Множитель должен быть от 2 до 5.");
                return null;
            }
            return value;
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "Множитель должен быть числом от 2 до 5.");
            return null;
        }
    }

    private List<String> onlinePlayerNames() { // список имён игроков для автодополнения
        List<String> result = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            result.add(player.getName());
        }
        return result;
    }

    private List<String> filter(List<String> values, String input) { // фильтрация по введённому префиксу
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