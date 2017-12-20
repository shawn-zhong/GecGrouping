package com.shawn.gec.po;

public class Grouping {

	// CREATE TABLE activity_grouping (id integer primary key autoincrement, actitity_id integer not null, 
	// signup_id integer not null, english_name text not null, chinese_name text, role_id_mask integer, role_desc text, 
	// wanna_be_with text, group_id integer null, group_role int not null, remark text);
	
	private int id;
	private int person_id;
	private int role_mask;
	private int be_with_uid;
	private int group_id;
	private String language;
	private String remark;
	private int isFixed;

	private Role role;
	private Role groupedRole;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("Grouping {id:%d, person_id:%d, role_mask:%d,  "
				+ "be_with_uid:%d, group_id:%d, language:%s, remark:%s, isFixed:%d}", 
				id, person_id, role_mask, be_with_uid, group_id, language, remark, isFixed);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getGroup_id() {
		return group_id;
	}
	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getBe_with_uid() {
		return be_with_uid;
	}
	public void setBe_with_uid(int wanna_be_with_uid) {
		this.be_with_uid = wanna_be_with_uid;
	}
	public int getPerson_id() {
		return person_id;
	}
	public void setPerson_id(int person_id) {
		this.person_id = person_id;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
	public Role getGroupedRole() {
		return groupedRole;
	}

	public void setGroupedRole(Role groupedRole) {
		this.groupedRole = groupedRole;
	}
	
	public int getIsFixed() {
		return isFixed;
	}

	public void setIsFixed(int isFixed) {
		this.isFixed = isFixed;
	}
}
