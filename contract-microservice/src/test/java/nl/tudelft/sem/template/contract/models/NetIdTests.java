package nl.tudelft.sem.template.contract.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class NetIdTests {

    @Test
    public void netIdTest() {
        String string = "user123";
        NetId netId = new NetId(string);

        assertThat(netId.toString()).isEqualTo(string);
    }
}
