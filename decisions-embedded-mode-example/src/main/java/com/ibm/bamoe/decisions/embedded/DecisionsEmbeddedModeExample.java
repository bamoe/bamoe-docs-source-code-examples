package com.ibm.bamoe.decisions.embedded;

import java.util.Arrays;

import org.drools.commands.SetActiveAgendaGroup;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.kie.api.KieServices;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieRuntimeFactory;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.internal.command.CommandFactory;

public class DecisionsEmbeddedModeExample {

    public static void main(String[] args) {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();

        System.out.println("-----> Now we execute rules in the stateful session <-----");

        KieSession kieSession = kieContainer.newKieSession();
        kieSession.addEventListener(new DebugRuleRuntimeEventListener());

        ExecutionResults executionResults = kieSession.execute(
            CommandFactory.newBatchExecution(Arrays.asList(
                CommandFactory.newInsert(new Applicant("#0001", 20), "applicant"),
                CommandFactory.newInsert(new LoanApplication("#0001"), "application"),
                new SetActiveAgendaGroup("applicationGroup"),
                CommandFactory.newFireAllRules()
            ))
        );

        System.out.println(executionResults.getResults().get("application"));

        kieSession.dispose();

        System.out.println("-----> Now we execute rules in the stateless session <-----");

        StatelessKieSession statelessKieSession = kieContainer.newStatelessKieSession();
        statelessKieSession.addEventListener(new DebugRuleRuntimeEventListener());

        ExecutionResults statelessExecutionResults = statelessKieSession.execute(
            CommandFactory.newBatchExecution(Arrays.asList(
                CommandFactory.newInsert(new Applicant("#0001", 20), "applicant"),
                CommandFactory.newInsert(new LoanApplication("#0001"), "application"),
                new SetActiveAgendaGroup("applicationGroup")            ))
        );

        System.out.println(statelessExecutionResults.getResults().get("application"));

        System.out.println("-----> Now we execute DMN <-----");
        DMNRuntime dmnRuntime = KieRuntimeFactory.of(kieContainer.getKieBase()).get(DMNRuntime.class);

        String namespace = "https://kie.org/dmn/_C83DFD16-A42A-46BE-A843-370444580E0F";
        String modelName = "loan-application-age-limit";

        DMNModel dmnModel = dmnRuntime.getModel(namespace, modelName);

        DMNContext dmnContext = dmnRuntime.newContext();  
        dmnContext.set("Applicant", new Applicant("#0001", 20));  
        dmnContext.set("Application", new LoanApplication("#0001"));  
        DMNResult dmnResult = dmnRuntime.evaluateAll(dmnModel, dmnContext);  

        for (DMNDecisionResult dr : dmnResult.getDecisionResults()) {  
            System.out.println(
                "Decision: '" + dr.getDecisionName() + "', " +
                "Result: " + dr.getResult());        
         }
    }
}
