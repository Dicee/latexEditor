package latex.formules;


public class Equation extends FormuleComposee {

	public Equation(Formule op1, Formule op2) {
		super(new Egalite(op1,op2));
	}
	
	public Equation(int op1, Formule op2) {
		this(new Constante(op1),op2);
	}
	
	public Equation(Formule op1, int op2) {
		this(op1,new Constante(op2));
	}
	
	public Equation(int op1, int op2) {
		this(new Constante(op1),new Constante(op2));
	}
}
