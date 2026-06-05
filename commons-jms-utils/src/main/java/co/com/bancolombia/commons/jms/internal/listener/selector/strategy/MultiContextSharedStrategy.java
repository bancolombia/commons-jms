package co.com.bancolombia.commons.jms.internal.listener.selector.strategy;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@AllArgsConstructor
public class MultiContextSharedStrategy implements SelectorStrategy {
    private final List<ContextSharedStrategy> contexts;
    private final int concurrency;

    public MultiContextSharedStrategy(ConnectionFactory factory, int concurrency) {
        this.concurrency = concurrency;
        this.contexts = IntStream.range(0, concurrency)
                .mapToObj(idx -> new ContextSharedStrategy(factory.createContext()))
                .collect(Collectors.toList());
    }

    @Override
    public Message getMessageBySelector(String selector, long timeout, Destination destination) {
        return getRandom().getMessageBySelector(selector, timeout, destination);
    }

    protected ContextSharedStrategy getRandom() {
        int selectIndex = (int) (System.currentTimeMillis() % concurrency);
        return contexts.get(selectIndex);
    }
}
