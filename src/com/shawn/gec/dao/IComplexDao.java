package com.shawn.gec.dao;

import com.shawn.gec.po.GroupingStat;

import java.sql.SQLException;
import java.util.List;

public interface IComplexDao {

    void testDBConnection() throws SQLException;
    void cleanDatabase();
    int getMaxPersonId();
    void deletePersonAndGrouping(int uid);
    List<GroupingStat> getGroupingStatistics();
    List<String> getAllRegesterLanguages();
    void updateGroupName(String language, int groupId);
    void insertOrUpdateRolesPriority(String roleName, int priority);
}
