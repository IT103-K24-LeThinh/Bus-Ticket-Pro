package com.re.busticket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ResponseSepayDto {
    private String gateway;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("subAccount")
    private String subAccount;

    private String code;
    private String content;

    @JsonProperty("transferType")
    private String transferType;

    private String description;

    @JsonProperty("transferAmount")
    private Long transferAmount;

    @JsonProperty("referenceCode")
    private String referenceCode;

    private Long accumulated;
    private Long id;
}
