package com.shawn.gec.po;

import com.shawn.gec.control.SettingCenter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Role {
	
	public static String ROLE_TEAMLEADER_DEF = "组长";
	public static String ROLE_VICETEAMLEADER_DEF = "副组长";

	public String RoleNames;

    public Role(String singleRoleName) {
        RoleNames = singleRoleName;
    }

    public Role(List<String> roleNames) {

        StringBuilder builder = new StringBuilder();
        for (String roleName : roleNames) {
            builder.append(" ");
            builder.append(roleName);
        }

        RoleNames = builder.toString().trim();
    }

    public static Role explainFromText(String text) {
        text = text.replace("┋", " ");
        String[] dbRoleStrs = text.split(" ");

        List<String> allRoles = SettingCenter.getRoleNameList();
        List<String> hasRoles = new LinkedList<>();
        for (String stdS : allRoles) {
            if (Arrays.stream(dbRoleStrs).filter(s->s.startsWith(stdS)).count() >=1 ) {
                hasRoles.add(stdS);
            }
        };

        return new Role(hasRoles);
    }

    public boolean hasRole(String roleName) {
        String[] roles = RoleNames.split(" ");
        if (Arrays.stream(roles).filter(s->s.compareToIgnoreCase(roleName)==0).count()>= 1)
            return  true;

        return false;
    }

    @Override
    public String toString() {
        return String.format("Role {%s}", RoleNames);
    }
}
