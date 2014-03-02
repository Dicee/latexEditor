package utils;


/**Classe reprensentant un couple de deux objets de n'importe quel type chacun.
 * @author David Courtinot
 */

public class Couple<A,B>
{
	protected A a;
	protected B b;
	
	/**Construire un couple a partir de deux elements de type A et B.
	 * @param a objet de type A
	 * @param b objet de type B
	 */
	public Couple(A a, B b)
	{
		this.a = a;
		this.b = b;
	}
	
	/**Obtenir la valeur du premier element du couple.
	 * @return a si le couple est (a,b)
	 */
	public A getA()
	{
		return a;
	}
	
	/**Obtenir la valeur du deuxieme element du couple.
	 * @return b si le couple est (a,b)
	 */
	public B getB()
	{
		return b;
	}
	
	/**Fixer la valeur du premier element du couple.
	 * @param a nouvelle valeur
	 */
	public void setA(A a)
	{
		this.a = a;
	}
	
	/**Fixer la valeur du deuxieme element du couple.
	 * @param b nouvelle valeur
	 */
	public void setB(B b)
	{
		this.b = b;
	}
	
	/**Fournir une representation textuelle du Couple.
	 * @return representation textuelle du Couple
	 */
	public String toString()
	{
		return "Couple(" + a + "," + b + ")";
	}
	
	/**Cloner l'instance.
	 * @return clone de l'instance
	 */
	public Couple<A,B> clone() {
		return new Couple<A,B>(a,b);
	}
}
