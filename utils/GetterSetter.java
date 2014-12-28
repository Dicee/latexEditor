package utils;

public class GetterSetter<T> implements Getter<T>, Setter<T> {
	private final Getter<T>	getter;
	private final Setter<T>	setter;

	public GetterSetter(Getter<T> getter, Setter<T> setter) {
		this.getter = getter;
		this.setter = setter;
	}

	public T get() {
		return getter.get();
	}

	public void set(T t) {
		setter.set(t);
	}
}