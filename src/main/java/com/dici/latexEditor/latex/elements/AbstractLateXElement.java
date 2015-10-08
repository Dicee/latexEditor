package com.dici.latexEditor.latex.elements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import com.dici.latexEditor.latex.Textifiable;

public abstract class AbstractLateXElement implements LateXElement {
	protected String						content;
	protected String						name;
	private java.util.List<LateXElement>	children	= new ChildrenList();
	private int								depth;

	public AbstractLateXElement(String content, String name, int depth) {
		this.content = content;
		this.name    = name;
		this.depth   = depth;
	}
	
	public abstract LateXElement clone();
	public java.util.List<LateXElement> getChildren() { return children; }

	@Override public       String  textify()               { return getType() + " ##\n" + getText() + "\n##"; }
	@Override public       String  getType()               { return name                                    ; }
	@Override public       String  getText()               { return content                                 ; }
	@Override public       void    setText(String content) { this.content = content                         ; }
	@Override public       String  toString()              { return name                                    ; }
	@Override public       int     getDepth()              { return depth                                   ; }
	@Override public final int     hashCode()              { return Objects.hash(name, content)             ; }
	@Override public final boolean equals(Object o)        {
	    if (this == o) return true;
	    if (!(o instanceof Textifiable)) return false;
	    Textifiable that = (Textifiable) o;
	    return Objects.equals(getType(), that.getType()) && Objects.equals(getText(), that.getText());
	}

	private class ChildrenList extends ArrayList<LateXElement> {
		private static final long serialVersionUID = 1L;
		
		private void checkArgDepth(LateXElement elt) {
			if (elt.getDepth() <= depth) 
			    throw new IllegalArgumentException(String.format("%s cannot be a child of %s",elt,AbstractLateXElement.this));
		}
		
		private void checkArgsDepth(Collection<? extends LateXElement> elts) {
			for (LateXElement elt : elts) checkArgDepth(elt);
		}
		
		@Override
		public void add(int i, LateXElement elt) {
			checkArgDepth(elt);
			super.add(i,elt);
		}
		
		@Override
		public boolean add(LateXElement elt) {
			checkArgDepth(elt);
			return super.add(elt);
		}
		
		@Override
		public boolean addAll(Collection<? extends LateXElement> elts) {
			checkArgsDepth(elts);
			return super.addAll(elts);
		}
		
		@Override
		public boolean addAll(int index, Collection<? extends LateXElement> elts) {
			checkArgsDepth(elts);
			return super.addAll(index,elts);
		}
	}
}