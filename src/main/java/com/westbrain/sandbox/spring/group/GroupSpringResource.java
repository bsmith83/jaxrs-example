package com.westbrain.sandbox.spring.group;

import com.google.common.base.Objects;
import com.westbrain.sandbox.spring.exception.BadRequestException;
import com.westbrain.sandbox.spring.exception.NotFoundException;
import com.wordnik.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.ws.rs.core.MediaType;
import java.net.URI;

/**
 *
 */
@RestController
@RequestMapping(value = "/api/v2/groups", produces = MediaType.APPLICATION_JSON)
@Api(value = "/groups", description = "Operations on groups")
public class GroupSpringResource {

    @Autowired
    private GroupRepository repository;

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(
            httpMethod = "GET",
            value = "List groups using paging",
            notes = "List groups using paging and limit results.  Multiple filters and sort options can also be applied.",
            response = Group.class,
            responseContainer = "List"
    )
    @ApiResponses({@ApiResponse(code = 400, response = String.class, message = "Invalid page number")})
    public Iterable<Group> findGroups(@ApiParam(value = "Page to fetch") @RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                      @ApiParam(value = "Max limit of items returned") @RequestParam(value = "limit", defaultValue = "10", required = false) int limit,
                                      @ApiParam(value = "Filters to apply in the format: filter=&lt;name&gt;::&lt;regex&gt;|&lt;name&gt;::&lt;regex&gt;") @RequestParam(value = "filter", required = false) String filter,
                                      @ApiParam(value = "Sorts to apply in the format (- for descending): sort=&lt;sortName&gt;|-&lt;sortName&gt;") @RequestParam(value = "sort", required = false) String sort) {
        if (page <= 0) {
            throw new BadRequestException("Invalid page number");
        }

        Iterable<Group> result = repository.findGroups(page, limit, filter, sort);

        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get a group by id",
            notes = "Get a group by id"
    )
    public Group getGroupById(@ApiParam(value = "Id of the group", required = true) @PathVariable Long id) {
        Group group = repository.findOne(id);
        if (group == null) {
            throw new NotFoundException();
        }
        return group;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(
            value = "Add a group",
            notes = "Add a group"
    )
    @ApiResponses({@ApiResponse(code = 400, response = String.class, message = "Group is required")})
    public ResponseEntity<Group> addGroup(@ApiParam(value = "Group object to be added", required = true) @RequestBody Group group) {
        if (group == null) {
            throw new BadRequestException("Group is required");
        }
        Group savedGroup = repository.save(group);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedGroup.getId()).toUri();
        return ResponseEntity.created(location).body(savedGroup);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update a group",
            notes = "Update a group through replacement with the specified id, the group update must contain all fields"
    )
    @ApiResponses({@ApiResponse(code = 400, response = String.class, message = "Id of group object must match id supplied")})
    public ResponseEntity<Void> updateGroup(@ApiParam(value = "Id of the group", required = true) @PathVariable Long id,
                                            @ApiParam(value = "Group replacement object", required = true) @RequestBody Group group) {
        if (repository.findOne(id) == null) {
            throw new NotFoundException();
        }
        if (group == null || !Objects.equal(group.getId(), id)) {
            throw new BadRequestException("Id of group object must match id supplied");
        }
        if (group.getId() == null) {
            // make sure we have the proper id set before we update
            group.setId(id);
        }
        repository.save(group);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @ApiOperation(
            value = "Delete a group",
            notes = "Delete a group with the specified id"
    )
    public ResponseEntity<Void> deleteGroup(@ApiParam(value = "Id of the group", required = true) @PathVariable Long id) {
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


    @RequestMapping(value = "/{id}/members", method = RequestMethod.GET)
    @ApiOperation(
            value = "List members of a group using paging",
            notes = "List members of a group using paging and limit results.  Multiple filters and sort options can also be applied.",
            response = Group.class,
            responseContainer = "List"
    )
    @ApiResponses({@ApiResponse(code = 404, response = String.class, message = "Group not found by id")})
    public Iterable<Member> getGroupMembers(@ApiParam(value = "Id of the group", required = true) @PathVariable Long id,
                                            @ApiParam(value = "Page to fetch") @RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                            @ApiParam(value = "Max limit of items returned") @RequestParam(value = "limit", defaultValue = "15", required = false) int limit,
                                            @ApiParam(value = "Filters to apply in the format: filter=&lt;name&gt;::&lt;regex&gt;|&lt;name&gt;::&lt;regex&gt;") @RequestParam(value = "filter", required = false) String filter,
                                            @ApiParam(value = "Sorts to apply in the format (- for descending): sort=&lt;sortName&gt;|-&lt;sortName&gt;") @RequestParam(value = "sort", required = false) String sort) {
        if (repository.findOne(id) == null) {
            throw new NotFoundException("Group not found by id");
        }
        Iterable<Member> members = repository.findMembers(id, page, limit, filter, sort);
        return members;
    }

    @RequestMapping(value = "/{id}/members/{memberId}", method = RequestMethod.GET)
    @ApiOperation(
            value = "Get a member by id",
            notes = "Get a member by id in the group by id"
    )
    @ApiResponses({@ApiResponse(code = 404, response = String.class, message = "Invalid group or member id")})
    public Member getGroupMember(@ApiParam(value = "Id of the group", required = true) @PathVariable Long id,
                                 @ApiParam(value = "Id of the member", required = true) @PathVariable Long memberId) {
        Member member = repository.findMember(id, memberId);
        if (member == null) {
            throw new NotFoundException("Invalid group or member id");
        }
        return member;
    }

    @RequestMapping(value = "/{id}/members", method = RequestMethod.POST)
    @ApiOperation(
            value = "Add a member to a group",
            notes = "Add a member to a group by group id"
    )
    @ApiResponses({@ApiResponse(code = 400, response = String.class, message = "Member is required")})
    public ResponseEntity<Member> addMember(@ApiParam(value = "Id of the group", required = true) @PathVariable Long id,
                                            @ApiParam(value = "Member object to be added", required = true) @RequestBody Member member) {
        if (member == null) {
            throw new BadRequestException("Member is required");
        }
        Member savedMember = repository.saveMember(id, member);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedMember.getId()).toUri();
        return ResponseEntity.created(location).body(savedMember);

    }

    @RequestMapping(value = "/{id}/members/{memberId}", method = RequestMethod.PUT)
    @ApiOperation(
            value = "Update a member of a group",
            notes = "Update a member of a group through replacement with the specified id, the group update must contain all fields"
    )
    @ApiResponses({@ApiResponse(code = 400, response = String.class, message = "Id of member object must match id supplied")})
    public ResponseEntity<Void> updateMember(@ApiParam(value = "Id of the group", required = true) @PathVariable Long id,
                                             @ApiParam(value = "Id of the member", required = true) @PathVariable Long memberId,
                                             @ApiParam(value = "Member object replacement", required = true) @RequestBody Member member) {
        if (repository.findOne(id) == null) {
            throw new NotFoundException();
        }
        if (repository.findMember(id, memberId) == null) {
            throw new NotFoundException();
        }
        if (member == null || !Objects.equal(member.getId(), memberId)) {
            throw new BadRequestException("Id of member object must match id supplied");
        }
        if (member.getId() == null) {
            // make sure we have the proper id set before we update
            member.setId(memberId);
        }
        repository.saveMember(id, member);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{id}/members/{memberId}", method = RequestMethod.DELETE)
    @ApiOperation(
            value = "Delete a member of a group",
            notes = "Delete a member of a group with the specified id"
    )
    public ResponseEntity<Void> deleteMember(@ApiParam(value = "Id of the group", required = true) @PathVariable Long id,
                                             @ApiParam(value = "Id of the member", required = true) @PathVariable Long memberId) {
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
