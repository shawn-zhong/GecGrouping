package com.shawn.gec.control;

import java.util.*;
import java.util.Map.Entry;

import com.shawn.gec.po.Grouping;
import com.shawn.gec.po.LanguageGrouping;
import com.shawn.gec.po.MemGroupingItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Utils {

	private static Logger logger = LoggerFactory.getLogger(Utils.class);

	public static int explainExperienceStarts(String experience) {
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
		return String.format("%s-%03d", lan, grpId);
	}
	
	public static String regulateMobileNumber(String _phoneno)
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
		
		if (circleGroups.containsKey(personId)) {
			return circleGroups.get(personId).size();
		}
		
		if (circleGroups.containsKey(ancestorId)) {
			return circleGroups.get(ancestorId).size();
		}
		
		return 1;
	}
	
	public static boolean isRoleExisted(Map<Integer, MemGroupingItem> groups, String role){

		for (Entry<Integer, MemGroupingItem> item : groups.entrySet()) {
			MemGroupingItem grouping = item.getValue();

			if (grouping.grouping.getGroupedRole() != null && grouping.grouping.getGroupedRole().hasRole(role)) {
				return true;
			}
		}
		
		return false;
	}

	public static boolean needFemaleTendency(LanguageGrouping groupingInfo) {
		float idealFemale = groupingInfo.idealFemaleCapacity;
		float idealTotal = groupingInfo.idealCapacity;

		long realFemale = groupingInfo.groupingItems.values().stream().filter(g -> g.person.getIs_male() != 1).count();
		long realTotal = groupingInfo.groupingItems.size();

		boolean retVal = false;

		float idealRate = idealFemale/idealTotal;
		float currentRate = realFemale/(float)realTotal;

		retVal = currentRate < idealRate;

		logger.info("{} ideal people:{}, ideal male:{} ideal female:{} rate:{};  current people:{}, current male:{} current female:{} rate:{},  tendency:{}",
				Utils.getGroupName(groupingInfo.language, groupingInfo.groupId),
				groupingInfo.idealCapacity, groupingInfo.idealCapacity-groupingInfo.idealFemaleCapacity, groupingInfo.idealFemaleCapacity,idealRate,
				realTotal, realTotal-realFemale, realFemale, currentRate,
				retVal?"Need Female":"Need Male");

		return retVal;
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

				logger.info("Add circle person(UID:{}, name:{}) into Group:{}", grouping.person.getId(), grouping.person.getEnglish_name(), groupId);
			}
			
			if (scope != null) {
				scope.remove(grouping.person.getId());
			}
		}
	}

	public static LanguageGrouping getTheLanguageGroupOfLeastPerson(String language, Map<String, LanguageGrouping> _groupingResult) {
		List<LanguageGrouping> qualified = new LinkedList<>();
		LanguageGrouping ret = null;

		for (Entry<String, LanguageGrouping> entry : _groupingResult.entrySet()) {
			LanguageGrouping resultGroupSet = entry.getValue();
			String matchLanguage = resultGroupSet.language;

			if (matchLanguage.compareTo(language) == 0) {
				qualified.add(resultGroupSet);

				if (ret == null)
					ret = resultGroupSet;
				else if (ret.groupingItems.size() > resultGroupSet.groupingItems.size())
					ret = resultGroupSet;
			}
		}

		assert qualified.size() != 0;

		if (qualified.size() == 1)
			return qualified.get(0);

		return ret;

	}

	/*
	public static LanguageGrouping getRandonLanguageGroup(String language, Map<String, LanguageGrouping> _groupingResult) {
		
		List<LanguageGrouping> qualified = new LinkedList<>();

		for (Entry<String, LanguageGrouping> entry : _groupingResult.entrySet()) {
			LanguageGrouping resultGroupSet = entry.getValue();
			String matchLanguage = resultGroupSet.language;

			if (matchLanguage.compareTo(language) == 0)
				qualified.add(resultGroupSet);
		}
		
		if (qualified.size() == 0)
			assert(false);
		
		if (qualified.size() == 1)
			return qualified.get(0);

		//return qualified.stream().min((o1, o2) -> o1.groupingItems.size()<o2.groupingItems.size()?o1.groupingItems.size():o2.groupingItems.size()).get();

		// ramdon
		Random random = new Random();
		int index = random.nextInt(qualified.size());
		return qualified.get(index);
	}*/
	
	
	public static MemGroupingItem lookForCompetentPerson(String language, Map<Integer, MemGroupingItem> scope, int preferGender) {
		for (Entry<Integer, MemGroupingItem> entty : scope.entrySet()) {
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

				boolean roleMatched = (acceptableRoles == null) || acceptableRoles.size()==0;
				if (acceptableRoles != null && acceptableRoles.size() >0) {
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
		if (walkedId.size() > 6) {	// limit the depth
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

		assert item != null;
		
		return getAncestorGrouping(item.grouping, walkedId);
	}
	
}
