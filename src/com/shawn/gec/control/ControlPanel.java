package com.shawn.gec.control;


import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.List;

import com.csvreader.*;
import com.shawn.gec.dao.ActivityDao;
import com.shawn.gec.dao.IActivityDao;
import com.shawn.gec.dao.IPersonDao;
import com.shawn.gec.dao.PersonDao;
import com.shawn.gec.po.Activity;
import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.Person;
import com.shawn.gec.po.Role;

public class ControlPanel {
	
	public static Activity act = new Activity();

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		
		// add a new activity info
		
		act.setCapacity(260);
		act.setGroup_capacity(15);
		act.setCode("outdoor-1");
		act.setCreate_staff("shawn");
		//act.setCreate_time("");
		act.setName("户外活动第一期");
		act.setRemark("这是一条备注");
		
		IActivityDao dao2 = new ActivityDao();
		act = dao2.InsertOrUpdateActivity(act);

		List<Activity> lst = dao2.GetActivityList();
		System.out.println(lst.toString());

		
	}

}
