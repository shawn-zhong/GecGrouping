package com.shawn.gec.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.shawn.gec.control.SettingCenter;
import com.shawn.gec.po.Activity;

public class ActivityDao implements IActivityDao {
	
	{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.shawn.gec.dao.IActivityDao#InsertOrUpdateActivity(com.shawn.gec.po.Activity)
	 */
	@Override
	public Activity InsertOrUpdateActivity(Activity act){
		
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ SettingCenter.getDbFilePath())){
			Statement statement = conn.createStatement();

			
			String sqlInsert = String.format("insert into activity(code, name, capacity, group_capacity, launch_time , create_time , create_staff, remark) " + 
											"select '%s', '%s', %d, '%d', '%s', '%s', '%s', '%s' " +
											"where not exists (select 1 from activity where code = '%s');", 
											act.getCode(), act.getName(), act.getCapacity(), act.getGroup_capacity(), act.getLaunch_time().toString(), act.getCreate_time().toString(), 
											act.getCreate_staff(), act.getRemark(), act.getCode()
										);
			
			statement.executeUpdate(sqlInsert);
			
			String sqlUpdate = String.format("update activity set name = '%s', capacity=%d, launch_time='%s', create_time = '%s', remark='%s' where code='%s';", 
											act.getName(), act.getCapacity(), act.getLaunch_time(), act.getCreate_time(), act.getRemark(), act.getCode()
											);
			
			statement.executeUpdate(sqlUpdate);
			
			String sqlSelect = String.format("select id from activity where code='%s';", act.getCode());
			
			ResultSet rs = statement.executeQuery(sqlSelect);
			String idstr = "-1";
			
			while (rs.next()) {
	            idstr   = rs.getString("id");   // Column 1
	        }
	       
	        // Close the connection
	        conn.close();
	        
	        act.setId(Integer.parseInt(idstr));
	        return act;
	        
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.shawn.gec.dao.IActivityDao#GetActivityList()
	 */
	@Override
	public List<Activity> GetActivityList() {
		
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.getDbFilePath())){
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select * from activity");
			
			ResultSet rs = statement.executeQuery(sqlSelect);
			
			List<Activity> list = new LinkedList<Activity>();
			
			while (rs.next()) {
				
				Activity info = new Activity();
				
	            info.setId(rs.getInt("id"));
	            info.setCapacity(rs.getInt("capacity"));
	            info.setCode(rs.getString("code"));
	            info.setCreate_staff(rs.getString("create_staff"));
	            info.setGroup_capacity(rs.getInt("group_capacity"));
	            info.setName(rs.getString("name"));
	            info.setRemark(rs.getString("remark"));

	            list.add(info);
	        }
	       
	        // Close the connection
	        conn.close();
	        
	        
	        return list;
			
		}catch (Exception ex){
			System.out.println(ex.getMessage());
		}
		
		return null;
	}
}
