package com.shawn.gec.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.shawn.gec.control.SettingCenter;
import com.shawn.gec.control.Utils;
import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.Role;

public class GroupingDao implements IGroupingDao {

	{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.shawn.gec.dao.IActivityGroupDao#InsertOrUpdate(com.shawn.gec.po.ActivityGroup)
	 */
	@Override
	public Grouping InsertOrUpdate(Grouping grp)
	{
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlInsert = String.format("insert into grouping(g_uid, g_roledesc , g_bewith, g_gid, g_language, g_groupname, g_remark, g_fixed ) " +
											"select %d, '%s', %d, %d, '%s', '%s', '%s', %d " +
											"where not exists (select 1 from grouping where g_uid = %d);", 
											grp.getPerson_id(),
											grp.getGroupedRole() == null?null:grp.getGroupedRole().RoleNames, grp.getBe_with_uid(),
											grp.getGroup_id(), grp.getLanguage(), Utils.getGroupName(grp), grp.getRemark(), grp.getIsFixed(),
											grp.getPerson_id()
										);
			
			//System.out.println(sqlInsert);
			
			statement.executeUpdate(sqlInsert);
			
			// update
			String sqlUpdate = String.format("update grouping set g_roledesc='%s', "
					+ "							g_bewith=%d, g_gid=%d, g_language='%s', g_groupname='%s', g_remark='%s', g_fixed=%d where g_uid = %d",
											grp.getGroupedRole() == null?null:grp.getGroupedRole().RoleNames,
											grp.getBe_with_uid(), grp.getGroup_id(), grp.getLanguage(), Utils.getGroupName(grp), grp.getRemark(), grp.getIsFixed(), grp.getPerson_id());
			statement.executeUpdate(sqlUpdate);
			
			
	        // Close the connection
	        conn.close();
	        
	        return GetGroupingByPersonId(grp.getPerson_id());
	        
		}catch(Exception ex) {
			ex.printStackTrace();
		}

		return new Grouping();
	}
	
	public List<Grouping> GetGroupings(){
		
		List<Grouping> result = new LinkedList<Grouping>();
		
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select * from grouping;");
			
			ResultSet rs = statement.executeQuery(sqlSelect);
			Grouping g = new Grouping();

			while (rs.next()) {
	            g.setId(rs.getInt("id"));   // Column 1
	            g.setPerson_id(rs.getInt("g_uid"));
	            g.setBe_with_uid(rs.getInt("g_bewith"));
	            g.setGroup_id(rs.getInt("g_gid"));
	            g.setLanguage(rs.getString("g_language"));
	            g.setRemark(rs.getString("g_remark"));
	            g.setIsFixed(rs.getInt("g_fixed"));
	        
	            String roles = rs.getString("g_roledesc");
	            Role groupedRole = new Role(roles);
	            g.setGroupedRole(groupedRole);
	            
	            result.add(g);
			}
			
			System.out.println(g.toString());
			
			return result;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public Grouping GetGroupingByPersonId(int personId){
		
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select * from grouping where g_uid = %d;", personId);
			
			ResultSet rs = statement.executeQuery(sqlSelect);
			Grouping g = new Grouping();
			

			while (rs.next()) {
	            g.setId(rs.getInt("id"));   // Column 1
	            g.setPerson_id(rs.getInt("g_uid"));
	            g.setBe_with_uid(rs.getInt("g_bewith"));
	            g.setGroup_id(rs.getInt("g_gid"));
	            g.setLanguage(rs.getString("g_language"));
	            g.setRemark(rs.getString("g_remark"));
	            g.setIsFixed(rs.getInt("g_fixed"));
	        
	            String roles = rs.getString("g_roledesc");
	            Role groupedRole = new Role(roles);
	            g.setGroupedRole(groupedRole);
			}
			
			System.out.println(g.toString());
			
			return g;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void LockGroup(int grpID, boolean needLock) {
		// 
		
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sql = String.format("update grouping set g_fixed = %d where g_gid = %d;", needLock ? 1 : 0, grpID);
			statement.executeUpdate(sql);

	        // Close the connection
	        conn.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
}
