package co.com.bancolombia.jms.sample.noreactive.entrypoints;

import co.com.bancolombia.jms.sample.noreactive.domain.model.Result;
import co.com.bancolombia.jms.sample.noreactive.domain.usecase.SampleUseCase;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class Rest {
    private final SampleUseCase sampleUseCase;

    @GetMapping(value = "/api/mq", produces = "application/json")
    public Result route() {
        return sampleUseCase.sendAndListen();
    }
}
