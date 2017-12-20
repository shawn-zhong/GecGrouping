package com.shawn.gec.ui.model;

import com.shawn.gec.control.Utils;
import com.shawn.gec.po.GroupingStat;

import javax.swing.table.AbstractTableModel;
import java.util.Vector;

public class GecGroupTableModel extends AbstractTableModel {


    /**
     *
     */
    private static final long serialVersionUID = -8755310218687779671L;

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // TODO Auto-generated method stub
        ((Vector)   content.get(rowIndex)).remove(columnIndex);
        ((Vector)   content.get(rowIndex)).add(columnIndex, aValue);
        this.fireTableCellUpdated(rowIndex,   columnIndex);

        //super.setValueAt(aValue, rowIndex, columnIndex);
    }

    private Vector content = null;
    private String[] titles = {"组号", "小组名称", "小组人数", "男(人数)", "女(人数)", "锁定状态"};

    public GecGroupTableModel() {
        content = new Vector();
    }

    public Vector getContent()
    {
        return content;
    }

    public void addRow(GroupingStat stat){
        Vector v = new Vector();
        v.add(0, stat.getGroupId());
        v.add(1, Utils.getGroupName(stat.getLanguage(), stat.getGroupId()));
        v.add(2, stat.getPeopleCount());
        v.add(3, stat.getMaleCount());
        v.add(4, stat.getPeopleCount()-stat.getMaleCount());
        v.add(5, stat.isG_fixed()?"已上锁":"");
        content.add(v);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void removeRow(int rowNum){
        content.remove(rowNum);
    }

    public void removeAll(){
        content.clear();
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