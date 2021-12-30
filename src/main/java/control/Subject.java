package main.java.control;

public interface Subject {
	public void register(Observer o);
	public void remove(Observer o);
	public void notifyObservers();
}
