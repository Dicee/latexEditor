package guifx.utils;

import java.util.List;
import javafx.util.Pair;

public class NamedList<E> extends Pair<List<String>,List<E>> {
		private static final long serialVersionUID = 1L;
		public NamedList(List<String> a, List<E> b) { super(a,b); }
	}