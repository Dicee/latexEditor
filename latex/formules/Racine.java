package latex.formules;

public class Racine extends OperateurBinaire {

	public Racine(Formule op1, Formule op2) {
		super(op1,op2);
	}
	
	public String getLateXCode() {
		return "\\sqrt[" + op2.getLateXCode() + "]{" + op1.getLateXCode() + "}";
	}
}
