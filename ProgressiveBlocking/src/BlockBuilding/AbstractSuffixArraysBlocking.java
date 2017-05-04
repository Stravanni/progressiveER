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
import DataStructures.EntityProfile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gap2
 */
public abstract class AbstractSuffixArraysBlocking extends AbstractTokenBlocking {

    protected final int maximumBlockSize;
    protected final int minimumSuffixLength;

    public AbstractSuffixArraysBlocking(int maxSize, int minLength, List<EntityProfile>[] profiles) {
        this(maxSize, minLength, "Memory-based Suffix Arrays Blocking", profiles);
    }

    public AbstractSuffixArraysBlocking(int maxSize, int minLength, String description, List<EntityProfile>[] profiles) {
        super(description, profiles);
        maximumBlockSize = maxSize;
        minimumSuffixLength = minLength;
        //System.out.println("min suffix length: " + minimumSuffixLength);
    }

    public AbstractSuffixArraysBlocking(int maxSize, int minLength, String[] entities, String[] index) {
        this(maxSize, minLength, "Disk-based Suffix Arrays Blocking", entities, index);
    }

    public AbstractSuffixArraysBlocking(int maxSize, int minLength, String description, String[] entities, String[] index) {
        super(description, entities, index);
        maximumBlockSize = maxSize;
        minimumSuffixLength = minLength;
    }

    @Override
    public List<AbstractBlock> buildBlocks() {
        blocks = super.buildBlocks();
        Utilities.purgeBlocksByAssignments(maximumBlockSize, blocks);
        return blocks;
    }

    @Override
    protected Set<String> getBlockingKeys(String attributeValue) {
        final Set<String> suffixes = new HashSet<>();
        for (String token : getTokens(attributeValue)) {
            suffixes.addAll(Utilities.getSuffixes(minimumSuffixLength, token));
        }
        return suffixes;
    }
}