package ed.inf.adbs.minibase;

import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import ed.inf.adbs.minibase.base.*;
import ed.inf.adbs.minibase.parser.QueryParser;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Minimization of conjunctive queries
 *
 */
public class CQMinimizer {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: CQMinimizer input_file output_file");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        minimizeCQ(inputFile, outputFile);

//        parsingExample(inputFile);
    }

    /**
     * CQ minimization procedure
     *
     * Assume the body of the query from inputFile has no comparison atoms
     * but could potentially have constants in its relational atoms.
     *
     */
    public static void minimizeCQ(String inputFile, String outputFile) {
        try {
            Query query = QueryParser.parse(Paths.get(inputFile));
            Head head = query.getHead();
            List<Atom> body = query.getBody();
            HashMap<String,Integer> opVarMap=query.getOutputVar();
            HashMap<String,Integer> atomVarMap= query.getAtomVar();

            //遍历删除一个
            for(int i=0; i< body.size(); i++){
                System.out.println("size"+body.size());
                Atom atom=body.get(i);
                if(atom instanceof RelationalAtom) {
                    HashMap<String, Integer> temMap=new HashMap<>();
                    temMap.putAll(opVarMap);
                    RelationalAtom reAtom= (RelationalAtom) atom;
                    List<Term> termList= reAtom.getTerms();
                    int count=0;
                    for(Term term: termList){
                        if(term instanceof Variable) {
                            Variable var = (Variable) term;
                            String varName=var.getName();
                            if(opVarMap.containsKey(varName)){
                                if(opVarMap.get(varName)==1)
                                    break;
                                else
                                    temMap.put(varName, temMap.get(varName)-1);
                            }
                        }
                        count++;
                    }
                    if(count==termList.size() && checkHomomorphism(reAtom, query, i)){
                        opVarMap=temMap;
                        body.remove(atom);
                        System.out.println("Xquery"+query);
                        i--;
                    }
                }
            }
            System.out.println("Fquery"+query);


        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    public static boolean checkHomomorphism(RelationalAtom target, Query query, int delNo) throws IOException, ClassNotFoundException {
        List<Term> termList= target.getTerms();
        StringBuilder termStr= new StringBuilder();

        Query temQuery=deepcocy(query);
        temQuery.getBody().remove(delNo);
        System.out.println("temQuery"+temQuery);

        HashMap<String, Integer> opVarMap= temQuery.getOutputVar();
        HashMap<String, Integer> varMap= temQuery.getAtomVar();

        for(Term term: termList){
            if(term instanceof Variable){
                Variable var=(Variable) term;
                String varStr=var.getName();
                if(varMap.containsKey(varStr)&&varMap.get(varStr)==1)
                    var.setName("*");
            }
        }

        boolean sign= false;
        for(Atom atom: temQuery.getBody()){
            RelationalAtom reAtom=(RelationalAtom) atom;
            List<Term> atomTerm= reAtom.getTerms();
            if(termList.size()==atomTerm.size()){
                int count=0;
                for(int i=0; i<termList.size(); i++){
                    String tarStr=""+termList.get(i);
                    String atomStr=""+atomTerm.get(i);
                    System.out.println("symbol "+tarStr+" "+atomStr);
                    if(tarStr.equals("*") || tarStr.equals(atomStr))
                        count++;
                    else
                        break;
                }
                if(count==termList.size()){
                    sign=true;
                    break;
                }
            }
        }
        System.out.println(sign);
        return sign;

//        termStr.append(target.getName());
//        for(Term term: termList){
//            if(term instanceof Constant) {
//                termStr.append(term);
//                continue;
//            }
//            String var=((Variable) term).getName();
//            if(opVarMap.containsKey(var)||varMap.get(var)>1)
//                termStr.append(term);
//            else
//                termStr.append('*');
//        }
//        boolean sign= false;
//        for(Atom atom: temQuery.getBody()){
//            RelationalAtom reAtom=(RelationalAtom) atom;
//            List<Term> atomTerm= reAtom.getTerms();
//            StringBuilder reAtomStr= new StringBuilder();
//            reAtomStr.append(reAtom.getName());
//            for(Term term: atomTerm)
//                reAtomStr.append(term);
//            System.out.println("reAS "+reAtomStr.toString());
//            if(reAtomStr.length()== termStr.length()){
//                int count=0;
//                for(int i=0; i<reAtomStr.length(); i++){
//                    if(termStr.charAt(i)=='*'||termStr.charAt(i)==reAtomStr.charAt(i))
//                        count++;
//                    else
//                        break;
//                }
//                if(count==reAtomStr.length()){
//                    sign=true;
//                    break;
//                }
//            }
//        }
//        System.out.println(sign);
//        return sign;
    }

    /**
     * Example method for getting started with the parser.
     * Reads CQ from a file and prints it to screen, then extracts Head and Body
     * from the query and prints them to screen.
     */

    public static void parsingExample(String filename) {

        try {
            Query query = QueryParser.parse(Paths.get(filename));
//            Query query = QueryParser.parse("Q(x, y) :- R(x, z), S(y, z, w)");
            // Query query = QueryParser.parse("Q(x) :- R(x, 'z'), S(4, z, w)");

            System.out.println("Entire query: " + query);
            Head head = query.getHead();
            System.out.println("Head: " + head);
            List<Atom> body = query.getBody();
            System.out.println("Body: " + body);
        }
        catch (Exception e)
        {
            System.err.println("Exception occurred during parsing");
            e.printStackTrace();
        }
    }

    public static Query deepcocy(Query query) throws IOException, ClassNotFoundException{
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(bo);
        objectOutputStream.writeObject(query);//将user对象，以字节数组的形式写入到内层缓冲区中
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(bi);
        return (Query) objectInputStream.readObject();
    }
}


