package com.westbrain.sandbox.jaxrs.group;

import com.google.common.base.Objects;
import com.wordnik.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


/**
 * A resource implementation which provides a REST-ful, JSON-based API to the groups resource and it's members
 * sub resource.
 *
 * <p>Leverages standard JAX-RS annotations for defining the API.</p>
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
@Service
@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/groups", description = "Operations on groups")
public class GroupResource {

    @Autowired
    private GroupRepository repository;

    @Context
    private Response response;
    @Context
    private UriInfo uriInfo;

    public static int DEFAULT_GROUP_LIMIT = 5;

    @GET
    @ApiOperation(
    	    value = "List groups using paging",
    	    notes = "List groups using paging and limit results.  Multiple filters and sort options can also be applied.",
    	    response = Group.class,
    	    responseContainer = "List"
    )
    @ApiResponses({ @ApiResponse(code = 400, message = "Invalid page number") })
    public Response getGroups(@ApiParam(value = "Page to fetch") @DefaultValue("1") @QueryParam("page") int page,
                              @ApiParam(value = "Max limit of items returned") @DefaultValue("10") @QueryParam("limit") int limit,
                              @ApiParam(value = "Filters to apply in the format: filter=&lt;name&gt;::&lt;regex&gt;|&lt;name&gt;::&lt;regex&gt;") @QueryParam("filter") String filter,
                              @ApiParam(value = "Sorts to apply in the format (- for descending): sort=&lt;sortName&gt;|-&lt;sortName&gt;") @QueryParam("sort") String sort) {
        if (page <= 0) {
            return badRequest("Invalid page number");
        }

        Iterable<Group> result = repository.findGroups(page, limit, filter, sort);

        return Response.ok(result).build();
    }


    @GET
    @Path("/{id}")
    @ApiOperation(
    	    value = "Get a group by id",
    	    notes = "Get a group by id"
    )
    public Response getGroupById(@ApiParam(value = "Id of the group", required = true) @PathParam("id") Long id) {
        Group group = repository.findOne(id);
        if (group == null) {
            return notFound();
        }
        return Response.ok(group).build();
    }

    @POST
    @Path("/{id}")
    @ApiOperation(
    	    value = "Add a group",
    	    notes = "Add a group"
    )
    @ApiResponses({ @ApiResponse(code = 400, message = "Group is required") })
    public Response addGroup(@ApiParam(value = "Group object to be added", required = true) Group group) {
        if (group == null) {
            return badRequest("Group is required");
        }
        Group savedGroup = repository.save(group);
        return Response.created(uriInfo.getAbsolutePathBuilder().path("{id}").build(savedGroup.getId())).entity(savedGroup).build();
    }

    @PUT
    @Path("/{id}")
    @ApiOperation(
    	    value = "Update a group",
    	    notes = "Update a group through replacement with the specified id, the group update must contain all fields"
    )
    @ApiResponses({ @ApiResponse(code = 400, message = "Id of group object must match id supplied") })
    public Response updateGroup(@ApiParam(value = "Id of the group", required = true) @PathParam("id") Long id,
                                @ApiParam(value = "Group replacement object", required = true) Group group) {
        if (repository.findOne(id) == null) {
            return notFound();
        }
        if (group == null || !Objects.equal(group.getId(), id)) {
            return badRequest("Id of group object must match id supplied");
        }
        if (group.getId() == null) {
            // make sure we have the proper id set before we update
            group.setId(id);
        }
        repository.save(group);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}")
    public Response partialUpdateGroup(Long id, Group group) {
        // TODO: implement ability to perform a partial update via POST
        return notFound();
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(
    	    value = "Delete a group",
    	    notes = "Delete a group with the specified id"
    )
    public Response deleteGroup(@ApiParam(value = "Id of the group", required = true) @PathParam("id") Long id) {
        Group removedGroup = null;
        if (id != null) {
            removedGroup = repository.delete(id);
        }
        if (removedGroup == null) {
            return notFound();
        } else {
            return Response.noContent().build();
        }
    }

    @GET
    @Path("/{id}/members")
    @ApiOperation(
    	    value = "List members of a group using paging",
    	    notes = "List members of a group using paging and limit results.  Multiple filters and sort options can also be applied.",
    	    response = Group.class,
    	    responseContainer = "List"
    )
    @ApiResponses({ @ApiResponse(code = 404, message = "Group not found by id") })
    public Response getGroupMembers(@ApiParam(value = "Id of the group", required = true) @PathParam("id") Long id, @DefaultValue("1") @QueryParam("page") int page,
                                            @DefaultValue("15") @QueryParam("limit") int limit,
                                            @QueryParam("filter") String filter,
                                            @QueryParam("sort") String sort) {
        if (repository.findOne(id) == null) {
            return notFound("Group not found by id");
        }
        Iterable<Member> members = repository.findMembers(id, page, limit, filter, sort);
        return Response.ok(members).build();
    }

    @GET
    @Path("/{id}/members/{memberId}")
    @ApiOperation(
    	    value = "Get a member by id",
    	    notes = "Get a member by id in the group by id"
    )
    @ApiResponses({ @ApiResponse(code = 404, message = "Invalid group or member id") })
    public Response getGroupMembers(@ApiParam(value = "Id of the group", required = true) @PathParam("id") Long id,
                                    @ApiParam(value = "Id of the member", required = true) @PathParam("memberId") Long memberId) {
        Member member = repository.findMember(id, memberId);
        if (member == null) {
            return notFound("Invalid group or member id");
        }
        return Response.ok(member).build();
    }

    @POST
    @Path("/{id}/members")
    @ApiOperation(
    	    value = "Add a member to a group",
    	    notes = "Add a member to a group by group id"
    )
    @ApiResponses({ @ApiResponse(code = 400, message = "Member is required") })
    public Response addMember(@ApiParam(value = "Id of the group", required = true) @PathParam("id") Long id,
                              @ApiParam(value = "Member object to be added", required = true) Member member) {
        if (member == null) {
            return badRequest("Member is required");
        }
        Member savedMember = repository.saveMember(id, member);
        return Response.created(uriInfo.getAbsolutePathBuilder().path("{id}").build(member.getId())).entity(savedMember).build();
    }

    @PUT
    @Path("/{id}/members/{memberId}")
    @ApiOperation(
    	    value = "Update a member of a group",
    	    notes = "Update a member of a group through replacement with the specified id, the group update must contain all fields"
    )
    @ApiResponses({ @ApiResponse(code = 400, message = "Id of member object must match id supplied") })
    public Response updateMember(@ApiParam(value = "Id of the group", required = true) @PathParam("id") Long id,
                                 @ApiParam(value = "Id of the member", required = true) @PathParam("memberId") Long memberId,
                                 @ApiParam(value = "Member object replacement", required = true) Member member) {
        if (repository.findOne(id) == null) {
            return notFound();
        }
        if (repository.findMember(id, memberId) == null) {
            return notFound();
        }
        if (member == null || !Objects.equal(member.getId(), memberId)) {
            return badRequest();
        }
        if (member.getId() == null) {
            // make sure we have the proper id set before we update
            member.setId(memberId);
        }
        repository.saveMember(id, member);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/members/{memberId}")
    public Response partialUpdateMember(@PathParam("id") Long id, @PathParam("memberId") Long memberId, Member member) {
        // TODO: implement ability to perform a partial update via POST
        return notFound();
    }

    @DELETE
    @Path("/{id}/members/{memberId}")
    @ApiOperation(
    	    value = "Delete a member of a group",
    	    notes = "Delete a member of a group with the specified id"
    )
    public Response deleteMember(@ApiParam(value = "Id of the group", required = true) @PathParam("id") Long id,
                                 @ApiParam(value = "Id of the member", required = true) @PathParam("memberId") Long memberId) {
        Member removedMember = null;
        if (id != null && memberId != null) {
            removedMember = repository.deleteMember(id, memberId);
        }
        if (removedMember == null) {
            return notFound();
        } else {
            return Response.noContent().build();
        }
    }

    private Response notFound() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND).type("text/plain").entity(message).build();
    }

    private Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST).type("text/plain").entity(message).build();
    }



}
