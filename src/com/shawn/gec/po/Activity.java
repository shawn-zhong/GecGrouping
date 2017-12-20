package com.shawn.gec.po;

import java.util.Date;

public class Activity {

	// EATE TABLE activity (id integer primary key autoincrement, name text not null, capacity integer, group_capacity integer, launch_time text, create_time text, create_staff text, remark text);
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("Activity{id:%d, code:%s, name:%s, capacity:%d, group_capacity:%d, launch_time:%s, "
				+ "create_time:%s, create_staff:%s, remark:%s}", 
				id, code, name, capacity, group_capacity, launch_time, create_time, create_staff, remark);
	}

	private int id;
	
	private String code;
	
	private String name;
	
	private int capacity;
	
	private int group_capacity;
	
	private Date launch_time;
	
	private Date create_time;
	
	private String create_staff;
	
	private String remark;

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getGroup_capacity() {
		return group_capacity;
	}

	public void setGroup_capacity(int group_capacity) {
		this.group_capacity = group_capacity;
	}

	public Date getLaunch_time() {
		if (launch_time == null)
			return new Date(System.currentTimeMillis());
		
		return launch_time;
	}

	public void setLaunch_time(Date launch_time) {
		this.launch_time = launch_time;
	}

	public Date getCreate_time() {
		if (launch_time == null)
			return new Date(System.currentTimeMillis());
		
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}

	public String getCreate_staff() {
		return create_staff;
	}

	public void setCreate_staff(String create_staff) {
		this.create_staff = create_staff;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
}
