package opt.simulation;

import api.OTMdev;
import common.Link;
import error.OTMException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import opt.AppMainController;
import opt.data.FreewayScenario;

import java.util.Iterator;

public class OTMTask  extends Task {

	private OTMdev otmdev;
	private Exception exception;

	private float start_time;
	private float duration;
	private int numsteps;
	private int step_per_progbar_update;
	private AppMainController mainController;
	private float simdt;


	public OTMTask(AppMainController mainController, FreewayScenario fwyscenario,float start_time, float duration, int progbar_steps){

		this.mainController = mainController;
		this.start_time = start_time;
		this.duration = duration;

		// bind the progress bar and make it visible
		if(mainController!=null)
			mainController.bindSimProgress(progressProperty());

		// create a runnable OTM scenario
		try {
			jaxb.Scenario jscenario = fwyscenario.get_scenario().to_jaxb();

			// TODO REMOVE THIS ------------------------------------------------
//			ProjectFactory.save_scenario(jscenario,"/home/gomes/code/opt/before.xml");
			remove_unsimulatable_stuff(jscenario);
//			ProjectFactory.save_scenario(jscenario,"/home/gomes/code/opt/after.xml");
			// TODO ------------------------------------------------------------

			api.OTM otm = new api.OTM();
			otm.load_from_jaxb(jscenario,true);
			this.otmdev = new OTMdev(otm);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// number of time steps in the simulation
		this.simdt = 2f;
		this.numsteps = (int) Math.ceil(duration/simdt);

		// number of steps per progrbar update
		this.step_per_progbar_update = Math.max( numsteps / progbar_steps , 1);

	}

	@Override
	protected Object call()  {
		this.run_simulation();
		return null;
	}

	@Override
	protected void done() {
		super.done();

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// unbind progress bar and make it invisible.
				if (mainController!=null) {
					mainController.unbindSimProgress();
					mainController.completeSimulation();
				}
			}
		});
	}

	public NetworkData run_simulation(){

		NetworkData netdata = new NetworkData(otmdev.scenario);

		try {

			otmdev.otm.initialize(start_time);

			int steps_taken = 0;
			netdata.update(start_time,otmdev.scenario);

			while(steps_taken<numsteps){

				if (isCancelled())
					break;

				// advance otm, get back information
				otmdev.otm.advance(simdt);
				steps_taken += 1;

				// retrieve information
				netdata.update(start_time+simdt*steps_taken,otmdev.scenario);

				// progress bar
				if(mainController!=null && steps_taken%step_per_progbar_update==0){
					final int ii = steps_taken;
					Platform.runLater(new Runnable() {
						@Override public void run() {
							updateProgress(ii, numsteps);
						}
					});
				}

			}

		} catch (OTMException e) {
			this.exception = e;
			failed();
		}

		return netdata;

	}

	private static void remove_unsimulatable_stuff(jaxb.Scenario scn){

		// remove road geometries (HOV lanes)
		if(scn.getNetwork().getRoadgeoms()!=null){
			scn.getNetwork().setRoadgeoms(null);
			for(jaxb.Link link : scn.getNetwork().getLinks().getLink()){
				link.setRoadgeom(null);
			}
		}

		// remove controllers on non-gp lanegroups
		// remove alinra controllers
		if(scn.getControllers()!=null){
			Iterator<jaxb.Controller> it = scn.getControllers().getController().iterator();
			while(it.hasNext()){
				jaxb.Controller cntrl = it.next();

				if(cntrl.getParameters()!=null) {
					boolean foundit = false;
					for (jaxb.Parameter param : cntrl.getParameters().getParameter()) {
						if (param.getName() == "lane_group" && param.getValue() != "gp")
							foundit = true;
					}
					if(foundit){
						it.remove();
						continue;
					}
				}

				if(cntrl.getType()=="alinea"){
					it.remove();
					continue;
				}
			}
		}

	}

}
