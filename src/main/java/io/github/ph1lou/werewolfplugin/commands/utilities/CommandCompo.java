package io.github.ph1lou.werewolfplugin.commands.utilities;

import io.github.ph1lou.werewolfapi.Commands;
import io.github.ph1lou.werewolfapi.WereWolfAPI;
import io.github.ph1lou.werewolfapi.enums.Category;
import io.github.ph1lou.werewolfapi.enums.ConfigsBase;
import io.github.ph1lou.werewolfapi.enums.LoverType;
import io.github.ph1lou.werewolfplugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public class CommandCompo implements Commands {


    private final Main main;

    public CommandCompo(Main main) {
        this.main = main;
    }

    @Override
    public void execute(Player player, String[] args) {

        WereWolfAPI game = main.getWereWolfAPI();


        if (game.getConfig().isConfigActive(ConfigsBase.HIDE_COMPOSITION.getKey())) {

            player.sendMessage(game.translate("werewolf.commands.compo.composition_hide"));
        } else {

            StringBuilder sb = new StringBuilder(game.translate("werewolf.commands.compo._"));
            sb.append(ChatColor.WHITE);
            if (game.getConfig().getLoverSize() > 0) {
                sb.append(LoverType.LOVER.getChatColor()).append(game.translate(LoverType.LOVER.getKey())).append(ChatColor.WHITE);
                if (game.getConfig().getLoverSize() == 1) {
                    sb.append(", ");
                } else {
                    sb.append(" (§b").append(game.getConfig().getLoverSize()).append("§f), ");
                }
            }
            if (game.getConfig().getAmnesiacLoverSize() > 0) {
                sb.append(LoverType.AMNESIAC_LOVER.getChatColor()).append(game.translate(LoverType.AMNESIAC_LOVER.getKey())).append(ChatColor.WHITE);
                if (game.getConfig().getAmnesiacLoverSize() == 1) {
                    sb.append(", ");
                } else {
                    sb.append(" (§b").append(game.getConfig().getAmnesiacLoverSize()).append("§f), ");
                }
            }
            if (game.getConfig().getCursedLoverSize() > 0) {
                sb.append(LoverType.CURSED_LOVER.getChatColor()).append(game.translate(LoverType.CURSED_LOVER.getKey())).append(ChatColor.WHITE);
                if (game.getConfig().getLoverSize() != 1) {
                    sb.append(" (§b").append(game.getConfig().getCursedLoverSize()).append("§f");
                }
                sb.append("\n");
            }

            sb.append(getCompo(game, Category.WEREWOLF));
            sb.append(getCompo(game, Category.VILLAGER));
            sb.append(getCompo(game, Category.NEUTRAL));

            sb.append(game.translate("werewolf.commands.compo._"));
            player.sendMessage(sb.toString());
        }
    }

    public String getCompo(WereWolfAPI game, Category category) {

        StringBuilder sb = new StringBuilder(category.getChatColor() + game.translate(category.getKey()));
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        sb.append("§f : ");

        main.getRegisterManager().getRolesRegister().stream()
                .filter(roleRegister -> roleRegister.getCategories().contains(category))
                .forEach(roleRegister -> {
                    String key = roleRegister.getKey();
                    int number = game.getConfig().getRoleCount(key);
                    if (number > 0) {
                        if (number == 1) {
                            sb.append(game.translate(roleRegister.getKey())).append(", ");
                        } else {
                            sb.append(game.translate(roleRegister.getKey())).append(" (§b").append(game.getConfig().getRoleCount(key)).append("§f), ");
                        }
                        atomicBoolean.set(true);
                    }
                });
        sb.replace(sb.length() - 2, sb.length(), "");
        sb.append("\n");
        if (!atomicBoolean.get()) {
            return "";
        }
        return sb.toString();
    }
}
