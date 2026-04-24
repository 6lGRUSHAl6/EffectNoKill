package zov.dayn.effectNoKill;

import org.bukkit.Location;

// состояние игрока: заморозка, урон, откидывание
public final class PlayerState {

    private boolean frozen; // заморожен ли игрок
    private boolean blockDamage; // обнулить ли урон (с откидыванием)
    private boolean cancelDamage; // полностью отменить событие урона
    private double damageMultiplier = 1.0D; // множитель урона (1.0 - без изменений, 2.0 - двойной урон и т.д.)
    private Location frozenLocation; // координаты к которым привязан замороженный игрок

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isBlockDamage() {
        return blockDamage;
    }

    public void setBlockDamage(boolean blockDamage) {
        this.blockDamage = blockDamage;
    }

    public boolean isCancelDamage() {
        return cancelDamage;
    }

    public void setCancelDamage(boolean cancelDamage) {
        this.cancelDamage = cancelDamage;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public Location getFrozenLocation() {
        return frozenLocation;
    }

    public void setFrozenLocation(Location frozenLocation) {
        this.frozenLocation = frozenLocation;
    }
}