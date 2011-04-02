package com.pi.coelho.CookieMonster;

import java.util.Arrays;
import java.util.HashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class CMEntityListener extends EntityListener {

    protected HashMap<Integer, MonsterAttack> attacks = new HashMap<Integer, MonsterAttack>();

    public CMEntityListener() {
    }

    @Override
    public void onEntityDamage(EntityDamageEvent entEvent) {
        if (entEvent.isCancelled()) {
            return;
        }

        if (entEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) entEvent;
            if (event.getEntity() instanceof LivingEntity) {
                monsterDamaged(event.getEntity(),
                        event.getDamager() instanceof Player ? (Player) event.getDamager() : null);
                return;
                 //System.out.println((Monster) event.getEntity() + " damaged by " + ((Player) event.getDamager()).getName());
            }
        } else if (entEvent instanceof EntityDamageByProjectileEvent) {
            EntityDamageByProjectileEvent event = (EntityDamageByProjectileEvent) entEvent;
            if (event.getEntity() instanceof LivingEntity) {
                monsterDamaged(event.getEntity(),
                        event.getDamager() instanceof Player ? (Player) event.getDamager() : null);
                return;
                //System.out.println((Monster) event.getEntity() + " damaged by " + ((Player) event.getDamager()).getName());
            }
        }
        monsterDamaged(entEvent.getEntity(), null);
    }

    public void monsterDamaged(Entity monster, Player player) {
        if (!attacks.containsKey(monster.getEntityId())) {
            if (player != null) {
                attacks.put(monster.getEntityId(), new MonsterAttack(player));
            }
        } else {
            attacks.get(monster.getEntityId()).setAttack(player);
        }
    }

    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof LivingEntity
                && attacks.containsKey(event.getEntity().getEntityId())) {
            if (attacks.get(event.getEntity().getEntityId()).attackTimeAgo()
                    <= CMConfig.damageTimeThreshold) {
                attacks.get(event.getEntity().getEntityId()).rewardKill(event);
            }
            attacks.remove(event.getEntity().getEntityId());
        }
    }

    public class MonsterAttack {

        long lastAttackTime;
        Player lastAttackPlayer;

        public MonsterAttack(Player attacker) {
            setAttack(attacker);
        }

        public long attackTimeAgo() {
            return lastAttackTime > 0 ? System.currentTimeMillis() - lastAttackTime : 0;
        }

        public final void setAttack(Player attacker) {
            lastAttackPlayer = attacker;
            lastAttackTime = System.currentTimeMillis();
        }

        public void rewardKill(EntityDeathEvent event){
            if(lastAttackPlayer != null){
                CookieMonster.getRewardHandler().GivePlayerCoinReward(lastAttackPlayer, event.getEntity());
                ItemStack newDrops[] = CookieMonster.getRewardHandler().getDropReward(event.getEntity());
                if(newDrops!=null){
                    event.getDrops().clear();
                    event.getDrops().addAll(Arrays.asList(newDrops));
                }
            }
        }
    }
}