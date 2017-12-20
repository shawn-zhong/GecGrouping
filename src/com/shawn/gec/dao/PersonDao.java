package com.shawn.gec.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import com.shawn.gec.control.SettingCenter;
import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.MemGroupingItem;
import com.shawn.gec.po.Person;
import com.shawn.gec.po.Role;

public class PersonDao implements IPersonDao {
	
	{
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see com.shawn.gec.dao.IPersonDao#InsertOrUpdatePerson(com.shawn.gec.po.Person)
	 */
	@Override
	public Person InsertOrUpdatePerson(Person p)
	{
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlInsert = String.format("INSERT INTO person (id,is_male,english_name,chinese_name,hometown,occupation,mobile," + 
                    "qq,wechat,district,language,experience,roles,remark,be_with) " +
					"select %d, %d, '%s', '%s', '%s',  '%s', '%s', '%s', '%s', '%s',  '%s', %d, '%s', '%s', '%s' " +
                    "where not exists (select 1 from person where id = %d);",
                    p.getId(), p.getIs_male(), p.getEnglish_name(), p.getChinese_name(), p.getHometown(),
                    p.getOccupation(), p.getMobile(), p.getQq(), p.getWechat(), p.getDistrict(), 
                    p.getLanguage(), p.getExperience(), p.getRoles(), p.getRemark(), p.getBe_with(),
                    p.getId()
                    );
                    
			/*
			String sqlInsert = String.format("insert into person(is_male, english_name, chinese_name, first_time_english_name, hometown, occupation, mobile_number, qq, wechat, district) " + 
											"select %d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s' " +
											"where not exists (select 1 from person where mobile_number = '%s');", 
											p.getIs_male(), p.getEnglish_name(), p.getChinese_name(), p.getEnglish_name(), p.getHometown(), p.getOccupation(), p.getMobile(),
											p.getQq(), p.getWechat(), p.getDistrict(),
											p.getMobile()
										);*/
			
			//System.out.println(sqlInsert);
			statement.executeUpdate(sqlInsert);
			
			String sqlUpdate = String.format("update person set is_male = %d, english_name='%s', chinese_name='%s', hometown='%s', occupation='%s', mobile='%s', " +
			"qq='%s', wechat='%s', district = '%s', language='%s', experience=%d,  roles='%s', remark='%s', be_with='%s'  where id=%d;", 
											p.getIs_male(), p.getEnglish_name(), p.getChinese_name(), p.getHometown(), p.getOccupation(), p.getMobile(),
											p.getQq(), p.getWechat(), p.getDistrict(), p.getLanguage(), p.getExperience(), p.getRoles(), p.getRemark(), p.getBe_with(),
											p.getId()
											);
			
			statement.executeUpdate(sqlUpdate);
			
	        // Close the connection
	        conn.close();
	        
	        return GetPersonById(p.getId()).person;
	        
		}catch(Exception ex){
			ex.printStackTrace();
			//System.out.println(ex.getMessage());
		}

		return  new Person();
	}
	
	public List<MemGroupingItem> GetPersonByKeyword(String keyword) {
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select * from person left join grouping on person.id = grouping.g_uid where english_name like '%%%s%%' or chinese_name like '%%%s%%' or occupation like '%%%s%%' or mobile like '%%%s%%' " +
                        " or qq like '%%%s%%' or wechat like '%%%s%%' or language like '%%%s%%' or roles like '%%%s%%' or be_with like '%%%s%%' " +
                        " or g_roledesc like '%%%s%%' or g_groupname like '%%%s%%' ",
                        keyword,keyword,keyword,keyword,keyword,keyword,keyword,keyword,keyword, keyword, keyword);
			
			ResultSet rs = statement.executeQuery(sqlSelect);
			List<MemGroupingItem> items = readResultSet(rs);

	        conn.close();
	        
	        //System.out.print(p.toString());

