package com.shawn.gec.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import com.shawn.gec.dao.ComplexDao;
import com.shawn.gec.dao.GroupingDao;
import com.shawn.gec.po.GroupingStat;
import com.shawn.gec.ui.model.GecGroupTableModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class GroupListDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTable tableStat;
	private GecGroupTableModel tableModel=new GecGroupTableModel();
	private JButton btn_lock;
	private JLabel label_stat;

	private static final GroupListDialog dialog = new GroupListDialog();
	
	/**
	 * Launch the application.
	 */
	public static void ShowDialog() {
		try {
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			dialog.InitJTableWithData();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public GroupListDialog() {
		setBounds(100, 100, 600, 380);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		{
			JScrollPane scrollPane = new JScrollPane(); 
			scrollPane.setBounds(10, 10, 580, 247);
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			
			tableStat = new JTable(tableModel);
			tableStat.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					
					if (e.getClickCount() == 1) {
						
						int row = tableStat.getSelectedRow();
						if (row != -1) {
							String lock = tableModel.getValueAt(row, 5).toString();
							if (lock.isEmpty()) {
								btn_lock.setText("上锁");
							} else {
								btn_lock.setText("解锁");
							}
						}
					}
					
	        		if (e.getClickCount() == 2) {
	        			int row = tableStat.getSelectedRow();
						if (row != -1) {
							int groupId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
							AppWindow.window.searchAndShow(AppWindow.SearchType.ByGroupId, groupId, null);
						}
	        		}
					
				}
			});
			//tableStat.setShowHorizontalLines(true);
			scrollPane.setViewportView(tableStat);

			label_stat = new JLabel("* ");
			label_stat.setForeground(Color.GRAY);
			label_stat.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			label_stat.setBounds(10, 260, 455, 16);
			contentPanel.add(label_stat);

			JLabel lblNewLabel = new JLabel("* 双击小组条目可在大屏幕显示该小组的所有成员");
			lblNewLabel.setForeground(Color.GRAY);
			lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			lblNewLabel.setBounds(10, 277, 455, 16);
			contentPanel.add(lblNewLabel);
			
			JLabel label = new JLabel("* 锁定状态下的小组及成员将不会受自动分组的影响；支持上锁及解锁");
			label.setForeground(Color.GRAY);
			label.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			label.setBounds(10, 294, 455, 16);
			contentPanel.add(label);

			
			TableColumn tableColumn = tableStat.getColumnModel().getColumn(0);
	        tableColumn.setPreferredWidth(30);	        
	        
	        tableColumn = tableStat.getColumnModel().getColumn(1);
	        tableColumn.setPreferredWidth(150);	
		}
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			btn_lock = new JButton("锁定");
			buttonPane.add(btn_lock);
			btn_lock.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					int row = tableStat.getSelectedRow();
					if (row != -1) {
						int groupId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
						String lock = tableModel.getValueAt(row, 5).toString();

						new Thread(()->{
							GroupingDao dao = new GroupingDao();
							if (lock.isEmpty()) {
								dao.LockGroup(groupId, true);
								tableModel.setValueAt("已上锁", row, 5);
								btn_lock.setText("解锁");
							} else {
								dao.LockGroup(groupId, false);
								tableModel.setValueAt("", row, 5);
								btn_lock.setText("上锁");
							}
						}).start();

						if (lock.isEmpty()) {
							tableModel.setValueAt("已上锁", row, 5);
							btn_lock.setText("解锁");
						} else {
							tableModel.setValueAt("", row, 5);
							btn_lock.setText("上锁");
						}
					}
					
					tableStat.updateUI();
				}
			});
			
			JLabel label = new JLabel("        ");
			buttonPane.add(label);
			
			{
				{
					
					JButton btn_refresh = new JButton("刷新");
					btn_refresh.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							dialog.InitJTableWithData();
						}
					});
					buttonPane.add(btn_refresh);
				}
			}
			{
				JButton cancelButton = new JButton("退出");
				//cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e){
						tableModel.removeAll();
						tableStat.updateUI();
						dialog.setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
				getRootPane().setDefaultButton(cancelButton);
			}
		}
		
		
	}
	
	private void InitJTableWithData(){

		tableModel.removeAll();

		new Thread(()->{
			ComplexDao dao = new ComplexDao();
			List<GroupingStat> stats = dao.getGroupingStatistics();

			SwingUtilities.invokeLater(()->{
				stats.stream().filter(t->t.getGroupId()>0).forEach(t -> tableModel.addRow(t));
				tableStat.updateUI();

				// stat info
				int groupsCnt =0;
				int peopleCnt=0;
				int maleCnt = 0;

				for (GroupingStat stat : stats) {

					if (stat.getGroupId()<=0)
						continue;;

					groupsCnt++;
					peopleCnt += stat.getPeopleCount();
					maleCnt += stat.getMaleCount();
				}

				String statInfo = String.format("* 共计%d个小组%d人 (male:%d/female:%d)", groupsCnt, peopleCnt, maleCnt, peopleCnt-maleCnt);
				label_stat.setText(statInfo);
			});
		}).start();
	}
}

// http://blog.163.com/zhouyii26@126/blog/static/13291642120117167536168/
