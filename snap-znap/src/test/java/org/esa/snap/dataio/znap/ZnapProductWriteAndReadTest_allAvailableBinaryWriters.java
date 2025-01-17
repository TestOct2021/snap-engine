/*
 * Copyright (c) 2021.  Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 *
 */

package org.esa.snap.dataio.znap;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.io.TreeDeleter;
import org.esa.snap.dataio.bigtiff.BigGeoTiffProductWriterPlugIn;
import org.esa.snap.dataio.envi.EnviProductWriterPlugIn;
import org.esa.snap.dataio.geotiff.GeoTiffProductWriterPlugIn;
import org.esa.snap.dataio.netcdf.metadata.profiles.cf.CfNetCdf4WriterPlugIn;
import org.esa.snap.runtime.Engine;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static com.bc.ceres.core.ProgressMonitor.NULL;
import static org.esa.snap.dataio.znap.ZnapConstantsAndUtils.ZNAP_CONTAINER_EXTENSION;
import static org.esa.snap.dataio.znap.preferences.ZnapPreferencesConstants.PROPERTY_NAME_BINARY_FORMAT;
import static org.esa.snap.dataio.znap.preferences.ZnapPreferencesConstants.PROPERTY_NAME_USE_ZIP_ARCHIVE;
import static org.junit.Assert.*;


public class ZnapProductWriteAndReadTest_allAvailableBinaryWriters {

