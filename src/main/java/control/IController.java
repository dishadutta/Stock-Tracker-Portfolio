package main.java.control;

import main.java.utility.Screen;

public interface IController {
    public abstract void switchScreen(Screen target, String title, String url);
	public abstract void makeNewStage(Screen target, String title, String url);
}
