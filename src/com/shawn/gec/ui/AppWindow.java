package com.shawn.gec.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import javax.swing.table.TableColumn;

import com.shawn.gec.control.ControlPanel;
import com.shawn.gec.control.SettingCenter;
import com.shawn.gec.dao.ComplexDao;
import com.shawn.gec.dao.IComplexDao;
import com.shawn.gec.dao.IPersonDao;
import com.shawn.gec.dao.PersonDao;
import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.MemGroupingItem;
import com.shawn.gec.po.Person;
import com.shawn.gec.ui.model.GecMainTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.FlowLayout;
import java.util.Map;
import java.util.Optional;
import java.awt.Font;
import java.awt.Color;

public class AppWindow {

    public enum SearchType {
        ByKeyWord, ByGroupId, ByDuplicateReg, ByBeWith, ByGroupOrder, ByHavingRemark, ByGroupLeaders
    }

    private static final Logger logger = LoggerFactory.getLogger(AppWindow.class);

	private JFrame frmGecGroupingSoftware;
	private GecMainTableModel gecMainTableModel = new GecMainTableModel();
	private JTable table_data;
	private JTextField textKeyword;
	private JButton btnKeywordSearch;
	private JLabel lbl_title_search_info;
	private JLabel lbl_title_search_stat;

	private static boolean isDBOK = true;

	public static AppWindow window = new AppWindow();

	// below 3 is for search condition:
    private SearchType _conditonSearchType;
	private Optional<Integer> _conditionGroupId;
	private Optional<String> _conditionKeyword;
	
	public static void main(String[] args) {

		// read settings and test DB
		readSettingsAndTestDBConnectivity();

		window.refreshWindowTitle();

        // store the roles for selecting order
        new Thread(()->{
            IComplexDao dao = new ComplexDao();
            int i=100;
            for (String role : SettingCenter.getRoleNameList()) {
                dao.insertOrUpdateRolesPriority(role, i--);
            }
        }).start();

		EventQueue.invokeLater(() -> {
            try {
                window.frmGecGroupingSoftware.setVisible(true);
            } catch (Exception e) {
                logger.error("error when initialize AppWindow", e);
            }
        });
	}

	/**
	 * Create the application.
	 * @wbp.parser.entryPoint
	 */
	private AppWindow() {
		initialize();
	}

	private static void readSettingsAndTestDBConnectivity() {

		// read the settings from xml file
		SettingCenter.ReadFromSettingFile();
		logger.info("All the settings :");
		logger.info(SettingCenter.instance.toString());

		new Thread(()->{
			try {
				IComplexDao dao = new ComplexDao();
				dao.testDBConnection();
				isDBOK = true;
			}catch (Exception ex) {
				logger.error("there is problem connecting to DB. please check the DB file", ex);
				isDBOK = false;
				SwingUtilities.invokeLater(()->{
					window.updateHeader("连接数据库文件出错，请检查setting.xml里的<DbFilePath/> ");
				});
			}
		}).start();
	}

	public void searchAndShow(SearchType searchType, Integer groupId, String keyword) {
	    _conditonSearchType = searchType;
	    _conditionGroupId = Optional.ofNullable(groupId);
	    _conditionKeyword = Optional.ofNullable(keyword);

        refreshSearch();
    }

    public void refreshSearch() {

	    // non-UI thread for loading data
	    new Thread(()->{

            List<MemGroupingItem> items;
            String searchInfo;
            IPersonDao personDao = new PersonDao();

            switch (_conditonSearchType) {
                case ByKeyWord: {
                    String key = _conditionKeyword.orElse("");
                    searchInfo = String.format("当前查询条件: %s", key.isEmpty() ? "所有" : "<关键字>"+key);
                    items = personDao.getPersonByKeyword(_conditionKeyword.orElse(""));
                    break;
                }
                case ByGroupId: {
                    searchInfo = String.format("当前查询条件: " + String.format("第%d组", _conditionGroupId.get()));
                    items = personDao.getGroupMembers(_conditionGroupId.get());
                    break;
                }
                case ByDuplicateReg: {
                    searchInfo = "当前查询条件: 所有可能重复报名的人";
                    items = personDao.getDuplicateRegistration();
                    break;
                }
                case ByBeWith: {
                    searchInfo = "当前查询条件: 要求想要一起的人";
                    items = personDao.getWannaBeWithList();
                    break;
                }
                case ByGroupOrder: {
                    searchInfo = "当前查询条件: 按小组顺序显示所有";
                    items = personDao.getAllGroupMembersOrderByGroupNo();
                    break;
                }
                case ByHavingRemark:{
                    searchInfo = "当前查询条件：处理中发现有问题的记录";
                    items = personDao.getProblemeticItems();
                    break;
                }
				case ByGroupLeaders:{
					searchInfo = "当前查询条件：显示所有组长副组长";
					items = personDao.getGroupLeaders();
					break;
				}

                default: {
                    searchInfo = "当前查询条件: 不知道你想搜素什么";
                    items = personDao.getPersonByKeyword(_conditionKeyword.orElse(""));
                    break;
                }
            }

            // UI thread for updating UI
            SwingUtilities.invokeLater(()->{
                gecMainTableModel.initTableData(items);
                table_data.updateUI();

                long peopleCnt = items.stream().count();
                long maleCnt = items.stream().filter(t-> t.person.getIs_male()==1).count();
                lbl_title_search_stat.setText(String.format("共计: %d人 (男:%d, 女:%d, 男/女 ~ 1/%.1f)", peopleCnt, maleCnt, peopleCnt-maleCnt, (float)(peopleCnt-maleCnt)/maleCnt));
                lbl_title_search_info.setText(searchInfo);
            });

        }).start();
    }

