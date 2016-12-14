package it.cnr.ilc.saussure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;

@Named
@SessionScoped
public class Saussure implements Serializable {

	@Inject
	private transient SaussureModel saussureModel;

	private static final long serialVersionUID = 1L;
	private static final String NS = "http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#";

	private final static List<String> VALID_COLUMN_KEYS = Arrays.asList(
			"termine", "relazione", "tratto", "classe", "valore",
			"classe_target", "termine_target", "inferita");
	private String columnTemplate_1_relazione = "termine classe relazione termine_target classe_target inferita";
	// private String columnTemplate_1_relazione =
	// "relazione termine classe tipo";
	private String columnTemplate_1_tratto = "tratto valore inferita";
	private String columnTemplate_2 = "termine classe inferita";
	// private String columnTemplate_3 = "termine classe tipo";
	private String columnTemplate_3 = "termine classe relazione termine_target classe_target inferita";
	private String columnTemplate_4 = "termine classe inferita";

	private static Map<String, String> instanceMap;
	private static Map<String, String> attributeMap;
	private static Map<String, String> valueMap;
	private static Map<String, String> objRelationMap;
	private static Map<String, String> objRelation_2Map;
	private static Map<String, String> instance_2Map;
	private static Map<String, String> instance_3Map;
	private static Map<String, String> attribute_2Map;
	private static Map<String, String> value_2Map;
	private static Map<String, String> subject_Map;
	
	private static ArrayList<String> relationAlternativeMap = new ArrayList<String>();
	private static ArrayList<String> valuesOfRelationAlternativeMap = new ArrayList<String>();
	private static ArrayList<String> trattoAlternativeMap = new ArrayList<String>();
	private static ArrayList<String> valuesOfTrattoAlternativeMap = new ArrayList<String>();
	
	private static Map<String, Integer> tagCloudMap;
	
	private static ArrayList<String> constraint_1 = new ArrayList<String>();
	private static ArrayList<String> constraint_2 = new ArrayList<String>();
	
	private static ArrayList<String> tagCloudOptions = new ArrayList<String>();
	
	private static List<OntoResult> res = new ArrayList<OntoResult>();
	private static List<OntoResult> clearRes = new ArrayList<OntoResult>();

	private List<ColumnModel> columns = new ArrayList<ColumnModel>();

	private String query_1_param_1 = "ObjectProperty";
	private String query_1_param_2 = "";

	private String query_2_param_1 = "sémantique";
	private String query_2_param_2 = "Abstract";
	private String query_2_param_3 = "hapax";

	private String query_3_param_1 = "Lautphysiologie";
	private String query_3_param_2 = "";

	private String query_4_param_1 = "affects";
	private String query_4_param_2 = "consonne";
	private String query_4_param_3 = "PoS";
	private String query_4_param_4 = "noun";

	private String query_4_param_5 = "";
	private String query_4_param_6 = "";
	private String query_4_param_7 = "";
	private String query_4_param_8 = "";
	
	private String opt_Cloud = "Synonymie";

	private boolean disabled = false;
	private boolean addRelationButton = true;
	private boolean addTraitButton = true;
	
	private boolean addRelationForm = false;
	private boolean addTraitForm = false;
	
	private boolean printButton = false;

	// 1-> query 1
	// 2-> query 2
	// 3-> query 3
	// 4-> query 4
	// 5-> query 4 con vincolo relazione
	// 6-> query 4 con vincolo tratto
	// 7-> query 4 con tutti e due i vincoli
	private int currentQuery = 0;
	
	private boolean relationConstraint = false;
	private boolean trattoConstraint = false;

	private TagCloudModel model = null;

	static {
		instanceMap = new LinkedHashMap<String, String>();
		attributeMap = new LinkedHashMap<String, String>();
		valueMap = new LinkedHashMap<String, String>();
		objRelationMap = new LinkedHashMap<String, String>();
		objRelation_2Map = new LinkedHashMap<String, String>();
		instance_2Map = new LinkedHashMap<String, String>();
		subject_Map = new LinkedHashMap<String, String>();
		instance_3Map = new LinkedHashMap<String, String>();
		value_2Map = new LinkedHashMap<String, String>();
		attribute_2Map = new LinkedHashMap<String, String>();
		tagCloudMap = new LinkedHashMap<String, Integer>();
	}

	@SuppressWarnings("serial")
	static public class ColumnModel implements Serializable {

		private String header;
		private String property;

		public ColumnModel(String header, String property) {
			this.header = header;
			this.property = property;
		}

		public String getHeader() {
			return header;
		}

		public String getProperty() {
			return property;
		}
	}

	public List<ColumnModel> getColumns() {
		return columns;
	}

	public void createDynamicColumns(String template) {
		String[] columnKeys = template.split(" ");
		columns.clear();
		for (String columnKey : columnKeys) {
			String key = columnKey.trim();
			if (VALID_COLUMN_KEYS.contains(key)) {
				columns.add(new ColumnModel(getColumnTitle(columnKey),
						columnKey));
			}
		}
	}

	private String getColumnTitle(String col) {
		if (col.equals("termine"))
			return "TERME SOURCE";
		else if (col.equals("classe"))
			return "TYPE ONTOLOGIQUE";
		else if (col.equals("inferita"))
			return "INFERENCE";
		else if (col.equals("relazione"))
			return "RELATION";
		else if (col.equals("termine_target"))
			return "TERME CIBLE";
		else if (col.equals("classe_target"))
			return "TYPE ONTOLOGIQUE";
		else if (col.equals("tratto"))
			return "TRAIT";
		else
			return "VALEUR";
	}

	public void query_1_propertyChanged(ValueChangeEvent e) {
		query_1_param_1 = e.getNewValue().toString();
	}

	public String getQuery_1_param_1() {
		return query_1_param_1;
	}

	public void setQuery_1_param_1(String query_1_param_1) {
		this.query_1_param_1 = query_1_param_1;
	}

	public Map<String, String> getInstances() {
		return instanceMap;
	}

	public String getQuery_1_param_2() {
		return query_1_param_2;
	}

	public void setQuery_1_param_2(String query_1_param_2) {
		this.query_1_param_2 = query_1_param_2;
	}

	public void query_2_propertyChanged(ValueChangeEvent e) {
		query_2_param_1 = e.getNewValue().toString();
		if (query_2_param_1.equals("SemanticProperty"))
			query_2_param_2 = "Abstract";
		else if (query_2_param_1.equals("MorphosyntacticProperty"))
			query_2_param_2 = "PoS";
		else if (query_2_param_1.equals("TermUsageInformationProperty"))
			query_2_param_2 = "AttestationPeriod";
	}

	public String getQuery_2_param_1() {
		return query_2_param_1;
	}

	public void setQuery_2_param_1(String query_2_param_1) {
		this.query_2_param_1 = query_2_param_1;
	}

	public Map<String, String> getAttributes() {
		return attributeMap;
	}

	public String getQuery_2_param_2() {
		return query_2_param_2;
	}

	public void setQuery_2_param_2(String query_2_param_2) {
		this.query_2_param_2 = query_2_param_2;
	}

	public String getQuery_2_param_3() {
		return query_2_param_3;
	}

	public void setQuery_2_param_3(String query_2_param_3) {
		this.query_2_param_3 = query_2_param_3;
	}

	public void query_2_attributeChanged(ValueChangeEvent e) {
		query_2_param_2 = e.getNewValue().toString();
	}

	public Map<String, String> getValues() {
		return valueMap;
	}

	public void query_3_instanceChanged(ValueChangeEvent e) {
		query_3_param_1 = e.getNewValue().toString();
	}

	public String getQuery_3_param_1() {
		return query_3_param_1;
	}

	public void setQuery_3_param_1(String query_3_param_1) {
		this.query_3_param_1 = query_3_param_1;
	}

	public Map<String, String> getInstances_2() {
		return instance_2Map;
	}

	public Map<String, String> getObjRelations() {
		return objRelationMap;
	}

	public String getQuery_3_param_2() {
		return query_3_param_2;
	}

	public void setQuery_3_param_2(String query_3_param_2) {
		this.query_3_param_2 = query_3_param_2;
	}

	public String getQuery_4_param_1() {
		return query_4_param_1;
	}

	public void setQuery_4_param_1(String query_4_param_1) {
		this.query_4_param_1 = query_4_param_1;
	}

	public String getQuery_4_param_2() {
		return query_4_param_2;
	}

	public void setQuery_4_param_2(String query_4_param_2) {
		this.query_4_param_2 = query_4_param_2;
	}

