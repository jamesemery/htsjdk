package htsjdk.variant.vcf;

import htsjdk.tribble.TribbleException;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.variant.VariantBaseTest;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;



public class AbstractVCFCodecTest extends VariantBaseTest {

	@Test
	public void shouldPreserveSymbolicAlleleCase() {
		VCFFileReader reader = new VCFFileReader(new File(VariantBaseTest.variantTestDataRoot + "breakpoint.vcf"), false);
		VariantContext variant = reader.iterator().next();
		reader.close();
		
		// VCF v4.1 s1.4.5
		// Tools processing VCF files are not required to preserve case in the allele String, except for IDs, which are case sensitive.
		Assert.assertTrue(variant.getAlternateAllele(0).getDisplayString().contains("chr12"));
	}

	@Test
	public void TestSpanDelParseAlleles(){
		List<Allele> list = VCF3Codec.parseAlleles("A", Allele.SPAN_DEL_STRING, 0);
	}

	@Test(expectedExceptions = TribbleException.class)
	public void TestSpanDelParseAllelesException(){
		List<Allele> list1 = VCF3Codec.parseAlleles(Allele.SPAN_DEL_STRING, "A", 0);
	}

	@DataProvider(name="thingsToTryToDecode")
	public Object[][] getThingsToTryToDecode(){
		return new Object[][] {
				{"src/test/resources/htsjdk/tribble/tabix/testTabixIndex.vcf", true},
				{"src/test/resources/htsjdk/tribble/tabix/testTabixIndex.vcf.gz", true},
				{"src/test/resources/htsjdk/tribble/nonexistant.garbage", false},
				{"src/test/resources/htsjdk/tribble/testIntervalList.list", false}
		};
	}

	@Test(dataProvider = "thingsToTryToDecode")
	public void testCanDecodeFile(String potentialInput, boolean canDecode) {
		Assert.assertEquals(AbstractVCFCodec.canDecodeFile(potentialInput, VCFCodec.VCF4_MAGIC_HEADER), canDecode);
	}

	@Test
	public void testGetTabixFormat() {
		Assert.assertEquals(new VCFCodec().getTabixFormat(), TabixFormat.VCF);
		Assert.assertEquals(new VCF3Codec().getTabixFormat(), TabixFormat.VCF);
	}

	@Test (expectedExceptions = TribbleException.MalformedFeatureFile.class)
	public void testDuplicatedHeaderFieldCrash() {
		final File originalVCF = new File("src/test/resources/htsjdk/variant/HiSeq.10000.duplicatedField.vcf");
		final VCFFileReader reader = new VCFFileReader(originalVCF, false);
		final VCFHeader header = reader.getFileHeader();
		reader.close();
	}

	@Test (expectedExceptions = TribbleException.class)
	public void testDuplicatedInfoFieldCrash() {
		final File originalVCF = new File("src/test/resources/htsjdk/variant/ex2.duplicatedInfo.vcf");
		final VCFFileReader reader = new VCFFileReader(originalVCF, false);
		final VCFHeader header = reader.getFileHeader();
		reader.iterator().next(); // This should crash due to duplicated DB info tag
	}

	@Test
	public void testSpacesInInfoField() {
		final File originalVCF = new File("src/test/resources/htsjdk/variant/ex2.spacesInfoField.vcf");
		final VCFFileReader reader = new VCFFileReader(originalVCF, false);
		final VCFHeader header = reader.getFileHeader();
		reader.iterator().next(); // This should not crash despite having a space in the info field
	}
}
