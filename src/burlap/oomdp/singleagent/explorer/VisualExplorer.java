package burlap.oomdp.singleagent.explorer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.NullRewardFunction;
import burlap.oomdp.visualizer.Visualizer;

/**
 * This class allows you act as the agent by choosing actions to take in
 * specific states. States are conveyed to the user through a 2D visualization
 * and the user specifies actions by either pressing keys that are mapped to
 * actions or by typing the actions into the action command field. Action
 * parameters in the action field are specified by space delineated input. For
 * instance: "stack block0 block1" will cause the stack action to called with
 * action parameters block0 and block1. The ` key causes the state to reset to
 * the initial state provided to the explorer or to a state that is sampled from
 * a provided {@link StateGenerator} object. Other special kinds of actions not
 * described in the domain can be added and executed by pressing corresponding
 * keys for them. The episodes of action taken by a user may also be recorded to
 * a list of recorded episodes and then subsequentlly polled by a client object.
 * To enable episode recording, use the method
 * {@link #enableEpisodeRecording(String, String)} or
 * {@link #enableEpisodeRecording(String, String, RewardFunction)}. To check if
 * the user is still recording episodes, use the method {@link #isRecording()}.
 * To retrieve the recorded episodes, use the method
 * {@link #getRecordedEpisodes()}.
 * 
 * @author James MacGlashan
 * 
 */
public class VisualExplorer extends JFrame {

    private static final long serialVersionUID = 1L;

    protected Domain domain;
    protected Map<String, String> keyActionMap;
    protected Map<String, SpecialExplorerAction> keySpecialMap;
    protected State baseState;
    protected State curState;

    protected Visualizer painter;
    protected TextArea propViewer;
    protected TextField actionField;
    protected JButton actionButton;
    protected int cWidth;
    protected int cHeight;

    protected int numSteps;

    // recording data members
    protected EpisodeAnalysis currentEpisode = null;
    protected List<EpisodeAnalysis> recordedEpisodes = null;
    protected RewardFunction trackingRewardFunction = new NullRewardFunction();

    protected boolean isRecording = false;

    /**
     * Initializes the visual explorer with the domain to explorer, the
     * visualizer to use, and the base state from which to explore.
     * 
     * @param domain
     *            the domain to explore
     * @param painter
     *            the 2D state visualizer
     * @param baseState
     *            the initial state from which to explore
     */
    public VisualExplorer(Domain domain, Visualizer painter, State baseState) {

	this.init(domain, painter, new ConstantStateGenerator(baseState), 800,
		800);
    }

    /**
     * Initializes the visual explorer with the domain to explorer, the
     * visualizer to use, the base state from which to explore, and the
     * dimensions of the visualizer.
     * 
     * @param domain
     *            the domain to explore
     * @param painter
     *            the 2D state visualizer
     * @param baseState
     *            the initial state from which to explore
     * @param w
     *            the width of the visualizer canvas
     * @param h
     *            the height of the visualizer canvas
     */
    public VisualExplorer(Domain domain, Visualizer painter, State baseState,
	    int w, int h) {
	this.init(domain, painter, new ConstantStateGenerator(baseState), w, h);
    }

    /**
     * Initializes the visual explorer with the domain to explorer, the
     * visualizer to use, and an initial state generator from which to explore,
     * and the dimensions of the visualizer.
     * 
     * @param domain
     *            the domain to explore
     * @param painter
     *            the 2D state visualizer
     * @param initialStateGenerator
     *            a generator for initial states that is polled everytime the
     *            special reset action is called
     * @param w
     *            the width of the visualizer canvas
     * @param h
     *            the height of the visualizer canvas
     */
    public VisualExplorer(Domain domain, Visualizer painter,
	    StateGenerator initialStateGenerator, int w, int h) {
	this.init(domain, painter, initialStateGenerator, w, h);
    }

    protected void init(Domain domain, Visualizer painter,
	    StateGenerator stateGeneratorForReset, int w, int h) {

	this.domain = domain;
	this.baseState = stateGeneratorForReset.generateState();
	this.curState = baseState.copy();
	this.painter = painter;
	this.keyActionMap = new HashMap<String, String>();
	this.keySpecialMap = new HashMap<String, SpecialExplorerAction>();

	StateResetSpecialAction reset = new StateResetSpecialAction(
		stateGeneratorForReset);
	this.addSpecialAction("`", reset);

	this.cWidth = w;
	this.cHeight = h;

	this.propViewer = new TextArea();
	this.propViewer.setEditable(false);

	this.numSteps = 0;

    }

    /**
     * Returns a special action that causes the state to reset to the initial
     * state.
     * 
     * @return a special action that causes the state to reset to the initial
     *         state.
     */
    public StateResetSpecialAction getResetSpecialAction() {
	return (StateResetSpecialAction) keySpecialMap.get("`");
    }

