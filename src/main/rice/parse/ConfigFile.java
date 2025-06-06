package main.rice.parse;

import main.rice.node.APyNode;
import java.util.List;

/**
 * Encapsulates all configuration parameters parsed from a JSON config file:
 *  - the name of the function under test,
 *  - the list of parameter‐generation nodes (APyNode trees),
 *  - and the number of random test cases to generate.
 */
public class ConfigFile {

    /**
     * The name of the function under test
     */
    private String funcName;
    /**
     * A List of PyNodes that will be used to generate TestCases for the function under test.
     */
    private List<APyNode<?>> nodes;
    /**
     * The number of random test cases to be generated.
     */
    private int numRand;

    /**
     *
     * @param funcName A string with the name of the function under test
     * @param nodes List of PyNodes that will be used to generate TestCases for the function under test.
     * @param numRand an int with the number of random test cases to be generated.
     */
    public ConfigFile(String funcName, List<APyNode<?>> nodes, int numRand) {
        this.funcName = funcName;
        this.nodes = nodes;
        this.numRand = numRand;
    }

    /**
     * A method to get the func name
     * @return A string of the func name
     */
    public String getFuncName() {
        return this.funcName;
    }

    /**
     * A getter for the nodes
     * @return A list of APyNodes representing the nodes
     */
    public List<APyNode<?>> getNodes() {
        return this.nodes;
    }

    /**
     * A getter for the random number
     * @return An integer representing the random number
     */
    public int getNumRand() {
        return this.numRand;
    }
}
