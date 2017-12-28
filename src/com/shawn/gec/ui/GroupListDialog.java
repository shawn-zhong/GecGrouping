package com.shawn.gec.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import com.shawn.gec.dao.ComplexDao;
import com.shawn.gec.dao.GroupingDao;
import com.shawn.gec.dao.IComplexDao;
import com.shawn.gec.dao.IGroupingDao;
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
	private GecGroupTableModel tableModel=new GecGroupTableModel();
	private JButton btn_lock;

	private static final GroupListDialog dialog = new GroupListDialog();
	
	private JLabel label_stat;
	private JTable tableStat;
	
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
		setBounds(100, 100, 600, 450);
		getContentPane().setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.SOUTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				label_stat = new JLabel("stat");
				label_stat.setForeground(Color.GRAY);
				label_stat.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel.add(label_stat, BorderLayout.NORTH);
			}
			{
				JLabel lblNewLabel_1 = new JLabel("* 双击小组条目可在大屏幕显示该小组的所有成员");
				lblNewLabel_1.setForeground(Color.GRAY);
				lblNewLabel_1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel.add(lblNewLabel_1, BorderLayout.WEST);
			}
			{
				JLabel lblNewLabel_2 = new JLabel("* 锁定状态下的小组及成员将不会受自动分组的影响；支持上锁及解锁");
				lblNewLabel_2.setForeground(Color.GRAY);
				lblNewLabel_2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel.add(lblNewLabel_2, BorderLayout.SOUTH);
			}
		}
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				tableStat = new JTable(tableModel);
				scrollPane.setViewportView(tableStat);

				tableStat.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						int selectedRowNumber = tableStat.getSelectedRow();
						if (selectedRowNumber == -1 ) {
							return;
						}

						if (e.getClickCount() == 2) {
							int groupID = Integer.parseInt(tableStat.getValueAt(selectedRowNumber, 0).toString());
							AppWindow.window.searchAndShow(AppWindow.SearchType.ByGroupId, groupID, null);
						}

						// update the btn lock display
						String lockText = tableModel.getValueAt(selectedRowNumber, 5).toString();
						if (lockText.isEmpty()) {
							btn_lock.setText("上锁");
						} else {
							btn_lock.setText("解锁");
						}
						btn_lock.updateUI();
					}
				});
			}
		}
		
		{
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
						String lockText = tableModel.getValueAt(row, 5).toString();

						new Thread(()->{
							IGroupingDao dao = new GroupingDao();
							if (lockText.isEmpty()) {
								dao.lockGroup(groupId, true);
								tableModel.setValueAt("已上锁", row, 5);
								btn_lock.setText("解锁");
							} else {
								dao.lockGroup(groupId, false);
								tableModel.setValueAt("", row, 5);
								btn_lock.setText("上锁");
							}

							SwingUtilities.invokeLater(()->{
								tableStat.updateUI();
							});
						}).start();
					}
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
			IComplexDao dao = new ComplexDao();
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
