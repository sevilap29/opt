package opt.data.control;

import jaxb.Actuator;
import jaxb.Controller;
import opt.data.Scenario;

import java.util.Map;

public class ControllerPolicyHOT extends AbstractController {

	// from jaxb
	public ControllerPolicyHOT(Controller j, Map<Long, Actuator> a, Map<Long,jaxb.Sensor> s, Scenario scn) throws Exception {
		super(j,a,s,scn);

//		// CHECK
//		if(actuators.values().stream().anyMatch(act -> !(act instanceof opt.data.control.ActuatorPolicy)))
//			throw new OTMException("Found an HOT controller on a non-policy actuator");
	}

	// from factory
	public ControllerPolicyHOT(long id,float dt, float start_time, Float end_time) throws Exception {
		super(id,dt,start_time,end_time,"hot");

//		// CHECK
//		if(actuators.values().stream().anyMatch(act -> !(act instanceof opt.data.control.ActuatorPolicy)))
//			throw new OTMException("Found an HOT controller on a non-policy actuator");

	}

	////////////////////////////////
	// API
	////////////////////////////////

}