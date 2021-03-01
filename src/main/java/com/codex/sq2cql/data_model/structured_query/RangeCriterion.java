package com.codex.sq2cql.data_model.structured_query;

import com.codex.sq2cql.data_model.common.TermCode;

public class RangeCriterion extends Criterion{
    public RangeCriterion(TermCode concept, double minValue, double maxValue) {
        super(concept);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    private final double minValue;
    private final double maxValue;
    private String unit;

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
