package net.minecraft.client.gui.uiutils;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

/** Packet replay controls and recording history. */
public class UiUtilsPacketReplayScreen extends Screen {
    private final Screen parent;
    private int scroll;
    public UiUtilsPacketReplayScreen(Screen parent) { super(new StringTextComponent("UI Utils Packet Replay")); this.parent=parent; }
    private int left(){return width/2-190;}
    private boolean inside(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}
    private void button(int x,int y,int w,String text,int mx,int my){boolean hover=inside(mx,my,x,y,w,22);fill(x,y,x+w,y+22,hover?0xFF555555:0xFF333333);fill(x,y,x+w,y+1,0xFF777777);fill(x,y+21,x+w,y+22,0xFF777777);drawCenteredString(font,text,x+w/2,y+7,0xFFFFFF);}
    @Override public boolean mouseClicked(double mouseX,double mouseY,int button){
        if(button!=0)return super.mouseClicked(mouseX,mouseY,button);
        int x=left();
        if(inside(mouseX,mouseY,x+10,38,105,22)){UiUtilsPacketReplay.toggleRecording();return true;}
        if(inside(mouseX,mouseY,x+123,38,105,22)){UiUtilsPacketReplay.clear();scroll=0;return true;}
        if(inside(mouseX,mouseY,x+241,38,129,22)){replay();return true;}
        if(inside(mouseX,mouseY,x+10,68,105,22)){UiUtilsPacketReplay.clear();scroll=0;return true;}
        if(inside(mouseX,mouseY,x+123,68,105,22)){if(mc!=null)mc.displayGuiScreen(parent);return true;}
        return super.mouseClicked(mouseX,mouseY,button);
    }
    private void replay(){if(mc==null||!(mc.player instanceof ClientPlayerEntity))return;ClientPlayerEntity p=(ClientPlayerEntity)mc.player;if(p.connection!=null)UiUtilsPacketReplay.replayOutgoing(p.connection.getNetworkManager());}
    @Override public boolean mouseScrolled(double mouseX,double mouseY,double delta){scroll-=((int)delta);List<UiUtilsPacketReplay.Record> r=UiUtilsPacketReplay.getRecords();scroll=Math.max(0,Math.min(scroll,Math.max(0,r.size()-9)));return true;}
    @Override public boolean keyPressed(int keyCode,int scanCode,int modifiers){if(keyCode==256){if(mc!=null)mc.displayGuiScreen(parent);return true;}return super.keyPressed(keyCode,scanCode,modifiers);}
    @Override public void render(int mouseX,int mouseY,float partialTicks){
        renderBackground();int x=left();fill(x,4,x+380,274,0xEE111111);drawCenteredString(font,"UI Utils Packet Replay",x+190,10,0xFFFFFF);
        drawString(font,"Record outgoing/incoming packet traffic, then replay C -> S packets.",x+10,26,0xAAAAAA);
        button(x+10,38,105,UiUtilsPacketReplay.isRecording()?"Recording: ON":"Recording: OFF",mouseX,mouseY);button(x+123,38,105,"Clear",mouseX,mouseY);button(x+241,38,129,"Replay C -> S",mouseX,mouseY);
        button(x+10,68,105,"Clear",mouseX,mouseY);button(x+123,68,105,"Done",mouseX,mouseY);
        List<UiUtilsPacketReplay.Record> records=UiUtilsPacketReplay.getRecords();int start=Math.min(scroll,Math.max(0,records.size()-1));int end=Math.min(records.size(),start+9);
        for(int i=start;i<end;++i){UiUtilsPacketReplay.Record r=records.get(i);int y=100+(i-start)*18;fill(x+10,y,x+370,y+17,0xFF333333);drawString(font,r.direction.toString(),x+14,y+5,0x55AAFF);drawString(font,r.name,x+70,y+5,0xFFFFFF);drawString(font,r.size+" B",x+320,y+5,0xAAAAAA);}
        drawString(font,"Records: "+records.size(),x+240,75,0xAAAAAA);super.render(mouseX,mouseY,partialTicks);
    }
}
