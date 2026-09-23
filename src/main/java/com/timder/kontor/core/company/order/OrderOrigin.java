package com.timder.kontor.core.company.order;

import com.timder.kontor.core.company.request.RequestOrigin;

public sealed interface OrderOrigin permits RequestOrigin {

    /**
     * Whether the order counts against the legal forms order book limit
     * @return True, if the order counts
     */
    boolean occupiesOrderBookSlot();

    double reputationSurchargeFactor();
}
