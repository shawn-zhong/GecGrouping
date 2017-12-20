package com.shawn.gec.ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ActivityInfoWindow {

	private JFrame frame;
	private JTextField textActivityNum;
	private JTextField textExpectedAmount;
	private JTextField textGroupSize;
	private JTextField text_name;
	private JTextField text_date;
	private JTextField textRemark;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ActivityInfoWindow window = new ActivityInfoWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ActivityInfoWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 480, 330);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		textActivityNum = new JTextField();
		textActivityNum.setBounds(147, 19, 90, 26);
		frame.getContentPane().add(textActivityNum);
		textActivityNum.setColumns(10);
		
		JLabel label = new JLabel("户外活动第几期：");
		label.setBounds(24, 24, 126, 16);
		frame.getContentPane().add(label);
		
		JLabel label_1 = new JLabel("（请填数字）");
		label_1.setBounds(249, 24, 118, 16);
		frame.getContentPane().add(label_1);
		
		JLabel label_2 = new JLabel("期待活动总人数:");
		label_2.setBounds(24, 62, 108, 16);
		frame.getContentPane().add(label_2);
		
		textExpectedAmount = new JTextField();
		textExpectedAmount.setBounds(147, 57, 90, 26);
		frame.getContentPane().add(textExpectedAmount);
		textExpectedAmount.setColumns(10);
		
		textGroupSize = new JTextField();
		textGroupSize.setBounds(147, 97, 90, 26);
		frame.getContentPane().add(textGroupSize);
		textGroupSize.setColumns(10);
		
		JLabel label_3 = new JLabel("（请填数字）");
		label_3.setBounds(249, 62, 118, 16);
		frame.getContentPane().add(label_3);
		
		JLabel label_4 = new JLabel("（请填数字）");
		label_4.setBounds(249, 102, 118, 16);
		frame.getContentPane().add(label_4);
		
		JLabel label_5 = new JLabel("每小组最佳人数:");
		label_5.setBounds(24, 102, 108, 16);
		frame.getContentPane().add(label_5);
		
		JLabel label_6 = new JLabel("该户外活动名称：");
		label_6.setBounds(24, 140, 108, 17);
		frame.getContentPane().add(label_6);
		
		text_name = new JTextField();
		text_name.setBounds(147, 135, 296, 26);
		frame.getContentPane().add(text_name);
		text_name.setColumns(10);
		
		JLabel label_7 = new JLabel("活动日期：");
		label_7.setBounds(24, 178, 90, 16);
		frame.getContentPane().add(label_7);
		
		text_date = new JTextField();
		text_date.setBounds(147, 173, 296, 26);
		frame.getContentPane().add(text_date);
		text_date.setColumns(10);
		
		JLabel label_8 = new JLabel("备注信息：");
		label_8.setBounds(24, 225, 90, 16);
		frame.getContentPane().add(label_8);
		
		textRemark = new JTextField();
		textRemark.setBounds(147, 212, 296, 50);
		frame.getContentPane().add(textRemark);
		textRemark.setColumns(10);
		
		JButton button = new JButton("确定");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		button.setBounds(326, 274, 117, 29);
		frame.getContentPane().add(button);
		
		JButton button_1 = new JButton("取消");
		button_1.setBounds(197, 274, 117, 29);
		frame.getContentPane().add(button_1);
		
		JLabel lblWell = new JLabel("well");
		lblWell.setBounds(24, 279, 61, 16);
		frame.getContentPane().add(lblWell);
	}
}
