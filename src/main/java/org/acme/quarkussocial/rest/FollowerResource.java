package org.acme.quarkussocial.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.quarkussocial.domain.model.Follower;
import org.acme.quarkussocial.domain.repository.FollowerRepository;
import org.acme.quarkussocial.domain.repository.UserRepository;
import org.acme.quarkussocial.rest.dto.FollowerRequest;
import org.acme.quarkussocial.rest.dto.FollowerResponse;
import org.acme.quarkussocial.rest.dto.FollowersPerUserResponse;

import java.util.stream.Collectors;


@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private final FollowerRepository repository;
    private final UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository repository ,UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }
    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId , FollowerRequest request){
        if(userId.equals(request.getFollowerId())){
            return Response.status(Response.Status.CONFLICT).entity("You can't follow yourself").build();
        }
        var user = userRepository.findById(userId);
        var follower = userRepository.findById(request.getFollowerId());

        if(user == null || follower == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean follows = repository.follows(follower, user);

        if(!follows) {
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);
            repository.persist(entity);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
    @GET
    public Response listFollowers(@PathParam("userId") Long userId){

        var user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var list = repository.findByUser(userId);
        FollowersPerUserResponse responseObject = new FollowersPerUserResponse();
        responseObject.setFollowersCount(list.size());

        var followersList = list.stream()
                .map(FollowerResponse::new).collect(Collectors.toList());
        responseObject.setContent(followersList);

        return Response.ok(responseObject).build();
    }
    @DELETE
    @Transactional
    public Response unfollowUser(
            @PathParam("userId") Long userId,
            @QueryParam("followerId") Long followerId){
        var user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        repository.deleteByFollowerAndUser(followerId,userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}