    /**
     * Specifies which action to execute for a given key press
     * 
     * @param key
     *            the key that is pressed by the user
     * @param action
     *            the action to take when the key is pressed
     */
    public void addKeyAction(String key, String action) {
	keyActionMap.put(key, action);
    }

    /**
     * Specifies which special non-domain action to take for a given key press
     * 
     * @param key
     *            the key that is pressed by the user
     * @param action
     *            the special non-domain action to take when the key is pressed
     */
    public void addSpecialAction(String key, SpecialExplorerAction action) {
	keySpecialMap.put(key, action);
    }

    /**
     * Enables episodes recording of actions taken. Whenever the
     * recordLastEpisodeKey is pressed, the episode starting from the initial
     * state, or last state reset (activated with the ` key) up until the
     * current state is stored in a list of recorded episodes. When the
     * finishedRecordingKey is pressed, the {@link #isRecording()} flag is set
     * to false to let any client objects know that the list of recorded
     * episodes can be safely polled. The list of recorded episodes can be
     * polled using the method {@link #getRecordedEpisodes()}. Rewards stored in
     * the recorded episode will all be zero.
     * 
     * @param recordLastEpisodeKey
     *            the key to press to indidcate that the last episode should be
     *            recorded/saved.
     * @param finishedRecordingKey
     *            the key to press to indicate that no more episodes will be
     *            recorded so that the list of recorded episodes can be safely
     *            polled by a client object.
     */
    public void enableEpisodeRecording(String recordLastEpisodeKey,
	    String finishedRecordingKey) {
	this.currentEpisode = new EpisodeAnalysis(this.baseState);
	this.recordedEpisodes = new ArrayList<EpisodeAnalysis>();
	this.isRecording = true;

	this.keySpecialMap.put(recordLastEpisodeKey,
		new SpecialExplorerAction() {

		    @Override
		    public State applySpecialAction(State curState) {
			synchronized (VisualExplorer.this) {
			    VisualExplorer.this.recordedEpisodes
				    .add(VisualExplorer.this.currentEpisode);
			}
			return curState;
		    }
		});

	this.keySpecialMap.put(finishedRecordingKey,
		new SpecialExplorerAction() {

		    @Override
		    public State applySpecialAction(State curState) {
			synchronized (VisualExplorer.this) {
			    VisualExplorer.this.isRecording = false;
			}
			return curState;
		    }
		});

    }

    /**
     * Enables episodes recording of actions taken. Whenever the
     * recordLastEpisodeKey is pressed, the episode starting from the initial
     * state, or last state reset (activated with the ` key) up until the
     * current state is stored in a list of recorded episodes. When the
     * finishedRecordingKey is pressed, the {@link #isRecording()} flag is set
     * to false to let any client objects know that the list of recorded
     * episodes can be safely polled. The list of recorded episodes can be
     * polled using the method {@link #getRecordedEpisodes()}.
     * 
     * @param recordLastEpisodeKey
     *            the key to press to indidcate that the last episode should be
     *            recorded/saved.
     * @param finishedRecordingKey
     *            the key to press to indicate that no more episodes will be
     *            recorded so that the list of recorded episodes can be safely
     *            polled by a client object.
     * @param rewardFunction
     *            the reward function to use to record the reward received for
     *            each action taken.
     */
    public void enableEpisodeRecording(String recordLastEpisodeKey,
	    String finishedRecordingKey, RewardFunction rewardFunction) {
	this.currentEpisode = new EpisodeAnalysis(this.baseState);
	this.recordedEpisodes = new ArrayList<EpisodeAnalysis>();
	this.isRecording = true;
	this.trackingRewardFunction = rewardFunction;

	this.keySpecialMap.put(recordLastEpisodeKey,
		new SpecialExplorerAction() {

		    @Override
		    public State applySpecialAction(State curState) {
			synchronized (VisualExplorer.this) {
			    VisualExplorer.this.recordedEpisodes
				    .add(VisualExplorer.this.currentEpisode);
			}
			return curState;
		    }
		});

	this.keySpecialMap.put(finishedRecordingKey,
		new SpecialExplorerAction() {

		    @Override
		    public State applySpecialAction(State curState) {
			synchronized (VisualExplorer.this) {
			    VisualExplorer.this.isRecording = false;
			}
			return curState;
		    }
		});

    }

    /**
     * Returns whether episodes are still be recorded by a user.
     * 
     * @return true is the user is still recording episode; false otherwise.
     */
    public boolean isRecording() {
	return this.isRecording;
    }

    /**
     * Returns the list of episodes recorded by a user.
     * 
     * @return the list of episodes recorded by a user.
     */
    public List<EpisodeAnalysis> getRecordedEpisodes() {
	return this.recordedEpisodes;
    }

