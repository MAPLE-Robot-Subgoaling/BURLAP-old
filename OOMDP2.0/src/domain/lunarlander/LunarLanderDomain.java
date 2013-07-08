package domain.lunarlander;

import java.util.List;

import oomdptb.oomdp.Action;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.DomainGenerator;
import oomdptb.oomdp.ObjectClass;
import oomdptb.oomdp.ObjectInstance;
import oomdptb.oomdp.PropositionalFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.explorer.TerminalExplorer;
import oomdptb.oomdp.explorer.VisualExplorer;
import oomdptb.oomdp.visualizer.Visualizer;


public class LunarLanderDomain implements DomainGenerator {

	public static final String				XATTNAME = "xAtt"; //x attribute
	public static final String				YATTNAME = "yAtt"; //y attribute
	
	public static final String				VXATTNAME = "vxAtt"; //velocity x attribute
	public static final String				VYATTNAME = "vyAtt"; //velocity y attribute
	
	public static final String				AATTNAME = "angAtt"; //angle of lander
	
	public static final String				LATTNAME = "lAtt"; //left boundary
	public static final String				RATTNAME = "rAtt"; //right boundary
	public static final String				BATTNAME = "bAtt"; //bottom boundary
	public static final String				TATTNAME = "tAtt"; //top boundary
	
	
	
	public static final String				AGENTCLASS = "agent";
	public static final String				OBSTACLECLASS = "obstacle";
	public static final String				PADCLASS = "goal";
	
	
	public static final String				ACTIONTURNL = "turnLeft";
	public static final String				ACTIONTURNR = "turnRight";
	public static final String				ACTIONHTHRUST = "hThrust";
	public static final String				ACTIONWTHRUST = "wThrust";
	public static final String				ACTIONMTHRUST = "mThrust";
	public static final String				ACTIONIDLE = "idle";
	
	
	public static final String				PFONPAD = "onLandingPad";
	public static final String				PFTPAD = "touchingLandingPad";
	public static final String				PFTOUCHSURFACE = "touchingSurface"; //either horitonzally or landed on obstacle
	public static final String				PFONGROUND = "onGround"; //landed on ground
	
	
	public static final double				XMIN = 0.;
	public static final double				XMAX = 100.;
	public static final double				YMIN = 0.;
	public static final double				YMAX = 50.;
	public static final double				VMAX = 4.0;
	public static final double				ANGLEMAX = Math.PI/4.;
	public static final double				ANGLEINC = Math.PI/20.;
	
	public static final double				GRAVTIY = -0.2;
	public static double					HTHRUST = 0.32;
	public static double					WTHRUST = -GRAVTIY;
	public static double					MTHRUST = 0.26;
	
	
	
	private static Domain					LLDOMAIN = null;
	

	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		LunarLanderDomain lld = new LunarLanderDomain();
		lld.generateDomain();
		
		State clean = lld.getCleanState(1);

		/*//these commented items just have different task configuration; just choose one
		lld.setAgent(clean, 0., 5, 0.);
		lld.setObstacle(clean, 0, 30., 45., 0., 20.);
		lld.setPad(clean, 75., 95., 0., 10.);
		*/
		
		/*
		lld.setAgent(clean, 0., 5, 0.);
		lld.setObstacle(clean, 0, 20., 40., 0., 20.);
		lld.setPad(clean, 65., 85., 0., 10.);
		*/
		
		
		lld.setAgent(clean, 0., 5, 0.);
		lld.setObstacle(clean, 0, 20., 50., 0., 20.);
		lld.setPad(clean, 80., 95., 0., 10.);
		
		
		int expMode = 1;
		
		if(args.length > 0){
			if(args[0].equals("v")){
				expMode = 1;
			}
		}
		
