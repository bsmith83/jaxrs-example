package com.westbrain.sandbox.jaxrs.group;

import com.google.common.base.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.net.URI;

/**
 * Created by Brian on 2/24/15.
 */
@RestController
@RequestMapping("/api/v2/groups")
public class GroupSpringResource {

    @Autowired
    private GroupRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    public Iterable<Group> findGroups(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                      @RequestParam(value = "limit", defaultValue = "10", required = false) int limit,
                                      @RequestParam(value = "filter", required = false) String filter,
                                      @RequestParam(value = "sort", required = false) String sort) {
        if (page <= 0) {
            throw new BadRequestException();
        }

        Iterable<Group> result = repository.findGroups(page, limit, filter, sort);

        return result;
    }

    @RequestMapping("/{id}")
    public Group getGroupById(@PathVariable Long id) {
        Group group = repository.findOne(id);
        if (group == null) {
            throw new NotFoundException();
        }
        return group;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Group> createGroup(@RequestBody Group group) {
        if (group == null) {
            throw new BadRequestException();
        }
        Group savedGroup = repository.save(group);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedGroup.getId()).toUri();
        return ResponseEntity.created(location).body(savedGroup);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Void> updateGroup(@PathVariable Long id, @RequestBody Group group) {
        if (repository.findOne(id) == null) {
            throw new NotFoundException();
        }
        if (group == null || !Objects.equal(group.getId(), id)) {
            throw new BadRequestException();
        }
        if (group.getId() == null) {
            // make sure we have the proper id set before we update
            group.setId(id);
        }
        repository.save(group);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        Group removedGroup = null;
        if (id != null) {
            removedGroup = repository.delete(id);
        }

        if (removedGroup == null) {
            throw new NotFoundException();
        } else {
            return ResponseEntity.noContent().build();
        }
    }


    @RequestMapping("/{id}/members")
    public Iterable<Member> getGroupMembers(@PathVariable Long id,
                                            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                            @RequestParam(value = "limit", defaultValue = "15", required = false) int limit,
                                            @RequestParam(value = "filter", required = false) String filter,
                                            @RequestParam(value = "sort", required = false) String sort) {
        if (repository.findOne(id) == null) {
            throw new NotFoundException();
        }
        Iterable<Member> members = repository.findMembers(id, page, limit, filter, sort);
        return members;
    }

    @RequestMapping("/{id}/members/{memberId}")
    public Member getGroupMember(@PathVariable Long id, @PathVariable Long memberId) {
        Member member = repository.findMember(id, memberId);
        if (member == null) {
            throw new NotFoundException();
        }
        return member;
    }

    @RequestMapping(value = "/{id}/members", method = RequestMethod.POST)
    public ResponseEntity<Member> addMember(@PathVariable Long id, @RequestBody Member member) {
        if (member == null) {
            throw new BadRequestException();
        }
        Member savedMember = repository.saveMember(id, member);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedMember.getId()).toUri();
        return ResponseEntity.created(location).body(savedMember);

    }

    @RequestMapping(value = "/{id}/members/{memberId}", method = RequestMethod.PUT)
    public ResponseEntity<Void> updateMember(@PathVariable Long id, @PathVariable Long memberId, @RequestBody Member member) {
        if (repository.findOne(id) == null) {
            throw new NotFoundException();
        }
        if (repository.findMember(id, memberId) == null) {
            throw new NotFoundException();
        }
        if (member == null || !Objects.equal(member.getId(), memberId)) {
            throw new BadRequestException();
        }
        if (member.getId() == null) {
            // make sure we have the proper id set before we update
            member.setId(memberId);
        }
        repository.saveMember(id, member);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMember(@PathVariable Long id, @PathVariable Long memberId) {
        Member removedMember = null;
        if (id != null && memberId != null) {
            removedMember = repository.deleteMember(id, memberId);
        }

        if (removedMember == null) {
            throw new NotFoundException();
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
