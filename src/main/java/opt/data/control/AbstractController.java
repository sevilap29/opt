package opt.data.control;

import error.OTMException;
import opt.data.ControlFactory;
import opt.data.LaneGroupType;
import utils.OTMUtils;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public abstract class AbstractController implements Comparable {



	protected long id;
	protected float dt;
	protected float start_time;
	protected float end_time;
	protected control.AbstractController.Algorithm algorithm;
	protected Map<Long,AbstractActuator> actuators = new HashMap<>();
	protected Map<Long,Sensor> sensors = new HashMap<>();

	public AbstractController(long id, float dt, float start_time, Float end_time, control.AbstractController.Algorithm algorithm) throws Exception {

		// CHECKS
		if(start_time<0)
			throw new OTMException("start_time<0");

		if(end_time!=null && end_time<=start_time)
			throw new OTMException("end_time<=start_time");

		this.id = id;
		this.dt = dt;
		this.start_time = start_time;
		this.end_time = end_time==null ? Float.POSITIVE_INFINITY : end_time;
		this.algorithm = algorithm;

	}

	public final String getName(){
		return ControlFactory.cntrl_alg_name.AtoB(algorithm);
	}

	public final control.AbstractController.Algorithm getAlgorithm(){
		return algorithm;
	}

	protected void add_sensor(Sensor sensor){
		sensors.put(sensor.id,sensor);
	}

	protected void add_actuator(AbstractActuator actuator){
		actuators.put(actuator.id,actuator);
	}

	public jaxb.Controller to_jaxb(){
		jaxb.Controller j = new jaxb.Controller();
		j.setId(id);
		j.setDt(dt);
		j.setStartTime(start_time);
		j.setEndTime(end_time);
		j.setType(algorithm.toString());
		j.setParameters(new jaxb.Parameters());

		// TODO THIS IS TEMPORARY FOR STORING THE ACTUATOR LANE GROUP AS A PARAMETER OF THE CONTROLLER
		if(actuators!=null && actuators.size()==1){
			AbstractActuator act = actuators.values().iterator().next();

			jaxb.Parameter param = new jaxb.Parameter();
			param.setName("lane_group");
			param.setValue(act.lgtype.toString());
			j.getParameters().getParameter().add(param);
		}

		if(actuators!=null && !actuators.isEmpty()){
			jaxb.TargetActuators tgtacts = new jaxb.TargetActuators();
			j.setTargetActuators(tgtacts);
			tgtacts.setIds(OTMUtils.comma_format(actuators.values().stream().map(x->x.id).collect(toSet())));
		}

		if(sensors!=null && !sensors.isEmpty()){
			jaxb.FeedbackSensors fbsensors = new jaxb.FeedbackSensors();
			j.setFeedbackSensors(fbsensors);
			fbsensors.setIds(OTMUtils.comma_format(sensors.values().stream().map(x->x.id).collect(toSet())));
		}

		return j;
	}

	@Override
	public int compareTo(Object o) {
		AbstractController that = (AbstractController) o;
		if(this.start_time<that.start_time)
			return -1;
		else if(this.start_time>that.start_time)
			return 1;
		else return 0;
	}

	////////////////////////////////
	// API
	////////////////////////////////

	public Set<LaneGroupType> get_lanegroup_types(){
		return new HashSet<>(actuators.values().stream().map(a->a.lgtype).collect(toSet()));
	}

	public Set<Long> get_link_ids(){
		return new HashSet<>(actuators.values().stream().map(a->a.link_id).collect(toSet()));
	}

	public Set<Long>get_actuator_ids(){
		return new HashSet<>(actuators.keySet());
	}

	public Set<Long>get_sensor_ids(){
		return sensors.keySet();
	}

	public long getId() {
		return id;
	}

	public float getDt() {
		return dt;
	}

	public void setDt(float dt) {
		this.dt = dt;
	}

	public float getStartTime() {
		return start_time;
	}

	public void setStartTime(float start_time) {
		this.start_time = start_time;
	}

	public float getEndTime() {
		return end_time;
	}

	public void setEndTime(float end_time) {
		this.end_time = end_time;
	}

	public Map<Long,AbstractActuator> get_actuators(){
		return actuators;
	}

	public Map<Long,Sensor> get_sensors(){
		return sensors;
	}

	public void setId(long id){
		this.id = id;
	}
}
