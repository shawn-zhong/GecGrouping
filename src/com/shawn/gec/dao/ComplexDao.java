package com.shawn.gec.dao;

import com.shawn.gec.control.Utils;
import com.shawn.gec.po.GroupingStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static com.shawn.gec.control.SettingCenter.*;

public class ComplexDao implements IComplexDao {

	private Logger logger = LoggerFactory.getLogger(ComplexDao.class);

	{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("Exception while loading database driver", e);
		}
	}

	public void testDBConnection() throws SQLException{

		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ getDbFilePath())){
			Statement statement = conn.createStatement();

			String sqlSelect = "select max(id) as maxid from person;";
			logger.debug(sqlSelect);
			ResultSet rs = statement.executeQuery(sqlSelect);

			while (rs.next()) {
				rs.getInt("maxid");
			}

			conn.close();
		}catch(Exception ex) {
			logger.error("Exception while executing database sql", ex);
			throw ex;
		}
	}
	
	public void cleanDatabase() {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ getDbFilePath())){
			Statement statement = conn.createStatement();
			
			String sql = "delete from person;";
			logger.debug(sql);
			statement.executeUpdate(sql);
			
			sql = "delete from grouping;";
			statement.executeUpdate(sql);
			
			sql = "VACUUM;";
			statement.executeUpdate(sql);
			
	        // Close the connection
	        conn.close();
			
		}catch(Exception ex){
			logger.error("Exception while executing database sql", ex);
		}
	}

	public int getMaxPersonId() {
		int maxPid=-1;
		
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ getDbFilePath())){
			Statement statement = conn.createStatement();
			
			String sqlSelect = "select max(id) as maxid from person;";
			logger.debug(sqlSelect);
			ResultSet rs = statement.executeQuery(sqlSelect);

			while (rs.next()) {
				maxPid = rs.getInt("maxid");
			}

			conn.close();
		}catch(Exception ex) {
			logger.error("Exception while executing database sql", ex);
		}

		return maxPid;
	}
	
	public void deletePersonAndGrouping(int uid) {

		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ getDbFilePath())){
			Statement statement = conn.createStatement();
			
			String sql = String.format("delete from person where id = %d;", uid);
			logger.debug(sql);
			statement.executeUpdate(sql);
			
			sql = String.format("delete from grouping where g_uid = %d;", uid);
			statement.executeUpdate(sql);
			
			sql = "VACUUM;";
			statement.executeUpdate(sql);
			
	        // Close the connection
	        conn.close();
			
		}catch(Exception ex){
			logger.error("Exception while executing database sql", ex);
		}
	}
	
	public List<GroupingStat> getGroupingStatistics() {
		
		List<GroupingStat> result = new LinkedList<GroupingStat>();
		
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ getDbFilePath())){
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select g_gid, g_language, peopleCount as maleCount, sum(peopleCount) as peopleCount, g_fixed from ( " +
											 "	select g_gid, is_male, count(is_male) as peopleCount, g_language, count(*), g_fixed from person left join grouping on person.id = grouping.g_uid " +
											 "		group by grouping.g_gid, person.is_male order by g_gid, is_male asc " +
											 ") group by g_gid");

			logger.debug(sqlSelect);
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
			logger.error("Exception while executing database sql", ex);
		}
		
		return result;
	}

	public List<String> getAllRegesterLanguages() {

		List<String> result = new LinkedList<>();

		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ getDbFilePath())){
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery("select language from person group by language order by (count(*)) desc");

			while (rs.next()) {
				result.add(rs.getString("language"));
			}

			conn.close();
		}catch(Exception ex) {
			logger.error("Exception while executing database sql", ex);
		}

		return result;
	}

	public void updateGroupName(String language, int groupId) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ getDbFilePath())){
			Statement statement = conn.createStatement();

			String sql = String.format("update grouping set g_language='%s', g_groupname='%s' where g_gid=%d;", language, Utils.getGroupName(language, groupId), groupId);
			logger.debug(sql);
			statement.executeUpdate(sql);

			// Close the connection
			conn.close();

		}catch(Exception ex){
			logger.error("Exception while executing database sql", ex);
		}
	}

	public void insertOrUpdateRolesPriority(String roleName, int priority) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:"+ getDbFilePath())){
			Statement statement = conn.createStatement();

			String sqlInsert = String.format("INSERT INTO Roles (roleName, priority) " +
							"select '%s', %d " +
							"where not exists (select 1 from Roles where roleName = '%s');",
						roleName, priority, roleName
			);
			logger.debug(sqlInsert);
			statement.executeUpdate(sqlInsert);

			String sqlUpdate = String.format("update roles set priority = %d where roleName='%s';",
					priority, roleName
			);

			statement.executeUpdate(sqlUpdate);

			conn.close();

		}catch(Exception ex){
			logger.error("Exception while executing database sql", ex);
		}
	}
}
