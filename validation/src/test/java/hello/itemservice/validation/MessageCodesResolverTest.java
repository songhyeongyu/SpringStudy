package hello.itemservice.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageCodesResolverTest {

    MessageCodesResolver codesResolverTest = new DefaultMessageCodesResolver();

    @Test
    void messageCodesResolverObject() {
        String[] messageCode = codesResolverTest.resolveMessageCodes("required", "item");
        for (String message : messageCode) {
            System.out.println("message = " + message);
        }

        assertThat(messageCode).containsExactly("required.item", "required");
    }

    @Test
    void messageCodesResolverField() {
        String[] messageCodes = codesResolverTest.resolveMessageCodes("required",
                "item", "itemName", String.class);
        assertThat(messageCodes).containsExactly("required.item.itemName",
                "required.itemName",
                "required.java.lang.String",
                "required"
        ); }
}
