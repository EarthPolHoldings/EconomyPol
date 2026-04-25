package com.earthpol.economyPol.economy.service.money;

import com.earthpol.economyPol.economy.service.support.DenominationService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

final class LiveMoneySnapshotService {

    private final DenominationService denominationService;
    private final ShulkerDeliveryService shulkerDeliveryService;

    LiveMoneySnapshotService(
            DenominationService denominationService,
            ShulkerDeliveryService shulkerDeliveryService
    ) {
        this.denominationService = denominationService;
        this.shulkerDeliveryService = shulkerDeliveryService;
    }

    long scanPlayerMoney(Player player) {
        return countPlayerInventoryMoney(player) + countPlayerEnderChestMoney(player);
    }

    long countPlayerInventoryMoney(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack offHand = inventory.getItemInOffHand();
        return countContents(inventory.getStorageContents())
                + shulkerDeliveryService.countMoneyInTopLevelGoldShulkers(inventory.getStorageContents())
                + denominationService.valueOf(offHand)
                + shulkerDeliveryService.countMoneyInGoldShulker(offHand);
    }

    long countPlayerEnderChestMoney(Player player) {
        return countInventory(player.getEnderChest())
                + shulkerDeliveryService.countMoneyInTopLevelGoldShulkers(player.getEnderChest().getContents());
    }

    long countTopLevelEnderChest(Player player) {
        return countInventory(player.getEnderChest())
                + shulkerDeliveryService.countMoneyInTopLevelGoldShulkers(player.getEnderChest().getContents());
    }

    LiveMoneyService.LiveContainerSnapshot captureLiveContainerSnapshot(Player player) {
        ItemStack[] inventoryContents = cloneContents(player.getInventory().getStorageContents());
        ItemStack[] enderContents = cloneContents(player.getEnderChest().getContents());
        ItemStack offHand = cloneStack(player.getInventory().getItemInOffHand());
        return new LiveMoneyService.LiveContainerSnapshot(inventoryContents, enderContents, offHand);
    }

    void restoreLiveContainerSnapshot(Player player, LiveMoneyService.LiveContainerSnapshot snapshot) {
        player.getInventory().setStorageContents(cloneContents(snapshot.inventoryContents()));
        player.getEnderChest().setContents(cloneContents(snapshot.enderChestContents()));
        player.getInventory().setItemInOffHand(cloneStack(snapshot.offHand()));
    }

    LiveMoneySimulatedState simulatedStateFromSnapshot(LiveMoneyService.LiveContainerSnapshot snapshot) {
        return new LiveMoneySimulatedState(
                cloneContents(snapshot.inventoryContents()),
                cloneContents(snapshot.enderChestContents()),
                cloneStack(snapshot.offHand())
        );
    }

    long scanPlayerMoney(LiveMoneySimulatedState simulatedState) {
        long total = countContents(simulatedState.inventoryContents());
        total += shulkerDeliveryService.countMoneyInTopLevelGoldShulkers(simulatedState.inventoryContents());
        total += denominationService.valueOf(simulatedState.offHand());
        total += shulkerDeliveryService.countMoneyInGoldShulker(simulatedState.offHand());
        total += countContents(simulatedState.enderChestContents());
        total += shulkerDeliveryService.countMoneyInTopLevelGoldShulkers(simulatedState.enderChestContents());
        return total;
    }

    long countContents(ItemStack[] contents) {
        long total = 0L;
        for (ItemStack itemStack : contents) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            total += denominationService.valueOf(itemStack);
        }
        return total;
    }

    ItemStack[] cloneContents(ItemStack[] contents) {
        ItemStack[] clone = new ItemStack[contents.length];
        for (int index = 0; index < contents.length; index++) {
            clone[index] = cloneStack(contents[index]);
        }
        return clone;
    }

    ItemStack cloneStack(ItemStack itemStack) {
        return itemStack == null ? null : itemStack.clone();
    }

    private long countInventory(Inventory inventory) {
        long total = 0L;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            total += denominationService.valueOf(itemStack);
        }
        return total;
    }
}
