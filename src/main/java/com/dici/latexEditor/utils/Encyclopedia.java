package com.dici.latexEditor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Encyclopedia<T> {
	private Set<Entry> entries = new HashSet<>();
	
	public Encyclopedia(Path path, Function<String,T> converter) { 
		fromFile(path,converter);
	}
	
	private void fromFile(Path path, Function<String,T> converter) {
		try (BufferedReader br = Files.newBufferedReader(path)){
			String line;
			while ((line = br.readLine()) != null) {
				String[]    split    = line.split(",");
				String[]    kw       = split[1].trim().split("\\s+");
				Set<String> keywords = new HashSet<>(kw.length);
				
				for (String keyword : kw) keywords.add(keyword);
				entries.add(new Entry(converter.apply(split[0]),keywords));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Entry> search(String query) {
		return search(Arrays.stream(query.split("\\s+")).collect(Collectors.toSet()));
	}
	
	public List<Entry> search(Set<String> keywords) {
		Map<Entry,Integer> scores = computeScores(keywords,entries);
		return scores.entrySet().stream()
			.filter(e -> e.getValue() > 0)
			.sorted((e1,e2) -> Integer.compare(e1.getValue(),e2.getValue()))
			.map(e -> e.getKey())
			.collect(Collectors.toList());
	}
	
	public Map<Entry,Integer> computeScores(Set<String> keywords, Collection<Entry> entries) {
		Map<Entry,Integer> scores  = new HashMap<>();
		int[]              results = new int[entries.size()];
		
		for (String keyword : keywords) {
			int i = 0;
			for (Encyclopedia<T>.Entry entry : entries) 
				if (entry.keywords.contains(keyword))
					results[i++]++;
		}
		
		int i = 0;
		for (Encyclopedia<T>.Entry entry : entries)
			scores.put(entry,results[i++]);
		
		return scores;
	}
	
	public class Entry {
		public final T data;
		public final Set<String> keywords;
		
		private Entry(T data, Set<String> keywords) {
			this.data     = data;
			this.keywords = keywords;
		}
		
		@Override
		public String toString() {
			return String.format("[src=%s,kw=%s]","",keywords.stream().reduce((s1,s2) -> s1 + " " + s2).get());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((data == null) ? 0 : data.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("unchecked")
			Entry other = (Entry) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (data == null) {
				if (other.data != null)
					return false;
			} else if (!data.equals(other.data))
				return false;
			return true;
		}

		private Encyclopedia<T> getOuterType() {
			return Encyclopedia.this;
		}
	}
	
//	public static void main(String[] args) throws URISyntaxException {
//		Encyclopedia<String> latexpidia = new Encyclopedia<>(Paths.get(Encyclopedia.class.getResource("/data/encyclopedia.txt").toURI()),Function.identity());
//		System.out.println(latexpidia.entries);
//		System.out.println(latexpidia.search("fucki fuck"));
//	}
}