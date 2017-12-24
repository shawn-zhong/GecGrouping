package com.shawn.gec.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.shawn.gec.dao.*;
import com.shawn.gec.po.*;
import com.shawn.gec.ui.AppWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlPanel {

	private Logger logger = LoggerFactory.getLogger(ControlPanel.class);

	public static ControlPanel instance = new ControlPanel();
	
	private ControlPanel(){}
	
	private void cleanDatabaseAndMemory() {
		IComplexDao dao = new ComplexDao();
		dao.cleanDatabase();
		MemGrouping.Instance.emptyAll();
	}
	
	public void readCsvFileAndWriteToDB() {
		// clean the db and memory and reload all the things
		cleanDatabaseAndMemory();
		readFileAndWriteDB(SettingCenter.getExlFilePath(), -1);
	}
	
	public void readDeltaCsvFileAndWriteToDB() {
		IComplexDao dao = new ComplexDao();
		int maxId = dao.getMaxPersonId();
		
		// only load thoese new ones
		readFileAndWriteDB(SettingCenter.getExlDeltaFilePath(), maxId);
	}
	
	public void autoGroupingAndPersist() {

		logger.info("Grouping function begins ...");

		SettingCenter.ReadFromSettingFile();
		AppWindow.window.refreshWindowTitle();
		MemGrouping.Instance.emptyAll();
		
		IPersonDao personDao = new PersonDao();
		List<MemGroupingItem> items = personDao.getPersonByKeyword("");
		Set<Integer> usedGroupIds = new HashSet<>();

		int addedCounter = 0;
		for (MemGroupingItem item : items ) {
			if (item.grouping.getIsFixed() != 1) {    // only process those unfixed
				item.grouping.setRemark(""); // clean the grouping remark first
				item.grouping.setGroupedRole(null);	// null : waiting for grouping
				item.grouping.setBe_with_uid(-1);
				item.grouping.setGroup_id(0);

				MemGrouping.Instance.addGrouping(item.person, item.grouping);
				addedCounter++;
			} else {
				usedGroupIds.add(item.grouping.getGroup_id());
			}
		}

		logger.info("Loaded all the records needing grouping into memory. count:{}", addedCounter);
		
		// now to execute the group steps:

		logger.info("GROUPING-STEP-1 : refreshWannaWithWho");
		MemGrouping.Instance.refreshWannaWithWho();

		logger.info("GROUPING-STEP-2 : correctLanguageData");
		MemGrouping.Instance.correctLanguageData();

		logger.info("GROUPING-STEP-3 : divideToCircleGroupsByWannaBeWith");
		MemGrouping.Instance.divideToCircleGroupsByWannaBeWith();

		logger.info("GROUPING-STEP-4 : calcHowManyGroupsAndIdealCapacity");
		MemGrouping.Instance.calcHowManyGroupsAndIdealCapacity(usedGroupIds);

		logger.info("GROUPING-STEP-5 : allocateImportantRoles(TeamLeader and Vice TeamLeader)");
		MemGrouping.Instance.allocateImportantRoles();

		logger.info("GROUPING-STEP-6 : allocateRemainCircles");
		MemGrouping.Instance.allocateRemainCircles();

		logger.info("GROUPING-STEP-7 : allocateOtherRoles");
		MemGrouping.Instance.allocateOtherRoles();

		logger.info("GROUPING-STEP-8 : allocateRestPerson");
		MemGrouping.Instance.allocateRestPerson();

		logger.info("GROUPING-STEP-9 : markedAllGrouping");
		MemGrouping.Instance.markedAllGrouping();
		
		// update the DB
		IGroupingDao dao = new GroupingDao();
		
		Map<String, LanguageGrouping> groupingResult = MemGrouping.Instance.getGroupedResult();
		
		Iterator<Entry<String, LanguageGrouping>> lanIter = groupingResult.entrySet().iterator();
		while (lanIter.hasNext()) {
			Map.Entry<String, LanguageGrouping> lanEntry = lanIter.next();
			LanguageGrouping lanGroup = lanEntry.getValue();
			Map<Integer, MemGroupingItem> grpPersons = lanGroup.groupingItems;
			
			String language = lanGroup.language;
			
			Iterator<Entry<Integer, MemGroupingItem>> personIter = grpPersons.entrySet().iterator();
			while (personIter.hasNext()) {
				Entry<Integer, MemGroupingItem> personEntry = personIter.next();
				MemGroupingItem person = personEntry.getValue();
				
				Person p = person.person;
				Grouping g = person.grouping;
				dao.insertOrUpdate(g);
			}
		}

		logger.info("Grouping result updated to database");

		// all loaded. refresh UI for displaying
		AppWindow.window.searchAndShow(AppWindow.SearchType.ByKeyWord, null, null);
	}
	
	private void readFileAndWriteDB(String _filePath, int _currentMaxPid) {

		try {
			CsvReader csvReader = new CsvReader(_filePath, ',', Charset.forName("UTF-8"));
			csvReader.readHeaders(); // skip the first line 

			int counter = 0;
			while (csvReader.readRecord()){
			
				// get the raw data
				int _signupId = Integer.parseInt(csvReader.get(SettingCenter.COL_SIGNUP_ID));
				
				if (_signupId <= _currentMaxPid)	// incremental mode
					continue;	
				
				//Date _signupDate = DateFormat.parse(csvReader.get(COL_SIGNUP_DATE));
				int _gender = csvReader.get(SettingCenter.COL_GENDER).contains("(M)")?1:0;
				String _englishName = csvReader.get(SettingCenter.COL_ENG_NAME);
				String _chineseName = csvReader.get(SettingCenter.COL_CHN_NAME);
				String _hometown = csvReader.get(SettingCenter.COL_HOMETOWN);
				String _occupation = csvReader.get(SettingCenter.COL_OCCUPATION);
				String _roleSource = csvReader.get(SettingCenter.COL_ROLE);
				String _wannaBeWith = csvReader.get(SettingCenter.COL_WANNA_BE_WITH);
				String _mobileNumber = csvReader.get(SettingCenter.COL_MOBILE_NUMBER);
				String _qq = csvReader.get(SettingCenter.COL_QQ);
				String _wechat = csvReader.get(SettingCenter.COL_WECHAT);
				String _district = csvReader.get(SettingCenter.COL_DISTRICT);
				String _experience = csvReader.get(SettingCenter.COL_EXPERIENCE);
				String _language = csvReader.get(SettingCenter.COL_LANGUAGE);
				
				// update Person in database
				
				Person person = new Person();
				person.setId(_signupId);
				person.setIs_male(_gender);
				person.setEnglish_name(_englishName);
				person.setChinese_name(_chineseName);
				person.setHometown(_hometown);
				person.setOccupation(_occupation);

				person.setQq(_qq);
				person.setWechat(_wechat);
				person.setDistrict(_district);
				person.setLanguage(_language);
				person.setExperience(Utils.explainExperienceStarts(_experience));
				person.setRemark("");

				int mobileLen = Utils.regulateMobileNumber(_mobileNumber).length();
				if (mobileLen ==11 || mobileLen == 13) {
					_mobileNumber = Utils.regulateMobileNumber(_mobileNumber);
				}
				person.setMobile(_mobileNumber);


				Role r = Role.explainFromText(_roleSource);
				person.setRoles(r.RoleNames);
				
				// sameple:〖15220036407〗
				int st = _wannaBeWith.indexOf("〖");
				int ed = _wannaBeWith.indexOf("〗");
				String beWithText = "";
				if (st>-1 && ed>-1) {
					beWithText = _wannaBeWith.substring(st+1, ed);
				}

				int beWithLen = Utils.regulateMobileNumber(beWithText).length();
				if (beWithLen == 11 || beWithLen == 13)
					beWithText = Utils.regulateMobileNumber(beWithText);
				
				person.setBe_with(beWithText);
				
				// Load into Database
				IPersonDao dao = new PersonDao();
				person = dao.insertOrUpdatePerson(person);
				
				// update grouping in DB and memory
				Grouping grp = new Grouping();
				grp.setPerson_id(person.getId());
				grp.setGroup_id(0);
				grp.setRole(r);
				grp.setLanguage(_language);
				grp.setRemark("");
				
				IGroupingDao gdao = new GroupingDao();
				grp = gdao.insertOrUpdate(grp);
				
				// store grouping in memory
				counter++;
				AppWindow.window.addRowToTable(person, grp);
				AppWindow.window.updateHeader(String.format("Loading %d", counter));
			}

			// all loaded. refresh for better displaying
			AppWindow.window.searchAndShow(AppWindow.SearchType.ByKeyWord, null, null);
			
			//System.out.println(MemGrouping.Instance.toString());

			csvReader.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void writeGroupedResultToCsvFile() {
		try {
			IPersonDao dao = new PersonDao();
			List<MemGroupingItem> data = dao.getAllGroupMembersOrderByGroupNo();

			File ofile = new File(SettingCenter.getExlOutputPath());
			OutputStream out = new FileOutputStream(ofile);
			out.write(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF });
			CsvWriter writer = new CsvWriter(out, ',', Charset.forName("UTF-8"));

			//CsvWriter writer = new CsvWriter(SettingCenter.getExlOutputPath(), ',', Charset.forName("UTF-8"));

			// write header

			int lastGroupId = 0;
			int groupIndex = 0;
			for (MemGroupingItem item : data) {

				String regId = String.format("%03d", item.person.getId());
				String groupName = Utils.getGroupName(item.grouping.getLanguage(), item.grouping.getGroup_id());
				String groupNo = item.grouping.getGroup_id()+"";
				String engName = item.person.getEnglish_name();
				String chnName = item.person.getChinese_name();
				String gRole = item.grouping.getGroupedRole().RoleNames;
				String gender = item.person.getIs_male()==1?"M":"F";
				String mobile = item.person.getMobile();
				String wechat = item.person.getWechat();
				String experience = String.format("%d", item.person.getExperience());
				String displayName = String.format("%s (*%s)", engName, mobile.substring((mobile.length()-3)<0?0:(mobile.length()-3), mobile.length()));

				String regRole = item.person.getRoles();
				String remark = item.person.getRemark()+" "+item.grouping.getRemark();
				remark.trim();

				if (lastGroupId != item.grouping.getGroup_id()) {

					if (lastGroupId !=0) {
						writer.writeRecord(new String[]{"", "", ""});
					}

					groupIndex = 0;
					lastGroupId = item.grouping.getGroup_id();


					String[] header = new String[] {"ID", "第几组", "组内编号", "小组名称", "组内角色", "性别",  "英文名(&手机尾号)", "中文名", "手机", "微信", "经验值", "报名角色/备注"};
					writer.writeRecord(header);
				}

				groupIndex++;

				String[] record = new String[] {regId, groupNo, String.format("%d", groupIndex), groupName, gRole, gender, displayName, chnName, mobile, wechat, experience, regRole+";"+remark};
				writer.writeRecord(record);
			}

			writer.close();

			AppWindow.window.updateHeader("分组结果已导出至文件"+SettingCenter.getExlOutputPath());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
