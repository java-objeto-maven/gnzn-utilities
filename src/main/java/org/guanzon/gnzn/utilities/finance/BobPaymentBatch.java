package org.guanzon.gnzn.utilities.finance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ordered set of {@link BobPayment} detail lines that will become one
 * BOB Outward Payment upload file.
 *
 * <p>Unlike {@code CheckBatch} (PNB), there is no header/trailer record and
 * no batch-level field (such as a funding account or a running total) in the
 * "Outward Payment File Format - Regular BOB" sheet - every line in the file
 * is simply one {@link BobPayment} detail record, in the order added here.
 */
public final class BobPaymentBatch {

    private final List<BobPayment> payments = new ArrayList<BobPayment>();

    public BobPaymentBatch addPayment(BobPayment payment) {
        payments.add(payment);
        return this;
    }

    public List<BobPayment> getPayments() {
        return Collections.unmodifiableList(payments);
    }
}
