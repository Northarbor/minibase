package ed.inf.adbs.minibase.base;

import ed.inf.adbs.minibase.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.io.Serializable;

public class Query implements Serializable{
    private Head head;

    private List<Atom> body;

    private HashMap<String,Integer> outputVar;

    private HashMap<String,Integer> atomVar;


    public Query(Head head, List<Atom> body) {
        this.head = head;
        this.body = body;
        extractVarMap();
    }

    public Head getHead() {
        return head;
    }

    public List<Atom> getBody() {
        return body;
    }

    public HashMap<String,Integer> getOutputVar() {
        return outputVar;
    }

    public HashMap<String, Integer> getAtomVar() {
        return atomVar;
    }

    private void extractVarMap(){
        this.outputVar= new HashMap<>();
        this.atomVar= new HashMap<>();
        List<Variable> outputVarList=this.head.getVariables();
        List<String> opNameList = new LinkedList<>();
        for(Variable opVar: outputVarList)
            opNameList.add(opVar.getName());
        for(Atom atom: this.body){
            if(atom instanceof RelationalAtom){
                RelationalAtom reAtom= (RelationalAtom) atom;
                List<Term> termList= reAtom.getTerms();
                for(Term term: termList){
                    if(term instanceof Variable) {
                        Variable var = (Variable) term;
                        String varName= var.getName();
                        if(opNameList.contains(varName)){
                            if(this.outputVar.containsKey(varName))
                                this.outputVar.put(varName, this.outputVar.get(varName)+1);
                            else
                                this.outputVar.put(varName, 1);
                        }
                        else{
                            if(this.atomVar.containsKey(varName))
                                this.atomVar.put(varName, this.atomVar.get(varName)+1);
                            else
                                this.atomVar.put(varName, 1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return head + " :- " + Utils.join(body, ", ");
    }
}
