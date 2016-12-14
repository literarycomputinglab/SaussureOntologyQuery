package it.cnr.ilc.saussure;

import java.io.InputStream;
import java.io.Serializable;

import javax.annotation.PostConstruct;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class SaussureModel implements Serializable {

    private static final long serialVersionUID = 2L;

    private static OntModel model = null;
    private static OntModel clearModel = null;

    @PostConstruct
    private void initOnto() {
        InputStream in = getClass().getClassLoader().getResourceAsStream("Ontology_F_d_S.owl");
        InputStream in2 = getClass().getClassLoader().getResourceAsStream("Ontology_F_d_S.owl");
        model = ModelFactory
                .createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
        model.read(in, "RDF/XML");
        clearModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        clearModel.read(in2, "RDF/XML");
    }

    public OntModel getModel() {
        return model;
    }

    public OntModel getClearModel() {
        return clearModel;
    }

}
