package pneumaticCraft.common.progwidgets;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import pneumaticCraft.client.gui.GuiProgrammer;
import pneumaticCraft.client.gui.programmer.GuiProgWidgetItemAssign;
import pneumaticCraft.common.ai.DroneAIManager;
import pneumaticCraft.common.ai.IDroneBase;
import pneumaticCraft.common.item.ItemPlasticPlants;
import pneumaticCraft.lib.Textures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProgWidgetItemAssign extends ProgWidget implements IVariableWidget{
    private String variable = "";
    private DroneAIManager aiManager;

    @Override
    public boolean hasStepInput(){
        return true;
    }

    @Override
    public boolean hasBlacklist(){
        return false;
    }

    @Override
    public Class<? extends IProgWidget> returnType(){
        return null;
    }

    @Override
    public Class<? extends IProgWidget>[] getParameters(){
        return new Class[]{ProgWidgetItemFilter.class};
    }

    @Override
    public String getWidgetString(){
        return "itemAssign";
    }

    @Override
    public int getCraftingColorIndex(){
        return ItemPlasticPlants.BURST_PLANT_DAMAGE;
    }

    @Override
    public WidgetDifficulty getDifficulty(){
        return WidgetDifficulty.ADVANCED;
    }

    @Override
    public void addErrors(List<String> curInfo){
        super.addErrors(curInfo);
        if(variable.equals("")) {
            curInfo.add("gui.progWidget.general.error.emptyVariable");
        }
    }

    @Override
    protected ResourceLocation getTexture(){
        return Textures.PROG_WIDGET_ITEM_ASSIGN;
    }

    @Override
    public void setAIManager(DroneAIManager aiManager){
        this.aiManager = aiManager;
    }

    @Override
    public IProgWidget getOutputWidget(IDroneBase drone, List<IProgWidget> allWidgets){
        if(!variable.equals("")) {
            ProgWidgetItemFilter filter = (ProgWidgetItemFilter)getConnectedParameters()[0];
            aiManager.setItem(variable, filter != null ? filter.getFilter() : null);
        }
        return super.getOutputWidget(drone, allWidgets);
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        tag.setString("variable", variable);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        variable = tag.getString("variable");
    }

    public String getVariable(){
        return variable;
    }

    public void setVariable(String variable){
        this.variable = variable;
    }

    @Override
    public void getTooltip(List<String> curTooltip){
        super.getTooltip(curTooltip);
        curTooltip.add("Setting variable: \"" + variable + "\"");
    }

    @Override
    public String getExtraStringInfo(){
        return "\"" + variable + "\"";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getOptionWindow(GuiProgrammer guiProgrammer){
        return new GuiProgWidgetItemAssign(this, guiProgrammer);
    }
}
