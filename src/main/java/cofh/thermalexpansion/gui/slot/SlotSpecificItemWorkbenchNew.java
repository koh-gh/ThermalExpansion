package cofh.thermalexpansion.gui.slot;

import cofh.lib.gui.slot.SlotSpecificItem;
import cofh.thermalexpansion.block.workbench.TileWorkbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotSpecificItemWorkbenchNew extends SlotSpecificItem {

	TileWorkbench myTile;

	public SlotSpecificItemWorkbenchNew(IInventory inventory, int slotIndex, int x, int y, ItemStack stack) {

		super(inventory, slotIndex, x, y, stack);
		myTile = (TileWorkbench) inventory;
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {

		if (myTile.getCurrentSchematicSlot() == getSlotIndex()) {
			return true;
		}
		myTile.setCurrentSchematicSlot(getSlotIndex());
		onSlotChanged();
		return false;

	}

}
