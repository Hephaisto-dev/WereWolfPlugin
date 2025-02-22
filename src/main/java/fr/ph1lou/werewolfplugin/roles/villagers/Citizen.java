// 
// Decompiled by Procyon v0.5.36
// 

package fr.ph1lou.werewolfplugin.roles.villagers;

import fr.ph1lou.werewolfapi.annotations.Role;
import fr.ph1lou.werewolfapi.enums.Category;
import fr.ph1lou.werewolfapi.basekeys.ConfigBase;
import fr.ph1lou.werewolfapi.basekeys.Prefix;
import fr.ph1lou.werewolfapi.enums.RoleAttribute;
import fr.ph1lou.werewolfapi.basekeys.RoleBase;
import fr.ph1lou.werewolfapi.enums.StatePlayer;
import fr.ph1lou.werewolfapi.basekeys.TimerBase;
import fr.ph1lou.werewolfapi.events.game.day_cycle.DayEvent;
import fr.ph1lou.werewolfapi.events.game.vote.NewVoteResultEvent;
import fr.ph1lou.werewolfapi.events.game.vote.VoteEndEvent;
import fr.ph1lou.werewolfapi.events.random_events.RumorsWriteEvent;
import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.interfaces.IPlayerWW;
import fr.ph1lou.werewolfapi.player.utils.Formatter;
import fr.ph1lou.werewolfapi.role.impl.RoleVillage;
import fr.ph1lou.werewolfapi.role.interfaces.IAffectedPlayers;
import fr.ph1lou.werewolfapi.role.interfaces.ILimitedUse;
import fr.ph1lou.werewolfapi.role.interfaces.IPower;
import fr.ph1lou.werewolfapi.role.utils.DescriptionBuilder;
import fr.ph1lou.werewolfplugin.Register;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

  @Role(key = RoleBase.CITIZEN,
          category = Category.VILLAGER,
          attributes = {RoleAttribute.VILLAGER,RoleAttribute.MINOR_INFORMATION})
public class Citizen extends RoleVillage implements ILimitedUse, IAffectedPlayers, IPower
{
    private int use;
    private final List<IPlayerWW> affectedPlayer;
    private boolean power;

    public Citizen(WereWolfAPI api, IPlayerWW playerWW) {
        super(api, playerWW);
        this.use = 0;
        this.affectedPlayer = new ArrayList<>();
        this.power = true;
    }

    @Override
    public void setPower(boolean power) {
        this.power = power;
    }

    @Override
    public boolean hasPower() {
        return this.power;
    }

