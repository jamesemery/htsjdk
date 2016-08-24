package htsjdk.variant.vcf;

import htsjdk.tribble.TribbleException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VCFUtilsUnitTest {

    @Test (dataProvider = "percentEncodedCharsTest")
    public static void decodePercentEncodedCharsTest(String original, String expected) {
        String actual = VCFUtils.decodePercentEncodedChars(original);
        Assert.assertEquals(actual, expected,
                String.format("decodePercentEncodedChars produced invalid result decoding string '%s', expected string '%s' and received '%s'",original, expected, actual));
    }

    @Test (dataProvider = "percentEncodedCharsTestBadInput", expectedExceptions = TribbleException.VCFException.class )
    public static void decodePercentEncodedCharsTestBadInput(String original) {
        VCFUtils.decodePercentEncodedChars(original);
    }

    @DataProvider(name = "percentEncodedCharsTest")
    public Object[][] makePercentEncodedCharsTest() {
        final Object[][] tests = new Object[][] {
                {"ab cde","ab cde"},
                {"%41bcde","Abcde"},
                {"%2c%2524",",%24"},
                {"%41%42%43%44","ABCD"},
                {"%3B",";"},
                {"%3b",";"},
                {"",""}
        };
        return tests;
    }

    @DataProvider(name = "percentEncodedCharsTestBadInput")
    public Object[][] makePercentEncodedCharsTestBadInput() {
        final Object[][] tests = new Object[][] {
                {"%4"},//Test for incomplete end
                {"%%%%%"},//Test for non-single charater encoded stirng
                {"%FF"},//Test for codepoint >127
                {"40% of"},
        };
        return tests;
    }

    @DataProvider(name = "toPercentEncodingTest")
    public Object[][] makeToPercentEncodingTest() {
        final Object[][] tests = new Object[][] {
                {"",""},//Test for empty strings behaving
                {"abcd","abcd"},//Test for simple strings
                {", %24","%2C %2524"},//Test for mix of encoded and unencoded characters
                {"a,;:=%\n","a%2C%3B%3A%3D%25%0A"}, //test of most dissalowed characters
        };
        return tests;
    }

    @Test
    public void speedTest() {
        String s = "lalalalalalalalalalalala";
        System.out.println("["+VCFUtils.DANGEROUS_VCF_CHARACTERS.toString()+"]");
        Pattern p = VCFUtils.catchPattern;

        long nanotime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            p.matcher(s);
        }
        System.out.println("Matcher time: "+(System.nanoTime()-nanotime));
        nanotime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            for (char c : VCFUtils.DANGEROUS_VCF_CHARACTERS) {
                if (s.indexOf(c)>2);
            }
        }
        System.out.println("indexOf time: "+(System.nanoTime()-nanotime));
    }
}
