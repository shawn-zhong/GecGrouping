package com.shawn.gec.dao;

import java.util.List;

import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.MemGroupingItem;

public interface IGroupingDao {

	Grouping InsertOrUpdate(Grouping grp);
	Grouping GetGroupingByPersonId(int personId);
	List<Grouping> GetGroupings();
	void LockGroup(int grpID, boolean needLock);

}


/*
 * 
 * 
PRAGMA foreign_keys = 0;

CREATE TABLE sqlitestudio_temp_table AS SELECT *
                                          FROM grouping;

DROP TABLE grouping;

CREATE TABLE grouping (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    g_uid       INTEGER NOT NULL,
    g_roledesc  TEXT,
    g_bewith    INTEGER,
    g_gid       INTEGER NOT NULL,
    g_language  TEXT    NOT NULL,
    g_groupname TEXT,
    g_remark    TEXT,
    g_fixed     INTEGER DEFAULT (0)
                        NOT NULL
);

INSERT INTO grouping (
                         id,
                         g_uid,
                         g_roledesc,
                         g_bewith,
                         g_gid,
                         g_language,
                         g_groupname,
                         g_remark,
                         g_fixed
                     )
                     SELECT id,
                            g_uid,
                            g_roledesc,
                            g_bewith,
                            g_gid,
                            g_language,
                            g_groupname,
                            g_remark,
                            g_fixed
                       FROM sqlitestudio_temp_table;

DROP TABLE sqlitestudio_temp_table;

PRAGMA foreign_keys = 1;


 * 
 */