    public void updateHeader(String info) {
        SwingUtilities.invokeLater(() -> {
            lbl_title_search_info.setText(info+ " ");
        });
    }

    public void addRowToTable(Person p, Grouping g) {
        SwingUtilities.invokeLater(() -> {
            gecMainTableModel.addRow(p, g);
            table_data.updateUI();
        });
    }

    public void refreshWindowTitle() {
		SwingUtilities.invokeLater(()->{
			frmGecGroupingSoftware.setTitle("GEC Grouping Software @ Shawn @ " + SettingCenter.getDbFilePath());
		});
	}

	/**
	 * Initialize the contents of the frame.
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frmGecGroupingSoftware = new JFrame();

		frmGecGroupingSoftware.setBounds(100, 100, 1024, 750);
		frmGecGroupingSoftware.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmGecGroupingSoftware.getContentPane().setLayout(new BorderLayout());

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		frmGecGroupingSoftware.getContentPane().add(panel, BorderLayout.NORTH);
		
		JButton btnReadFile = new JButton("读取文件");
		btnReadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			    logger.info("Button clicked : Read the records from file");

			    if (!isDBOK) {
					readSettingsAndTestDBConnectivity();
					return;
				}

			    // clean first
                gecMainTableModel.removeAll();

                // read data from csv file to database
                new Thread(() -> {
                    ControlPanel.instance.readCsvFileAndWriteToDB();
                }).start();
            }
		});
		panel.add(btnReadFile);
		
		JButton btnDealtaReadFile = new JButton("增量读取文件");
		btnDealtaReadFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			    logger.info("Button clicked : Read the incremental records from file");

				if (!isDBOK) {
					readSettingsAndTestDBConnectivity();
					return;
				}

			    new Thread(()->{
                    ControlPanel.instance.readDeltaCsvFileAndWriteToDB();
                }).start();
			}
		});
		panel.add(btnDealtaReadFile);
		
		JButton btnReadDB = new JButton("读取数据库");
		//btnReadDB.setForeground(Color.blue);
		
		btnReadDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			    logger.info("Button clicked : (Refresh) Read all the records from DB");

				readSettingsAndTestDBConnectivity();

				// read data from database and fill up the table
                searchAndShow(SearchType.ByKeyWord,null, null);
			}
		});
		
		JLabel label_2 = new JLabel(">>");
		panel.add(label_2);
		panel.add(btnReadDB);
		
		lbl_title_search_info = new JLabel("当前查询条件:");
		lbl_title_search_info.setForeground(Color.GRAY);
		lbl_title_search_info.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		panel.add(lbl_title_search_info);
		
		lbl_title_search_stat = new JLabel("共计：");
		lbl_title_search_stat.setForeground(Color.GRAY);
		lbl_title_search_stat.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		panel.add(lbl_title_search_stat);
		
		JPanel panel_operations = new JPanel();
		frmGecGroupingSoftware.getContentPane().add(panel_operations, BorderLayout.SOUTH);
		panel_operations.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_operations.add(panel_3, BorderLayout.WEST);
		
		JButton btnDuplidateRegister = new JButton("重复报名");
		btnDuplidateRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			    logger.info("Button clicked : read possible duplicate records");

				// read data from database and fill up the table
				searchAndShow(SearchType.ByDuplicateReg, null, null);
			}
		});
		panel_3.add(btnDuplidateRegister);
		
		JButton btnWannaBeWithList = new JButton("和TA一起");
		btnWannaBeWithList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			    logger.info("Button clicked : read be-with records");

				// read data from database and fill up the table
				searchAndShow(SearchType.ByBeWith, null, null);
			}
		});
		panel_3.add(btnWannaBeWithList);
		
		JLabel label_3 = new JLabel(">>");
		panel_3.add(label_3);
		
		JButton btnAutoGrouping = new JButton("自动分组");
		btnAutoGrouping.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			    logger.info("Button clicked : auto-grouping");

			    gecMainTableModel.cleanGroupColumn();

			    new Thread(()->{
                    ControlPanel.instance.autoGroupingAndPersist();
                }).start();
			}
		});
		panel_3.add(btnAutoGrouping);
		
		JButton btn_show_remark_list = new JButton("问题查看");
		btn_show_remark_list.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

			    logger.info("Button clicked : read those remarked records");

			    searchAndShow(SearchType.ByHavingRemark, null, null);
			}
		});
		panel_3.add(btn_show_remark_list);
		
		JLabel label_4 = new JLabel(">>");
		panel_3.add(label_4);
		
		JButton btnExport = new JButton("结果导出");
        btnExport.addActionListener(e->{
            new Thread(()->{

                logger.info("Button clicked : export result to CSV file");

                ControlPanel.instance.writeGroupedResultToCsvFile();
            }).start();
        });
		panel_3.add(btnExport);
		
		JPanel panel_4 = new JPanel();
		panel_operations.add(panel_4, BorderLayout.EAST);
		
		JLabel lblNewLabel_3 = new JLabel("  ");
		panel_4.add(lblNewLabel_3);
		
		JPanel panel_modify = new JPanel();
		frmGecGroupingSoftware.getContentPane().add(panel_modify, BorderLayout.EAST);
		panel_modify.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_east_blank = new JPanel();
		panel_modify.add(panel_east_blank, BorderLayout.NORTH);
		
		JLabel lblNewLabel = new JLabel("使用关键字查询数据");
		lblNewLabel.setForeground(new Color(238,238,238));
		panel_east_blank.add(lblNewLabel);
		
		JPanel panel_east_buttons = new JPanel();
		panel_modify.add(panel_east_buttons, BorderLayout.CENTER);
		panel_east_buttons.setLayout(null);
		
		JButton btnEdit = new JButton("修改");
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				int selectedRowNumber = table_data.getSelectedRow();
				
				if (selectedRowNumber == -1 ) {
					return;
				}

				logger.info("Button clicked : Modify user info. UID:{}", table_data.getValueAt(selectedRowNumber,0));

				int selectedId = Integer.parseInt(table_data.getValueAt(selectedRowNumber, 0).toString());
				PersonInfoDialog.showDialog(selectedId);
			}
		});
		btnEdit.setBounds(6, 170, 103, 29);
		panel_east_buttons.add(btnEdit);
		
		JButton btnDelete = new JButton("删除");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table_data.getSelectedRow() == -1){
					
				} else {
					int selectedRowNumber = table_data.getSelectedRow();
					int selectedId = Integer.parseInt(table_data.getValueAt(selectedRowNumber, 0).toString());

                    logger.info("Button clicked : delete the person UID:{}", selectedId);

					new Thread(()->{
                        IComplexDao dao = new ComplexDao();
                        dao.deletePersonAndGrouping(selectedId);
                    }).start();

					gecMainTableModel.removeRow(selectedRowNumber);
					table_data.updateUI();
				}
			}
		});
		btnDelete.setBounds(6, 204, 103, 29);
		panel_east_buttons.add(btnDelete);
		
		textKeyword = new JTextField();

        textKeyword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode()== KeyEvent.VK_ENTER) {
                    btnKeywordSearch.doClick();
                }
                super.keyPressed(e);
            }
        });

		textKeyword.setBounds(6, 49, 103, 26);
		panel_east_buttons.add(textKeyword);
		textKeyword.setColumns(10);
		
		btnKeywordSearch = new JButton("查询");

		btnKeywordSearch.addActionListener(e -> {

            String key = textKeyword.getText();
            searchAndShow(SearchType.ByKeyWord,null, key);

            logger.info("Button clicked : search key workd {}", key);

        });
		btnKeywordSearch.setBounds(6, 82, 103, 29);
		panel_east_buttons.add(btnKeywordSearch);
		
		JLabel label = new JLabel("关键字查询");
		label.setBounds(19, 21, 73, 16);
		panel_east_buttons.add(label);
		
		JButton btn_grpMgr = new JButton("小组管理");
		btn_grpMgr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Button clicked : team management");
			    GroupListDialog.ShowDialog();
			}
		});
		btn_grpMgr.setBounds(6, 354, 103, 29);
		panel_east_buttons.add(btn_grpMgr);
		
		JButton btn_allTealLeader = new JButton("所有组长");
		btn_allTealLeader.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Button clicked : show all team leaders");
			    searchAndShow(SearchType.ByGroupLeaders, null, null);
			}
		});
		btn_allTealLeader.setBounds(6, 284, 103, 29);
		panel_east_buttons.add(btn_allTealLeader);
		
		JButton btn_searchInOrder = new JButton("列组显示");
		btn_searchInOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Button clicked : show all in group order");
			    searchAndShow(SearchType.ByGroupOrder, null, null);
			}
		});
		btn_searchInOrder.setBounds(6, 319, 103, 29);
		panel_east_buttons.add(btn_searchInOrder);
		
		JPanel panel_center = new JPanel();
		frmGecGroupingSoftware.getContentPane().add(panel_center, BorderLayout.CENTER);
		panel_center.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_center_top = new JPanel();
		panel_center.add(panel_center_top, BorderLayout.NORTH);
		panel_center_top.setLayout(new BorderLayout(0, 0));
		
		/*
		JPanel panel_center_top_east = new JPanel();
		panel_center_top.add(panel_center_top_east, BorderLayout.EAST);
		*/
		
