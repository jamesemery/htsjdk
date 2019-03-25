/*
 * The MIT License
 *
 * Copyright (c) 2018 The Broad Institute
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

package htsjdk.samtools.reference;

import htsjdk.samtools.util.BlockCompressedOutputStream;
import htsjdk.samtools.util.GZIIndex;
import htsjdk.samtools.util.IOUtil;
import htsjdk.utils.ValidationUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Buider for a {@link htsjdk.samtools.reference.FastaReferenceWriter}
 * <p>
 * You can set each of the three outputs (fasta, dictionary and index) to a file or a stream.
 * by default if you provide a file to the fasta an accompanying index and dictionary will be created.
 * This behaviour can be controlled by {@link #setMakeDictOutput(boolean)} and {@link #setMakeFaiOutput(boolean)}
 * <p>
 * The default bases-per-line is {@value FastaReferenceWriter#DEFAULT_BASES_PER_LINE}.
 * <p>
 * Setting a file or an output stream for any of the three outputs (fasta, index or dict) will invalidate the other
 * output type (i.e. setting a file output will invalidate a previous stream and vice-versa).
 * </p>
 */
public class FastaReferenceWriterBuilder {
    private Path fastaFile;
    private boolean gzippedFastaFile = false;
    private boolean makeGziOutput = true;
    private boolean makeFaiOutput = true;
    private boolean makeDictOutput = true;
    private boolean emitMd5 = true;
    private int basesPerLine = FastaReferenceWriter.DEFAULT_BASES_PER_LINE;
    private Path gziIndexFile;
    private Path faiIndexFile;
    private Path dictFile;
    private OutputStream fastaOutput;
    private OutputStream faiIndexOutput;
    private OutputStream gziIndexOutput;
    private OutputStream dictOutput;

    private static Path defaultGziFile(final boolean makeGziFile, final Path fastaFile) {
        return makeGziFile ? GZIIndex.resolveIndexNameForBgzipFile(fastaFile) : null;
    }

    private static Path defaultFaiFile(final boolean makeFaiFile, final Path fastaFile) {
        return makeFaiFile ? ReferenceSequenceFileFactory.getFastaIndexFileName(fastaFile) : null;
    }

    private static Path defaultDictFile(final boolean makeDictFile, final Path fastaFile) {
        return makeDictFile ? ReferenceSequenceFileFactory.getDefaultDictionaryForReferenceSequence(fastaFile) : null;
    }

    protected static int checkBasesPerLine(final int value) {
        ValidationUtils.validateArg(value > 0, "bases per line must be 1 or greater");
        return value;
    }

    /**
     * Set the output fasta file to write to. Doesn't (currently) support compressed filenames.
     * If the index file and output stream are both null and makeFaiOutput is true (default), a default index file will be created as well.
     * If the dictionary file and output stream are both null and makeDictOutput is true (default), a default dictionary file will be created as well.
     *
     * You can only provide a compressed stream to the fastaOutput, and only in the case that an index isn't written.
     *
     * @param fastaFile a {@link Path} to the output fasta file.
     * @return this builder
     */
    public FastaReferenceWriterBuilder setFastaFile(final Path fastaFile) {
        this.fastaFile = fastaFile;
        this.fastaOutput = null;
        if (IOUtil.hasGzipFileExtension(fastaFile)) {
            this.gzippedFastaFile = true;
            this.makeGziOutput = true;
        }
        return this;
    }

    /**
     * Sets whether to automatically generate an index file from the name of the fasta-file (assuming it is given
     * as a file). This can only happen if both the index file and output stream are null.
     *
     * @param makeFaiOutput a boolean flag
     * @return this builder
     */
    public FastaReferenceWriterBuilder setMakeFaiOutput(final boolean makeFaiOutput) {
        this.makeFaiOutput = makeFaiOutput;
        return this;
    }

    /**
     * Sets whether to automatically generate an dictionary file from the name of the fasta-file (assuming it is given
     * as a file). This can only happen if both the dictionary file and output stream are null.
     *
     * @param makeDictOutput a boolean flag
     * @return this builder
     */

    public FastaReferenceWriterBuilder setMakeDictOutput(final boolean makeDictOutput) {
        this.makeDictOutput = makeDictOutput;
        return this;
    }

    /**
     * Sets whether to automatically generate a gzi index file from the name of the fasta-file (assuming it is given
     * a valid BCF as a file). This can only happen if both the index file and output stream are null.
     *
     * @param makeGziOutput a boolean flag
     * @return this builder
     */

    public FastaReferenceWriterBuilder setMakeGziOutput(final boolean makeGziOutput) {
        this.makeGziOutput = makeGziOutput;
        return this;
    }

    /**
     * Sets the number of bases each line of the fasta file will have.
     * the default is {@value FastaReferenceWriter#DEFAULT_BASES_PER_LINE}
     *
     * @param basesPerLine integer (must be positive, validated on {@link #build()}) indicating the number of bases per line in
     *                     the output
     * @return this builder
     */
    public FastaReferenceWriterBuilder setBasesPerLine(final int basesPerLine) {
        this.basesPerLine = basesPerLine;
        return this;
    }

    /**
     * Set the output index file to write to. Doesn't (currently) support compressed filenames.
     */
    public FastaReferenceWriterBuilder setIndexFile(final Path faiIndexFile) {
        this.faiIndexFile = faiIndexFile;
        this.faiIndexOutput = null;
        return this;
    }

