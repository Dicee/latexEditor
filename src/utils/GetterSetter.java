package utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class GetterSetter<T> {
	private final Supplier<T>	getter;
	private final Consumer<T>	setter;

	public GetterSetter(Supplier<T> getter, Consumer<T> setter) {
		this.getter = getter;
		this.setter = setter;
	}

	public T get() {
		return getter.get();
	}

	public void set(T t) {
		setter.accept(t);
	}
}