package fr.ph1lou.werewolfplugin.listeners;


import fr.ph1lou.werewolfapi.game.WereWolfAPI;
import fr.ph1lou.werewolfapi.player.impl.PotionModifier;
import fr.ph1lou.werewolfapi.utils.BukkitUtils;
import fr.ph1lou.werewolfapi.versions.VersionUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.Optional;

public class PatchPotions implements Listener {

    private final WereWolfAPI game;

    public PatchPotions(WereWolfAPI game) {
        this.game = game;
    }

    @EventHandler
    private void onPatchPotion(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player)) return;

        if (!(event.getDamager() instanceof Player)) return;
        Player damager = (Player) event.getDamager();
        Player player = (Player) event.getEntity();

        if (damager.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {

            if (damager.getActivePotionEffects().stream().filter(potionEffect -> potionEffect.getType().equals(PotionEffectType.INCREASE_DAMAGE)).map(PotionEffect::getAmplifier).findFirst().orElse(-1) == 0) {
                event.setDamage(event.getDamage() / 2.3f *
                        (1 + game.getConfig().getStrengthRate() / 100f));
            } else event.setDamage(event.getDamage() *
                    (1 + game.getConfig().getStrengthRate() / 100f));
        }
        if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            if (game.getConfig().getResistanceRate() >= 100) {
                event.setCancelled(true);
            }
            event.setDamage(event.getDamage() * (100 - game.getConfig().getResistanceRate()) / 80f);
        }
    }

    @EventHandler
    public void onEffectGet(PotionSplashEvent event){

        event.setCancelled(true);
        event.getAffectedEntities().stream()
                .map(livingEntity -> game.getPlayerWW(livingEntity.getUniqueId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(playerWW -> event.getPotion().getEffects().forEach(potionEffect -> {
                    if(potionEffect.getDuration() == 1){ //handle instant potion
                        if(potionEffect.getType().equals(PotionEffectType.HEAL)){
                            if(potionEffect.getAmplifier() == 0){
                                playerWW.addPlayerHealth(4);
                            }
                            else{
                                playerWW.addPlayerHealth(8);
                            }
                        }
                        else if (potionEffect.getType().equals(PotionEffectType.HARM)){
                            if(potionEffect.getAmplifier() == 0){
                                playerWW.removePlayerHealth(6);
                            }
                            else{
                                playerWW.removePlayerHealth(12);
                            }
                        }
                    }
                    else{
                        playerWW.addPotionModifier(PotionModifier.add(
                                potionEffect.getType(),
                                potionEffect.getDuration(),
                                potionEffect.getAmplifier(),
                                "splash_potion"));
                    }
                }));
    }


    @EventHandler
    public void onDrinkPotionEvent(PlayerItemConsumeEvent event){

        Player player = event.getPlayer();
        ItemStack itemStack = event.getItem();
        Collection<PotionEffect> potionEffectList = VersionUtils.getVersionUtils()
                .getPotionEffect(itemStack);

        if(!potionEffectList.isEmpty()) {

            event.setCancelled(true);

            BukkitUtils.scheduleSyncDelayedTask(game, () ->
            {
                ItemStack itemStack1 = VersionUtils.getVersionUtils().getItemInHand(player);

                if(itemStack1.getAmount() > 1){
                    itemStack1.setAmount(itemStack1.getAmount()-1);
                    VersionUtils.getVersionUtils().setItemInHand(player,itemStack1);
                }
                else{
                    VersionUtils.getVersionUtils().setItemInHand(player,null);
                }
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
            });

            game.getPlayerWW(player.getUniqueId())
                    .ifPresent(playerWW -> potionEffectList
                            .forEach(potionEffect -> playerWW.addPotionModifier(
                                    PotionModifier.add(
                                            potionEffect.getType(),
                                            potionEffect.getDuration(),
                                            potionEffect.getAmplifier(),
                                            "potion_drink"))));
        }
    }
}
