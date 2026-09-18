package ru.leonaugust.link_aggregator.ai.common;

import lombok.Builder;

@Builder
public record GenerateRequest(String model, String prompt, boolean stream) {

}