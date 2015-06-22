package models;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.jstrava.entities.activity.Polyline;

import com.avaje.ebean.Model;

@Entity
public class PolylineModel extends Model {
	
	@Id
    public String id;
    public String polyline;
    @Column(length=1000)
    public String summary_polyline;
    public String resource_state;
	public void setPolyline(Polyline map) {
		id = map.getId();
		polyline = map.getPolyline();
		summary_polyline = map.getSummary_polyline();
		resource_state = map.getResource_state();
	}
	public Polyline getStravaPlyline() {
		Polyline map = new Polyline();
		map.setId(id);
		map.setPolyline(polyline);
		map.setResource_state(resource_state);
		map.setSummary_polyline(summary_polyline);
		
		return map;
	}
	
	public String toString(){
		return "[ id = " + id + " , " + summary_polyline + "] ";   
	}

}
