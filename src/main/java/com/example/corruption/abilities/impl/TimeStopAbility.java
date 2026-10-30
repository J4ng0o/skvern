// В activate():
java.util.List<LivingEntity> frozen = new java.util.ArrayList<>();
for (World world : Bukkit.getWorlds()) {
    for (LivingEntity entity : world.getLivingEntities()) {
        if (entity.equals(player)) continue;
        if (entity.getLocation().distanceSquared(player.getLocation()) > RADIUS * RADIUS) continue;

        entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 255, false, false));
        frozen.add(entity);

        if (entity instanceof Player frozenPlayer) {
            AttributeInstance jumpAttr = frozenPlayer.getAttribute(Attribute.JUMP_STRENGTH);
            if (jumpAttr != null) {
                originalJumpValues.put(frozenPlayer.getUniqueId(), jumpAttr.getBaseValue());
                jumpAttr.setBaseValue(0.0);
            }
        }
    }
}

// В runTaskLater():
new BukkitRunnable() {
    @Override
    public void run() {
        for (LivingEntity entity : frozen) {
            entity.removePotionEffect(PotionEffectType.SLOWNESS);
            if (entity instanceof Player fp) {
                AttributeInstance jumpAttr = fp.getAttribute(Attribute.JUMP_STRENGTH);
                if (jumpAttr != null) {
                    Double original = originalJumpValues.remove(fp.getUniqueId());
                    jumpAttr.setBaseValue(original != null ? original : 0.42);
                }
            }
        }
        player.sendMessage(ChatColor.GRAY + "Время снова потекло.");
    }
}.runTaskLater(manager.getPlugin(), durationTicks);
