package main.java.dao;

public interface IManager<T> {
	public boolean add(T obj);
	
	public boolean remove(T obj);
	
	public boolean update(T obj);
}
