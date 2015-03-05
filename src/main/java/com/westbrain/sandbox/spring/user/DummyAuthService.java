package com.westbrain.sandbox.spring.user;

import com.google.common.collect.ImmutableMap;
import com.westbrain.sandbox.spring.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Brian on 3/4/15.
 */
public class DummyAuthService {

    static Map<String, List<String>> permissionMap = ImmutableMap.<String, List<String>>builder()
            .put("readGroups", Arrays.asList("user1", "user2", "user3"))
            .put("writeGroups", Arrays.asList("user1"))
            .put("readMembers", Arrays.asList("user1", "user2"))
            .put("writeMembers", Arrays.asList("user1"))
            .build();

    public static boolean hasPermission(User user, String permissionType) {
            return permissionMap.get(permissionType).contains(user.getLogin());
    }
}
