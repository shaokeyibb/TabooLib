package io.izzel.taboolib.util.item.inventory;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.module.inject.TListener;
import io.izzel.taboolib.util.item.Items;
import io.izzel.taboolib.util.lite.Vectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * @Author 坏黑
 * @Since 2019-05-21 18:16
 */
@TListener
class ClickListener implements Listener {

    @EventHandler
    public void e(PluginDisableEvent e) {
        Bukkit.getOnlinePlayers().stream().filter(player -> MenuHolder.get(player.getOpenInventory().getTopInventory()) != null).forEach(HumanEntity::closeInventory);
    }

    @EventHandler
    public void e(InventoryOpenEvent e) {
        MenuBuilder builder = MenuHolder.get(e.getInventory());
        if (builder != null) {
            TabooLib.getPlugin().runTask(() -> {
                try {
                    builder.getBuildTask().run(e.getInventory());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, 1);
            TabooLib.getPlugin().runTaskAsync(() -> {
                try {
                    builder.getBuildTaskAsync().run(e.getInventory());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, 1);
        }
    }

    @EventHandler
    public void e(InventoryClickEvent e) {
        MenuBuilder builder = MenuHolder.get(e.getInventory());
        if (builder != null) {
            // lock hand
            if (builder.isLockHand() && (e.getRawSlot() - e.getInventory().getSize() - 27 == e.getWhoClicked().getInventory().getHeldItemSlot() || (e.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY && e.getHotbarButton() == e.getWhoClicked().getInventory().getHeldItemSlot()))) {
                e.setCancelled(true);
            }
            try {
                builder.getClickTask().run(new ClickEvent(ClickType.CLICK, e, builder.getSlot(e.getRawSlot())));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            // drop on empty area
            if (!e.isCancelled() && Items.nonNull(e.getCurrentItem()) && e.getClick() == org.bukkit.event.inventory.ClickType.DROP) {
                Item item = Vectors.itemDrop((Player) e.getWhoClicked(), e.getCurrentItem());
                item.setPickupDelay(20);
                item.setMetadata("internal-drop", new FixedMetadataValue(TabooLib.getPlugin(), true));
                PlayerDropItemEvent event = new PlayerDropItemEvent((Player) e.getWhoClicked(), item);
                if (event.isCancelled()) {
                    event.getItemDrop().remove();
                } else {
                    e.setCurrentItem(null);
                }
            }
            // drop by keyboard
            else if (!e.isCancelled() && Items.nonNull(e.getCursor()) && e.getRawSlot() == -999) {
                Item item = Vectors.itemDrop((Player) e.getWhoClicked(), e.getCursor());
                item.setPickupDelay(20);
                item.setMetadata("internal-drop", new FixedMetadataValue(TabooLib.getPlugin(), true));
                PlayerDropItemEvent event = new PlayerDropItemEvent((Player) e.getWhoClicked(), item);
                if (event.isCancelled()) {
                    event.getItemDrop().remove();
                } else {
                    e.getView().setCursor(null);
                }
            }
        }
    }

    @EventHandler
    public void e(InventoryDragEvent e) {
        MenuBuilder builder = MenuHolder.get(e.getInventory());
        if (builder != null) {
            try {
                builder.getClickTask().run(new ClickEvent(ClickType.DRAG, e, ' '));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @EventHandler
    public void e(InventoryCloseEvent e) {
        MenuBuilder builder = MenuHolder.get(e.getInventory());
        if (builder != null) {
            try {
                builder.getCloseTask().run(e);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @EventHandler
    public void e(PlayerDropItemEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand() && !e.getItemDrop().hasMetadata("internal-drop")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void e(PlayerItemHeldEvent e) {
        if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder && ((MenuHolder) e.getPlayer().getOpenInventory().getTopInventory().getHolder()).getBuilder().isLockHand()) {
            e.setCancelled(true);
        }
    }
}
