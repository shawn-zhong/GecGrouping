package com.shawn.gec.ui.model;

import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.shawn.gec.control.Utils;
import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.MemGroupingItem;
import com.shawn.gec.po.Person;

public class GecMainTableModel extends AbstractTableModel {

	private Vector content = new Vector();
	private String[] titles = {"报名ID", "分配组号", "分组角色", "性别", "英文名", "中文名", "手机", "微信", "和TA一起", "报名语言", "报名角色", "经验值", "备注信息"};
	private boolean isTableEditing = false;
	
	public Vector getContent() {
		return content;
	}
	
	public void initTableData(List<MemGroupingItem> items) {
		content.clear();
		for (MemGroupingItem item : items) {
			addRow(item.person, item.grouping);
		}
	}
	
	public void addRow(Person p, Grouping g){
		Vector v = new Vector();
		//v.add(i);
		v.add(p.getId());

		if (g.getGroup_id() > 0) {
			v.add(Utils.getGroupName(g.getLanguage(), g.getGroup_id()) + (g.getIsFixed()==1 ? " ＊":""));
			v.add(g.getGroupedRole().RoleNames);
		} else {
			v.add("<null>");
			v.add("<null>");
		}
		
		v.add(p.getIs_male()==1?"M":"F");
		v.add(p.getEnglish_name());
		v.add(p.getChinese_name());
		v.add(p.getMobile());
		v.add(p.getWechat());
		v.add(p.getBe_with());
		v.add(p.getLanguage());
		v.add(p.getRoles());
		
		int experience = p.getExperience();
		String experienceText;
		switch(experience) {
			case 1: experienceText="＊"; break;
			case 2: experienceText="＊＊"; break;
			case 3: experienceText="＊＊＊"; break;
			case 4: experienceText="＊＊＊＊"; break;
			case 5: experienceText="＊＊＊＊＊"; break;
			default : experienceText="*";
		}
		
		v.add(experienceText);
		v.add(g.getRemark()+p.getRemark());
	
		content.add(v);
	}
	
	public void removeRow(int rowNum){
		content.remove(rowNum);
	}
	
	public void removeAll(){
		content.clear();
	}

	public void cleanGroupColumn() {
		for(int i=0; i<content.size(); i++) {
			((Vector)   content.get(i)).remove(1);
			((Vector)   content.get(i)).remove(2);
			((Vector)   content.get(i)).add(1, "<waiting..>");
			((Vector)   content.get(i)).add(2, "");
			this.fireTableCellUpdated(i,   1);
			this.fireTableCellUpdated(i,   2);
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		((Vector)   content.get(rowIndex)).remove(columnIndex);     
		((Vector)   content.get(rowIndex)).add(columnIndex, aValue);     
		this.fireTableCellUpdated(rowIndex,   columnIndex);
		
		//super.setValueAt(aValue, rowIndex, columnIndex);
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
			return isTableEditing? true : false;
	}
	
	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return content.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return titles.length;
	}
	
	@Override
	public String getColumnName(int column) {
		// TODO Auto-generated method stub
		return titles[column];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return ((Vector)content.get(rowIndex)).get(columnIndex);
	}

}
