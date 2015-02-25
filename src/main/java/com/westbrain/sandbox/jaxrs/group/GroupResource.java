package com.westbrain.sandbox.jaxrs.group;

import com.google.common.base.Objects;
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
public class GroupResource {

    @Autowired
    private GroupRepository repository;

    @Context
    private Response response;
    @Context
    private UriInfo uriInfo;

    public static int DEFAULT_GROUP_LIMIT = 5;

    @GET
    public Response getGroups(@DefaultValue("1") @QueryParam("page") int page,
                                        @DefaultValue("10") @QueryParam("limit") int limit,
                                        @QueryParam("filter") String filter,
                                        @QueryParam("sort") String sort) {
        if (page <= 0) {
            return badRequest();
        }

        Iterable<Group> result = repository.findGroups(page, limit, filter, sort);

        return Response.ok(result).build();
    }


    @GET
    @Path("/{id}")
    public Response getGroupById(@PathParam("id") Long id) {
        Group group = repository.findOne(id);
        if (group == null) {
            return notFound();
        }
        return Response.ok(group).build();
    }

    @POST
    public Response createGroup(Group group) {
        if (group == null) {
            return badRequest();
        }
        Group savedGroup = repository.save(group);
        return Response.created(uriInfo.getAbsolutePathBuilder().path("{id}").build(savedGroup.getId())).entity(savedGroup).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateGroup(@PathParam("id") Long id, Group group) {
        if (repository.findOne(id) == null) {
            return notFound();
        }
        if (group == null || !Objects.equal(group.getId(), id)) {
            return badRequest();
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
    public Response partialUpdateGroup(@PathParam("id") Long id, Group group) {
        // TODO: implement ability to perform a partial update via POST
        return notFound();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteGroup(@PathParam("id") Long id) {
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
    public Response getGroupMembers(@PathParam("id") Long id, @DefaultValue("1") @QueryParam("page") int page,
                                            @DefaultValue("15") @QueryParam("limit") int limit,
                                            @QueryParam("filter") String filter,
                                            @QueryParam("sort") String sort) {
        if (repository.findOne(id) == null) {
            return notFound();
        }
        Iterable<Member> members = repository.findMembers(id, page, limit, filter, sort);
        return Response.ok(members).build();
    }

    @GET
    @Path("/{id}/members/{memberId}")
    public Response getGroupMembers(@PathParam("id") Long id, @PathParam("memberId") Long memberId) {
        Member member = repository.findMember(id, memberId);
        if (member == null) {
            return notFound();
        }
        return Response.ok(member).build();
    }

    @POST
    @Path("/{id}/members")
    public Response addMember(@PathParam("id") Long id, Member member) {
        if (member == null) {
            return badRequest();
        }
        Member savedMember = repository.saveMember(id, member);
        return Response.created(uriInfo.getAbsolutePathBuilder().path("{id}").build(member.getId())).entity(savedMember).build();
    }

    @PUT
    @Path("/{id}/members/{memberId}")
    public Response updateMember(@PathParam("id") Long id, @PathParam("memberId") Long memberId, Member member) {
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
    public Response deleteMember(@PathParam("id") Long id, @PathParam("memberId") Long memberId) {
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

    private Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }



}
