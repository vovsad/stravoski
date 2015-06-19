package models;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Constraint;

import org.jstrava.entities.activity.Activity;
import org.jstrava.entities.activity.Polyline;
import org.jstrava.entities.activity.SplitsMetric;
import org.jstrava.entities.activity.SplitsStandard;
import org.jstrava.entities.athlete.Athlete;
import org.jstrava.entities.segment.SegmentEffort;

import com.avaje.ebean.Model;
import com.google.gson.annotations.SerializedName;

import play.data.validation.*;

@Entity
public class ActivityModel extends Model {
	
	@Id
    public int id;
    public int resource_state;
    public String external_id;
    public int upload_id;
    @Constraints.Required
    public Athlete athlete;/*Simple Athlete representation with just id*/
    public String name;
    public float distance;
    public int moving_time;
    public int elapsed_time;
    public float total_elevation_gain;
    public String type;
    public String start_date;
    public String start_date_local;
    public String time_zone;
    public String start_lat;
    public String start_lng;
    public String end_lat;
    public String end_lng;
    public String location_city;
    public String location_state;
    public int achievement_count;
    public int kudos_count;
    public int comment_count;
    public int athlete_count;
    public int photo_count;
    @OneToOne(cascade = CascadeType.ALL)
    public PolylineModel map;
    public boolean trainer;
    public boolean commute;
    public boolean manual;
    @SerializedName("public")
    public boolean PRIVATE;
    public boolean flagged;
    public String gear_id;
    public float average_speed;
    public float max_speed;
    public float average_cadence;
    public int average_temp;
    public float average_watts;
    public float kilojoules;
    public float average_heartrate;
    public float max_heartrate;
    public float calories;
    public int truncated;
    public boolean has_kudoed;
//    public List<SegmentEffort> segment_efforts;
//    public List<SplitsMetric> splits_metric;
//    public List<SplitsStandard> splits_standard;
//    public List<SegmentEffort> best_efforts;
    
	public ActivityModel(Activity a) {
	    id = a.getId();
	    resource_state = a.getResource_state();
	    external_id = a.getExternal_id();
	    upload_id = a.getUpload_id();
	    athlete = a.getAthlete();//.getId();/*Simple Athlete representation with just id*/
	    name = a.getName();
	    distance = a.getDistance();
	    moving_time = a.getMoving_time();
	    elapsed_time = a.getElapsed_time();
	    total_elevation_gain = a.getTotal_elevation_gain();
	    type = a.getType();
	    start_date = a.getStart_date();
	    start_date_local = a.getStart_date_local();
	    time_zone = a.getTime_zone();
	    start_lat = a.getStart_latlng()[0];
	    start_lng = a.getStart_latlng()[1];
	    end_lat = a.getEnd_latlng()[0];
	    end_lng = a.getEnd_latlng()[1];
	    location_city = a.getLocation_city();
	    location_state = a.getLocation_state();
	    achievement_count = a.getAchievement_count();
	    kudos_count = a.getKudos_count();
	    comment_count = a.getComment_count();
	    athlete_count = a.getAthlete_count();
	    photo_count = a.getPhoto_count();
	    map = new PolylineModel();
	    map.setPolyline(a.getMap());
	    trainer = a.getTrainer();
	    commute = a.getCommute();
	    manual = a.getManual();
	    PRIVATE = a.getPRIVATE();
	    flagged = a.getFlagged();
	    gear_id = a.getGear_id();
	    average_speed = a.getAverage_speed();
	    max_speed = a.getMax_speed();
	    average_cadence = a.getAverage_cadence();
	    average_temp = a.getAverage_temp();
	    average_watts = a.getAverage_watts();
	    kilojoules = a.getKilojoules();
	    average_heartrate = a.getAverage_heartrate();
	    max_heartrate = a.getMax_heartrate();
	    calories = a.getCalories();
	    truncated = a.getTruncated();
	    has_kudoed = a.getHas_kudoed();
	}
	
	public Activity getActivity(){
		
		Activity a = new Activity();
		a.setId(id);
		a.setResource_state(resource_state);
		a.setExternal_id(external_id);
		a.setUpload_id(upload_id);
		a.setAthlete(athlete);
		a.setName(name);
		a.setDistance(distance);
		a.setMoving_time(moving_time);
		a.setElapsed_time(elapsed_time);
		a.setTotal_elevation_gain(total_elevation_gain);
		a.setType(type);
		a.setStart_date(start_date);
		a.setStart_date(start_date);
		a.setTime_zone(time_zone);
		a.setStart_latlng(new String[]{start_lat, start_lng});
	    a.setEnd_latlng(new String[]{end_lat, end_lng});
	    a.setLocation_city(location_city);
	    a.setLocation_city(location_city);
	    a.setAchievement_count(achievement_count);
	    a.setKudos_count(kudos_count);
	    a.setComment_count(comment_count);
	    a.setAchievement_count(achievement_count);
	    a.setPhoto_count(photo_count);
	    a.setMap(map.getStravaPlyline());
	    a.setTrainer(trainer);
	    a.setCommute(commute);
	    a.setManual(manual);
	    a.setPRIVATE(PRIVATE);
	    a.setFlagged(flagged);
	    a.setGear_id(gear_id);
	    a.setAverage_cadence(average_cadence);
	    a.setMax_speed(max_speed);
	    a.setAverage_cadence(average_cadence);
	    a.setAverage_cadence(average_cadence);
	    a.setAverage_watts(average_watts);
	    a.setKilojoules(kilojoules);
	    a.setAverage_heartrate(average_heartrate);
	    a.setMax_heartrate(max_heartrate);
	    a.setCalories(calories);
	    a.setTruncated(truncated);
	    a.setHas_kudoed(has_kudoed);
		
		return a;
		
	}
    
    
    
    public static final Finder<Long, ActivityModel> find =
            new Finder<Long, ActivityModel>(ActivityModel.class);


}
