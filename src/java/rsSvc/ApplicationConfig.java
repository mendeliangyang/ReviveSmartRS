/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc;

import java.util.Set;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
/**
 *
 * @author Administrator
 */
@javax.ws.rs.ApplicationPath("Revive")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        resources.add(MultiPartFeature.class);
        //resources.add(org.slf4j.LoggerFactory);//org/slf4j/LoggerFactory
        return resources;
    }

    //resources.add(MultiPartFeature.class);
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(rsSvc.AustraliaBankRS.AustraliaBankRS.class);
        resources.add(rsSvc.Electornic.ElectornicRS.class);
        resources.add(rsSvc.FileDepotRS.class);
        resources.add(rsSvc.RESTCorsDemoRequestFilter.class);
        resources.add(rsSvc.RESTCorsDemoResponseFilter.class);
        resources.add(rsSvc.ReviveRS.class);
        resources.add(rsSvc.ReviveSign.class);
    }
}
