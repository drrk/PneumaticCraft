package pneumaticCraft.common.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkPosition;
import pneumaticCraft.api.item.IProgrammable;
import pneumaticCraft.common.progwidgets.IProgWidget;
import pneumaticCraft.common.progwidgets.ProgWidgetAreaItemBase;
import pneumaticCraft.common.tileentity.TileEntityProgrammer;
import pneumaticCraft.common.util.IOHelper;

public class DroneAIExternalProgram extends DroneAIBlockInteraction{

    private final DroneAIManager aiManager;
    private final Set<ChunkPosition> traversedPositions = new HashSet<ChunkPosition>();
    private int curSlot;
    private NBTTagCompound curProgramTag; //Used to see if changes have been made to the program while running it.

    public DroneAIExternalProgram(IDroneBase drone, ProgWidgetAreaItemBase widget){
        super(drone, widget);
        aiManager = new DroneAIManager(drone, new ArrayList<IProgWidget>());
    }

    @Override
    public boolean shouldExecute(){
        if(super.shouldExecute()) {
            traversedPositions.clear();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean moveToPositions(){
        return false;
    }

    @Override
    protected boolean isValidPosition(ChunkPosition pos){
        if(traversedPositions.add(pos)) {
            curSlot = 0;
            TileEntity te = drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ);
            return te instanceof IInventory;
        }
        return false;
    }

    @Override
    protected boolean doBlockInteraction(ChunkPosition pos, double distToBlock){
        IInventory inv = IOHelper.getInventoryForTE(drone.getWorld().getTileEntity(pos.chunkPosX, pos.chunkPosY, pos.chunkPosZ));
        if(curProgramTag != null) {
            if(curSlot < inv.getSizeInventory()) {
                ItemStack stack = inv.getStackInSlot(curSlot);
                if(stack != null && curProgramTag.equals(stack.getTagCompound())) {
                    aiManager.onUpdateTasks();
                    if(aiManager.isIdling() || isRunningSameProgram(aiManager.getCurrentAI())) {
                        curProgramTag = null;
                        curSlot++;
                    }
                } else {
                    curProgramTag = null;
                    aiManager.setWidgets(new ArrayList<IProgWidget>());
                }
            }
            return true;
        } else {
            while(curSlot < inv.getSizeInventory()) {
                ItemStack stack = inv.getStackInSlot(curSlot);
                if(stack != null && stack.getItem() instanceof IProgrammable) {
                    IProgrammable programmable = (IProgrammable)stack.getItem();
                    if(programmable.canProgram(stack) && programmable.usesPieces(stack)) {
                        List<IProgWidget> widgets = TileEntityProgrammer.getProgWidgets(stack);

                        boolean areWidgetsValid = true;
                        for(IProgWidget widget : widgets) {
                            if(!drone.isProgramApplicable(widget)) {
                                areWidgetsValid = false;
                                break;
                            }
                        }

                        if(areWidgetsValid) {
                            aiManager.setWidgets(widgets);
                            curProgramTag = stack.getTagCompound();
                            if(!aiManager.isIdling()) return true;
                        }
                    }
                }
                curSlot++;
            }
            return false;
        }
    }

    //Prevent a memory leak, as a result of the same External program recursively calling itself.
    private boolean isRunningSameProgram(EntityAIBase ai){
        return ai instanceof DroneAIExternalProgram && curProgramTag.equals(((DroneAIExternalProgram)ai).curProgramTag);
    }

}
