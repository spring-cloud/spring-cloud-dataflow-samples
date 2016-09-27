package io.mross.mycoolprocessor;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.Transformer;

/**
 * Created by mross on 9/26/16.
 */


@EnableBinding(Processor.class)
public class MyCoolProcessorConfiguration {

    @Transformer(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    public int convertToCelsius(String payload) {
        int farenheitTemperature = Integer.parseInt(payload);
        return (farenheitTemperature-30)/2;
    }


}