    /**
     * Set the output index file to write to. Doesn't (currently) support compressed filenames.
     */
    public FastaReferenceWriterBuilder setGziIndexFile(final Path gziIndexFile) {
        this.gziIndexFile = gziIndexFile;
        this.gziIndexOutput = null;
        return this;
    }

    /**
     * Set the output dictionary file to write to.
     */
    public FastaReferenceWriterBuilder setDictFile(final Path dictFile) {
        this.dictFile = dictFile;
        this.dictOutput = null;
        return this;
    }

    /**
     * Set the output stream for writing the reference. Doesn't support compressed streams.
     *
     * @param fastaOutput a {@link OutputStream} for the output fasta file.
     * @return this builder
     */

    public FastaReferenceWriterBuilder setFastaOutput(final OutputStream fastaOutput) {
        this.fastaOutput = fastaOutput;
        this.fastaFile = null;
        this.makeGziOutput = false;
        return this;
    }

    /**
     * Set the output stream for writing the index. Doesn't support compressed streams.
     *
     * @param faiIndexOutput a  {@link OutputStream} for the output index.
     * @return this builder
     */
    public FastaReferenceWriterBuilder setIndexOutput(final OutputStream faiIndexOutput) {
        this.faiIndexOutput = faiIndexOutput;
        this.faiIndexFile = null;
        return this;
    }

    /**
     * Set the output stream for writing the index. Doesn't support compressed streams.
     *
     * @param gziIndexOutput a  {@link OutputStream} for the gzi output index.
     * @return this builder
     */
    public FastaReferenceWriterBuilder setGziIndexOutput(final OutputStream gziIndexOutput) {
        this.gziIndexOutput = gziIndexOutput;
        this.gziIndexFile = null;
        return this;
    }

    /**
     * Set the output stream for writing the dictionary. Doesn't support compressed streams.
     *
     * @param dictOutput a {@link OutputStream} for the output dictionary.
     * @return this builder
     */
    public FastaReferenceWriterBuilder setDictOutput(final OutputStream dictOutput) {
        this.dictOutput = dictOutput;
        this.dictFile = null;
        return this;
    }

    /**
     * Create the {@link FastaReferenceWriter}. This is were all the validations happen:
     * <ld>
     * <li>
     * -One of fastaFile and fastaOutput must be non-null.
     * </li>
     * <li>
     * -the number of bases-per-line must be positive
     * </li>
     * </ld>
     *
     * @return a {@link FastaReferenceWriter}
     * @throws IOException if trouble opening files
     */
    public FastaReferenceWriter build() throws IOException {
        if (fastaFile == null && fastaOutput == null) {
            throw new IllegalArgumentException("Both fastaFile and fastaOutput were null. Please set one of them to be non-null.");
        }
        if(fastaFile != null) {
            if (faiIndexFile == null && faiIndexOutput == null) {
                faiIndexFile = defaultFaiFile(makeFaiOutput, fastaFile);
            } else if (faiIndexFile != null && faiIndexOutput != null) {
                throw new IllegalArgumentException("Both faiIndexFile and faiIndexOutput were non-null. Please set one of them to be null.");
            }
            if (dictFile == null && dictOutput == null) {
                dictFile = defaultDictFile(makeDictOutput, fastaFile);
            } else if (dictFile != null && dictOutput != null) {
                throw new IllegalArgumentException("Both dictFile and dictOutput were non-null. Please set one of them to be null.");
            }
            if (gzippedFastaFile && gziIndexFile == null && gziIndexOutput == null) {
                gziIndexFile = defaultGziFile(makeGziOutput, fastaFile);
            } else if (gziIndexFile != null && gziIndexOutput != null) {
                throw new IllegalArgumentException("Both dictFile and dictOutput were non-null. Please set one of them to be null.");
            } else if (!gzippedFastaFile && (gziIndexFile != null || gziIndexOutput != null)) {
                throw new IllegalArgumentException("Requested a gzi index but the output format fasta file was not a block compressed gzip file");
            }
                //TODO Except if usere requests fai && gzippedOutputStream && noGZI

        }
        // checkout bases-perline first, so that files are not created if failure;
        checkBasesPerLine(basesPerLine);

        if (gziIndexFile != null) {
            gziIndexOutput = new BufferedOutputStream(Files.newOutputStream(gziIndexFile));
        }
        if (fastaFile != null) {
            if (gzippedFastaFile) {
                fastaOutput = new BlockCompressedOutputStream(Files.newOutputStream(fastaFile), fastaFile);
                ((BlockCompressedOutputStream) fastaOutput).addIndexer(gziIndexOutput);
            } else {
                fastaOutput = new BufferedOutputStream(Files.newOutputStream(fastaFile));
            }
        }
        if (faiIndexFile != null) {
            faiIndexOutput = new BufferedOutputStream(Files.newOutputStream(faiIndexFile));
        }
        if (dictFile != null) {
            dictOutput = new BufferedOutputStream(Files.newOutputStream(dictFile));
        }

        return new FastaReferenceWriter(basesPerLine, emitMd5, fastaOutput, faiIndexOutput, dictOutput);
    }

    /**
     * @return whether the reference builder will emit M5 tag in the header lines
     */
    public boolean getEmitMd5() {
        return emitMd5;
    }

    /**
     * @param emitMd5 whether the reference builder will emit the M5 tag in the header line
     *      * (and populate it with the md5 digest of the sequence)
     * @return this builder
     */
    public FastaReferenceWriterBuilder setEmitMd5(final boolean emitMd5) {
        this.emitMd5 = emitMd5;
        return this;
    }
}
