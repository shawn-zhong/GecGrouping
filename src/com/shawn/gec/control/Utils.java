package com.shawn.gec.control;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.LanguageGrouping;
import com.shawn.gec.po.MemGroupingItem;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

import javax.swing.text.html.Option;

public class Utils {

	public static int explainExperience(String experience) {
		int exp = 1;
		if (experience.contains("＊"))
			exp =1;
		if (experience.contains("＊＊"))
			exp =2;
		if (experience.contains("＊＊＊"))
			exp =3;
		if (experience.contains("＊＊＊＊"))
			exp =4;
		if (experience.contains("＊＊＊＊＊"))
			exp =5;

		return exp;
	}

	public static String getGroupName(String lan, int grpId) {
		String name = String.format("%s-%03d", lan, grpId);
		return name;
	}
	
	public static String getGroupName(Grouping g) {
		String name = String.format("%s-%03d", g.getLanguage(), g.getGroup_id());
		return name;
	}
	
	public static String trimPhoneNumber(String _phoneno)
	{
		// trim and delete the alphabatic letters
		String cleanMobile = "";
		for (int i=0; i<_phoneno.length(); i++) {
			char a = _phoneno.charAt(i);
			if (a>='0'&& a<='9'){ // numeric
				cleanMobile += a;
			}
		}
		
		return cleanMobile;
	}
	
	public static int calculateHowManyPeopleThisPersonWith(int personId, Map<Integer, HashMap<Integer, MemGroupingItem>> circleGroups) {
		MemGroupingItem person = MemGrouping._staticGroupingSources.get(personId);
		int ancestorId = Utils.getAncestorId(person.grouping);
		
		if (circleGroups.containsKey(person)) {
			return circleGroups.get(personId).size();
		}
		
		if (circleGroups.containsKey(ancestorId)) {
			return circleGroups.get(ancestorId).size();
		}
		
		return 1;
	}
	
