package org.apache.camel.test;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Aggregation Strategy that collects exchange bodies into a List.
 * Each incoming exchange's body is added to the list and returned as the aggregated result.
 */
public class MyAggregationStrategy implements AggregationStrategy {
    
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        // Get the body from the new exchange
        Object newBody = newExchange.getIn().getBody();
        
        // If this is the first exchange (oldExchange is null), initialize the list
        if (oldExchange == null) {
            List<Object> list = new ArrayList<>();
            list.add(newBody);
            newExchange.getIn().setBody(list);
            return newExchange;
        }
        
        // Get the existing list from the old exchange
        @SuppressWarnings("unchecked")
        List<Object> list = oldExchange.getIn().getBody(List.class);
        
        // Add the new body to the list
        list.add(newBody);
        
        // Set the updated list back to the exchange
        oldExchange.getIn().setBody(list);
        
        return oldExchange;
    }
}