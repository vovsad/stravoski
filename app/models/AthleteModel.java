package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.jstrava.entities.activity.Activity;
import org.jstrava.entities.athlete.Athlete;
import org.jstrava.entities.club.Club;
import org.jstrava.entities.gear.Gear;

import com.avaje.ebean.Model;

@Entity
public class AthleteModel extends Model {

	@Id
	public int id;
	public String resource_state;
	public String firstname;
	public String lastname;
	public String profile_medium;
	public String profile;
	public String city;
	public String state;
	public String sex;
	public String friend;
	public String follower;
	public boolean premium;
	public String created_at;
	public String updated_at;
	public String date_preference;
	public String measurement_preference;
	public String email;

	public AthleteModel(Athlete athlete) {
		id = athlete.getId();
		resource_state = athlete.getResource_state();
		firstname = athlete.getFirstname();
		lastname = athlete.getLastname();
		profile_medium = athlete.getProfile_medium();
		profile = athlete.getProfile();
		city = athlete.getCity();
		state = athlete.getState();
		sex = athlete.getSex();
		friend = athlete.getFriend();
		follower = athlete.getFollower();
		premium = athlete.getPremium();
		created_at = athlete.getCreated_at();
		updated_at = athlete.getUpdated_at();
		date_preference = athlete.getDate_preference();
		measurement_preference = athlete.getMeasurement_preference();
		email = athlete.getEmail();

	}

	public Athlete getAthlete() {
		Athlete a = new Athlete();
		a.setId(id);
		a.setResource_state(resource_state);
		a.setFirstname(firstname);
		a.setLastname(lastname);
		a.setProfile_medium(profile_medium);
		a.setProfile(profile);
		a.setCity(city);
		a.setState(state);
		a.setSex(sex);
		a.setFriend(friend);
		a.setFollower(follower);
		a.setPremium(premium);
		a.setCreated_at(created_at);
		a.setUpdated_at(updated_at);
		a.setDate_preference(date_preference);
		a.setMeasurement_preference(measurement_preference);
		a.setEmail(email);

		return a;

	}

	public static final Finder<Long, AthleteModel> find = new Finder<Long, AthleteModel>(
			AthleteModel.class);

}
