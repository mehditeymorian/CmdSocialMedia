package main.java.database;

import lombok.SneakyThrows;
import main.java.io.PostRaf;
import main.java.io.UserRaf;
import main.java.io.UserRelationRaf;
import main.java.model.Post;
import main.java.model.User;
import main.java.model.UserRelation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static main.java.io.UserRelationRaf.FOLLOWER;
import static main.java.io.UserRelationRaf.FOLLOWING;

public final class FileRepository extends Repository {
    static Repository INSTANCE;
    private final PostRaf postRaf;
    private final UserRaf userRaf;
    private final UserRelationRaf userRelationRaf;


    FileRepository() {
        postRaf = new PostRaf();
        userRaf = new UserRaf();
        userRelationRaf = new UserRelationRaf();
    }

    static Repository getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FileRepository();

        return INSTANCE;
    }

    //region Post methods
    @Override
    public ArrayList<Post> getPostsBy(int userId) {
        try {
            return postRaf.getPostsByUser(userId);
        } catch (IOException e) {
            // TODO: 6/19/2020 log error
            return new ArrayList<>();
        }
    }

    @Override
    public boolean addPost( Post post) {
        try {
            postRaf.add(post);
            return true;
        } catch (Exception exception) {
            // TODO: 6/19/2020 log error
            return false;
        }
    }

    @Override
    public Post getPostById(int id) {
        try {
            return postRaf.getPostById(id);
        } catch (IOException e) {
            // TODO: 6/19/2020 log error
            return null;
        }
    }

    @Override
    public boolean likePostById(int postId) {
        // TODO: 7/17/2020 this need another RAF class to save who likes which posts for distinct number of likes.
        try {
            postRaf.likePostById(postId);
            return true;
        } catch (Exception exception) {
            // TODO: 6/19/2020 log error
            return false;
        }
    }

    //endregion

    //region User methods
    @SneakyThrows
    @Override
    public void addUser(User user) {
        userRaf.add(user);
    }

    @Override
    public Optional<User> getUserByUserPass(String username , String password) {
        return getAllUsers().stream()
                .filter(each -> each.getUsername().equals(username))
                .filter(each -> each.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public ArrayList<User> getAllUsers() {
        try {
            return userRaf.getAllUsers(postRaf , userRelationRaf);
        } catch (IOException e) {
            // TODO: 6/19/2020 log error
            return new ArrayList<>();
        }
    }

    @Override
    public ArrayList<User> getFollowingSuggestionFor(User user) {
        return getAllUsers().stream()
                .filter(each -> each.getId() != user.getId())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ArrayList<User> getUserFollowings(User user) {
        try {
            List<Integer> userFollowingsIds = userRelationRaf.getUserFollowingsIds(user.getId());
            return userRaf.getUserFromList(userFollowingsIds , postRaf , userRelationRaf);
        } catch (IOException e) {
            // TODO: 6/19/2020 log error
            return new ArrayList<>();
        }
    }


    @Override
    public ArrayList<User> getUserFollowers(User user) {
        try {
            List<Integer> userFollowersIds = userRelationRaf.getUserFollowersIds(user.getId());
            return userRaf.getUserFromList(userFollowersIds,postRaf, userRelationRaf);
        } catch (IOException e) {
            // TODO: 6/19/2020 log error
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<User> getUserFollowing(int userId , int secondUserId) {
        Optional<User> result = Optional.empty();
        try {
            // return null if doesnt have a relation
            if (userRelationRaf.haveThisRelation(userId , secondUserId , FOLLOWING))
                result = userRaf.getUserById(secondUserId , postRaf , userRelationRaf);
        } catch (IOException e) {
            // TODO: 6/19/2020 log error
        }

        return result;
    }

    @Override
    public Optional<User> getUserFollower(int userId , int followerId) {
        Optional<User> result = Optional.empty();

        try {
            // return null if doesnt have a relation
            if (userRelationRaf.haveThisRelation(userId , followerId , FOLLOWER))
                result = userRaf.getUserById(followerId , postRaf , userRelationRaf);

        } catch (IOException e) {
            // TODO: 6/19/2020 log error
        }

        return result;
    }

    @Override
    public boolean followUser(User currentUser , int secondUserId) {
        boolean result = false;
        try {
            // current user become a following of <<secondUserId>>
            if (!userRelationRaf.haveThisRelation(currentUser.getId() , secondUserId , FOLLOWING)) {
                userRelationRaf.add(new UserRelation(-1 , secondUserId , currentUser.getId()));
                result = true;
            }

        } catch (IOException e) {
            // TODO: 6/19/2020 log error
        }

        return result;
    }

    //endregion

    @SneakyThrows
    @Override
    public void closeResources() {
        userRaf.close();
        postRaf.close();
        userRelationRaf.close();
    }

}
