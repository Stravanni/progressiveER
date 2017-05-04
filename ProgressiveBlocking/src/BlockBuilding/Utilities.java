/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
 */
package BlockBuilding;

import DataStructures.AbstractBlock;
import Utilities.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;

/**
 * @author gap2
 */
public class Utilities implements Constants {

    public static void closeReader(IndexReader iReader) {
        try {
            iReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Set<String> getCombinationsFor(List<String> sublists, int sublistLength) {
        if (sublistLength == 0 || sublists.size() < sublistLength) {
            return new HashSet<>();
        }

        List<String> remainingElements = new ArrayList<>(sublists);
        String lastSublist = remainingElements.remove(sublists.size() - 1);

        final Set<String> combinationsExclusiveX = getCombinationsFor(remainingElements, sublistLength);
        final Set<String> combinationsInclusiveX = getCombinationsFor(remainingElements, sublistLength - 1);

        final Set<String> resultingCombinations = new HashSet<>();
        resultingCombinations.addAll(combinationsExclusiveX);
        if (combinationsInclusiveX.isEmpty()) {
            resultingCombinations.add(lastSublist);
        } else {
            combinationsInclusiveX.stream().forEach((combination) -> {
                resultingCombinations.add(combination + lastSublist);
            });
        }
        return resultingCombinations;
    }

    public static int[] getDocumentIds(IndexReader reader) {
        int[] documentIds = new int[reader.numDocs()];
        for (int i = 0; i < documentIds.length; i++) {
            try {
                Document document = reader.document(i);
                documentIds[i] = Integer.parseInt(document.get(DOC_ID));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return documentIds;
    }

    public static Set<String> getExtendedSuffixes(int minimumLength, String blockingKey) {
        final Set<String> suffixes = new HashSet<>();
        suffixes.add(blockingKey);
        if (minimumLength <= blockingKey.length()) {
            for (int nGramSize = blockingKey.length() - 1; minimumLength <= nGramSize; nGramSize--) {
                int currentPosition = 0;
                final int length = blockingKey.length() - (nGramSize - 1);
                while (currentPosition < length) {
                    String newSuffix = blockingKey.substring(currentPosition, currentPosition + nGramSize);
                    suffixes.add(newSuffix);
                    currentPosition++;
                }
            }
        }
        return suffixes;
    }

    public static double getJaccardSimilarity(int[] tokens1, int[] tokens2) {
        double commonTokens = 0.0;
        int noOfTokens1 = tokens1.length;
        int noOfTokens2 = tokens2.length;
        for (int i = 0; i < noOfTokens1; i++) {
            for (int j = 0; j < noOfTokens2; j++) {
                if (tokens2[j] < tokens1[i]) {
                    continue;
                }

                if (tokens1[i] < tokens2[j]) {
                    break;
                }

                if (tokens1[i] == tokens2[j]) {
                    commonTokens++;
                }
            }
        }
        return commonTokens / (noOfTokens1 + noOfTokens2 - commonTokens);
    }

    public static List<String> getNGrams(int n, String blockingKey) {
        final List<String> nGrams = new ArrayList<>();
        if (blockingKey.length() < n) {
            nGrams.add(blockingKey);
        } else {
            int currentPosition = 0;
            final int length = blockingKey.length() - (n - 1);
            while (currentPosition < length) {
                nGrams.add(blockingKey.substring(currentPosition, currentPosition + n));
                currentPosition++;
            }
        }
        return nGrams;
    }

    public static Set<String> getPrefixes(int minimumLength, String blockingKey) {
        final Set<String> prefixes = new HashSet<>();
        if (blockingKey.length() < minimumLength) {
            prefixes.add(blockingKey);
        } else {
            for (int i = minimumLength; i < blockingKey.length() + 1; i++) {
                prefixes.add(blockingKey.substring(0, i));
            }
        }
        return prefixes;
    }

    public static Set<String> getSuffixes(int minimumLength, String blockingKey) {
        final Set<String> suffixes = new HashSet<>();
        if (blockingKey.length() < minimumLength) {
            suffixes.add(blockingKey);
        } else {
            int limit = blockingKey.length() - minimumLength + 1;
            for (int i = 0; i < limit; i++) {
                suffixes.add(blockingKey.substring(i));
            }
        }
        return suffixes;
    }

    public static Set<String> getPrefixes(int minimumLength, String blockingKey, boolean strict_minimum) {
        /*System.out.println("min lengh: " + minimumLength);*/
        final Set<String> prefixes = new HashSet<>();
        if (blockingKey.length() < minimumLength && !strict_minimum) {
            prefixes.add(blockingKey);
        } else {
            for (int i = minimumLength; i < blockingKey.length() + 1; i++) {
                prefixes.add(blockingKey.substring(0, i));
            }
        }
        return prefixes;
    }

    public static Set<String> getSuffixes(int minimumLength, String blockingKey, boolean strict_minimum) {
        final Set<String> suffixes = new HashSet<>();
        /*TODO check srict_minimum, before was with "!"*/
        if (blockingKey.length() < minimumLength && strict_minimum) {
            /*System.out.println("min suffix: " + blockingKey + " < " + minimumLength);*/
            suffixes.add(blockingKey);
        } else {
            int limit = blockingKey.length() - minimumLength + 1;
            for (int i = 0; i < limit; i++) {
                suffixes.add(blockingKey.substring(i));
            }
        }
        return suffixes;
    }

    public static IndexReader openReader(Directory directory) {
        try {
            return DirectoryReader.open(directory);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void purgeBlocksByAssignments(int maxAssignments, List<AbstractBlock> blocks) {
        Iterator<AbstractBlock> blocksIterator = blocks.iterator();
        while (blocksIterator.hasNext()) {
            AbstractBlock block = (AbstractBlock) blocksIterator.next();
            if (maxAssignments < block.getTotalBlockAssignments()) {
                blocksIterator.remove();
            }
        }
    }

    public static void main(String[] args) {
        String test = "test";
        Set<String> suffixes = Utilities.getSuffixes(1, test);
        for (String suffix : suffixes) {
            System.out.println(suffix);
        }

        System.out.println("\n\nPrefixes!!");
        Set<String> prefixes = Utilities.getPrefixes(1, test);
        for (String prefix : prefixes) {
            System.out.println(prefix);
        }
    }
}