    /**
     * Initializes the visual explorer GUI and presents it to the user.
     */
    public void initGUI() {

	painter.setPreferredSize(new Dimension(cWidth, cHeight));
	propViewer.setPreferredSize(new Dimension(cWidth, 100));
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	Container bottomContainer = new Container();
	bottomContainer.setLayout(new BorderLayout());
	bottomContainer.add(propViewer, BorderLayout.NORTH);

	getContentPane().add(bottomContainer, BorderLayout.SOUTH);
	getContentPane().add(painter, BorderLayout.CENTER);

	addKeyListener(new KeyListener() {
	    @Override
	    public void keyPressed(KeyEvent e) {
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	    }

	    @Override
	    public void keyTyped(KeyEvent e) {
		handleKeyPressed(e);
	    }

	});

	// also add key listener to the painter in case the focus is changed
	painter.addKeyListener(new KeyListener() {
	    @Override
	    public void keyPressed(KeyEvent e) {
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	    }

	    @Override
	    public void keyTyped(KeyEvent e) {
		handleKeyPressed(e);
	    }

	});

	propViewer.addKeyListener(new KeyListener() {
	    @Override
	    public void keyPressed(KeyEvent e) {
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	    }

	    @Override
	    public void keyTyped(KeyEvent e) {
		handleKeyPressed(e);
	    }

	});

	actionField = new TextField(20);
	bottomContainer.add(actionField, BorderLayout.CENTER);

	actionButton = new JButton("Execute");
	actionButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		handleExecute();

	    }
	});
	bottomContainer.add(actionButton, BorderLayout.EAST);

	painter.updateState(baseState);
	this.updatePropTextArea(baseState);

	pack();
	setVisible(true);
    }

    protected void handleExecute() {

	String actionCommand = this.actionField.getText();

	if (actionCommand.length() == 0) {
	    return;
	}

	String[] comps = actionCommand.split(" ");
	String actionName = comps[0];

	// construct parameter list as all that remains
	String params[];
	if (comps.length > 1) {
	    params = new String[comps.length - 1];
	    for (int i = 1; i < comps.length; i++) {
		params[i - 1] = comps[i];
	    }
	} else {
	    params = new String[0];
	}

	Action action = domain.getAction(actionName);
	if (action == null) {
	    System.out.println("Unknown action: " + actionName);
	} else {
	    GroundedAction ga = new GroundedAction(action, params);
	    State nextState = ga.executeIn(curState);
	    this.currentEpisode
		    .recordTransitionTo(nextState, ga,
			    this.trackingRewardFunction.reward(curState, ga,
				    nextState));
	    curState = nextState;
	    numSteps++;

	    painter.updateState(curState);
	    this.updatePropTextArea(curState);
	}
    }

    protected void handleKeyPressed(KeyEvent e) {

	String key = String.valueOf(e.getKeyChar());

	// otherwise this could be an action, see if there is an action mapping
	String mappedAction = keyActionMap.get(key);
	if (mappedAction != null) {

	    // then we have a action for this key
	    // split the string up into components
	    String[] comps = mappedAction.split(" ");
	    String actionName = comps[0];

	    // construct parameter list as all that remains
	    String params[];
	    if (comps.length > 1) {
		params = new String[comps.length - 1];
		for (int i = 1; i < comps.length; i++) {
		    params[i - 1] = comps[i];
		}
	    } else {
		params = new String[0];
	    }

	    Action action = domain.getAction(actionName);
	    if (action == null) {
		System.out.println("Unknown action: " + actionName);
	    } else {
		GroundedAction ga = new GroundedAction(action, params);
		State nextState = ga.executeIn(curState);
		this.currentEpisode.recordTransitionTo(nextState, ga,
			this.trackingRewardFunction.reward(curState, ga,
				nextState));
		curState = nextState;
		numSteps++;
	    }

	} else {

	    SpecialExplorerAction sea = keySpecialMap.get(key);
	    if (sea != null) {
		curState = sea.applySpecialAction(curState);
	    }
	    if (sea instanceof StateResetSpecialAction) {
		System.out.println("Number of steps before reset: " + numSteps);
		numSteps = 0;
		this.currentEpisode = new EpisodeAnalysis(curState);
	    }
	}

	// now paint the screen with the new state
	painter.updateState(curState);
	this.updatePropTextArea(curState);
	// System.out.println(curState_.getStateDescription());
	// System.out.println("-------------------------------------------");

    }

    private void updatePropTextArea(State s) {

	StringBuffer buf = new StringBuffer();

	List<PropositionalFunction> props = domain.getPropFunctions();
	for (PropositionalFunction pf : props) {
	    List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
	    for (GroundedProp gp : gps) {
		if (gp.isTrue(s)) {
		    buf.append(gp.toString()).append("\n");
		}
	    }
	}
	propViewer.setText(buf.toString());

    }

}