	public static boolean isRoleExisted(Map<Integer, MemGroupingItem> groups, String role){
		Iterator<Entry<Integer, MemGroupingItem>> iter = groups.entrySet().iterator();
		
		while (iter.hasNext()) {
			Entry<Integer, MemGroupingItem> item = iter.next();
			MemGroupingItem grouping = item.getValue();
			
			if (grouping.grouping.getGroupedRole() != null && grouping.grouping.getGroupedRole().hasRole(role)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean needFemaleTendency(Map<Integer, MemGroupingItem> grpPersons) {
		Iterator<Entry<Integer, MemGroupingItem>> iter = grpPersons.entrySet().iterator();
		
		int maleCounter=0;
		int femaleCounter=0;
		
		while (iter.hasNext()) {
			Map.Entry<Integer, MemGroupingItem> item = iter.next();
			MemGroupingItem grouping = item.getValue();
			
			if (grouping.person.getIs_male()==1)
				maleCounter+=1;
			else 
				femaleCounter+=1;
		}
		
		if (maleCounter > femaleCounter)
			return true;
		
		return false;
	}
	
	public static void transferTheGroupingItems(Map<Integer, MemGroupingItem> source, Map<Integer, MemGroupingItem> dest, Map<Integer, MemGroupingItem> scope, int groupId) {
		Iterator<Entry<Integer, MemGroupingItem>> removeIter = source.entrySet().iterator();
		
		while (removeIter.hasNext()) {
			Map.Entry<Integer, MemGroupingItem> removeEntry = removeIter.next();
			Integer personId = removeEntry.getKey();
			MemGroupingItem grouping = removeEntry.getValue();
			
			if (!dest.containsKey(grouping.person.getId())) {
				grouping.grouping.setGroup_id(groupId);
				dest.put(grouping.person.getId(), grouping);
				removeIter.remove();
				
				System.out.println(String.format("Add circle person(UID:%d, name:%s) into Group:%d", grouping.person.getId(), grouping.person.getEnglish_name(), groupId));;
			}
			
			if (scope != null) {
				scope.remove(grouping.person.getId());
			}
		}
		
	}
	
	/*
	void transferTheGroupingItems(Map<Integer, MemGroupingItem> source, Map<Integer, MemGroupingItem> dest) {
		Iterator<Entry<Integer, MemGroupingItem>> removeIter = source.entrySet().iterator();
		
		while (removeIter.hasNext()) {
			Map.Entry<Integer, MemGroupingItem> removeEntry = removeIter.next();
			Integer personId = removeEntry.getKey();
			MemGroupingItem grouping = removeEntry.getValue();
			
			dest.put(grouping.person.getId(), grouping);
		}
	}*/

	/*
	public static MemGroupingItem tryAnObjectFromMap(HashMap<Integer, MemGroupingItem> map) {
		Iterator<Entry<Integer, MemGroupingItem>> Iter = map.entrySet().iterator();
		
		while (Iter.hasNext()) {
			Map.Entry<Integer, MemGroupingItem> entry = Iter.next();
			MemGroupingItem grouping = entry.getValue();
			return grouping;
		}
		
		return null;
	}*/
	
	public static LanguageGrouping getRandonLanguageGroup(String language, Map<String, LanguageGrouping> _groupingResult) {
		
		List<LanguageGrouping> qualified = new LinkedList();
		
		Iterator<Entry<String, LanguageGrouping>> iter = _groupingResult.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, LanguageGrouping> entry = iter.next();
			LanguageGrouping resultGroupSet = entry.getValue();
			String matchLanguage = resultGroupSet.language;
			
			if (matchLanguage.compareTo(language)==0)
				qualified.add(resultGroupSet);
		}
		
		if (qualified.size() == 0)
			assert(false);
		
		if (qualified.size() == 1)
			return qualified.get(0);
		
		// ramdon
		Random random = new Random();
		int index = random.nextInt(qualified.size());
		return qualified.get(index);
	}
	
	
	public static MemGroupingItem lookForCompetentPerson(String language, Map<Integer, MemGroupingItem> scope, int preferGender) {
		Iterator<Entry<Integer, MemGroupingItem>> iter = scope.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, MemGroupingItem> entty = iter.next();
			Integer personId = entty.getKey();
			MemGroupingItem grpItem = entty.getValue();
			
			if (grpItem.grouping.getLanguage().compareTo(language) == 0
					&& (preferGender == -1 || grpItem.person.getIs_male() == preferGender)) {
				return grpItem;
			}
		}
		
		// didnt find one, try to loose the gender limit
		if (preferGender != -1)
			return lookForCompetentPerson(language, scope, -1);
		
		return null;
	}

	public static MemGroupingItem lookForCompetentPerson(
			String language,
			List<Integer> experiencePriorities,
			List<String> acceptableRoles,
			Map<Integer, MemGroupingItem> seachingScope,
			Integer preferGender,
			List<Integer> excludedIds,
			boolean toTryBest
			) {

		for (Integer _experience : experiencePriorities) {

			for (Entry<Integer, MemGroupingItem> entry : seachingScope.entrySet()) {
				MemGroupingItem item = entry.getValue();

				if (item.grouping.getGroupedRole() != null                                        // already assigned a group
						|| (excludedIds != null && excludedIds.contains(item.person.getId()))    // excluded
						|| (item.grouping.getLanguage().compareToIgnoreCase(language) != 0)        // language doesn't match
						|| (_experience != item.person.getExperience())                            // experience doesn't match
						|| (preferGender != -1 && item.person.getIs_male() != preferGender)        // gender doesn't match
						) {
					continue;
				}

				boolean roleMatched = (acceptableRoles == null);
				if (acceptableRoles != null) {
					for (String r : acceptableRoles)
						if (item.grouping.getRole().hasRole(r)) {
							roleMatched = true;
							break;
						}
				}

				if (!roleMatched) {    // role doesn't match
					continue;
				}


				// all matched, return
				return item;
			}
		}

		if (!toTryBest)
			return null;

		MemGroupingItem retItem = null;

		// didnt find anyone, loose the gender limit:
		if (preferGender != -1) {
			retItem = lookForCompetentPerson(language, experiencePriorities, acceptableRoles, seachingScope, -1, excludedIds, false);
		}

		// still didnt find anyone, loose the role limit:
		if (retItem == null) {
			retItem = lookForCompetentPerson(language, experiencePriorities, null, seachingScope, preferGender, excludedIds, false);
		}

		// still didnt find anyone, loose the gender and role limitation
		if (retItem == null) {
			retItem = lookForCompetentPerson(language, experiencePriorities, null, seachingScope, -1, excludedIds, false);
		}

		if (retItem == null) {
			retItem = lookForCompetentPerson(language, experiencePriorities, null, seachingScope, -1, null, false);
		}

		if (retItem == null) {
			retItem = lookForCompetentPerson(language, Arrays.asList(5, 4, 3, 2, 1), null, seachingScope, -1, null, false);
		}

		return retItem;
	}

	// experiencePriority:-1:doesnt care, 0: bottom-up, 1:top-down, preferGender:-1 dont care
	/*
	public static MemGroupingItem lookForCompetentPerson(
			String language, 
			boolean prioritySensitive, 
			int[] experiencePriorities, 
			String needed_role,
			boolean roleMustMatch, 
			Map<Integer, MemGroupingItem> scope, 
			int preferGender, 
			boolean toTryBest,
			List<Integer> excludedIds){
		
		for (int i=0; i<experiencePriorities.length; i++) {
			Integer experience = experiencePriorities[i];
			
			Iterator<Entry<Integer, MemGroupingItem>> iter = scope.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Integer, MemGroupingItem> entty = iter.next();
				Integer personId = entty.getKey();
				MemGroupingItem grpItem = entty.getValue();
				
				if (grpItem.grouping.getGroupedRole() != null)
					continue;	// has been assigned a role, skip
				
				if (excludedIds!= null && excludedIds.contains(grpItem.person.getId()))
						continue;
				
				if (grpItem.grouping.getLanguage().compareTo(language)==0 
						&& (roleMustMatch ? grpItem.grouping.getRole().hasRole(needed_role) : true)
						&& (prioritySensitive ? grpItem.person.getExperience() == experience : true)
						&& (preferGender==-1? true : grpItem.person.getIs_male()==preferGender)) {
					return grpItem;
				}
			}
			
		}
		
		if (!toTryBest)
			return null;
		
		MemGroupingItem retItem = null;
		
		// didnt find anyone, loose the gender limit: 
		if (preferGender != -1)
			retItem = lookForCompetentPerson(language, prioritySensitive, experiencePriorities, needed_role, true, scope, -1, false, excludedIds);
		
		// still didnt find anyone, loose the role limit:
		if (retItem == null)
			retItem = lookForCompetentPerson(language, prioritySensitive, experiencePriorities, needed_role, false, scope, -1, false, excludedIds);
		
		return retItem;
	}*/
	
	public static int getAncestorId(Grouping grp){
		return getAncestorGrouping(grp).getPerson_id();
	}
	
	public static Grouping getAncestorGrouping(Grouping grp) {
		// put those guys who have same parent in same group
		HashSet<Integer> walkedIdSet = new HashSet<Integer>();
		Grouping ancestor = getAncestorGrouping(grp, walkedIdSet);
		walkedIdSet.clear();
		return ancestor;
	}
	
	// get the ancestor person id. return self personId when no ancestor
	// circle link prevented
	public static Grouping getAncestorGrouping(Grouping grp, HashSet<Integer> walkedId){
		
		// limit the depth and break the link
		if (walkedId.size() > ControlPanel.act.getGroup_capacity()*0.4 || walkedId.size() > 5) {
			grp.setBe_with_uid(0);
			return grp;
		}
		
		int parentId = grp.getBe_with_uid();
		
		// When the parent is one of his children, cut short
		if (walkedId.contains(parentId)) {
			grp.setBe_with_uid(0);
			return grp;	
		}
		
		// no parent
		if (parentId <= 0)
			return grp;
		
		walkedId.add(grp.getPerson_id());
		
		MemGroupingItem item = MemGrouping._staticGroupingSources.get(parentId);
		
		
		if (item == null) {
			assert(false);
		}
		
		return getAncestorGrouping(item.grouping, walkedId);
	}
	
}
