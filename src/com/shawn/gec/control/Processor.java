package com.shawn.gec.control;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.shawn.gec.dao.ComplexDao;
import com.shawn.gec.dao.GroupingDao;
import com.shawn.gec.dao.IGroupingDao;
import com.shawn.gec.dao.IPersonDao;
import com.shawn.gec.dao.PersonDao;
import com.shawn.gec.po.*;
import com.shawn.gec.ui.AppWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Processor {

	private Logger logger = LoggerFactory.getLogger(Processor.class);

	public static Processor instance = new Processor();
	
	private Processor(){}
	
	public void CleanDatabaseAndMemory() {
		ComplexDao dao = new ComplexDao();
		dao.cleanDatabase();
		MemGrouping.Instance.emptyAll();
	}
	
	public void ReadRecordsFromCsvFile() {
		// clean the db and memory and reload all the things
		CleanDatabaseAndMemory();
		ReadFileAndWriteDB(SettingCenter.getExlFilePath(), -1);
	}
	
	public void ReadDeltaRecordsFromCsvFile() {
		ComplexDao dao = new ComplexDao();
		int maxId = dao.getMaxPersonId();
		
		// only load thoese new ones
		ReadFileAndWriteDB(SettingCenter.getExlDeltaFilePath(), maxId);
	}
	
	public void AutoGrouping() {

		logger.info("Grouping function begins ...");

		MemGrouping.Instance.emptyAll();
		
		PersonDao personDao = new PersonDao();
		List<MemGroupingItem> items = personDao.GetPersonByKeyword("");
		Set<Integer> usedGroupIds = new HashSet<>();

		int addedCounter = 0;
		for (MemGroupingItem item : items ) {
			item.grouping.setGroupedRole(null);	// null : waiting for grouping
			
			if (item.grouping.getIsFixed() != 1) {    // only process those unfixed
				item.grouping.setRemark(""); // clean the grouping remark first
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
		GroupingDao dao = new GroupingDao();
		
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
				dao.InsertOrUpdate(g);
			}
		}

		logger.info("Grouping result updated to database");

		// all loaded. refresh UI for displaying
		AppWindow.window.searchAndShow(AppWindow.SearchType.ByKeyWord, null, null);
	}
	
	private void ReadFileAndWriteDB(String _filePath, int _currentMaxPid) {

		try {
			CsvReader csvReader = new CsvReader(_filePath, ',', Charset.forName("UTF-8"));
			csvReader.readHeaders(); // skip the first line 

			int counter = 0;
			while (csvReader.readRecord()){
				//System.out.println(csvReader.getRawRecord());
			
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
				person.setExperience(Utils.explainExperience(_experience));
				person.setRemark("");

				int mobileLen = Utils.trimPhoneNumber(_mobileNumber).length();
				if (mobileLen ==11 || mobileLen == 13) {
					_mobileNumber = Utils.trimPhoneNumber(_mobileNumber);
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

				int beWithLen = Utils.trimPhoneNumber(beWithText).length();
				if (beWithLen == 11 || beWithLen == 13)
					beWithText = Utils.trimPhoneNumber(beWithText);
				
				person.setBe_with(beWithText);
				
				// Load into Database
				IPersonDao dao = new PersonDao();
				person = dao.InsertOrUpdatePerson(person);
				
				// update grouping in DB and memory
				Grouping grp = new Grouping();
				grp.setPerson_id(person.getId());
				grp.setGroup_id(0);
				grp.setRole(r);
				grp.setLanguage(_language);
				grp.setRemark("");
				
				IGroupingDao gdao = new GroupingDao();
				grp = gdao.InsertOrUpdate(grp);
				
				// store grouping in memory
				counter++;
				AppWindow.window.addRowToTable(person, grp);
				AppWindow.window.updateTitle(String.format("Loading %d", counter));
			}

			// all loaded. refresh for better displaying
			AppWindow.window.searchAndShow(AppWindow.SearchType.ByKeyWord, null, null);
			
			System.out.println(MemGrouping.Instance.toString());

			csvReader.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	public void WriteRecordsToCsvFile() {
		try {
			PersonDao dao = new PersonDao();
			List<MemGroupingItem> data = dao.GetAllGroupMembers();

			CsvWriter writer = new CsvWriter(SettingCenter.getExlOutputPath(), ',', Charset.forName("UTF-8"));

			// write header

			int lastGroupId = 0;
			int groupIndex = 0;
			for (MemGroupingItem item : data) {

				String regId = String.format("%03d", item.person.getId());
				String groupName = Utils.getGroupName(item.grouping.getLanguage(), item.grouping.getGroup_id());
				String engName = item.person.getEnglish_name();
				String chnName = item.person.getChinese_name();
				String gRole = item.grouping.getGroupedRole().RoleNames;
				String gender = item.person.getIs_male()==1?"M":"F";
				String mobile = item.person.getMobile();
				String wechat = item.person.getWechat();
				String experience = String.format("%d", item.person.getExperience());
				String displayName = String.format("%s (*%s)", engName, mobile.substring((mobile.length()-3)<0?0:(mobile.length()-3), mobile.length()));

				if (lastGroupId != item.grouping.getGroup_id()) {
					groupIndex = 0;
					lastGroupId = item.grouping.getGroup_id();

					String[] header = new String[] {"报名编号", "小组编号", "小组名称", "小组角色", "性别",  "英文名(手机尾号)", "中文名", "手机", "微信", "经验值"};
					writer.writeRecord(header);
				}

				groupIndex++;

				String[] record = new String[] {regId, String.format("%d", groupIndex), groupName, gRole, gender, displayName, chnName, mobile, wechat, experience};
				writer.writeRecord(record);
			}

			writer.close();

			AppWindow.window.updateTitle("分组结果已导出至文件"+SettingCenter.getExlOutputPath());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
