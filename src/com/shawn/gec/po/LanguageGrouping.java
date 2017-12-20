package com.shawn.gec.po;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.MemGroupingItem;
import com.shawn.gec.po.Person;
import com.shawn.gec.po.Role;


 
public class LanguageGrouping {
	 public String language;
	 public int groupId;
	 public float idealCapacity;
	 public HashMap<Integer, MemGroupingItem> groupingItems;
 }
