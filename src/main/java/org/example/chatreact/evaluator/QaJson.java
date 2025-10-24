package org.example.chatreact.evaluator;

import java.util.List;

public record QaJson(String question, List<String> docs, String answer) {
}