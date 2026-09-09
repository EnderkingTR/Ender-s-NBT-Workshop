package com.nadir.endersnbt.client.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class NbtNode {
    public final String name;
    public Tag tag;
    public final int depth;
    public final NbtNode parent;
    public final List<NbtNode> children = new ArrayList<>();
    public boolean expanded = false;

    public NbtNode(String name, Tag tag, int depth, NbtNode parent) {
        this.name = name;
        this.tag = tag;
        this.depth = depth;
        this.parent = parent;

        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                children.add(new NbtNode(key, compound.get(key), depth + 1, this));
            }
        } else if (tag instanceof ListTag listTag) {
            for (int i = 0; i < listTag.size(); i++) {
                children.add(new NbtNode(String.valueOf(i), listTag.get(i), depth + 1, this));
            }
        }
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public NbtNode getChild(String childName) {
        for (NbtNode child : children) {
            if (child.name.equals(childName)) return child;
        }
        return null;
    }

    public NbtNode getOrCreateChild(String childName, Tag defaultTag) {
        NbtNode child = getChild(childName);
        if (child == null) {
            child = new NbtNode(childName, defaultTag, this.depth + 1, this);
            this.children.add(child);
            if (this.tag instanceof CompoundTag compound) {
                compound.put(childName, defaultTag);
            }
        }
        return child;
    }

    public Tag rebuildTag() {
        if (tag instanceof CompoundTag) {
            CompoundTag newCompound = new CompoundTag();
            for (NbtNode child : children) {
                newCompound.put(child.name, child.rebuildTag());
            }
            return newCompound;
        } else if (tag instanceof ListTag) {
            ListTag newList = new ListTag();
            for (NbtNode child : children) {
                newList.add(child.rebuildTag());
            }
            return newList;
        }
        return this.tag;
    }
}