    @Override
    public void addAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.add(playerWW);
    }

    @Override
    public void removeAffectedPlayer(IPlayerWW playerWW) {
        this.affectedPlayer.remove(playerWW);
    }

    @Override
    public void clearAffectedPlayer() {
        this.affectedPlayer.clear();
    }

    @Override
    public List<IPlayerWW> getAffectedPlayers() {
        return this.affectedPlayer;
    }

    @Override
    public int getUse() {
        return this.use;
    }

    @Override
    public void setUse(int use) {
        this.use = use;
    }

    @EventHandler
    public void onVoteEnd(VoteEndEvent event) {
        if (!this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        if(!this.isAbilityEnabled()){
            return;
        }
        if (this.game.getConfig().isConfigActive(ConfigBase.NEW_VOTE)) {
            return;
        }
        if (this.getUse() < 2) {
            this.getPlayerWW().sendMessage(this.seeVote());
        }
        if (this.hasPower()) {
            this.getPlayerWW().sendMessage(this.cancelVote());
        }
    }

    @NotNull
    @Override
    public String getDescription() {
        return new DescriptionBuilder(this.game, this)
                .setDescription(this.game.translate(this.game.getConfig()
                        .isConfigActive(ConfigBase.NEW_VOTE) ? "werewolf.roles.citizen.description_new_vote" : "werewolf.roles.citizen.description")).addExtraLines(this.game.translate("werewolf.roles.citizen.description_extra")).build();
    }

    @EventHandler
    public void onDay(DayEvent event) {
        if (this.getPlayerWW().isState(StatePlayer.DEATH)) {
            return;
        }
        if(!this.isAbilityEnabled()){
            return;
        }
        if (!this.game.getConfig().isConfigActive(ConfigBase.HIDE_COMPOSITION)) {
            return;
        }
        List<String> roles = Register.get()
                .getRolesRegister().stream()
                .map(iRoleRoleWrapper -> iRoleRoleWrapper.getMetaDatas().key())
                .collect(Collectors.toList());
        if (roles.size() < 3) {
            return;
        }
        Collections.shuffle(roles, this.game.getRandom());
        int count = roles.subList(0, 3).stream().mapToInt(s -> this.game.getConfig().getRoleCount(s)).sum();
        this.getPlayerWW().sendMessageWithKey(Prefix.ORANGE, "werewolf.roles.citizen.hide_composition", Formatter.format("&role1&", this.game.translate(roles.get(0))), Formatter.format("&role2&", this.game.translate(roles.get(1))), Formatter.format("&role3&", this.game.translate(roles.get(2))), Formatter.number(count));
    }

    @Override
    public void recoverPower() {
    }

    @EventHandler
    public void onNewVote(NewVoteResultEvent event) {
        if (!this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        if(!this.isAbilityEnabled()){
            return;
        }
        if (event.getPlayerVotedByWerewolfWW() != null && this.hasPower()) {
            this.getPlayerWW().sendMessage(this.seeWerewolfVote(event.getPlayerVotedByWerewolfWW().getUUID()));
        }
        if (event.getPlayerVotedByVillagerWW() == null) {
            return;
        }
        this.getPlayerWW().sendMessageWithKey(Prefix.ORANGE, "werewolf.roles.citizen.new_vote_count",
                Formatter.player(event.getPlayerVotedByVillagerWW().getName()),
                Formatter.number(this.game.getVoteManager().getVotes().getOrDefault(event.getPlayerVotedByVillagerWW(), 0)));
    }

    private TextComponent cancelVote() {
        TextComponent cancelVote = new TextComponent(this.game.translate("werewolf.roles.citizen.click"));
        cancelVote.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/ww %s", this.game.translate("werewolf.roles.citizen.command_2"))));
        cancelVote.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.game.translate("werewolf.roles.citizen.cancel")).create()));
        TextComponent cancel = new TextComponent(this.game.translate(Prefix.YELLOW, "werewolf.roles.citizen.cancel_vote_message", Formatter.number(this.hasPower() ? 1 : 0)));
        cancel.addExtra(cancelVote);
        cancel.addExtra(new TextComponent(this.game.translate("werewolf.roles.citizen.time_left", Formatter.timer(game, TimerBase.VOTE_WAITING))));
        return cancel;
    }

    private TextComponent seeVote() {
        TextComponent seeVote = new TextComponent(this.game.translate("werewolf.roles.citizen.click"));
        seeVote.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/ww %s", this.game.translate("werewolf.roles.citizen.command_1"))));
        seeVote.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.game.translate("werewolf.roles.citizen.see")).create()));
        TextComponent see = new TextComponent(this.game.translate(Prefix.YELLOW, "werewolf.roles.citizen.see_vote_message", Formatter.number(2 - this.getUse())));
        see.addExtra(seeVote);
        see.addExtra(new TextComponent(this.game.translate("werewolf.roles.citizen.time_left", Formatter.timer(game, TimerBase.VOTE_WAITING))));
        return see;
    }

    private TextComponent seeWerewolfVote(UUID werewolf) {
        TextComponent seeVote = new TextComponent(this.game.translate(Prefix.GREEN, "werewolf.roles.citizen.click_to_see_werewolf.configurations.vote"));
        seeVote.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/ww %s %s", this.game.translate("werewolf.roles.citizen.command_1"),
                werewolf.toString())));
        return seeVote;
    }

    @EventHandler
    public void onRumor(RumorsWriteEvent event){
        if(event.isCancelled()){
            return;
        }
        if (!this.getPlayerWW().isState(StatePlayer.ALIVE)) {
            return;
        }
        if(!this.isAbilityEnabled()){
            return;
        }

        this.getPlayerWW().sendMessageWithKey(Prefix.GREEN, "werewolf.roles.citizen.rumor",
                Formatter.player(event.getPlayerWW().getName()),
                Formatter.format("&message&", event.getMessage()));
    }
}
