package beast.evolution.branchratemodel;

import beast.core.Description;
import beast.core.Input;
import beast.core.Parameter;
import beast.core.State;
import beast.evolution.tree.Node;

/**
 * @author Alexei Drummond
 */

@Description("Defines a mean rate for each branch in the beast.tree.")
public class StrictClockModel extends BranchRateModel {

    public Input<Parameter> muParameter = new Input<Parameter>("clock.rate", "the clock rate (defaults to 1.0)");


    @Override
    public void initAndValidate(State state) throws Exception {

        if (muParameter.get() != null) {
            muParameter.get().setBounds(0.0, Double.POSITIVE_INFINITY);
        }
    }

    public double getRateForBranch(State state, Node node) {
        return (muParameter.get() != null) ? state.getParameter(muParameter).getValue() : 1.0;
    }
}