/*
 * The MIT License
 *
 * Copyright (c) 2010 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package htsjdk.samtools.filter;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMUtils;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalUtil;
import htsjdk.samtools.util.OverlapDetector;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Filter SAMRecords so that only those that overlap the given list of intervals.
 * It is required that the SAMRecords are passed in coordinate order, and have non-null SAMFileHeaders.
 *
 *
 * @author kbergin@broadinstitute.org
 */
public class IntervalKeepPairFilter implements SamRecordFilter {
    private final SAMFileHeader samHeader;
    private OverlapDetector<Interval> intervalOverlapDetector;

    /**
     * Prepare to filter out SAMRecords that do not overlap the given list of intervals
     * @param intervals -- must be locus-ordered & non-overlapping
     */
    public IntervalKeepPairFilter(final List<Interval> intervals, final SAMFileHeader samHeader) {
        this.samHeader = samHeader;
        IntervalUtil.assertOrderedNonOverlapping(intervals.iterator(), samHeader.getSequenceDictionary());
        this.intervalOverlapDetector = new OverlapDetector<>(0,0);
        this.intervalOverlapDetector.addAll(intervals, intervals);
    }

    /**
     * Determines whether a SAMRecord matches this filter
     *
     * @param record the SAMRecord to evaluate
     * @return true if the SAMRecord matches the filter, otherwise false
     */
    public boolean filterOut(final SAMRecord record) {
        /*
        Take record, find its mate
        Check if either record overlaps the current interval using overlap detector
        If yes, return false, don't filter it out
         */

        //get read 1 interval and check for overlaps in any intervals in list
        Interval readInterval = new Interval(record.getReferenceName(), record.getStart(), record.getEnd());
        final Collection<Interval> overlapsRead1 = intervalOverlapDetector.getOverlaps(readInterval);

        //get read 2 interval and check for overlaps
        readInterval = new Interval(record.getMateReferenceName(), record.getMateAlignmentStart(), SAMUtils.getMateAlignmentEnd(record));
        final Collection<Interval> overlapsRead2 = intervalOverlapDetector.getOverlaps(readInterval);

        if (overlapsRead1.size() > 0 || overlapsRead2.size() > 0) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Determines whether a pair of SAMRecord matches this filter
     *
     * @param first  the first SAMRecord to evaluate
     * @param second the second SAMRecord to evaluate
     *
     * @return true if the SAMRecords matches the filter, otherwise false
     */
    public boolean filterOut(final SAMRecord first, final SAMRecord second) {
        throw new UnsupportedOperationException("IntervalKeepPairFilter(record) keeps both of a pair if either passes the filter.");
    }
}
