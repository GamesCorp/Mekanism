package mekanism.common;

import ic2.api.Direction;
import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import ic2.api.IEnergyStorage;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;

import mekanism.api.IEnergyCube;
import mekanism.api.Tier.EnergyCubeTier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IElectricityStorage;
import universalelectricity.core.block.IVoltage;
import universalelectricity.core.electricity.ElectricityNetworkHelper;
import universalelectricity.core.electricity.IElectricityNetwork;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.IPowerProvider;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerFramework;
import buildcraft.api.power.PowerProvider;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class TileEntityEnergyCube extends TileEntityElectricBlock implements IEnergySink, IEnergySource, IEnergyStorage, IPowerReceptor, IElectricityStorage, IVoltage, IPeripheral
{
	public EnergyCubeTier tier = EnergyCubeTier.BASIC;
	
	/** Output per tick this machine can transfer. */
	public int output;
	
	/**
	 * A block used to store and transfer electricity.
	 * @param energy - maximum energy this block can hold.
	 * @param i - output per tick this block can handle.
	 */
	public TileEntityEnergyCube()
	{
		super("Energy Cube", 0);
		
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(powerProvider != null)
		{
			int received = (int)(powerProvider.useEnergy(0, (float)((tier.MAX_ELECTRICITY-electricityStored)*Mekanism.TO_BC), true)*Mekanism.FROM_BC);
			setJoules(electricityStored + received);
		}
		
		if(inventory[0] != null && electricityStored > 0)
		{
			setJoules(getJoules() - ElectricItemHelper.chargeItem(inventory[0], getJoules(), getVoltage()));
			
			if(Mekanism.hooks.IC2Loaded && inventory[0].getItem() instanceof IElectricItem)
			{
				double sent = ElectricItem.charge(inventory[0], (int)(electricityStored*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
				setJoules(electricityStored - sent);
			}
		}
		
		if(inventory[1] != null && electricityStored < tier.MAX_ELECTRICITY)
		{
			setJoules(getJoules() + ElectricItemHelper.dechargeItem(inventory[1], getMaxJoules() - getJoules(), getVoltage()));
			
			if(Mekanism.hooks.IC2Loaded && inventory[1].getItem() instanceof IElectricItem)
			{
				IElectricItem item = (IElectricItem)inventory[1].getItem();
				if(item.canProvideEnergy())
				{
					double gain = ElectricItem.discharge(inventory[1], (int)((tier.MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
					setJoules(electricityStored + gain);
				}
			}
			else if(inventory[1].itemID == Item.redstone.itemID && electricityStored+1000 <= tier.MAX_ELECTRICITY)
			{
				setJoules(electricityStored + 1000);
				--inventory[1].stackSize;
				
                if (inventory[1].stackSize <= 0)
                {
                    inventory[1] = null;
                }
			}
		}
		
		if(electricityStored > 0)
		{
			TileEntity tileEntity = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), ForgeDirection.getOrientation(facing));
			
			if(Mekanism.hooks.IC2Loaded)
			{
				if(electricityStored >= output)
				{
					EnergyTileSourceEvent event = new EnergyTileSourceEvent(this, output);
					MinecraftForge.EVENT_BUS.post(event);
					setJoules(electricityStored - (output - event.amount));
				}
			}
			
			if(tileEntity != null)
			{
				if(isPowerReceptor(tileEntity))
				{
					IPowerReceptor receptor = (IPowerReceptor)tileEntity;
	            	double electricityNeeded = Math.min(receptor.powerRequest(), receptor.getPowerProvider().getMaxEnergyStored() - receptor.getPowerProvider().getEnergyStored())*Mekanism.FROM_BC;
	            	float transferEnergy = (float)Math.min(electricityStored, Math.min(electricityNeeded, output));
	            	receptor.getPowerProvider().receiveEnergy((float)(transferEnergy*Mekanism.TO_BC), ForgeDirection.getOrientation(facing).getOpposite());
	            	setJoules(electricityStored - transferEnergy);
				}
			}
		}
		
		if(!worldObj.isRemote)
		{
			ForgeDirection outputDirection = ForgeDirection.getOrientation(facing);
			ArrayList<IElectricityNetwork> inputNetworks = new ArrayList<IElectricityNetwork>();
			
			for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				if(direction != outputDirection)
				{
					IElectricityNetwork network = ElectricityNetworkHelper.getNetworkFromTileEntity(VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), direction), direction);
					if(network != null)
					{
						inputNetworks.add(network);
					}
				}
			}
			
			TileEntity outputTile = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(this), outputDirection);

			IElectricityNetwork outputNetwork = ElectricityNetworkHelper.getNetworkFromTileEntity(outputTile, outputDirection);

			if(outputNetwork != null && !inputNetworks.contains(outputNetwork))
			{
				double outputWatts = Math.min(outputNetwork.getRequest().getWatts(), Math.min(getJoules(), 10000));

				if(getJoules() > 0 && outputWatts > 0 && getJoules()-outputWatts >= 0)
				{
					outputNetwork.startProducing(this, Math.min(outputWatts, getJoules()) / getVoltage(), getVoltage());
					setJoules(electricityStored - outputWatts);
				}
				else {
					outputNetwork.stopProducing(this);
				}
			}
		}
	}
	
	@Override
	protected EnumSet<ForgeDirection> getConsumingSides()
	{
		HashSet<ForgeDirection> set = new HashSet<ForgeDirection>();
		
		for(ForgeDirection dir : ForgeDirection.values())
		{
			if(dir != ForgeDirection.getOrientation(facing))
			{
				set.add(dir);
			}
		}
		
		return EnumSet.copyOf(set);
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, Direction direction)
	{
		return direction.toForgeDirection() != ForgeDirection.getOrientation(facing);
	}

	@Override
	public int getStored() 
	{
		return (int)(electricityStored*Mekanism.TO_IC2);
	}

	@Override
	public int getCapacity() 
	{
		return (int)(tier.MAX_ELECTRICITY*Mekanism.TO_IC2);
	}

	@Override
	public int getOutput() 
	{
		return output;
	}

	@Override
	public int demandsEnergy() 
	{
		return (int)((tier.MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2);
	}

	@Override
    public int injectEnergy(Direction direction, int i)
    {
		double givenEnergy = i*Mekanism.FROM_IC2;
    	double rejects = 0;
    	double neededEnergy = tier.MAX_ELECTRICITY-electricityStored;
    	
    	if(givenEnergy <= neededEnergy)
    	{
    		electricityStored += givenEnergy;
    	}
    	else if(givenEnergy > neededEnergy)
    	{
    		electricityStored += neededEnergy;
    		rejects = givenEnergy-neededEnergy;
    	}
    	
    	return (int)(rejects*Mekanism.TO_IC2);
    }

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, Direction direction)
	{
		return direction.toForgeDirection() == ForgeDirection.getOrientation(facing);
	}

	@Override
	public int getMaxEnergyOutput()
	{
		return output;
	}

	@Override
	public double getJoules() 
	{
		return electricityStored;
	}

	@Override
	public void setJoules(double joules)
	{
		electricityStored = Math.max(Math.min(joules, getMaxJoules()), 0);
	}

	@Override
	public double getMaxJoules() 
	{
		return tier.MAX_ELECTRICITY;
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
		if(side == ForgeDirection.getOrientation(1))
		{
			return 0;
		}
		
		return 1;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}

	@Override
	public double getVoltage() 
	{
		return tier.VOLTAGE;
	}
	
	/**
	 * Whether or not the declared Tile Entity is an instance of a BuildCraft power receptor.
	 * @param tileEntity - tile entity to check
	 * @return if the tile entity is a power receptor
	 */
	public boolean isPowerReceptor(TileEntity tileEntity)
	{
		if(tileEntity instanceof IPowerReceptor) 
		{
			IPowerReceptor receptor = (IPowerReceptor)tileEntity;
			IPowerProvider provider = receptor.getPowerProvider();
			return provider != null && provider.getClass().getSuperclass().equals(PowerProvider.class);
		}
		return false;
	}

	@Override
	public String getType() 
	{
		return getInvName();
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {tier.MAX_ELECTRICITY};
			case 3:
				return new Object[] {(tier.MAX_ELECTRICITY-electricityStored)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public boolean canAttachToSide(int side) 
	{
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		tier = EnergyCubeTier.getFromName(dataStream.readUTF());
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(tier.name);
		return data;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        tier = EnergyCubeTier.getFromName(nbtTags.getString("tier"));
        output = tier.OUTPUT;
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setString("tier", tier.name);
    }
	
	@Override
	public int getMaxSafeInput()
	{
		return 2048;
	}

	@Override
	public void setStored(int energy)
	{
		setJoules(energy*Mekanism.FROM_IC2);
	}

	@Override
	public int addEnergy(int amount)
	{
		setJoules(electricityStored + amount*Mekanism.FROM_IC2);
		return (int)electricityStored;
	}

	@Override
	public boolean isTeleporterCompatible(Direction side) 
	{
		return true;
	}
	
	@Override
	public int powerRequest() 
	{
		return (int)(tier.MAX_ELECTRICITY-electricityStored);
	}
}
