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
package Utilities;

import BlockBuilding.Utilities;
import DataStructures.AbstractBlock;
import DataStructures.BilateralBlock;
import DataStructures.UnilateralBlock;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author gap2
 */
public class ExportBlocks implements Constants {

    private final List<AbstractBlock> blocks;
    private final Directory[] directories;
    private IndexReader[] iReaders;

    public ExportBlocks(String[] paths) {
        this(getDirectories(paths));
    }

    public ExportBlocks(Directory[] dirs) {
        blocks = new ArrayList<>();
        directories = dirs;
    }

    public void closeIndices() {
        for (IndexReader iReader : iReaders) {
            Utilities.closeReader(iReader);
        }
    }

    public List<AbstractBlock> getBlocks() {
        //extract blocks from Lucene index
        iReaders = new IndexReader[directories.length];
        iReaders[0] = Utilities.openReader(directories[0]);
        if (directories.length == 1) { //Dirty ER
            parseIndex(iReaders[0]);
        } else if (directories.length == 2) { // Clean-Clean ER
            iReaders[1] = Utilities.openReader(directories[1]);
            Map<String, int[]> hashedBlocks = parseD1Index(iReaders[0], iReaders[1]);
            parseD2Index(iReaders[1], hashedBlocks);
        }

        return blocks;
    }

    public static Directory[] getDirectories(String[] indexPaths) {
        Directory[] indexDirectory = new Directory[indexPaths.length];
        for (int i = 0; i < indexPaths.length; i++) {
            try {
                indexDirectory[i] = FSDirectory.open(new File(indexPaths[i]));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return indexDirectory;
    }

    public int getNoOfEntities() {
        if (directories.length == 1) { //Dirty ER
            return iReaders[0].numDocs();
        }
        //Clean-Clean ER
        return iReaders[0].numDocs() + iReaders[1].numDocs();
    }

    protected Map<String, int[]> parseD1Index(IndexReader d1Index, IndexReader d2Index) {
        try {
            int[] documentIds = Utilities.getDocumentIds(d1Index);
            final Map<String, int[]> hashedBlocks = new HashMap<>();
            Fields fields = MultiFields.getFields(d1Index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator(null);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    // check whether it is a common term
                    int d2DocFrequency = d2Index.docFreq(new Term(field, text));
                    if (d2DocFrequency == 0) {
                        continue;
                    }

                    final List<Integer> entityIds = new ArrayList<>();
                    DocsEnum de = MultiFields.getTermDocsEnum(d1Index, MultiFields.getLiveDocs(d1Index), field, text);
                    int doc;
                    while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                        entityIds.add(documentIds[doc]);
                    }

                    int[] idsArray = Converter.convertCollectionToArray(entityIds);
                    hashedBlocks.put(text.utf8ToString(), idsArray);
                }
            }
            return hashedBlocks;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    protected void parseD2Index(IndexReader d2Index, Map<String, int[]> hashedBlocks) {
        try {
            int[] documentIds = Utilities.getDocumentIds(d2Index);
            Fields fields = MultiFields.getFields(d2Index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator(null);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    if (!hashedBlocks.containsKey(text.utf8ToString())) {
                        continue;
                    }

                    final List<Integer> entityIds = new ArrayList<>();
                    DocsEnum de = MultiFields.getTermDocsEnum(d2Index, MultiFields.getLiveDocs(d2Index), field, text);
                    int doc;
                    while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                        entityIds.add(documentIds[doc]);
                    }

                    int[] idsArray = Converter.convertCollectionToArray(entityIds);
                    int[] d1Entities = hashedBlocks.get(text.utf8ToString());
                    blocks.add(new BilateralBlock(d1Entities, idsArray));
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    protected void parseIndex(IndexReader d1Index) {
        try {
            int[] documentIds = Utilities.getDocumentIds(d1Index);
            Fields fields = MultiFields.getFields(d1Index);
            for (String field : fields) {
                Terms terms = fields.terms(field);
                TermsEnum termsEnum = terms.iterator(null);
                BytesRef text;
                while ((text = termsEnum.next()) != null) {
                    if (termsEnum.docFreq() < 2) {
                        continue;
                    }

                    final List<Integer> entityIds = new ArrayList<>();
                    DocsEnum de = MultiFields.getTermDocsEnum(d1Index, MultiFields.getLiveDocs(d1Index), field, text);
                    int doc;
                    while ((doc = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
                        entityIds.add(documentIds[doc]);
                    }

                    int[] idsArray = Converter.convertCollectionToArray(entityIds);
                    UnilateralBlock block = new UnilateralBlock(idsArray);
                    blocks.add(block);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void storeBlocks(String outputPath) {
        System.out.println("\n\nStoring blocks...");
        SerializationUtilities.storeSerializedObject(blocks, outputPath);
        System.out.println("Blocks were stored!");
    }

    public static void main(String[] args) throws IOException {
        String mainDirectory = "/opt/data/frameworkData/";
        String blocksPath = mainDirectory + "blocks/movies/tokenUnigramsBlocking";
        String[] indexDirs = {mainDirectory + "indices/movies/tokenUnigramsDBP", mainDirectory + "indices/movies/tokenUnigramsIMDB"};
        ExportBlocks expbl = new ExportBlocks(indexDirs);
        expbl.storeBlocks(blocksPath);
    }
}
