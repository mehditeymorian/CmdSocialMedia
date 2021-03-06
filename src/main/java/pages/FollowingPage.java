package main.java.pages;

import main.java.account.Credentials;
import main.java.database.Repository;
import main.java.database.RepositoryFactory;
import main.java.model.Post;
import main.java.model.User;
import main.java.ui.Menu;
import main.java.ui.Printer;

import java.util.ArrayList;
import java.util.Optional;

public class FollowingPage extends Page {
    public static String title = "Following Menu";
    public static String[] menu = {
            "Following Page" ,
            "Add Following" ,
            "Exit"
    };

    public static final int EXIT = 0;
    public static final int SHOW_POSTS = 1;
    public static final int ADD_FOLLOWING = 2;

    private Credentials credentials;
    private Repository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        credentials = Credentials.getInstance();
        repository = RepositoryFactory.getInstance();
    }

    @Override
    public void onStart() {
        followingMenu();
    }

    private void followingMenu() {
        repository.getUserFollowings(credentials.getUserInSystem()).forEach(user -> System.out.println(user.inlinePrintForm()));
        Printer.printLine();
        Menu.printFollowingMenu();
        int choice = getInput().intIn();
        switch (choice) {
            case ADD_FOLLOWING:
                addFollowingMenu();
                break;
            case EXIT:
                getPageManager().addToStack(MainPage.class);
                break;
            default:
                followingProfileMenu(choice);
        }

        if (choice != EXIT) followingMenu();
    }

    private void addFollowingMenu() {
        ArrayList<User> suggestion = repository.getFollowingSuggestionFor(credentials.getUserInSystem());
        suggestion.forEach(user -> System.out.println(user.inlinePrintForm()));

        if (suggestion.isEmpty()) {
            Printer.printERR("No user to follow");
            getInput().pressEnterToContinue();
            return;
        }

        Printer.printLine();
        Menu.printAddFollowingMenu();
        Printer.printERR("**: Enter Zero to Exit");
        addFollowing();
    }

    private void addFollowing() {
        int choice = getInput().intIn();
        if (choice == EXIT) return;

        // follow a user
        boolean success = repository.followUser(credentials.getUserInSystem() , choice);

        if (success) Printer.println("User added to your followings successfully." , Printer.COLOR_GREEN);
        else Printer.printERR("You Followed this user before.");
        addFollowing();
    }

    private void followingProfileMenu(int followingId) {
        Optional<User> user = repository.getUserFollowing(credentials.getUserInSystem().getId() , followingId);

        if (user.isPresent()) {
            Printer.println(user.get().profileForOtherUsers() , Printer.COLOR_YELLOW);
            Printer.printLine();
            followingProfile(user.get());
        } else {
            Printer.printERR("User Is Not your Followings");
        }
    }

    private void followingProfile(User following) {
        Menu.printFollowingProfileMenu();
        int choice = getInput().intIn();

        if (choice == EXIT) return;

        if (choice == SHOW_POSTS)
            followingPostsMenu(following);

        followingProfileMenu(following.getId());
    }

    private void followingPostsMenu(User following) {
        ArrayList<Post> posts = repository.getPostsBy(following.getId());
        posts.forEach(System.out::println);
        if (posts.isEmpty()) {
            Printer.println(String.format("%s has no posts" , following.getName()) , Printer.COLOR_YELLOW);
            getInput().pressEnterToContinue();
        } else {
            Printer.printLine();
            Menu.printUserPostsMenu();
            int choice = getInput().intIn();

            if (choice == EXIT) return;

            Post postById = getPostById(posts , choice);
            followingSinglePostMenu(postById);

            followingPostsMenu(following);
        }

    }

    private void followingSinglePostMenu(Post post) {
        if (post == null) {
            Printer.printERR("No Post Founded");
            getInput().pressEnterToContinue();
            return;
        }

        Printer.println(post.getExpandedDetail() , Printer.COLOR_YELLOW);
        Printer.printLine();
        Menu.printPostDetailMenu();

        String choice = getInput().lineIn();

        if (!choice.toLowerCase().equals("l")) return;

        boolean success = repository.likePostById(post.getId());
        if (success) Printer.println("Post Liked Successfully" , Printer.COLOR_GREEN);
        else Printer.printERR("Failed to Like Post!");
    }

    private Post getPostById(ArrayList<Post> posts , int postId) {
        return posts.stream().filter(post -> post.getId() == postId).findFirst().orElse(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        credentials = null;
        repository = null;
    }

}
