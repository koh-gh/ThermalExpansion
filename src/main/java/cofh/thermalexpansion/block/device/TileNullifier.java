package cofh.thermalexpansion.block.device;

import cofh.core.init.CoreProps;
import cofh.lib.render.RenderHelper;
import cofh.lib.util.helpers.FluidHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.client.device.GuiNullifier;
import cofh.thermalexpansion.gui.container.device.ContainerNullifier;
import cofh.thermalexpansion.init.TETextures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

public class TileNullifier extends TileDeviceBase {

	private static final int TYPE = BlockDevice.Type.NULLIFIER.getMetadata();

	public static void initialize() {

		SIDE_CONFIGS[TYPE] = new SideConfig();
		SIDE_CONFIGS[TYPE].numConfig = 2;
		SIDE_CONFIGS[TYPE].slotGroups = new int[][] { {}, { 0 }, {} };
		SIDE_CONFIGS[TYPE].allowInsertionSide = new boolean[] { false, false, false };
		SIDE_CONFIGS[TYPE].allowExtractionSide = new boolean[] { false, false, false };
		SIDE_CONFIGS[TYPE].sideTex = new int[] { 0, 1, 4 };
		SIDE_CONFIGS[TYPE].defaultSides = new byte[] { 0, 1, 1, 1, 1, 1 };

		SLOT_CONFIGS[TYPE] = new SlotConfig();
		SLOT_CONFIGS[TYPE].allowInsertionSlot = new boolean[] { true };
		SLOT_CONFIGS[TYPE].allowExtractionSlot = new boolean[] { false };

		GameRegistry.registerTileEntity(TileNullifier.class, "thermalexpansion:device_nullifier");

		config();
	}

	public static void config() {

		String category = "Device.Nullifier";
		BlockDevice.enable[TYPE] = ThermalExpansion.CONFIG.get(category, "Enable", true);
	}

	private static final int[] SLOTS = { 0 };

	public TileNullifier() {

		super();
		inventory = new ItemStack[1];
	}

	@Override
	public int getType() {

		return TYPE;
	}

	@Override
	public void setDefaultSides() {

		sideCache = getDefaultSides();
		sideCache[facing] = 0;
	}

	@Override
	public int getLightValue() {

		return isActive ? FluidHelper.getFluidLuminosity(FluidRegistry.LAVA) : 0;
	}

	@Override
	public boolean sendRedstoneUpdates() {

		return true;
	}

	protected boolean isSideAccessible(EnumFacing side) {

		return sideCache[side.ordinal()] == 1 && redstoneControlOrDisable();
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiNullifier(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerNullifier(inventory, this);
	}

	/* NBT METHODS */
	@Override
	public void readInventoryFromNBT(NBTTagCompound nbt) {

	}

	@Override
	public void writeInventoryToNBT(NBTTagCompound nbt) {

	}

	/* IInventory */
	@Override
	public ItemStack getStackInSlot(int slot) {

		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {

		if (inventory[slot] == null) {
			return null;
		}
		if (inventory[slot].stackSize <= amount) {
			amount = inventory[slot].stackSize;
		}
		ItemStack stack = inventory[slot].splitStack(amount);

		if (inventory[slot].stackSize <= 0) {
			inventory[slot] = null;
		}
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {

		if (inventory[slot] == null) {
			return null;
		}
		ItemStack stack = inventory[slot];
		inventory[slot] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {

		if (slot == 0) {
			return;
		}
		inventory[slot] = stack;

		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
	}

	/* ISidedTexture */
	@Override
	public TextureAtlasSprite getTexture(int side, int pass) {

		if (pass == 0) {
			if (side == 0) {
				return TETextures.DEVICE_BOTTOM;
			} else if (side == 1) {
				return TETextures.DEVICE_TOP;
			}
			return side != facing ? TETextures.DEVICE_SIDE : isActive ? RenderHelper.getFluidTexture(FluidRegistry.LAVA) : TETextures.DEVICE_FACE[TYPE];
		} else if (side < 6) {
			return side != facing ? TETextures.CONFIG[sideConfig.sideTex[sideCache[side]]] : isActive ? TETextures.DEVICE_ACTIVE[TYPE] : TETextures.DEVICE_FACE[TYPE];
		}
		return TETextures.DEVICE_SIDE;
	}

	/* ISidedInventory */
	@Override
	public int[] getSlotsForFace(EnumFacing side) {

		return isSideAccessible(side) ? SLOTS : CoreProps.EMPTY_INVENTORY;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side) {

		return isSideAccessible(side);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side) {

		return false;
	}

	/* CAPABILITIES */
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing from) {

		return super.hasCapability(capability, from) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, final EnumFacing facing) {

		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new net.minecraftforge.fluids.capability.IFluidHandler() {

				@Override
				public IFluidTankProperties[] getTankProperties() {

					return new IFluidTankProperties[] { new FluidTankProperties(null, Integer.MAX_VALUE, true, false) };
				}

				@Override
				public int fill(FluidStack resource, boolean doFill) {

					return isSideAccessible(facing) ? resource.amount : 0;
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {

					return null;
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {

					return null;
				}
			});
		}
		return super.getCapability(capability, facing);
	}

}
