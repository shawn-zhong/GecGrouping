package com.shawn.gec.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.shawn.gec.control.SettingCenter;
import com.shawn.gec.dao.*;
import com.shawn.gec.po.MemGroupingItem;
import com.shawn.gec.po.Role;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;

public class PersonInfoDialog extends JDialog {

	private int _personId = -1;

	private final JPanel contentPanel = new JPanel();
	private JTextField text_id;
	private JTextField text_gender;
	private JTextField text_english_name;
	private JTextField text_chinese_name;
	private JTextField text_qq;
	private JTextField text_wechat;
	private JTextField text_mobile;
	private JTextField text_bewith;
	//private JTextField text_language;
	private JTextField text_role;
	private JTextField text_experience;
	private JTextField text_other;
	private JTextField text_g_language;
    private JLabel lb_msg;
	private PersonInfoDialog _dialog = null;
	private MemGroupingItem _groupingItem = null;
	private JTextField text_g_gid;
	private JCheckBox checkBox_manual;
	private JTextArea textArea_g_remark;
	private JComboBox cmb_g_role;
	private JTextField text_remark;
	private JComboBox cmb_language;
	
	/**
	 * Launch the application.
	 */
	public static void showDialog(int personId) {
		
		try {
			PersonInfoDialog dialog = new PersonInfoDialog(personId);
			dialog._dialog = dialog;
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			
			dialog.initDialogInfo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initDialogInfo() {

		// init combo
		for (String roleDesc : SettingCenter.getRoleNameList()) {
			cmb_g_role.addItem(roleDesc);
		}

		cmb_g_role.addItem("<null>");


		new Thread(()->{

			IPersonDao dao = new PersonDao();
			MemGroupingItem item = dao.getPersonById(_personId);
			_groupingItem = item;

			IComplexDao complexDao = new ComplexDao();
			List<String> lans = complexDao.getAllRegesterLanguages();

			SwingUtilities.invokeLater(()->{

				if (item == null) {
					lb_msg.setText("找不到此注册信息");
					return;
				}

				for (String lan : lans) {
					cmb_language.addItem(lan);
				}

				cmb_language.addItem("<自编辑>");

				text_id.setText(item.person.getId()+"");
				text_gender.setText(item.person.getIs_male()==1?"M":"F");
				text_english_name.setText(item.person.getEnglish_name());
				text_chinese_name.setText(item.person.getChinese_name());
				text_qq.setText(item.person.getQq());
				text_wechat.setText(item.person.getWechat());
				text_mobile.setText(item.person.getMobile());
				text_bewith.setText(item.person.getBe_with());
				cmb_language.setSelectedItem(item.person.getLanguage());
				text_role.setText(item.person.getRoles());
				text_experience.setText(item.person.getExperience()+"");
				text_other.setText(item.person.getHometown()+";"+item.person.getDistrict()+";"+item.person.getOccupation());
				text_remark.setText(item.person.getRemark());

				if (item.grouping.getGroup_id() != -1) {	// grouped
					text_g_language.setText(item.grouping.getLanguage());
					text_g_gid.setText(String.format("%03d", item.grouping.getGroup_id()));
					textArea_g_remark.setText(item.grouping.getRemark());

					if (item.grouping.getIsFixed()==1) { // manual mode
						EnalbeGroupInfoEditable(true);
						checkBox_manual.setSelected(true);
					} else {
						EnalbeGroupInfoEditable(false);
						checkBox_manual.setSelected(false);
					}

					cmb_g_role.setSelectedItem(item.grouping.getGroupedRole().RoleNames.trim());
				}
				else {
					text_g_language.setText("");
					text_g_gid.setText("");
					textArea_g_remark.setText("");
					EnalbeGroupInfoEditable(false);
					cmb_g_role.setSelectedItem("<null>");
				}

			});

		}).start();
		
	}

	private void EnalbeGroupInfoEditable(boolean _editable) {
		if (_editable) {
			
			text_g_language.setEditable(true);
			text_g_gid.setEditable(true);
			cmb_g_role.setEnabled(true);
			textArea_g_remark.setEditable(true);
			
			text_g_language.setBackground(Color.WHITE);
			text_g_gid.setBackground(Color.WHITE);
			cmb_g_role.setBackground(Color.WHITE);
			textArea_g_remark.setBackground(Color.WHITE);
		
		} else {
			
			text_g_language.setEditable(false);
			text_g_gid.setEditable(false);
			cmb_g_role.setEnabled(false);
			textArea_g_remark.setEditable(false);
			
			text_g_language.setBackground(contentPanel.getBackground());
			text_g_gid.setBackground(contentPanel.getBackground());
			cmb_g_role.setBackground(contentPanel.getBackground());
			textArea_g_remark.setBackground(contentPanel.getBackground());

		}
	}
	
	/**
	 * Create the dialog.
	 */
	private PersonInfoDialog(int personId) {
		_personId = personId;

		setBounds(100, 100, 620, 450);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel lblid = new JLabel("注册ID: ");
		lblid.setBounds(23, 21, 61, 16);
		contentPanel.add(lblid);
		
		text_id = new JTextField();
		text_id.setBounds(89, 16, 181, 26);
		contentPanel.add(text_id);
		text_id.setColumns(10);
		text_id.setEditable(false);
		
		text_gender = new JTextField();
		text_gender.setColumns(10);
		text_gender.setBounds(386, 16, 181, 26);
		text_gender.setEditable(false);
		contentPanel.add(text_gender);
		
		JLabel label = new JLabel("性别: ");
		label.setBounds(320, 21, 61, 16);
		contentPanel.add(label);
		
		JLabel label_1 = new JLabel("英文名: ");
		label_1.setBounds(23, 54, 61, 16);
		contentPanel.add(label_1);
		
		text_english_name = new JTextField();
		text_english_name.setColumns(10);
		text_english_name.setBounds(89, 49, 181, 26);
		contentPanel.add(text_english_name);
		
		JLabel label_2 = new JLabel("中文名: ");
		label_2.setBounds(320, 54, 61, 16);
		contentPanel.add(label_2);
		
		text_chinese_name = new JTextField();
		text_chinese_name.setColumns(10);
		text_chinese_name.setBounds(386, 49, 181, 26);
		contentPanel.add(text_chinese_name);
		
		JLabel lblQq = new JLabel("QQ号: ");
		lblQq.setBounds(23, 87, 61, 16);
		contentPanel.add(lblQq);
		
		text_qq = new JTextField();
		text_qq.setColumns(10);
		text_qq.setBounds(89, 82, 181, 26);
		contentPanel.add(text_qq);
		
		JLabel label_4 = new JLabel("微信号: ");
		label_4.setBounds(320, 87, 61, 16);
		contentPanel.add(label_4);
		
		text_wechat = new JTextField();
		text_wechat.setColumns(10);
		text_wechat.setBounds(386, 82, 181, 26);
		contentPanel.add(text_wechat);
		
		JLabel label_3 = new JLabel("手机号: ");
		label_3.setBounds(23, 120, 61, 16);
		contentPanel.add(label_3);
		
		text_mobile = new JTextField();
		text_mobile.setColumns(10);
		text_mobile.setBounds(89, 115, 181, 26);
		contentPanel.add(text_mobile);
		
		JLabel lblta = new JLabel("和ta一起: ");
		lblta.setBounds(320, 120, 61, 16);
		contentPanel.add(lblta);
		
		text_bewith = new JTextField();
		text_bewith.setColumns(10);
		text_bewith.setBounds(386, 115, 181, 26);
		contentPanel.add(text_bewith);
		
		JLabel label_5 = new JLabel("报名语言: ");
		label_5.setBounds(23, 153, 61, 16);
		contentPanel.add(label_5);
		
		JLabel label_6 = new JLabel("报名角色: ");
		label_6.setBounds(320, 153, 61, 16);
		contentPanel.add(label_6);
		
		text_role = new JTextField();
		text_role.setColumns(10);
		text_role.setBounds(386, 148, 181, 26);
		contentPanel.add(text_role);
		
		JLabel label_7 = new JLabel("活动经验: ");
		label_7.setBounds(23, 186, 61, 16);
		contentPanel.add(label_7);
		
		text_experience = new JTextField();
		text_experience.setColumns(10);
		text_experience.setBounds(89, 181, 181, 26);
		contentPanel.add(text_experience);
		
		JLabel label_8 = new JLabel("其他信息: ");
		label_8.setBounds(320, 186, 61, 16);
		contentPanel.add(label_8);
		
		text_other = new JTextField();
		text_other.setColumns(10);
		text_other.setBounds(386, 181, 181, 26);
		text_other.setEditable(false);
		contentPanel.add(text_other);
		
		JLabel label_9 = new JLabel("分配语言-");
		label_9.setBounds(23, 297, 61, 16);
		contentPanel.add(label_9);
		

		text_g_language = new JTextField();
		text_g_language.setHorizontalAlignment(SwingConstants.RIGHT);
		text_g_language.setColumns(10);
		text_g_language.setBounds(146, 292, 146, 26);
		contentPanel.add(text_g_language);
		
		JLabel label_10 = new JLabel("分配角色: ");
		label_10.setBounds(386, 297, 61, 16);
		contentPanel.add(label_10);
		
		JLabel label_11 = new JLabel("分配备注: ");
		label_11.setBounds(23, 330, 61, 16);
		contentPanel.add(label_11);
		
		JLabel label_12 = new JLabel("分配组号");
		label_12.setBounds(84, 297, 61, 16);
		contentPanel.add(label_12);
		
		text_g_gid = new JTextField();
		text_g_gid.setColumns(10);
		text_g_gid.setBounds(304, 292, 61, 26);
		contentPanel.add(text_g_gid);
		
		checkBox_manual = new JCheckBox("手动分组（锁定小组）");
		checkBox_manual.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		checkBox_manual.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				EnalbeGroupInfoEditable(cb.isSelected());
				
				if (!cb.isSelected()) {
					text_g_gid.setText("");
					text_g_language.setText(cmb_language.getSelectedItem().toString());
					cmb_g_role.setSelectedItem("<null>");
				} else {
					if (_groupingItem.grouping.getGroup_id() != -1) {	// grouped
						text_g_language.setText(_groupingItem.grouping.getLanguage());
						text_g_gid.setText(String.format("%03d", _groupingItem.grouping.getGroup_id()));
						textArea_g_remark.setText(_groupingItem.grouping.getRemark());
						
						cmb_g_role.setSelectedItem(_groupingItem.grouping.getGroupedRole().RoleNames.trim());
					}
				}
			}
		});
		checkBox_manual.setBounds(16, 262, 162, 23);
		contentPanel.add(checkBox_manual);
		
