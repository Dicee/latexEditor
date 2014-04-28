package utils;

import java.util.Iterator;
import java.util.List;
import javafx.util.Pair;

/**
 *
 * @author David Courtinot
 * @param <E>
 * @param <F>
 */
public class ParallelList<E,F> implements Iterable<Pair<E,F>> {

	private final List<E> listE;
	private final List<F> listF;
	
	public ParallelList(List<E> listE, List<F> listF) {
		this.listE = listE;
		this.listF = listF;
	}
	
	@Override
	public Iterator<Pair<E,F>> iterator() {
		return new Iterator<Pair<E,F>>() {
			private final Iterator<E> itE = listE.iterator();
			private final Iterator<F> itF = listF.iterator();
					
			@Override
			public boolean hasNext() {
				return itE.hasNext() && itF.hasNext();
			}

			@Override
			public Pair<E,F> next() {
				return new Pair<>(itE.next(),itF.next());
			}
		};
	}
}
