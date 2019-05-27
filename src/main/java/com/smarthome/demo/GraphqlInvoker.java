package com.smarthome.demo;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Internal;
import graphql.spring.web.servlet.GraphQLInvocation;
import graphql.spring.web.servlet.GraphQLInvocationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.concurrent.CompletableFuture;

@Component
@Internal
@Primary
public class GraphqlInvoker implements GraphQLInvocation {

    @Autowired
    GraphQL graphQL;

    @Override
    public CompletableFuture<ExecutionResult> invoke(GraphQLInvocationData invocationData, WebRequest webRequest) {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(invocationData.getQuery())
                .context(webRequest)
                .operationName(invocationData.getOperationName())
                .variables(invocationData.getVariables())
                .build();
        CompletableFuture<ExecutionInput> customizedExecutionInput = CompletableFuture.completedFuture(executionInput);
        return customizedExecutionInput.thenCompose(graphQL::executeAsync);
    }

}
