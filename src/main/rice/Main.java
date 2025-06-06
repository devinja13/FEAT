package main.rice;

import main.rice.basegen.BaseSetGenerator;
import main.rice.parse.ConfigFile;
import main.rice.parse.InvalidConfigException;
import main.rice.test.TestCase;
import main.rice.test.TestResults;
import main.rice.test.Tester;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static main.rice.concisegen.ConciseSetGenerator.setCover;
import static main.rice.parse.ConfigFileParser.parse;
import static main.rice.parse.ConfigFileParser.readFile;

/**
 * The entry point for the test‐generation tool:
 *  1. Reads a ConfigFile from disk,
 *  2. Builds a base set of TestCases,
 *  3. Runs those tests against the reference and buggy implementations,
 *  4. And returns a minimal covering set of tests via setCover().
 */
public class Main {

    /**
     * Main method to generate the test cases
     * @param args a String containing the path to the config file, a String containing the path to the reference solution.
     * a String containing the path to the directory containing the buggy implementations.
     * @throws IOException if there is an invalid file path
     * @throws InvalidConfigException if there is an error parsing
     * @throws InterruptedException if there is an error
     */
    public static void main(String[] args)
            throws IOException, InvalidConfigException, InterruptedException {
        Set<TestCase> testCases = generateTests(args);
    }

    /**
     * A helper function to generate the test cases set
     * @param args a String containing the path to the config file, a String containing the path to the reference solution.
     * a String containing the path to the directory containing the buggy implementations.
     * @throws IOException if there is an invalid file path
     * @throws InvalidConfigException if there is an error parsing
     * @throws InterruptedException if there is an error
     */
    public static Set<TestCase> generateTests(String[] args)
            throws IOException, InvalidConfigException, InterruptedException {
        // reading the file
        ConfigFile configFile = parse(readFile(args[0]));

        // making the base set
        BaseSetGenerator baseSet = new BaseSetGenerator(configFile.getNodes(), configFile.getNumRand());

        List<TestCase> testCases = baseSet.genBaseSet();
        Tester tester = new Tester(configFile.getFuncName(), args[1], args[2], testCases);
        List<String> expectRes = tester.computeExpectedResults();
        TestResults res = tester.runTests();
        return setCover(res);
    }
}
