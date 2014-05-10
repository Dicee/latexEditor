package latex.formules;

public class OperatorFactory {
	
	public Racine createRacine(Formule f1, Formule f2) {
		return new Racine(f1,f2);
	}
	
	public Plus createPlus(Formule f1, Formule f2) {
		return new Plus(f1,f2);
	}
	
	public Moins createMoins(Formule f1, Formule f2) {
		return new Moins(f1,f2);
	}
	
	public  Produit createProduit(Formule f1, Formule f2) {
		return new Produit(f1,f2);
	}
	
	public Quotient createQuotient(Formule f1, Formule f2) {
		return new Quotient(f1,f2);
	}
	
	public  Exposant createExposant(Formule f1, Formule f2) {
		return new Exposant(f1,f2);
	}

	public Indice createIndice(Formule f1, Formule f2) {
		return new Indice(f1,f2);
	}
	
	public OperateurUnaire createOperateurUnaire(String name, Formule f) {
		switch (name) {
			default       : return null;
		}
	}
	
	public OperateurBinaire createOperateurBinaire(String name, Formule f1, Formule f2) {
		switch (name) {
			case "+"        : return createPlus(f1,f2);
			case "-"        : return createMoins(f1,f2);
			case "*"        : return createProduit(f1,f2);
			case "/"        : return createQuotient(f1,f2);
			case "Exposant" : return createExposant(f1,f2);
			case "Indice"   : return createIndice(f1,f2);
			case "Racine"   : return createRacine(f1,f2);
			default         : return null;
		}
	}
}
