package mekanism.common;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mekanism.client.GuiControlPanel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

/**
 * Block class for handling multiple metal block IDs.
 * 0: Osmium Block
 * 1: Bronze Block
 * 2: Refined Obsidian
 * 3: Coal Block
 * 4: Refined Glowstone
 * 5: Steel Block
 * 6: Control Panel
 * 7: Teleporter
 * 8: Teleporter Frame
 * 9: Steel Casing
 * @author AidanBrady
 *
 */
public class BlockBasic extends Block
{
	public Icon[] icons = new Icon[256];
	public BlockBasic(int id)
	{
		super(id, Material.iron);
		setHardness(5F);
		setResistance(10F);
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void func_94332_a(IconRegister register)
	{
		icons[0] = register.func_94245_a("mekanism:OsmiumBlock");
		icons[1] = register.func_94245_a("mekanism:BronzeBlock");
		icons[2] = register.func_94245_a("mekanism:RefinedObsidian");
		icons[3] = register.func_94245_a("mekanism:CoalBlock");
		icons[4] = register.func_94245_a("mekanism:RefinedGlowstone");
		icons[5] = register.func_94245_a("mekanism:SteelBlock");
		icons[6] = register.func_94245_a("mekanism:ControlPanel");
		icons[7] = register.func_94245_a("mekanism:Teleporter");
		icons[8] = register.func_94245_a("mekanism:TeleporterFrame");
		icons[9] = register.func_94245_a("mekanism:SteelCasing");
	}
	
	@Override
	public Icon getBlockTextureFromSideAndMetadata(int side, int meta)
	{
		return icons[meta];
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
		list.add(new ItemStack(i, 1, 4));
		list.add(new ItemStack(i, 1, 5));
		//list.add(new ItemStack(i, 1, 6));
		list.add(new ItemStack(i, 1, 7));
		list.add(new ItemStack(i, 1, 8));
		list.add(new ItemStack(i, 1, 9));
	}
	
	@Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int i1, float f1, float f2, float f3)
    {
    	int metadata = world.getBlockMetadata(x, y, z);
    	
    	if(metadata == 2)
    	{
    		if(entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, 1, world, x, y, z);
    			return true;
    		}
    	}
    	else if(metadata == 6)
    	{
    		if(!entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, 9, world, x, y, z);
    			return true;
    		}
    	}
    	else if(metadata == 7)
    	{
    		if(entityplayer.isSneaking())
    		{
    			entityplayer.openGui(Mekanism.instance, 13, world, x, y, z);
    			return true;
    		}
    		if(!world.isRemote)
    		{
    			TileEntityTeleporter tileEntity = (TileEntityTeleporter)world.getBlockTileEntity(x, y, z);
    			
    			if(tileEntity.canTeleport() == 1)
    			{
    				tileEntity.teleport();
    			}
    		}
    	}
        return false;
    }
    
	@Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) 
    {
        int metadata = world.getBlockMetadata(x, y, z);
        switch(metadata)
        {
        	case 2:
        		return 8;
        	case 4:
        		return 15;
        	case 8:
        		return 12;
        }
        return 0;
    }
	
	@Override
	public boolean hasTileEntity(int metadata)
	{
		return metadata == 6 || metadata == 7;
	}
	
	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		switch(metadata)
		{
		     case 6:
		    	 return new TileEntityControlPanel();
		     case 7:
		    	 return new TileEntityTeleporter();
		}
		return null;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entityliving, ItemStack itemstack)
	{
		world.markBlockForRenderUpdate(x, y, z);
		world.updateAllLightTypes(x, y, z);
	}
	
    @Override
    public void breakBlock(World world, int x, int y, int z, int i1, int i2)
    {
        TileEntity tile = world.getBlockTileEntity(x, y, z);

        if (tile instanceof TileEntityContainerBlock)
        {
        	Random random = new Random();
        	TileEntityContainerBlock tileEntity = (TileEntityContainerBlock)tile;
        	
            for (int i = 0; i < tileEntity.getSizeInventory(); ++i)
            {
                ItemStack slotStack = tileEntity.getStackInSlot(i);

                if (slotStack != null)
                {
                    float xRandom = random.nextFloat() * 0.8F + 0.1F;
                    float yRandom = random.nextFloat() * 0.8F + 0.1F;
                    float zRandom = random.nextFloat() * 0.8F + 0.1F;

                    while (slotStack.stackSize > 0)
                    {
                        int j = random.nextInt(21) + 10;

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
                        item.motionX = (double)((float)random.nextGaussian() * k);
                        item.motionY = (double)((float)random.nextGaussian() * k + 0.2F);
                        item.motionZ = (double)((float)random.nextGaussian() * k);
                        world.spawnEntityInWorld(item);
                    }
                }
            }
        }
	        
    	super.breakBlock(world, x, y, z, i1, i2);
    }
}