package htsjdk.variant.vcf;


public class VCFPedigreeHeaderLine extends VCFCompoundHeaderLine{

    public VCFPedigreeHeaderLine(String name, int count, VCFHeaderLineType type, String description) {
        super(name, count, type, description, SupportedHeaderLineType.PEDIGREE);
        if (type == VCFHeaderLineType.Flag)
            throw new IllegalArgumentException("Flag is an unsupported type for pedigree fields");
    }

    public VCFPedigreeHeaderLine(String name, VCFHeaderLineCount count, VCFHeaderLineType type, String description) {
        super(name, count, type, description, VCFCompoundHeaderLine.SupportedHeaderLineType.FORMAT);
    }

    public VCFPedigreeHeaderLine(String line, VCFHeaderVersion version) {
        super(line, version, VCFCompoundHeaderLine.SupportedHeaderLineType.FORMAT);
    }

    // format fields do not allow flag values (that wouldn't make much sense, how would you encode this in the genotype).
    @Override
    boolean allowFlagValues() {
        return false;
    }

    @Override
    public boolean shouldBeAddedToDictionary() {
        return true;
    }
}
