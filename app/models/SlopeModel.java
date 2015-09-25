package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.Model;

@Entity
public class SlopeModel extends Model {
	@Id
	public int id;
	public int average_grade;
	public int length;
    public String start_lat;
    public String start_lng;
    public String end_lat;
    public String end_lng;

}
