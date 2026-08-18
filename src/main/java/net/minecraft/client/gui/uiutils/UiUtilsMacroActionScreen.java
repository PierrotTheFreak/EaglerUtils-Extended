package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import java.util.List;

/** Editor for one macro action. */
public class UiUtilsMacroActionScreen extends Screen {
    private static final int PANEL_WIDTH=420,PANEL_HEIGHT=300;
    private final Screen parent;
    private final List<UiUtilsMacroAction> actions;
    private UiUtilsMacroAction.Type selectedType=UiUtilsMacroAction.Type.CHAT;
    private TextFieldWidget valueField,amountField;
    private boolean enabled=true;

    public UiUtilsMacroActionScreen(Screen parent,List<UiUtilsMacroAction> actions){super(new StringTextComponent("Add Macro Action"));this.parent=parent;this.actions=actions;}
    @Override protected void init(){int left=getPanelLeft();valueField=new TextFieldWidget(font,left+120,55,280,20,"Value");valueField.setMaxStringLength(256);amountField=new TextFieldWidget(font,left+120,80,120,20,"Amount");amountField.setMaxStringLength(8);addButton(valueField);addButton(amountField);setFocused(valueField);}
    private int getPanelLeft(){return width/2-PANEL_WIDTH/2;}
    private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private void focus(TextFieldWidget field,double mx,double my,int button){field.mouseClicked(mx,my,button);field.setFocused(true);setFocused(field);}
    private UiUtilsMacroAction createCurrentAction(){UiUtilsMacroAction a=new UiUtilsMacroAction(selectedType);a.setText(valueField==null?"":valueField.getText());try{a.setAmount(Integer.parseInt(amountField==null?"0":amountField.getText().trim()));}catch(Exception ignored){a.setAmount(0);}a.setEnabled(enabled);return a;}
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(valueField!=null&&inside(mx,my,valueField.x,valueField.y,valueField.getWidth(),20)){focus(valueField,mx,my,button);return true;}
        if(amountField!=null&&inside(mx,my,amountField.x,amountField.y,amountField.getWidth(),20)){focus(amountField,mx,my,button);return true;}
        if(button==0){int left=getPanelLeft();
            if(inside(mx,my,left+10,105,200,24)){UiUtilsMacroAction.Type[] v=UiUtilsMacroAction.Type.values();selectedType=v[(selectedType.ordinal()+1)%v.length];return true;}
            if((selectedType==UiUtilsMacroAction.Type.DELAY_PACKETS||selectedType==UiUtilsMacroAction.Type.SEND_PACKETS)&&inside(mx,my,left+220,105,180,24)){enabled=!enabled;return true;}
            if(selectedType==UiUtilsMacroAction.Type.DELAY_PACKETS&&inside(mx,my,left+220,135,180,24)){UiUtilsMacroAction a=createCurrentAction();mc.displayGuiScreen(new UiUtilsMacroPacketScreen(this,a,actions));return true;}
            if(inside(mx,my,left+10,250,130,24)){actions.add(createCurrentAction());mc.displayGuiScreen(parent);return true;}
            if(inside(mx,my,left+280,250,130,24)){mc.displayGuiScreen(parent);return true;}
        }
        return super.mouseClicked(mx,my,button);
    }
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){if(valueField!=null&&valueField.isFocused()&&valueField.keyPressed(keyCode,scanCode,modifiers))return true;if(amountField!=null&&amountField.isFocused()&&amountField.keyPressed(keyCode,scanCode,modifiers))return true;if(keyCode==256){mc.displayGuiScreen(parent);return true;}return super.keyPressed(keyCode,scanCode,modifiers);}
    @Override public boolean charTyped(char c,int modifiers){if(valueField!=null&&valueField.isFocused()&&valueField.charTyped(c,modifiers))return true;if(amountField!=null&&amountField.isFocused()&&amountField.charTyped(c,modifiers))return true;return super.charTyped(c,modifiers);}
    @Override public void render(int mx,int my,float partialTicks){renderBackground();int left=getPanelLeft();fill(left,0,left+PANEL_WIDTH,PANEL_HEIGHT,0xEE111111);drawCenteredString(font,"Add Macro Action",width/2,10,0xFFFFFF);drawString(font,"Action Type:",left+10,63,0xAAAAAA);drawButton(left+10,105,200,24,selectedType.name(),mx,my);drawString(font,"Text:",left+10,61,0xAAAAAA);drawString(font,"Amount:",left+10,86,0xAAAAAA);
        if(selectedType==UiUtilsMacroAction.Type.DELAY_PACKETS||selectedType==UiUtilsMacroAction.Type.SEND_PACKETS)drawButton(left+220,105,180,24,enabled?"Enabled: ON":"Enabled: OFF",mx,my);
        if(selectedType==UiUtilsMacroAction.Type.DELAY_PACKETS){drawButton(left+220,135,180,24,"Configure Packets",mx,my);drawString(font,"Packets remain delayed until a later action turns delay off.",left+10,165,0xAAAAAA);}
        if(selectedType==UiUtilsMacroAction.Type.CHAT)drawString(font,"Value is the chat message.",left+10,115,0xAAAAAA);
        if(selectedType==UiUtilsMacroAction.Type.WAIT)drawString(font,"Amount is ticks to wait.",left+10,115,0xAAAAAA);
        if(selectedType==UiUtilsMacroAction.Type.WAIT_FOR_PACKETS)drawString(font,"Amount is packets to wait for.",left+10,115,0xAAAAAA);
        drawButton(left+10,250,130,24,"Add Action",mx,my);drawButton(left+280,250,130,24,"Cancel",mx,my);super.render(mx,my,partialTicks);}
    private void drawButton(int x,int y,int w,int h,String text,int mx,int my){boolean hover=inside(mx,my,x,y,w,h);fill(x,y,x+w,y+h,hover?0xFF555555:0xFF333333);drawString(font,text,x+8,y+7,0xFFFFFF);}
}