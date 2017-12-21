package com.shawn.gec.control;

import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.shawn.gec.po.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MemGrouping {

	private Logger logger = LoggerFactory.getLogger(MemGrouping.class);

	// this map stores all the grouping items and keep still for search
	public static Map<Integer, MemGroupingItem> _staticGroupingSources = new ConcurrentHashMap<Integer, MemGroupingItem>();
	
	// this map stores all the grouping items but for dynamics process
	private static Map<Integer, MemGroupingItem> _dynamicGroupingSources = new ConcurrentHashMap<Integer, MemGroupingItem>();
	
	// this is for looking up user-id by phone number
	private static Map<String, Integer> _phoneNumberMap = new ConcurrentHashMap<String, Integer>();
	
	// person and wanna-be-with person would be in a same circle. this map is dynamic also
	private static Map<Integer, HashMap<Integer, MemGroupingItem>> _dynamicCircleGroups = new ConcurrentHashMap<Integer, HashMap<Integer, MemGroupingItem>>();
	
	// here to generate and store the grouping results
	private static Map<String, LanguageGrouping> _groupingResult = new ConcurrentHashMap<String, LanguageGrouping>();
	 
	
	public static final MemGrouping Instance = new MemGrouping();
	//public static Activity _activity;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("MemGrouping {size:%d}", _dynamicGroupingSources.size());
	}
	
	private MemGrouping() {}

	/*
	 * Empty all the repository
	 */
	public void emptyAll() {
		_staticGroupingSources.clear();
		_dynamicGroupingSources.clear();
		_phoneNumberMap.clear();
		_dynamicCircleGroups.clear();
		_groupingResult.clear();
	}
	
	/*
	 * Initiate the grouping data
	 */
	public void addGrouping(Person p, Grouping g){
		MemGroupingItem item = new MemGroupingItem(p, g);
		_staticGroupingSources.put(p.getId(), item);
		_dynamicGroupingSources.put(p.getId(), item);
		_phoneNumberMap.put(p.getMobile(), p.getId());
	}
	
	/*
	 * look for wanna-be-with UID by phone number
	 */
	public void refreshWannaWithWho(){

		Iterator<Entry<Integer, MemGroupingItem>> iter = _dynamicGroupingSources.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, MemGroupingItem> entry = iter.next();
			Integer personId = entry.getKey();
			MemGroupingItem grpItem = entry.getValue();
			String wannaBeWith = grpItem.person.getBe_with();
			
			if (wannaBeWith == null || wannaBeWith.isEmpty())
				continue;
			
			// trim and remove the non-numeric charaters ***
			wannaBeWith = wannaBeWith.trim();			
			
			// look for the phone number want to be with
			if (_phoneNumberMap.get(wannaBeWith) != null){
				grpItem.grouping.setBe_with_uid(_phoneNumberMap.get(wannaBeWith));

				logger.info("Match succeeded. (UID:{} name:{}) wants to be with (UID:{} name:{})",
						grpItem.person.getId(), grpItem.person.getEnglish_name(), _phoneNumberMap.get(wannaBeWith),
						_dynamicGroupingSources.get(_phoneNumberMap.get(wannaBeWith)).person.getEnglish_name());

				continue;
			}
			
			// correct the stupid guys want to be with himself
			if (grpItem.grouping.getBe_with_uid() == grpItem.person.getId()){
				grpItem.grouping.setBe_with_uid(0);

				logger.info("Match ILLEGAL (UID:{}, name:{}) wants to be him/herself, corrected",
						grpItem.person.getId(), grpItem.person.getEnglish_name());

				continue;
			}
			
			// didnt find the guy he/she want to be with
			grpItem.grouping.setBe_with_uid(0);
			String remark = grpItem.grouping.getRemark();
			if (!remark.contains("找不到想一起的TA"))
				remark += "找不到想一起的TA;";
			grpItem.grouping.setRemark(remark);

			logger.info("Match FAILED;  (UID:{}, name:{}) wants to be with {}", grpItem.person.getId(), grpItem.person.getEnglish_name(), grpItem.person.getBe_with());
		}
	}
	
	/*
	 * to correct the data if he/she and the wanna-with guy not in same group
	 */
	public void correctLanguageData(){
		// rule is, to follow the ancestor's language
		Iterator<Entry<Integer, MemGroupingItem>> iter = _dynamicGroupingSources.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, MemGroupingItem> entry = iter.next();
			Integer personId = entry.getKey();
			MemGroupingItem grpItem = entry.getValue();

			// auto grouping would force to align with the sign-up language if it is not fixed
			if (grpItem.person.getLanguage().compareToIgnoreCase(grpItem.grouping.getLanguage()) != 0) {
				grpItem.grouping.setLanguage(grpItem.person.getLanguage());
			}

			if (grpItem.grouping.getBe_with_uid() > 0) {
				Grouping ancestor = Utils.getAncestorGrouping(grpItem.grouping);
				if (grpItem.grouping.getLanguage().compareTo(ancestor.getLanguage()) != 0) {
					String remark = grpItem.grouping.getRemark();
					String errMsg = String.format("与想一起的TA报名语言不同，调整为%s;", ancestor.getLanguage());
					if (!remark.contains(errMsg))
						remark += errMsg;
					grpItem.grouping.setRemark(remark);
					grpItem.grouping.setLanguage(ancestor.getLanguage());

					logger.info("Language changed : (UID:{}, name:{}) to follow ancestor's language, changed from {} to {}",
							grpItem.person.getId(), grpItem.person.getEnglish_name(), grpItem.person.getLanguage(), ancestor.getLanguage());
				}
			}
		}
		
	}
	
	/*
	 * Collect those people have same ancestor into same Map
	 */
	public void divideToCircleGroupsByWannaBeWith(){
		
		_dynamicCircleGroups.clear();
		
		// find ancestor and put into map
		Iterator<Entry<Integer, MemGroupingItem>> iter = _dynamicGroupingSources.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, MemGroupingItem> entry = iter.next();
			Integer personId = entry.getKey();
			MemGroupingItem grpItem = entry.getValue();
			
			int ancestorId = Utils.getAncestorId(grpItem.grouping);
			
			if (_dynamicCircleGroups.get(ancestorId) == null) {
				// put parent and self in a same circle
				HashMap<Integer, MemGroupingItem> ancestorAndChildren = new HashMap<Integer, MemGroupingItem>();
				ancestorAndChildren.put(ancestorId, _dynamicGroupingSources.get(ancestorId));
				ancestorAndChildren.put(grpItem.person.getId(), grpItem);
				
				_dynamicCircleGroups.put(ancestorId, ancestorAndChildren);
			} else {
				// existed, add the child (self) directly
				_dynamicCircleGroups.get(ancestorId).put(grpItem.person.getId(), grpItem);
			}
		}
		
		// delete those single ones (self-depedent only)
		Iterator<Entry<Integer, HashMap<Integer, MemGroupingItem>>> removeIter = _dynamicCircleGroups.entrySet().iterator();
		while (removeIter.hasNext()) {
			Map.Entry<Integer, HashMap<Integer, MemGroupingItem>> removeEntry = removeIter.next();
			Integer ancestorId = removeEntry.getKey();
			HashMap<Integer, MemGroupingItem> children = removeEntry.getValue();
			
			if (children.size() <= 1) {
				removeIter.remove();
			} else {
				String childrenStrs = children.values().stream().filter(p->p.person.getId()!=ancestorId).map(p->String.format("{UID:%d, name:%s},", p.person.getId(), p.person.getEnglish_name())).reduce((a,b)->a+b).orElse("");
				logger.info("Circle done: ancestor {UID:{}, name:{}} has children : {}", ancestorId, children.get(ancestorId).person.getEnglish_name(), childrenStrs);
			}
		}
	}

	/*
	 * Sum up the people of each language and calc the number and capacity of groups
	 */
	public void calcHowManyGroupsAndIdealCapacity(Set<Integer> excludedIDs) {
		
		// calculate how many people for each language
		ConcurrentHashMap<String, Integer> _mapLanguageAndPeopleCount = new ConcurrentHashMap<String, Integer>();
		
		Iterator<Entry<Integer, MemGroupingItem>> iterCnt = _dynamicGroupingSources.entrySet().iterator();
		while (iterCnt.hasNext()) {
			Entry<Integer, MemGroupingItem> entryCnt = iterCnt.next();
			Integer personId = entryCnt.getKey();
			MemGroupingItem grpItem = entryCnt.getValue();
			
			if (_mapLanguageAndPeopleCount.get(grpItem.grouping.getLanguage()) != null) {
				int count = _mapLanguageAndPeopleCount.get(grpItem.grouping.getLanguage());
				_mapLanguageAndPeopleCount.put(grpItem.grouping.getLanguage(), count+1);
			} else {
				_mapLanguageAndPeopleCount.put(grpItem.grouping.getLanguage(), 1);
			}
		}

		// calculate how many groups we need for each language and the ideal capacity
		int groupNumber =1;
		Iterator<Entry<String, Integer>> iter = _mapLanguageAndPeopleCount.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Integer> entry = iter.next();
			String language = entry.getKey();
			Integer personCount = entry.getValue();
			
			int groupsCount = personCount / SettingCenter.getGroupCapacity();
			int restPersonCount = personCount % SettingCenter.getGroupCapacity();
			
			if (groupsCount >=1 ) {
				if (restPersonCount > 8.0 && restPersonCount/groupsCount >= SettingCenter.getGroupCapacity()*0.3)
					groupsCount += 1;
			}
			
			groupsCount = (groupsCount==0) ? 1:groupsCount;
			logger.info("Language:{} contains {} people, wanted capacity:{}, splits to {} groups:", language, personCount, SettingCenter.getGroupCapacity(), groupsCount);
			
			// store the results in _groupingResult
			for (int i=0; i<groupsCount; i++){
				HashMap<Integer, MemGroupingItem> persons = new HashMap<Integer, MemGroupingItem>();
				LanguageGrouping group = new LanguageGrouping();
				group.groupingItems = persons;
				group.language = language;
				group.idealCapacity = personCount / (float)groupsCount;

				// get an available groupID
				for (int x=0; x<excludedIDs.size()+1; x++){
					if (!excludedIDs.contains(groupNumber)) {
						group.groupId = groupNumber;
						groupNumber++;
						break;
					} else {
						groupNumber++;
					}
				}

				String groupName = Utils.getGroupName(language, group.groupId);
				_groupingResult.put(groupName, group);
				logger.info("{}, Ideal capacity:{}", groupName, group.idealCapacity);
			}
		}
	}
	
	/*
	 * Find a team Leader and Vice Team Leader for each group, with concern of circles
	 */
	public void allocateImportantRoles() {
		
		// iterate each language group
		{
			// look for a team leader for each group
			for (Entry<String, LanguageGrouping> entry : _groupingResult.entrySet()) {

				LanguageGrouping lanGroupSet = entry.getValue();
				String matchLanguage = lanGroupSet.language;
				int preferGender = -1;

				logger.info("Looking for a team leader for group:{} : ", Utils.getGroupName(matchLanguage, lanGroupSet.groupId));

				// Look for TeamLeader Role with experience 5,4,3 , if none, look for ViceTeamLader Role with experience 5,4,3
				logger.info("Trying to find a team leader from experience 5 to 3 with role TeamLeader ...");
				List<Integer> priorities = Arrays.asList(5, 4, 3);
				List<String> acceptedRoles = Arrays.asList(Role.ROLE_TEAMLEADER_DEF);
				MemGroupingItem leader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, _dynamicGroupingSources, preferGender, null, false);

				if (leader == null ) {
					logger.info("Trying to find a team leader from experience 5 to 3 with role TeamLeader or ViceTeamLeader ...");
					acceptedRoles = Arrays.asList(Role.ROLE_TEAMLEADER_DEF, Role.ROLE_VICETEAMLEADER_DEF);
					leader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, _dynamicGroupingSources, preferGender, null, false);
				}

				if (leader == null) {
					logger.info("Trying to find a team leader from experience 5 to 1 with role TeamLeader or ViceTeamLeader with best effort...");
					acceptedRoles = Arrays.asList(Role.ROLE_TEAMLEADER_DEF, Role.ROLE_VICETEAMLEADER_DEF);
					leader = Utils.lookForCompetentPerson(matchLanguage, Arrays.asList(5, 4, 3, 2, 1), acceptedRoles, _dynamicGroupingSources, preferGender, null, true);
				}

				if (leader != null) {
					logger.info("Found a team leader for group:{}, (UID:{}, name:{}), register role:{}, experience:{}, gender:{}",
							Utils.getGroupName(matchLanguage, lanGroupSet.groupId), leader.person.getId(), leader.person.getEnglish_name(),
							leader.person.getRoles(), leader.person.getExperience(), leader.person.getIs_male() == 1 ? 'M' : 'F');

					leader.grouping.setGroup_id(lanGroupSet.groupId);
					leader.grouping.setGroupedRole(new Role(Role.ROLE_TEAMLEADER_DEF));
					lanGroupSet.groupingItems.put(leader.person.getId(), leader);
					_dynamicGroupingSources.remove(leader.person.getId());
				} else {
					// maybe not enough people to appoint one
					logger.info("Couldn't find a team leader for group: {}", Utils.getGroupName(matchLanguage, lanGroupSet.groupId));
				}
			}

			// make sure people in same group would be in same team
			allocateWannaBeWithPersons();
		}

		// look for a vice team leader for each group
		{

			for (Entry<String, LanguageGrouping> entry : _groupingResult.entrySet()) {

				LanguageGrouping lanGroupSet = entry.getValue();
				String matchLanguage = lanGroupSet.language;
				logger.info("Looking for a vice team leader for group:{} : ", Utils.getGroupName(matchLanguage, lanGroupSet.groupId));

				// find prefer gender based on found team leader
				int preferGender = -1;
				MemGroupingItem teamLeader = entry.getValue().groupingItems.values().stream().filter(g -> g.grouping.getGroupedRole() != null && g.grouping.getGroupedRole().hasRole(Role.ROLE_TEAMLEADER_DEF)).findFirst().orElse(null);
				if (teamLeader == null) {
					logger.info("Couldn't find team Leader, ERROR");
				} else {
					preferGender = teamLeader.person.getIs_male() == 1 ? 0 : 1;
				}

				// search in-house first
				logger.info("Trying to find a vice team leader in-house from experience 4,3,2,5 with role ViceTeamLeader ...");
				List<Integer> priorities = Arrays.asList(4, 3, 2, 5);
				List<String> acceptedRoles = Arrays.asList(Role.ROLE_VICETEAMLEADER_DEF);
				MemGroupingItem viceLeader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, lanGroupSet.groupingItems, preferGender, null, false);

				// search in-house (TEAMLEADER role is ok for being VICE TEAM LEADER)
				if (viceLeader == null) {
					logger.info("Trying to find a vice team leader in-house from experience 4,3,2,5 with role ViceTeamLeader or TeamLeader ...");
					acceptedRoles = Arrays.asList(Role.ROLE_VICETEAMLEADER_DEF, Role.ROLE_TEAMLEADER_DEF);
					viceLeader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, lanGroupSet.groupingItems, preferGender, null, false);
				}

				LinkedList<Integer> excludedIds = new LinkedList<Integer>();
				for (int i = 0; i < 50; i++)    // 50 chances for try
				{
					// search outside
					if (viceLeader == null) {
						logger.info("Trying to find a vice team leader externally from experience 4,3,2,5 with role ViceTeamLeader ...");
						priorities = Arrays.asList(4, 3, 2, 5);
						acceptedRoles = Arrays.asList(Role.ROLE_VICETEAMLEADER_DEF);
						viceLeader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, _dynamicGroupingSources, preferGender, excludedIds, false);
					}

					// search outside trying best
					if (viceLeader == null) {
						logger.info("Trying to find a vice team leader externally from experience 4,3,2,5,1 with role ViceTeamLeader or TeamLeader with best effort...");
						priorities = Arrays.asList(4, 3, 2, 5, 1);
						acceptedRoles = Arrays.asList(Role.ROLE_VICETEAMLEADER_DEF, Role.ROLE_TEAMLEADER_DEF);
						viceLeader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, _dynamicGroupingSources, preferGender, excludedIds, true);
					}

					// found one, calculate total people
					if (viceLeader != null) {
						int howManyNewPpl = Utils.calculateHowManyPeopleThisPersonWith(viceLeader.person.getId(), _dynamicCircleGroups);
						if (howManyNewPpl + lanGroupSet.groupingItems.size() > lanGroupSet.idealCapacity) {
							excludedIds.add(viceLeader.person.getId());
							logger.info("Found a vice team leader outside but failed to introduce into team because has too many children {UID:{}, name{}}", viceLeader.person.getId(), viceLeader.person.getEnglish_name());
							viceLeader = null;
							continue;
						}

						break;
					}
				}

				// search inside trying best
				if (viceLeader == null) {
					logger.info("Trying to find a vice team leader externally from experience 4,3,2,5,1 with role ViceTeamLeader or TeamLeader with best effort...");
					acceptedRoles = Arrays.asList(Role.ROLE_VICETEAMLEADER_DEF, Role.ROLE_TEAMLEADER_DEF);
					viceLeader = Utils.lookForCompetentPerson(matchLanguage, Arrays.asList(4, 3, 2, 5, 1), acceptedRoles, lanGroupSet.groupingItems, preferGender, null, true);
				}

				// search outside trying best
				if (viceLeader == null) {
					logger.info("Trying to find a vice team leader in-house from experience 4,3,2,5,1 with role ViceTeamLeader or TeamLeader with best effort...");
					acceptedRoles = Arrays.asList(Role.ROLE_VICETEAMLEADER_DEF, Role.ROLE_TEAMLEADER_DEF);
					viceLeader = Utils.lookForCompetentPerson(matchLanguage, Arrays.asList(4, 3, 2, 5, 1), acceptedRoles, _dynamicGroupingSources, preferGender, null, true);
				}

				// found one
				if (viceLeader != null) {

					logger.info("Found a vice team leader for group:{}, (UID:{}, name:{}), register role:{}, experience:{}, gender:{}",
							Utils.getGroupName(matchLanguage, lanGroupSet.groupId), viceLeader.person.getId(), viceLeader.person.getEnglish_name(),
							viceLeader.person.getRoles(), viceLeader.person.getExperience(), viceLeader.person.getIs_male() == 1 ? 'M' : 'F');

					viceLeader.grouping.setGroup_id(lanGroupSet.groupId);
					viceLeader.grouping.setGroupedRole(new Role(Role.ROLE_VICETEAMLEADER_DEF));

					lanGroupSet.groupingItems.put(viceLeader.person.getId(), viceLeader);
					_dynamicGroupingSources.remove(viceLeader.person.getId());

				} else {
					// maybe no enough ppl to appoint one
					logger.info("Couldn't find a vice team leader for group: {}", Utils.getGroupName(matchLanguage, lanGroupSet.groupId));
				}
			}

			// make sure people in same group would be in same team
			allocateWannaBeWithPersons();
		}
	}
	
	/*
	 * Make sure people in same circle would be in same group
	 */
	public void allocateWannaBeWithPersons() {
		
		{
			System.out.println("Allocate wanna-be-with person based on Leader/Vice Leader allocation");
			
			// collect those need ancestors
			for (Entry<String, LanguageGrouping> entry : _groupingResult.entrySet()) {
				LanguageGrouping resultGroupSet = entry.getValue();
				String matchLanguage = resultGroupSet.language;

				List<Integer> _needAncestors = new LinkedList<Integer>();
				Map<Integer, MemGroupingItem> resultSet = resultGroupSet.groupingItems;


				for (Entry<Integer, MemGroupingItem> resultEntry : resultSet.entrySet()) {
					Integer resultPersonId = resultEntry.getKey();
					MemGroupingItem resultGrouping = resultEntry.getValue();

					// has ancestor : add to the list
					Integer ancestorId = Utils.getAncestorId(resultGrouping.grouping);
					if (ancestorId != resultPersonId) {    // has wanna be with

						if (!_needAncestors.contains(ancestorId)) {
							_needAncestors.add(ancestorId);
						}
					}

					// self is ancestor : add to the list
					if (_dynamicCircleGroups.containsKey(resultGrouping.person.getId())) {
						_needAncestors.add(resultGrouping.person.getId());
					}
				}

				// process needed ancestors
				for (Integer ancestorId : _needAncestors) {

					if (_dynamicCircleGroups.containsKey(ancestorId)) {
						HashMap<Integer, MemGroupingItem> newMap = _dynamicCircleGroups.get(ancestorId);

						Utils.transferTheGroupingItems(newMap, resultSet, _dynamicGroupingSources, resultGroupSet.groupId);
						_dynamicCircleGroups.remove(ancestorId);

					} else {
						assert (false);
					}

				}
			}
		}
		
	}
	
	/*
	 * allocate the remain circles to groups based on each group's capacity
	 */
	void allocateRemainCircles() {
		
		System.out.println("Allocate else wanna-be-with person to groups");
		
		// process remain circles:
		Iterator<Entry<Integer, HashMap<Integer, MemGroupingItem>>> remainIter = _dynamicCircleGroups.entrySet().iterator();
		while (remainIter.hasNext()) {
			Map.Entry<Integer, HashMap<Integer, MemGroupingItem>> entry = remainIter.next();
			Integer ancestorId = entry.getKey();
			HashMap<Integer, MemGroupingItem> children = entry.getValue();

			String childLan = children.values().stream().findAny().get().grouping.getLanguage();

			//String childLan = Utils.tryAnObjectFromMap(children).grouping.getLanguage();
			
			for (int i=50; i>=0; i--) {	// make it easier, just try enough times to find a receiver
				LanguageGrouping lanGroup = Utils.getRandonLanguageGroup(childLan, _groupingResult);
				//if ((SettingCenter.instance.getGroupCapacity() - lanGroup.groupingItems.size()) > (children.size() +1) || i == 0 ) {
				if ((children.size() + lanGroup.groupingItems.size() <= lanGroup.idealCapacity) || i ==0) {

					System.out.println("Get a random group for circle map: " + Utils.getGroupName(lanGroup.language, lanGroup.groupId ));
					
					Utils.transferTheGroupingItems(children, lanGroup.groupingItems, _dynamicGroupingSources, lanGroup.groupId);	
					remainIter.remove();
					
					System.out.println(String.format("Allocate remain circles : %s, total ppl :%d, ideal people:%f", Utils.getGroupName(lanGroup.language, lanGroup.groupId), lanGroup.groupingItems.size(), lanGroup.idealCapacity));
					
					break;
				}
			}
		}
	}
	
	
	void allocateOtherRoles() {

		List<String> rolesSet = SettingCenter.getRoleNameList();
		for (String needrole : rolesSet) {
			
			Iterator<Entry<String, LanguageGrouping>> iter = _groupingResult.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, LanguageGrouping> entry = iter.next();
				LanguageGrouping resultGroupSet = entry.getValue();
				String matchLanguage = resultGroupSet.language;
				Map<Integer, MemGroupingItem> grpPersons = resultGroupSet.groupingItems;
			
				if (!Utils.isRoleExisted(grpPersons, needrole)) {
					
					int preferGender = Utils.needFemaleTendency(grpPersons) ? 0 : 1;
					//int[] experiencePriorities = {2,3,1,4,5};
					
					// Try to look for one in house first 
//					MemGroupingItem personItem = Utils.lookForCompetentPerson(matchLanguage, false, experiencePriorities, needrole, true, grpPersons, -1, false, null);
					MemGroupingItem personItem = Utils.lookForCompetentPerson(matchLanguage, Arrays.asList(2,1,3,4,5), Arrays.asList(needrole), grpPersons, -1, null, false);
					
					if (personItem == null) {
						// try to look for one from outside and bring it in
						//personItem = Utils.lookForCompetentPerson(matchLanguage, false, experiencePriorities, needrole, true, _dynamicGroupingSources, preferGender, true, null);
						personItem = Utils.lookForCompetentPerson(matchLanguage, Arrays.asList(2,1,3,4,5), Arrays.asList(needrole), _dynamicGroupingSources, preferGender, null, true);
					}
					
					if (personItem == null) {
						assert(false);
						System.out.println(String.format("FAILED: Group:%s, cannot find a person for role:%s", Utils.getGroupName(matchLanguage, resultGroupSet.groupId), needrole));
						continue;
					}
					
					personItem.grouping.setGroup_id(resultGroupSet.groupId);
					personItem.grouping.setGroupedRole(new Role(needrole));
					
					_dynamicGroupingSources.remove(personItem.person.getId());
					grpPersons.put(personItem.person.getId(), personItem);
					
					System.out.println(String.format("Found person (UID:%d, name:%s) for group:%03d as Role:%s", personItem.person.getId(),
							personItem.person.getEnglish_name(), resultGroupSet.groupId, needrole));
				}
			}
		}
		
		printDebugMessage("After assigining side roles");
	}
	
	void allocateRestPerson() {
		
		// to reach the ideal capacity first
		for (int i=0; i<500; i++)	// simple and effective
		{
			// to see if all has reached the capacity
			boolean allReachedIdealCapacity = true;
			Iterator<Entry<String, LanguageGrouping>> counterIter = _groupingResult.entrySet().iterator();
			while (counterIter.hasNext()) {
				Map.Entry<String, LanguageGrouping> counterEntry = counterIter.next();
				LanguageGrouping cntLanGroups = counterEntry.getValue();
				Map<Integer, MemGroupingItem> grpPersons = cntLanGroups.groupingItems;
				
				if (grpPersons.size() < (int)cntLanGroups.idealCapacity) {
					allReachedIdealCapacity = false;
					break;
				}
			}
			
			if (allReachedIdealCapacity)
				break;
			
			Iterator<Entry<String, LanguageGrouping>> lanIter = _groupingResult.entrySet().iterator();
			while (lanIter.hasNext()) {
				Map.Entry<String, LanguageGrouping> lanEntry = lanIter.next();
				LanguageGrouping lanGroups = lanEntry.getValue();
				String language = lanGroups.language;
				Map<Integer, MemGroupingItem> grpPersons = lanGroups.groupingItems;
				
				if (grpPersons.size() >= (int)lanGroups.idealCapacity) {	// reached the ideal capacity
					System.out.println(String.format("%s has reached the capacity: %d/%f", Utils.getGroupName(language, lanGroups.groupId), grpPersons.size(), lanGroups.idealCapacity));
					continue;
				} else {
					System.out.println(String.format("%s has NOT reached the capacity: %d/%f, looking for one", Utils.getGroupName(language, lanGroups.groupId), grpPersons.size(), lanGroups.idealCapacity));
				}
				
				int preferGender = Utils.needFemaleTendency(grpPersons) ? 0 : 1;
				
				MemGroupingItem newPerson = Utils.lookForCompetentPerson(language, _dynamicGroupingSources, preferGender);
				if (newPerson != null) {
					newPerson.grouping.setGroup_id(lanGroups.groupId);
					newPerson.grouping.setGroupedRole(new Role(""));
					
					_dynamicGroupingSources.remove(newPerson.person.getId());
					grpPersons.put(newPerson.person.getId(), newPerson);
					
					System.out.println(String.format("Filliping up blanks allocating person (UID:%d, name:%s) for group:%03d as Role:%s", newPerson.person.getId(),
							newPerson.person.getEnglish_name(), lanGroups.groupId, ""));
				} else {
					System.out.println("WELL IT SHOULDNT HAPPEN");;
				}
			}
		}
		
		printDebugMessage("After reaching the ideal capacity");
		
		// randomly allocate the rest person 
		Iterator<Entry<Integer, MemGroupingItem>> iter = _dynamicGroupingSources.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, MemGroupingItem> entry = iter.next();
			Integer personId = entry.getKey();
			MemGroupingItem grpItem = entry.getValue();
			String language = grpItem.grouping.getLanguage();
			
			LanguageGrouping lanGrp = Utils.getRandonLanguageGroup(language, _groupingResult);
			
			MemGroupingItem newPerson = grpItem;
			newPerson.grouping.setGroup_id(lanGrp.groupId);
			newPerson.grouping.setGroupedRole(new Role(""));
			
			lanGrp.groupingItems.put(grpItem.person.getId(), newPerson);
			iter.remove();	
			
			System.out.println(String.format("Randomly allocating person (UID:%d, name:%s) for group:%03d as Role:%s", newPerson.person.getId(),
					newPerson.person.getEnglish_name(), lanGrp.groupId, ""));

		}
		
		printDebugMessage("After allocating ALL people");
	}
	
	public void printDebugMessage(String prefix) {
		Iterator<Entry<String, LanguageGrouping>> testIter = _groupingResult.entrySet().iterator();
		while (testIter.hasNext()) {
			Map.Entry<String, LanguageGrouping> testEntry = testIter.next();
			LanguageGrouping testGroup = testEntry.getValue();
			String language = testGroup.language;
			Map<Integer, MemGroupingItem> grpPersons = testGroup.groupingItems;
		
			String str = String.format("%s, %s has %d ppl. ideal capacity:%f", prefix, Utils.getGroupName(testGroup.language, testGroup.groupId), testGroup.groupingItems.size(), testGroup.idealCapacity);
			System.out.println(str);
		}
	}
	
	public Map<String, LanguageGrouping> getGroupedResult() {
		return _groupingResult;
	}
	
	public void markedAllGrouping() {
		System.out.println("Grouping ended !");
		
		int counter=0;
		int maleCounter = 0;
		int femaleCounter = 0;
		
		LinkedList<Integer> forTest = new LinkedList<Integer>();
		
		Iterator<Entry<String, LanguageGrouping>> lanIter = _groupingResult.entrySet().iterator();
		while (lanIter.hasNext()) {
			Map.Entry<String, LanguageGrouping> lanEntry = lanIter.next();
			LanguageGrouping lanGroup = lanEntry.getValue();
			String language = lanGroup.language;
			Map<Integer, MemGroupingItem> grpPersons = lanGroup.groupingItems;
			
			System.out.println(String.format("DONE: %s: %d people", Utils.getGroupName(language, lanGroup.groupId), grpPersons.size()));
			
			Iterator<Entry<Integer, MemGroupingItem>> personIter = grpPersons.entrySet().iterator();
			while (personIter.hasNext()) {
				Entry<Integer, MemGroupingItem> personEntry = personIter.next();
				MemGroupingItem person = personEntry.getValue();
				
				if (person.grouping.getGroupedRole() == null) {
					person.grouping.setGroupedRole(new Role(""));
				}
				
				//System.out.println(String.format("Group: %s-%d, Person(UID-%d, name:%s), registered role:%s, experience:%d, assigned role:%s", language, lanGroup.groupId, person.person.getId(), 
				//		person.person.getEnglish_name(), person.person.getRoles(), person.person.getExperience(), person.grouping.getGroupedRole()==null? "" : person.grouping.getGroupedRole().roleDesc));
				
				if (person.person.getIs_male() == 1)
					maleCounter ++;
				else
					femaleCounter ++;
			
				counter++;
			}
			
			//System.out.println(String.format("Statistics: %s-%d, total:%d, ideal:%f, male:%d, female:%d", language, lanGroup.groupId, maleCounter+femaleCounter, lanGroup.idealCapacity, maleCounter, femaleCounter));
			maleCounter=0;
			femaleCounter=0;
		}
		
		System.out.println(String.format("In total there are %d people grouped", counter));
	}
	
}


