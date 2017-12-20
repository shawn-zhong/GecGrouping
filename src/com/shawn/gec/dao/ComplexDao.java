package com.shawn.gec.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.shawn.gec.control.SettingCenter;
import com.shawn.gec.control.Utils;
import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.GroupingStat;
import com.shawn.gec.po.MemGroupingItem;
import com.shawn.gec.po.Person;
import com.shawn.gec.po.Role;

public class ComplexDao {
	
	{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void cleanDatabase() {
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sql = String.format("delete from person;");
			statement.executeUpdate(sql);
			
			sql = String.format("delete from grouping;");
			statement.executeUpdate(sql);
			
			sql = String.format("VACUUM;");
			statement.executeUpdate(sql);
			
	        // Close the connection
	        conn.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public int getMaxPersonId() {
		int maxPid=-1;
		
		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select max(id) as maxid from person;");
			
			ResultSet rs = statement.executeQuery(sqlSelect);

			while (rs.next()) {
				maxPid = rs.getInt("maxid");
			}

			conn.close();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("max ID from person Table is :" + maxPid);
		return maxPid;
	}
	
	public void deletePersonAndGrouping(int uid) {

		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sql = String.format("delete from person where id = %d;", uid);
			statement.executeUpdate(sql);
			
			sql = String.format("delete from grouping where g_uid = %d;", uid);
			statement.executeUpdate(sql);
			
			sql = String.format("VACUUM;");
			statement.executeUpdate(sql);
			
	        // Close the connection
	        conn.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public List<GroupingStat> getGroupingStatistics() {
		
		List<GroupingStat> result = new LinkedList<GroupingStat>();
		
		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select g_gid, g_language, peopleCount as maleCount, sum(peopleCount) as peopleCount, g_fixed from ( " +
											 "	select g_gid, is_male, count(is_male) as peopleCount, g_language, count(*), g_fixed from person left join grouping on person.id = grouping.g_uid " +
											 "		group by grouping.g_gid, person.is_male order by g_gid, is_male asc " +
											 ") group by g_gid");
			
			ResultSet rs = statement.executeQuery(sqlSelect);

			while (rs.next()) {
				GroupingStat item = new GroupingStat();
				item.setGroupId(rs.getInt("g_gid"));
				item.setLanguage(rs.getString("g_language"));
				item.setMaleCount(rs.getInt("maleCount"));
				item.setPeopleCount(rs.getInt("peopleCount"));
				item.setG_fixed(rs.getInt("g_fixed")==1);
				result.add(item);
			}
			conn.close();
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return result;
	}

	public List<String> getAllRegesterLanguages() {

		List<String> result = new LinkedList<>();

		try {
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.getDbFilePath());
			System.out.println(SettingCenter.getDbFilePath());
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("select language from person group by language order by (count(*)) desc");

			while (rs.next()) {
				result.add(rs.getString("language"));
			}

			conn.close();
		}catch(Exception ex) {
			ex.printStackTrace();
		}

		return result;
	}

	public void UpdateGroupName(String language, int groupId) {
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.getDbFilePath());
			Statement statement = conn.createStatement();

			String sql = String.format("update grouping set g_language='%s', g_groupname='%s' where g_gid=%d;", language, Utils.getGroupName(language, groupId), groupId);
			statement.executeUpdate(sql);

			// Close the connection
			conn.close();

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void InsertOrUpdateRolesPriority(String roleName, int priority) {
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();

			String sqlInsert = String.format("INSERT INTO Roles (roleName, priority) " +
							"select '%s', %d " +
							"where not exists (select 1 from Roles where roleName = '%s');",
						roleName, priority, roleName
			);

			statement.executeUpdate(sqlInsert);

			String sqlUpdate = String.format("update roles set priority = %d where roleName='%s';",
					priority, roleName
			);

			statement.executeUpdate(sqlUpdate);

			conn.close();

		}catch(Exception ex){
			ex.printStackTrace();
			//System.out.println(ex.getMessage());
		}
	}
}