		if(expMode == 0){
			
			TerminalExplorer te = new TerminalExplorer(LLDOMAIN);
			
			te.addActionShortHand("a", ACTIONTURNL);
			te.addActionShortHand("d", ACTIONTURNR);
			te.addActionShortHand("w", ACTIONHTHRUST);
			te.addActionShortHand("s", ACTIONWTHRUST);
			te.addActionShortHand("x", ACTIONIDLE);
			
			te.exploreFromState(clean);
			
		}
		else if(expMode == 1){
			
			Visualizer vis = LLVisualizer.getVisualizer();
			VisualExplorer exp = new VisualExplorer(LLDOMAIN, vis, clean);
			
			exp.addKeyAction("w", ACTIONHTHRUST);
			exp.addKeyAction("s", ACTIONWTHRUST);
			exp.addKeyAction("a", ACTIONTURNL);
			exp.addKeyAction("d", ACTIONTURNR);
			exp.addKeyAction("x", ACTIONIDLE);
			
			exp.initGUI();
			
		}

	}
	
	
	
	public void setAgent(State s, double a, double x, double y){
		this.setAgent(s, a, x, y, 0., 0.);
	}
	
	public void setAgent(State s, double a, double x, double y, double vx, double vy){
		ObjectInstance agent = s.getObjectsOfTrueClass(AGENTCLASS).get(0);
		
		agent.setValue(AATTNAME, a);
		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(VXATTNAME, vx);
		agent.setValue(VYATTNAME, vy);
	}
	
	public void setObstacle(State s, int i, double l, double r, double b, double t){
		ObjectInstance obst = s.getObjectsOfTrueClass(OBSTACLECLASS).get(i);
		
		obst.setValue(LATTNAME, l);
		obst.setValue(RATTNAME, r);
		obst.setValue(BATTNAME, b);
		obst.setValue(TATTNAME, t);
	}
	
	public void setPad(State s, double l, double r, double b, double t){
		ObjectInstance pad = s.getObjectsOfTrueClass(PADCLASS).get(0);
		
		pad.setValue(LATTNAME, l);
		pad.setValue(RATTNAME, r);
		pad.setValue(BATTNAME, b);
		pad.setValue(TATTNAME, t);
	}
	
	
	
	@Override
	public Domain generateDomain() {
		
		if(LLDOMAIN != null){
			return LLDOMAIN;
		}
		
		LLDOMAIN = new Domain();
		
		//create attributes
		Attribute xatt = new Attribute(LLDOMAIN, XATTNAME, Attribute.AttributeType.REAL);
		xatt.setLims(XMIN, XMAX);
		
		Attribute yatt = new Attribute(LLDOMAIN, YATTNAME, Attribute.AttributeType.REAL);
		yatt.setLims(YMIN, YMAX);
		
		Attribute vxatt = new Attribute(LLDOMAIN, VXATTNAME, Attribute.AttributeType.REAL);
		vxatt.setLims(-VMAX, VMAX);
		
		Attribute vyatt = new Attribute(LLDOMAIN, VYATTNAME, Attribute.AttributeType.REAL);
		vyatt.setLims(-VMAX, VMAX);
		
		Attribute aatt = new Attribute(LLDOMAIN, AATTNAME, Attribute.AttributeType.REAL);
		aatt.setLims(-ANGLEMAX, ANGLEMAX);
		
		Attribute latt = new Attribute(LLDOMAIN, LATTNAME, Attribute.AttributeType.REAL);
		latt.setLims(XMIN, XMAX);
		
		Attribute ratt = new Attribute(LLDOMAIN, RATTNAME, Attribute.AttributeType.REAL);
		ratt.setLims(XMIN, XMAX);
		
		Attribute batt = new Attribute(LLDOMAIN, BATTNAME, Attribute.AttributeType.REAL);
		batt.setLims(YMIN, YMAX);
		
		Attribute tatt = new Attribute(LLDOMAIN, TATTNAME, Attribute.AttributeType.REAL);
		tatt.setLims(YMIN, YMAX);
		
		
		
		
		
		//create classes
		ObjectClass agentclass = new ObjectClass(LLDOMAIN, AGENTCLASS);
		agentclass.addAttribute(xatt);
		agentclass.addAttribute(yatt);
		agentclass.addAttribute(vxatt);
		agentclass.addAttribute(vyatt);
		agentclass.addAttribute(aatt);
		
		ObjectClass obstclss = new ObjectClass(LLDOMAIN, OBSTACLECLASS);
		obstclss.addAttribute(latt);
		obstclss.addAttribute(ratt);
		obstclss.addAttribute(batt);
		obstclss.addAttribute(tatt);
		
		
		ObjectClass padclass = new ObjectClass(LLDOMAIN, PADCLASS);
		padclass.addAttribute(latt);
		padclass.addAttribute(ratt);
		padclass.addAttribute(batt);
		padclass.addAttribute(tatt);
		
		
		//add actions
		Action turnl = new ActionTurnL(ACTIONTURNL, LLDOMAIN, "");
		Action turnr = new ActionTurnR(ACTIONTURNR, LLDOMAIN, "");
		Action idle = new ActionIdle(ACTIONIDLE, LLDOMAIN, "");
		Action hthrust = new ActionHThrust(ACTIONHTHRUST, LLDOMAIN, "");
		Action wthrust = new ActionWThrust(ACTIONWTHRUST, LLDOMAIN, "");
		Action mthrust = new ActionMThrust(ACTIONMTHRUST, LLDOMAIN, "");
		
		
		//add pfs
		PropositionalFunction onpad = new OnPadPF(PFONPAD, LLDOMAIN, new String[]{AGENTCLASS, PADCLASS});
		PropositionalFunction touchpad = new TouchPadPF(PFTPAD, LLDOMAIN, new String[]{AGENTCLASS, PADCLASS});
		PropositionalFunction touchsur = new TouchSurfacePF(PFTOUCHSURFACE, LLDOMAIN, new String[]{AGENTCLASS, OBSTACLECLASS});
		PropositionalFunction touchgrd = new TouchGroundPF(PFONGROUND, LLDOMAIN, new String[]{AGENTCLASS});
		
		
		
		return LLDOMAIN;
		
	}
	
	
	public State getCleanState(int no){
		
		this.generateDomain();
		
		State s = new State();
		
		ObjectInstance agent = new ObjectInstance(LLDOMAIN.getObjectClass(AGENTCLASS), AGENTCLASS + "0");
		s.addObject(agent);
		
		ObjectInstance pad = new ObjectInstance(LLDOMAIN.getObjectClass(PADCLASS), PADCLASS + "0");
		s.addObject(pad);
		
		for(int i = 0; i < no; i++){
			ObjectInstance obst = new ObjectInstance(LLDOMAIN.getObjectClass(OBSTACLECLASS), OBSTACLECLASS + i);
			s.addObject(obst);
		}

		return s;
		
	}

	
	
	public static void incAngle(State s, int dir){
		
		ObjectInstance agent = s.getObjectsOfTrueClass(AGENTCLASS).get(0);
		double curA = agent.getRealValForAttribute(AATTNAME);
		
		double newa = curA + (dir * ANGLEINC);
		if(newa > ANGLEMAX){
			newa = ANGLEMAX;
		}
		else if(newa < -ANGLEMAX){
			newa = -ANGLEMAX;
		}
		
		agent.setValue(AATTNAME, newa);
		
	}
	
	public static void updateMotion(State s, double thrust){
		
		double ti = 1.;
		double tt = ti*ti;
		
		ObjectInstance agent = s.getObjectsOfTrueClass(AGENTCLASS).get(0);
		double ang = agent.getRealValForAttribute(AATTNAME);
		double x = agent.getRealValForAttribute(XATTNAME);
		double y = agent.getRealValForAttribute(YATTNAME);
		double vx = agent.getRealValForAttribute(VXATTNAME);
		double vy = agent.getRealValForAttribute(VYATTNAME);
		
		double worldAngle = (Math.PI/2.) - ang;
		
		double tx = Math.cos(worldAngle)*thrust;
		double ty = Math.sin(worldAngle)*thrust;
		
		double ax = tx;
		double ay = ty + GRAVTIY;
		
		double nx = x + vx*ti + (0.5*ax*tt);
		double ny = y + vy*ti + (0.5*ay*tt);
		
		double nvx = vx + ax*ti;
		double nvy = vy + ay*ti;
		
		double nang = ang;
		
		//check for boundaries
		if(ny > YMAX){
			ny = YMAX;
			nvy = 0.;
		}
		else if(ny <= YMIN){
			ny = YMIN;
			nvy = 0.;
			nang = 0.;
			nvx = 0.;
		}
		
		if(nx > XMAX){
			nx = XMAX;
			nvx = 0.;
		}
		else if(nx < XMIN){
			nx = XMIN;
			nvx = 0.;
		}
		
		if(nvx > VMAX){
			nvx = VMAX;
		}
		else if(nvx < -VMAX){
			nvx = -VMAX;
		}
		
		if(nvy > VMAX){
			nvy = VMAX;
		}
		else if(nvy < -VMAX){
			nvy = -VMAX;
		}
		
		
		
		//check for collisions
		List <ObjectInstance> obstacles = s.getObjectsOfTrueClass(OBSTACLECLASS);
		for(ObjectInstance o : obstacles){
			double l = o.getRealValForAttribute(LATTNAME);
			double r = o.getRealValForAttribute(RATTNAME);
			double b = o.getRealValForAttribute(BATTNAME);
			double t = o.getRealValForAttribute(TATTNAME);
			
			//are we intersecting?
			if(nx > l && nx < r && ny >= b && ny < t){
				//intersection!
				
				//from which direction did we hit it (check previous position)?
				if(x <= l){
					nx = l;
					nvx = 0.;
				}
				else if(x >= r){
					nx = r;
					nvx = 0.;
				}
				
				if(y <= b){
					ny = b;
					nvy = 0.;
				}
				else if(y >= t){
					ny = t;
					nvy = 0.;
					nang = 0.;
					nvx = 0.;
				}
				
				
				//can only hit one obsbtacle so break out of search
				break;
				
			}
			
			
		}
		
		
		//check the pad collision
		ObjectInstance pad = s.getObjectsOfTrueClass(PADCLASS).get(0);
		double l = pad.getRealValForAttribute(LATTNAME);
		double r = pad.getRealValForAttribute(RATTNAME);
		double b = pad.getRealValForAttribute(BATTNAME);
		double t = pad.getRealValForAttribute(TATTNAME);
		
		//did we colloide?
		if(nx > l && nx < r && ny >= b && ny < t){
			//intersection!
			
			//from which direction did we hit it (check previous position)?
			if(x <= l){
				nx = l;
				nvx = 0.;
			}
			else if(x >= r){
				nx = r;
				nvx = 0.;
			}
			
			if(y <= b){
				ny = b;
				nvy = 0.;
			}
			else if(y >= t){
				ny = t;
				nvy = 0.;
				nang = 0.;
				nvx = 0.;
			}

			
		}
		
		
		
		
		//now set the new values
		agent.setValue(XATTNAME, nx);
		agent.setValue(YATTNAME, ny);
		agent.setValue(VXATTNAME, nvx);
		agent.setValue(VYATTNAME, nvy);
		agent.setValue(AATTNAME, nang);
		
		
	}
	
	
	
	
	
	public class ActionTurnL extends Action{

		public ActionTurnL(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public ActionTurnL(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			LunarLanderDomain.incAngle(st, -1);
			LunarLanderDomain.updateMotion(st, 0.0);
			return st;
		}
		
		
		
	}
	
	public class ActionTurnR extends Action{

		public ActionTurnR(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public ActionTurnR(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			LunarLanderDomain.incAngle(st, 1);
			LunarLanderDomain.updateMotion(st, 0.0);
			return st;
		}
		
		
		
	}
	
	public class ActionIdle extends Action{

		public ActionIdle(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public ActionIdle(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			LunarLanderDomain.updateMotion(st, 0.0);
			return st;
		}
		
		
		
	}
	
	public class ActionHThrust extends Action{

		public ActionHThrust(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public ActionHThrust(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			LunarLanderDomain.updateMotion(st, HTHRUST);
			return st;
		}
		
		
		
	}
	
	public class ActionWThrust extends Action{

		public ActionWThrust(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public ActionWThrust(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			LunarLanderDomain.updateMotion(st, WTHRUST);
			return st;
		}
		
		
	}
	
	public class ActionMThrust extends Action{

		public ActionMThrust(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public ActionMThrust(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			LunarLanderDomain.updateMotion(st, MTHRUST);
			return st;
		}
		
		
	}
	
	
	/*
	 * Returns true if the agent is not only touching the landing pad, but has landed on the surface
	 */
	
	public class OnPadPF extends PropositionalFunction{

		public OnPadPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public OnPadPF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
	
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance pad = st.getObject(params[1]);
			
			
			double l = pad.getRealValForAttribute(LATTNAME);
			double r = pad.getRealValForAttribute(RATTNAME);
			double b = pad.getRealValForAttribute(BATTNAME);
			double t = pad.getRealValForAttribute(TATTNAME);
			
			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);
			
			//on pad means landed on surface, so y should be equal to top
			if(x > l && x < r && y == t){
				return true;
			}
			

			return false;
		}
		
		
		
	}
	
	
	/*
	 * Return true if the agent is touching the landing pad
	 */
	
	public class TouchPadPF extends PropositionalFunction{

		public TouchPadPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public TouchPadPF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
	
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance pad = st.getObject(params[1]);
			
			
			double l = pad.getRealValForAttribute(LATTNAME);
			double r = pad.getRealValForAttribute(RATTNAME);
			double b = pad.getRealValForAttribute(BATTNAME);
			double t = pad.getRealValForAttribute(TATTNAME);
			
			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);
			
			//on pad means landed on surface, so y should be equal to top
			if(x >= l && x < r && y >= b && y <= t){
				return true;
			}
			

			return false;
		}
		
		
		
	}
	
	
	/*
	 * Return true if the agent is touching an obstacle surface
	 */
	
	public class TouchSurfacePF extends PropositionalFunction{

		public TouchSurfacePF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public TouchSurfacePF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance o = st.getObject(params[1]);
			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);
			
			double l = o.getRealValForAttribute(LATTNAME);
			double r = o.getRealValForAttribute(RATTNAME);
			double b = o.getRealValForAttribute(BATTNAME);
			double t = o.getRealValForAttribute(TATTNAME);
			
			if(x >= l && x <= r && y >= b && y <= t){
				return true;
			}
			
			return false;
		}
		
		
		
	}
	
	/*
	 * Returns true if the agent is touching the ground
	 */
	public class TouchGroundPF extends PropositionalFunction{

		public TouchGroundPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}
		
		public TouchGroundPF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(params[0]);
			double y = agent.getRealValForAttribute(YATTNAME);
			
			if(y == YMIN){
				return true;
			}
			
			return false;
		}
		
		
		
	}
	
	

}
