package spiderdrawer.shape.containers;

import java.util.ArrayList;

public class MultiContainer<T,S> extends Container<T,S> {

	ArrayList<T> list;
	ArrayList<Container<S,T>> containerList;
	
	public MultiContainer(S parent) {
		super(parent);
		list = new ArrayList<T>();
		containerList = new ArrayList<Container<S,T>>(); 
	}
	
	public void add(T t, Container<S,T> c) {
		list.add(t);
		containerList.add(c);
		if (t != null) {
			if (c instanceof SingleContainer) {
				SingleContainer<S,T> singleContainer = (SingleContainer<S, T>) c;
				if (!parent.equals(singleContainer.get()))
					singleContainer.set(parent, this);
			} else {
				MultiContainer<S,T> multiContainer = (MultiContainer<S, T>) c;
				if (!multiContainer.contains(parent))
					multiContainer.add(parent, this);
			}
		}		
	}
	
	public void remove(T t) {
		int index = list.indexOf(t);
		if (index != -1) {
			Container<S,T> c = containerList.get(index);
			list.remove(index);
			containerList.remove(index);
			if (t != null) {
				if (c instanceof SingleContainer) {
					SingleContainer<S,T> singleContainer = (SingleContainer<S, T>) c;
					if (parent.equals(singleContainer.get()))
						singleContainer.set(null, this);
				} else {
					MultiContainer<S,T> multiContainer = (MultiContainer<S, T>) c;
					if (multiContainer.contains(parent))
						multiContainer.remove(parent);
				}
			}
		}
	}
	
	public boolean contains(T t) {
		return list.contains(t);
	}
	
	public void removeAll() {
		while (list.size() > 0)
			remove(list.get(0));
	}
	
	public T get(int index) {
		return list.get(index);
	}
	
	public boolean containsAll(MultiContainer<T,S> mc) {
		return list.containsAll(mc.list);
	}
	
	public boolean isEmpty() {
		return (list.size() == 0);
	}
	
	public int size() {
		return list.size();
	}
	
}