		JLabel label_13 = new JLabel("-");
		label_13.setBounds(294, 275, 14, 16);
		contentPanel.add(label_13);
		
		textArea_g_remark = new JTextArea();
		textArea_g_remark.setLineWrap(true);
		textArea_g_remark.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		textArea_g_remark.setBounds(89, 331, 474, 39);
		contentPanel.add(textArea_g_remark);
		
		cmb_g_role = new JComboBox();
		cmb_g_role.setBounds(447, 293, 120, 27);
		contentPanel.add(cmb_g_role);
		
		JLabel lblLockExplaination = new JLabel("勾选后该人所在的小组将自行锁定，该小组的所有成员将不再受自动分组影响");
		lblLockExplaination.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		lblLockExplaination.setBounds(165, 267, 402, 16);
		lblLockExplaination.setForeground(Color.gray);
		contentPanel.add(lblLockExplaination);
		
		text_remark = new JTextField();
		text_remark.setColumns(10);
		text_remark.setBounds(89, 214, 478, 26);
		contentPanel.add(text_remark);
		
		JLabel label_14 = new JLabel("报名备注: ");
		label_14.setBounds(23, 219, 61, 16);
		contentPanel.add(label_14);
		
		cmb_language = new JComboBox();
		cmb_language.setEditable(true);
		cmb_language.setBounds(89, 149, 181, 27);
		contentPanel.add(cmb_language);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("保存");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						lb_msg.setText("");
						
