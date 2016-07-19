package htsjdk.samtools.filter;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordSetBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import htsjdk.samtools.util.Interval;
import java.util.List;
import java.util.ArrayList;


/**
 * Created by kbergin on 7/19/16.
 */
public class IntervalKeepPairFilterTest {
    private static final int READ_LENGTH = 151;
    private final SAMRecordSetBuilder builder = new SAMRecordSetBuilder();

    @BeforeTest
    public void setUp() {
        builder.setReadLength(READ_LENGTH);
        builder.addPair("mapped_pair_chr1", 0, 1, 151); //should be kept in first test, filtered out in third
        builder.addPair("mapped_pair_chr2", 1, 1, 151); //should be filtered out for first test, and kept in third
        builder.addPair("prove_one_of_pair", 0, 1000, 1000); //neither of these will be kept in any test
        builder.addPair("one_of_pair", 0, 1, 1000); //first read should pass, second should not, but both will be kept in first test
    }

    @Test(dataProvider = "data")
    public void testIntervalPairFilter(final List<Interval> intervals, final int expectedPassingRecords) {
        final IntervalKeepPairFilter filter = new IntervalKeepPairFilter(intervals, builder.getHeader());
        int actualPassingRecords = 0;
        for (final SAMRecord rec : builder) {
            if (!filter.filterOut(rec)) actualPassingRecords++;
        }
        Assert.assertEquals(actualPassingRecords, expectedPassingRecords);
    }

    @DataProvider(name = "data")
    private Object[][] testData() {
        Interval interval = new Interval("chr1", 1, 999);
        List<Interval> intervalList_twoPair = new ArrayList<>();
        intervalList_twoPair.add(interval);

        interval = new Interval("chr3", 1, 2);
        List<Interval> intervalList_noMatch = new ArrayList<>();
        intervalList_noMatch.add(interval);

        interval = new Interval("chr2", 1, 2);
        List<Interval> intervalList_onePair = new ArrayList<>();
        intervalList_onePair.add(interval);

        return new Object[][]{
                {intervalList_twoPair, 4},
                {intervalList_noMatch, 0},
                {intervalList_onePair, 2}
        };
    }
}
