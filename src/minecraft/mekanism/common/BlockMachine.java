package mekanism.common;

import java.util.List;
import java.util.Random;

import buildcraft.api.tools.IToolWrench;

import thermalexpansion.api.core.IDismantleable;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.prefab.implement.IToolConfigurator;

import mekanism.api.IActiveState;
import mekanism.api.IEnergyCube;
import mekanism.api.IFactory;
import mekanism.api.Tier;
import mekanism.api.IFactory.RecipeType;
import mekanism.api.IUpgradeManagement;
import mekanism.client.ClientProxy;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Block class for handling multiple machine block IDs.
 * 0: Enrichment Chamber
 * 1: Osmium Compressor
 * 2: Combiner
 * 3: Crusher
 * 4: Theoretical Elementizer
 * 5: Basic Factory
 * 6: Advanced Factory
 * 7: Elite Factory
 * 8: Metallurgic Infuser
 * 9: Purification Chamber
 * 10: Energized Smelter
 * @author AidanBrady
 *
 */
public class BlockMachine extends BlockContainer implements IDismantleable
{
	public Icon[][] icons = new Icon[256][256];
	public Random machineRand = new Random();
	
	public BlockMachine(int id)
	{
		super(id, Material.iron);
		setHardness(3.5F);
		setResistance(8F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void func_94332_a(IconRegister register)
	{
		icons[0][0] = register.func_94245_a("mekanism:EnrichmentChamberFrontOff");
		icons[0][1] = register.func_94245_a("mekanism:EnrichmentChamberFrontOn");
		icons[0][2] = register.func_94245_a("mekanism:SteelCasing");
		icons[1][0] = register.func_94245_a("mekanism:OsmiumCompressorFrontOff");
		icons[1][1] = register.func_94245_a("mekanism:OsmiumCompressorFrontOn");
		icons[1][2] = register.func_94245_a("mekanism:SteelCasing");
		icons[2][0] = register.func_94245_a("mekanism:CombinerFrontOff");
		icons[2][1] = register.func_94245_a("mekanism:CombinerFrontOn");
		icons[2][2] = register.func_94245_a("mekanism:SteelCasing");
		icons[3][0] = register.func_94245_a("mekanism:CrusherFrontOff");
		icons[3][1] = register.func_94245_a("mekanism:CrusherFrontOn");
		icons[3][2] = register.func_94245_a("mekanism:SteelCasing");
		icons[5][0] = register.func_94245_a("mekanism:BasicSmeltingFactoryFront");
		icons[5][1] = register.func_94245_a("mekanism:BasicSmeltingFactorySide");
		icons[5][2] = register.func_94245_a("mekanism:BasicSmeltingFactoryTop");
		icons[6][0] = register.func_94245_a("mekanism:AdvancedFactoryFront");
		icons[6][1] = register.func_94245_a("mekanism:AdvancedFactorySide");
		icons[6][2] = register.func_94245_a("mekanism:AdvancedFactoryTop");
		icons[7][0] = register.func_94245_a("mekanism:EliteFactoryFront");
		icons[7][1] = register.func_94245_a("mekanism:EliteFactorySide");
		icons[7][2] = register.func_94245_a("mekanism:EliteFactoryTop");
		icons[8][0] = register.func_94245_a("mekanism:MetallurgicInfuserFrontOff");
		icons[8][1] = register.func_94245_a("mekanism:MetallurgicInfuserFrontOn");
		icons[8][2] = register.func_94245_a("mekanism:MetallurgicInfuserSideOff");
		icons[8][3] = register.func_94245_a("mekanism:MetallurgicInfuserSideOn");
		icons[8][4] = register.func_94245_a("mekanism:MetallurgicInfuserTopOff");
		icons[8][5] = register.func_94245_a("mekanism:MetallurgicInfuserTopOn");
		icons[8][6] = register.func_94245_a("mekanism:MetallurgicInfuserBackOff");
		icons[8][7] = register.func_94245_a("mekanism:MetallurgicInfuserBackOn");
		icons[9][0] = register.func_94245_a("mekanism:PurificationChamberFrontOff");
		icons[9][1] = register.func_94245_a("mekanism:PurificationChamberFrontOn");
		icons[9][2] = register.func_94245_a("mekanism:SteelCasing");
		icons[10][0] = register.func_94245_a("mekanism:EnergizedSmelterFrontOff");
		icons[10][1] = register.func_94245_a("mekanism:EnergizedSmelterFrontOn");
		icons[10][2] = register.func_94245_a("mekanism:SteelCasing");
	}
	
	@Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving, ItemStack itemstack)
    {
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
        int side = MathHelper.floor_double((double)(entityliving.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int change = 3;
        
        switch(side)
        {
        	case 0: change = 2; break;
        	case 1: change = 5; break;
        	case 2: change = 3; break;
        	case 3: change = 4; break;
        }
        
        tileEntity.setFacing((short)change);
        
        if(tileEntity instanceof IBoundingBlock)
        {
        	((IBoundingBlock)tileEntity).onPlace();
        }
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
        if (MekanismUtils.isActive(world, x, y, z))
        {
            float xRandom = (float)x + 0.5F;
            float yRandom = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float)z + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;

            if (tileEntity.facing == 4)
            {
                world.spawnParticle("smoke", (double)(xRandom - iRandom), (double)yRandom, (double)(zRandom + jRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(xRandom - iRandom), (double)yRandom, (double)(zRandom + jRandom), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 5)
            {
                world.spawnParticle("smoke", (double)(xRandom + iRandom), (double)yRandom, (double)(zRandom + jRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(xRandom + iRandom), (double)yRandom, (double)(zRandom + jRandom), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 2)
            {
                world.spawnParticle("smoke", (double)(xRandom + jRandom), (double)yRandom, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(xRandom + jRandom), (double)yRandom, (double)(zRandom - iRandom), 0.0D, 0.0D, 0.0D);
            }
            else if (tileEntity.facing == 3)
            {
                world.spawnParticle("smoke", (double)(xRandom + jRandom), (double)yRandom, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
                world.spawnParticle("reddust", (double)(xRandom + jRandom), (double)yRandom, (double)(zRandom + iRandom), 0.0D, 0.0D, 0.0D);
            }
        }
    }
	
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		
		if(tileEntity instanceof IActiveState)
		{
			if(((IActiveState)tileEntity).getActive())
			{
				return 15;
			}
		}
		
		return 0;
	}
    
	@Override
    public Icon getBlockTextureFromSideAndMetadata(int side, int meta)
    {
    	if(meta == 0)
    	{
        	if(side == 3)
        	{
        		return icons[0][0];
        	}
        	else {
        		return icons[0][2];
        	}
    	}
    	else if(meta == 1)
    	{
        	if(side == 3)
        	{
        		return icons[1][0];
        	}
        	else {
        		return icons[1][2];
        	}
    	}
    	else if(meta == 2)
    	{
        	if(side == 3)
        	{
        		return icons[2][0];
        	}
        	else {
        		return icons[2][2];
        	}
    	}
    	else if(meta == 3)
    	{
        	if(side == 3)
        	{
        		return icons[3][0];
        	}
        	else {
        		return icons[3][2];
        	}
    	}
    	else if(meta == 5)
    	{
    		if(side == 3)
    		{
    			return icons[5][0];
    		}
     		else if(side == 0 || side == 1)
    		{
    			return icons[5][2];
    		}
    		else {
    			return icons[5][1];
    		}
    	}
    	else if(meta == 6)
    	{
    		if(side == 3)
    		{
    			return icons[6][0];
    		}
     		else if(side == 0 || side == 1)
    		{
    			return icons[6][2];
    		}
    		else {
    			return icons[6][1];
    		}
    	}
    	else if(meta == 7)
    	{
    		if(side == 3)
    		{
    			return icons[7][0];
    		}
     		else if(side == 0 || side == 1)
    		{
    			return icons[7][2];
    		}
    		else {
    			return icons[7][1];
    		}
    	}
    	else if(meta == 8)
    	{
        	if(side == 0 || side == 1)
        	{
        		return icons[8][4];
        	}
        	else if(side == 3)
        	{
        		return icons[8][0];
        	}
        	else if(side == 2)
        	{
        		return icons[8][6];
        	}
        	else {
        		return icons[8][2];
        	}
    	}
    	else if(meta == 9)
    	{
    		if(side == 3)
    		{
    			return icons[9][0];
    		}
    		else {
    			return icons[9][2];
    		}
    	}
    	else if(meta == 10)
    	{
    		if(side == 3)
    		{
    			return icons[10][0];
    		}
    		else {
    			return icons[10][2];
    		}
    	}
    	
    	return null;
    }
    
	@Override
    @SideOnly(Side.CLIENT)
    public Icon getBlockTexture(IBlockAccess world, int x, int y, int z, int side)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
        
    	if(metadata == 0)
    	{
	        if(side == tileEntity.facing)
	        {
	        	return MekanismUtils.isActive(world, x, y, z) ? icons[0][1] : icons[0][0];
	        }
	        else {
	        	return icons[0][2];
	        }
    	}
    	else if(metadata == 1)
    	{
            if(side == tileEntity.facing)
            {
            	return MekanismUtils.isActive(world, x, y, z) ? icons[1][1] : icons[1][0];
            }
            else {
            	return icons[1][2];
            }
    	}
    	else if(metadata == 2)
    	{
            if(side == tileEntity.facing)
            {
            	return MekanismUtils.isActive(world, x, y, z) ? icons[2][1] : icons[2][0];
            }
            else {
            	return icons[2][2];
            }
    	}
    	else if(metadata == 3)
    	{
            if(side == tileEntity.facing)
            {
            	return MekanismUtils.isActive(world, x, y, z) ? icons[3][1] : icons[3][0];
            }
            else {
            	return icons[3][2];
            }
    	}
    	else if(metadata == 5)
    	{
    		if(side == tileEntity.facing)
    		{
    			return icons[5][0];
    		}
    		else if(side == 0 || side == 1)
    		{
    			return icons[5][2];
    		}
    		else {
    			return icons[5][1];
    		}
    	}
    	else if(metadata == 6)
    	{
    		if(side == tileEntity.facing)
    		{
    			return icons[6][0];
    		}
       		else if(side == 0 || side == 1)
    		{
    			return icons[6][2];
    		}
    		else {
    			return icons[6][1];
    		}
    	}
    	else if(metadata == 7)
    	{
    		if(side == tileEntity.facing)
    		{
    			return icons[7][0];
    		}
       		else if(side == 0 || side == 1)
    		{
    			return icons[7][2];
    		}
    		else {
    			return icons[7][1];
    		}
    	}
    	else if(metadata == 8)
    	{
            if(side == 0 || side == 1)
            {
            	return MekanismUtils.isActive(world, x, y, z) ? icons[8][5] : icons[8][4];
            }
            else {
            	if(side == tileEntity.facing)
            	{
            		return MekanismUtils.isActive(world, x, y, z) ? icons[8][1] : icons[8][0];
            	}
            	else if(side == ForgeDirection.getOrientation(tileEntity.facing).getOpposite().ordinal())
            	{
            		return MekanismUtils.isActive(world, x, y, z) ? icons[8][7] : icons[8][6];
            	}
            	else {
            		return MekanismUtils.isActive(world, x, y, z) ? icons[8][3] : icons[8][2];
            	}
            }
    	}
    	else if(metadata == 9)
    	{
    		if(side == tileEntity.facing)
    		{
    			return MekanismUtils.isActive(world, x, y, z) ? icons[9][1] : icons[9][0];
    		}
    		else {
    			return icons[9][2];
    		}
    	}
    	else if(metadata == 10)
    	{
    		if(side == tileEntity.facing)
    		{
    			return MekanismUtils.isActive(world, x, y, z) ? icons[10][1] : icons[10][0];
    		}
    		else {
    			return icons[10][2];
    		}
    	}
    	
    	return null;
    }
    
	@Override
    public int damageDropped(int i)
    {
    	return i;
    }
    
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int i, CreativeTabs creativetabs, List list)
	{
		list.add(new ItemStack(i, 1, 0));
		list.add(new ItemStack(i, 1, 1));
		list.add(new ItemStack(i, 1, 2));
		list.add(new ItemStack(i, 1, 3));
		
		if(Mekanism.extrasEnabled)
		{
			list.add(new ItemStack(i, 1, 4));
		}
		
		for(RecipeType type : RecipeType.values())
		{
			for(Tier.FactoryTier tier : Tier.FactoryTier.values())
			{
				ItemStack stack = new ItemStack(i, 1, 5+tier.ordinal());
				((IFactory)stack.getItem()).setRecipeType(type.ordinal(), stack);
				list.add(stack);
			}
		}
		
		list.add(new ItemStack(i, 1, 8));
		list.add(new ItemStack(i, 1, 9));
		list.add(new ItemStack(i, 1, 10));
	}
    
    @Override
    public void breakBlock(World world, int x, int y, int z, int i1, int i2)
    {
        TileEntityContainerBlock tileEntity = (TileEntityContainerBlock)world.getBlockTileEntity(x, y, z);

        if (tileEntity != null)
        {
            for (int i = 0; i < tileEntity.getSizeInventory(); ++i)
            {
                ItemStack slotStack = tileEntity.getStackInSlot(i);

                if (slotStack != null)
                {
                    float xRandom = machineRand.nextFloat() * 0.8F + 0.1F;
                    float yRandom = machineRand.nextFloat() * 0.8F + 0.1F;
                    float zRandom = machineRand.nextFloat() * 0.8F + 0.1F;

                    while (slotStack.stackSize > 0)
                    {
                        int j = machineRand.nextInt(21) + 10;

                        if (j > slotStack.stackSize)
                        {
                            j = slotStack.stackSize;
                        }

                        slotStack.stackSize -= j;
                        EntityItem item = new EntityItem(world, (double)((float)x + xRandom), (double)((float)y + yRandom), (double)((float)z + zRandom), new ItemStack(slotStack.itemID, j, slotStack.getItemDamage()));

                        if (slotStack.hasTagCompound())
                        {
                            item.getEntityItem().setTagCompound((NBTTagCompound)slotStack.getTagCompound().copy());
                        }

                        float k = 0.05F;
                        item.motionX = (double)((float)machineRand.nextGaussian() * k);
                        item.motionY = (double)((float)machineRand.nextGaussian() * k + 0.2F);
                        item.motionZ = (double)((float)machineRand.nextGaussian() * k);
                        world.spawnEntityInWorld(item);
                    }
                }
            }
        }
	        
    	super.breakBlock(world, x, y, z, i1, i2);
    }
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int facing, float playerX, float playerY, float playerZ)
    {
    	if(world.isRemote)
    	{
    		return true;
    	}
    	
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(entityplayer.getCurrentEquippedItem() != null)
    	{
	    	if(entityplayer.getCurrentEquippedItem().getItem() instanceof IToolConfigurator)
	    	{
	    		((IToolConfigurator)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);
	    		
	    		int change = 0;
	    		
	    		switch(tileEntity.facing)
	    		{
	    			case 3:
	    				change = 5;
	    				break;
	    			case 5:
	    				change = 2;
	    				break;
	    			case 2:
	    				change = 4;
	    				break;
	    			case 4:
	    				change = 3;
	    				break;
	    		}
	    		
	    		tileEntity.setFacing((short)change);
	    		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
	    		return true;
	    	}
	    	else if(entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench && !entityplayer.getCurrentEquippedItem().getItemName().contains("omniwrench"))
	    	{
	    		if(entityplayer.isSneaking())
	    		{
	    			dismantleBlock(world, x, y, z, false);
	    			return true;
	    		}
	    		
	    		((IToolWrench)entityplayer.getCurrentEquippedItem().getItem()).wrenchUsed(entityplayer, x, y, z);
	    		
	    		int change = 0;
	    		
	    		switch(tileEntity.facing)
	    		{
	    			case 3:
	    				change = 5;
	    				break;
	    			case 5:
	    				change = 2;
	    				break;
	    			case 2:
	    				change = 4;
	    				break;
	    			case 4:
	    				change = 3;
	    				break;
	    		}
	    		
	    		tileEntity.setFacing((short)change);
	    		world.notifyBlocksOfNeighborChange(x, y, z, blockID);
	    		return true;
	    	}
    	}
    	
        if (tileEntity != null)
        {
        	if(!entityplayer.isSneaking())
        	{
        		entityplayer.openGui(Mekanism.instance, MachineType.getFromMetadata(metadata).guiId, world, x, y, z);
        		return true;
        	}
        }
    	return false;
    }
    
    @Override
    public TileEntity createTileEntity(World world, int metadata)
    {
    	return MachineType.getFromMetadata(metadata).create();
    }
	
    @Override
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}
    
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return ClientProxy.RENDER_ID;
	}
	
    @Override
    public boolean removeBlockByPlayer(World world, EntityPlayer player, int x, int y, int z)
    {
    	if(!player.capabilities.isCreativeMode && !world.isRemote && canHarvestBlock(player, world.getBlockMetadata(x, y, z)))
    	{
	    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
	    	
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            
            EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, new ItemStack(Mekanism.MachineBlock, 1, world.getBlockMetadata(x, y, z)));
	        
	        IUpgradeManagement upgrade = (IUpgradeManagement)entityItem.getEntityItem().getItem();
	        upgrade.setEnergyMultiplier(((IUpgradeManagement)tileEntity).getEnergyMultiplier(), entityItem.getEntityItem());
	        upgrade.setSpeedMultiplier(((IUpgradeManagement)tileEntity).getSpeedMultiplier(), entityItem.getEntityItem());
	        
	        IItemElectric electricItem = (IItemElectric)entityItem.getEntityItem().getItem();
	        electricItem.setJoules(tileEntity.electricityStored, entityItem.getEntityItem());
	        
	        if(tileEntity instanceof TileEntityFactory)
	        {
	        	IFactory factoryItem = (IFactory)entityItem.getEntityItem().getItem();
	        	factoryItem.setRecipeType(((TileEntityFactory)tileEntity).recipeType, entityItem.getEntityItem());
	        }
	        
	        world.spawnEntityInWorld(entityItem);
    	}
    	
        return world.func_94571_i(x, y, z);
    }
    
