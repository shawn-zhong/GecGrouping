package com.shawn.gec.dao;

import java.util.List;

import com.shawn.gec.po.MemGroupingItem;
import com.shawn.gec.po.Person;

public interface IPersonDao {

	Person insertOrUpdatePerson(Person p);
	MemGroupingItem getPersonById(int id);
	List<MemGroupingItem> getPersonByKeyword(String keyword);
	List<MemGroupingItem> getDuplicateRegistration();
	List<MemGroupingItem> getWannaBeWithList();
	List<MemGroupingItem> getGroupMembers(int groupId);
	List<MemGroupingItem> getAllGroupMembersOrderByGroupNo();
	List<MemGroupingItem> getProblemeticItems();
	List<MemGroupingItem> getGroupLeaders();
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
    be_with      TEXT,
    search_text  TEXT
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
                       be_with,
                       search_text
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
                          be_with,
                          search_text
                     FROM sqlitestudio_temp_table;

DROP TABLE sqlitestudio_temp_table;






DROP TRIGGER trigger_person_after_update;

CREATE TRIGGER IF NOT EXISTS trigger_person_after_update
AFTER UPDATE
ON person
FOR EACH ROW
BEGIN
    UPDATE person SET search_text = 'id='||new.id||' '||'mobile='||new.mobile||' '||'name='||new.english_name||' '||'gender='||new.is_male||' '||
    'language='||new.language||' '||'be_with='||new.be_with||' '||'experience='||new.experience||' ' where id = new.id;
END;


 * 
 * 
 * 
 * */





/*



DROP TRIGGER trigger_person_after_update;

CREATE TRIGGER IF NOT EXISTS trigger_person_after_update
AFTER UPDATE
ON person
FOR EACH ROW
BEGIN
    UPDATE person SET search_text = 'id='||new.id||' '||'mobile='||new.mobile||' '||'name='||new.english_name||' '||'gender='||new.is_male||' '||
    'language='||new.language||' '||'be_with='||new.be_with||' ' where id = new.id;
END;




 */