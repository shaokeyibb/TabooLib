package io.izzel.taboolib.util.item.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * @Author 坏黑
 * @Since 2019-05-21 20:28
 */
class MenuHolder implements InventoryHolder {

    private MenuBuilder builder;

    public MenuHolder(MenuBuilder builder) {
        this.builder = builder;
    }

    public MenuBuilder getBuilder() {
        return builder;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public static MenuBuilder get(Inventory inventory) {
        return inventory.getHolder() instanceof MenuHolder ? ((MenuHolder) inventory.getHolder()).getBuilder() : null;
    }
}
