package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.droppoint.service.SimpleFileContentCheckingService;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created by zaskanov on 13.04.2017.
 */
@Test
public class SimpleFileContentCheckingServiceTest {

    SimpleFileContentCheckingService service = new SimpleFileContentCheckingService();

    @Test
    public void testNonTextFile() throws URISyntaxException {
        File carImg = new File(this.getClass().getClassLoader()
                .getResource("ftpRoot/1level/2level/car.jpg").toURI());
        assertFalse(service.isFileContains(carImg, "test"));
    }

    @Test
    public void testTextFile() throws URISyntaxException {
        File report = new File(this.getClass().getClassLoader().getResource("ftpRoot/FinanceReport.csv").toURI());
        assertTrue(service.isFileContains(report, "finance"));

        File candidates = new File(this.getClass().getClassLoader().getResource("ftpRoot/candidates.txt").toURI());
        assertTrue(service.isFileContains(candidates, "john doE"));

        File notes = new File(this.getClass().getClassLoader().getResource("ftpRoot/1level/notes.xml").toURI());
        assertTrue(service.isFileContains(notes, "Tove"));
    }
}
