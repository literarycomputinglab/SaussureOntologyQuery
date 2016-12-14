package it.cnr.ilc.saussure;

import javax.faces.bean.SessionScoped;
import javax.inject.Named;

import org.primefaces.model.tagcloud.DefaultTagCloudItem;
import org.primefaces.model.tagcloud.DefaultTagCloudModel;
import org.primefaces.model.tagcloud.TagCloudModel;

@Named(value = "tagCloud")
@SessionScoped
public class TagCloudBean {

    private TagCloudModel model;

    public TagCloudBean() {
        model = new DefaultTagCloudModel();
        model.addTag(new DefaultTagCloudItem("Transformers", 1));
        model.addTag(new DefaultTagCloudItem("RIA", "/ui/tagCloud.jsf", 3));
        model.addTag(new DefaultTagCloudItem("AJAX", 2));
        model.addTag(new DefaultTagCloudItem("jQuery", "/ui/tagCloud.jsf", 5));
        model.addTag(new DefaultTagCloudItem("NextGen", 4));
        model.addTag(new DefaultTagCloudItem("JSF 2.0", "/ui/tagCloud.jsf", 2));
        model.addTag(new DefaultTagCloudItem("FCB", 5));
        model.addTag(new DefaultTagCloudItem("Mobile", 3));
        model.addTag(new DefaultTagCloudItem("Themes", "/ui/tagCloud.jsf", 4));
        model.addTag(new DefaultTagCloudItem("Rocks", "/ui/tagCloud.jsf", 1));
    }

    public TagCloudModel getModel() {
        return model;
    }
}
