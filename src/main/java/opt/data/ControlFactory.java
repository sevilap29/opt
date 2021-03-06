package opt.data;

import error.OTMException;
import opt.data.control.*;
import opt.utils.BijectiveMap;
import utils.OTMUtils;

import java.util.*;

public class ControlFactory {

	public static BijectiveMap<control.AbstractController.Algorithm,String> cntrl_alg_name;
	static{
		cntrl_alg_name = new BijectiveMap<>();
		cntrl_alg_name.put(control.AbstractController.Algorithm.alinea,"Alinea");
		cntrl_alg_name.put(control.AbstractController.Algorithm.fixed_rate,"TOD");
	}

	public static List<control.AbstractController.Algorithm> get_available_ramp_metering_algorithms(){
		List<control.AbstractController.Algorithm> x = new ArrayList<>();
		x.add(control.AbstractController.Algorithm.alinea);
		x.add(control.AbstractController.Algorithm.fixed_rate);
		return x;
	}

	public static List<String> get_available_ramp_metering_names(){
		List<String> x = new ArrayList<>();
		x.add(cntrl_alg_name.AtoB(control.AbstractController.Algorithm.alinea));
		x.add(cntrl_alg_name.AtoB(control.AbstractController.Algorithm.fixed_rate));
		return x;
	}

	/////////////////////
	// controller
	/////////////////////

	public static ControllerRampMeterAlinea create_controller_alinea(FreewayScenario fwyscn,Long id, float dt, float start_time, float end_time, boolean has_queue_control, float min_rate_vph, float max_rate_vph,Long sensor_id, long sensor_link_id, float sensor_offset,Long act_id, long ramp_link_id, LaneGroupType lgtype) throws Exception {
		parameters_check(dt,start_time,end_time);
		return new ControllerRampMeterAlinea(fwyscn,id,dt,start_time,end_time,has_queue_control,min_rate_vph,max_rate_vph,sensor_id,sensor_link_id,sensor_offset,act_id,ramp_link_id,lgtype);
	}

	public static ControllerRampMeterTOD create_controller_tod(FreewayScenario fwyscn,Long id, float dt, float start_time, Float end_time, boolean has_queue_control, float min_rate_vph, float max_rate_vph,Long act_id, long ramp_link_id, LaneGroupType lgtype) throws Exception {
		parameters_check(dt,start_time,end_time);
		return new ControllerRampMeterTOD(fwyscn,id,dt,start_time,end_time,has_queue_control,min_rate_vph,max_rate_vph,act_id,ramp_link_id,lgtype);
	}

	public static ControllerPolicyHOV create_controller_hov(FreewayScenario fwyscn,Long id, float dt, float start_time, Float end_time) throws Exception {
		parameters_check(dt,start_time,end_time);
		ControllerPolicyHOV ctrl = new ControllerPolicyHOV(fwyscn,id,dt,start_time,end_time);
		return ctrl;
	}

	public static ControllerPolicyHOT create_controller_hot(FreewayScenario fwyscn,Long id, float dt, float start_time, Float end_time) throws Exception {
		parameters_check(dt,start_time,end_time);
		ControllerPolicyHOT ctrl = new ControllerPolicyHOT(fwyscn,id,dt,start_time,end_time);
		return ctrl;
	}

	/////////////////////
	// sensor
	/////////////////////

	public static Sensor create_sensor(FreewayScenario fwyscn,Long sensor_id, long link_id, float offset, AbstractController myController){
		return new Sensor(sensor_id!=null?sensor_id:fwyscn.new_sensor_id(),link_id,offset,myController);
	}

	/////////////////////
	// actuator
	/////////////////////

	public static ActuatorRampMeter create_ramp_meter(FreewayScenario fwyscn,Long act_id,long link_id,LaneGroupType lgtype, AbstractController myController){
		return new ActuatorRampMeter(act_id!=null?act_id:fwyscn.new_actuator_id(),link_id,lgtype,myController);
	}

	public static ActuatorPolicy create_policy_actuator(FreewayScenario fwyscn,Long act_id,long link_id,LaneGroupType lgtype, AbstractController myController){
		return new ActuatorPolicy(fwyscn.new_actuator_id(),link_id,lgtype,myController);
	}

	/////////////////////////
	// jaxb
	/////////////////////////

