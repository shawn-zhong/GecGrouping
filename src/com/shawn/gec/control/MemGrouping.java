package com.shawn.gec.control;

import com.shawn.gec.po.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class MemGrouping {

	private Logger logger = LoggerFactory.getLogger(MemGrouping.class);

	// this map stores all the grouping items and keep still for search
	public static Map<Integer, MemGroupingItem> _staticGroupingSources = new LinkedHashMap<>();
	
	// this map stores all the grouping items but for dynamics process
	private static Map<Integer, MemGroupingItem> _dynamicGroupingSources = new LinkedHashMap<>();
	
	// this is for looking up user-id by phone number
	private static Map<String, Integer> _phoneNumberMap = new LinkedHashMap<>();
	
	// person and wanna-be-with person would be in a same circle. this map is dynamic also
	private static Map<Integer, HashMap<Integer, MemGroupingItem>> _dynamicCircleGroups = new LinkedHashMap<>();
	
	// here to generate and store the grouping results
	private static Map<String, LanguageGrouping> _groupingResult = new LinkedHashMap<>();

	public static final MemGrouping Instance = new MemGrouping();
	
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

        for (Entry<Integer, MemGroupingItem> entry : _dynamicGroupingSources.entrySet()) {
            MemGroupingItem grpItem = entry.getValue();
            String wannaBeWith = grpItem.person.getBe_with();

            if (wannaBeWith == null || wannaBeWith.isEmpty()) {
                grpItem.grouping.setBe_with_uid(-1);
                continue;
            }

            // trim and remove the non-numeric charaters ***
            wannaBeWith = wannaBeWith.trim();

            // look for the phone number want to be with
            if (_phoneNumberMap.get(wannaBeWith) != null) {
                grpItem.grouping.setBe_with_uid(_phoneNumberMap.get(wannaBeWith));

                logger.info("Match succeeded. (UID:{} name:{}) wants to be with (UID:{} name:{})",
                        grpItem.person.getId(), grpItem.person.getEnglish_name(), _phoneNumberMap.get(wannaBeWith),
                        _dynamicGroupingSources.get(_phoneNumberMap.get(wannaBeWith)).person.getEnglish_name());

                continue;
            }

            // correct the stupid guys want to be with himself
            if (grpItem.grouping.getBe_with_uid() == grpItem.person.getId()) {
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
        for (Entry<Integer, MemGroupingItem> entry : _dynamicGroupingSources.entrySet()) {
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
        for (Entry<Integer, MemGroupingItem> entry : _dynamicGroupingSources.entrySet()) {
            MemGroupingItem grpItem = entry.getValue();

            int ancestorId = Utils.getAncestorId(grpItem.grouping);

            if (grpItem.person.getId() != ancestorId) {
                logger.info("GENERATING CIRCLES, {UID:{}, name:{}} 's ancestor is {}", grpItem.person.getId(), grpItem.person.getEnglish_name(), ancestorId);
            }

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
		
		// delete those single ones (self-dependent only)
		Iterator<Entry<Integer, HashMap<Integer, MemGroupingItem>>> removeIter = _dynamicCircleGroups.entrySet().iterator();
		while (removeIter.hasNext()) {
			Map.Entry<Integer, HashMap<Integer, MemGroupingItem>> removeEntry = removeIter.next();
			Integer ancestorId = removeEntry.getKey();
			HashMap<Integer, MemGroupingItem> children = removeEntry.getValue();
			
			if (children.size() <= 1) {
				removeIter.remove();
			} else {
				String childrenStrs = children.values().stream().filter(p->p.person.getId()!=ancestorId).map(p->String.format("{UID:%d, name:%s},", p.person.getId(), p.person.getEnglish_name())).reduce((a,b)->a+b).orElse("");
				logger.info("Circle done: ancestor {UID:{}, name:{}} has {} child(ren) : {}", ancestorId, children.get(ancestorId).person.getEnglish_name(), children.size()-1, childrenStrs);
			}
		}
	}

	/*
	 * Sum up the people of each language and calc the number and capacity of groups
	 */
	public void calcHowManyGroupsAndIdealCapacity(Set<Integer> excludedIDs) {
		
		// calculate how many people (and female) for each language
		ConcurrentHashMap<String, Integer> _mapLanguageAndPeopleCount = new ConcurrentHashMap<String, Integer>();
		ConcurrentHashMap<String, Integer> _mapLanguageAndFemaleCount = new ConcurrentHashMap<>();

        for (Entry<Integer, MemGroupingItem> entryCnt : _dynamicGroupingSources.entrySet()) {
            MemGroupingItem grpItem = entryCnt.getValue();

            // calculate how many people for this language
            int peopleCount = 0;
            if (_mapLanguageAndPeopleCount.get(grpItem.grouping.getLanguage()) != null) {
                peopleCount = _mapLanguageAndPeopleCount.get(grpItem.grouping.getLanguage());
            }
            _mapLanguageAndPeopleCount.put(grpItem.grouping.getLanguage(), peopleCount + 1);

            // calcualte how many female for this language
            int femaleCount = 0;
            if (_mapLanguageAndFemaleCount.get(grpItem.grouping.getLanguage()) != null) {
                femaleCount = _mapLanguageAndFemaleCount.get(grpItem.grouping.getLanguage());
            }
            if (grpItem.person.getIs_male() != 1) {
                femaleCount += 1;
            }
            _mapLanguageAndFemaleCount.put(grpItem.grouping.getLanguage(), femaleCount);
        }

		// calculate how many groups we need for each language and the ideal capacity
		int groupNumber =1;
        for (Entry<String, Integer> entry : _mapLanguageAndPeopleCount.entrySet()) {
            String language = entry.getKey();
            Integer personCount = entry.getValue();
            int femaleCount = _mapLanguageAndFemaleCount.get(language);


            int groupsCount = personCount / SettingCenter.getGroupCapacity();
            int restPersonCount = personCount % SettingCenter.getGroupCapacity();

            if (groupsCount >= 1) {
                if (restPersonCount >= 8.0 && restPersonCount / groupsCount >= SettingCenter.getGroupCapacity() * 0.3)
                    groupsCount += 1;
            }

            groupsCount = (groupsCount == 0) ? 1 : groupsCount;
            logger.info("Language:{} contains {} people, wanted capacity:{}, splits to {} groups:", language, personCount, SettingCenter.getGroupCapacity(), groupsCount);

            // store the results in _groupingResult
            for (int i = 0; i < groupsCount; i++) {
                HashMap<Integer, MemGroupingItem> persons = new HashMap<Integer, MemGroupingItem>();
                LanguageGrouping group = new LanguageGrouping();
                group.groupingItems = persons;
                group.language = language;
                group.idealCapacity = personCount / (float) groupsCount;
                group.idealFemaleCapacity = femaleCount / (float) groupsCount;

                // get an available groupID
                for (int x = 0; x < excludedIDs.size() + 1; x++) {
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
                logger.info("{}, Ideal capacity:{}. Female ideal capacity:{}", groupName, group.idealCapacity, group.idealFemaleCapacity);
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
				//List<Integer> priorities = Arrays.asList(5, 4, 3);
				List<Integer> priorities = Arrays.asList(5, 4, 3);
				List<String> acceptedRoles = Arrays.asList(Role.ROLE_TEAMLEADER_DEF);
				MemGroupingItem leader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, _dynamicGroupingSources, preferGender, null, false);


				if (leader == null ) {
					logger.info("Trying to find a team leader from experience 5 to 3 with role TeamLeader or ViceTeamLeader ...");
					acceptedRoles = Arrays.asList(Role.ROLE_TEAMLEADER_DEF, Role.ROLE_VICETEAMLEADER_DEF);
					leader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, _dynamicGroupingSources, preferGender, null, false);
				}

				// select from higest experience
				if (leader == null) {
					logger.info("Trying to find a team leader from experience 5 to 3 with/without role TeamLeader or ViceTeamLeader...");
					acceptedRoles = Arrays.asList();
					priorities = Arrays.asList(5, 4, 3); // 2
					leader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, _dynamicGroupingSources, preferGender, null, false);
				}

				// select from higest experience
				if (leader == null) {
					logger.info("Trying to find a team leader from experience 2 to 1 with/without role TeamLeader or ViceTeamLeader with best effort...");
					acceptedRoles = Arrays.asList(Role.ROLE_TEAMLEADER_DEF, Role.ROLE_VICETEAMLEADER_DEF);
					priorities = Arrays.asList(2, 1); // 2
					leader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, _dynamicGroupingSources, preferGender, null, true);
				}

				if (leader != null) {
					logger.info("Found a team leader for group:{}, (UID:{}, name:{}), register role:{}, experience:{}, gender:{}",
							Utils.getGroupName(matchLanguage, lanGroupSet.groupId), leader.person.getId(), leader.person.getEnglish_name(),
							leader.person.getRoles(), leader.person.getExperience(), leader.person.getIs_male() == 1 ? 'M' : 'F');

					leader.grouping.setGroup_id(lanGroupSet.groupId);
					leader.grouping.setGroupedRole(new Role(Role.ROLE_TEAMLEADER_DEF));
					lanGroupSet.groupingItems.put(leader.person.getId(), leader);
					_dynamicGroupingSources.remove(leader.person.getId());

					// make sure people in same group would be in same team
					allocateWannaBeWithPersons();
				} else {
					// maybe not enough people to appoint one
					logger.info("Couldn't find a team leader for group: {}", Utils.getGroupName(matchLanguage, lanGroupSet.groupId));
				}
			}
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
				List<Integer> priorities = Arrays.asList(4, 5, 3, 2);
				List<String> acceptedRoles = Arrays.asList(Role.ROLE_VICETEAMLEADER_DEF, Role.ROLE_TEAMLEADER_DEF);
				MemGroupingItem viceLeader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, lanGroupSet.groupingItems, preferGender, null, false);

				// search in-house (TEAMLEADER role is ok for being VICE TEAM LEADER)
				if (viceLeader == null) {
					logger.info("Trying to find a vice team leader in-house from experience 4,3,2,5 with role ViceTeamLeader or TeamLeader ...");
					acceptedRoles = Arrays.asList(Role.ROLE_VICETEAMLEADER_DEF, Role.ROLE_TEAMLEADER_DEF);
					viceLeader = Utils.lookForCompetentPerson(matchLanguage, priorities, acceptedRoles, lanGroupSet.groupingItems, preferGender, null, false);
				}

				LinkedList<Integer> excludedIds = new LinkedList<Integer>();
				for (int i = 0; i < _dynamicCircleGroups.size(); i++)    // chances for try
				{
					// search outside
					if (viceLeader == null) {
						logger.info("Trying to find a vice team leader externally from experience 4,3,2,5 with role ViceTeamLeader ...");
						priorities = Arrays.asList(4, 5, 3, 2);
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
					}

					break;	// cannot find even one
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

					// make sure people in same group would be in same team
					allocateWannaBeWithPersons();

				} else {
					// maybe no enough ppl to appoint one
					logger.info("Couldn't find a vice team leader for group: {}", Utils.getGroupName(matchLanguage, lanGroupSet.groupId));
				}
			}
		}
	}
	
	/*
	 * Make sure people in same circle would be in same group
	 */
	private void allocateWannaBeWithPersons() {
		
		{
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
	public void allocateRemainCircles() {
		
		// process remain circles:
		Iterator<Entry<Integer, HashMap<Integer, MemGroupingItem>>> remainIter = _dynamicCircleGroups.entrySet().iterator();
		while (remainIter.hasNext()) {
			Map.Entry<Integer, HashMap<Integer, MemGroupingItem>> entry = remainIter.next();
			Integer ancestorId = entry.getKey();
			HashMap<Integer, MemGroupingItem> children = entry.getValue();

			String childLan = children.values().stream().findAny().get().grouping.getLanguage();
			
			for (int i=30; i>=0; i--) {	// make it easier, just try enough times to find a receiver

                LanguageGrouping lanGroup = Utils.getTheLanguageGroupOfLeastPerson(childLan, _groupingResult);

				//if ((SettingCenter.instance.getGroupCapacity() - lanGroup.groupingItems.size()) > (children.size() +1) || i == 0 ) {
				if ((children.size() + lanGroup.groupingItems.size() <= lanGroup.idealCapacity - 3) || i ==0) {

					logger.info("Allocating remaining circles, got a random group ({}) for circle (ancestorId:{}, size:{})", Utils.getGroupName(lanGroup.language, lanGroup.groupId ), ancestorId, children.size());
					
					Utils.transferTheGroupingItems(children, lanGroup.groupingItems, _dynamicGroupingSources, lanGroup.groupId);	
					remainIter.remove();

					logger.info("Allocating done :{}, total ppl:{}, ideal people:{}", Utils.getGroupName(lanGroup.language, lanGroup.groupId), lanGroup.groupingItems.size(), lanGroup.idealCapacity);
					
					break;
				}
			}
		}
	}
	
	
	void allocateOtherRoles() {

		List<String> rolesSet = SettingCenter.getRoleNameList();
		for (String needrole : rolesSet) {

            for (Entry<String, LanguageGrouping> entry : _groupingResult.entrySet()) {
                LanguageGrouping resultGroupSet = entry.getValue();
                String matchLanguage = resultGroupSet.language;
                Map<Integer, MemGroupingItem> grpPersons = resultGroupSet.groupingItems;

                if (!Utils.isRoleExisted(grpPersons, needrole)) {

                    int preferGender = Utils.needFemaleTendency(resultGroupSet) ? 0 : 1;
                    //int[] experiencePriorities = {2,3,1,4,5};

                    // Try to look for one in house first
                    logger.info("Trying to look for role {} in house, prefer gender: {} for {}", needrole, preferGender == 1 ? "M" : "F", Utils.getGroupName(resultGroupSet.language, resultGroupSet.groupId));
                    MemGroupingItem personItem = Utils.lookForCompetentPerson(matchLanguage, Arrays.asList(2, 3, 1, 4, 5), Arrays.asList(needrole), grpPersons, -1, null, false);

                    if (personItem == null) {
                        // try to look for one from outside and bring it in
                        logger.info("Trying to look for role {} externally, prefer gender: {} for {}", needrole, preferGender == 1 ? "M" : "F", Utils.getGroupName(resultGroupSet.language, resultGroupSet.groupId));
                        personItem = Utils.lookForCompetentPerson(matchLanguage, Arrays.asList(2, 3, 1, 4, 5), Arrays.asList(needrole), _dynamicGroupingSources, preferGender, null, true);
                    }

                    if (personItem == null) {
                        assert (false);
                        logger.info("FAILED allocating other roles for Group:{}, cannot find a person as role:{}", Utils.getGroupName(matchLanguage, resultGroupSet.groupId), needrole);
                        continue;
                    }

                    personItem.grouping.setGroup_id(resultGroupSet.groupId);
                    personItem.grouping.setGroupedRole(new Role(needrole));

                    _dynamicGroupingSources.remove(personItem.person.getId());
                    grpPersons.put(personItem.person.getId(), personItem);

                    logger.info("Succeeded allocating other roles({}) for Group:{},  found a person (UID:{}, name:{}, gender:{})",
                            needrole, Utils.getGroupName(matchLanguage, resultGroupSet.groupId), personItem.person.getId(), personItem.person.getEnglish_name(), personItem.person.getIs_male() == 1 ? "M" : "F");
                }
            }
		}

		printStatusMessage();
	}
	
	void allocateRestPerson() {
		
		// to reach the ideal capacity first
		for (int i=0; i<500; i++)	// simple and effective
		{
			// to see if all has reached the capacity
			boolean allReachedIdealCapacity = true;
            for (Entry<String, LanguageGrouping> counterEntry : _groupingResult.entrySet()) {
                LanguageGrouping cntLanGroups = counterEntry.getValue();
                Map<Integer, MemGroupingItem> grpPersons = cntLanGroups.groupingItems;

                if (grpPersons.size() < (int) cntLanGroups.idealCapacity) {
                    allReachedIdealCapacity = false;
                    break;
                }
            }
			
			if (allReachedIdealCapacity)
				break;

            for (Entry<String, LanguageGrouping> lanEntry : _groupingResult.entrySet()) {
                LanguageGrouping lanGroups = lanEntry.getValue();
                String language = lanGroups.language;
                Map<Integer, MemGroupingItem> grpPersons = lanGroups.groupingItems;

                if (grpPersons.size() >= (int) lanGroups.idealCapacity) {    // reached the ideal capacity

                    logger.info(String.format("Group %s has reached the capacity: %d/%f", Utils.getGroupName(language, lanGroups.groupId), grpPersons.size(), lanGroups.idealCapacity));
                    continue;
                } else {
                    logger.info(String.format("Group %s has NOT reached the capacity: %d/%f, looking for one", Utils.getGroupName(language, lanGroups.groupId), grpPersons.size(), lanGroups.idealCapacity));
                }

                int preferGender = Utils.needFemaleTendency(lanGroups) ? 0 : 1;

                MemGroupingItem newPerson = Utils.lookForCompetentPerson(language, _dynamicGroupingSources, preferGender);
                if (newPerson != null) {
                    newPerson.grouping.setGroup_id(lanGroups.groupId);
                    newPerson.grouping.setGroupedRole(new Role(""));

                    _dynamicGroupingSources.remove(newPerson.person.getId());
                    grpPersons.put(newPerson.person.getId(), newPerson);

                    logger.info(String.format("Filliping up blanks allocating person (UID:%d, name:%s, gender:%s) for group %s to reach the capacity:%s as Role:%s",
                            newPerson.person.getId(),
                            newPerson.person.getEnglish_name(), newPerson.person.getIs_male() == 1 ? "M" : "F", Utils.getGroupName(language, lanGroups.groupId), lanGroups.idealCapacity, "<null>"));
                } else {
                    logger.info("NOT ENOUGH PERSON FOR GROUP {}", Utils.getGroupName(language, lanGroups.groupId));
                    ;
                }
            }
		}

		printStatusMessage();
		
		// randomly allocate the rest person 
		Iterator<Entry<Integer, MemGroupingItem>> iter = _dynamicGroupingSources.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, MemGroupingItem> entry = iter.next();
			MemGroupingItem grpItem = entry.getValue();
			String language = grpItem.grouping.getLanguage();

            LanguageGrouping lanGrp = Utils.getTheLanguageGroupOfLeastPerson(language, _groupingResult);

            grpItem.grouping.setGroup_id(lanGrp.groupId);
			grpItem.grouping.setGroupedRole(new Role(""));
			
			lanGrp.groupingItems.put(grpItem.person.getId(), grpItem);
			iter.remove();	

			logger.info(String.format("Randomly allocating after reaching the capacity, person (UID:%d, name:%s) for group:%s as Role:%s", grpItem.person.getId(),
					grpItem.person.getEnglish_name(), Utils.getGroupName(language, lanGrp.groupId), "<null>"));
		}

		printStatusMessage();
	}
	
	private void printStatusMessage() {
        for (Entry<String, LanguageGrouping> testEntry : _groupingResult.entrySet()) {
            LanguageGrouping testGroup = testEntry.getValue();

            String str = String.format("PRINT STATUS :%s has %d ppl. %d female, ideal capacity:%f, ideal female:%f",
                    Utils.getGroupName(testGroup.language, testGroup.groupId), testGroup.groupingItems.size(),
                    testGroup.groupingItems.values().stream().filter(p -> p.person.getIs_male() != 1).count(),
                    testGroup.idealCapacity, testGroup.idealFemaleCapacity);
            logger.info(str);
        }
	}
	
	public Map<String, LanguageGrouping> getGroupedResult() {
		return _groupingResult;
	}
	
	public void markedAllGrouping() {
		int counter=0;
		
		LinkedList<Integer> forTest = new LinkedList<Integer>();

        for (Entry<String, LanguageGrouping> lanEntry : _groupingResult.entrySet()) {
            LanguageGrouping lanGroup = lanEntry.getValue();
            String language = lanGroup.language;
            Map<Integer, MemGroupingItem> grpPersons = lanGroup.groupingItems;

            logger.info(String.format("AUTO GROUPING DONE: %s: %d people, ideal capacity:%f", Utils.getGroupName(language, lanGroup.groupId), grpPersons.size(), lanGroup.idealCapacity));

            for (Entry<Integer, MemGroupingItem> personEntry : grpPersons.entrySet()) {
                MemGroupingItem person = personEntry.getValue();

                if (person.grouping.getGroupedRole() == null) {
                    person.grouping.setGroupedRole(new Role(""));
                }

                counter++;
            }
        }

		logger.info("In total there are {} people grouped. ", counter);
	}
	
}


