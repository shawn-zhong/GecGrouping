package com.shawn.gec.dao;

import java.util.List;

import com.shawn.gec.po.Activity;

public interface IActivityDao {

	Activity InsertOrUpdateActivity(Activity act);

	List<Activity> GetActivityList();

}