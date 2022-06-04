/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ZipUtils {

	private ZipUtils() {
		/**
		 * Hidden constructor
		 */
	}

	/**
	 * Compresses the received String to a byte array.
	 *
	 * @param text the String to be compressed.
	 * @return the compressed byte array.
	 */
	public static byte[] zipText(String text) {
		ByteArrayOutputStream bosReturn;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); GZIPOutputStream zout = new GZIPOutputStream(bos)) {
			zout.write(text.getBytes(StandardCharsets.UTF_8));
			bosReturn = bos;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return bosReturn.toByteArray();
	}

	/**
	 * Decompress the received byte array to a String.
	 *
	 * @param zippedBytes the compressed bytes.
	 * @return the decompressed data as a String.
	 * @throws IOException
	 */
	public static String unzip(byte[] zippedBytes) throws IOException {

		try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(zippedBytes));
				BufferedReader bf = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8))) {

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bf.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();

		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
