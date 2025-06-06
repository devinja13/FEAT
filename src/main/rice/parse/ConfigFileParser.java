package main.rice.parse;
import main.rice.node.*;
import main.rice.obj.APyObj;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses a JSON configuration file into a ConfigFile object,
 * including type trees and their exhaustive/random domains.
 */
public class ConfigFileParser {


    /**
     * Reads and returns the contents of the file located at the input filepath;
     * @param fileName Path to the file to be read
     * @return A String with the contents of the file
     * @throws IOException  if the file does not exist or cannot be read
     */
    public static String readFile(String fileName) throws IOException {

        //starts a new reader
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line;
        //reading each line
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    /**
     * arses the input string, which should be the contents of a JSON file formatted
     * according to the config file specifications.
     * This should build an APyNode tree for each parameter,
     * where each node's type, exhaustive domain, and random domain should be set
     * up to reflect the contents of the config file.
     * @param contents A String representing a JSON object
     * @return A ConfigFile with the func name, a list of APyNodes, and an random integer
     * @throws InvalidConfigException if the JSON object has a content issue
     */
    public static ConfigFile parse(String contents) throws InvalidConfigException {

        //convert to JSON file
        JSONObject json;
        try {
            json = new JSONObject(contents);
        } catch (JSONException e) {
            throw new InvalidConfigException("Not a JSON File");
        }

        //intitializing the variables to hold each the JSONArray for each key
        String fname;
        JSONArray type;
        JSONArray exDom;
        JSONArray ranDom;
        int numRandom;
        List<APyNode<?>> configNodes = new ArrayList<>();

        //try catch block for accessing fname
        try {
            fname = (String) json.get("fname");
        } catch (JSONException e) {
            throw new InvalidConfigException("fname is missing");
        } catch (ClassCastException e) {
            throw new InvalidConfigException("fname is not a string");
        }

        //uses a helper function to get the json array
        type = getJSONArray(json, "types");
        exDom = getJSONArray(json, "exhaustive domain");
        ranDom = getJSONArray(json, "random domain");

        //checks to make sure the type, exDom, and ranDom are all the same length
        if (exDom.length() == type.length() && ranDom.length() == exDom.length()) {

            for (int i = 0; i < exDom.length(); i++) {

                String stringTypeName = type.getString(i);
                String stringExDom = exDom.getString(i);
                String stringRanDom = ranDom.getString(i);
                //parsing each element together
                configNodes.add(nodeHelper(stringTypeName, stringExDom, stringRanDom));
            }
        }

        //getting the num random
        try{
            numRandom = (int) json.get("num random");
        } catch (JSONException e) {
            throw new InvalidConfigException("There does not exist a random number");
        } catch (ClassCastException e) {
            throw new InvalidConfigException("Random number is not an integer");
        }

        return new ConfigFile(fname, configNodes, numRandom);

    }

    /**
     * A helper function to create a single APyNode tree
     * @param type A String representing the type of the tree
     * @param exDomain A String representing the exhaustive domain of the tree
     * @param randDom A string representing the random domain of the tree
     * @return An APyNode representing the parameters
     * @throws InvalidConfigException if there is an syntacical or semantic mistake in any of the parameters
     */
    private static APyNode<? extends APyObj<?>> nodeHelper(String type, String exDomain, String randDom) throws InvalidConfigException {

        //declaring and intializing the variables I use to split the type, exDomain, and randDom
        String firstType;
        String secType = "";
        String thirdType = "";

        String firstDom;
        String secDom = "";
        String thirdDom = "";

        String randFirstDom;
        String randSecDom = "";
        String randThirdDom = "";
        type = type.strip();
        exDomain = exDomain.strip();
        randDom = randDom.strip();
        //checking for invalid starting or ending parenthesis or colons
        if (type.startsWith("(") || exDomain.startsWith("(") || randDom.startsWith("(")) {
            throw new InvalidConfigException("Invalid starting parenthesis");
        } else if (type.endsWith("(") || exDomain.endsWith("(") || randDom.endsWith("(")) {
            throw new InvalidConfigException("Invalid ending parenthesis");
        } else if (type.startsWith(":") || exDomain.startsWith(":") || randDom.startsWith(":")) {
             throw new InvalidConfigException("Invalid starting colon");
        } else if (type.endsWith(":") || exDomain.endsWith(":") || randDom.endsWith(":")) {
            throw new InvalidConfigException("Invalid ending colon");
        }

        //finding the indexes for the first parenthesis and colon if any in type, exDomain, randDom

        int parIdx = type.indexOf("(");
        int colIdx = type.indexOf(":");
        int randomParIdx = randDom.indexOf("(");
        int randomColIdx = randDom.indexOf(":");
        int domParIdx = exDomain.indexOf("(");
        int domColIdx = exDomain.indexOf(":");
        //no splits when there are no parenthsis or colons
        if (parIdx == -1 && randomParIdx == -1 && domParIdx == -1 && domColIdx == -1 && colIdx == -1 && randomColIdx == -1) {
            firstType = type.strip();
            randFirstDom = randDom.strip();
            firstDom = exDomain.strip();

        } else if (parIdx != -1 && randomParIdx == -1 && domParIdx == -1) {
            //the string case only split the type
            firstType = type.substring(0, parIdx).strip();
            secType = type.substring(parIdx + 1).strip();
            randFirstDom = randDom.strip();
            firstDom = exDomain.strip();
        } else if (domColIdx == -1 && colIdx == -1 && randomColIdx == -1 && domParIdx != -1 && parIdx != -1 && randomParIdx != -1){
            //the list, tuple, set case need to split each into two
            firstType = type.substring(0, parIdx).strip();
            secType = type.substring(parIdx + 1).strip();
            firstDom = exDomain.substring(0, domParIdx).strip();
            secDom = exDomain.substring(domParIdx + 1).strip();
            randFirstDom = randDom.substring(0, randomParIdx).strip();
            randSecDom = randDom.substring(randomParIdx + 1).strip();
        } else if ((domColIdx != -1 && colIdx != -1 && randomColIdx != -1 &&  parIdx != -1 && randomParIdx != -1 && domParIdx != -1 )){
                //the dict case need to split each into three
                firstType = type.substring(0, parIdx).strip();
                secType = type.substring(parIdx + 1, colIdx).strip();
                thirdType = type.substring(colIdx + 1).strip();
                firstDom = exDomain.substring(0, domParIdx).strip();
                secDom = exDomain.substring(domParIdx + 1, domColIdx).strip();
                thirdDom = exDomain.substring(domColIdx + 1).strip();
                randFirstDom = randDom.substring(0, randomParIdx).strip();
                randSecDom = randDom.substring(randomParIdx + 1, randomColIdx).strip();
                randThirdDom = randDom.substring(randomColIdx + 1).strip();

        } else {
            //If none of the above cases are met then there must be an error in one of the strings
            throw new InvalidConfigException("Invalid trailing parenthesis or colon");
        }

        if (firstType.equals("int")) {
            //int case
            PyIntNode node = new PyIntNode();
            //setting domains
            node.setExDomain(intDomainHelper(firstDom));
            node.setRanDomain(intDomainHelper(randFirstDom));

            return node;

        } else if (firstType.equals("float")) {
            //float case
            PyFloatNode node = new PyFloatNode();
            //setting domains
            node.setExDomain(floatDomainHelper(firstDom));
            node.setRanDomain(floatDomainHelper(randFirstDom));

            return node;
        } else if (firstType.equals("bool")) {

            //boolean case
            PyBoolNode node = new PyBoolNode();
            //setting the domains
            node.setExDomain(boolDomainHelper(firstDom));
            node.setRanDomain(boolDomainHelper(randFirstDom));

            return node;
        } else if (firstType.equals("str")) {
            //String case, adding the chars to construct the string
            Set<Character> chars = new HashSet<>();
            for (char c : secType.toCharArray()) {
                chars.add(c);
            }
            PyStringNode node = new PyStringNode(chars);
            //setting the domains
            node.setExDomain(nonNegIntDomainHelper(firstDom));
            node.setRanDomain(nonNegIntDomainHelper(randFirstDom));

            return node;

        } else if (firstType.equals("list")) {

            //recurse to create the left child
            APyNode leftChild = nodeHelper(secType, secDom, randSecDom);

            //list case with the leftChild
            PyListNode<APyObj<?>> node = new PyListNode<>(leftChild);

            //setting the domains
            node.setExDomain(nonNegIntDomainHelper(firstDom));
            node.setRanDomain(nonNegIntDomainHelper(randFirstDom));
            return node;
        } else if (firstType.equals("tuple")) {
            //recurse to create the left child
            APyNode leftChild = nodeHelper(secType, secDom, randSecDom);
            //tuple case with the leftChild
            PyTupleNode<APyObj<?>> node = new PyTupleNode<>(leftChild);
            //setting the domains
            node.setExDomain(nonNegIntDomainHelper(firstDom));
            node.setRanDomain(nonNegIntDomainHelper(randFirstDom));

            return node;
        } else if (firstType.equals("set")) {
            //recurse to create the left child
            APyNode leftChild = nodeHelper(secType, secDom, randSecDom);
            //tuple case with the leftChild
            PySetNode<APyObj<?>> node = new PySetNode<>(leftChild);
            //setting the domains
            node.setExDomain(nonNegIntDomainHelper(firstDom));
            node.setRanDomain(nonNegIntDomainHelper(randFirstDom));

            return node;
        } else if (firstType.equals("dict")) {
            //dict case, recurse to create the left and right children
            APyNode leftChild = nodeHelper(secType, secDom, randSecDom);
            APyNode rightChild = nodeHelper(thirdType, thirdDom, randThirdDom);

            //creating the dict
            PyDictNode<APyObj<?>, APyObj<?>> node = new PyDictNode<>(leftChild, rightChild);
            //setting the domains
            node.setExDomain(nonNegIntDomainHelper(firstDom));
            node.setRanDomain(nonNegIntDomainHelper(randFirstDom));

            return node;
        } else {
            //case where the type is not a valid python type
            throw new InvalidConfigException("This is not one of the valid type forms");
        }
    }

    /**
     * A helper function to turn a string into a valid integer domain
     * @param firstDom a string to be parsed into a domain
     * @return A List of integers representing the domain
     * @throws InvalidConfigException if the string is not in BNF form or is not semantically correct
     */

    private static List<Integer> intDomainHelper(String firstDom) throws InvalidConfigException {

        int tildaIdx = firstDom.indexOf("~");
        //case where the domain is an array
        if (tildaIdx == -1) {
            //ensure it starts with brackets
            if (firstDom.startsWith("[") && firstDom.endsWith("]")) {
                firstDom = firstDom.substring(1, firstDom.length() - 1).strip();
                //split each element
                List<String> exDomItemsCheck = List.of(firstDom.split(","));
                List<Integer> exDomItems = new ArrayList<>();
                //iterates through each element and adds to the domain
                for (String domItem : exDomItemsCheck) {
                    domItem = domItem.strip();
                    try {
                        int val = Integer.parseInt(domItem);
                        exDomItems.add(val);
                    } catch (NumberFormatException e) {
                        throw new InvalidConfigException("Invalid Domain, ensure it is an integer");
                    }
                }
                return exDomItems;
            } else {
                //case where  is not a valid array
                throw new InvalidConfigException("Invalid Domain, ensure it is an array");
            }
        } else {
            //tilda case exception if there is a space
            if ((firstDom.contains(" "))){
                throw new InvalidConfigException("Invalid Domain, there should not be a space");
            }
            int firstNum = Integer.parseInt(firstDom.substring(0, tildaIdx).strip());
            int secondNum = Integer.parseInt(firstDom.substring(tildaIdx + 1).strip());
            //ensuring the first number is smaller than the second
            return getIntegers(firstNum, secondNum);

        }
    }
    /**
     * A helper function to turn a string into a valid bool domain
     * @param firstDom a string to be parsed into a domain
     * @return A List of integers representing the domain
     * @throws InvalidConfigException if the string is not in BNF form or is not semantically correct
     */
    private static List<Integer> boolDomainHelper(String firstDom) throws InvalidConfigException {
        int tildaIdx = firstDom.indexOf("~");

        if (tildaIdx == -1) {
            //expect an explicit list [0,1] form
            if (firstDom.startsWith("[") && firstDom.endsWith("]")) {
                firstDom = firstDom.substring(1, firstDom.length() - 1).strip();
                List<String> exDomItemsCheck = List.of(firstDom.split(","));
                List<Integer> exDomItems = new ArrayList<>();
                //iterates through each item in the list
                for (String domItem : exDomItemsCheck) {
                    domItem = domItem.strip();
                    try {
                        int val = Integer.parseInt(domItem);
                        if (val == 0 || val == 1) {
                            exDomItems.add(val);
                        } else {
                            throw new InvalidConfigException("Invalid Domain, ensure it is a bool");
                        }
                    } catch (NumberFormatException e) {
                        throw new InvalidConfigException("Invalid Domain, ensure it is a bool");
                    }
                }
                return exDomItems;
            } else {
                throw new InvalidConfigException("Invalid Domain, ensure it is an array");
            }
        } else {
            //tilda case
            if (firstDom.contains(" ")) {
                throw new InvalidConfigException("Invalid Domain, there should not be a space");
            }
            int firstNum = Integer.parseInt(firstDom.substring(0, tildaIdx).strip());
            //ensuring the numbers can be a bool
            if (firstNum != 0 && firstNum != 1) {
                throw new InvalidConfigException("Invalid Domain, ensure it is a bool");
            }
            int secondNum = Integer.parseInt(firstDom.substring(tildaIdx + 1).strip());
            if (secondNum != 0 && secondNum != 1) {
                throw new InvalidConfigException("Invalid Domain, ensure it is a bool");
            }
            //enusre the first number is less than the second
            return getIntegers(firstNum, secondNum);
        }
    }

    /**
     * A helper function to turn a string into a valid non negative integer domain
     * @param firstDom a string to be parsed into a domain
     * @return A List of non-negative integers representing the domain
     * @throws InvalidConfigException if the string is not in BNF form or is not semantically correct
     */
    private static List<Integer> nonNegIntDomainHelper(String firstDom) throws InvalidConfigException {

        int tildaIdx = firstDom.indexOf("~");
        if (tildaIdx == -1) {
            //making sure it is a valid array
            if (firstDom.startsWith("[") && firstDom.endsWith("]")) {
                firstDom = firstDom.substring(1, firstDom.length() - 1).strip();
                List<String> exDomItemsCheck = List.of(firstDom.split(","));
                List<Integer> exDomItems = new ArrayList<>();
                //iterating through the items
                for (String domItem : exDomItemsCheck) {
                    domItem = domItem.strip();
                    try {
                        int val = Integer.parseInt(domItem);
                        exDomItems.add(val);
                    } catch (NumberFormatException e) {
                        throw new InvalidConfigException("Invalid Domain, ensure it is an integer");
                    }
                }
                return exDomItems;
            } else {
                throw new InvalidConfigException("Invalid Domain, ensure it is an array");
            }
        } else {
            if ((firstDom.contains(" "))){
                throw new InvalidConfigException("Invalid Domain, there should not be a space");
            }

            int firstNum = Integer.parseInt(firstDom.substring(0, tildaIdx).strip());
            int secondNum = Integer.parseInt(firstDom.substring(tildaIdx + 1).strip());
            //making sure all ints are non-negative
            if (firstNum <= secondNum && firstNum >= 0) {
                List<Integer> exDomItems = new ArrayList<>();
                for (int i = firstNum; i <= secondNum; i++) {
                    exDomItems.add(i);
                }
                return exDomItems;
            } else {
                throw new InvalidConfigException("Invalid Domain, ensure the second number is bigger than the first");
            }
        }
    }

    /**
     * A helper function to turn a string into a valid float domain
     * @param firstDom a string to be parsed into a domain
     * @return A List of floats representing the domain
     * @throws InvalidConfigException if the string is not in BNF form or is not semantically correct
     */
    private static List<Double> floatDomainHelper(String firstDom) throws InvalidConfigException {

        int tildaIdx = firstDom.indexOf("~");
        if (tildaIdx == -1) {
            //making sure it is a valid array
            if (firstDom.startsWith("[") && firstDom.endsWith("]")) {
                firstDom = firstDom.substring(1, firstDom.length() - 1).strip();
                List<String> exDomItemsCheck = List.of(firstDom.split(","));
                List<Double> exDomItems = new ArrayList<>();
                //adding each item to the array
                for (String domItem : exDomItemsCheck) {
                    domItem = domItem.strip();
                    try {
                        double val = Double.parseDouble(domItem);
                        exDomItems.add(val);
                    } catch (NumberFormatException e) {
                        throw new InvalidConfigException("Invalid Domain, ensure it is an float");
                    }
                }
                return exDomItems;
            } else {
                throw new InvalidConfigException("Invalid Domain, ensure it is an array");
            }
        } else {
            if ((firstDom.contains(" "))){
                throw new InvalidConfigException("Invalid Domain, there should not be a space");
            }
            int firstNum = Integer.parseInt(firstDom.substring(0, tildaIdx).strip());
            int secondNum = Integer.parseInt(firstDom.substring(tildaIdx + 1).strip());
            //ensuring the first number is less than the second
            if (firstNum <= secondNum) {
                List<Double> exDomItems = new ArrayList<>();
                for (double i = firstNum; i <= secondNum; i++) {
                    exDomItems.add(i);
                }
                return exDomItems;
            } else {
                throw new InvalidConfigException("Invalid Domain, ensure the second number is bigger than the first");
            }
        }
    }

    /**
     * A helper function returns a JSONArray when given a JSON object and key
     * @param json A JSON object
     * @param key A String representing the key for the JSON object
     * @return A JSONArray from the JSON object
     * @throws InvalidConfigException if the key or JSON objct doesn't exist
     */
    private static JSONArray getJSONArray(JSONObject json, String key) throws InvalidConfigException {
        try {
            return (JSONArray) json.get(key);
        } catch (JSONException e) {
            //missing key case
            throw new InvalidConfigException("Missing key: " + key);
        } catch (ClassCastException e) {
            //invalid array case
            throw new InvalidConfigException("Invalid array for key: " + key);
        }
    }

    /**
     * A helper function to create the list of integers
     * @param firstNum the first integer which is the lower bound
     * @param secondNum the second integer which is the upper bound
     * @return A List of Integers reprsenting the domain
     * @throws InvalidConfigException if the second number is bigger than the first
     */
    private static List<Integer> getIntegers(int firstNum, int secondNum) throws InvalidConfigException {
        //ensures the second number is bigger than the first
        if (firstNum <= secondNum) {
            List<Integer> exDomItems = new ArrayList<>();
            for (int i = firstNum; i <= secondNum; i++) {
                exDomItems.add(i);
            }
            return exDomItems;
        } else {
            throw new InvalidConfigException("Invalid Domain, ensure the second number is bigger than the first");
        }
    }
}