package com.westbrain.sandbox.jaxrs.group;

import com.thedeanda.lorem.Lorem;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A "fake" in-memory repository for Group data.
 *
 * @auhor Eric Westfall (ewestfal@gmail.com)
 */
@Repository
public class GroupRepository {

    private static Random random = new Random();
    private static AtomicLong nextGroupId = new AtomicLong();
    private static AtomicLong nextMemberId = new AtomicLong();

    private final ConcurrentHashMap<Long, Group> groups = new ConcurrentHashMap<Long, Group>();
    private final ConcurrentHashMap<Long, Map<Long, Member>> members = new ConcurrentHashMap<Long, Map<Long, Member>>();

    public Iterable<Group> findAll() {
        return groups.values();
    }

    public Iterable<Group> findGroups(int pageNum, int limit, String filter, String sort) {

        List<Group> groupList = new ArrayList<Group>(groups.values());

        groupList = filter(filter, groupList);
        groupList = sort(sort, groupList);
        groupList = page(pageNum, limit, groupList);

        return groupList;
    }

    private <T> List<T> page(int pageNum, int limit, List<T> objectList) {
        int endIndex = pageNum * limit;
        int startIndex = endIndex - limit;

        if (endIndex > objectList.size()) {
            endIndex = objectList.size();
        }

        if (startIndex >= objectList.size()) {
            // start is out of range, return empty
            return new ArrayList<T>();
        }

        return objectList.subList(startIndex, endIndex);
    }

    private <T> List<T> filter(String filterParam, List<T> objectList) {
        if (StringUtils.isEmpty(filterParam)) {
            return objectList;
        }

        String[] filters = filterParam.split("\\|");
        List<T> removedObjects = new ArrayList<T>();

        for (T object : objectList) {
            for (String filter : filters) {
                String[] filterValues = filter.split("::");
                if (filterValues.length != 2) {
                    continue;
                }

                String name = filterValues[0].toLowerCase();

                try {
                    Method getMethod = object.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
                    String value = getMethod.invoke(object).toString();
                    if (!value.toLowerCase().matches(filterValues[1].toLowerCase())) {
                        removedObjects.add(object);
                    }
                } catch (NoSuchMethodException e) {
                    continue;
                } catch (InvocationTargetException e) {
                    continue;
                } catch (IllegalAccessException e) {
                    continue;
                }
            }
        }

        objectList.removeAll(removedObjects);

        return objectList;
    }

    private <T> List<T> sort(final String sortParam, List<T> objectList) {
        if (StringUtils.isEmpty(sortParam)) {
            return objectList;
        }

        final List<Comparator<T>> comparators = new ArrayList<Comparator<T>>();
        String[] sorts = sortParam.split("\\|");
        for (String sort: sorts) {
            comparators.add((Comparator<T>)createComparator(sort));
        }

        objectList.sort(new Comparator<T>() {
            public int compare(T object1, T object2) {
                for (Comparator<T> comparator : comparators) {
                    int result = comparator.compare(object1, object2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        });

        return objectList;
    }

    private <T> Comparator<T> createComparator(final String sort) {
        return new Comparator<T>() {
            public int compare(T object1, T object2) {


            String sortName = sort;
                        boolean ascending = true;
                        if (sortName.startsWith("-")) {
                            sortName = sortName.substring(1);
                            ascending = false;
                        }

                        try {
                            Method getMethod = object1.getClass().getMethod("get" + Character.toUpperCase(sortName.charAt(0))
                                    + sortName.substring(1));

                            Object value1 = getMethod.invoke(object1);
                            Object value2 = getMethod.invoke(object2);

                            int result = 0;

                            if (value1 instanceof Number) {
                                if (((Number) value1).longValue() > ((Number) value2).longValue()) {
                                    result = 1;
                                } else if (((Number) value1).longValue() < ((Number) value2).longValue()) {
                                    result = -1;
                                }
                            } else if (value1 instanceof String) {
                                result = ((String) value1).compareTo(((String) value2));
                            }

                            if (!ascending) {
                                result = result * -1;
                            }

                            return result;

                        } catch (NoSuchMethodException e) {
                            return 0;
                        } catch (InvocationTargetException e) {
                            return 0;
                        } catch (IllegalAccessException e) {
                            return 0;
                        }
            }
        };
    }

    public Group findOne(Long id) {
        return groups.get(id);
    }

    public Group save(Group group) {
        if (group.getId() == null) {
            group.setId(nextGroupId.incrementAndGet());

        }
        groups.put(group.getId(), group);
        return group;
    }

    public Group delete(Long id) {
        return groups.remove(id);
    }

    public Member saveMember(Long groupId, Member member) {
        if (groupId == null) {
            throw new IllegalArgumentException("groupId was null");
        }
        this.members.putIfAbsent(groupId, new ConcurrentHashMap<Long, Member>());
        Map<Long, Member> groupMembers = this.members.get(groupId);
        if (member.getId() != null) {
            // if the id is not null, it's an update
            if (!groupMembers.containsKey(member.getId())) {
                throw new IllegalArgumentException("Tried to provide a member id for a member that doesn't exist");
            }
        } else {
            member.setId(nextMemberId.incrementAndGet());
        }
        groupMembers.put(member.getId(), member);
        return member;
    }

    public Iterable<Member> findMembers(Long groupId, int pageNum, int limit, String filter, String sort) {
        Map<Long, Member> groupMembers = members.get(groupId);
        if (groupMembers == null) {
            return null;
        }

        List<Member> memberList = new ArrayList<Member>(groupMembers.values());

        memberList = filter(filter, memberList);
        memberList = sort(sort, memberList);
        memberList = page(pageNum, limit, memberList);

        return memberList;
    }

    public Member findMember(Long groupId, Long memberId) {
        Map<Long, Member> groupMembers = this.members.get(groupId);
        if (groupMembers == null) {
            return null;
        }
        return groupMembers.get(memberId);
    }

    public Member deleteMember(Long id, Long memberId) {
        Map<Long, Member> groupMembers = members.get(id);
        if (groupMembers == null) {
            return null;
        }
        return groupMembers.remove(memberId);
    }

    /**
     * Create 10 groups each with 1-15 members
     */
    @PostConstruct
    private void initializeData() {
        for (int i = 0; i < 100; i++) {
            Group group = save(new Group(Lorem.getWords(1), Lorem.getWords(7)));
            int membersToGenerate = random.nextInt(15) + 1;
            for (int j = 0; j < membersToGenerate; j++) {
                saveMember(group.getId(), new Member(Lorem.getFirstName()));
            }
        }

    }


}