    private Path testPath;
    private Product dummy;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Engine.start();
    }

    @Before
    public void setUp() throws Exception {
        testPath = Files.createTempDirectory(getClass().getCanonicalName());
        dummy = createDummyProduct();
    }

    @After
    public void tearDown() throws IOException {
        TreeDeleter.deleteDir(testPath);
//        Files.walk(testPath).sorted(Collections.reverseOrder()).forEach(path -> {
//            path.toFile().deleteOnExit();
//            try {
//                Files.delete(path);
//            } catch (IOException ignore) {
//                System.out.println(ignore.getMessage());
//            }
//        });
    }

    @Test
    public void testWriteAndRead_withBinaryWriter_ENVI() throws IOException {
        //preparation
        final Properties properties = new Properties();
        properties.put(PROPERTY_NAME_USE_ZIP_ARCHIVE, "false");
        properties.put(PROPERTY_NAME_BINARY_FORMAT, EnviProductWriterPlugIn.FORMAT_NAME);
        final ZnapProductWriter writer = new ZnapProductWriter(new ZnapProductWriterPlugIn(), properties);
        writer.setPreferencesForTestPurposesOnly(properties);
        final Band band = dummy.getBand("band");

        //execution
        try {
            writer.writeProductNodes(dummy, testPath.resolve("filename"));
            writer.writeBandRasterData(band, 0, 0, 2, 2, band.getData(), NULL);
        } finally {
            writer.close();
        }

        //verification
        final Path rootDir = testPath.resolve("filename" + ZNAP_CONTAINER_EXTENSION);
        assertTrue(Files.isDirectory(rootDir));
        assertTrue(Files.isDirectory(rootDir.resolve("band")));
        assertTrue(Files.isDirectory(rootDir.resolve("band").resolve("band")));
        assertTrue(Files.isRegularFile(rootDir.resolve("band").resolve("band").resolve("data.img")));
        assertTrue(Files.isRegularFile(rootDir.resolve("band").resolve("band").resolve("data.hdr")));

        final ZnapProductReader reader = new ZnapProductReader(new ZnapProductReaderPlugIn());
        try {
            final Product product = reader.readProductNodes(rootDir, null);
            final Band readInBand = product.getBand("band");
            readInBand.readRasterDataFully();
            final int[] ints = (int[]) readInBand.getData().getElems();
            assertNotNull(ints);
            assertArrayEquals(new int[]{12, 13, 14, 15}, ints);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testWriteAndRead_withBinaryWriter_GeoTIFF() throws IOException {
        //preparation
        final Properties properties = new Properties();
        properties.put(PROPERTY_NAME_USE_ZIP_ARCHIVE, "false");
        properties.put(PROPERTY_NAME_BINARY_FORMAT, GeoTiffProductWriterPlugIn.GEOTIFF_FORMAT_NAME);
        final ZnapProductWriter writer = new ZnapProductWriter(new ZnapProductWriterPlugIn(), properties);
        writer.setPreferencesForTestPurposesOnly(properties);
        final Band band = dummy.getBand("band");

        //execution
        try {
            writer.writeProductNodes(dummy, testPath.resolve("filename"));
            writer.writeBandRasterData(band, 0, 0, 2, 2, band.getData(), NULL);
        } finally {
            writer.close();
        }

        //verification
        final Path rootDir = testPath.resolve("filename" + ZNAP_CONTAINER_EXTENSION);
        assertTrue(Files.isDirectory(rootDir));
        assertTrue(Files.isDirectory(rootDir.resolve("band")));
        assertTrue(Files.isRegularFile(rootDir.resolve("band").resolve("band.tif")));

        final ZnapProductReader reader = new ZnapProductReader(new ZnapProductReaderPlugIn());
        try {
            final Product product = reader.readProductNodes(rootDir, null);
            final Band readInBand = product.getBand("band");
            readInBand.readRasterDataFully();
            final int[] ints = (int[]) readInBand.getData().getElems();
            assertNotNull(ints);
            assertArrayEquals(new int[]{12, 13, 14, 15}, ints);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testWriteAndRead_withBinaryWriter_GeoTIFF_BigTIFF() throws IOException {
        //preparation
        final Properties properties = new Properties();
        properties.put(PROPERTY_NAME_USE_ZIP_ARCHIVE, "false");
        properties.put(PROPERTY_NAME_BINARY_FORMAT, BigGeoTiffProductWriterPlugIn.FORMAT_NAME);
        final ZnapProductWriter writer = new ZnapProductWriter(new ZnapProductWriterPlugIn(), properties);
        writer.setPreferencesForTestPurposesOnly(properties);
        final Band band = dummy.getBand("band");

        //execution
        try {
            writer.writeProductNodes(dummy, testPath.resolve("filename"));
            writer.writeBandRasterData(band, 0, 0, 2, 2, band.getData(), NULL);
        } finally {
            writer.close();
        }

        //verification
        final Path rootDir = testPath.resolve("filename" + ZNAP_CONTAINER_EXTENSION);
        assertTrue(Files.isDirectory(rootDir));
        assertTrue(Files.isDirectory(rootDir.resolve("band")));
        assertTrue(Files.isRegularFile(rootDir.resolve("band").resolve("band.tif")));

        final ZnapProductReader reader = new ZnapProductReader(new ZnapProductReaderPlugIn());
        try {
            final Product product = reader.readProductNodes(rootDir, null);
            final Band readInBand = product.getBand("band");
            readInBand.readRasterDataFully();
            final int[] ints = (int[]) readInBand.getData().getElems();
            assertNotNull(ints);
            assertArrayEquals(new int[]{12, 13, 14, 15}, ints);
        } finally {
            reader.close();
        }
    }

    @Test
    public void testWriteAndRead_withBinaryWriter_NetCDF4_CF() throws IOException {
        //preparation
        final Properties properties = new Properties();
        properties.put(PROPERTY_NAME_USE_ZIP_ARCHIVE, "false");
        properties.put(PROPERTY_NAME_BINARY_FORMAT, new CfNetCdf4WriterPlugIn().getFormatNames()[0]);
        final ZnapProductWriter writer = new ZnapProductWriter(new ZnapProductWriterPlugIn(), properties);
        writer.setPreferencesForTestPurposesOnly(properties);
        final Band band = dummy.getBand("band");

        //execution
        try {
            writer.writeProductNodes(dummy, testPath.resolve("filename"));
            writer.writeBandRasterData(band, 0, 0, 2, 2, band.getData(), NULL);
        } finally {
            writer.close();
        }

        //verification
        final Path rootDir = testPath.resolve("filename" + ZNAP_CONTAINER_EXTENSION);
        assertTrue(Files.isDirectory(rootDir));
        assertTrue(Files.isDirectory(rootDir.resolve("band")));
        assertTrue(Files.isRegularFile(rootDir.resolve("band").resolve("band.nc")));

        final ZnapProductReader reader = new ZnapProductReader(new ZnapProductReaderPlugIn());
        try {
            final Product product = reader.readProductNodes(rootDir, null);
            final Band readInBand = product.getBand("band");
            readInBand.readRasterDataFully();
            final int[] ints = (int[]) readInBand.getData().getElems();
            assertNotNull(ints);
            assertArrayEquals(new int[]{12, 13, 14, 15}, ints);
        } finally {
            reader.close();
        }
    }

    private Product createDummyProduct() {
        final Product targetProduct = new Product("name", "type");
        Band band = new Band("band", ProductData.TYPE_INT32, 2, 2);
        band.setData(ProductData.createInstance(new int[]{12, 13, 14, 15}));
        targetProduct.addBand(band);
        return targetProduct;
    }
}