	public static ControllerRampMeterAlinea create_controller_alinea(FreewayScenario fwyscn, jaxb.Controller jcnt, Map<Long,jaxb.Actuator> actuator_pool, Map<Long,jaxb.Sensor> sensor_pool) throws Exception {

		// complex sensors : check that there are none
		if(jcnt.getFeedbackSensors()!=null)
			assert(jcnt.getFeedbackSensors().getFeedbackSensor().isEmpty());

		// complex actuators : check that there are none
		if(jcnt.getTargetActuators()!=null)
			assert(jcnt.getTargetActuators().getTargetActuator().isEmpty());

		// read parameters
		boolean has_queue_control = false;
		LaneGroupType lgtype = LaneGroupType.gp;
		if(jcnt.getParameters()!=null)
			for(jaxb.Parameter param : jcnt.getParameters().getParameter()){
				switch(param.getName()){
					case "queue_control":
						has_queue_control = param.getValue().equals("true");
						break;
					case "lane_group":
						lgtype = LaneGroupType.valueOf(param.getValue());
						break;
					default:
						throw new Exception("Unknown controller parameter");
				}
			}

		// read actuators
		long act_id = -1l;
		long ramp_link_id = -1l;
		float min_rate_vph = -1f;
		float max_rate_vph = -1f;
		List<Long> act_ids = OTMUtils.csv2longlist(jcnt.getTargetActuators().getIds());
		assert(act_ids.size()==1);
		for(long a : act_ids) {
			jaxb.Actuator jact = actuator_pool.get(a);
			act_id = jact.getId();
			min_rate_vph = jact.getMinValue();
			max_rate_vph = jact.getMaxValue();
			ramp_link_id = jact.getActuatorTarget().getId();
		}

		// read sensors
		long sensor_id = -1l;
		long sensor_link_id = -1l;
		float sensor_offset = -1f;
		List<Long> sens_ids = OTMUtils.csv2longlist(jcnt.getFeedbackSensors().getIds());
		assert(sens_ids.size()==1);
		for(long sens_id :sens_ids) {
			jaxb.Sensor jsns = sensor_pool.get(sens_id);
			sensor_id = jsns.getId();
			sensor_link_id = jsns.getLinkId();
			sensor_offset = jsns.getPosition();
		}

		ControllerRampMeterAlinea cntrl = create_controller_alinea(fwyscn,jcnt.getId(),jcnt.getDt(),jcnt.getStartTime(),jcnt.getEndTime(),has_queue_control,min_rate_vph,max_rate_vph,sensor_id,sensor_link_id,sensor_offset,act_id,ramp_link_id,lgtype);

		cntrl.setId( jcnt.getId() );

		return cntrl;
	}

	public static ControllerRampMeterTOD create_controller_tod(FreewayScenario fwyscn,jaxb.Controller jcnt, Map<Long,jaxb.Actuator> actuator_pool) throws Exception {

		// read parameters
		boolean has_queue_control = false;
		LaneGroupType lgtype = LaneGroupType.gp;
		if(jcnt.getParameters()!=null)
			for(jaxb.Parameter param : jcnt.getParameters().getParameter()){
				switch(param.getName()){
					case "queue_control":
						has_queue_control = param.getValue().equals("true");
						break;
					case "lane_group":
						lgtype = LaneGroupType.valueOf(param.getValue());
						break;
					default:
						throw new Exception("Unknown controller parameter");
				}
			}

		// read actuators
		long act_id = -1l;
		long ramp_link_id = -1l;
		float min_rate_vph = -1f;
		float max_rate_vph = -1f;
		List<Long> act_ids = OTMUtils.csv2longlist(jcnt.getTargetActuators().getIds());
		assert(act_ids.size()==1);
		for(long a : act_ids) {
			jaxb.Actuator jact = actuator_pool.get(a);
			act_id = jact.getId();
			min_rate_vph = jact.getMinValue();
			max_rate_vph = jact.getMaxValue();
			ramp_link_id = jact.getActuatorTarget().getId();
		}

		ControllerRampMeterTOD cntrl = create_controller_tod(fwyscn,jcnt.getId(),jcnt.getDt(),jcnt.getStartTime(),jcnt.getEndTime(),has_queue_control,min_rate_vph,max_rate_vph,act_id,ramp_link_id,lgtype);

		cntrl.setId( jcnt.getId() );

		return cntrl;

	}

	public static ControllerPolicyHOV create_controller_hov(FreewayScenario fwyscn,jaxb.Controller jcnt) throws Exception {
		ControllerPolicyHOV cntrl = create_controller_hov(fwyscn,jcnt.getId(),jcnt.getDt(),jcnt.getStartTime(),jcnt.getEndTime());
		cntrl.setId( jcnt.getId() );
		return cntrl;
	}

	public static ControllerPolicyHOT create_controller_hot(FreewayScenario fwyscn,jaxb.Controller jcnt) throws Exception {
		ControllerPolicyHOT cntrl = create_controller_hot(fwyscn,jcnt.getId(),jcnt.getDt(),jcnt.getStartTime(),jcnt.getEndTime());
		cntrl.setId( jcnt.getId() );
		return cntrl;
	}

	/////////////////////////
	// private
	/////////////////////////

	private static void parameters_check(float dt,float start_time,Float end_time) throws OTMException {
		if(dt<=0f)
			throw new OTMException("dt<=0f");
		if(start_time<0)
			throw new OTMException("start_time<0");
		if(end_time!=null && end_time<=start_time)
			throw new OTMException("end_time!=null && end_time<=start_time");
	}

}
