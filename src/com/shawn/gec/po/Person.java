package com.shawn.gec.po;

public class Person {

	/**
	 * 
	 */
	private int id;
	
	private int is_male;
	
	private String english_name;
	
	private String chinese_name;

	private String hometown;
	
	private String occupation;
	
	private String mobile;
	
	private String qq;
	
	private String wechat;
	
	private String district;
	
	private String language;
	
	private String roles;

	private int experience;
	
	private String be_with;

	private String remark;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("Person {id:%d, is_maile:%d, english_name:%s, chinese_name:%s, hometown:%s, occupation:%s, "
				+ "mobile:%s, experience:%d, qq:%s, wechat:%s, district:%s, language: %s, roles: %s, be_with:%s, remark:%s}", 
				id, is_male, english_name, chinese_name, hometown, occupation, mobile, experience, qq, wechat, district, language, 
				roles, be_with, remark);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIs_male() {
		return is_male;
	}

	public void setIs_male(int is_male) {
		this.is_male = is_male;
	}

	public String getEnglish_name() {
		return english_name;
	}

	public void setEnglish_name(String english_name) {
		this.english_name = english_name;
	}

	public String getChinese_name() {
		return chinese_name;
	}

	public void setChinese_name(String chinese_name) {
		this.chinese_name = chinese_name;
	}

	public String getHometown() {
		return hometown;
	}

	public void setHometown(String hometown) {
		this.hometown = hometown;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile_number) {
		this.mobile = mobile_number;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getWechat() {
		return wechat;
	}

	public void setWechat(String wechat) {
		this.wechat = wechat;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public int getExperience() {
		return experience;
	}

	public void setExperience(int experience) {
		this.experience = experience;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}
	
	public String getBe_with() {
		return be_with;
	}

	public void setBe_with(String be_with) {
		this.be_with = be_with;
	}
}
