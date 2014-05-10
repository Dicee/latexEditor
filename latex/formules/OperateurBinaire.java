package latex.formules;

public abstract class OperateurBinaire extends Operateur {
	protected Formule op1, op2;
	
	public OperateurBinaire(Formule op1, Formule op2) {
		this.op1  = op1;
		this.op2  = op2;
	}	
	
	public OperateurBinaire(Formule op1, int op2) {
		this(op1,new Constante(op2));
	}
	
	public OperateurBinaire(int op1, int op2) {
		this(new Constante(op1),new Constante(op2));
	}
	
	public OperateurBinaire(int op1, Formule op2) {
		this(new Constante(op1),op2);
	}
}
