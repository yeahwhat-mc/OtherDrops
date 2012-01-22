// OtherDrops - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant, Zarius Tularial, Celtic Minstrel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.	 If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.otherdrops.drop;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.gmail.zariust.common.CommonMaterial;
import com.gmail.zariust.otherdrops.OtherDrops;
import com.gmail.zariust.otherdrops.data.Data;
import com.gmail.zariust.otherdrops.data.ItemData;
import com.gmail.zariust.otherdrops.options.DoubleRange;
import com.gmail.zariust.otherdrops.options.IntRange;
import com.gmail.zariust.otherdrops.subject.Target;

public class ItemDrop extends DropType {
	private Material material;
	private Data durability;
	private IntRange quantity;
	private int rolledQuantity;
	
	public ItemDrop(Material mat) {
		this(mat, 100.0);
	}
	
	public ItemDrop(Material mat, int data) {
		this(mat, data, 100.0);
	}
	
	public ItemDrop(IntRange amount, Material mat) {
		this(amount, mat, 100.0);
	}

	public ItemDrop(IntRange amount, Material mat, int data) {
		this(amount, mat, data, 100.0);
	}

	public ItemDrop(ItemStack stack) {
		this(stack, 100.0);
	}
	
	public ItemDrop(Material mat, double percent) {
		this(mat, 0, percent);
	}
	
	public ItemDrop(Material mat, int data, double percent) {
		this(new ItemStack(mat, 1, (short) data), percent);
	}
	
	public ItemDrop(IntRange amount, Material mat, double percent) {
		this(amount, mat, 0, percent);
	}
	
	public ItemDrop(IntRange amount, Material mat, int data, double percent) {
		this(amount, mat, new ItemData(data), percent);
	}
	
	public ItemDrop(ItemStack stack, double percent) {
		this(new IntRange(stack.getAmount()), stack.getType(), new ItemData(stack), percent);
	}
	
	public ItemDrop(IntRange amount, Material mat, Data data, double percent) { // Rome
		super(DropCategory.ITEM, percent);
		quantity = amount;
		material = mat;
		durability = data;
	}

	public ItemStack getItem(Random rng) {
		rolledQuantity = quantity.getRandomIn(rng);
		return new ItemStack(material, rolledQuantity, (short)durability.getData());
	}

	@Override
	protected void performDrop(Target source, Location where, DropFlags flags) {
		if(quantity.getMax() == 0) return;
		if(flags.spread) {
			ItemStack stack = new ItemStack(material, 1, (short)durability.getData());
			int count = quantity.getRandomIn(flags.rng);
			while(count-- > 0) drop(where, stack, flags.naturally);
		} else drop(where, getItem(flags.rng), flags.naturally);
	}

	public static DropType parse(String drop, String defaultData, IntRange amount, double chance) {
		drop = drop.toUpperCase();
		String state = defaultData;
		String[] split = drop.split("@");
		drop = split[0];
		if(split.length > 1) state = split[1];
		Material mat = null;
		try {
			int dropInt = Integer.parseInt(drop);
			mat = Material.getMaterial(dropInt);
		} catch(NumberFormatException e) {
			mat = CommonMaterial.matchMaterial(drop);
		}
		if (mat == null) return null;


		// Parse data, which could be an integer or an appropriate enum name
		try {
			int d = Integer.parseInt(state);
			return new ItemDrop(amount, mat, d, chance);
		} catch(NumberFormatException e) {}
		Data data = null;
		try {
			data = ItemData.parse(mat, state);
		} catch(IllegalArgumentException e) {
			OtherDrops.logWarning(e.getMessage());
			return null;
		}
		if(data != null) return new ItemDrop(amount, mat, data, chance);
		return new ItemDrop(amount, mat, chance);
	}

	@Override
	public String getName() {
		String ret = material.toString();
		// TODO: Will durability ever be null, or will it just be 0?
		if(durability != null) {
			String dataString = durability.get(material);
			ret += (dataString.isEmpty()) ? "" : "@" + durability.get(material);
		}
		return ret;
	}

	@Override
	public double getAmount() {
		return rolledQuantity;
	}

	@Override
	public DoubleRange getAmountRange() {
		return quantity.toDoubleRange();
	}

	public Material getMaterial() {
		return material;
	}
}
