package latex.formules;


public abstract class OperateurUnaire extends Operateur {
	
	protected Formule op;
	
	public OperateurUnaire(Formule op) {
		this.op  = op;
	}
}