	public String getQuery_4_param_3() {
		return query_4_param_3;
	}

	public void setQuery_4_param_3(String query_4_param_3) {
		this.query_4_param_3 = query_4_param_3;
	}

	public String getQuery_4_param_4() {
		return query_4_param_4;
	}

	public void setQuery_4_param_4(String query_4_param_4) {
		this.query_4_param_4 = query_4_param_4;
	}

	public void query_4_trattoChanged(ValueChangeEvent e) {
		query_4_param_3 = e.getNewValue().toString();
	}

	public void query_4_valueChanged(ValueChangeEvent e) {
		query_4_param_2 = e.getNewValue().toString();
	}

	public void query_4_propertyChanged(ValueChangeEvent e) {
		query_4_param_1 = e.getNewValue().toString();
	}


	//***********************************************************************//
	
	public void query_4_propertyChanged_2(ValueChangeEvent e) {
//		System.out.println("CAMBIO VALORE");
		query_4_param_5 = e.getNewValue().toString();
	}

	public String getQuery_4_param_5() {
		return query_4_param_5;
	}

	public void setQuery_4_param_5(String query_4_param_5) {
		this.query_4_param_5 = query_4_param_5;
	}
	
	public void query_4_valueOfProperty_2Changed(ValueChangeEvent e) {
		query_4_param_6 = e.getNewValue().toString();
//		System.out.println("valore della relazione: " + query_4_param_6);
	}
	
	public String getQuery_4_param_6() {
		return query_4_param_6;
	}

	public void setQuery_4_param_6(String query_4_param_6) {
		this.query_4_param_6 = query_4_param_6;
	}
	
	public void query_4_trattoChanged_2(ValueChangeEvent e) {
		query_4_param_7 = e.getNewValue().toString();
	}

	public void opt_Cloud_Changed(ValueChangeEvent e) {
		opt_Cloud = e.getNewValue().toString();
	}
	
	public String getQuery_4_param_7() {
		return query_4_param_7;
	}

	public void setQuery_4_param_7(String query_4_param_7) {
		this.query_4_param_7 = query_4_param_7;
	}
	
	public void query_4_valueOfTratto_2Changed(ValueChangeEvent e) {
		query_4_param_8 = e.getNewValue().toString();
	}
	
	public String getQuery_4_param_8() {
		return query_4_param_8;
	}

	public void setQuery_4_param_8(String query_4_param_8) {
		this.query_4_param_8 = query_4_param_8;
	}
	
	//***********************************************************************//
	

	public Map<String, String> getAttribute_2() {
		return attribute_2Map;
	}
	
	public Map<String, String> getValue_2() {
		return value_2Map;
	}

	public Map<String, String> getInstances_3() {
		return instance_3Map;
	}
	
	public ArrayList<String> getRelationAlternativeMap() {
		return relationAlternativeMap;
	}
	
	public ArrayList<String> getValuesOfRelationAlternativeMap() {
		return valuesOfRelationAlternativeMap;
	}
	
	public ArrayList<String> getTrattoAlternativeMap() {
		return trattoAlternativeMap;
	}
	
	public ArrayList<String> getValuesOfTrattoAlternativeMap() {
		return valuesOfTrattoAlternativeMap;
	}
	
	public Map<String, String> getObjRelations_2() {
		return objRelation_2Map;
	}
	
	public List<OntoResult> getResults() {
		return res;
	}
	
	private boolean inferred(OntoResult e, int n) {
		// 1: query 1 con object properties / query 3
		// 2: query 1 con data properties 
		// 3: query 2 e 4
		switch (n) {
		case 1: 
			for (OntoResult or : clearRes) {
				if ((or.getTermine().equals(e.getTermine())) && ((or.getTermine_target().equals(e.getTermine_target()))) 
						&& ((or.getRelazione().equals(e.getRelazione())))) {
					return false;
				}
			}
		break;
		case 2: 
			for (OntoResult or : clearRes) {
				if ((or.getValore().equals(e.getValore())) && ((or.getTratto().equals(e.getTratto())))) {
					return false;
				}
			}
		break;
		case 3: 
			for (OntoResult or : clearRes) {
				if ((or.getTermine().equals(e.getTermine()))) {
					return false;
				}
			}
		break;
		}
		return true;
		
	}

	public void runQuery_1() throws IOException {
		setCurrentQuery(1);
		if (!isPrintButton())
			setPrintButton(!isPrintButton());
		FacesContext context = FacesContext.getCurrentInstance();
		res.clear();
		clearRes.clear();
		columns.clear();
		String prop, term;
		if (query_1_param_1.equals("ObjectProperty")) {
			String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT DISTINCT ?property ?termine ?definition "
					+ "WHERE { saussure:"
					+ query_1_param_2
					+ " ?property ?termine . ?property rdf:type owl:"
					+ query_1_param_1
					+ " ."
					+ " ?termine saussure:Definition ?definition . } "
					+ "ORDER BY ?termine ?property ";
			Query query = QueryFactory.create(queryString);
			QueryExecution qe = QueryExecutionFactory.create(query,
					saussureModel.getModel());
			for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
				QuerySolution binding = rs.nextSolution();
				prop = binding.get("property").toString().split("#")[1];

				if ((!prop.equals("agentive"))
						&& (!prop.equals("constitutive"))
						&& (!prop.equals("derivational"))
						&& (!prop.equals("formal"))
						&& (!prop.equals("hasLocation"))
						&& (!prop.equals("telic"))) {

					OntoResult ontoRes = new OntoResult();
					ontoRes.setTermine(query_1_param_2);
					ontoRes.setRelazione(prop);
					ontoRes.setTermine_target(binding.get("termine").toString()
							.split("#")[1]);
					ontoRes.setDefinition(binding.get("definition").toString());
					Individual dd = saussureModel.getModel().getIndividual(
							binding.get("termine").toString());
					for (ExtendedIterator i = dd.listOntClasses(true); i
							.hasNext();) {
						OntClass cls = (OntClass) i.next();
						if (cls.getNameSpace() != null) {
							if (cls.getNameSpace().equals(NS))
								ontoRes.setClasse_target(cls.toString().split(
										"#")[1]);
						}
					}

					Individual dd2 = saussureModel.getModel().getIndividual(
							NS + query_1_param_2);
					for (ExtendedIterator i = dd2.listOntClasses(true); i
							.hasNext();) {
						OntClass cls = (OntClass) i.next();
						if (cls.getNameSpace() != null) {
							if (cls.getNameSpace().equals(NS))
								ontoRes.setClasse(cls.toString().split("#")[1]);
						}
					}

					res.add(ontoRes);

				}
			}
			
			
			// verifica delle triple inferite tramite interrogazione al modello semplice
			Query query2 = QueryFactory.create(queryString);
			QueryExecution qe2 = QueryExecutionFactory.create(query2,
					saussureModel.getClearModel());
			for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
				QuerySolution binding = rs2.nextSolution();
				prop = binding.get("property").toString().split("#")[1];
				term = binding.get("termine").toString().split("#")[1];
				if ((!prop.equals("agentive"))
						&& (!prop.equals("constitutive"))
						&& (!prop.equals("derivational"))
						&& (!prop.equals("formal"))
						&& (!prop.equals("hasLocation"))
						&& (!prop.equals("telic"))) {
					
					OntoResult cleanOntoRes = new OntoResult();
					cleanOntoRes.setTermine(query_1_param_2);
					cleanOntoRes.setRelazione(prop);
					cleanOntoRes.setTermine_target(binding.get("termine").toString().split("#")[1]);
					clearRes.add(cleanOntoRes);
				}
			}
			qe2.close();
			//---------------------------------------------------------------------------
			
			queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT DISTINCT ?property ?termine ?definition "
					+ "WHERE { ?termine ?property saussure:"
					+ query_1_param_2
					+ " . ?property rdf:type owl:"
					+ query_1_param_1
					+ " ."
					+ " ?termine saussure:Definition ?definition . } "
					+ "ORDER BY ?termine ?property ";
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, saussureModel.getModel());
			for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
				QuerySolution binding = rs.nextSolution();
				prop = binding.get("property").toString().split("#")[1];

				if ((!prop.equals("agentive"))
						&& (!prop.equals("constitutive"))
						&& (!prop.equals("derivational"))
						&& (!prop.equals("formal"))
						&& (!prop.equals("hasLocation"))
						&& (!prop.equals("telic"))) {

					OntoResult ontoRes = new OntoResult();
					ontoRes.setTermine_target(query_1_param_2);
					ontoRes.setRelazione(prop);
					ontoRes.setTermine(binding.get("termine").toString()
							.split("#")[1]);
					ontoRes.setDefinition(binding.get("definition").toString());
					Individual dd = saussureModel.getModel().getIndividual(
							binding.get("termine").toString());
					for (ExtendedIterator i = dd.listOntClasses(true); i
							.hasNext();) {
						OntClass cls = (OntClass) i.next();
						if (cls.getNameSpace() != null) {
							if (cls.getNameSpace().equals(NS))
								ontoRes.setClasse(cls.toString().split("#")[1]);
						}
					}

					Individual dd2 = saussureModel.getModel().getIndividual(
							NS + query_1_param_2);
					for (ExtendedIterator i = dd2.listOntClasses(true); i
							.hasNext();) {
						OntClass cls = (OntClass) i.next();
						if (cls.getNameSpace() != null) {
							if (cls.getNameSpace().equals(NS))
								ontoRes.setClasse_target(cls.toString().split(
										"#")[1]);
						}
					}
					if (!ontoRes.getTermine().equals(
							ontoRes.getTermine_target()))
						res.add(ontoRes);

				}
			}
			qe.close();
			
			
			// verifica delle triple inferite tramite interrogazione al modello semplice
			query2 = QueryFactory.create(queryString);
			qe2 = QueryExecutionFactory.create(query2, saussureModel.getClearModel());
			for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
				QuerySolution binding = rs2.nextSolution();
				prop = binding.get("property").toString().split("#")[1];

				if ((!prop.equals("agentive"))
						&& (!prop.equals("constitutive"))
						&& (!prop.equals("derivational"))
						&& (!prop.equals("formal"))
						&& (!prop.equals("hasLocation"))
						&& (!prop.equals("telic"))) {
					OntoResult cleanOntoRes = new OntoResult();
					cleanOntoRes.setTermine(query_1_param_2);
					cleanOntoRes.setRelazione(prop);
					cleanOntoRes.setTermine_target(binding.get("termine").toString().split("#")[1]);
					clearRes.add(cleanOntoRes);
				}
			}
			qe2.close();
			//---------------------------------------------------------------------------
//			System.out.println("Con inferenza: " + countInf + "  -  senza inferenza: " + count);
			
			// setta le triple inferite
			for (OntoResult e : res) {
				if (inferred(e, 1)) e.setInferita("oui");
			}
			
			createDynamicColumns(columnTemplate_1_relazione);
			context.addMessage(null, new FacesMessage("Interrogation",
					"Quelles sont les relations sémantiques auxquelles est lié le terme "
							+ query_1_param_2 + " ?"));
		} else {
			String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT DISTINCT ?property ?valore "
					+ "WHERE { saussure:"
					+ query_1_param_2
					+ " ?property ?valore . ?property rdf:type owl:"
					+ query_1_param_1 + " . }" + "ORDER BY ?property ";

			Query query = QueryFactory.create(queryString);
			QueryExecution qe = QueryExecutionFactory.create(query,
					saussureModel.getModel());
			for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
				QuerySolution binding = rs.nextSolution();
				prop = binding.get("property").toString().split("#")[1];
				if ((!prop.equals("SemanticProperty"))
						&& (!prop.equals("MorphosyntacticProperty"))
						&& (!prop.equals("TermUsageInformationProperty"))) {
					OntoResult ontoRes = new OntoResult();
					ontoRes.setTratto(prop);
					ontoRes.setValore(binding.get("valore").toString());
					res.add(ontoRes);
				}
			}
			
			// verifica delle triple inferite tramite interrogazione al modello semplice
			Query query2 = QueryFactory.create(queryString);
			QueryExecution qe2 = QueryExecutionFactory.create(query,
					saussureModel.getClearModel());
			for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
				QuerySolution binding = rs2.nextSolution();
				prop = binding.get("property").toString().split("#")[1];
				if ((!prop.equals("SemanticProperty"))
						&& (!prop.equals("MorphosyntacticProperty"))
						&& (!prop.equals("TermUsageInformationProperty"))) {
					OntoResult cleanOntoRes = new OntoResult();
					cleanOntoRes.setTratto(prop);
					cleanOntoRes.setValore(binding.get("valore").toString());
					clearRes.add(cleanOntoRes);
				}
			}
			//---------------------------------------------------------------------------
			
			// setta le triple inferite
			for (OntoResult e : res) {
				if (inferred(e, 2)) e.setInferita("oui");
			}
			
			createDynamicColumns(columnTemplate_1_tratto);
			context.addMessage(null, new FacesMessage("Interrogation",
					"Quelles sont les propriétés auxquelles est lié le terme "
							+ query_1_param_2 + " ?"));
		}
	}

	public void runQuery_2() throws IOException {
		setCurrentQuery(2);
		if (!isPrintButton())
			setPrintButton(!isPrintButton());
		String value = null, prop = null;
		res.clear();
		columns.clear();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT DISTINCT ?termine "
				+ "WHERE { ?termine saussure:"
				+ query_2_param_2 + " \"" + query_2_param_3 + "\" . }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		
		QueryExecution qe2 = QueryExecutionFactory.create(query,
				saussureModel.getClearModel());
		
		// verifica delle triple inferite tramite interrogazione al modello semplice
		for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
			QuerySolution binding = rs2.nextSolution();
			prop = binding.get("termine").toString().split("#")[1];
			OntoResult cleanOntoRes = new OntoResult();
			cleanOntoRes.setTermine(binding.get("termine").toString().split("#")[1]);
			clearRes.add(cleanOntoRes);
		}
		qe2.close();
		
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			prop = binding.get("termine").toString().split("#")[1];
			OntoResult ontoRes = new OntoResult();
			ontoRes.setTermine(binding.get("termine").toString().split("#")[1]);
			Individual dd = saussureModel.getModel().getIndividual(
					binding.get("termine").toString());
			for (ExtendedIterator i = dd.listOntClasses(true); i.hasNext();) {
				OntClass cls = (OntClass) i.next();
				if (cls.getNameSpace() != null) {
					if (cls.getNameSpace().equals(NS))
						ontoRes.setClasse(cls.toString().split("#")[1]);
				}
			}
			res.add(ontoRes);
		}
		qe.close();
		
		// setta le triple inferite
		for (OntoResult e : res) {
			if (inferred(e, 3)) e.setInferita("oui");
		}
		
		createDynamicColumns(columnTemplate_2);