		JScrollPane scrollPane = new JScrollPane();

		panel_center.add(scrollPane, BorderLayout.CENTER);
		
		table_data = new JTable(gecMainTableModel);
		scrollPane.setViewportView(table_data);


		final int COL_REG = 0;
		final int COL_G_GROUP=1;
		final int COL_G_ROLE =2;
		final int COL_GENDER=3;
		final int COL_ENAME=4;
		final int COL_CNAME=5;
		final int COL_MOBILE=6;
		final int COL_WECHAT=7;
		final int COL_BEWITH=8;
		final int COL_LANGUAGE=9;
		final int COL_REG_ROLE=10;
		final int COL_EXPERIENCE=11;
		final int COL_REMARK=12;

        Map<Integer, Integer> colWidthMap = new HashMap<>();
        colWidthMap.put(COL_REG, 60);
        colWidthMap.put(COL_G_GROUP, 180);
        colWidthMap.put(COL_G_ROLE, 70);
        colWidthMap.put(COL_GENDER, 33);
        colWidthMap.put(COL_ENAME, 90);
        colWidthMap.put(COL_CNAME, 90);
        colWidthMap.put(COL_MOBILE, 105);
        colWidthMap.put(COL_WECHAT, 150);
        colWidthMap.put(COL_BEWITH, 105);
        colWidthMap.put(COL_LANGUAGE, 150);
        colWidthMap.put(COL_REG_ROLE, 280);
        colWidthMap.put(COL_EXPERIENCE, 90);
        colWidthMap.put(COL_REMARK, 150);

