package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketDirection;
import net.minecraft.util.text.StringTextComponent;

/** Searchable, directional packet-delay configuration. */
public class UiUtilsPacketSettingsScreen extends Screen {
    private static final int W=360, TOP=91, ROW=18, VISIBLE=8;
    private final Screen parent;
    private TextFieldWidget searchField, delayField;
    private PacketDirection direction=PacketDirection.SERVERBOUND;
    private int scroll;
    public UiUtilsPacketSettingsScreen(Screen parent){super(new StringTextComponent("UI Utils Packet Delay Settings"));this.parent=parent;}
    @Override protected void init(){int left=getLeft();searchField=new TextFieldWidget(font,left+10,38,W-20,20,"Search packets...");searchField.setMaxStringLength(128);addButton(searchField);delayField=new TextFieldWidget(font,left+10,65,90,20,"250");delayField.setMaxStringLength(5);delayField.setText(String.valueOf(UiUtilsPacketManager.getDelayMilliseconds()));addButton(delayField);searchField.setFocused(true);setFocused(searchField);scroll=0;}
    private int getLeft(){return width/2-W/2;}
    private String[] packets(){String search=searchField==null?"":searchField.getText();String[] all=UiUtilsPacketManager.getAllPacketTypes(direction);String[] tmp=new String[all.length];int n=0;for(String name:all)if(UiUtilsPacketManager.packetMatchesSearch(name,search))tmp[n++]=name;String[] r=new String[n];System.arraycopy(tmp,0,r,0,n);return r;}
    private int maxScroll(String[] p){return Math.max(0,p.length*ROW-VISIBLE*ROW);}
    private void clamp(String[] p){scroll=Math.max(0,Math.min(scroll,maxScroll(p)));}
    private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private void setDelayFromField(){if(delayField==null)return;try{UiUtilsPacketManager.setDelayMilliseconds(Long.parseLong(delayField.getText().trim()));delayField.setText(String.valueOf(UiUtilsPacketManager.getDelayMilliseconds()));}catch(Exception ignored){}}
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(searchField!=null&&inside(mouseX,mouseY,searchField.x,searchField.y,searchField.getWidth(),20)){searchField.mouseClicked(mouseX,mouseY,button);setFocused(searchField);return true;}
        if(delayField!=null&&inside(mouseX,mouseY,delayField.x,delayField.y,delayField.getWidth(),20)){delayField.mouseClicked(mouseX,mouseY,button);setFocused(delayField);return true;}
        int left=getLeft();if(button==0){if(inside(mouseX,mouseY,left+110,65,110,20)){setDelayFromField();return true;}if(inside(mouseX,mouseY,left+228,65,122,20)){UiUtilsPacketManager.setDelayMilliseconds(UiUtilsPacketManager.getDelayMilliseconds()+50);if(delayField!=null)delayField.setText(String.valueOf(UiUtilsPacketManager.getDelayMilliseconds()));return true;}if(inside(mouseX,mouseY,left+10,8,100,22)){UiUtilsPacketManager.delayAllPackets(direction);return true;}if(inside(mouseX,mouseY,left+118,8,100,22)){UiUtilsPacketManager.clearDelayedPacketTypes(direction);return true;}if(inside(mouseX,mouseY,left+226,8,124,22)){direction=direction==PacketDirection.SERVERBOUND?PacketDirection.CLIENTBOUND:PacketDirection.SERVERBOUND;scroll=0;return true;}String[] p=packets();for(int i=0;i<p.length;++i){int y=TOP+i*ROW-scroll;if(y<TOP||y>=TOP+VISIBLE*ROW)continue;if(inside(mouseX,mouseY,left+10,y,W-20,ROW)){UiUtilsPacketManager.togglePacketDelayed(p[i],direction);return true;}}if(inside(mouseX,mouseY,left+10,247,140,22)){closeScreen();return true;}}return super.mouseClicked(mouseX,mouseY,button);}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double delta){scroll-=(int)delta*ROW;clamp(packets());return true;}
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){if(keyCode==256){closeScreen();return true;}if(delayField!=null&&delayField.isFocused()&&delayField.keyPressed(keyCode,scanCode,modifiers)){if(keyCode==257||keyCode==335)setDelayFromField();return true;}if(searchField!=null&&searchField.isFocused()&&searchField.keyPressed(keyCode,scanCode,modifiers))return true;return super.keyPressed(keyCode,scanCode,modifiers);}
    @Override public boolean charTyped(char c,int modifiers){if(delayField!=null&&delayField.isFocused()&&delayField.charTyped(c,modifiers))return true;if(searchField!=null&&searchField.isFocused()&&searchField.charTyped(c,modifiers))return true;return super.charTyped(c,modifiers);}
    private void closeScreen(){if(mc!=null)mc.displayGuiScreen(parent);}
    private void drawButton(int x,int y,int w,int h,String text,int mx,int my){boolean hover=inside(mx,my,x,y,w,h);fill(x,y,x+w,y+h,hover?0xFF555555:0xFF333333);fill(x,y,x+w,y+1,0xFF777777);fill(x,y+h-1,x+w,y+h,0xFF777777);drawCenteredString(font,text,x+w/2,y+7,0xFFFFFF);}
    @Override public void render(int mouseX,int mouseY,float partialTicks){renderBackground();int left=getLeft();fill(left,4,left+W,290,0xEE111111);drawCenteredString(font,"UI Utils Packet Delay",width/2,10,0xFFFFFF);drawButton(left+10,8,100,22,"Delay All",mouseX,mouseY);drawButton(left+118,8,100,22,"Clear All",mouseX,mouseY);drawButton(left+226,8,124,22,direction==PacketDirection.SERVERBOUND?"C -> S":"S -> C",mouseX,mouseY);drawString(font,"Delay (ms)",left+10,56,0xAAAAAA);drawButton(left+110,65,110,20,"Apply",mouseX,mouseY);drawButton(left+228,65,122,20,"+50 ms",mouseX,mouseY);if(delayField!=null)delayField.render(mouseX,mouseY,partialTicks);if(searchField!=null)searchField.render(mouseX,mouseY,partialTicks);drawString(font,direction==PacketDirection.SERVERBOUND?"Client -> Server":"Server -> Client",left+10,TOP-13,0xAAAAAA);String[] p=packets();for(int i=0;i<p.length;++i){int y=TOP+i*ROW-scroll;if(y<TOP||y>=TOP+VISIBLE*ROW)continue;String name=p[i];boolean locked=UiUtilsPacketManager.isKeepAlivePacket(name);boolean selected=UiUtilsPacketManager.isPacketDelayed(name,direction);fill(left+10,y,left+W-10,y+ROW-1,locked?0xFF222222:selected?0xFF335533:0xFF333333);drawString(font,(locked?"[-] ":selected?"[x] ":"[ ] ")+name,left+15,y+5,locked?0x777777:selected?0x55FF55:0xFFFFFF);}drawButton(left+10,247,140,22,"Done",mouseX,mouseY);drawString(font,"Selected: "+UiUtilsPacketManager.getSelectedPacketTypes(direction).size(),left+165,254,0xAAAAAA);drawString(font,"Delay: "+UiUtilsPacketManager.getDelayMilliseconds()+" ms",left+10,273,0xAAAAAA);super.render(mouseX,mouseY,partialTicks);}
}
