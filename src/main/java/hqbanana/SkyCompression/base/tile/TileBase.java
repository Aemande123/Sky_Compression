package hqbanana.SkyCompression.base.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class TileBase extends TileEntity {
	private int previousRedstoneSignal;
	private String name;
	
	public TileBase(String name) {
		this.name = name;
	}
	
	public ITextComponent getDisplayName() {
		return new TextComponentTranslation("container.sc." + name);
	}
	
	public boolean receivedPulse() {
		return getRedstoneSignal() > 0 && previousRedstoneSignal == 0;
	}
	
	public boolean canAcceptRedstone() {
		return true;
	}
	
	public int getRedstoneSignal() {
		int signal = 0;
		if (canAcceptRedstone()) {
			for (EnumFacing dir : EnumFacing.VALUES) {
				int redstoneSide = getWorld().getRedstonePower(getPos().offset(dir), dir);
				signal = Math.max(signal, redstoneSide);
			}
		}
		return signal;
	}
	
	public int getRedstoneSignalFromSize(EnumFacing dir) {
		int redstoneSignal = 0;
		if (canAcceptRedstone()) {
			int redstoneSide = getWorld().getRedstonePower(getPos().offset(dir), dir);
			redstoneSignal = Math.max(redstoneSignal, redstoneSide);
		}
		return redstoneSignal;
	}
	
	public void updateRedstone() {
		if (!world.isRemote) previousRedstoneSignal = getRedstoneSignal();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
	{
		super.onDataPacket(net, packet);
		this.readFromNBT(packet.getNbtCompound());
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
	    NBTTagCompound nbtTagCompound = new NBTTagCompound();
	    writeToNBT(nbtTagCompound);
	    return nbtTagCompound;
	}
	
	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}

	public void markDirty() {
		super.markDirty();
		world.notifyBlockUpdate(getPos(), world.getBlockState(getPos()), world.getBlockState(getPos()), 3);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		super.writeToNBT(compound);
		compound.setInteger("pSignal", previousRedstoneSignal);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		previousRedstoneSignal = compound.getInteger("pSignal");
	}
}