//		System.out.println("INFERITE: " + countInf + "  -  NORMALI: " + count);

		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage("Interrogation",
				"Quels sont les termes caractérisés par la propriété "
						+ query_2_param_1 + " dont l'attribut est "
						+ query_2_param_2 + " et la valeur est \""
						+ query_2_param_3 + "\" ?"));
	}

	public void runQuery_3() throws IOException {
		setCurrentQuery(3);
		if (!isPrintButton())
			setPrintButton(!isPrintButton());
		String value = null, prop = null;
		res.clear();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT DISTINCT ?termine "
				+ "WHERE { saussure:"
				+ query_3_param_1
				+ " saussure:"
				+ query_3_param_2
				+ " ?termine . } " + "ORDER BY ?termine ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			prop = binding.get("termine").toString().split("#")[1];
			OntoResult ontoRes = new OntoResult();
			ontoRes.setTermine(query_3_param_1);
			ontoRes.setRelazione(query_3_param_2);
			ontoRes.setTermine_target(prop);
			Individual dd = saussureModel.getModel().getIndividual(
					binding.get("termine").toString());
			for (ExtendedIterator i = dd.listOntClasses(true); i.hasNext();) {
				OntClass cls = (OntClass) i.next();
				if (cls.getNameSpace() != null) {
					if (cls.getNameSpace().equals(NS))
						ontoRes.setClasse_target(cls.toString().split("#")[1]);
				}
			}

			Individual dd2 = saussureModel.getModel().getIndividual(
					NS + query_3_param_1);
			for (ExtendedIterator i = dd2.listOntClasses(true); i.hasNext();) {
				OntClass cls = (OntClass) i.next();
				if (cls.getNameSpace() != null) {
					if (cls.getNameSpace().equals(NS))
						ontoRes.setClasse(cls.toString().split("#")[1]);
				}
			}
			res.add(ontoRes);

		}
		
		// verifica delle triple inferite tramite interrogazione al modello semplice
		QueryExecution qe2 = QueryExecutionFactory.create(query,
				saussureModel.getClearModel());
		for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
			QuerySolution binding = rs2.nextSolution();
			prop = binding.get("termine").toString().split("#")[1];
			OntoResult cleanOntoRes = new OntoResult();
			cleanOntoRes.setTermine(query_3_param_1);
			cleanOntoRes.setRelazione(query_3_param_2);
			cleanOntoRes.setTermine_target(prop);
			clearRes.add(cleanOntoRes);
		}
		
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT DISTINCT ?termine "
				+ "WHERE { ?termine saussure:"
				+ query_3_param_2
				+ " saussure:"
				+ query_3_param_1
				+ " . } "
				+ "ORDER BY ?termine ";
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			prop = binding.get("termine").toString().split("#")[1];
			OntoResult ontoRes = new OntoResult();
			ontoRes.setTermine(prop);
			ontoRes.setRelazione(query_3_param_2);
			ontoRes.setTermine_target(query_3_param_1);
			Individual dd = saussureModel.getModel().getIndividual(
					binding.get("termine").toString());
			for (ExtendedIterator i = dd.listOntClasses(true); i.hasNext();) {
				OntClass cls = (OntClass) i.next();
				if (cls.getNameSpace() != null) {
					if (cls.getNameSpace().equals(NS))
						ontoRes.setClasse(cls.toString().split("#")[1]);
				}
			}

			Individual dd2 = saussureModel.getModel().getIndividual(
					NS + query_3_param_1);
			for (ExtendedIterator i = dd2.listOntClasses(true); i.hasNext();) {
				OntClass cls = (OntClass) i.next();
				if (cls.getNameSpace() != null) {
					if (cls.getNameSpace().equals(NS))
						ontoRes.setClasse_target(cls.toString().split("#")[1]);
				}
			}
			if (!ontoRes.getTermine().equals(ontoRes.getTermine_target()))
				res.add(ontoRes);

		}
		qe.close();
		
		// verifica delle triple inferite tramite interrogazione al modello semplice
		qe2 = QueryExecutionFactory.create(query,
				saussureModel.getClearModel());
		for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
			QuerySolution binding = rs2.nextSolution();
			prop = binding.get("termine").toString().split("#")[1];
			OntoResult cleanOntoRes = new OntoResult();
			cleanOntoRes.setTermine(prop);
			cleanOntoRes.setRelazione(query_3_param_2);
			cleanOntoRes.setTermine_target(query_3_param_1);
			if (!cleanOntoRes.getTermine().equals(cleanOntoRes.getTermine_target()))
				clearRes.add(cleanOntoRes);
		}
		
		// setta le triple inferite
		for (OntoResult e : res) {
			if (inferred(e, 1)) e.setInferita("oui");
		}
		createDynamicColumns(columnTemplate_3);

		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage("Interrogation",
				"Quels sont les termes liés au terme " + query_3_param_1
						+ " par la relation " + query_3_param_2 + " ?"));
	}

	public void runQuery_4() throws IOException {
		if (!((getCurrentQuery() == 5) || (getCurrentQuery() == 6) || (getCurrentQuery() == 7))) setCurrentQuery(4);
		if (!isPrintButton())
			setPrintButton(!isPrintButton());
		String value = null, prop = null;
		res.clear();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?termine "
				+ "WHERE { ?termine saussure:"
				+ (query_4_param_1.equals("nessun valore") ? "null"
						: query_4_param_1)
				+ " saussure:"
				+ (query_4_param_2.equals("nessun valore") ? "null"
						: query_4_param_2)
				+ " . ?termine saussure:"
				+ (query_4_param_3.equals("nessun valore") ? "null"
						: query_4_param_3)
				+ " \""
				+ (query_4_param_4.equals("nessun valore") ? "null"
						: query_4_param_4) + "\" . ";
						
		if (!isAddRelationButton()) queryString = queryString 
				+ "?termine saussure:"
				+ query_4_param_5
				+ " saussure:" + query_4_param_6 + " . ";
		
		////////////////////////
		if (!isAddTraitButton()) queryString = queryString 
				+ "?termine saussure:"
				+ query_4_param_7
				+ " \"" + query_4_param_8 + "\" . ";
		////////////////////////
		
		queryString = queryString + "} ORDER BY ?termine ";
				
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			prop = binding.get("termine").toString().split("#")[1];
			OntoResult ontoRes = new OntoResult();
			ontoRes.setTermine(binding.get("termine").toString().split("#")[1]);
			Individual dd = saussureModel.getModel().getIndividual(
					binding.get("termine").toString());
			for (ExtendedIterator i = dd.listOntClasses(true); i.hasNext();) {
				OntClass cls = (OntClass) i.next();
				if (cls.getNameSpace() != null) {
					if (cls.getNameSpace().equals(NS))
						ontoRes.setClasse(cls.toString().split("#")[1]);
				}
			}
			res.add(ontoRes);
		}
		qe.close();
		
		// verifica delle triple inferite tramite interrogazione al modello semplice
		QueryExecution qe2 = QueryExecutionFactory.create(query,
				saussureModel.getClearModel());
		for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
			QuerySolution binding = rs2.nextSolution();
			prop = binding.get("termine").toString().split("#")[1];
			OntoResult cleanOntoRes = new OntoResult();
			cleanOntoRes.setTermine(binding.get("termine").toString().split("#")[1]);
			clearRes.add(cleanOntoRes);
		}
		qe.close();
		
		// setta le triple inferite
		for (OntoResult e : res) {
			if (inferred(e, 3)) e.setInferita("oui");
		}
		
		createDynamicColumns(columnTemplate_4);

		FacesContext context = FacesContext.getCurrentInstance();
		if (res.size() != 0) {
			if (getCurrentQuery() == 4) 
			context.addMessage(null, new FacesMessage("Interrogation",
					"Quels sont les termes caractérisés par: \n"
							+ "     la relation sémantique " + query_4_param_1
							+ " avec valeur " + query_4_param_2 + "\n"
							+ "     le trait sémantique " + query_4_param_3
							+ " avec valeur \"" + query_4_param_4 + "\" ?"));
			if (getCurrentQuery() == 5) 
			context.addMessage(null, new FacesMessage("Interrogation",
					"Quels sont les termes caractérisés par: \n"
							+ "     la relation sémantique " + query_4_param_1
							+ " avec valeur " + query_4_param_2 + "\n"
							+ "     le trait sémantique " + query_4_param_3
							+ " avec valeur \"" + query_4_param_4 + "\n" 
							+ "     la relation sémantique " + query_4_param_5
							+ " avec valeur " + query_4_param_6 + "\" ?"));
			if (getCurrentQuery() == 6) 
			context.addMessage(null, new FacesMessage("Interrogation",
					"Quels sont les termes caractérisés par: \n"
							+ "     la relation sémantique " + query_4_param_1
							+ " avec valeur " + query_4_param_2 + "\n"
							+ "     le trait sémantique " + query_4_param_3
							+ " avec valeur \"" + query_4_param_4 + "\n"
							+ "     le trait sémantique " + query_4_param_7
							+ " avec valeur \"" + query_4_param_8 + "\" ?"));
			if (getCurrentQuery() == 7) 
			context.addMessage(null, new FacesMessage("Interrogation",
					"Quels sont les termes caractérisés par: \n"
							+ "     la relation sémantique " + query_4_param_1
							+ " avec valeur " + query_4_param_2 + "\n"
							+ "     le trait sémantique " + query_4_param_3
							+ " avec valeur \"" + query_4_param_4 + "\n"
							+ "     la relation sémantique " + query_4_param_5
							+ " avec valeur " + query_4_param_6 + "\n"
							+ "     le trait sémantique " + query_4_param_7
							+ " avec valeur \"" + query_4_param_8 + "\" ?"));
		}
	}

	@PostConstruct
	private void loadSimpleModel() {
		if (!isDisabled()) {
			setDisabled(true);
			try {
				ArrayList<String> opt = new ArrayList<String>();
				opt.add("Synonymie");
				opt.add("Antonymie");
				opt.add("En relation avec");
				setTagCloudOptions(opt);
				initializeTagCloud();
				initializeQuery_1();
				initializeQuery_2();
				initializeQuery_3();
				initializeQuery_4();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public TagCloudModel getTagCloudModel() {
		return model;
	}
	
	public void initializeTagCloud() throws IOException {
		String queryString = null, term = null, property = null;
		if (opt_Cloud.equals("Synonymie")) property = "hasSynonym";
		else if (opt_Cloud.equals("Antonymie")) property = "hasAntonym";
		else property = "isRelatedTo";
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "SELECT ?termine ?value "
				+ "WHERE { ?termine saussure:" + property + " ?value . } " + "ORDER BY ?termine";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			term = binding.get("termine").toString().split("#")[1];
			if (tagCloudMap.containsKey(term)) tagCloudMap.put(term, tagCloudMap.get(term) + 1);
			else tagCloudMap.put(term, 1);
		}
		qe.close();
		drawTagCloud();
	}

	public void drawTagCloud() {
		if (model == null) model = new DefaultTagCloudModel();
		else model.clear();
		
		Iterator it = tagCloudMap.entrySet().iterator();
		while (it.hasNext()) {
		    Map.Entry pairs = (Map.Entry)it.next();
//		    System.out.println(pairs.getKey() + " = " + pairs.getValue());
		    it.remove(); // avoids a ConcurrentModificationException
			model.addTag(new DefaultTagCloudItem(pairs.getKey().toString(), Integer.parseInt(pairs.getValue().toString())));
		}
//
//			model.addTag(new DefaultTagCloudItem("RIA", "/ui/tagCloud.jsf",3));
//			model.addTag(new DefaultTagCloudItem("AJAX", 2));
//			model.addTag(new DefaultTagCloudItem("jQuery","/ui/tagCloud.jsf", 5));
//			model.addTag(new DefaultTagCloudItem("NextGen", 4));
//			model.addTag(new DefaultTagCloudItem("JSF 2.0","/ui/tagCloud.jsf", 2));
//			model.addTag(new DefaultTagCloudItem("FCB", 5));
//			model.addTag(new DefaultTagCloudItem("Mobile", 3));
//			model.addTag(new DefaultTagCloudItem("Themes","/ui/tagCloud.jsf", 4));
//			model.addTag(new DefaultTagCloudItem("Rocks","/ui/tagCloud.jsf", 1));
	}
	
	public void initializeQuery_1() throws IOException {
		String queryString = null;
		String inst = null;
		instanceMap.clear();
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "SELECT ?termine "
				+ "WHERE { ?termine a owl:Thing . ?termine ?property ?value . ?property rdf:type owl:"
				+ query_1_param_1 + " . } " + "ORDER BY ?termine";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			inst = binding.get("termine").toString().split("#")[1];
			instanceMap.put(inst, inst);
		}
		qe.close();
	}

	public void initializeQuery_2() throws IOException {
		String queryString = null;
		String elem = null;
		attributeMap.clear();
		valueMap.clear();
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT ?property "
				+ "WHERE { ?property rdfs:subPropertyOf saussure:SemanticProperty. } "
				+ "ORDER BY ?property";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("property").toString().split("#")[1];
			if ((!elem.equals("SemanticProperty"))
					&& (!elem.equals("MorphosyntacticProperty"))
					&& (!elem.equals("TermUsageInformationProperty"))) {
				attributeMap.put(elem, elem);
			}
		}

		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?value "
				+ "WHERE { ?termine saussure:"
				+ query_2_param_2 + " ?value . } " + "ORDER BY ?value";
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("value").toString();
			valueMap.put(elem, elem);
		}
		qe.close();
	}

	public void initializeQuery_3() throws IOException {
		String queryString = null;
		String elem = null;
		objRelationMap.clear();
		instance_2Map.clear();
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "SELECT ?termine "
				+ "WHERE { ?termine a owl:Thing . } "
				+ "ORDER BY ?termine";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("termine").toString().split("#")[1];
			instance_2Map.put(elem, elem);
		}
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?property "
				+ "WHERE { "
				+ "{ saussure:"
				+ query_3_param_1
				+ " ?property ?v1 . ?property rdf:type owl:ObjectProperty . } "
				+ "UNION "
				+ "{ ?v2 ?property saussure:"
				+ query_3_param_1
				+ " . ?property rdf:type owl:ObjectProperty . } "
				+ "} "
				+ "ORDER BY ?property";
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("property").toString().split("#")[1];

			if ((!elem.equals("agentive")) && (!elem.equals("constitutive"))
					&& (!elem.equals("derivational"))
					&& (!elem.equals("formal"))
					&& (!elem.equals("hasLocation")) && (!elem.equals("telic"))) {

				objRelationMap.put(elem, elem);
			}
		}
		qe.close();
	}

	public void initializeQuery_4() throws IOException {
		String queryString = null;
		String elem = null;
		objRelation_2Map.clear();
		instance_3Map.clear();
		attribute_2Map.clear();
		value_2Map.clear();
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?property "
				+ "WHERE { ?property rdf:type owl:ObjectProperty . } "
				+ "ORDER BY ?property";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("property").toString().split("#")[1];
			if ((!elem.equals("agentive")) && (!elem.equals("constitutive"))
					&& (!elem.equals("derivational"))
					&& (!elem.equals("formal")) && (!elem.equals("sameAs"))
					&& (!elem.equals("hasLocation")) && (!elem.equals("telic"))) {
				objRelation_2Map.put(elem, elem);
			}
		}
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?v1 ?subj "
				+ "WHERE { ?subj saussure:"
				+ query_4_param_1 + " ?v1 .} " + "ORDER BY ?v1 ";
		query = QueryFactory.create(queryString);
		qe = QueryExecutionFactory.create(query, saussureModel.getModel());
		// System.out.println("QUERY 2: " + queryString);
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("v1").toString().split("#")[1];

			// query_4_param_2 = elem;

			instance_3Map.put(elem, elem);
			if (elem.equals(query_4_param_2)) {
				String subj = binding.get("subj").toString().split("#")[1];
				// System.out.println("SOGGETTI TROVATI: " + subj);
				subject_Map.put(subj, subj);
				String queryString2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
						+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
						+ "SELECT ?property ?value "
						+ "WHERE { saussure:"
						+ subj
						+ " saussure:"
						+ query_4_param_1
						+ " saussure:"
						+ query_4_param_2
						+ " . "
						+ "saussure:"
						+ subj
						+ " ?property ?value . "
						+ "?property rdf:type owl:DatatypeProperty . } "
						+ "ORDER BY ?property ";
				Query query2 = QueryFactory.create(queryString2);
				QueryExecution qe2 = QueryExecutionFactory.create(query2,
						saussureModel.getModel());
				// System.out.println("QUERY 3: " + queryString2);
				for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
					QuerySolution binding2 = rs2.nextSolution();
					String elem2 = binding2.get("property").toString()
							.split("#")[1];

					// query_4_param_3 = elem2;

					if ((!elem2.equals("SemanticProperty"))
							&& (!elem2.equals("MorphosyntacticProperty"))
							&& (!elem2.equals("TermUsageInformationProperty"))) {
						attribute_2Map.put(elem2, elem2);
						// System.out.println("DATAPROPERTY TROVATE: " + elem2);
						String queryString3 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
								+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
								+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
								+ "SELECT ?value "
								+ "WHERE { saussure:"
								+ subj
								+ " saussure:"
								+ elem2
								+ " ?value . } "
								+ "ORDER BY ?value ";
						Query query3 = QueryFactory.create(queryString3);
						QueryExecution qe3 = QueryExecutionFactory.create(
								query3, saussureModel.getModel());
						// System.out.println("QUERY 4: " + queryString3);
						for (ResultSet rs3 = qe3.execSelect(); rs3.hasNext();) {
							QuerySolution binding3 = rs3.nextSolution();
							// System.out.println("\n Mi chiedo se " + elem2
							// + " è uguale a " + query_4_param_3 + "\n");
							if (elem2.equals(query_4_param_3)) {
								value_2Map.put(
										binding3.get("value").toString(),
										binding3.get("value").toString());
								// System.out.println("VALORI TROVATI: "
								// + binding3.get("value").toString());
							}
						}
						qe3.close();
					}
				}
				qe2.close();
			}
		}
		qe.close();
		// System.out.println(" CONTENUTO ATTRIBUTI: " +
		// attribute_2Map.values().toArray().toString());
		if (value_2Map.isEmpty())
			value_2Map.put("aucune valeur", "aucune valeur");
		if (attribute_2Map.isEmpty())
			attribute_2Map.put("aucune valeur", "aucune valeur");

	}

	public boolean getValue_2isEmpty() {
		if (value_2Map.get("aucune valeur") != null) {
			return true;
		} else
			return false;
	}

	public boolean getAttribute_2isEmpty() {
		if (attribute_2Map.get("aucune valeur") != null) {
			return true;
		} else
			return false;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void q_1_p_1Changed(AjaxBehaviorEvent event) {
		String queryString = null;
		String inst = null;
		instanceMap.clear();
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "SELECT ?termine "
				+ "WHERE { ?termine a owl:Thing . ?termine ?property ?value . ?property rdf:type owl:"
				+ query_1_param_1 + " . } " + "ORDER BY ?termine";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			inst = binding.get("termine").toString().split("#")[1];
			instanceMap.put(inst, inst);
		}
		qe.close();
	}

	public void q_2_p_1Changed(AjaxBehaviorEvent event) {
		String queryString = null;
		String att = null;
		attributeMap.clear();
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT ?property "
				+ "WHERE { ?property rdfs:subPropertyOf saussure:"
				+ query_2_param_1 + ". } " + "ORDER BY ?property";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			att = binding.get("property").toString().split("#")[1];
			if ((!att.equals("SemanticProperty"))
					&& (!att.equals("MorphosyntacticProperty"))
					&& (!att.equals("TermUsageInformationProperty")))
				attributeMap.put(att, att);
		}
		qe.close();
		q_2_p_2Changed(event);
	}

	public void q_2_p_2Changed(AjaxBehaviorEvent event) {
		String queryString = null;
		String val = null;
		valueMap.clear();
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?value "
				+ "WHERE { ?termine saussure:"
				+ query_2_param_2 + " ?value . } " + "ORDER BY ?value";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			val = binding.get("value").toString();
			valueMap.put(val, val);
		}
		qe.close();
	}

	public void q_3_p_1Changed(AjaxBehaviorEvent event) {
		String queryString = null;
		String elem = null;
		objRelationMap.clear();
		queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?property "
				+ "WHERE { "
				+ "{ saussure:"
				+ query_3_param_1
				+ " ?property ?v1 . ?property rdf:type owl:ObjectProperty . } "
				+ "UNION "
				+ "{ ?v2 ?property saussure:"
				+ query_3_param_1
				+ " . ?property rdf:type owl:ObjectProperty . } "
				+ "} "
				+ "ORDER BY ?property";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("property").toString().split("#")[1];

			if ((!elem.equals("agentive")) && (!elem.equals("constitutive"))
					&& (!elem.equals("derivational"))
					&& (!elem.equals("formal"))
					&& (!elem.equals("hasLocation")) && (!elem.equals("telic"))) {

				objRelationMap.put(elem, elem);
			}
		}
		qe.close();
	}

	public void q_4_p_1Changed(AjaxBehaviorEvent event) {
		subject_Map.clear();
		attribute_2Map.clear();
		value_2Map.clear();
		instance_3Map.clear();
		int cont = 0;
		String elem = null;
		int cont_0 = 0;
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?v1 ?subj "
				+ "WHERE { ?subj saussure:"
				+ query_4_param_1 + " ?v1 .} " + "ORDER BY ?v1 ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			cont_0++;
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("v1").toString().split("#")[1];
			// System.out.println(" ****TRIPLE " +
			// binding.get("subj").toString().split("#")[1] + " - " +
			// query_4_param_1 + " - " + elem);
			instance_3Map.put(elem, elem);
			if (cont_0 == 1) {
				query_4_param_2 = elem;
				queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
						+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
						+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
						+ "SELECT DISTINCT ?subj "
						+ "WHERE { ?subj saussure:"
						+ query_4_param_1
						+ " saussure:"
						+ query_4_param_2
						+ " . } " + "ORDER BY ?subj ";
				Query query4 = QueryFactory.create(queryString);
				QueryExecution qe4 = QueryExecutionFactory.create(query,
						saussureModel.getModel());
				for (ResultSet rs4 = qe4.execSelect(); rs4.hasNext();) {
					QuerySolution binding4 = rs4.nextSolution();
					elem = binding4.get("subj").toString().split("#")[1];
					subject_Map.put(elem, elem);
					String queryString2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
							+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
							+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
							+ "SELECT ?property ?value "
							+ "WHERE { saussure:"
							+ elem
							+ " ?property ?value . "
							+ "?property rdf:type owl:DatatypeProperty . } "
							+ "ORDER BY ?property ";
					Query query2 = QueryFactory.create(queryString2);
					QueryExecution qe2 = QueryExecutionFactory.create(query2,
							saussureModel.getModel());
					for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
						QuerySolution binding2 = rs2.nextSolution();
						String elem2 = binding2.get("property").toString()
								.split("#")[1];
						if ((!elem2.equals("SemanticProperty"))
								&& (!elem2.equals("MorphosyntacticProperty"))
								&& (!elem2
										.equals("TermUsageInformationProperty"))) {
							attribute_2Map.put(elem2, elem2);
							cont++;
							int cont_2 = 0;
							if (cont == 1)
								query_4_param_3 = elem2;
							String queryString3 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
									+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
									+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
									+ "SELECT ?value "
									+ "WHERE { saussure:"
									+ elem
									+ " saussure:"
									+ elem2
									+ " ?value . } " + "ORDER BY ?value ";
							Query query3 = QueryFactory.create(queryString3);
							QueryExecution qe3 = QueryExecutionFactory.create(
									query3, saussureModel.getModel());
							for (ResultSet rs3 = qe3.execSelect(); rs3
									.hasNext();) {
								cont_2++;
								QuerySolution binding3 = rs3.nextSolution();
								if (cont == 1)
									value_2Map.put(binding3.get("value")
											.toString(), binding3.get("value")
											.toString());
								if (cont_2 == 1)
									query_4_param_4 = binding3.get("value")
											.toString();
							}
							qe3.close();
						}
					}
					qe2.close();
				}
				qe4.close();
			}
		}
		qe.close();
		// System.out.println(" CONTENUTO ATTRIBUTI: " +
		// attribute_2Map.values().toArray().toString());
		if (value_2Map.isEmpty())
			value_2Map.put("aucune valeur", "aucune valeur");
		if (attribute_2Map.isEmpty())
			attribute_2Map.put("aucune valeur", "aucune valeur");
	}

	public void q_4_p_2Changed(AjaxBehaviorEvent event) {
		subject_Map.clear();
		attribute_2Map.clear();
		value_2Map.clear();
		int cont = 0;
		String elem = null;
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?v1 ?subj "
				+ "WHERE { ?subj saussure:"
				+ query_4_param_1
				+ " saussure:"
				+ query_4_param_2
				+ " .} "
				+ "ORDER BY ?subj ";
		// System.out.println(" ****QUERY " + queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			elem = binding.get("subj").toString().split("#")[1];
			// System.out.println(" ****TRIPLE " + elem + " - " +
			// query_4_param_1 + " - " + query_4_param_2);
			subject_Map.put(elem, elem);
			String queryString2 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "SELECT ?property ?value "
					+ "WHERE { saussure:"
					+ elem
					+ " ?property ?value . "
					+ "?property rdf:type owl:DatatypeProperty . } "
					+ "ORDER BY ?property ";
			// System.out.println(" ****QUERY " + queryString);
			Query query2 = QueryFactory.create(queryString2);
			QueryExecution qe2 = QueryExecutionFactory.create(query2,
					saussureModel.getModel());
			for (ResultSet rs2 = qe2.execSelect(); rs2.hasNext();) {
				QuerySolution binding2 = rs2.nextSolution();
				String elem2 = binding2.get("property").toString().split("#")[1];
				if ((!elem2.equals("SemanticProperty"))
						&& (!elem2.equals("MorphosyntacticProperty"))
						&& (!elem2.equals("TermUsageInformationProperty"))) {
					attribute_2Map.put(elem2, elem2);
					cont++;
					int cont_2 = 0;
					if (cont == 1)
						query_4_param_3 = elem2;
					String queryString3 = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
							+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
							+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
							+ "SELECT ?value "
							+ "WHERE { saussure:"
							+ elem
							+ " saussure:"
							+ elem2
							+ " ?value . } "
							+ "ORDER BY ?value ";
					Query query3 = QueryFactory.create(queryString3);
					QueryExecution qe3 = QueryExecutionFactory.create(query3,
							saussureModel.getModel());
					for (ResultSet rs3 = qe3.execSelect(); rs3.hasNext();) {
						cont_2++;
						QuerySolution binding3 = rs3.nextSolution();
						if (cont == 1)
							value_2Map.put(binding3.get("value").toString(),
									binding3.get("value").toString());
						if (cont_2 == 1)
							query_4_param_4 = binding3.get("value").toString();
					}
					qe3.close();
				}
			}
			qe2.close();
		}
		qe.close();
		if (value_2Map.isEmpty())
			value_2Map.put("aucune valeur", "aucune valeur");
		if (attribute_2Map.isEmpty())
			attribute_2Map.put("aucune valeur", "aucune valeur");
	}

	public void q_4_p_3Changed(AjaxBehaviorEvent event) {

		String queryString = null;
		value_2Map.clear();
		Set<Entry<String, String>> set = subject_Map.entrySet();
		Iterator<Entry<String, String>> i = set.iterator();
		while (i.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry subj = (Map.Entry) i.next();
			queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
					+ "SELECT DISTINCT ?value "
					+ "WHERE { saussure:"
					+ subj.getValue()
					+ " saussure:"
					+ query_4_param_3
					+ " ?value . } " + "ORDER BY ?value ";
			Query query = QueryFactory.create(queryString);
			QueryExecution qe = QueryExecutionFactory.create(query,
					saussureModel.getModel());
			for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
				QuerySolution binding = rs.nextSolution();
				value_2Map.put(binding.get("value").toString(),
						binding.get("value").toString());
			}
			qe.close();
		}

	}
	
	public void q_4_p_5Changed(AjaxBehaviorEvent event) {
//		System.out.println("IL VALORE è CAMBIATO in : " + query_4_param_5);
		valuesOfRelationAlternativeMap.clear();
		for(String item: constraint_1)
			if (item.split("-")[0].equals(query_4_param_5)) {
				if (!valuesOfRelationAlternativeMap.contains(item.split("-")[1])) valuesOfRelationAlternativeMap.add(item.split("-")[1]);
				System.out.println("aggiunto: " + item.toString());
			} else {
				System.out.println("non aggiunto: " + item.toString());
			}
//			if (item.split("-")[0].equals(query_4_param_5)) valuesOfRelationAlternativeMap.put(item.split("-")[1], item.split("-")[1]);
	}
	
	private void setRelationConstraint() {
		setRelationConstraint(true);
		setAddRelationForm(false);
		setAddTraitForm(true);
		relationAlternativeMap.clear();
		valuesOfRelationAlternativeMap.clear();
		constraint_1.clear();
		String value = null, term = null;
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?termine "
				+ "WHERE { ?termine saussure:"
				+ (query_4_param_1.equals("nessun valore") ? "null"
						: query_4_param_1)
				+ " saussure:"
				+ (query_4_param_2.equals("nessun valore") ? "null"
						: query_4_param_2)
				+ " . ?termine saussure:"
				+ (query_4_param_3.equals("nessun valore") ? "null"
						: query_4_param_3)
				+ " \""
				+ (query_4_param_4.equals("nessun valore") ? "null"
						: query_4_param_4) + "\" . ";
		if (isTrattoConstraint()) queryString = queryString + "?termine saussure:" + query_4_param_7 + " \"" + query_4_param_8 + "\" . } ORDER BY ?termine";
		else queryString = queryString + "} ORDER BY ?termine ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			term = binding.get("termine").toString().split("#")[1];
			queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT DISTINCT ?obj ?prop "
//					+ "WHERE { ?subj ?prop saussure:" + term + " . } ORDER BY ?prop";
					+ "WHERE { saussure:" + term + " ?prop ?obj . ?prop rdf:type owl:ObjectProperty . } ORDER BY ?prop";
//			System.out.println("*** QUERY: " + queryString);
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, saussureModel.getModel());
			for (ResultSet rs2 = qe.execSelect(); rs2.hasNext();) {
				QuerySolution binding2 = rs2.nextSolution();
				if (binding2.get("prop").toString().contains("http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#")) { 
					if (!binding2.get("prop").toString().split("#")[1].equals(query_4_param_1)) { 
//						System.out.println("*** RES: " +binding2.get("prop").toString() + "-" + binding2.get("obj").toString());
						constraint_1.add(binding2.get("prop").toString().split("#")[1] + "-" + binding2.get("obj").toString().split("#")[1]);
						if (!relationAlternativeMap.contains(binding2.get("prop").toString().split("#")[1]))
							relationAlternativeMap.add(binding2.get("prop").toString().split("#")[1]);
		//				if (!valuesOfRelationAlternativeMap.contains(binding2.get("obj").toString().split("#")[1]))
		//					valuesOfRelationAlternativeMap.add(binding2.get("obj").toString().split("#")[1]);
					}
				}
			}
		}
		if (!relationAlternativeMap.isEmpty()) this.query_4_param_5 = relationAlternativeMap.get(0);
		else this.query_4_param_5 = "aucun relation";
		///////
		q_4_p_5Changed(null);
		//////
		if (!valuesOfRelationAlternativeMap.isEmpty()) this.query_4_param_6 = valuesOfRelationAlternativeMap.get(0);
		else this.query_4_param_6 = "aucun valeur";
		qe.close();
	}

	public void q_4_p_7Changed(AjaxBehaviorEvent event) {
//		System.out.println("IL VALORE è CAMBIATO in : " + query_4_param_7);
		valuesOfTrattoAlternativeMap.clear();
		for(String item: constraint_2)
			if (item.split("-")[0].equals(query_4_param_7)) {
				if (!valuesOfTrattoAlternativeMap.contains(item.split("-")[1])) valuesOfTrattoAlternativeMap.add(item.split("-")[1]);
//				System.out.println("aggiunto: " + item.toString());
			} else {
//				System.out.println("non aggiunto: " + item.toString());
			}
//			if (item.split("-")[0].equals(query_4_param_5)) valuesOfRelationAlternativeMap.put(item.split("-")[1], item.split("-")[1]);
	}
	
	private void setTrattoConstraint() {
		setTrattoConstraint(true);
		setAddTraitForm(false);
		setAddRelationForm(true);
		trattoAlternativeMap.clear();
		valuesOfTrattoAlternativeMap.clear();
		constraint_2.clear();
		String value = null, term = null, prop = null;
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "SELECT DISTINCT ?termine "
				+ "WHERE { ?termine saussure:"
				+ (query_4_param_1.equals("nessun valore") ? "null"
						: query_4_param_1)
				+ " saussure:"
				+ (query_4_param_2.equals("nessun valore") ? "null"
						: query_4_param_2)
				+ " . ?termine saussure:"
				+ (query_4_param_3.equals("nessun valore") ? "null"
						: query_4_param_3)
				+ " \""
				+ (query_4_param_4.equals("nessun valore") ? "null"
						: query_4_param_4) + "\" . ";
		if (isRelationConstraint()) queryString = queryString + "?termine saussure:" + query_4_param_5 + " saussure:" + query_4_param_6 + " . } ORDER BY ?termine";
		else queryString = queryString + "} ORDER BY ?termine ";
//		System.out.println("*** QUERY: " + queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query,
				saussureModel.getModel());
		for (ResultSet rs = qe.execSelect(); rs.hasNext();) {
			QuerySolution binding = rs.nextSolution();
			term = binding.get("termine").toString().split("#")[1];
			queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
					+ "PREFIX saussure: <http://www.semanticweb.org/ontologies/2012/0/Ontology1326277267409.owl#> "
					+ "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
					+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
					+ "SELECT DISTINCT ?obj ?prop "
//					+ "WHERE { ?subj ?prop saussure:" + term + " . } ORDER BY ?prop";
					+ "WHERE { saussure:" + term + " ?prop ?obj . ?prop rdf:type owl:DatatypeProperty . } ORDER BY ?prop";
//			System.out.println("*** QUERY: " + queryString);
			query = QueryFactory.create(queryString);
			qe = QueryExecutionFactory.create(query, saussureModel.getModel());
			for (ResultSet rs2 = qe.execSelect(); rs2.hasNext();) {
				QuerySolution binding2 = rs2.nextSolution();
				prop = binding2.get("prop").toString().split("#")[1];
				if ((!prop.equals("SemanticProperty"))
						&& (!prop.equals("MorphosyntacticProperty"))
						&& (!prop.equals("TermUsageInformationProperty"))) {
					if (!prop.equals(query_4_param_3)) { 
//						System.out.println("*** RES: " +binding2.get("prop").toString() + "-" + binding2.get("obj").toString());
						constraint_2.add(prop + "-" + binding2.get("obj").toString());
						if (!trattoAlternativeMap.contains(prop))
							trattoAlternativeMap.add(prop);
					}
				}
			}
		}
		if (!trattoAlternativeMap.isEmpty()) this.query_4_param_7 = trattoAlternativeMap.get(0);
		else this.query_4_param_7 = "aucun relation";
		///////
		q_4_p_7Changed(null);
		//////
		if (!valuesOfTrattoAlternativeMap.isEmpty()) this.query_4_param_8 = valuesOfTrattoAlternativeMap.get(0);
		else this.query_4_param_8 = "aucun valeur";
		qe.close();
	}

	public void menuUpdater(Long opt) {
		// aggiungi relazione
		if (opt == 1) {
//			if (isAddTraitForm()) setAddRelationForm(false);
			setRelationConstraint();
			if (isTrattoConstraint()) setCurrentQuery(7);
			else setCurrentQuery(5);
			System.out.println("CQ: " + getCurrentQuery());
		}
		// aggiungi tratto
		if (opt == 2) {
//			if (isAddRelationForm()) setAddTraitForm(false);
			setTrattoConstraint();
			if (isRelationConstraint()) setCurrentQuery(7);
			else setCurrentQuery(6);
			System.out.println("CQ: " + getCurrentQuery());
		}
		// rimuovi relazione
		if (opt == 3) {
			if (!isAddRelationForm()) setAddTraitForm(false);
			setRelationConstraint(false);
//			setAddRelationForm(false);
			if (isTrattoConstraint()) setCurrentQuery(6);
			else setCurrentQuery(4);
			System.out.println("CQ: " + getCurrentQuery());
		}
		// rimuovi tratto
		if (opt == 4) {
			if (!isAddTraitForm()) setAddRelationForm(false);
			setTrattoConstraint(false);
			if (isRelationConstraint()) setCurrentQuery(5);
			else setCurrentQuery(4);
			System.out.println("CQ: " + getCurrentQuery());
//			setAddTraitForm(false);
		}
		
		if ((opt == 1) || (opt == 3))
			setAddRelationButton(!isAddRelationButton());
		else if ((opt == 2) || (opt == 4))
			setAddTraitButton(!isAddTraitButton());
	}

	public boolean isAddRelationButton() {
		return addRelationButton;
	}

	public void setAddRelationButton(boolean addRelationButton) {
		this.addRelationButton = addRelationButton;
	}

	public boolean isAddTraitButton() {
		return addTraitButton;
	}

	public void setAddTraitButton(boolean addTraitButton) {
		this.addTraitButton = addTraitButton;
	}

	public StreamedContent exportPDF() throws DocumentException,
			MalformedURLException, IOException {
		String queryText = null;
		System.out.println("current query = " + getCurrentQuery());
		if (getCurrentQuery() == 1) {
			if (query_1_param_1.equals("ObjectProperty"))
				queryText = "Quelles sont les relations sémantiques auxquelles est lié le terme "
						+ query_1_param_2 + " ?";
			else
				queryText = "Quelles sont les propriétés auxquelles est lié le terme "
						+ query_1_param_2 + " ?";
			return exportPDF(queryText, "Saussure_Q1");
		} else if (getCurrentQuery() == 2) {
			queryText = "Quels sont les termes caractérisés par la propriété "
					+ query_2_param_1 + " dont l'attribut est "
					+ query_2_param_2 + " et la valeur est \""
					+ query_2_param_3 + "\" ?";
			return exportPDF(queryText, "Saussure_Q2");
		} else if (getCurrentQuery() == 3) {
			queryText = "Quels sont les termes liés au terme "
					+ query_3_param_1 + " par la relation " + query_3_param_2
					+ " ?";
			return exportPDF(queryText, "Saussure_Q3");
		} else if (getCurrentQuery() == 4) {
			queryText = "Quels sont les termes caractérisés par: \n"
					+ "     la relation sémantique " + query_4_param_1
					+ " avec valeur " + query_4_param_2 + "\n"
					+ "     le trait sémantique " + query_4_param_3
					+ " avec valeur \"" + query_4_param_4 + "\" ?";
			return exportPDF(queryText, "Saussure_Q4");
		} else if (getCurrentQuery() == 5) { // vincoli della query 4 - Relazione
			queryText = "Quels sont les termes caractérisés par: \n"
					+ "     la relation sémantique " + query_4_param_1
					+ " avec valeur " + query_4_param_2 + "\n"
					+ "     le trait sémantique " + query_4_param_3
					+ " avec valeur \"" + query_4_param_4 + "\n" 
					+ "     la relation sémantique " + query_4_param_5
					+ " avec valeur " + query_4_param_6 + "\" ?";
			return exportPDF(queryText, "Saussure_Q4_1");
		} else if (getCurrentQuery() == 6) { // vincoli della query 4 - Tratto
			queryText = "Quels sont les termes caractérisés par: \n"
					+ "     la relation sémantique " + query_4_param_1
					+ " avec valeur " + query_4_param_2 + "\n"
					+ "     le trait sémantique " + query_4_param_3
					+ " avec valeur \"" + query_4_param_4 + "\n"
					+ "     le trait sémantique " + query_4_param_7
					+ " avec valeur \"" + query_4_param_8 + "\" ?";
			return exportPDF(queryText, "Saussure_Q4_2");
		} else { // vincoli della query 4 - Relazione + Tratto
			queryText = "Quels sont les termes caractérisés par: \n"
					+ "     la relation sémantique " + query_4_param_1
					+ " avec valeur " + query_4_param_2 + "\n"
					+ "     le trait sémantique " + query_4_param_3
					+ " avec valeur \"" + query_4_param_4 + "\n"
					+ "     la relation sémantique " + query_4_param_5
					+ " avec valeur " + query_4_param_6 + "\n"
					+ "     le trait sémantique " + query_4_param_7
					+ " avec valeur \"" + query_4_param_8 + "\" ?";
			return exportPDF(queryText, "Saussure_Q4_3");
		}
	}

	private StreamedContent exportPDF(String queryText, String fileName)
			throws DocumentException, MalformedURLException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PDFCreator pdfDoc = null;
		if (fileName.contains("Q1") || fileName.contains("Q3"))
			pdfDoc = new PDFCreator(1);
		else
			pdfDoc = new PDFCreator(0);
		PdfWriter.getInstance(pdfDoc.getDocument(), baos);
		pdfDoc.openDocument();
		pdfDoc.addMetaData();
		pdfDoc.addContent();
		pdfDoc.addQuestion(queryText);
		if (fileName.contains("Q2"))
			pdfDoc.ontoResultTable(res, 2);
		else if (fileName.contains("Q1")) {
			if (query_1_param_1.equals("ObjectProperty"))
				pdfDoc.ontoResultTable(res, 1);
			else
				pdfDoc.ontoResultTable(res, 5);
		} else if (fileName.contains("Q3"))
			pdfDoc.ontoResultTable(res, 3);
		else
			pdfDoc.ontoResultTable(res, 4);
		
		
		pdfDoc.addFooterPage();
		pdfDoc.closeDocument();
		ByteArrayInputStream stream = new ByteArrayInputStream(
				baos.toByteArray());
		return new DefaultStreamedContent(stream, "application/pdf", fileName);
	}

	public boolean isPrintButton() {
		return printButton;
	}

	public void setPrintButton(boolean printButton) {
		this.printButton = printButton;
	}

	public int getCurrentQuery() {
		return currentQuery;
	}

	public void setCurrentQuery(int currentQuery) {
		this.currentQuery = currentQuery;
	}

	public List<String> getTagCloudOptions() {
		return tagCloudOptions;
	}

	public void setTagCloudOptions(ArrayList<String> tagCloudOptions) {
		this.tagCloudOptions = tagCloudOptions;
	}

	public String getOpt_Cloud() {
		return opt_Cloud;
	}

	public void setOpt_Cloud(String opt_Cloud) {
		this.opt_Cloud = opt_Cloud;
	}

	public boolean isRelationConstraint() {
		return relationConstraint;
	}

	public void setRelationConstraint(boolean relationConstraint) {
		this.relationConstraint = relationConstraint;
	}

	public boolean isTrattoConstraint() {
		return trattoConstraint;
	}

	public void setTrattoConstraint(boolean trattoConstraint) {
		this.trattoConstraint = trattoConstraint;
	}

	public boolean isAddRelationForm() {
		return addRelationForm;
	}

	public void setAddRelationForm(boolean addRelationForm) {
		this.addRelationForm = addRelationForm;
	}

	public boolean isAddTraitForm() {
		return addTraitForm;
	}

	public void setAddTraitForm(boolean addTraitForm) {
		this.addTraitForm = addTraitForm;
	}

}