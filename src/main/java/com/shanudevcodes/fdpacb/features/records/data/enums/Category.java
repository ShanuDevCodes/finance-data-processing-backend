package com.shanudevcodes.fdpacb.features.records.data.enums;

import lombok.Getter;

@Getter
public enum Category {
    SALARY(RecordType.INCOME),
    BUSINESS(RecordType.INCOME),
    INVESTMENT(RecordType.INCOME),
    FREELANCE(RecordType.INCOME),
    OTHER_INCOME(RecordType.INCOME),
    FOOD(RecordType.EXPENSE),
    TRANSPORT(RecordType.EXPENSE),
    SHOPPING(RecordType.EXPENSE),
    BILLS(RecordType.EXPENSE),
    ENTERTAINMENT(RecordType.EXPENSE),
    HEALTH(RecordType.EXPENSE),
    EDUCATION(RecordType.EXPENSE),
    TRAVEL(RecordType.EXPENSE),
    RENT(RecordType.EXPENSE),
    OTHER_EXPENSE(RecordType.EXPENSE);
    private final RecordType type;
    Category(RecordType type) {
        this.type = type;
    }
}