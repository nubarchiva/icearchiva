package org.icearchiva.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;

public class LTAArchiveInputStream extends FilterInputStream {

	public static final String HASH_ALGORITHM = "SHA-256";

	private String hash;
	private Long size;
	private String mimeType;
	private String hashAlgorithm;
	
	MessageDigest digest;
	
	public LTAArchiveInputStream(InputStream is) {
		super(is);
        size = 0l;
        hash = null;
        hashAlgorithm = HASH_ALGORITHM;
        try {
			digest = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
        digest.reset();
	}
	
	public LTAArchiveInputStream(InputStream is, String hashAlgorithm) {
		super(is);
        size = 0l;
        hash = null;
        this.hashAlgorithm = hashAlgorithm;
        try {
			digest = MessageDigest.getInstance(hashAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
        digest.reset();
	}
	
	
	public String getHash() {
		return hash;
	}
	
	public Long getSize() {
		return size;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		int read = super.read(buffer);
		if (read != -1) {
			digest.update(buffer, 0, read);
			// Try to discover mime type only with the first data block
			if (size == 0) {
                this.mimeType = getMimeType(buffer);
			}
			size = size + read;
		} else {
			// -1 return can occur several times (!)
			if (hash == null) {
		        byte[] hashByteArray = digest.digest();
		        BigInteger bigInt = new BigInteger(1, hashByteArray);
		        hash = bigInt.toString(16);
			}
		}
		return read;
	}

	@Override
	public synchronized int read(byte[] buffer, int off, int len) throws IOException {
		int read = super.read(buffer, off, len);
		if (read != -1) {
			digest.update(buffer, 0, read);
			// Try to discover mime type only with the first data block
			if (size == 0) {
                this.mimeType = getMimeType(buffer);
			}
			size = size + read;
		} else {
			// -1 return can occur several times (!)
			if (hash == null) {
		        byte[] hashByteArray = digest.digest();
		        BigInteger bigInt = new BigInteger(1, hashByteArray);
		        hash = bigInt.toString(16);
			}
		}
		return read;
	}
	
    private static String getMimeType(byte[] data) {
		return getFirstMimeType(new MagicMimeMimeDetector().getMimeTypes(data));
	}

	private static String getFirstMimeType(Collection<?> mimeTypes) {
		if(mimeTypes.size() >= 1){
			return mimeTypes.iterator().next() + "";
		}else{
			return "";
		}
	}
	
}
