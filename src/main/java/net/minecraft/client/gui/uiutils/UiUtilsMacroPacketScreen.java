package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import java.util.List;

/** Packet selector used by a macro's Delay Packets action. */
public class UiUtilsMacroPacketScreen extends Screen {
    private static final int PANEL_WIDTH=360,PANEL_HEIGHT=270,ENTRY_HEIGHT=18,LIST_TOP=58,VISIBLE_ENTRIES=9;
    private final Screen parent;
    private final UiUtilsMacroAction action;
    private final List<UiUtilsMacroAction> ignoredActions;
    private TextFieldWidget searchField;
    private int scrollOffset;
    public UiUtilsMacroPacketScreen(Screen parent,UiUtilsMacroAction action,List<UiUtilsMacroAction> ignoredActions){super(new StringTextComponent("Macro Packet Selection"));this.parent=parent;this.action=action;this.ignoredActions=ignoredActions;}
    @Override protected void init(){int left=getPanelLeft();searchField=new TextFieldWidget(font,left+10,32,PANEL_WIDTH-20,20,"Search packets...");searchField.setMaxStringLength(128);addButton(searchField);setFocused(searchField);}
    private int getPanelLeft(){return width/2-PANEL_WIDTH/2;}
    private String[] getPackets(){String query=searchField==null?"":searchField.getText();String[] all=UiUtilsPacketManager.getAllPacketTypes();String[] temp=new String[all.length];int count=0;for(String packet:all)if(UiUtilsPacketManager.packetMatchesSearch(packet,query))temp[count++]=packet;String[] result=new String[count];System.arraycopy(temp,0,result,0,count);return result;}
    private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(searchField!=null&&inside(mx,my,searchField.x,searchField.y,searchField.getWidth(),20)){searchField.mouseClicked(mx,my,button);searchField.setFocused(true);setFocused(searchField);return true;}
        if(button==0){int left=getPanelLeft();String[] packets=getPackets();for(int i=0;i<packets.length;i++){int y=LIST_TOP+i*ENTRY_HEIGHT-scrollOffset;if(y<LIST_TOP||y>=LIST_TOP+VISIBLE_ENTRIES*ENTRY_HEIGHT)continue;if(inside(mx,my,left+10,y,PANEL_WIDTH-20,ENTRY_HEIGHT)){String packet=packets[i];if(!UiUtilsPacketManager.isKeepAlivePacket(packet)){if(action.hasPacketType(packet))action.removePacketType(packet);else action.addPacketType(packet);}return true;}}
            if(inside(mx,my,left+10,235,100,22)){action.clearPacketTypes();for(String packet:UiUtilsPacketManager.getAllPacketTypes())if(!UiUtilsPacketManager.isKeepAlivePacket(packet))action.addPacketType(packet);return true;}
            if(inside(mx,my,left+118,235,100,22)){action.clearPacketTypes();return true;}
            if(inside(mx,my,left+226,235,110,22)){mc.displayGuiScreen(parent);return true;}}
        return super.mouseClicked(mx,my,button);
    }
    @Override public boolean mouseScrolled(double mx,double my,double delta){String[] packets=getPackets();int maximum=Math.max(0,(packets.length-VISIBLE_ENTRIES)*ENTRY_HEIGHT);scrollOffset-=(int)delta*ENTRY_HEIGHT;if(scrollOffset<0)scrollOffset=0;if(scrollOffset>maximum)scrollOffset=maximum;return true;}
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){if(searchField!=null&&searchField.isFocused()&&searchField.keyPressed(keyCode,scanCode,modifiers))return true;if(keyCode==256){mc.displayGuiScreen(parent);return true;}return super.keyPressed(keyCode,scanCode,modifiers);}
    @Override public boolean charTyped(char c,int modifiers){if(searchField!=null&&searchField.isFocused()&&searchField.charTyped(c,modifiers))return true;return super.charTyped(c,modifiers);}
    @Override public void render(int mx,int my,float partialTicks){renderBackground();int left=getPanelLeft();fill(left,0,left+PANEL_WIDTH,PANEL_HEIGHT,0xEE111111);drawCenteredString(font,"Macro Packet Selection",width/2,10,0xFFFFFF);String[] packets=getPackets();for(int i=0;i<packets.length;i++){int y=LIST_TOP+i*ENTRY_HEIGHT-scrollOffset;if(y<LIST_TOP||y>=LIST_TOP+VISIBLE_ENTRIES*ENTRY_HEIGHT)continue;String packet=packets[i];boolean locked=UiUtilsPacketManager.isKeepAlivePacket(packet),selected=action.hasPacketType(packet);fill(left+10,y,left+PANEL_WIDTH-10,y+ENTRY_HEIGHT-1,locked?0xFF222222:selected?0xFF335533:0xFF333333);drawString(font,(locked?"[-] ":selected?"[x] ":"[ ] ")+packet,left+15,y+5,locked?0x777777:selected?0x55FF55:0xFFFFFF);}drawButton(left+10,235,100,22,"Select All",mx,my);drawButton(left+118,235,100,22,"Clear",mx,my);drawButton(left+226,235,110,22,"Done",mx,my);drawString(font,"Selected: "+action.getPacketTypes().size(),left+10,220,0xAAAAAA);super.render(mx,my,partialTicks);}
    private void drawButton(int x,int y,int w,int h,String text,int mx,int my){boolean hover=inside(mx,my,x,y,w,h);fill(x,y,x+w,y+h,hover?0xFF555555:0xFF333333);drawString(font,text,x+8,y+7,0xFFFFFF);}
}