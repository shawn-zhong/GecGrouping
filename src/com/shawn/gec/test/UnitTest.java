package com.shawn.gec.test;
 
import java.util.List;
import java.util.Random;

import com.shawn.gec.po.Role;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.shawn.gec.control.SettingCenter;
import com.shawn.gec.dao.ComplexDao;
import com.shawn.gec.dao.IPersonDao;
import com.shawn.gec.dao.PersonDao;
import com.shawn.gec.po.GroupingStat;
import com.shawn.gec.po.MemGroupingItem;

public class UnitTest {

	@Before
	public void setup() {
		SettingCenter.ReadFromSettingFile();
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	@Ignore
	public void testFetchPersonByKey(){
		IPersonDao personDao = new PersonDao();
		List<MemGroupingItem> items = personDao.GetPersonByKeyword("");
		
		System.out.println("seached size : " + items.size());
		
		for (MemGroupingItem item : items) {
			System.out.println(item.person);
			System.out.println(item.grouping);
		}
	}
	
	@Test
	@Ignore
	public void testFetchPersonById() {
		IPersonDao personDao = new PersonDao();
		MemGroupingItem item = personDao.GetPersonById(3);
		System.out.println(item);
	}
	
	@Test
	@Ignore
	public void testCleanDB() {
		ComplexDao complexDao = new ComplexDao();
		complexDao.cleanDatabase();
	}
	
	@Test

	public void testGetMaxPid() {
		ComplexDao complexDao = new ComplexDao();
		complexDao.getMaxPersonId();
	}
	
	@Test
	@Ignore
	public void testGroupingStat() {
		ComplexDao dao = new ComplexDao();
		List<GroupingStat> result = dao.getGroupingStatistics();
	
		//result.stream().forEach(r-> {System.out.println(r);});
		result.stream().forEach(System.out::println);
	}

	@Test
	@Ignore
	public void testRoleExpalnation() {
		Role role = Role.explainFromText("副组长（联络及安全）-Vice TL┋接待（邀请接待老外）-Reception┋摄影（图要美美的）-Camerist┋零食（采购后AA制）-Snack┋后勤（美食美景寻找）-Logistics┋娱乐（搞气氛）-Entertainment┋写报告（记录快乐）-Report");
		System.out.println(role.toString());

		System.out.println(role.hasRole("组长"));
		System.out.println(role.hasRole("副组长"));
		System.out.println(role.hasRole("接待"));
	}

	@Test
	@Ignore
	public void testCast() {
		Integer a = null;
		System.out.println(a);

	}

	@Test
	@Ignore
	public void testRandom() {

		for (int i=0; i<30; i++) {
			Random random = new Random();
			int index = random.nextInt(2);
			System.out.println(index + "");
		}
	}

	@Test
	public  void testGetAllLanguage() {
		ComplexDao dao = new ComplexDao();
		dao.getAllRegesterLanguages().forEach(System.out::println);
	}
}
 