    @Override
    public int idDropped(int i, Random random, int j)
    {
    	return 0;
    }
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    	ItemStack itemStack = new ItemStack(Mekanism.MachineBlock, 1, world.getBlockMetadata(x, y, z));
        
        IUpgradeManagement upgrade = (IUpgradeManagement)itemStack.getItem();
        upgrade.setEnergyMultiplier(((IUpgradeManagement)tileEntity).getEnergyMultiplier(), itemStack);
        upgrade.setSpeedMultiplier(((IUpgradeManagement)tileEntity).getSpeedMultiplier(), itemStack);
        
        IItemElectric electricItem = (IItemElectric)itemStack.getItem();
        electricItem.setJoules(tileEntity.electricityStored, itemStack);
        
        if(tileEntity instanceof TileEntityFactory)
        {
        	IFactory factoryItem = (IFactory)itemStack.getItem();
        	factoryItem.setRecipeType(((TileEntityFactory)tileEntity).recipeType, itemStack);
        }
        
        return itemStack;
	}
	
	@Override
	public ItemStack dismantleBlock(World world, int x, int y, int z, boolean returnBlock) 
	{
    	TileEntityElectricBlock tileEntity = (TileEntityElectricBlock)world.getBlockTileEntity(x, y, z);
    	ItemStack itemStack = new ItemStack(Mekanism.MachineBlock, 1, world.getBlockMetadata(x, y, z));
        
        IUpgradeManagement upgrade = (IUpgradeManagement)itemStack.getItem();
        upgrade.setEnergyMultiplier(((IUpgradeManagement)tileEntity).getEnergyMultiplier(), itemStack);
        upgrade.setSpeedMultiplier(((IUpgradeManagement)tileEntity).getSpeedMultiplier(), itemStack);
        
        IItemElectric electricItem = (IItemElectric)itemStack.getItem();
        electricItem.setJoules(tileEntity.electricityStored, itemStack);
        
        if(tileEntity instanceof TileEntityFactory)
        {
        	IFactory factoryItem = (IFactory)itemStack.getItem();
        	factoryItem.setRecipeType(((TileEntityFactory)tileEntity).recipeType, itemStack);
        }
        
        world.func_94571_i(x, y, z);
        
        if(!returnBlock)
        {
            float motion = 0.7F;
            double motionX = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionY = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            double motionZ = (world.rand.nextFloat() * motion) + (1.0F - motion) * 0.5D;
            
            EntityItem entityItem = new EntityItem(world, x + motionX, y + motionY, z + motionZ, itemStack);
	        
            world.spawnEntityInWorld(entityItem);
        }
        
        return itemStack;
	}

	@Override
	public boolean canDismantle(World world, int x, int y, int z) 
	{
		return true;
	}
	
	public static enum MachineType
	{
		ENRICHMENT_CHAMBER(0, 3, 2000, TileEntityEnrichmentChamber.class, false),
		OSMIUM_COMPRESSOR(1, 4, 2000, TileEntityOsmiumCompressor.class, false),
		COMBINER(2, 5, 2000, TileEntityCombiner.class, false),
		CRUSHER(3, 6, 2000, TileEntityCrusher.class, false),
		THEORETICAL_ELEMENTIZER(4, 7, 4800, TileEntityTheoreticalElementizer.class, true),
		BASIC_FACTORY(5, 11, 6000, TileEntityFactory.class, false),
		ADVANCED_FACTORY(6, 11, 10000, TileEntityAdvancedFactory.class, false),
		ELITE_FACTORY(7, 11, 14000, TileEntityEliteFactory.class, false),
		METALLURGIC_INFUSER(8, 12, 2000, TileEntityMetallurgicInfuser.class, false),
		PURIFICATION_CHAMBER(9, 15, 12000, TileEntityPurificationChamber.class, false),
		ENERGIZED_SMELTER(10, 16, 2000, TileEntityEnergizedSmelter.class, false);
		
		public int meta;
		public int guiId;
		public double baseEnergy;
		public Class<? extends TileEntity> tileEntityClass;
		public boolean hasModel;
		
		private MachineType(int i, int j, double k, Class<? extends TileEntity> tileClass, boolean model)
		{
			meta = i;
			guiId = j;
			baseEnergy = k;
			tileEntityClass = tileClass;
			hasModel = model;
		}
		
		public static MachineType getFromMetadata(int meta)
		{
			return values()[meta];
		}
		
		public TileEntity create()
		{
			TileEntity tileEntity;
			
			try {
				tileEntity = tileEntityClass.newInstance();
				return tileEntity;
			} catch(Exception e) {
				System.err.println("[Mekanism] Unable to indirectly create tile entity.");
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public String toString()
		{
			return Integer.toString(meta);
		}
	}
}
