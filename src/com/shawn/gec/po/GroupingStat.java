package com.shawn.gec.po;

public class GroupingStat {

	private int groupId;
	private String language;
	private int maleCount;
	private int peopleCount;
	private boolean g_fixed;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("GroupingStat{groupId:%d, language:%s, maleCount:%d, peopleCount:%d. g_fixed:%s}", 
				groupId, language, maleCount, peopleCount, g_fixed);
	}
	
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public int getMaleCount() {
		return maleCount;
	}
	public void setMaleCount(int maleCount) {
		this.maleCount = maleCount;
	}
	public int getPeopleCount() {
		return peopleCount;
	}
	public void setPeopleCount(int peopleCount) {
		this.peopleCount = peopleCount;
	}
	public boolean isG_fixed() {
		return g_fixed;
	}
	public void setG_fixed(boolean g_fixed) {
		this.g_fixed = g_fixed;
	}
}
