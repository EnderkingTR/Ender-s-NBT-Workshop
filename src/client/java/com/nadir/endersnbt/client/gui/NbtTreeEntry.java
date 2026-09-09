package com.nadir.endersnbt.client.gui;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class NbtTreeEntry extends ObjectSelectionList.Entry<NbtTreeEntry> {
    private final NbtTreeListWidget parentList;
    private final NbtNode node;
    private long lastClickTime = 0;

    public NbtTreeEntry(NbtTreeListWidget parentList, NbtNode node) {
        this.parentList = parentList;
        this.node = node;
    }

    @Override
    public Component getNarration() {
        return Component.literal(node.name);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
        int indent = node.depth * 10;
        int startX = left + indent;
        
        if (node.hasChildren()) {
            String arrow = node.expanded ? "[-]" : "[+]";
            guiGraphics.drawString(Minecraft.getInstance().font, arrow, startX, top + 2, 0xAAAAAA, false);
            startX += 15;
        } else {
            startX += 15;
        }

        String text = node.name + ": " + node.tag.toString();
        guiGraphics.drawString(Minecraft.getInstance().font, text, startX, top + 2, 0xFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (node.hasChildren()) {
                node.expanded = !node.expanded;
                parentList.rebuildList();
                return true;
            } else {
                long time = net.minecraft.Util.getMillis();
                if (time - lastClickTime < 250L) {
                    parentList.getParentScreen().openEditBox(node, (int)mouseX, (int)mouseY - 8, 150);
                    return true;
                }
                lastClickTime = time;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
