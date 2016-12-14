package it.cnr.ilc.saussure;

public class OntoResult {
	private String termine;
	private String relazione;
	private String tratto;
	private String classe;
	private String classe_target;
	private String termine_target;
	private String valore;
	private String tipo;
	private String definition;
	private String inferita = "non";
	
	public OntoResult() {
		
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getRelazione() {
		return relazione;
	}

	public void setRelazione(String relazione) {
		this.relazione = relazione;
	}

	public String getTratto() {
		return tratto;
	}

	public void setTratto(String tratto) {
		this.tratto = tratto;
	}

	public String getClasse() {
		return classe;
	}

	public void setClasse(String classe) {
		this.classe = classe;
	}

	public String getValore() {
		return valore;
	}

	public void setValore(String valore) {
		this.valore = valore;
	}

	public String getTermine() {
		return termine;
	}

	public void setTermine(String termine) {
		this.termine = termine;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getClasse_target() {
		return classe_target;
	}

	public void setClasse_target(String classe_target) {
		this.classe_target = classe_target;
	}

	public String getTermine_target() {
		return termine_target;
	}

	public void setTermine_target(String termine_target) {
		this.termine_target = termine_target;
	}

	public String getInferita() {
		return inferita;
	}

	public void setInferita(String inferita) {
		this.inferita = inferita;
	}	
	
}