	        return items;
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public MemGroupingItem GetPersonById(int id){
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select * from person left join grouping on person.id = grouping.g_uid where person.id = %d;", id);
			ResultSet rs = statement.executeQuery(sqlSelect);
			
			List<MemGroupingItem> items = readResultSet(rs);

	        conn.close();
	        
	        return items.size()==0? null: items.get(0);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public List<MemGroupingItem> GetWannaBeWithList(){
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlSelect = String.format("select * from person left join grouping on person.id = grouping.g_uid where be_with <> '';");
			ResultSet rs = statement.executeQuery(sqlSelect);
			
			List<MemGroupingItem> items = readResultSet(rs);

	        conn.close();
	        
	        return items;
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
	
	public List<MemGroupingItem> GetDuplicateRegistration() {
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();
			
			String sqlSelect = "select * from person left join grouping on person.id = grouping.g_uid where person.mobile in ( " +
								"select mobile from person group by mobile having count(*) > 1 " +
								") " +
								"union " +
								"select * from person left join grouping on person.id = grouping.g_uid where person.wechat in ( " +
								"select wechat from person group by wechat having count(*) > 1 " +
								") order by mobile; ";
			
			ResultSet rs = statement.executeQuery(sqlSelect);
			List<MemGroupingItem> items = readResultSet(rs);

	        conn.close();
	        
	        //System.out.print(p.toString());

	        return items;
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}

	public List<MemGroupingItem> GetAllGroupMembers() {
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.instance.getDbFilePath());
			Statement statement = conn.createStatement();

			String sqlSelect = "select Tmp.g_uid as id, * from (\n" +
					"select * from grouping left join roles on grouping.g_roledesc=roles.roleName\n" +
					") as Tmp left join person on Tmp.g_uid=person.id\n" +
					"order by Tmp.g_gid asc, Tmp.priority desc;";

			ResultSet rs = statement.executeQuery(sqlSelect);
			List<MemGroupingItem> items = readResultSet(rs);

			conn.close();

			return items;

		}catch(Exception ex){
			ex.printStackTrace();
		}

		return null;
	}

	public List<MemGroupingItem> GetGroupMembers(int groupId) {
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.getDbFilePath());
			Statement statement = conn.createStatement();

			String sqlSelect = String.format("select Tmp.g_uid as id, * from (\n" +
					"select * from grouping left join roles on grouping.g_roledesc=roles.roleName where grouping.g_gid = %d\n" +
					") as Tmp left join person on Tmp.g_uid=person.id\n" +
					"order by Tmp.g_gid asc, Tmp.priority desc;", groupId);
			
			ResultSet rs = statement.executeQuery(sqlSelect);
			List<MemGroupingItem> items = readResultSet(rs);

	        conn.close();
	       
	        return items;
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}

	public List<MemGroupingItem> GetProblemeticItems() {
		try{
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+SettingCenter.getDbFilePath());
			Statement statement = conn.createStatement();

			String sqlSelect = "select * from person left join grouping on person.id = grouping.g_uid where length(grouping.g_remark) >0; ";

			ResultSet rs = statement.executeQuery(sqlSelect);
			List<MemGroupingItem> items = readResultSet(rs);

			conn.close();

			return items;

		}catch(Exception ex){
			ex.printStackTrace();
		}

		return null;

	}

	
	private List<MemGroupingItem> readResultSet(ResultSet rs) {
		List<MemGroupingItem> items = new LinkedList<MemGroupingItem>();

		try {
		while (rs.next()) {
			Person p = new Person();
			Grouping g = new Grouping();
			
			//id, is_male,english_name,chinese_name,hometown,occupation,mobile
            //qq,wechat,district,language,experience,roles,remark,be_with
            p.setId(rs.getInt("id"));   // Column 1
            p.setIs_male(rs.getInt("is_male"));
            p.setEnglish_name(rs.getString("english_name"));
            p.setChinese_name(rs.getString("chinese_name"));
            p.setHometown(rs.getString("hometown"));
            p.setOccupation(rs.getString("occupation"));
            p.setMobile(rs.getString("mobile"));
            p.setQq(rs.getString("qq"));
            p.setWechat(rs.getString("wechat"));
            p.setDistrict(rs.getString("district"));
            p.setLanguage(rs.getString("language"));
            p.setExperience(rs.getInt("experience"));
            p.setRoles(rs.getString("roles"));
            p.setRemark(rs.getString("remark"));
            p.setBe_with(rs.getString("be_with"));
            
            Role r = Role.explainFromText(rs.getString("roles"));
            
            int gid = rs.getInt("g_gid");
            if (gid  >0) {
	            g.setBe_with_uid(rs.getInt("g_bewith"));
	            g.setGroup_id(rs.getInt("g_gid"));
	            
	            String roles = rs.getString("g_roledesc");
	            if (roles != null)
	            	g.setGroupedRole(new Role(roles));
	            
	            g.setRole(r);
	            
	            g.setLanguage(rs.getString("g_language"));
	            g.setPerson_id(rs.getInt("g_uid"));
	            g.setRemark(rs.getString("g_remark"));
	            g.setIsFixed(rs.getInt("g_fixed"));
            } else {
            	g.setBe_with_uid(-1);
	            g.setGroup_id(-1);
	            g.setLanguage(rs.getString("g_language"));
	            g.setPerson_id(rs.getInt("g_uid"));
	            g.setRemark(rs.getString("g_remark"));
	            g.setRole(r);
	            g.setGroupedRole(null);
            }
            
	        MemGroupingItem item = new MemGroupingItem(p, g);
	        items.add(item);
        }
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return items;
	}
}
