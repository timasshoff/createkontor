package com.timder.kontor.core.company.request;

import com.timder.kontor.core.company.order.OrderOrigin;

/**
 * An order that came from an accepted request
 * @param requestNumber The number of the request this order comes from
 */
public record RequestOrigin(long requestNumber) implements OrderOrigin {
    @Override
    public boolean occupiesOrderBookSlot() {
        return true;
    }

    @Override
    public double reputationSurchargeFactor() {
        return 1.0;
    }
}
