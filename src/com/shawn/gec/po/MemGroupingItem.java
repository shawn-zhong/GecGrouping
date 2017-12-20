package com.shawn.gec.po;

public class MemGroupingItem {

	public Person person;
	 public Grouping grouping;
	// public int groupId;
	 
	 public MemGroupingItem(Person p, Grouping g){
		 person = p;
		 grouping = g;
		 //groupId = -1;
	 }
	 
	 @Override
	public String toString() {
		// TODO Auto-generated method stub
		String ret = person.toString();
		if (grouping != null) {
			ret += "\n";
			ret += grouping.toString();
		}
		
		return ret;
	}
	 
}