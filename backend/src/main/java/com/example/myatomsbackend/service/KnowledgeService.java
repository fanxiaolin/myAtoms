package com.example.myatomsbackend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class KnowledgeService {
    public record Knowledge(String id, String title, String filename, String text, long createdAt) {}
    public record Hit(String knowledgeId, String title, int chunkIndex, double score, String text) {}
    private final Map<String, Knowledge> items = new ConcurrentHashMap<>();

    public Knowledge upload(MultipartFile file, String title) throws IOException {
        if (file.isEmpty() || file.getSize() > 8 * 1024 * 1024) throw new IllegalArgumentException("文件须在 1B-8MB 之间");
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("knowledge.txt");
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        Knowledge item = new Knowledge(UUID.randomUUID().toString(), title == null || title.isBlank() ? filename : title,
                filename, text.substring(0, Math.min(text.length(), 120_000)), System.currentTimeMillis());
        items.put(item.id(), item); return item;
    }
    public List<Knowledge> list() { return items.values().stream().sorted(Comparator.comparingLong(Knowledge::createdAt).reversed()).toList(); }
    public void remove(String id) { items.remove(id); }
    public List<Hit> search(String query, Collection<String> ids, int limit) {
        Set<String> terms = tokens(query); List<Hit> hits = new ArrayList<>();
        for (Knowledge item : items.values()) {
            if (ids != null && !ids.isEmpty() && !ids.contains(item.id())) continue;
            List<String> chunks = chunks(item.text());
            for (int i = 0; i < chunks.size(); i++) {
                Set<String> chunkTerms = tokens(chunks.get(i));
                long matches = terms.stream().filter(chunkTerms::contains).count();
                if (matches > 0) hits.add(new Hit(item.id(), item.title(), i, (double) matches / Math.max(1, terms.size()), chunks.get(i)));
            }
        }
        return hits.stream().sorted(Comparator.comparingDouble(Hit::score).reversed()).limit(Math.max(1, Math.min(limit, 20))).toList();
    }
    public String context(String query, Collection<String> ids) {
        StringBuilder out = new StringBuilder();
        for (Hit hit : search(query, ids, 6)) out.append("\n\n## 知识库：").append(hit.title()).append("（片段 ").append(hit.chunkIndex()+1).append("）\n").append(hit.text());
        return out.toString();
    }
    private Set<String> tokens(String value) {
        Set<String> result = new LinkedHashSet<>(); String normalized = value == null ? "" : value.toLowerCase();
        for (String word : normalized.split("[^a-z0-9_\\u4e00-\\u9fff]+")) if (word.length() > 1) result.add(word);
        String chinese = normalized.replaceAll("[^\\u4e00-\\u9fff]", "");
        for (int i=0; i+1<chinese.length(); i++) result.add(chinese.substring(i, i+2));
        return result;
    }
    private List<String> chunks(String text) {
        List<String> result = new ArrayList<>();
        for (int start=0; start<text.length(); start+=760) result.add(text.substring(start, Math.min(start+900, text.length())));
        return result;
    }
}
