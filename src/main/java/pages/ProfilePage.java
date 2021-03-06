package main.java.pages;

import main.java.account.Credentials;
import main.java.ui.Printer;

public class ProfilePage extends Page {
    private Credentials credentials;

    @Override
    public void onCreate() {
        super.onCreate();
        credentials = Credentials.getInstance();
    }

    @Override
    public void onStart() {
        Printer.println(credentials.getUserInSystem().toString(),Printer.COLOR_YELLOW);
        getInput().pressEnterToContinue();
        getPageManager().addToStack(MainPage.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        credentials = null;
    }
}
