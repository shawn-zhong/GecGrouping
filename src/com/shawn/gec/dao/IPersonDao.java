package com.shawn.gec.dao;

import java.sql.ResultSet;
import java.util.List;

import com.shawn.gec.po.MemGroupingItem;
import com.shawn.gec.po.Person;

public interface IPersonDao {

	Person InsertOrUpdatePerson(Person p);
	MemGroupingItem GetPersonById(int id);
	List<MemGroupingItem> GetPersonByKeyword(String keyword);
	List<MemGroupingItem> GetDuplicateRegistration();
	List<MemGroupingItem> GetWannaBeWithList();
	List<MemGroupingItem> GetGroupMembers(int groupId);
	List<MemGroupingItem> GetAllGroupMembers();
	List<MemGroupingItem> GetProblemeticItems();
}


/*
 * 
 * 
 * PRAGMA foreign_keys = 0;

PRAGMA foreign_keys = 0;

CREATE TABLE sqlitestudio_temp_table AS SELECT *
                                          FROM person;

DROP TABLE person;

CREATE TABLE person (
    id           INTEGER PRIMARY KEY AUTOINCREMENT
                         NOT NULL
                         UNIQUE,
    is_male      INTEGER NOT NULL,
    english_name TEXT    NOT NULL,
    chinese_name TEXT,
    hometown     TEXT,
    occupation   TEXT,
    mobile       TEXT    NOT NULL,
    qq           TEXT,
    wechat       TEXT    NOT NULL,
    district     TEXT,
    language     TEXT    DEFAULT English
                         NOT NULL,
    experience   INT     DEFAULT (1) 
                         NOT NULL,
    roles        TEXT,
    remark       TEXT,
    be_with      TEXT
);

INSERT INTO person (
                       id,
                       is_male,
                       english_name,
                       chinese_name,
                       hometown,
                       occupation,
                       mobile,
                       qq,
                       wechat,
                       district,
                       language,
                       experience,
                       roles,
                       remark,
                       be_with
                   )
                   SELECT id,
                          is_male,
                          english_name,
                          chinese_name,
                          hometown,
                          occupation,
                          mobile,
                          qq,
                          wechat,
                          district,
                          language,
                          experience,
                          roles,
                          remark,
                          be_with
                     FROM sqlitestudio_temp_table;

DROP TABLE sqlitestudio_temp_table;

PRAGMA foreign_keys = 1;



 * 
 * 
 * 
 * */