        colWidthMap.forEach((k,v)->{
            TableColumn tableColumn = table_data.getColumnModel().getColumn(k);
            tableColumn.setPreferredWidth(v);
        });

        Map<Integer, Integer> colMaxWidthMap = new HashMap<>();
        colMaxWidthMap.put(COL_REG, 60);
        colMaxWidthMap.put(COL_G_GROUP, 180);
        colMaxWidthMap.put(COL_G_ROLE, 80);
        colMaxWidthMap.put(COL_GENDER, 33);
        //colPreferredWidthMap.put(COL_ENAME, 120);
        colMaxWidthMap.put(COL_CNAME, 120);
        //colPreferredWidthMap.put(COL_MOBILE, 100);
        //colPreferredWidthMap.put(COL_WECHAT, 150);
        //colPreferredWidthMap.put(COL_BEWITH, 105);
        //colPreferredWidthMap.put(COL_LANGUAGE, 150);
        colMaxWidthMap.put(COL_EXPERIENCE, 90);

        colMaxWidthMap.forEach((k,v)->{
            TableColumn tableColumn = table_data.getColumnModel().getColumn(k);
            tableColumn.setMaxWidth(v);
        });

    /*
		TableColumn tableColumn = table_data.getColumnModel().getColumn(0);
        tableColumn.setPreferredWidth(45);
        tableColumn.setMaxWidth(45);
        tableColumn = table_data.getColumnModel().getColumn(3);
        tableColumn.setPreferredWidth(33);
        tableColumn.setMaxWidth(33);**/
		
        table_data.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
        		if (e.getClickCount() == 2) {
        			btnEdit.doClick();
        		}
        	}
        });
        
	}
}