						if (_groupingItem != null ){
							// update person info
							_groupingItem.person.setIs_male(text_gender.getText().compareToIgnoreCase("M")==0?1:0);
							_groupingItem.person.setEnglish_name(text_english_name.getText());
							_groupingItem.person.setChinese_name(text_chinese_name.getText());
							_groupingItem.person.setQq(text_qq.getText());
							_groupingItem.person.setWechat(text_wechat.getText());
							_groupingItem.person.setMobile(text_mobile.getText());
							_groupingItem.person.setBe_with(text_bewith.getText());
							_groupingItem.person.setLanguage(cmb_language.getSelectedItem().toString());
							_groupingItem.person.setRoles(text_role.getText());
							_groupingItem.person.setRemark(text_remark.getText());
							
							try{
								int experience = Integer.parseInt(text_experience.getText());
								if (experience < 1 || experience > 5) {
									throw new Exception("");
								}
								_groupingItem.person.setExperience(experience);
							} catch(Exception ex) {
								lb_msg.setText("活动经验请填写1～5之间的数字。1表示最低，5表示最高。");
								return;
							}

							_groupingItem.grouping.setRemark(textArea_g_remark.getText());

							// update grouping info
							_groupingItem.grouping.setIsFixed(checkBox_manual.isSelected()?1:0);
							_groupingItem.grouping.setLanguage(text_g_language.getText());
							
							if (text_g_gid.getText().isEmpty()) {
								_groupingItem.grouping.setGroup_id(0);
							} else {
								try {
									int groupId = Integer.parseInt(text_g_gid.getText());
									_groupingItem.grouping.setGroup_id(groupId);

									if (checkBox_manual.isSelected() && groupId == 0)
										throw new Exception("");
								} catch(Exception ex) {
									lb_msg.setText("分配组号请填写数字（组号必须>=1）");
									return;
								}
							}

							if (cmb_g_role.getSelectedItem().toString().compareToIgnoreCase("<null>")== 0)
								_groupingItem.grouping.setGroupedRole(null);
							else
							  _groupingItem.grouping.setGroupedRole(new Role(cmb_g_role.getSelectedItem().toString()));

							// write to database

							new Thread(()->{
								IPersonDao pDao = new PersonDao();
								pDao.insertOrUpdatePerson(_groupingItem.person);

								IGroupingDao gDao = new GroupingDao();
								gDao.insertOrUpdate(_groupingItem.grouping);

								if (_groupingItem.grouping.getIsFixed()==1) {
									// Lock the group preventing from anto changing
									gDao.lockGroup(_groupingItem.grouping.getGroup_id(), true);

									// update the whole group name
									IComplexDao cDao = new ComplexDao();
									cDao.updateGroupName(_groupingItem.grouping.getLanguage(), _groupingItem.grouping.getGroup_id());
								}

								// refresh the mainBoard
								AppWindow.window.refreshSearch();
							}).start();
						}
					}
				});
				
			    lb_msg = new JLabel("  ");
				buttonPane.add(lb_msg);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("退出");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						//_dialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
						_dialog.setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
