package models;

import java.util.List;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import org.jstrava.entities.stream.Stream;

public class StreamsUnnested {
	private static final Object DISTANCE = "distance";
	private static final Object ALTITUDE = "altitude";
	private static final Object GRADE = "grade_smooth";
	
	private List<Double> distance;
	private List<Double> altitude;
	private List<Double> grade;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setData(Stream s){
		if (s.getType().equals(DISTANCE)){
			distance = (List<Double>)(List)s.getData();
		}else if(s.getType().equals(ALTITUDE)){
			altitude = (List<Double>)(List)s.getData();
		}else if (s.getType().equals(GRADE)){
			grade = (List<Double>)(List)s.getData();
		}
	}
	
	public List<Double> getDistance(){
		return distance;
	}

	public List<Double> getAltitude(){
		return altitude;
	}
	
	public List<Double> getGrade(){
		return grade;
	}
	
	public Table<Double, Double, Double> getMergedStreams(){
		Table<Double, Double, Double> table = TreeBasedTable.create();

		for (int i = 0; i < distance.size(); i++) {
		    table.put(distance.get(i), altitude.get(i), grade.get(i));
		}
		
		return table;
	}
}
