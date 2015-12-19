package burlap.oomdp.visualizer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

/**
 * This class provides 2D visualization of states by being provided a set of
 * classes that can paint ObjectInstnaces to the canvas as well as classes that
 * can paint general domain information. Painters for object classes as well as
 * specific object instances can be provided. If there is a painter for an
 * object class and a painter for a specific object instance of that same class,
 * then the specific object instance painter will be used to pain that object
 * instead of the painter for that instnace's OO-MDP class.
 * <p/>
 * The order of painting is first the static painters, in the order they were
 * added; then the object class painters in the order they were painted (except
 * for objects that have specific object painter); finally, the object instances
 * that have specific painter associated with them, in the order that they
 * appear in the state object list.
 * 
 * @author James MacGlashan
 * 
 */
public class StateRenderLayer implements RenderLayer {

	/**
	 * the current state to be painted next
	 */
	private State curState;

	/**
	 * list of static painters that pain static non-object defined properties of
	 * the domain
	 */
	private List<StaticPainter> staticPainters;

	/**
	 * Ordered list of painters for each object class
	 */
	private List<ObjectPainterAndClassNamePair> objectClassPainterList;

	/**
	 * Map of painters that define how to paint specific objects; if an object
	 * it appears in both specific and general lists, the specific painter is
	 * used
	 */
	private Map<String, ObjectPainter> specificObjectPainters;

	public StateRenderLayer() {
		curState = null;

		staticPainters = new ArrayList<StaticPainter>();
		specificObjectPainters = new HashMap<String, ObjectPainter>();
		objectClassPainterList = new ArrayList<ObjectPainterAndClassNamePair>();
	}

	/**
	 * Adds a static painter for the domain.
	 * 
	 * @param sp
	 *            the static painter to add.
	 */
	public void addStaticPainter(StaticPainter sp) {
		staticPainters.add(sp);
	}

	/**
	 * Adds a class that will paint objects that belong to a given OO-MDPclass.
	 * 
	 * @param className
	 *            the name of the class that the provided painter can paint
	 * @param op
	 *            the painter
	 */
	public void addObjectClassPainter(String className, ObjectPainter op) {
		objectClassPainterList.add(new ObjectPainterAndClassNamePair(className,
				op));
	}

	/**
	 * Adds a painter that will be used to paint a specific object in states
	 * 
	 * @param objectName
	 *            the name of the object this painter is used to paint
	 * @param op
	 *            the painter
	 */
	public void addSpecificObjectPainter(String objectName, ObjectPainter op) {
		specificObjectPainters.put(objectName, op);
	}

	/**
	 * Updates the state that needs to be painted
	 * 
	 * @param s
	 *            the state to paint
	 */
	public void updateState(State s) {
		curState = s;
	}

	@Override
	public void render(Graphics2D g2, float width, float height) {

		if (this.curState == null) {
			return; // don't render anything if there is no state to render
		}

		// draw the static properties
		for (StaticPainter sp : staticPainters) {
			sp.paint(g2, curState, width, height);
		}

		// draw each object by class order and if there is not a specific
		// painter for
		for (ObjectPainterAndClassNamePair op : this.objectClassPainterList) {
			List<ObjectInstance> objects = curState
					.getObjectsOfClass(op.className);
			for (ObjectInstance o : objects) {
				if (!this.specificObjectPainters.containsKey(o.getName())) {
					List<String> unsetAttributes = o.unsetAttributes();
					if (unsetAttributes.size() > 0) {
						System.out
								.println("Warning: not rendering object "
										+ o.getName()
										+ " because it has the following unset attributes: ");
						for (String a : unsetAttributes) {
							System.out.println("    " + a);
						}
						continue;
					}
					op.painter.paintObject(g2, curState, o, width, height);
				}
			}
		}

		// draw each object if there is a painter to do so
		List<ObjectInstance> objects = curState.getAllObjects();
		for (ObjectInstance o : objects) {

			// is there a specific object painter for this object?
			if (specificObjectPainters.containsKey(o.getName())) {
				List<String> unsetAttributes = o.unsetAttributes();
				if (unsetAttributes.size() > 0) {
					System.out
							.println("Warning: not rendering object "
									+ o.getName()
									+ " because it has the following unset attributes: ");
					for (String a : unsetAttributes) {
						System.out.println("    " + a);
					}
					continue;
				}
				specificObjectPainters.get(o.getName()).paintObject(g2,
						curState, o, width, height);
			}

		}

	}

	static class ObjectPainterAndClassNamePair {
		String className;
		ObjectPainter painter;

		public ObjectPainterAndClassNamePair(String className,
				ObjectPainter painter) {
			this.className = className;
			this.painter = painter;
		}
	}

}
