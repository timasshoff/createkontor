package com.timder.kontor.core.economy;

import com.timder.kontor.core.macro.MacroParams;
import com.timder.kontor.core.macro.ProgressParams;
import com.timder.kontor.core.raw.PriceProcessParams;
import com.timder.kontor.core.value.ProcessCosts;

/**
 * Every global setting the economy needs.
 * @param macro Parameters for the macroeconomic cycle
 * @param progress Parameters for the technical progress
 * @param priceProcess Parameters for the global movement of raw material prices
 * @param processCosts Costs for each processing type
 */
public record EconomyParams(
        MacroParams macro,
        ProgressParams progress,
        PriceProcessParams priceProcess,
        ProcessCosts processCosts
) {

    public EconomyParams {
        if (macro == null) throw new IllegalArgumentException("macro must not be null.");
        if (progress == null) throw new IllegalArgumentException("progress must not be null.");
        if (priceProcess == null) throw new IllegalArgumentException("priceProcess must not be null.");
        if (processCosts == null) throw new IllegalArgumentException("processCosts must not be null.");
    }

    /*
     * The following code is AI generated. Might be changed in the future.
     */

    public static EconomyParams standard() {
        return new EconomyParams(MacroParams.standard(), ProgressParams.standard(),
                PriceProcessParams.standard(), ProcessCosts.standard());
    }

}
