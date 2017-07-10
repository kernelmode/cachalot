package ru.cinimex.cachalot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import javax.jms.ConnectionFactory;

import org.springframework.jms.core.JmsTemplate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import ru.cinimex.cachalot.validation.GenericValidationRule;
import ru.cinimex.cachalot.validation.ValidationRule;

import static org.springframework.util.Assert.notNull;

@Getter(AccessLevel.PACKAGE)
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "WeakerAccess", "unused"})
public class JmsExpectation extends Womb {

    private final long id;
    private final String queue;
    private final JmsTemplate template;
    private final JmsCachalotWomb parent;
    private final Collection<ValidationRule<? super String>> rules = new ArrayList<>();

    @Setter(AccessLevel.PACKAGE)
    private String actual;
    private String expected;

    JmsExpectation(JmsCachalotWomb parent, ConnectionFactory factory, @NonNull String queue, long id) {
        this.id = id;
        this.queue = queue;
        this.parent = parent;
        this.template = new JmsTemplate(factory);
        revealWomb("Out queue set {}", queue);
    }

    /**
     * @param rule is {@link ValidationRule} instance. It could be {@link GenericValidationRule} or custom
     *             implementation of the rule. If provided, received message will be validated against this
     *             rule. If it returns false, test will be considered as failed.
     *             Note: this operation, like many others, is not idempotent. I.e. it changes the state of
     *             underlying infrastructure. You can add multiple rules for one message by calling
     *             {@link #addRule(ValidationRule)} multiple times.
     * @return self.
     */
    @SuppressWarnings("JavaDoc")
    public JmsExpectation addRule(ValidationRule<? super String> rule) {
        notNull(rule, "Given rule must not be null");
        rules.add(rule);
        revealWomb("Rule added {}", rule);
        return this;
    }

    /**
     * If provided, received messages will be compared with the body. If it won't be found, test will be
     * considered as failed.
     *
     * @param message to compare.
     * @return self.
     */
    public JmsExpectation withExpectedResponse(String message) {
        validateState("withExpectedResponse");
        notNull(message, "Output must be specified, if you call #withExpectedResponse");
        this.expected = message;
        revealWomb("Out message {}", message);
        return this;
    }

    public JmsCachalotWomb ingest() {
        return parent;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        JmsExpectation that = (JmsExpectation) other;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @SuppressWarnings("SameParameterValue")
    private void validateState(String callFrom) {
        notNull(template, "Illegal call #" + callFrom + " before CachalotWomb#usingJms");
    }

}
