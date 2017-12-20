package com.shawn.gec.control;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingCenter {

    public static int COL_SIGNUP_ID = 0;
    public static int COL_SIGNUP_DATE = 1;
    public static int COL_ENG_NAME = 6;
    public static int COL_CHN_NAME = 7;
    public static int COL_GENDER = 8;
    public static int COL_HOMETOWN = 9;
    public static int COL_OCCUPATION = 10;
    public static int COL_LANGUAGE = 11;
    public static int COL_ROLE = 12;
    public static int COL_EXPERIENCE = 13;
    public static int COL_WANNA_BE_WITH = 15;
    public static int COL_MOBILE_NUMBER = 16;
    public static int COL_QQ= 17;
    public static int COL_WECHAT = 18;
    public static int COL_DISTRICT = 26;

    private static String exlFilePath;
    private static String exlDeltaFilePath;
	private static String dbFilePath;
    private static String exlOutputPath;
	private static int groupCapacity;
	private static List<String> roleNameList;

	public static final SettingCenter instance = new SettingCenter();
	
	public static void fakeSettings() {
	    /*
		dbFilePath = "/Users/Shawn/Documents/dev/GEC_Grouping/gec.db";
		exlFilePath = "/Users/Shawn/Documents/dev/GEC_Grouping/newsample3.csv";
		exlDeltaFilePath = "/Users/Shawn/Documents/dev/GEC_Grouping/newsample2_delta.csv";
		groupCapacity = 15;*/


	}

	public static void ReadFromSettingFile() {

        SAXParserFactory factory = SAXParserFactory.newInstance();

        try {
            SAXParser parser = factory.newSAXParser();

            File f = new File("setting.xml");
            if (!f.exists()) {
                f = new File("/Users/Shawn/Documents/workspace/GecGrouping/resource/setting.xml");
            }

            if (f.exists()) {
                parser.parse(f, new SettingXmlHandler());
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void Clear() {
	    roleNameList.clear();
    }

	private SettingCenter() {
        roleNameList = new ArrayList<>(10);
	}

    public static String getExlFilePath() {
        return exlFilePath;
    }

    public static void setExlFilePath(String exlFilePath) {
        SettingCenter.exlFilePath = exlFilePath;
    }

    public static String getExlDeltaFilePath() {
        return exlDeltaFilePath;
    }

    public static void setExlDeltaFilePath(String exlDeltaFilePath) {
        SettingCenter.exlDeltaFilePath = exlDeltaFilePath;
    }

	public static String getDbFilePath() {
		return dbFilePath;
	}
	public static void setDbFilePath(String _dbFilePath) {
        dbFilePath = _dbFilePath;
	}

	public static int getGroupCapacity() {
		return groupCapacity;
	}

	public static void setGroupCapacity(int groupCapacity) {
		SettingCenter.groupCapacity = groupCapacity;
	}

	public static List<String> getRoleNameList() {
        return roleNameList;
	}

	public static void addToRoleNameList(String roleName) {
	    if (roleNameList == null) {
	        roleNameList = new ArrayList<>();
        }

        roleNameList.add(roleName);
    }

    public static String getExlOutputPath() {
        return exlOutputPath;
    }

    public static void setExlOutputPath(String exlOutputPath) {
        SettingCenter.exlOutputPath = exlOutputPath;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("SettingCenter : exlFilePath:%s, \r\nexlDeltaFilePath:%s, \r\ndbFilePath:%s, \r\nexportFilePath:%s, \r\ngroupCapacity:%d",
                exlFilePath, exlDeltaFilePath, dbFilePath, exlOutputPath, groupCapacity));

        builder.append("\r\nRoles: ");
        for (String role : roleNameList) {
            builder.append(role);
            builder.append(" ");
        }

        builder.append("\r\nColumns: ");

        builder.append(String.format("\nCOL_SIGNUP_ID - %d ", COL_SIGNUP_ID));
        builder.append(String.format("\nCOL_SIGNUP_DATE - %d ", COL_SIGNUP_DATE));
        builder.append(String.format("\nCOL_ENG_NAME - %d ", COL_ENG_NAME));
        builder.append(String.format("\nCOL_CHN_NAME - %d ", COL_CHN_NAME));
        builder.append(String.format("\nCOL_GENDER - %d ", COL_GENDER));
        builder.append(String.format("\nCOL_HOMETOWN - %d ", COL_HOMETOWN));
        builder.append(String.format("\nCOL_OCCUPATION - %d ", COL_OCCUPATION));
        builder.append(String.format("\nCOL_LANGUAGE - %d ", COL_LANGUAGE));
        builder.append(String.format("\nCOL_ROLE - %d ", COL_ROLE));
        builder.append(String.format("\nCOL_EXPERIENCE - %d ", COL_EXPERIENCE));
        builder.append(String.format("\nCOL_WANNA_BE_WITH - %d ", COL_WANNA_BE_WITH));
        builder.append(String.format("\nCOL_MOBILE_NUMBER - %d ", COL_MOBILE_NUMBER));

        builder.append(String.format("\nCOL_QQ - %d ", COL_QQ));
        builder.append(String.format("\nCOL_WECHAT - %d ", COL_WECHAT));
        builder.append(String.format("\nCOL_DISTRICT - %d ", COL_DISTRICT));

        return builder.toString();
    }
}
